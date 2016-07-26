/**
 * Created by kanari on 2016/7/26.
 */

package Network;

import com.sun.media.sound.InvalidFormatException;
import com.sun.tools.javac.util.Pair;

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


	public static Optional<Pair<HostData, String>> parseSignaturedMessage(String data) {
		if (data == null) return Optional.empty();
		String[] parts = data.split(Pattern.quote(separator));
		if (parts.length != 7) return Optional.empty();

		String profileName, avatarID;
		InetAddress IP;
		int uniqueID;
		Date date;

		if (!parts[6].equals("ReversiDuel")) return Optional.empty();
		try {
			date = new Date(Long.parseLong(parts[0]));
			profileName = parts[1];
			avatarID = parts[2];
			uniqueID = Integer.parseInt(parts[3]);
			IP = InetAddress.getByName(parts[4]);
		} catch (Exception e) {
			return Optional.empty();
		}

		return Optional.of(Pair.of(new HostData(profileName, avatarID, uniqueID, IP, date), parts[5]));
	}

	public static Optional<HostData> parseSignaturedMessage(String data, String message) {
		Optional<Pair<HostData, String>> result = parseSignaturedMessage(data);
		if (!result.isPresent()) return Optional.empty();
		if (!result.get().snd.equals(message)) return Optional.empty();
		return Optional.of(result.get().fst);
	}

	public static Optional<String> parseSignaturedMessage(String data, HostData hostData) {
		Optional<Pair<HostData, String>> result = parseSignaturedMessage(data);
		if (!result.isPresent()) return Optional.empty();
		if (!result.get().fst.equals(hostData)) return Optional.empty();
		return Optional.of(result.get().snd);
	}

	public static Pair<HostData, String> parseSignaturedMessageWithException(String data) throws InvalidFormatException {
		Optional<Pair<HostData, String>> result = parseSignaturedMessage(data);
		if (!result.isPresent()) throw new InvalidFormatException("Signatured message has invalid format.");
		return result.get();
	}

	public static HostData parseSignaturedMessageWithException(String data, String message) throws InvalidFormatException {
		Optional<HostData> hostData = parseSignaturedMessage(data, message);
		if (!hostData.isPresent()) throw new InvalidFormatException("Signatured message has invalid format.");
		return hostData.get();
	}

	public static String parseSignaturedMessageWithException(String data, HostData hostData) throws InvalidFormatException {
		Optional<String> message = parseSignaturedMessage(data, hostData);
		if (!message.isPresent()) throw new InvalidFormatException("Signatured message has invalid format.");
		return message.get();
	}
}
