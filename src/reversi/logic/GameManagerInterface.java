/**
 * Created by kanari on 2016/7/29.
 */

package logic;

import javafx.beans.property.BooleanProperty;
import util.Synchronous;

public class GameManagerInterface {
	private GameManager manager;
	private PlayerState player;

	public GameManagerInterface(GameManager manager, PlayerState player) {
		this.manager = manager;
		this.player = player;
	}

	public void sendChat(String message) {

	}

	public boolean isReady() {
		return manager.isReady(player);
	}

	public void ready() {
		manager.ready(player);
	}

	public boolean canUndo() {
		return false;
	}

	public boolean requestUndo() {
		return manager.requestUndo(player);
	}

	public boolean requestDraw() {
		return false;
	}

	public boolean requestSurrender() {
		return true;
	}

	public boolean requestExit() {
		return false;
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
