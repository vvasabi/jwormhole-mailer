package com.bradchen.jwormhole.mailer;

import com.bradchen.jwormhole.client.Client;
import com.bradchen.jwormhole.client.console.commands.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class ResendCommand extends Command {

	private Mailer mailer;

	public ResendCommand(Mailer mailer) {
		this.mailer = mailer;
	}

	@Override
	public String getName() {
		return "resend";
	}

	@Override
	public String getDescription() {
		return "Resend the last sent mail.";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("rs");
	}

	@Override
	public List<Argument> getArguments() {
		return Arrays.asList(new Argument("recipients", "a command-separated list of recipients",
			true));
	}

	@Override
	public void handle(Client client, ArgumentsList argumentsList) {
		String recipients = argumentsList.getValue("recipients");
		if (StringUtils.isBlank(mailer.getSubject()) || StringUtils.isBlank(mailer.getTemplateUrl())
				|| (mailer.getRecipients().isEmpty()) && StringUtils.isBlank(recipients)) {
			System.err.println("There is no previously sent mail.");
			return;
		}

		if (StringUtils.isBlank(recipients)) {
			mailer.sendMail();
		} else {
			mailer.sendMail(Arrays.asList(recipients.split(",")));
		}
	}

}
