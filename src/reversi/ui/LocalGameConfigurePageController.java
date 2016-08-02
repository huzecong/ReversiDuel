/**
 * Created by kanari on 2016/7/25.
 */

package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.FlowActionHandler;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;
import ui.controls.HostDataListCell;
import ui.controls.PlayerProfileDialog;
import util.BackgroundColorAnimator;
import util.PreferenceLoader;

import javax.annotation.PostConstruct;
import java.util.Properties;

@FXMLController(value = "fxml/LocalGameConfigurePane.fxml", title = "Configure Duel")
public class LocalGameConfigurePageController {
	@FXMLViewFlowContext
	private ViewFlowContext context;
	@ActionHandler
	private FlowActionHandler actionHandler;

	@FXML
	private StackPane __rootPane;
	@FXML
	private AnchorPane rootPane;

	@FXML
	private JFXButton player1ChangeButton, player2ChangeButton;
	@FXML
	private HostDataListCell player1Pane, player2Pane;

	@FXML
	private Label duelButton;

	@FXML
	private PlayerProfileDialog dialog;

	private Properties p1Properties, p2Properties;

	@PostConstruct
	public void init() {
		JFXDepthManager.setDepth(rootPane, 1);
		JFXDepthManager.setDepth(player1Pane.getParent(), 1);
		JFXDepthManager.setDepth(player2Pane.getParent(), 1);
		BackgroundColorAnimator.applyAnimation(player1ChangeButton);
		BackgroundColorAnimator.applyAnimation(player2ChangeButton);

		p1Properties = PreferenceLoader.loadFromPreferences("p1");
		p2Properties = PreferenceLoader.loadFromPreferences("p2");

		player1ChangeButton.setOnAction(e -> {
			dialog.setOnAccepted(ev -> {
				p1Properties = dialog.getPlayerProperties();
				PreferenceLoader.saveToPreferences(p1Properties, "p1");
				updateProperties(player1Pane, p1Properties);
			});
			dialog.show(p1Properties);
		});
		player2ChangeButton.setOnAction(e -> {
			dialog.setOnAccepted(ev -> {
				p2Properties = dialog.getPlayerProperties();
				PreferenceLoader.saveToPreferences(p2Properties, "p2");
				updateProperties(player2Pane, p2Properties);
			});
			dialog.show(p2Properties);
		});

		updateProperties(player1Pane, p1Properties);
		updateProperties(player2Pane, p2Properties);

		dialog.setDialogContainer(__rootPane);

		duelButton.setOnMouseClicked(e -> {
			try {
				context.register("player1", PreferenceLoader.playerFromProperties(p1Properties));
				context.register("player2", PreferenceLoader.playerFromProperties(p2Properties));
				context.register("p1TimeLimit", Integer.parseInt(p1Properties.getProperty("timeLimit")));
				context.register("p2TimeLimit", Integer.parseInt(p2Properties.getProperty("timeLimit")));
				actionHandler.navigate(LocalDuelGameBoardController.class);
			} catch (VetoException | FlowException e1) {
				e1.printStackTrace();
			}
		});
	}

	private void updateProperties(HostDataListCell pane, Properties properties) {
		pane.setName(properties.getProperty("profileName"));
		pane.setIcon(properties.getProperty("avatarID"));
		pane.setCaption(properties.getProperty("playerType").equals("AI") ? "AI player" : "Local player");
	}
}
