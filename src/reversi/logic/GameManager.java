/**
 * Created by kanari on 2016/7/28.
 */

package logic;


import util.Pair;

import java.awt.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GameManager {
	private static final int N = 8;
	private PlayerState[][] gameBoard;
	private PlayerState currentPlayer;
	private int roundCount;
	private boolean gameStarted;

	public static PlayerState flip(PlayerState player) {
		if (player == PlayerState.NONE) return PlayerState.NONE;
		if (player == PlayerState.BLACK) return PlayerState.WHITE;
		return PlayerState.BLACK;
	}

	private Consumer<Pair<Point, PlayerState>> dropPieceHandler;

	public Consumer<Pair<Point, PlayerState>> getDropPieceHandler() {
		return dropPieceHandler;
	}

	public void setDropPieceHandler(Consumer<Pair<Point, PlayerState>> dropPieceHandler) {
		this.dropPieceHandler = dropPieceHandler;
	}

	private AbstractPlayer blackPlayer, whitePlayer;

	public void init(AbstractPlayer black, AbstractPlayer white) {
		this.blackPlayer = black;
		this.whitePlayer = white;
		black.setManager(new GameManagerInterface(this, PlayerState.BLACK));
		white.setManager(new GameManagerInterface(this, PlayerState.WHITE));
		roundCount = 0;
		gameStarted = false;
	}

	public void newGame() {
		++roundCount;
		currentPlayer = PlayerState.BLACK;
		gameBoard = new PlayerState[N][N];
		for (int i = 0; i < N; ++i)
			Arrays.fill(gameBoard[i], PlayerState.NONE);
		flippedPositions.clear();
		candidatePositions.clear();

		gameBoard[3][3] = PlayerState.WHITE;
		gameBoard[3][4] = PlayerState.BLACK;
		gameBoard[4][3] = PlayerState.BLACK;
		gameBoard[4][4] = PlayerState.WHITE;
		dropPieceHandler.accept(Pair.of(new Point(3, 3), PlayerState.WHITE));
		dropPieceHandler.accept(Pair.of(new Point(3, 4), PlayerState.BLACK));
		dropPieceHandler.accept(Pair.of(new Point(4, 3), PlayerState.BLACK));
		dropPieceHandler.accept(Pair.of(new Point(4, 4), PlayerState.WHITE));
		updateCandidatePositions();
		gameStarted = true;
		blackPlayer.newGame();
		whitePlayer.newGame();
	}

	public PlayerState getCurrentPlayer() {
		return currentPlayer;
	}

	private ArrayList<Point> candidatePositions = new ArrayList<>();

	public Collection<Point> getCandidatePositions() {
		return Collections.unmodifiableList(candidatePositions);
	}

	private boolean isCandidatePosition(int x, int y, PlayerState player) {
		if (gameBoard[x][y] != PlayerState.NONE) return false;
		for (int dx = -1; dx <= 1; ++dx)
			for (int dy = -1; dy <= 1; ++dy) {
				if (dx == 0 && dy == 0) continue;
				boolean existsPiece = false;
				boolean shouldFlip = false;
				int tx = x, ty = y;
				while (isValid(tx + dx, ty + dy)) {
					tx += dx;
					ty += dy;
					PlayerState state = getState(tx, ty);
					if (state == player) {
						existsPiece = true;
						break;
					}
					else if (state == PlayerState.NONE) {
						shouldFlip = false;
						break;
					} else shouldFlip = true;
				}
				if (!existsPiece) shouldFlip = false;
				if (shouldFlip) return true;
			}
		return false;
	}

	private void updateCandidatePositions() {
		candidatePositions.clear();
		for (int x = 0; x < N; ++x)
			for (int y = 0; y < N; ++y)
				if (isCandidatePosition(x, y, currentPlayer))
					candidatePositions.add(new Point(x, y));
	}

	public boolean canDrop(int x, int y) {
		return gameBoard[x][y] == PlayerState.NONE && candidatePositions.contains(new Point(x, y));
	}

	private boolean isValid(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	ArrayList<Point> flippedPositions = new ArrayList<>();

	public Collection<Point> getFlippedPositions() {
		return Collections.unmodifiableList(flippedPositions);
	}

	public void dropPiece(int x, int y, PlayerState player) {
		if (currentPlayer != player) return;
		gameBoard[x][y] = player;

		flippedPositions.clear();
		for (int dx = -1; dx <= 1; ++dx)
			for (int dy = -1; dy <= 1; ++dy) {
				if (dx == 0 && dy == 0) continue;
				boolean existsPiece = false;
				boolean shouldFlip = false;
				int tx = x, ty = y;
				while (isValid(tx + dx, ty + dy)) {
					tx += dx;
					ty += dy;
					PlayerState state = getState(tx, ty);
					if (state == player) {
						existsPiece = true;
						break;
					}
					else if (state == PlayerState.NONE) {
						shouldFlip = false;
						break;
					} else shouldFlip = true;
				}
				if (!existsPiece) shouldFlip = false;
				if (shouldFlip) {
					for (int i = x + dx, j = y + dy; i != tx || j != ty; i += dx, j += dy) {
						gameBoard[i][j] = flip(gameBoard[i][j]);
						flippedPositions.add(new Point(i, j));
					}
				}
			}

		dropPieceHandler.accept(Pair.of(new Point(x, y), player));

		Optional<PlayerState> winner = checkWinner();
		if (!winner.isPresent()) {
			currentPlayer = flip(currentPlayer);
			updateCandidatePositions();
			if (player == PlayerState.BLACK) whitePlayer.informOpponentMove(new Point(x, y));
			else blackPlayer.informOpponentMove(new Point(x, y));
		} else {
			PlayerState result = winner.get();
			gameStarted = false;
			blackPlayer.gameOver(result == PlayerState.BLACK, result == PlayerState.NONE);
			whitePlayer.gameOver(result == PlayerState.WHITE, result == PlayerState.NONE);
		}
	}

	private Optional<PlayerState> checkWinner() {
		int blackCnt = 0, whiteCnt = 0;
		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j)
				switch (gameBoard[i][j]) {
					case BLACK:
						++blackCnt;
						break;
					case WHITE:
						++whiteCnt;
						break;
					case NONE:
						return Optional.empty();
				}
		if (blackCnt > whiteCnt) return Optional.of(PlayerState.BLACK);
		else if (blackCnt < whiteCnt) return Optional.of(PlayerState.WHITE);
		else return Optional.of(PlayerState.NONE);
	}

	public PlayerState getState(int x, int y) {
		return gameBoard[x][y];
	}
}
