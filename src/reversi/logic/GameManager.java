/**
 * Created by kanari on 2016/7/28.
 */

package logic;


import util.Pair;

import java.awt.*;
import java.util.*;

public class GameManager {
	private static final int N = 8;
	private PlayerState[][] gameBoard;
	private PlayerState currentPlayer;
	private int roundCount;
	private boolean gameStarted;

	/**
	 * Helper functions
	 */
	public static PlayerState flip(PlayerState player) {
		if (player == PlayerState.NONE) return PlayerState.NONE;
		if (player == PlayerState.BLACK) return PlayerState.WHITE;
		return PlayerState.BLACK;
	}

	public AbstractPlayer getPlayer() {
		return players.get(currentPlayer);
	}

	/**
	 * UI events
	 */
	public interface DropPieceHandler {
		public void handle(Point point, PlayerState player, Collection<Point> flippedPositions);
	}

	private DropPieceHandler dropPieceHandler;

	public DropPieceHandler getDropPieceHandler() {
		return dropPieceHandler;
	}

	public void setDropPieceHandler(DropPieceHandler dropPieceHandler) {
		this.dropPieceHandler = dropPieceHandler;
	}

	/**
	 * Main game flow
	 */
	private PlayerProperty<AbstractPlayer> players = new PlayerProperty<>();
	private PlayerProperty<GameManagerInterface> interfaces = new PlayerProperty<>();

	public void init(AbstractPlayer black, AbstractPlayer white) {
		players.setBlack(black);
		players.setWhite(white);
		interfaces.setBlack(new GameManagerInterface(this, PlayerState.BLACK));
		interfaces.setWhite(new GameManagerInterface(this, PlayerState.WHITE));
		black.setManager(interfaces.getBlack());
		white.setManager(interfaces.getWhite());
		roundCount = 0;
		gameStarted = false;
	}

	public void newGame() {
		++roundCount;
		moves = new ArrayList<>();
		currentPlayer = PlayerState.BLACK;
		gameBoard = new PlayerState[N][N];
		for (int i = 0; i < N; ++i)
			Arrays.fill(gameBoard[i], PlayerState.NONE);
		candidatePositions.clear();

		gameBoard[3][3] = PlayerState.WHITE;
		gameBoard[3][4] = PlayerState.BLACK;
		gameBoard[4][3] = PlayerState.BLACK;
		gameBoard[4][4] = PlayerState.WHITE;
		dropPieceHandler.handle(new Point(3, 3), PlayerState.WHITE, new ArrayList<>());
		dropPieceHandler.handle(new Point(3, 4), PlayerState.BLACK, new ArrayList<>());
		dropPieceHandler.handle(new Point(4, 3), PlayerState.BLACK, new ArrayList<>());
		updateCandidatePositions();
		dropPieceHandler.handle(new Point(4, 4), PlayerState.WHITE, new ArrayList<>());
		gameStarted = true;
		players.getBlack().newGame(PlayerState.BLACK);
		players.getWhite().newGame(PlayerState.WHITE);
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
					} else if (state == PlayerState.NONE) {
						shouldFlip = false;
						break;
					} else shouldFlip = true;
				}
				if (!existsPiece) shouldFlip = false;
				if (shouldFlip) return true;
			}
		return false;
	}

	private void updateCandidatePositions(PlayerState player) {
		candidatePositions.clear();
		for (int x = 0; x < N; ++x)
			for (int y = 0; y < N; ++y)
				if (isCandidatePosition(x, y, player))
					candidatePositions.add(new Point(x, y));
	}

	private void updateCandidatePositions() {
		updateCandidatePositions(currentPlayer);
	}

	public void dropPiece(int x, int y, PlayerState player) {
		if (currentPlayer != player) return;
		gameBoard[x][y] = player;

		ArrayList<Point> flipped = new ArrayList<>();
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
					} else if (state == PlayerState.NONE) {
						shouldFlip = false;
						break;
					} else shouldFlip = true;
				}
				if (!existsPiece) shouldFlip = false;
				if (shouldFlip) {
					for (int i = x + dx, j = y + dy; i != tx || j != ty; i += dx, j += dy) {
						gameBoard[i][j] = flip(gameBoard[i][j]);
						flipped.add(new Point(i, j));
					}
				}
			}

		Point point = new Point(x, y);
		Collection<Point> flippedPositions = Collections.unmodifiableList(flipped);
		dropPieceHandler.handle(point, player, flippedPositions);
		moves.add(Pair.of(Pair.of(point, player), flippedPositions));

		Optional<PlayerState> winner = checkWinner();
		if (!winner.isPresent()) {
			currentPlayer = flip(currentPlayer);
			// candidate positions already updated in checkWinner()
			boolean isSkipped = candidatePositions.size() == 0;
			players.get(flip(player)).informOpponentMove(point, isSkipped);
		} else {
			PlayerState result = winner.get();
			gameOver(result);
		}
	}

	private void gameOver(PlayerState result) {
		gameStarted = false;
		players.getBlack().gameOver(result == PlayerState.BLACK, result == PlayerState.NONE);
		players.getWhite().gameOver(result == PlayerState.WHITE, result == PlayerState.NONE);

		AbstractPlayer _tmp = players.getBlack();
		players.setBlack(players.getWhite());
		players.setWhite(_tmp);
		players.getBlack().setManager(interfaces.getBlack());
		players.getWhite().setManager(interfaces.getWhite());
		isReady.setBlack(false);
		isReady.setWhite(false);
	}

	private Optional<PlayerState> checkWinner() {
		boolean gameOver = true;
		updateCandidatePositions();
		if (candidatePositions.size() > 0) gameOver = false;
		updateCandidatePositions(flip(currentPlayer));
		if (candidatePositions.size() > 0) gameOver = false;
		if (!gameOver) return Optional.empty();

		int blackCnt = getPieces(PlayerState.BLACK), whiteCnt = getPieces(PlayerState.WHITE);
		if (blackCnt > whiteCnt) return Optional.of(PlayerState.BLACK);
		else if (blackCnt < whiteCnt) return Optional.of(PlayerState.WHITE);
		else return Optional.of(PlayerState.NONE);
	}

	/**
	 * Player interactions
	 */
	public PlayerState getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean canDrop(int x, int y) {
		return gameBoard[x][y] == PlayerState.NONE && candidatePositions.contains(new Point(x, y));
	}

	private boolean isValid(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	public int getPieces(PlayerState player) {
		int result = 0;
		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j)
				if (gameBoard[i][j] == player)
					++result;
		return result;
	}

	public PlayerState getState(int x, int y) {
		return gameBoard[x][y];
	}

	public PlayerState getState(Point point) {
		return gameBoard[point.x][point.y];
	}

	private PlayerProperty<Boolean> isReady = new PlayerProperty<>(false, false);

	public boolean isReady(PlayerState player) {
		return isReady.get(player);
	}

	public void ready(PlayerState player) {
		if (gameStarted) return;
		isReady.set(player, true);
		if (isReady.getBlack() && isReady.getWhite()) newGame();
	}

	private ArrayList<Pair<Pair<Point, PlayerState>, Collection<Point>>> moves;

	public boolean canUndo(PlayerState player) {
		int lastPos;
		for (lastPos = moves.size() - 1; lastPos >= 0; --lastPos)
			if (moves.get(lastPos).fst.snd == player) break;
		if (lastPos < 0) return false;
		return true;
	}

	public boolean requestUndo(PlayerState player) {
		int lastPos;
		for (lastPos = moves.size() - 1; lastPos >= 0; --lastPos)
			if (moves.get(lastPos).fst.snd == player) break;
		if (lastPos < 0) return false; // can not undo

		if (!players.get(flip(player)).undoRequested()) return false;

		System.out.println(lastPos);

		for (int i = moves.size() - 1; i >= lastPos; --i) {
			Pair<Pair<Point, PlayerState>, Collection<Point>> move = moves.remove(i);
			gameBoard[move.fst.fst.x][move.fst.fst.y] = PlayerState.NONE;
			for (Point point : move.snd)
				gameBoard[point.x][point.y] = flip(gameBoard[point.x][point.y]);
			dropPieceHandler.handle(move.fst.fst, PlayerState.NONE, move.snd);
		}
		updateCandidatePositions(player);
		return true;
	}
}
