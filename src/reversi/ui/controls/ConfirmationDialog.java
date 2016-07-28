/**
 * Created by kanari on 2016/7/26.
 */

package ui.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import util.BackgroundColorAnimator;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import util.TaskScheduler;

import java.io.IOException;
import java.util.Timer;

public class ConfirmationDialog extends JFXDialog {
	@FXML
	private Label heading;

	@FXML
	private JFXDialogLayout content;

	@FXML
	private JFXButton acceptButton, declineButton;

	private ObjectProperty<EventHandler<JFXDialogEvent>> onAccepted, onDeclined;

	public ConfirmationDialog() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/ConfirmationDialog.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		onAccepted = new SimpleObjectProperty<>(null);
		onDeclined = new SimpleObjectProperty<>(null);
		BackgroundColorAnimator.applyAnimation(acceptButton);
		BackgroundColorAnimator.applyAnimation(declineButton);
		acceptButton.setOnAction(e -> {
			if (onAccepted.get() != null) {
				setOnDialogClosed(onAccepted.get());
			} else {
				setOnDialogClosed(event -> {
				});
			}
			close();
		});
		declineButton.setOnAction(e -> {
			if (onDeclined.get() != null) {
				setOnDialogClosed(onDeclined.get());
			} else {
				setOnDialogClosed(event -> {
				});
			}
			close();
		});

		setOnKeyPressed(e -> {
			if (!isOverlayClose()) return;
			if (e.getCode() == KeyCode.ESCAPE)
				declineButton.fire();
			else if (e.getCode() == KeyCode.ENTER)
				acceptButton.fire();
		});
		setOverlayClose(false);
		onDeclinedProperty().addListener(((observable, oldValue, newValue) -> setOnDialogClosed(newValue)));
	}

	@Override
	public void show(StackPane dialogContainer) {
		super.show(dialogContainer);
		// focus must be requested after a short delay... dunno why
		TaskScheduler.singleShot(100, () -> Platform.runLater(this::requestFocus));
	}

	@Override
	public void show() {
		super.show();
		TaskScheduler.singleShot(100, () -> Platform.runLater(this::requestFocus));
	}

	public String getHeading() {
		return this.heading.getText();
	}

	public void setHeading(String heading) {
		this.heading.setText(heading);
	}

	public StringProperty headingProperty() {
		return this.heading.textProperty();
	}

	public ObservableList<Node> getBody() {
		return this.content.getBody();
	}

	public void setBody(Node... body) {
		this.content.setBody(body);
	}

	public EventHandler<JFXDialogEvent> getOnAccepted() {
		return onAccepted.get();
	}

	public ObjectProperty<EventHandler<JFXDialogEvent>> onAcceptedProperty() {
		return onAccepted;
	}

	public void setOnAccepted(EventHandler<JFXDialogEvent> onAccepted) {
		this.onAccepted.set(onAccepted);
	}

	public EventHandler<JFXDialogEvent> getOnDeclined() {
		return onDeclined.get();
	}

	public ObjectProperty<EventHandler<JFXDialogEvent>> onDeclinedProperty() {
		return onDeclined;
	}

	public void setOnDeclined(EventHandler<JFXDialogEvent> onDeclined) {
		this.onDeclined.set(onDeclined);
	}

	public String getAcceptButtonText() {
		return this.acceptButton.getText();
	}

	public void setAcceptButtonText(String acceptButtonText) {
		this.acceptButton.setText(acceptButtonText.toUpperCase());
	}

	public StringProperty acceptButtonTextProperty() {
		return this.acceptButton.textProperty();
	}

	public String getDeclineButtonText() {
		return this.declineButton.getText();
	}

	public void setDeclineButtonText(String declineButtonText) {
		this.declineButton.setText(declineButtonText.toUpperCase());
	}

	public StringProperty declineButtonTextProperty() {
		return this.declineButton.textProperty();
	}

	public boolean isDeclineButtonVisible() {
		return declineButton.isVisible();
	}

	public void setDeclineButtonVisible(boolean visible) {
		declineButton.setVisible(visible);
	}

	public BooleanProperty declineButtonVisibleProperty() {
		return declineButton.visibleProperty();
	}
}
