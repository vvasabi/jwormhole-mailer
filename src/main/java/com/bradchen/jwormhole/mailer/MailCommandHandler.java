package com.bradchen.jwormhole.mailer;

import au.com.bytecode.opencsv.CSVReader;
import com.bradchen.jwormhole.client.Client;
import com.bradchen.jwormhole.client.console.CommandHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.bradchen.jwormhole.client.SettingsUtils.readSettingsFromClassPathResource;

public class MailCommandHandler implements CommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailCommandHandler.class);

	// in class path
	private static final String DEFAULT_SETTINGS_FILE = "mailer.default.properties";
	private static final String SMTP = "smtp";
	private static final String SMTPS = "smtps";
	private static final Charset UTF8_CHARSET = Charset.forName("utf-8");
	private static final String HTML_CONTENT_TYPE = "text/html; charset=" + UTF8_CHARSET.name();

	private final List<String> recipients;
	private MailerSettings settings;
	private Session session;
	private String subject;
	private String templateUrl;

	public MailCommandHandler() {
		this.recipients = new ArrayList<>();
	}

	@Override
	public void configure(Properties overrideSettings, String server) {
		this.settings = new MailerSettings(getDefaultSettings(), overrideSettings);
		this.session = createSession(settings);
	}

	private static Properties getDefaultSettings() {
		try {
			return readSettingsFromClassPathResource(MailCommandHandler.class.getClassLoader(),
				DEFAULT_SETTINGS_FILE);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public List<String> getCommandHints() {
		List<String> hints = new ArrayList<>();
		hints.add("mail <subject> <template-url> <recipient> to send mail");
		if ((subject != null) && (templateUrl != null) && !recipients.isEmpty()) {
			hints.add("rs to resend the last mail");
		}
		return hints;
	}

	@Override
	public boolean handle(Client client, String command) {
		InputStream in = new ByteArrayInputStream(command.getBytes(UTF8_CHARSET));
		CSVReader reader = new CSVReader(new InputStreamReader(in), ' ');
		String[] tokens;
		try {
			tokens = reader.readNext();
			if (tokens == null) {
				LOGGER.warn("Unable to parse command.");
				return false;
			}
		} catch (IOException exception) {
			LOGGER.warn("Unable to parse command.", exception);
			return false;
		} finally {
			IOUtils.closeQuietly(reader);
		}

		if ("mail".equals(tokens[0])) {
			if (tokens.length != 4) {
				System.err.println("Usage: mail <subject> <template-url> <recipient>");
				return true;
			}

			subject = trimQuotes(tokens[1]);
			templateUrl = trimQuotes(tokens[2]);
			recipients.clear();
			recipients.addAll(Arrays.asList(trimQuotes(tokens[3]).split(",")));
			sendMail();
			return true;
		}

		if ("rs".equals(tokens[0])) {
			if ((tokens.length != 1) && (tokens.length != 2)) {
				System.err.println("Usage: rs [recipient]");
				return true;
			}
			if (tokens.length == 2) {
				recipients.clear();
				recipients.addAll(Arrays.asList(trimQuotes(tokens[1]).split(",")));
			}
			sendMail();
			return true;
		}
		return false;
	}

	private static String trimQuotes(String str) {
		return str.trim().replaceAll("^\"|\"$", "");
	}

	private void sendMail() {
		String template = downloadTemplateFile();
		if (template == null) {
			return;
		}

		int count = 0;
		Transport transport = null;
		try {
			transport = createTransport();
			for (String recipientEmail : recipients) {
				Address recipient = createAddress(recipientEmail, null);
				transport.sendMessage(createMimeMessage(recipient, template),
					new Address[] { recipient });
				count++;
			}
		} catch (AuthenticationFailedException exception) {
			LOGGER.error("Authentication failed.", exception);
		} catch (AddressException exception) {
			LOGGER.error("Invalid address.", exception);
		} catch (MessagingException exception) {
			LOGGER.error("Error occurred when building message.", exception);
		} finally {
			closeTransportQuietly(transport);
		}
		System.out.println("Mail sent to " + count + " recipient(s).");
	}

	private Message createMimeMessage(Address recipient, String template)
			throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(createAddress(settings.getFromEmail(), settings.getFromName()));
		message.addRecipient(Message.RecipientType.TO, recipient);
		message.setSubject(subject);
		message.setContent(template, HTML_CONTENT_TYPE);
		return message;
	}

	private String downloadTemplateFile() {
		InputStream in = null;
		try {
			in = new URL(templateUrl).openStream();
			StringBuilder sb = new StringBuilder();
			byte[] bytes = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(bytes)) != -1) {
				if (bytesRead == 0) {
					continue;
				}
				sb.append(new String(bytes, 0, bytesRead, UTF8_CHARSET));
			}
			return sb.toString();
		} catch (IOException exception) {
			LOGGER.warn("Unable to download email template file.", exception);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private Transport createTransport() throws AuthenticationFailedException {
		try {
			String protocol = settings.isSmtps() ? SMTPS : SMTP;
			Transport transport = session.getTransport(protocol);
			transport.connect(settings.getServer(), settings.getPort(), settings.getUsername(),
				settings.getPassword());
			return transport;
		} catch (AuthenticationFailedException exception) {
			throw exception;
		} catch (MessagingException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static Address createAddress(String email, String name) throws AddressException {
		if (StringUtils.isBlank(name)) {
			return new InternetAddress(email);
		}
		try {
			return new InternetAddress(email, name, UTF8_CHARSET.name());
		} catch (UnsupportedEncodingException ignored) {
			return new InternetAddress(email);
		}
	}

	private static Session createSession(MailerSettings settings) {
		String protocol = settings.isSmtps() ? SMTPS : SMTP;
		boolean auth = StringUtils.isNotBlank(settings.getUsername())
				|| StringUtils.isNotBlank(settings.getPassword());
		Properties properties = new Properties();
		properties.put("mail." + protocol + ".auth", auth);
		properties.put("mail." + protocol + ".starttls.enable", settings.isStartTlsEnabled());
		return Session.getDefaultInstance(properties);
	}

	private static void closeTransportQuietly(Transport transport) {
		if (transport == null) {
			return;
		}
		try {
			transport.close();
		} catch (MessagingException ignored) {
		}
	}

}
