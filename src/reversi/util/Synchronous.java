/**
 * Created by kanari on 2016/7/26.
 */

package util;

import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronous<T> {
	final AtomicBoolean hasSet;
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
		synchronized (hasSet) {
			value = newValue;
			hasSet.set(true);
			hasSet.notifyAll();
		}
	}

	public T getValue(int timeout) {
		synchronized (hasSet) {
			try {
				hasSet.wait(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!hasSet.get()) return null;
		return value;
	}

	public T getValue() {
		return getValue(0);
	}
}
