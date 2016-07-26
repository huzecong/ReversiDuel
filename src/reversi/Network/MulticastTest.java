/**
 * Created by kanari on 2016/7/26.
 */

package Network;

import java.net.InetAddress;
import java.util.Date;

public class MulticastTest {
	public static void main(String[] args) {
		MulticastManager manager = new MulticastManager(System.out::println, () ->
				new Date().getTime() + "\n" + "Somebody" + "\n" + "gomoku.jpg" + "\n" + 233 + "\n"
						+ InetAddress.getLocalHost().getHostAddress() + "\n" + "ReversiDuel");
		manager.startHost();
	}
}
