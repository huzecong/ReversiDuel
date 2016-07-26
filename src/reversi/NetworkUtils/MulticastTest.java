/**
 * Created by kanari on 2016/7/26.
 */

package NetworkUtils;

import java.net.InetAddress;
import java.util.Date;

public class MulticastTest {
	public static void main(String[] args) {
		MulticastManager manager = new MulticastManager(s -> {
		}, () -> new Date().toString() + "\n" + "Somebody" + "\n" + "gomoku.jpg" + "\n" + 233 + "\n"
				+ InetAddress.getLocalHost() + "\n" + "ReversiDuel");
		manager.startHost();
	}
}
