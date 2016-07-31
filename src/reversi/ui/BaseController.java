/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.*;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;
import override.*;
import ui.controls.ConfirmationDialog;

import javax.annotation.PostConstruct;

@FXMLController(value = "fxml/Base.fxml", title = "Reversi Duel")
public class BaseController {
	@FXMLViewFlowContext
	private ViewFlowContext context;

	@FXML
	private StackPane __rootPane, mainPane;

	@FXML
	private AnchorPane borderShadows;

	@FXML
	private JFXToolbar toolbar;

	@FXML
	private ConfirmationDialog closeDialog;

	@FXML
	private CustomRippler closeButton, backButton;

	@FXML
	private Label titleLabel;

	private double xOffset, yOffset;

	private FlowHandler flowHandler;

	@PostConstruct
	public void init() throws FlowException {
		Flow innerFlow = new Flow(MainMenuController.class)
				.withLink(MainMenuController.class, "singlePlayer", LocalDuelGameBoardController.class)
				.withLink(MainMenuController.class, "networkDuel", ConnectPageController.class)
				.withLink(MainMenuController.class, "profile", ProfilePageController.class);
		EventHandler<Event> closeHandler = event -> closeDialog.show(__rootPane);
		Stage stage = (Stage) context.getRegisteredObject("stage");
		CustomAnimatedFlowContainer container = new CustomAnimatedFlowContainer(Duration.millis(400));
		context.register("closeHandler", closeHandler);
		context.register("returnToHome", (Runnable) () -> Platform.runLater(() -> {
			while (!container.isIsInitialView()) {
				try {
					flowHandler.navigateBack();
				} catch (VetoException | FlowException e) {
					e.printStackTrace();
				}
			}
		}));

		stage.titleProperty().unbind();
		titleLabel.textProperty().bind(stage.titleProperty());

		JFXDepthManager.setDepth(toolbar, 1);
		borderShadows.getChildren().get(1).setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.26), 10, 0.12, 2, 0));
		borderShadows.getChildren().get(2).setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.26), 10, 0.12, -2, 0));
		borderShadows.getChildren().get(3).setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.26), 10, 0.12, 0, -2));

//		flowHandler = innerFlow.createHandler(context);
		flowHandler = new CustomFlowHandler(innerFlow, context);
		mainPane.getChildren().add(flowHandler.start(container));

		backButton.visibleProperty().bind(container.isInitialViewProperty().not());
//		backButton.setOnMouseClicked(event -> {          // using MouseClicked would trigger twice... don't know why
//		flowHandler.attachBackEventHandler(backButton);  // same for this
		backButton.setOnMouseReleased(event -> {
			try {
//				System.out.println("back");
				flowHandler.navigateBack();
			} catch (VetoException | FlowException e) {
				e.printStackTrace();
			}
		});

		// override default quit behavior
		closeButton.setOnMouseClicked(closeHandler);
		closeDialog.setOnAccepted(e -> Platform.runLater(() -> {
			Platform.exit();
			System.exit(0);
		}));
		stage.setOnCloseRequest(event -> {
			event.consume();
			closeHandler.handle(event);
		});

		// custom mouse event handlers for title toolbar
		toolbar.setOnMousePressed(mouseEvent -> {
			xOffset = mouseEvent.getSceneX();
			yOffset = mouseEvent.getSceneY();
		});
		titleLabel.setOnMousePressed(toolbar.getOnMousePressed());

		toolbar.setOnMouseDragged(mouseEvent -> {
			if (!mouseEvent.isPrimaryButtonDown()
					|| (xOffset == -1 && yOffset == -1)
					|| mouseEvent.isStillSincePress()) return;
			stage.setX(mouseEvent.getScreenX() - xOffset);
			stage.setY(mouseEvent.getScreenY() - yOffset);
			mouseEvent.consume();
		});
		titleLabel.setOnMouseDragged(toolbar.getOnMouseDragged());
	}
}
