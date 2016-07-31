/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logic.*;
import org.datafx.controller.FXMLController;

@FXMLController(value = "fxml/GameBoard.fxml", title = "Reversi Duel")
public class OnlineDuelGameBoardController extends AbstractGameBoardController {
	private BooleanProperty isLocalPlayer;

	@Override
	protected void initPlayersAndControls() {
		AbstractPlayer localPlayer, networkPlayer;
		if (!(player1 instanceof NetworkPlayer)) {
			localPlayer = player1;
			networkPlayer = player2;
		} else {
			localPlayer = player2;
			networkPlayer = player1;
		}

		assert localPlayer instanceof LocalPlayer || localPlayer instanceof AIPlayer;
		assert networkPlayer instanceof NetworkPlayer;

		undoButton.setDisable(true);
		if (localPlayer instanceof LocalPlayer) {
			undoButton.disableProperty().bind(manager.gameStartedProperty().not()
					.or(localPlayer.canUndoProperty().not()));
		}

		readyButton.setOnAction(e -> {
			if (localPlayer instanceof LocalPlayer) ((LocalPlayer) localPlayer).ready();
			else ((AIPlayer) localPlayer).ready();
		});
		undoButton.setOnAction(e -> ((LocalPlayer) localPlayer).requestUndo());
		surrenderButton.setOnAction(e -> ((LocalPlayer) localPlayer).requestSurrender());
		drawButton.setOnAction(e -> ((LocalPlayer) localPlayer).requestDraw());
		exitButton.setOnAction(e -> {
			if (localPlayer instanceof LocalPlayer) ((LocalPlayer) localPlayer).requestExit();
			else ((AIPlayer) localPlayer).requestExit();
		});
		sendChatButton.setOnAction(e -> {
			if (localPlayer instanceof LocalPlayer && !chatText.getText().isEmpty()) {
				((LocalPlayer) localPlayer).sendChat(chatText.getText());
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
