/**
 * Created by kanari on 2016/7/25.
 */

package override;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import org.datafx.controller.context.ViewContext;
import org.datafx.controller.flow.*;
import org.datafx.controller.flow.action.FlowLink;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;

import java.util.UUID;

public class CustomFlowHandler extends FlowHandler {
	private ObservableList<ViewHistoryDefinition<?>> controllerHistory;

	public interface NavigatingBackHandler {
		public void handle(Class from, Class to);
	}

	private NavigatingBackHandler onNavigatingBack;

	public NavigatingBackHandler getOnNavigatingBack() {
		return onNavigatingBack;
	}

	public void setOnNavigatingBack(NavigatingBackHandler onNavigatingBack) {
		this.onNavigatingBack = onNavigatingBack;
	}

	public CustomFlowHandler(Flow flow, ViewFlowContext flowContext) {
		super(flow, flowContext);
		controllerHistory = FXCollections.observableArrayList();
	}

	@Override
	public void attachAction(Node node, Runnable action) {
		if (node instanceof ButtonBase) {
			((ButtonBase) node).setOnAction((e) -> action.run());
		} else {
			node.setOnMouseClicked((ev) -> action.run());
		}
	}

	@Override
	public void attachEventHandler(Node node, String actionId) {
		if (node instanceof ButtonBase) {
			((ButtonBase) node).setOnAction((e) -> handleActionWithExceptionHandler(actionId));
		} else {
			node.setOnMouseClicked((e) -> handleActionWithExceptionHandler(actionId));
		}
	}

	@Override
	public void attachBackEventHandler(Node node) {
		if (node instanceof ButtonBase) {
			((ButtonBase) node).setOnAction((e) -> handleBackActionWithExceptionHandler());
		} else {
			node.setOnMouseClicked((e) -> handleBackActionWithExceptionHandler());
		}
	}

	@Override
	public <U> ViewContext<U> setNewView(FlowView<U> newView, boolean addOldToHistory)
			throws FlowException {
		if (getCurrentView() != null && addOldToHistory) {
			ViewHistoryDefinition<?> historyDefinition = new ViewHistoryDefinition(getCurrentView().getViewContext().getController().getClass(), "", null);
			controllerHistory.add(0, historyDefinition);
		}
		return super.setNewView(newView, addOldToHistory);
	}

	@Override
	public void navigateToHistoryIndex(int index) throws VetoException, FlowException {
		Class<?> controllerClass = controllerHistory.remove(index).getControllerClass();
		onNavigatingBack.handle(getCurrentView().getViewContext().getController().getClass(), controllerClass);
		for (int i = 0; i < index; ++i)
			controllerHistory.remove(i);
		handle(new FlowLink(controllerClass, false), "backAction-" + UUID.randomUUID().toString());
	}

	@Override
	public ObservableList<ViewHistoryDefinition<?>> getControllerHistory() {
		return FXCollections.unmodifiableObservableList(controllerHistory);
	}

	private void handleActionWithExceptionHandler(String id) {
		try {
			super.handle(id);
		} catch (VetoException | FlowException e) {
			getExceptionHandler().setException(e);
		}
	}

	private void handleBackActionWithExceptionHandler() {
		try {
			super.navigateBack();
		} catch (VetoException | FlowException e) {
			getExceptionHandler().setException(e);
		}
	}
}
