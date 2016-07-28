/**
 * Created by kanari on 2016/7/29.
 */

package logic;

public class GameManagerInterface {
	private GameManager manager;
	private PlayerState player;

	public GameManagerInterface(GameManager manager, PlayerState player) {
		this.manager = manager;
		this.player = player;
	}

	public boolean requestUndo() {
		return false;
	}

	public boolean requestSurrender() {
		return true;
	}

	public boolean canDrop(int x, int y) {
		return manager.canDrop(x, y);
	}

	public boolean isMyTurn() {
		return manager.getCurrentPlayer() == player;
	}

	public boolean dropPiece(int x, int y) {
		if (!isMyTurn() || !canDrop(x, y)) return false;
		manager.dropPiece(x, y, player);
		return true;
	}
}
