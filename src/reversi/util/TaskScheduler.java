/**
 * Created by kanari on 2016/7/28.
 */

package util;

import java.util.Timer;
import java.util.TimerTask;

public class TaskScheduler {
	private static Timer timer = new Timer();

	public static TimerTask singleShot(long delayMillis, Runnable task) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
		timer.schedule(timerTask, delayMillis);
		return timerTask;
	}

	public static TimerTask repeated(long periodMillis, Runnable task) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
		timer.scheduleAtFixedRate(timerTask, periodMillis, periodMillis);
		return timerTask;
	}
}
