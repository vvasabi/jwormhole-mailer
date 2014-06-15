package com.bradchen.jwormhole.mailer;

import com.bradchen.jwormhole.client.console.ConsolePlugin;
import com.bradchen.jwormhole.client.console.commands.CommandFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.bradchen.jwormhole.client.SettingsUtils.readSettingsFromClassPathResource;

public class MailerConsolePlugin implements ConsolePlugin {

	// in class path
	private static final String DEFAULT_SETTINGS_FILE = "mailer.default.properties";

	private Mailer mailer;

	@Override
	public void configure(Properties overrideSettings, String server) {
		this.mailer = new Mailer(new MailerSettings(getDefaultSettings(), overrideSettings));
	}

	private static Properties getDefaultSettings() {
		try {
			return readSettingsFromClassPathResource(MailCommandFactory.class.getClassLoader(),
				DEFAULT_SETTINGS_FILE);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public List<CommandFactory> getCommandFactories() {
		List<CommandFactory> factories = new ArrayList<>(2);
		factories.add(new MailCommandFactory(mailer));
		factories.add(new ResendCommandFactory(mailer));
		return factories;
	}

}
