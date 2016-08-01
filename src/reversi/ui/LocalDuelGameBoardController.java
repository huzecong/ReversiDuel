/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import logic.*;
import org.datafx.controller.FXMLController;
import util.TaskScheduler;

import java.io.File;
import java.util.function.*;

@FXMLController(value = "fxml/GameBoard.fxml", title = "Reversi Duel")
public class LocalDuelGameBoardController extends AbstractGameBoardController {
	private BooleanProperty isLocalPlayer;

	@Override
	protected void initPlayersAndControls() {
		chatDialog.setMaxHeight(chatDialog.getMaxHeight() + 50);
		chatDialog.setMinHeight(chatDialog.getMinHeight() + 50);
		chatBox.setVisible(false);

		assert player1 instanceof LocalPlayer || player1 instanceof AIPlayer;
		assert player2 instanceof LocalPlayer || player2 instanceof AIPlayer;

		boolean p1Local = player1 instanceof LocalPlayer;
		boolean p2Local = player2 instanceof LocalPlayer;

		if (p1Local) {
			((LocalPlayer) player1).setConfirmDialogCaller(this::showConfirmDialog);
			((LocalPlayer) player1).setInfoDialogCaller(this::showInfoDialog);
		}
		if (p2Local) {
			((LocalPlayer) player2).setConfirmDialogCaller(this::showConfirmDialog);
			((LocalPlayer) player2).setInfoDialogCaller(this::showInfoDialog);
		}

		BiConsumer<Button, Function<LocalPlayer, Runnable>> setAction = (button, function) -> {
			button.setOnAction(e -> {
				if (!(manager.getPlayer() instanceof LocalPlayer)) return;
				LocalPlayer player = (LocalPlayer) manager.getPlayer();
				TaskScheduler.singleShot(1, function.apply(player)); // make sure function is called on another thread!
			});
		};
		readyButton.setOnAction(e -> TaskScheduler.singleShot(1, () -> {
			if (player1 instanceof LocalPlayer) ((LocalPlayer) player1).ready();
			else ((AIPlayer) player1).ready();
			if (player2 instanceof LocalPlayer) ((LocalPlayer) player2).ready();
			else ((AIPlayer) player2).ready();
		}));
		setAction.accept(undoButton, p -> p::requestUndo);
		setAction.accept(surrenderButton, p -> p::requestSurrender);
		setAction.accept(drawButton, p -> p::requestDraw);
		exitButton.setOnAction(e -> TaskScheduler.singleShot(1, () -> {
			if (showConfirmDialog("Confirm request", "Quit match and return to main menu?", "YES", "NO")) {
				((Runnable) context.getRegisteredObject("returnToHome")).run();
			}
		}));

		isLocalPlayer = new SimpleBooleanProperty(false);
		undoButton.setDisable(true);
		surrenderButton.disableProperty().bind(manager.gameStartedProperty().not().or(isLocalPlayer.not()));
		drawButton.disableProperty().bind(manager.gameStartedProperty().not().or(isLocalPlayer.not()));
		Runnable enableSave = () -> {
			saveLoadButton.setText("SAVE");
			saveLoadButton.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save Game State");
				File file = fileChooser.showSaveDialog(saveLoadButton.getScene().getWindow());
				if (file != null) manager.saveGame(file.getAbsolutePath());
			});
		};
		Runnable enableLoad = () -> {
			saveLoadButton.setText("LOAD");
			saveLoadButton.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Load Game State");
				File file = fileChooser.showOpenDialog(saveLoadButton.getScene().getWindow());
				if (file != null) manager.loadGame(file.getAbsolutePath());
			});
		};
		enableLoad.run();
		manager.gameStartedProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
			if (newValue) enableSave.run();
			else enableLoad.run();
		}));
		manager.currentPlayerProperty().addListener((observable, oldValue, newValue) -> {
			undoButton.disableProperty().unbind();
			if (newValue == PlayerState.NONE) {
				undoButton.setDisable(true);
				isLocalPlayer.setValue(false);
			} else {
				AbstractPlayer player = manager.getPlayer(newValue);
				if (!(player instanceof LocalPlayer)) {
					undoButton.setDisable(true);
					isLocalPlayer.setValue(false);
				} else {
					undoButton.disableProperty().bind(manager.gameStartedProperty().not()
							.or(((LocalPlayer) manager.getPlayer(newValue)).canUndoProperty().not()));
					isLocalPlayer.setValue(true);
				}
			}
		});
	}
}
