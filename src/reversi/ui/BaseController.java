/**
 * Created by kanari on 2016/7/24.
 */

package ui;

import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.*;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;
import override.*;
import ui.controls.ConfirmationDialog;
import ui.controls.PlayerProfileDialog;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.function.Consumer;

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

	private FlowHandler flowHandler;

	private static double maxWidth = 1200, minWidth = 800;
	private static double maxHeight = 900, minHeight = 600;
	private static double aspectRatio = maxWidth / maxHeight;

	@PostConstruct
	public void init() throws FlowException {
		Flow innerFlow = new Flow(MainMenuController.class)
				.withLink(MainMenuController.class, "singlePlayer", LocalGameConfigurePageController.class)
				.withLink(MainMenuController.class, "networkDuel", ConnectPageController.class)
				.withLink(MainMenuController.class, "profile", ProfilePageController.class);
		EventHandler<Event> closeHandler = event -> closeDialog.show(__rootPane);
		Stage stage = (Stage) context.getRegisteredObject("stage");
		CustomAnimatedFlowContainer container = new CustomAnimatedFlowContainer(Duration.millis(400));
		context.register("closeHandler", closeHandler);
		context.register("returnToHome", (Runnable) () -> Platform.runLater(() -> {
			try {
				flowHandler.navigateToHistoryIndex(flowHandler.getControllerHistory().size() - 1);
			} catch (VetoException | FlowException e) {
				e.printStackTrace();
			}
//			while (!container.isIsInitialView()) {
//				try {
//					flowHandler.navigateBack();
//				} catch (VetoException | FlowException e) {
//					e.printStackTrace();
//				}
//			}
		}));

		stage.titleProperty().unbind();
		titleLabel.textProperty().bind(stage.titleProperty());

		__rootPane.widthProperty().addListener(observable -> System.out.println(__rootPane.getParent().getLayoutBounds()));
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

		// make tool bar draggable
		{
			final DoubleProperty xOffset = new SimpleDoubleProperty(-1);
			final DoubleProperty yOffset = new SimpleDoubleProperty(-1);
			toolbar.setOnMousePressed(mouseEvent -> {
				xOffset.set(mouseEvent.getSceneX());
				yOffset.set(mouseEvent.getSceneY());
			});
			titleLabel.setOnMousePressed(toolbar.getOnMousePressed());

			toolbar.setOnMouseDragged(mouseEvent -> {
				if (!mouseEvent.isPrimaryButtonDown()
						|| xOffset.get() == -1 || yOffset.get() == -1
						|| mouseEvent.isStillSincePress()) return;
				stage.setX(mouseEvent.getScreenX() - xOffset.get());
				stage.setY(mouseEvent.getScreenY() - yOffset.get());
				mouseEvent.consume();
			});
			titleLabel.setOnMouseDragged(toolbar.getOnMouseDragged());
		}

		// make left side resizable
		{
			final DoubleProperty xOffset = new SimpleDoubleProperty(-1);
			final DoubleProperty xSceneOffset = new SimpleDoubleProperty(-1);
			final DoubleProperty width = new SimpleDoubleProperty(-1);
			borderShadows.getChildren().get(1).setOnMouseMoved(mouseEvent -> {
				if (mouseEvent.getSceneX() >= 0) stage.getScene().setCursor(Cursor.H_RESIZE);
				else stage.getScene().setCursor(Cursor.DEFAULT);
			});
			borderShadows.getChildren().get(1).setOnMouseExited(e ->
					stage.getScene().setCursor(Cursor.DEFAULT));
			borderShadows.getChildren().get(1).setOnMousePressed(mouseEvent -> {
				xOffset.set(mouseEvent.getScreenX());
				xSceneOffset.set(mouseEvent.getSceneX());
				width.setValue(stage.getWidth());
			});
			borderShadows.getChildren().get(1).setOnMouseDragged(mouseEvent -> {
				if (!mouseEvent.isPrimaryButtonDown()
						|| xOffset.get() == -1 || xSceneOffset.get() == -1 || width.get() == -1
						|| mouseEvent.isStillSincePress()) return;
				double curWidth = width.get() + (xOffset.get() - mouseEvent.getScreenX());
				curWidth = Math.min(Math.max(curWidth, minWidth), maxWidth);
				stage.setX(width.get() - curWidth + xOffset.get() - xSceneOffset.get());
				stage.setWidth(curWidth);
				stage.setHeight(curWidth / aspectRatio);
			});
		}

		// make right side resizable
		{
			final DoubleProperty xOffset = new SimpleDoubleProperty(-1);
			final DoubleProperty width = new SimpleDoubleProperty(-1);
			borderShadows.getChildren().get(2).setOnMouseMoved(mouseEvent -> {
				if (mouseEvent.getSceneX() <= stage.getWidth()) stage.getScene().setCursor(Cursor.H_RESIZE);
				else stage.getScene().setCursor(Cursor.DEFAULT);
			});
			borderShadows.getChildren().get(2).setOnMouseExited(e ->
					stage.getScene().setCursor(Cursor.DEFAULT));
			borderShadows.getChildren().get(2).setOnMousePressed(mouseEvent -> {
				xOffset.set(mouseEvent.getScreenX());
				width.setValue(stage.getWidth());
			});
			borderShadows.getChildren().get(2).setOnMouseDragged(mouseEvent -> {
				if (!mouseEvent.isPrimaryButtonDown()
						|| xOffset.get() == -1 || width.get() == -1
						|| mouseEvent.isStillSincePress()) return;
				double curWidth = width.get() + (mouseEvent.getScreenX() - xOffset.get());
				curWidth = Math.min(Math.max(curWidth, minWidth), maxWidth);
				stage.setWidth(curWidth);
				stage.setHeight(curWidth / aspectRatio);
			});
		}

		// make lower side resizable
		{
			final DoubleProperty yOffset = new SimpleDoubleProperty(-1);
			final DoubleProperty height = new SimpleDoubleProperty(-1);
			borderShadows.getChildren().get(3).setOnMouseMoved(mouseEvent -> {
				if (mouseEvent.getSceneY() <= stage.getHeight()) stage.getScene().setCursor(Cursor.V_RESIZE);
				else stage.getScene().setCursor(Cursor.DEFAULT);
			});
			borderShadows.getChildren().get(3).setOnMouseExited(e ->
					stage.getScene().setCursor(Cursor.DEFAULT));
			borderShadows.getChildren().get(3).setOnMousePressed(mouseEvent -> {
				yOffset.set(mouseEvent.getScreenY());
				height.setValue(stage.getHeight());
			});
			borderShadows.getChildren().get(3).setOnMouseDragged(mouseEvent -> {
				if (!mouseEvent.isPrimaryButtonDown()
						|| yOffset.get() == -1 || height.get() == -1
						|| mouseEvent.isStillSincePress()) return;
				double curHeight = height.get() + (mouseEvent.getScreenY() - yOffset.get());
				curHeight = Math.min(Math.max(curHeight, minHeight), maxHeight);
				stage.setHeight(curHeight);
				stage.setWidth(curHeight * aspectRatio);
			});
		}

		// resize base elements on window size change

		Runnable windowResizer = () -> {
			double width = stage.getWidth();
			double scale = width / maxWidth;
			stage.getScene().getRoot().setScaleX(scale);
			stage.getScene().getRoot().setScaleY(scale);
		};
		stage.sceneProperty().addListener((observable, oldValue, newValue) -> {
			newValue.rootProperty().addListener(ob -> windowResizer.run());
			// initialize window to appropriate size
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			stage.setHeight(Math.min(maxHeight, primaryScreenBounds.getHeight() - 150));
			stage.setWidth(aspectRatio * stage.getHeight());
			windowResizer.run();
		});
		stage.widthProperty().addListener(ob -> windowResizer.run());
	}
}
