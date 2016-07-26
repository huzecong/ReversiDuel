/**
 * Created by kanari on 2016/7/26.
 */

package NetworkUtils;

public class MulticastTest {
	public static void main(String[] args) {
		MulticastManager manager = new MulticastManager(s -> {
		}, () -> "");
		manager.startHost();
	}
}
