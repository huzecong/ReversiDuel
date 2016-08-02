/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.effects.JFXDepthManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import logic.*;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import override.CustomFlowHandler;
import ui.controls.*;
import util.*;

import javax.annotation.PostConstruct;
import java.awt.Point;
import java.util.*;
import java.util.function.BiConsumer;

@FXMLController("fxml/GameBoard.fxml")
public abstract class AbstractGameBoardController {
	@FXMLViewFlowContext
	protected ViewFlowContext context;

	@FXML
	protected AnchorPane rootPane;
	@FXML
	protected StackPane __rootPane, gameBoard;

	@FXML
	protected PlayerTimerPane player1Pane, player2Pane;

	@FXML
	protected HBox chatBox;
	@FXML
	protected WebView chatDialog;
	@FXML
	protected TextField chatText;
	@FXML
	protected JFXButton sendChatButton;

	@FXML
	protected GridPane buttonsPane;
	@FXML
	protected JFXButton readyButton, undoButton, drawButton, surrenderButton, loadButton, saveButton;

	@FXML
	protected InformationDialog infoDialog;
	@FXML
	protected ConfirmationDialog confirmDialog;
	@FXML
	protected Label confirmDialogContents;
	@FXML
	protected JFXDialog waitDialog;
	@FXML
	protected Label waitDialogHeading;

	@FXML
	protected Label bannerText;

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
		return confirmDialog.showAndWaitResult();
	}

	public boolean showConfirmDialog(String heading, String contents, String acceptButtonText, String declineButtonText) {
		Platform.runLater(() -> {
			confirmDialog.setAcceptButtonText(acceptButtonText);
			confirmDialog.setDeclineButtonText(declineButtonText);
		});
		return showConfirmDialog(heading, contents);
	}

	public Runnable showWaitDialog(String heading) {
		Platform.runLater(() -> {
			waitDialogHeading.setText(heading);
			waitDialog.show(__rootPane);
		});
		return () -> Platform.runLater(() -> waitDialog.close());
	}

	protected final static int N = 8;
	protected final static double boardOffsetX = 39.5;
	protected final static double boardOffsetY = 35;
	protected final static double boardGridLength = 86.6;
	protected final static double boardLength = 774;

	private static class BoardPiece {
		static double imageLength = 70;
		static double candidateLength = 35;
		static Image blackPiece = new Image("image/black.png", imageLength, imageLength, true, true);
		static Image whitePiece = new Image("image/white.png", imageLength, imageLength, true, true);
		static Image blackCandidate = new Image("image/black_candidate.png", candidateLength, candidateLength, true, true);
		static Image whiteCandidate = new Image("image/white_candidate.png", candidateLength, candidateLength, true, true);

		StackPane container;
		ImageView view, candidateView;
		Timeline showAnimation, flipAnimation, candidateAnimation;
		boolean isShown, isCandidateShown;

		BoardPiece(int row, int column) {
			container = new StackPane();
			container.setTranslateX(-boardLength / 2 + (row + 0.5) * boardGridLength + boardOffsetX);
			container.setTranslateY(-boardLength / 2 + (column + 0.5) * boardGridLength + boardOffsetY);
			container.setAlignment(Pos.CENTER);

			view = new ImageView();
			JFXDepthManager.setDepth(view, 2);
			((DropShadow) view.getEffect()).setBlurType(BlurType.THREE_PASS_BOX);
			view.setOpacity(0.0);
			container.getChildren().add(view);

			candidateView = new ImageView();
			candidateView.setOpacity(0.0);
			container.getChildren().add(candidateView);

			isShown = false;
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

			isCandidateShown = false;
			candidateAnimation = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(candidateView.opacityProperty(), 0.0, Interpolator.EASE_IN)),
					new KeyFrame(Duration.millis(100), new KeyValue(candidateView.opacityProperty(), 0.75, Interpolator.EASE_IN)));
		}

		private void switchColor() {
			if (view.getImage() == blackPiece) view.setImage(whitePiece);
			else view.setImage(blackPiece);
		}

		void showAsCandidate(PlayerState player) {
			if (player == PlayerState.NONE) hideCandidate();
			else {
				if (isCandidateShown) return;
				if (player == PlayerState.WHITE) candidateView.setImage(whiteCandidate);
				else if (player == PlayerState.BLACK) candidateView.setImage(blackCandidate);
				candidateAnimation.setRate(1.0);
				candidateAnimation.play();
				isCandidateShown = true;
			}
		}

		void hideCandidate() {
			if (!isCandidateShown) return;
			candidateAnimation.setRate(-1.0);
			candidateAnimation.play();
			isCandidateShown = false;
		}

		void setRotationAxis(Point3D axis) {
			view.setRotationAxis(axis);
		}

		void show(PlayerState player) {
			if (player == PlayerState.NONE) hide();
			else {
				if (isShown) return;
				if (player == PlayerState.WHITE) view.setImage(whitePiece);
				else if (player == PlayerState.BLACK) view.setImage(blackPiece);
				showAnimation.setRate(1.0);
				showAnimation.play();
				isShown = true;
			}
		}

		void hide() {
			if (!isShown) return;
			showAnimation.setRate(-1.0);
			showAnimation.play();
			isShown = false;
		}

		void flip() {
			flipAnimation.play();
		}
	}

	private BoardPiece[][] boardPieces = new BoardPiece[N][N];

	protected GameManager manager;
	protected AbstractPlayer player1, player2;
	protected int p1TimeLimit, p2TimeLimit;

	/**
	 * Make sure all calls to AbstractPlayers are run in another thread!
	 */
	protected abstract void initPlayersAndControls();

	@PostConstruct
	public void init() {
		((CustomFlowHandler) context.getRegisteredObject("flowHandler"))
				.setOnNavigatingBack(AbstractGameBoardController.class, toClass -> manager.forceExit(""));

		player1 = (AbstractPlayer) context.getRegisteredObject("player1");
		player2 = (AbstractPlayer) context.getRegisteredObject("player2");
		p1TimeLimit = (Integer) context.getRegisteredObject("p1TimeLimit");
		p2TimeLimit = (Integer) context.getRegisteredObject("p2TimeLimit");

		JFXDepthManager.setDepth(rootPane, 1);
		BackgroundColorAnimator.applyAnimation(sendChatButton);
		buttonsPane.getChildren().forEach(BackgroundColorAnimator::applyAnimation);

		gameBoard.setOnMouseClicked(this::gameBoardClicked);
		infoDialog.setDialogContainer(__rootPane);
		confirmDialog.setDialogContainer(__rootPane);

		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j) {
				boardPieces[i][j] = new BoardPiece(i, j);
				gameBoard.getChildren().add(boardPieces[i][j].container);
			}

		manager = new GameManager();
		manager.init(player1, p1TimeLimit, player2, p2TimeLimit);
		manager.setDropPieceHandler(this::dropPiece);
		manager.setGameOverHandler(this::gameOver);
		manager.setNewGameHandler(this::newGame);
		manager.setExceptionHandler(message -> {
			if (!message.isEmpty()) showInfoDialog("Error occurred", message);
			((Runnable) context.getRegisteredObject("returnToHome")).run();
		});
		manager.setExecuteAfterAnimationHandler(runnable -> animationManager.executeAfterLastAnimation(runnable));
		manager.setDialogHandler(message -> Platform.runLater(() -> {
			chatDialog.getEngine().executeScript("" +
					"var child = document.createElement('P');" +
					"child.innerHTML = '" + message.replaceAll("'", "&#39;") + "';" +
					"document.getElementById('content').appendChild(child);" +
					"window.scrollTo(0, document.body.scrollHeight);");
		}));

		chatDialog.getEngine().loadContent("" +
				"<div id='content' style='font-family: Helvetica'>" +
				"</div>");
		chatText.setOnAction(e -> sendChatButton.fire());

		// prevent user from starting new game before animation finishes
		readyButton.disableProperty().bind(manager.gameStartedProperty().or(animationManager.isEmptyProperty().not()));
		initPlayersAndControls();

		player1Pane.setName(player1.getProfileName());
		player1Pane.setIcon(player1.getAvatarID());
		player1Pane.scoreProperty().bind(manager.p1ScoreProperty());
		player1Pane.remainingTimeProperty().bind(manager.p1RemainingTimeProperty());
		player1Pane.stateProperty().bind(manager.p1StateProperty());
		player2Pane.setName(player2.getProfileName());
		player2Pane.setIcon(player2.getAvatarID());
		player2Pane.scoreProperty().bind(manager.p2ScoreProperty());
		player2Pane.remainingTimeProperty().bind(manager.p2RemainingTimeProperty());
		player2Pane.stateProperty().bind(manager.p2StateProperty());

		manager.currentPlayerProperty().addListener((observable, oldValue, newValue) -> {
			BiConsumer<Boolean, PlayerTimerPane> toggleShadow = (hasShadow, pane) -> {
				if (!hasShadow && pane.getEffect() != null) {
					Timeline animation = new Timeline(new KeyFrame(Duration.millis(300),
							e -> pane.setEffect(null),
							new KeyValue(((DropShadow) pane.getEffect()).radiusProperty(), 0, Interpolator.EASE_IN),
							new KeyValue(((DropShadow) pane.getEffect()).offsetYProperty(), 0, Interpolator.EASE_OUT)));
					animation.play();
				} else if (hasShadow && pane.getEffect() == null) {
					pane.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.26), 0, 0.26, 0, 0));
					Timeline animation = new Timeline(new KeyFrame(Duration.millis(300),
							new KeyValue(((DropShadow) pane.getEffect()).radiusProperty(), 10, Interpolator.EASE_OUT),
							new KeyValue(((DropShadow) pane.getEffect()).offsetYProperty(), 2, Interpolator.EASE_IN)));
					animation.play();
				}
			};
			toggleShadow.accept(newValue == manager.getP1State(), player1Pane);
			toggleShadow.accept(newValue == manager.getP2State(), player2Pane);
		});
	}

	protected class BoardAnimationManager {
		LinkedList<Timeline> animationQueue = new LinkedList<>();
		Timeline lastAnimation;
		BooleanProperty isEmpty = new SimpleBooleanProperty(true);

		void add(Timeline animation) {
			animation.setOnFinished(e -> startNext());
			isEmpty.set(false);
			if (!animationQueue.isEmpty() || lastAnimation != null) {
				animationQueue.addLast(animation);
			} else {
				lastAnimation = animation;
				lastAnimation.play();
			}
		}

		void executeAfterLastAnimation(Runnable action) {
			if (animationQueue.isEmpty()) action.run();
			else {
				Timeline animation = animationQueue.getLast();
				EventHandler<ActionEvent> handler = animation.getOnFinished();
				animation.setOnFinished(e -> {
					action.run();
					handler.handle(e);
				});
			}
		}

		private void startNext() {
			lastAnimation = null;
			if (!animationQueue.isEmpty()) {
				lastAnimation = animationQueue.removeFirst();
				TaskScheduler.singleShot(200, lastAnimation::play);
			} else {
				isEmpty.set(true);
				if (manager.getPlayer() instanceof LocalPlayer)
					drawCandidatePositions();
			}
		}

		BooleanProperty isEmptyProperty() {
			return isEmpty;
		}
	}

	protected BoardAnimationManager animationManager = new BoardAnimationManager();

	/**
	 * Handlers
	 */
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
			animation.getKeyFrames().add(new KeyFrame(Duration.millis(200 + 300 * maxDist))); // mark the end of the whole animation
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
			animation.getKeyFrames().add(new KeyFrame(Duration.millis(300 * maxDist), event -> boardPieces[point.x][point.y].hide()));
			animation.getKeyFrames().add(new KeyFrame(Duration.millis(200 + 300 * maxDist))); // mark the end of the whole animation
		}

		animationManager.add(animation);
	}

	protected void newGame() {
		hideCandidates();
		hidePieces();
	}

	protected void gameOver(PlayerState state) {
		Timeline animation = new Timeline(
				new KeyFrame(Duration.ZERO,
						new KeyValue(bannerText.visibleProperty(), true),
						new KeyValue(bannerText.opacityProperty(), 0.0),
						new KeyValue(bannerText.translateYProperty(), 40)),
				new KeyFrame(Duration.millis(800),
						new KeyValue(bannerText.opacityProperty(), 1.0, Interpolator.EASE_OUT)),
				new KeyFrame(Duration.millis(1600),
						new KeyValue(bannerText.opacityProperty(), 1.0)),
				new KeyFrame(Duration.millis(2400),
						new KeyValue(bannerText.visibleProperty(), false),
						new KeyValue(bannerText.opacityProperty(), 0.0, Interpolator.EASE_IN),
						new KeyValue(bannerText.translateYProperty(), -100)));
		Platform.runLater(() -> {
			switch (state) {
				case BLACK:
					bannerText.setText("Black Wins!");
					break;
				case WHITE:
					bannerText.setText("White Wins!");
					break;
				case NONE:
					bannerText.setText("Draw");
					break;
			}
		});
		// this should not cause a problem (showing candidates), since by the time this animation is played,
		// currentPlayer would be NONE
		animationManager.add(animation);
//		animation.play();
	}

	public void drawCandidatePositions() {
		List<Point> positions = manager.getCandidatePositions();
		for (Point point : positions)
			boardPieces[point.x][point.y].showAsCandidate(manager.getCurrentPlayer());
	}

	public void hideCandidates() {
//		animationManager.add(new Timeline(new KeyFrame(Duration.ZERO, e -> {
		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j)
				boardPieces[i][j].hideCandidate();
//		}))); // does not wait for animation end
	}

	public void hidePieces() {
		animationManager.add(new Timeline(new KeyFrame(Duration.ZERO, e -> {
			for (int i = 0; i < N; ++i)
				for (int j = 0; j < N; ++j)
					boardPieces[i][j].hide();
		}), new KeyFrame(Duration.millis(200))));
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
		if (!(manager.getPlayer() instanceof LocalPlayer)) return;
		LocalPlayer player = ((LocalPlayer) manager.getPlayer());
		Point point = getCell(mouseEvent.getX(), mouseEvent.getY());
		if (point.x == -1) return;
		player.dropPiece(point);
	}
}
