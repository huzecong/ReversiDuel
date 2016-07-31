/**
 * Created by kanari on 2016/7/27.
 */

package util;

import java.io.IOException;

public class InvalidFormatException extends IOException {
	public InvalidFormatException() {
		super("Invalid format");
	}

	public InvalidFormatException(String message) {
		super(message);
	}
}
