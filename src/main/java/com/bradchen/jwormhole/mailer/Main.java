package com.bradchen.jwormhole.mailer;

import com.bradchen.jwormhole.client.console.ConsoleUI;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Properties;

import static com.bradchen.jwormhole.client.SettingsUtils.readSettingsFromClassPathResource;
import static com.bradchen.jwormhole.client.SettingsUtils.readSettingsFromFileRelativeToHome;

public final class Main {

	// in class path
	private static final String DEFAULT_SETTINGS_FILE = "mailer.default.properties";

	// relative to $HOME
	private static final String OVERRIDE_SETTINGS_FILE = ".jwormhole/mailer.properties";

	public static void main(String[] args) throws IOException, ParseException {
		Properties defaultSettings = getDefaultSettings();
		Properties overrideSettings = getOverrideSettings();
		MailerSettings settings = new MailerSettings(defaultSettings, overrideSettings);
		ConsoleUI console = new ConsoleUI(args);
		console.addCommandHandler(new MailCommandHandler(settings));
		console.run();
	}

	private static Properties getDefaultSettings() throws IOException {
		return readSettingsFromClassPathResource(DEFAULT_SETTINGS_FILE);
	}

	private static Properties getOverrideSettings() throws IOException {
		return readSettingsFromFileRelativeToHome(OVERRIDE_SETTINGS_FILE);
	}

}
