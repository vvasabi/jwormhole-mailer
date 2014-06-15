package com.bradchen.jwormhole.mailer;

import com.bradchen.jwormhole.client.console.commands.Command;
import com.bradchen.jwormhole.client.console.commands.CommandFactory;

public class MailCommandFactory implements CommandFactory {

	private final Mailer mailer;

	public MailCommandFactory(Mailer mailer) {
		this.mailer = mailer;
	}

	@Override
	public Command createCommand() {
		return new MailCommand(mailer);
	}

}
