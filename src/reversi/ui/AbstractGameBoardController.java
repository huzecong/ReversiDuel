/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Material;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import logic.GameManager;
import logic.LocalPlayer;
import logic.PlayerState;
import org.datafx.controller.FXMLController;
import ui.controls.PlayerTimerPane;
import util.BackgroundColorAnimator;
import util.TaskScheduler;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

@FXMLController(value = "fxml/GameBoard.fxml", title = "Duel!")
public class AbstractGameBoardController {
	@FXML
	protected AnchorPane __rootPane, rootPane;

	@FXML
	protected StackPane gameBoard;

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

	protected final static int N = 8;
	protected final static double boardOffsetX = 39.5;
	protected final static double boardOffsetY = 35;
	protected final static double boardGridLength = 86.6;
	protected final static double boardLength = 774;

	static class BoardPiece {
		static double imageLength = boardGridLength * 0.8;
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
			candidateView.setFitHeight(imageLength / 2);
			candidateView.setFitWidth(imageLength / 2);
			candidateView.setOpacity(0.5);
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
		manager = new GameManager();
		player1 = new LocalPlayer();
		player2 = new LocalPlayer();
		manager.init(player1, player2);
		manager.setDropPieceHandler(pair -> {
			boardPieces[pair.fst.x][pair.fst.y].show(pair.snd);
			Collection<Point> flippedPosition = manager.getFlippedPositions();
			if (flippedPosition.size() == 0) {
				drawCandidatePositions();
				return;
			}
			Timeline animation = new Timeline();
			ArrayList<Point>[] pointDist = new ArrayList[N];
			for (int i = 0; i < N; ++i)
				pointDist[i] = new ArrayList<Point>();
			for (Point point : flippedPosition) {
				int dist = Math.max(Math.abs(point.x - pair.fst.x), Math.abs(point.y - pair.fst.y));
				pointDist[dist - 1].add(point);
			}
			IntegerProperty ignored = new SimpleIntegerProperty(0);
			for (int i = 0; i < N; ++i) {
				final int index = i;
				if (pointDist[index].size() == 0) break;
				animation.getKeyFrames().add(new KeyFrame(Duration.millis(200 + 300 * index), event -> {
					for (Point point : pointDist[index])
						boardPieces[point.x][point.y].flip();
				}, new KeyValue(ignored, index + 1)));
			}
			animation.setOnFinished(e -> TaskScheduler.singleShot(300, this::drawCandidatePositions));
			animation.play();
		});

		JFXDepthManager.setDepth(rootPane, 1);
		chatDialog.setText("1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
		BackgroundColorAnimator.applyAnimation(sendChatButton);

		buttonsPane.getChildren().forEach(BackgroundColorAnimator::applyAnimation);

		gameBoard.setOnMouseClicked(this::gameBoardClicked);

		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j) {
				boardPieces[i][j] = new BoardPiece(i, j);
				gameBoard.getChildren().add(boardPieces[i][j].container);
			}

		manager.newGame();
	}

	private void drawCandidatePositions() {
		Collection<Point> positions = manager.getCandidatePositions();
		for (Point point : positions)
			boardPieces[point.x][point.y].showAsCandidate(manager.getCurrentPlayer());
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
		if (success) {
			for (int i = 0; i < N; ++i)
				for (int j = 0; j < N; ++j)
					boardPieces[i][j].hideCandidate();
		}
	}
}
