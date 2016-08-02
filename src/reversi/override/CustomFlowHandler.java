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

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class CustomFlowHandler extends FlowHandler {
	private ObservableList<ViewHistoryDefinition<?>> controllerHistory;

	private HashMap<Class, Consumer<Class>> onNavigatingBack;

	public Consumer<Class> getOnNavigatingBack(Class from) {
		return onNavigatingBack.get(from);
	}

	public void setOnNavigatingBack(Class klass, Consumer<Class> handler) {
		onNavigatingBack.put(klass, handler);
	}

	public CustomFlowHandler(Flow flow, ViewFlowContext flowContext) {
		super(flow, flowContext);
		controllerHistory = FXCollections.observableArrayList();
		onNavigatingBack = new HashMap<>();
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

		Class currentClass = getCurrentView().getViewContext().getController().getClass();
		do {
			Consumer<Class> handler = onNavigatingBack.get(currentClass);
			if (handler != null) {
				handler.accept(controllerClass);
				break;
			}
			currentClass = currentClass.getSuperclass();
		} while (currentClass != null);

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
