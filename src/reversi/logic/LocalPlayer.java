/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import java.awt.Point;
import java.util.function.*;

public class LocalPlayer extends AbstractPlayer {
	private BiConsumer<String, String> infoDialogCaller;
	private BiFunction<String, String, Boolean> confirmDialogCaller;
	private Consumer<String> waitDialogCaller;
	private Runnable waitDialogDismisser;

	public LocalPlayer(String profileName, String avatarID) {
		super(profileName, avatarID);
	}

	public void setInfoDialogCaller(BiConsumer<String, String> infoDialogCaller) {
		this.infoDialogCaller = infoDialogCaller;
	}

	public void setConfirmDialogCaller(BiFunction<String, String, Boolean> confirmDialogCaller) {
		this.confirmDialogCaller = confirmDialogCaller;
	}

	public void setWaitDialogCaller(Consumer<String> waitDialogCaller) {
		this.waitDialogCaller = waitDialogCaller;
	}

	public void setWaitDialogDismisser(Runnable waitDialogDismisser) {
		this.waitDialogDismisser = waitDialogDismisser;
	}

	@Override
	public void newGame(PlayerState state) {
		// does nothing
	}

	@Override
	public void informOpponentMove(Point point, boolean isSkipped, boolean isTimeout) {
		// does nothing
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

	public void requestUndo() {
		waitDialogCaller.accept("Undo requested");
		boolean result = manager.requestUndo();
		waitDialogDismisser.run();
		if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your undo request");
	}

	public void requestDraw() {
		waitDialogCaller.accept("Draw requested");
		boolean result = manager.requestDraw();
		waitDialogDismisser.run();
		if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your draw request");
	}

	public void requestSurrender() {
		waitDialogCaller.accept("Surrender requested");
		boolean result = manager.requestSurrender();
		waitDialogDismisser.run();
		if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your surrender request");
	}

	public void requestExit() {
		waitDialogCaller.accept("Exit requested");
		boolean result = manager.requestExit();
		waitDialogDismisser.run();
		if (!result) infoDialogCaller.accept("Request refused", "Your opponent refused your exit request");
	}

	public void sendChat(String message) {
		manager.sendChat(message);
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

	@Override
	public void receivedChat(String message) {
		// give it to UI
	}

	public boolean dropPiece(Point point) {
		return manager.dropPiece(point.x, point.y);
	}
}
