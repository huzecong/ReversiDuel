/**
 * Created by kanari on 2016/7/27.
 */

package Utility;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Adds background color fade effects for mouse hover events.
 */
public class BackgroundColorAnimator {

	public static void applyAnimation(Node node) {
		ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();
		StringProperty colorStyleProperty = new SimpleStringProperty();
		colorProperty.addListener((observable, oldValue, newValue) -> {
			colorStyleProperty.setValue("rgba("
					+ (int) (newValue.getRed() * 255) + ","
					+ (int) (newValue.getRed() * 255) + ","
					+ (int) (newValue.getRed() * 255) + ","
					+ String.format("%.5f", newValue.getOpacity()) + ")");
		});
		colorProperty.setValue(Color.WHITE);
		Timeline animation = new Timeline(
				new KeyFrame(Duration.millis(0), new KeyValue(colorProperty, Color.WHITE)),
				new KeyFrame(Duration.millis(150), new KeyValue(colorProperty, Color.color(0.95, 0.95, 0.95)))
		);
		node.styleProperty().bind(new SimpleStringProperty("-fx-background-color: ")
				.concat(colorStyleProperty).concat("; " + node.getStyle()));
		node.hoverProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) animation.setRate(1);
			else animation.setRate(-1);
			animation.play();
		});
	}
}
