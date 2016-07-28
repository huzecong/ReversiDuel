/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import java.awt.*;

public class LocalPlayer extends AbstractPlayer {
	@Override
	protected void newGame() {
		// does nothing
	}

	@Override
	protected void informOpponentMove(Point point) {
		// does nothing
	}

	@Override
	protected void gameOver(boolean isWinner, boolean isTie) {
		// does nothing
	}

	public boolean dropPiece(Point point) {
		return manager.dropPiece(point.x, point.y);
	}
}
