/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import com.sun.javafx.tk.Toolkit;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import logic.GameManager;
import logic.LocalPlayer;
import logic.PlayerState;
import org.datafx.controller.FXMLController;
import ui.controls.ConfirmationDialog;
import ui.controls.InformationDialog;
import ui.controls.PlayerTimerPane;
import util.BackgroundColorAnimator;
import util.TaskScheduler;

import javax.annotation.PostConstruct;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

@FXMLController(value = "fxml/GameBoard.fxml", title = "Duel!")
public class AbstractGameBoardController {
	@FXML
	protected AnchorPane rootPane;

	@FXML
	protected StackPane __rootPane, gameBoard;

	@FXML
	protected PlayerTimerPane player1Pane, player2Pane;

	@FXML
	protected TextArea chatDialog;

	@FXML
	protected TextField chatText;

	@FXML
	protected JFXButton sendChatButton;

	@FXML
	protected GridPane buttonsPane;

	@FXML
	protected JFXButton readyButton, undoButton, drawButton, surrenderButton, saveLoadButton, exitButton;

	@FXML
	protected InformationDialog infoDialog;

	@FXML
	protected ConfirmationDialog confirmDialog;

	@FXML
	protected Label confirmDialogContents;

	public void showInfoDialog(String heading, String contents) {
		Platform.runLater(() -> {
			infoDialog.setHeading(heading);
			infoDialog.setContents(contents);
		});
		infoDialog.showAndWait();
	}

	public boolean showConfirmDialog(String heading, String contents) {
		Platform.runLater(() -> {
			confirmDialog.setHeading(heading);
			confirmDialogContents.setText(contents);
		});
		boolean result = confirmDialog.showAndWaitResult();
		System.out.println(result);
		return result;
	}

	protected final static int N = 8;
	protected final static double boardOffsetX = 39.5;
	protected final static double boardOffsetY = 35;
	protected final static double boardGridLength = 86.6;
	protected final static double boardLength = 774;

	static class BoardPiece {
		static double imageLength = 70;
		static Image blackPiece = new Image("image/black.png", imageLength, imageLength, true, false);
		static Image whitePiece = new Image("image/white.png", imageLength, imageLength, true, false);

		StackPane container;
		ImageView view, candidateView;
		Timeline showAnimation, flipAnimation;

		BoardPiece(int row, int column) {
			container = new StackPane();
			container.setTranslateX(-boardLength / 2 + (row + 0.5) * boardGridLength + boardOffsetX);
			container.setTranslateY(-boardLength / 2 + (column + 0.5) * boardGridLength + boardOffsetY);
			container.setAlignment(Pos.CENTER);

			view = new ImageView();
			JFXDepthManager.setDepth(view, 2);
			view.setOpacity(0.0);
			container.getChildren().add(view);

			candidateView = new ImageView();
			candidateView.setSmooth(true);
			candidateView.setFitHeight(imageLength / 2);
			candidateView.setFitWidth(imageLength / 2);
			candidateView.setOpacity(0.75);
			candidateView.setVisible(false);
			container.getChildren().add(candidateView);

			showAnimation = new Timeline(
					new KeyFrame(Duration.ZERO,
							new KeyValue(view.opacityProperty(), 0.0, Interpolator.EASE_IN),
							new KeyValue(view.fitWidthProperty(), imageLength * 1.4, Interpolator.EASE_IN),
							new KeyValue(view.fitHeightProperty(), imageLength * 1.4, Interpolator.EASE_IN)),
					new KeyFrame(Duration.millis(200),
							new KeyValue(view.opacityProperty(), 1.0, Interpolator.EASE_IN),
							new KeyValue(view.fitWidthProperty(), imageLength, Interpolator.EASE_IN),
							new KeyValue(view.fitHeightProperty(), imageLength, Interpolator.EASE_IN)));

			view.setRotationAxis(new Point3D(1, 0, 0));
			flipAnimation = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(view.rotateProperty(), 0.0)),
					new KeyFrame(Duration.millis(150), event -> switchColor(), new KeyValue(view.rotateProperty(), 90.0)),
					new KeyFrame(Duration.millis(300), new KeyValue(view.rotateProperty(), 0.0)));
		}

		private void switchColor() {
			if (view.getImage() == blackPiece) view.setImage(whitePiece);
			else view.setImage(blackPiece);
		}

		void showAsCandidate(PlayerState player) {
			if (player == PlayerState.NONE) hideCandidate();
			else {
				if (player == PlayerState.WHITE) candidateView.setImage(whitePiece);
				else if (player == PlayerState.BLACK) candidateView.setImage(blackPiece);
				candidateView.setVisible(true);
			}
		}

		void hideCandidate() {
			candidateView.setVisible(false);
		}

		void setRotationAxis(Point3D axis) {
			view.setRotationAxis(axis);
		}

		void show(PlayerState player) {
			if (player == PlayerState.NONE) hide();
			else {
				if (player == PlayerState.WHITE) view.setImage(whitePiece);
				else if (player == PlayerState.BLACK) view.setImage(blackPiece);
				showAnimation.setRate(1.0);
				showAnimation.play();
			}
		}

		void hide() {
			showAnimation.setRate(-1.0);
			showAnimation.play();
		}

		void flip() {
			flipAnimation.play();
		}
	}

	BoardPiece[][] boardPieces = new BoardPiece[N][N];

	protected GameManager manager;
	protected LocalPlayer player1, player2;

	@PostConstruct
	public void init() {
		JFXDepthManager.setDepth(rootPane, 1);
		chatDialog.setText("1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
		BackgroundColorAnimator.applyAnimation(sendChatButton);

		buttonsPane.getChildren().forEach(BackgroundColorAnimator::applyAnimation);
		undoButton.setOnAction(e -> {
			LocalPlayer player = (LocalPlayer) manager.getPlayer();
			TaskScheduler.singleShot(1, player::requestUndo);
		});

		gameBoard.setOnMouseClicked(this::gameBoardClicked);
		infoDialog.setDialogContainer(__rootPane);
		confirmDialog.setDialogContainer(__rootPane);

		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j) {
				boardPieces[i][j] = new BoardPiece(i, j);
				gameBoard.getChildren().add(boardPieces[i][j].container);
			}

		manager = new GameManager();
		player1 = new LocalPlayer();
		player2 = new LocalPlayer();
		manager.init(player1, player2);
		manager.setDropPieceHandler(this::dropPiece);

		player1.setConfirmDialogCaller(this::showConfirmDialog);
		player1.setInfoDialogCaller(this::showInfoDialog);
		player2.setConfirmDialogCaller(this::showConfirmDialog);
		player2.setInfoDialogCaller(this::showInfoDialog);

		manager.newGame();
	}

	private class BoardAnimationManager {
		ArrayList<Timeline> animationQueue = new ArrayList<>();
		Timeline lastAnimation;

		void add(Timeline animation) {
			animation.setOnFinished(e -> startNext());
			if (!animationQueue.isEmpty() || lastAnimation != null) {
				animationQueue.add(animation);
			} else {
				lastAnimation = animation;
				lastAnimation.play();
			}
		}

		private void startNext() {
			lastAnimation = null;
			if (!animationQueue.isEmpty()) {
				lastAnimation = animationQueue.remove(0);
				TaskScheduler.singleShot(200, lastAnimation::play);
			} else {
				drawCandidatePositions();
			}
		}
	}

	private BoardAnimationManager animationManager = new BoardAnimationManager();

	private void dropPiece(Point point, PlayerState player, Collection<Point> flippedPositions) {
		hideCandidates();
		Timeline animation = new Timeline();
		ArrayList<Point>[] pointDist = new ArrayList[N];
		for (int i = 0; i < N; ++i)
			pointDist[i] = new ArrayList<Point>();
		int maxDist = 0;
		for (Point p : flippedPositions) {
			int dist = Math.max(Math.abs(p.x - point.x), Math.abs(p.y - point.y));
			maxDist = Math.max(maxDist, dist);
			pointDist[dist - 1].add(p);
		}

		if (player != PlayerState.NONE) { // drop piece
			animation.getKeyFrames().add(new KeyFrame(Duration.ZERO, event -> boardPieces[point.x][point.y].show(player)));

			for (int i = 0; i < maxDist; ++i) {
				final int index = i;
				animation.getKeyFrames().add(new KeyFrame(Duration.millis(200 + 300 * index), event -> {
					for (Point p : pointDist[index]) {
						boardPieces[p.x][p.y].setRotationAxis(new Point3D(-(p.y - point.y), p.x - point.x, 0));
						boardPieces[p.x][p.y].flip();
					}
				}));
			}
			animation.getKeyFrames().add(new KeyFrame(Duration.millis(200 + 300 * maxDist), event -> {
				// mark the end of the whole animation
			}));
		} else { // undo
			for (int i = maxDist - 1; i >= 0; --i) {
				final int index = i;
				animation.getKeyFrames().add(new KeyFrame(Duration.millis(300 * (maxDist - index - 1)), event -> {
					for (Point p : pointDist[index]) {
						boardPieces[p.x][p.y].setRotationAxis(new Point3D(-(p.y - point.y), p.x - point.x, 0));
						boardPieces[p.x][p.y].flip();
					}
				}));
			}
			animation.getKeyFrames().add(new KeyFrame(Duration.millis(200 + 300 * maxDist), event -> {
				// mark the end of the whole animation
			}));

			animation.getKeyFrames().add(new KeyFrame(Duration.millis(300 * maxDist), event -> boardPieces[point.x][point.y].hide()));
		}

		animationManager.add(animation);
	}

	public void drawCandidatePositions() {
		Collection<Point> positions = manager.getCandidatePositions();
		for (Point point : positions)
			boardPieces[point.x][point.y].showAsCandidate(manager.getCurrentPlayer());
	}

	public void hideCandidates() {
		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j)
				boardPieces[i][j].hideCandidate();
	}

	private Point getCell(double mouseX, double mouseY) {
		Point2D upperLeft = gameBoard.getChildren().get(0).getLocalToParentTransform().transform(0, 0);
		int row = (int) Math.floor((mouseX - upperLeft.getX() - boardOffsetX) / boardGridLength);
		int column = (int) Math.floor((mouseY - upperLeft.getY() - boardOffsetY) / boardGridLength);
		if (!(row >= 0 && row < N && column >= 0 && column < N))
			return new Point(-1, -1);
		return new Point(row, column);
	}

	protected void gameBoardClicked(MouseEvent mouseEvent) {
		Point point = getCell(mouseEvent.getX(), mouseEvent.getY());
		if (point.x == -1) return;
		boolean success = false;
		if (manager.getCurrentPlayer() == PlayerState.BLACK) success = player1.dropPiece(point);
		else if (manager.getCurrentPlayer() == PlayerState.WHITE) success = player2.dropPiece(point);
	}
}
