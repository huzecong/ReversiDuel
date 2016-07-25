import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.action.ActionTrigger;
import org.datafx.controller.flow.container.AnimatedFlowContainer;
import org.datafx.controller.flow.container.ContainerAnimations;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@FXMLController(value = "fxml/Base.fxml", title = "Gomoku Duel")
public class BaseController {
	@FXMLViewFlowContext
	private ViewFlowContext context;

	@FXML
	private StackPane __rootPane, mainPane;

	@FXML
	private JFXToolbar toolbar;

	@FXML
	private JFXDialog closeDialog;

	@FXML
	private JFXButton dialogAcceptButton, dialogDeclineButton;

	@FXML
	private JFXRippler closeButton;

	@FXML
	@ActionTrigger("back")
	private JFXRippler backButton;

	@FXML
	private Label titleLabel;

	private double xOffset, yOffset;

	@PostConstruct
	public void init() throws FlowException {
		Flow innerFlow = new Flow(MainMenuController.class)
				.withLink(MainMenuController.class, "singlePlayer", AIConfigurePageController.class)
				.withLink(MainMenuController.class, "networkDuel", ConnectPageController.class)
				.withLink(MainMenuController.class, "profile", ProfilePageController.class)
				.withGlobalBackAction("back");
		EventHandler<Event> closeHandler = event -> closeDialog.show(__rootPane);
		Stage stage = (Stage) context.getRegisteredObject("stage");
		context.register("closeHandler", closeHandler);

		titleLabel.textProperty().bind(stage.titleProperty());

		JFXDepthManager.setDepth(toolbar, 2);

		mainPane.getChildren().add(innerFlow.createHandler(context)
				.start(new AnimatedFlowContainer(Duration.millis(400), ContainerAnimations.SWIPE_RIGHT)));

		__rootPane.setMaxHeight(900);
		__rootPane.setMinHeight(900);
		__rootPane.setPrefHeight(900);
		__rootPane.setMaxWidth(1200);
		__rootPane.setMinWidth(1200);
		__rootPane.setPrefWidth(1200);

//		SVGGlyph glyphClose = new SVGGlyph(0, "CLOSE", "M810 274l-238 238 238 238-60 60-238-238-238 238-60-60 238-238-238-238 60-60 238 238 238-238z", Color.DARKSLATEGRAY);
//		glyphClose.setSize(20, 20);
//		closeButton.setGraphic(glyphClose);

		// override default quit behavior
		closeDialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
		closeButton.setOnMouseClicked(closeHandler);
		dialogAcceptButton.setOnAction(event -> {
			closeDialog.close();
			Platform.runLater(() -> {
				Platform.exit();
				System.exit(0);
			});
		});
		dialogDeclineButton.setOnAction(event -> closeDialog.close());
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
