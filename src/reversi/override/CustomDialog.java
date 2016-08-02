/**
 * Created by kanari on 2016/7/29.
 */

package override;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import util.Synchronous;
import util.TaskScheduler;

public class CustomDialog extends JFXDialog {
	@Override
	public void show(StackPane dialogContainer) {
		super.show(dialogContainer);
		// focus must be requested after a short delay... dunno why
		TaskScheduler.singleShot(200, () -> Platform.runLater(this::requestFocus));
	}

	@Override
	public void show() {
		super.show();
		TaskScheduler.singleShot(200, () -> Platform.runLater(this::requestFocus));
	}

	private ObjectProperty<EventHandler<? super JFXDialogEvent>> onDialogClosedProperty = new SimpleObjectProperty<>((closed) -> {
	});

	@Override
	public void setOnDialogClosed(EventHandler<? super JFXDialogEvent> handler) {
		onDialogClosedProperty.setValue(handler);
		super.setOnDialogClosed(handler);
	}

	public EventHandler<? super JFXDialogEvent> getOnDialogClosed() {
		return onDialogClosedProperty.get();
	}

	public void showAndWait() {
		Synchronous<Boolean> isClosed = new Synchronous<>();
		EventHandler<? super JFXDialogEvent> onDialogClosed = getOnDialogClosed();
		super.setOnDialogClosed(e -> {
			isClosed.setValue(true);
			onDialogClosed.handle(e);
			super.setOnDialogClosed(onDialogClosed);
		});
		Platform.runLater(this::show);
		try {
			isClosed.getValue(); // wait for close
		} catch (InterruptedException ignored) {
		}
	}

	/**
	 * Width and height of dialog pane.
	 * Implementation used a dirty trick to get past accessibility restrictions.
	 */
	private DoubleProperty fixedWidth = new SimpleDoubleProperty(Region.USE_PREF_SIZE);
	private DoubleProperty fixedHeight = new SimpleDoubleProperty(Region.USE_PREF_SIZE);

	public double getFixedWidth() {
		return fixedWidth.get();
	}

	public DoubleProperty fixedWidthProperty() {
		return fixedWidth;
	}

	public void setFixedWidth(double fixedWidth) {
		this.fixedWidth.set(fixedWidth);
		((StackPane) getContent().getParent()).setMaxWidth(fixedWidth);
	}

	public double getFixedHeight() {
		return fixedHeight.get();
	}

	public DoubleProperty fixedHeightProperty() {
		return fixedHeight;
	}

	public void setFixedHeight(double fixedHeight) {
		this.fixedHeight.set(fixedHeight);
		((StackPane) getContent().getParent()).setMaxHeight(fixedHeight);
	}
}
