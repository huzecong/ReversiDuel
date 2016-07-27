/**
 * Created by kanari on 2016/7/24.
 */

import Controls.ConfirmationDialog;
import CustomOverride.CustomAnimatedFlowContainer;
import CustomOverride.CustomFlowHandler;

import CustomOverride.CustomRippler;
import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.FlowHandler;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;

import javax.annotation.PostConstruct;

@FXMLController(value = "fxml/Base.fxml", title = "Gomoku Duel")
public class BaseController {
	@FXMLViewFlowContext
	private ViewFlowContext context;

	@FXML
	private StackPane __rootPane, mainPane;

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
				.withLink(MainMenuController.class, "singlePlayer", AIConfigurePageController.class)
				.withLink(MainMenuController.class, "networkDuel", ConnectPageController.class)
				.withLink(MainMenuController.class, "profile", ProfilePageController.class);
//				.withAction(MainMenuController.class, "networkDuel", (flowHandler, actionId) -> System.out.println("wow"))
//				.withGlobalBackAction("back");
		EventHandler<Event> closeHandler = event -> closeDialog.show(__rootPane);
		Stage stage = (Stage) context.getRegisteredObject("stage");
		context.register("closeHandler", closeHandler);

		titleLabel.textProperty().bind(stage.titleProperty());

		JFXDepthManager.setDepth(toolbar, 1);

//		flowHandler = innerFlow.createHandler(context);
		flowHandler = new CustomFlowHandler(innerFlow, context);
		CustomAnimatedFlowContainer container = new CustomAnimatedFlowContainer(Duration.millis(400));
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

//		SVGGlyph glyphClose = new SVGGlyph(0, "CLOSE", "M810 274l-238 238 238 238-60 60-238-238-238 238-60-60 238-238-238-238 60-60 238 238 238-238z", Color.DARKSLATEGRAY);
//		glyphClose.setSize(20, 20);
//		closeButton.setGraphic(glyphClose);

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
