/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import logic.AIPlayer;
import logic.LocalPlayer;
import logic.PlayerState;
import org.datafx.controller.FXMLController;
import util.TaskScheduler;

import java.util.function.BiConsumer;
import java.util.function.Function;

@FXMLController(value = "fxml/GameBoard.fxml", title = "Reversi Duel")
public class LocalDuelGameBoardController extends AbstractGameBoardController {
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
		setAction.accept(undoButton, p -> p::requestUndo);
		setAction.accept(surrenderButton, p -> p::requestSurrender);
		setAction.accept(drawButton, p -> p::requestDraw);
		exitButton.setOnAction(e -> TaskScheduler.singleShot(1, () -> {
			if (showConfirmDialog("Confirm request", "Quit match and return to main menu?", "YES", "NO"))
				((Runnable) context.getRegisteredObject("returnToHome")).run();
		}));

		readyButton.setOnAction(e -> {
			if (player1 instanceof LocalPlayer) ((LocalPlayer) player1).ready();
			else ((AIPlayer) player1).ready();
			if (player2 instanceof LocalPlayer) ((LocalPlayer) player2).ready();
			else ((AIPlayer) player2).ready();
		});

		undoButton.setDisable(true);
		manager.currentPlayerProperty().addListener((observable, oldValue, newValue) -> {
			undoButton.disableProperty().unbind();
			if (newValue == PlayerState.NONE) {
				undoButton.setDisable(true);
			} else {
				undoButton.disableProperty().bind(manager.gameStartedProperty().not()
						.or(manager.getPlayer(newValue).canUndoProperty().not()));
			}
		});
	}
}
