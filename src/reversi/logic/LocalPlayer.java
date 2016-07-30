/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class LocalPlayer extends AbstractPlayer {
	BiConsumer<String, String> infoDialogCaller;
	BiFunction<String, String, Boolean> confirmDialogCaller;

	public LocalPlayer(String profileName, String avatarID) {
		super(profileName, avatarID);
	}

	public void setInfoDialogCaller(BiConsumer<String, String> infoDialogCaller) {
		this.infoDialogCaller = infoDialogCaller;
	}

	public void setConfirmDialogCaller(BiFunction<String, String, Boolean> confirmDialogCaller) {
		this.confirmDialogCaller = confirmDialogCaller;
	}

	@Override
	public void newGame(PlayerState state) {
		// does nothing
	}

	@Override
	public void informOpponentMove(Point point, boolean isSkipped) {
		// does nothing
	}

	@Override
	public void gameOver(boolean isWinner, boolean isTie) {
		// does nothing
	}

	public void ready() {
		manager.ready();
	}

	public void requestUndo() {
		manager.requestUndo();
	}

	public void requestDraw() {
		manager.requestDraw();
	}

	public void requestSurrender() {
		manager.requestSurrender();
	}

	public void requestExit() {
		manager.requestExit();
	}

	@Override
	public boolean undoRequested() {
		return confirmDialogCaller.apply("Confirm request", "Your opponent wants to undo last move");
	}

	@Override
	public boolean drawRequested() {
		return confirmDialogCaller.apply("Confirm request", "Your opponent wants to declare a draw");
	}

	@Override
	public boolean surrenderRequested() {
		return confirmDialogCaller.apply("Confirm request", "Your opponent wants to declare defeat");
	}

	@Override
	public boolean exitRequested() {
		return confirmDialogCaller.apply("Confirm request", "Your opponent wants to quit this match");
	}

	public boolean dropPiece(Point point) {
		return manager.dropPiece(point.x, point.y);
	}
}
