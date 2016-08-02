/**
 * Created by kanari on 2016/7/28.
 */

package util;

import java.util.Timer;
import java.util.TimerTask;

public class TaskScheduler {
	private static Timer timer;

	private static Timer getTimer() {
		if (timer == null) timer = new Timer();
		try {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
				}
			}, 1);
		} catch (IllegalStateException e) {
			timer = new Timer();
		}
		return timer;
	}

	public static TimerTask singleShot(long delayMillis, Runnable task) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
		getTimer().schedule(timerTask, delayMillis);
		return timerTask;
	}

	public static TimerTask repeated(long periodMillis, Runnable task) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
		getTimer().scheduleAtFixedRate(timerTask, periodMillis, periodMillis);
		return timerTask;
	}
}
