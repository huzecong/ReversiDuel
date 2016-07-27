/**
 * Created by kanari on 2016/7/25.
 */

package override;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.FlowHandler;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;

public class CustomFlowHandler extends FlowHandler {
	public CustomFlowHandler(Flow flow, ViewFlowContext flowContext) {
		super(flow, flowContext);
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
