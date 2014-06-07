package com.bradchen.jwormhole.mailer;

import com.bradchen.jwormhole.client.SettingsUtils;

import java.util.Properties;

public class MailerSettings {

	private static final String SETTING_PREFIX = "jwormhole.mailer";

	private final String server;
	private final int port;
	private final String username;
	private final String password;
	private final boolean smtps;
	private final boolean startTls;
	private final String fromName;
	private final String fromEmail;

	public MailerSettings(Properties defaults, Properties overrides) {
		server = getSetting(defaults, overrides, "server");
		port = getSettingInteger(defaults, overrides, "port");
		username = getSetting(defaults, overrides, "username");
		password = getSetting(defaults, overrides, "password");
		smtps = getSettingBoolean(defaults, overrides, "smtps");
		startTls = getSettingBoolean(defaults, overrides, "startTls");
		fromName = getSetting(defaults, overrides, "fromName");
		fromEmail = getSetting(defaults, overrides, "fromEmail");
	}

	private static boolean getSettingBoolean(Properties defaults, Properties overrides,
											 String key) {
		String setting = SettingsUtils.getSetting(defaults, overrides, SETTING_PREFIX, null, key);
		return Boolean.parseBoolean(setting);
	}

	private static int getSettingInteger(Properties defaults, Properties overrides, String key) {
		String setting = SettingsUtils.getSetting(defaults, overrides, SETTING_PREFIX, null, key);
		return Integer.parseInt(setting);
	}

	private static String getSetting(Properties defaults, Properties overrides, String key) {
		return SettingsUtils.getSetting(defaults, overrides, SETTING_PREFIX, null, key);
	}

	public String getServer() {
		return server;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isSmtps() {
		return smtps;
	}

	public boolean isStartTlsEnabled() {
		return startTls;
	}

	public String getFromName() {
		return fromName;
	}

	public String getFromEmail() {
		return fromEmail;
	}

}
