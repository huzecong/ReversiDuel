/**
 * Created by kanari on 2016/7/26.
 */

package util;

import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronous<T> {
	AtomicBoolean hasSet;
	volatile T value;

	public Synchronous() {
		hasSet = new AtomicBoolean(false);
	}

	public boolean isSet() {
		return hasSet.get();
	}

	public void reset() {
		hasSet.set(false);
	}

	public void setValue(T newValue) {
		value = newValue;
		hasSet.set(true);
	}

	public T getValue(int timeout) {
		Thread thread = new Thread(new Helper(hasSet, timeout));
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!hasSet.get()) return null;
		return value;
	}

	public T getValue() {
		return getValue(0);
	}

	private class Helper implements Runnable {
		AtomicBoolean watchValue;
		int timeout;

		Helper(AtomicBoolean watchValue, int timeout) {
			this.watchValue = watchValue;
			this.timeout = timeout;
		}

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			while (!watchValue.get() && (timeout == 0 || System.currentTimeMillis() - time <= timeout)) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
