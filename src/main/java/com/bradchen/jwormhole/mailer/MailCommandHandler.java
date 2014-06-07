package com.bradchen.jwormhole.mailer;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MailCommandHandler implements CommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailCommandHandler.class);
	private static final String SMTP = "smtp";
	private static final String SMTPS = "smtps";
	private static final Charset UTF8_CHARSET = Charset.forName("utf-8");
	private static final String HTML_CONTENT_TYPE = "text/html; charset=" + UTF8_CHARSET.name();

	private final List<String> recipients;
	private final MailerSettings settings;
	private final Session session;
	private String subject;
	private String templateUrl;

	public MailCommandHandler(MailerSettings settings) {
		this.settings = settings;
		this.session = createSession(settings);
		this.recipients = new ArrayList<>();
	}

	@Override
	public List<String> getCommandHints() {
		List<String> hints = new ArrayList<>();
		hints.add("mail <subject> <template-url> <recipient> to send mail");
		if ((subject != null) && (templateUrl != null) && !recipients.isEmpty()) {
			hints.add("rs to resend last mail");
		}
		return hints;
	}

	@Override
	public boolean handle(Client client, String command) {
		String[] tokens = command.split(" ");
		if ("mail".equals(tokens[0]) && (tokens.length == 4)) {
			subject = tokens[1];
			templateUrl = tokens[2];
			recipients.clear();
			recipients.addAll(Arrays.asList(tokens[3].split(",")));
			sendMail();
			return true;
		}

		if ("rs".equals(tokens[0]) && (tokens.length == 1)) {
			sendMail();
			return true;
		}
		return false;
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
