/**
 * Created by kanari on 2016/7/29.
 */

package logic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

	private BooleanProperty canUndo = new SimpleBooleanProperty(false);

	public BooleanProperty canUndoProperty() {
		return canUndo;
	}

	public boolean canUndo() {
		return manager.canUndo(player);
	}

	public boolean requestUndo() {
		boolean result = manager.requestUndo(player);
		if (result) canUndo.set(canUndo());
		return result;
	}

	public boolean requestDraw() {
		return manager.requestDraw(player);
	}

	public boolean requestSurrender() {
		return manager.requestSurrender(player);
	}

	public boolean requestExit() {
		return manager.requestExit(player);
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
		canUndo.set(canUndo());
		return true;
	}
}
