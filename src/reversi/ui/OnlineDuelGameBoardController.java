/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logic.*;

public class OnlineDuelGameBoardController extends AbstractGameBoardController {
	private BooleanProperty isLocalPlayer;

	@Override
	protected void initPlayersAndControls() {
		assert player1 instanceof LocalPlayer || player1 instanceof AIPlayer;
		assert player2 instanceof NetworkPlayer;

		undoButton.setDisable(true);
		if (player1 instanceof LocalPlayer) {
			undoButton.disableProperty().bind(manager.gameStartedProperty().not()
					.or(player1.canUndoProperty().not()));
		}

		readyButton.setOnAction(e -> {
			if (player1 instanceof LocalPlayer) ((LocalPlayer) player1).ready();
			else ((AIPlayer) player1).ready();
		});
		undoButton.setOnAction(e -> ((LocalPlayer) player1).requestUndo());
		surrenderButton.setOnAction(e -> ((LocalPlayer) player1).requestSurrender());
		drawButton.setOnAction(e -> ((LocalPlayer) player1).requestDraw());
		exitButton.setOnAction(e -> {
			if (player1 instanceof LocalPlayer) ((LocalPlayer) player1).requestExit();
			else ((AIPlayer) player1).requestExit();
		});
		sendChatButton.setOnAction(e -> {
			if (player1 instanceof LocalPlayer && !chatText.getText().isEmpty()) {
				((LocalPlayer) player1).sendChat(chatText.getText());
				chatText.setText("");
			}
		});

		isLocalPlayer = new SimpleBooleanProperty(false);
		undoButton.disableProperty().bind(manager.gameStartedProperty().not().or(isLocalPlayer.not()));
		surrenderButton.disableProperty().bind(manager.gameStartedProperty().not().or(isLocalPlayer.not()));
		drawButton.disableProperty().bind(manager.gameStartedProperty().not().or(isLocalPlayer.not()));
		saveLoadButton.setDisable(true);
		manager.currentPlayerProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == PlayerState.NONE || !(manager.getPlayer(newValue) instanceof LocalPlayer)) {
				isLocalPlayer.setValue(false);
			} else {
				isLocalPlayer.setValue(true);
			}
		});
	}
}
