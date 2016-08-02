/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.awt.Point;
import java.util.function.*;

public class LocalPlayer extends AbstractPlayer {
	private BiConsumer<String, String> infoDialogCaller;
	private BiFunction<String, String, Boolean> confirmDialogCaller;
	private Function<String, Runnable> waitDialogCaller;

	public LocalPlayer(String profileName, String avatarID) {
		super(profileName, avatarID);
	}

	public void setInfoDialogCaller(BiConsumer<String, String> infoDialogCaller) {
		this.infoDialogCaller = infoDialogCaller;
	}

	public void setConfirmDialogCaller(BiFunction<String, String, Boolean> confirmDialogCaller) {
		this.confirmDialogCaller = confirmDialogCaller;
	}

	public void setWaitDialogCaller(Function<String, Runnable> waitDialogCaller) {
		this.waitDialogCaller = waitDialogCaller;
	}

	@Override
	public void newGame(PlayerState state) {
		canUndo.set(false);
	}

	@Override
	public void informOpponentMove(Point point, boolean isSkipped, boolean isTimeout) {
		canUndo.set(manager.canUndo());
	}

	@Override
	public void gameOver(boolean isWinner, boolean isTie) {
		// does nothing
	}

	@Override
	public void purge() {
		// does nothing
	}

	@Override
	public void opponentIsReady() {
		// does nothing
	}

	public void ready() {
		manager.ready();
	}

	private BooleanProperty canUndo = new SimpleBooleanProperty(false);

	public BooleanProperty canUndoProperty() {
		return canUndo;
	}

	public void requestUndo() {
		boolean result;
		if (waitDialogCaller != null) {
			Runnable dismissCaller = waitDialogCaller.apply("Undo requested");
			result = manager.requestUndo();
			dismissCaller.run();
			if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your undo request");
		} else result = manager.requestUndo();
		if (result) canUndo.set(manager.canUndo());
	}

	public void requestDraw() {
		if (waitDialogCaller != null) {
			Runnable dismissCaller = waitDialogCaller.apply("Draw requested");
			boolean result = manager.requestDraw();
			dismissCaller.run();
			if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your draw request");
		} else manager.requestDraw();
	}

	public void requestSurrender() {
		if (waitDialogCaller != null) {
			Runnable dismissCaller = waitDialogCaller.apply("Surrender requested");
			boolean result = manager.requestSurrender();
			dismissCaller.run();
			if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your surrender request");
		} else manager.requestSurrender();
	}

	public void requestExit() {
		if (waitDialogCaller != null) {
			Runnable dismissCaller = waitDialogCaller.apply("Exit requested");
			boolean result = manager.requestExit();
			dismissCaller.run();
			if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your exit request");
		} else manager.requestExit();
	}

	public void sendChat(String message) {
		manager.sendChat(message);
	}

	@Override
	public boolean undoRequested() {
		boolean result = confirmDialogCaller.apply("Confirm request", "Your opponent wants to undo last move");
		if (result) canUndo.set(manager.canUndo());
		return result;
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

	@Override
	public void receivedChat(String message) {
		// does nothing
	}

	public void dropPiece(Point point) {
		manager.dropPiece(point.x, point.y);
		canUndo.set(manager.canUndo());
	}
}
