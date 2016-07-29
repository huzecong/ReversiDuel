/**
 * Created by kanari on 2016/7/28.
 */

package util;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class TaskScheduler {

	public static void singleShot(Duration delay, Runnable task) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		}, delay.toMillis());
	}

	public static void singleShot(long delayMillis, Runnable task) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		}, delayMillis);
	}
}
