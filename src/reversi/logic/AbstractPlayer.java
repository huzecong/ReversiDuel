/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import java.awt.*;

public abstract class AbstractPlayer {
	protected GameManagerInterface manager;

	public void setManager(GameManagerInterface manager) {
		this.manager = manager;
	}

	public boolean isMyTurn() {
		return manager.isMyTurn();
	}

	/**
	 * Starts a new game, player should initialize self.
	 * If the player plays first, it should make its move.
	 */
	protected abstract void newGame();

	/**
	 * The player is informed of the opponent's move, and starts the player's round.
	 * Player should present its move using {@code GameManagerInterface}.
	 */
	protected abstract void informOpponentMove(Point point);

	/**
	 * Declares the end of game, and informs results.
	 * Note that next game does not start until {@code newGame()} is called.
	 */
	protected abstract void gameOver(boolean isWinner, boolean isTie);
}
