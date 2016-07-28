/**
 * Created by kanari on 2016/7/28.
 */

package util;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class TaskScheduler {
	private static Timer timer = new Timer();

	public static void singleShot(Duration delay, Runnable task) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		}, delay.toMillis());
	}

	public static void singleShot(long delayMillis, Runnable task) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		}, delayMillis);
	}
}
