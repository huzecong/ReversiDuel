/**
 * Created by kanari on 2016/7/28.
 */

package logic;


public class GameManager {
	static final int N = 8;
	boolean[][] isOccupied, isBlack;

	public void init() {
		isOccupied = new boolean[N][N];
		isBlack = new boolean[N][N];
	}

	public boolean myTurn() {
		return true;
	}

	public boolean canDrop(int x, int y) {
		return !isOccupied[x][y];
	}

	public void dropPiece(int x, int y) {
		isOccupied[x][y] = true;
	}
}
