/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import javafx.beans.property.BooleanProperty;

import java.awt.*;

public abstract class AbstractPlayer {
	protected GameManagerInterface manager;
	protected String profileName, avatarID;

	public AbstractPlayer(String profileName, String avatarID) {
		this.profileName = profileName;
		this.avatarID = avatarID;
	}

	public String getProfileName() {
		return profileName;
	}

	public String getAvatarID() {
		return avatarID;
	}

	public void setManager(GameManagerInterface manager) {
		this.manager = manager;
	}

	public boolean isMyTurn() {
		return manager.isMyTurn();
	}

	public BooleanProperty canUndoProperty() {
		return manager.canUndoProperty();
	}

	/**
	 * Starts a new game.
	 * Player should initialize self. If the player plays first (i.e. BLACK), it should make its move.
	 * @param state
	 *     The color of the player in this game.
	 */
	public abstract void newGame(PlayerState state);

	/**
	 * The player is informed of the opponent's move, and starts the player's round.
	 * If {@code isSkipped} is true, then current player does not have playable moves, and its round is skipped.
	 * Otherwise, player should present its move using {@code GameManagerInterface}.
	 * @param point
	 *     Coordinates of opponent's piece.
	 *     Note that if opponent was skipped, {@code point} is {@code null}, so make sure to check it.
	 * @param isSkipped
	 *     Whether the player is skipped due to having no playable moves.
	 */
	public abstract void informOpponentMove(Point point, boolean isSkipped);

	/**
	 * Declares the end of game, and informs results.
	 * Note that next game does not start until {@code newGame()} is called.
	 */
	public abstract void gameOver(boolean isWinner, boolean isTie);

	/**
	 * Called when the opponent sent out corresponding requests.
	 * @return Whether the player accepts the request.
	 */
	public abstract boolean undoRequested();

	public abstract boolean drawRequested();

	public abstract boolean surrenderRequested();

	public abstract boolean exitRequested();
}
