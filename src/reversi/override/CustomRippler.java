/**
 * Created by kanari on 2016/7/25.
 */

package override;

import com.jfoenix.controls.JFXRippler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class CustomRippler extends JFXRippler {

	@Override
	protected Node getMask() {
		double borderWidth = ripplerPane.getBorder() != null ? ripplerPane.getBorder().getInsets().getTop() : 0;
		Bounds bounds = control.getBoundsInParent();
		Shape mask = new Rectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth() - 0.1 - 2 * borderWidth, bounds.getHeight() - 0.1 - 2 * borderWidth); // -0.1 to prevent resizing the anchor pane
		if (getMaskType().equals(RipplerMask.CIRCLE)) {
			double radius = Math.min((bounds.getWidth() / 2) - 0.1 - 2 * borderWidth, (bounds.getHeight() / 2) - 0.1 - 2 * borderWidth);
			mask = new Circle((bounds.getMinX() + bounds.getMaxX()) / 2, (bounds.getMinY() + bounds.getMaxY()) / 2, radius, Color.BLUE);
		}
		return mask;
	}
}