/**
 * Created by kanari on 2016/8/1.
 */

package util;

import logic.AIPlayer;
import logic.AbstractPlayer;
import logic.LocalPlayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Random;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class PreferenceLoader {
	private static final Properties defaultProperties = new Properties() {{
		try {
			setProperty("p1profileName", InetAddress.getLocalHost().getHostName());
			setProperty("p1avatarID", "gomoku.jpg");
			setProperty("p1uniqueID", String.valueOf(new Random().nextInt()));
			setProperty("p1playerType", "local");
			setProperty("p1timeLimit", "20");

			setProperty("p2profileName", "Stupid AI");
			setProperty("p2avatarID", "think.gif");
			setProperty("p2uniqueID", String.valueOf(new Random().nextInt()));
			setProperty("p2playerType", "AI");
			setProperty("p2timeLimit", "1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}};

	public static Properties loadFromPreferences(String prefix) {
		Properties properties = new Properties();
		Preferences preferences = Preferences.userNodeForPackage(PreferenceLoader.class);
		Consumer<String> setProperty = suffix -> {
			String name = prefix + suffix;
			properties.setProperty(suffix, preferences.get(name, defaultProperties.getProperty(name)));
		};
		setProperty.accept("profileName");
		setProperty.accept("avatarID");
		setProperty.accept("uniqueID");
		setProperty.accept("playerType");
		setProperty.accept("timeLimit");
		return properties;
	}

	public static void saveToPreferences(Properties properties, String prefix) {
		Preferences preferences = Preferences.userNodeForPackage(PreferenceLoader.class);
		Consumer<String> setPreference = suffix -> {
			String name = prefix + suffix;
			preferences.put(name, properties.getProperty(suffix));
		};
		setPreference.accept("profileName");
		setPreference.accept("avatarID");
		setPreference.accept("uniqueID");
		setPreference.accept("playerType");
		setPreference.accept("timeLimit");
	}

	public static AbstractPlayer playerFromProperties(Properties properties) {
		String name = properties.getProperty("profileName");
		String avatarID = properties.getProperty("avatarID");
		if (properties.getProperty("playerType").equals("local")) {
			return new LocalPlayer(name, avatarID);
		} else {
			return new AIPlayer(name, avatarID, 1);
		}
	}
}
