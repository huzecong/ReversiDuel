/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.datafx.controller.FXMLController;
import ui.controls.PlayerTimerPane;
import util.BackgroundColorAnimator;

import javax.annotation.PostConstruct;

@FXMLController(value = "fxml/GameBoard.fxml", title = "Duel!")
public class AbstractGameBoardController {
	@FXML
	protected AnchorPane __rootPane, rootPane;

	@FXML
	protected ImageView gameBoard;

	@FXML
	protected PlayerTimerPane player1Pane, player2Pane;

	@FXML
	protected TextArea chatDialog;

	@FXML
	protected TextField chatText;

	@FXML
	protected JFXButton sendChatButton;

	@PostConstruct
	public void init() {
		JFXDepthManager.setDepth(rootPane, 1);
		JFXDepthManager.setDepth(gameBoard, 5);
		chatDialog.setText("1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
		BackgroundColorAnimator.applyAnimation(sendChatButton);
	}
}
