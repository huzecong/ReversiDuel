import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.container.AnimatedFlowContainer;
import org.datafx.controller.flow.container.ContainerAnimations;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;

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
	private JFXDialog closeDialog;

	@FXML
	private JFXButton closeButton, dialogAcceptButton, dialogDeclineButton;

	private double xOffset, yOffset;

	@PostConstruct
	public void init() throws FlowException {
		Flow innerFlow = new Flow(MainMenuController.class);

		JFXDepthManager.setDepth(toolbar, 2);

		mainPane.getChildren().add(innerFlow.createHandler()
				.start(new AnimatedFlowContainer(Duration.millis(400), ContainerAnimations.SWIPE_RIGHT)));

		__rootPane.setMaxHeight(900);
		__rootPane.setMinHeight(900);
		__rootPane.setPrefHeight(900);
		__rootPane.setMaxWidth(1200);
		__rootPane.setMinWidth(1200);
		__rootPane.setPrefWidth(1200);

		SVGGlyph glyphClose = new SVGGlyph(0, "CLOSE", "M810 274l-238 238 238 238-60 60-238-238-238 238-60-60 " +
				"238-238-23 8-238 60-60 238 238 238-238z", Color.DARKSLATEGRAY);
		glyphClose.setSize(20, 20);
		closeButton.setGraphic(glyphClose);

		closeDialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
		closeButton.setOnMouseClicked(event -> closeDialog.show(__rootPane));
		dialogAcceptButton.setOnAction(event -> {
			closeDialog.close();
			Platform.runLater(() -> {
				Platform.exit();
				System.exit(0);
			});
		});
		dialogDeclineButton.setOnAction(event -> closeDialog.close());

		// save the mouse pressed position when clicking on the decorator pane
		toolbar.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
			xOffset = mouseEvent.getSceneX();
			yOffset = mouseEvent.getSceneY();
		});
		// handle drag events on the decorator pane
		toolbar.setOnMouseDragged(mouseEvent -> {
			if (!mouseEvent.isPrimaryButtonDown() || (xOffset == -1 && yOffset == -1) || mouseEvent.isStillSincePress())
				return;

			((Stage) context.getRegisteredObject("stage")).setX(mouseEvent.getScreenX() - xOffset);
			((Stage) context.getRegisteredObject("stage")).setY(mouseEvent.getScreenY() - yOffset);
			mouseEvent.consume();
		});
	}
}
