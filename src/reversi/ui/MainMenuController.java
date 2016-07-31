/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.action.ActionTrigger;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import ui.controls.PlayerProfileDialog;
import util.PreferenceLoader;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@FXMLController(value = "fxml/MainMenu.fxml", title = "Reversi Duel")
public class MainMenuController {
	@FXMLViewFlowContext
	private ViewFlowContext context;

	@FXML
	private StackPane __rootPane;

	@FXML
	private VBox rootPane;

	@FXML
	private VBox buttonBox;

	@FXML
	@ActionTrigger("singlePlayer")
	public Label singlePlayerButton;

	@FXML
	@ActionTrigger("networkDuel")
	public Label networkDuelButton;

	@FXML
	public Label profileButton;

	@FXML
	private Label exitButton;

	@FXML
	private PlayerProfileDialog dialog;

	@PostConstruct
	public void init() {
		buttonBox.getChildren().forEach(node -> {
			if (node instanceof Label) {
				Label label = (Label) node;
				label.setEffect(new Glow(0.0));
				label.setOnMouseEntered(this::buttonMouseEntered);
				label.setOnMouseExited(this::buttonMouseExited);
			}
		});

		dialog.setDialogContainer(__rootPane);
		profileButton.setOnMouseClicked(e -> dialog.show(PreferenceLoader.loadFromPreferences("p1")));
		dialog.setOnAccepted(e -> PreferenceLoader.saveToPreferences(dialog.getPlayerProperties(), "p1"));
	}

	@FXML
	private void exitButtonClicked() {
		((EventHandler<Event>) context.getRegisteredObject("closeHandler")).handle(null);
	}

	private HashMap<String, Timeline> timelines = new HashMap<>();

	private void changeButtonScale(Label label, double scale, double glow) {
		Timeline timeline = timelines.get(label.getText());
		if (timeline != null) timeline.stop();
		timeline = new Timeline();
		KeyValue kvX = new KeyValue(label.scaleXProperty(), scale);
		KeyValue kvY = new KeyValue(label.scaleYProperty(), scale);
		KeyValue kvGlow = new KeyValue(((Glow) label.getEffect()).levelProperty(), glow);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(100), kvX, kvY, kvGlow);
		timeline.getKeyFrames().add(keyFrame);
		timeline.play();
		timelines.put(label.getText(), timeline);
	}

	@FXML
	private void buttonMouseEntered(MouseEvent event) {
		changeButtonScale((Label) event.getTarget(), 1.15, 0.4);
	}

	@FXML
	private void buttonMouseExited(MouseEvent event) {
		changeButtonScale((Label) event.getTarget(), 1.0, 0.0);
	}
}
