/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import java.awt.*;

public class AIPlayer extends AbstractPlayer {

	public AIPlayer(String profileName, String avatarID) {
		super(profileName, avatarID);
	}

	@Override
	public void newGame(PlayerState state) {

	}

	@Override
	public void informOpponentMove(Point point, boolean isSkipped) {

	}

	@Override
	public void gameOver(boolean isWinner, boolean isTie) {

	}

	@Override
	public boolean undoRequested() {
		return false;
	}

	@Override
	public boolean drawRequested() {
		return false;
	}

	@Override
	public boolean surrenderRequested() {
		return false;
	}

	@Override
	public boolean exitRequested() {
		return false;
	}

	public void ready() {
		manager.ready();
	}
}
