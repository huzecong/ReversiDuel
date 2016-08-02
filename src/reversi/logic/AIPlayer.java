/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer extends AbstractPlayer {

	private final int N = 8;
	private int difficulty;

	public AIPlayer(String profileName, String avatarID, int difficulty) {
		super(profileName, avatarID);
		this.difficulty = difficulty;
	}

	private void makeMove() {
		allowUndo = true;
		List<Point> candidatePositions = manager.getCandidatePositions();
		assert candidatePositions.size() > 0;

		if (difficulty == 1) {
			ArrayList<Point> considers = new ArrayList<>();
			for (Point p : candidatePositions)
				if (isCornerPoint(p)) considers.add(p);
			if (considers.size() == 0) {
				for (Point p : candidatePositions)
					if (isEdgePoint(p)) considers.add(p);
				if (considers.size() == 0) {
					int flipCount = 0;
					for (Point p : candidatePositions) {
						int count = manager.getFlippedPositions(p.x, p.y).size();
						if (count > flipCount) considers.clear();
						if (count >= flipCount) considers.add(p);
					}
				}
			}
			Point p = considers.get(new Random().nextInt(considers.size()));
			manager.dropPiece(p.x, p.y);
		} else if (difficulty == 0) {
			Point p = candidatePositions.get(new Random().nextInt(candidatePositions.size()));
			manager.dropPiece(p.x, p.y);
		}
	}

	private boolean isCornerPoint(Point point) {
		return (point.x == 0 || point.x == N - 1) && (point.y == 0 || point.y == N - 1);
	}

	private boolean isEdgePoint(Point point) {
		return (point.x == 0 || point.x == N - 1) || (point.y == 0 || point.y == N - 1);
	}

	@Override
	public void newGame(PlayerState state) {
		if (state == PlayerState.BLACK)
			makeMove();
	}

	@Override
	public void informOpponentMove(Point point, boolean isSkipped, boolean isTimeout) {
		if (!isSkipped) makeMove();
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

	private boolean allowUndo;

	@Override
	public boolean undoRequested() {
		if (allowUndo) {
			allowUndo = false;
			return true;
		} else return false;
	}

	@Override
	public boolean drawRequested() {
		return false;
	}

	@Override
	public boolean surrenderRequested() {
		return true;
	}

	@Override
	public boolean exitRequested() {
		return true;
	}

	@Override
	public void receivedChat(String message) {
		// does nothing
	}

	public boolean requestExit() {
		return manager.requestExit();
	}

	public void ready() {
		manager.ready();
	}

	public void sendChat(String text) {
		manager.sendChat(text);
	}
}
