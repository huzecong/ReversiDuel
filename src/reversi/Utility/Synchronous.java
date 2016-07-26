/**
 * Created by kanari on 2016/7/26.
 */

package Utility;

import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronous<T> {
	AtomicBoolean hasSet;
	T value;

	public Synchronous() {
		hasSet = new AtomicBoolean(false);
	}

	public void setValue(T newValue) {
		value = newValue;
		hasSet.set(true);
	}

	public T getValue() {
		Thread thread = new Thread(new Helper(hasSet));
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return value;
	}

	private class Helper implements Runnable {
		AtomicBoolean watchValue;

		Helper(AtomicBoolean watchValue) {
			this.watchValue = watchValue;
		}

		@Override
		public void run() {
			while (!watchValue.get()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
