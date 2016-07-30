/**
 * Created by kanari on 2016/7/28.
 */

package util;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class TaskScheduler {

	public static Timer singleShot(long delayMillis, Runnable task) {
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
		timer.schedule(timerTask, delayMillis);
		return timer;
	}

	public static Timer repeated(long periodMillis, Runnable task) {
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
		timer.scheduleAtFixedRate(timerTask, periodMillis, periodMillis);
		return timer;
	}
}
