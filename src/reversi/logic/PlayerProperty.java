/**
 * Created by kanari on 2016/7/29.
 */

package logic;

public class PlayerProperty<T> {
	private T black, white;

	public PlayerProperty() {
	}

	public PlayerProperty(T black, T white) {
		this.black = black;
		this.white = white;
	}

	public T getBlack() {
		return black;
	}

	public void setBlack(T black) {
		this.black = black;
	}

	public T getWhite() {
		return white;
	}

	public void setWhite(T white) {
		this.white = white;
	}

	public T get(PlayerState player) {
		if (player == PlayerState.WHITE) return white;
		if (player == PlayerState.BLACK) return black;
		return null;
	}

	public void set(PlayerState player, T value) {
		if (player == PlayerState.WHITE) white = value;
		if (player == PlayerState.BLACK) black = value;
	}

	public void swap() {
		T _tmp = black;
		black = white;
		white = _tmp;
	}
}
