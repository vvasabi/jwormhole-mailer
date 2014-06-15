package com.bradchen.jwormhole.mailer;

import com.bradchen.jwormhole.client.Client;
import com.bradchen.jwormhole.client.console.commands.Command;

import java.util.Arrays;
import java.util.List;

public class MailCommand extends Command {

	private Mailer mailer;

	public MailCommand(Mailer mailer) {
		this.mailer = mailer;
	}

	@Override
	public String getName() {
		return "mail";
	}

	@Override
	public String getDescription() {
		return "Send a mail.";
	}

	@Override
	public List<String> getAliases() {
		return null;
	}

	@Override
	public List<Argument> getArguments() {
		return Arrays.asList(new Argument("subject", "subject of the mail"),
			new Argument("template-url", "url to mail template"),
			new Argument("recipients", "a comma-separated list of recipient mail addresses"));
	}

	@Override
	public void handle(Client client, ArgumentsList argumentsList) {
		String subject = argumentsList.getValue("subject");
		String templateUrl = argumentsList.getValue("template-url");
		String recipients = argumentsList.getValue("recipients");
		mailer.sendMail(subject, templateUrl, Arrays.asList(recipients.split(",")));
	}

}
