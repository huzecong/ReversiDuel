/**
 * Created by kanari on 2016/7/29.
 */

package logic;

class PlayerProperty<T> {
	private T black, white;

	PlayerProperty() {
	}

	PlayerProperty(T black, T white) {
		this.black = black;
		this.white = white;
	}

	T getBlack() {
		return black;
	}

	void setBlack(T black) {
		this.black = black;
	}

	T getWhite() {
		return white;
	}

	void setWhite(T white) {
		this.white = white;
	}

	T get(PlayerState player) {
		if (player == PlayerState.WHITE) return white;
		if (player == PlayerState.BLACK) return black;
		return null;
	}

	void set(PlayerState player, T value) {
		if (player == PlayerState.WHITE) white = value;
		if (player == PlayerState.BLACK) black = value;
	}

	void swap() {
		T _tmp = black;
		black = white;
		white = _tmp;
	}
}
