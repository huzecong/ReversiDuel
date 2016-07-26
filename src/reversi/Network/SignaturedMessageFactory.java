/**
 * Created by kanari on 2016/7/26.
 */

package Network;

import com.sun.media.sound.InvalidFormatException;

import java.net.InetAddress;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

public class SignaturedMessageFactory {
	private static String separator = "\t";     // should not be line separators

	public static String createSignaturedMessage(HostData data, String message) {
		return new Date().getTime() + separator + data.getProfileName() + separator + data.getAvatarID() + separator
				+ data.getUniqueID() + separator + data.getIP() + separator + message + separator + "ReversiDuel";
	}

	public static Optional<HostData> parseSignaturedMessage(String data, String message) {
		String[] parts = data.split(Pattern.quote(separator));
		if (parts.length != 7) return Optional.empty();

		String profileName, avatarID;
		InetAddress IP;
		int uniqueID;
		Date date;

		if (!parts[6].equals("ReversiDuel")) return Optional.empty();
		if (!parts[5].equals(message)) return Optional.empty();
		try {
			date = new Date(Long.parseLong(parts[0]));
			profileName = parts[1];
			avatarID = parts[2];
			uniqueID = Integer.parseInt(parts[3]);
			IP = InetAddress.getByName(parts[4]);
		} catch (Exception e) {
			return Optional.empty();
		}

		return Optional.of(new HostData(profileName, avatarID, uniqueID, IP, date));
	}

	public static HostData parseSignaturedMessageWithException(String data, String message) throws InvalidFormatException {
		Optional<HostData> hostData = parseSignaturedMessage(data, message);
		if (!hostData.isPresent()) throw new InvalidFormatException("Signatured message has invalid format.");
		return hostData.get();
	}

	public static Optional<String> parseSignaturedMessage(String data, HostData hostData) {
		String[] parts = data.split(Pattern.quote(separator));
		if (parts.length != 7) return Optional.empty();

		if (!parts[6].equals("ReversiDuel")) return Optional.empty();
		try {
			new Date(Long.parseLong(parts[0]));
			if (!parts[1].equals(hostData.getProfileName())) return Optional.empty();
			if (!parts[2].equals(hostData.getAvatarID())) return Optional.empty();
			if (Integer.parseInt(parts[3]) != hostData.getUniqueID()) return Optional.empty();
			if (!parts[4].equals(hostData.getIP())) return Optional.empty();
		} catch (Exception e) {
			return Optional.empty();
		}

		return Optional.of(parts[5]);
	}

	public static String parseSignaturedMessageWithException(String data, HostData hostData) throws InvalidFormatException {
		Optional<String> message = parseSignaturedMessage(data, hostData);
		if (!message.isPresent()) throw new InvalidFormatException("Signatured message has invalid format.");
		return message.get();
	}
}
