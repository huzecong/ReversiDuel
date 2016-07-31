/**
 * Created by kanari on 2016/7/25.
 */

package override;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.datafx.controller.context.ViewContext;
import org.datafx.controller.flow.FlowContainer;
import org.datafx.controller.flow.container.AnimatedFlowContainer;

import java.util.*;
import java.util.function.Function;


/**
 * Rewrite of original AnimatedFlowController in DataFX
 * Animation is set to SWIPE_LEFT for normal navigation, and SWIPE_RIGHT for navigating back
 *
 * Due to the fact that a number of functions are private but not protected in the original class,
 * I had to rewrite instead of extend the class
 */
public class CustomAnimatedFlowContainer implements FlowContainer<StackPane> {

	private Stack<Integer> historyViews;
	private StackPane root;
	private Duration duration;
	private Function<AnimatedFlowContainer, List<KeyFrame>> animationProducer;
	private Timeline animation;
	private ImageView placeholder;

	// Copied from ContainerAnimations, in order to match function signature
	private static Function<CustomAnimatedFlowContainer, List<KeyFrame>> SWIPE_LEFT = c ->
			new ArrayList<>(Arrays.asList(
					new KeyFrame(Duration.ZERO,
							new KeyValue(c.getView().translateXProperty(), c.getView().getWidth(), Interpolator.EASE_BOTH),
							new KeyValue(c.getPlaceholder().translateXProperty(), -c.getView().getWidth(), Interpolator.EASE_BOTH)),
					new KeyFrame(c.getDuration(),
							new KeyValue(c.getView().translateXProperty(), 0, Interpolator.EASE_BOTH),
							new KeyValue(c.getPlaceholder().translateXProperty(), -c.getView().getWidth(), Interpolator.EASE_BOTH))
			));
	private static Function<CustomAnimatedFlowContainer, List<KeyFrame>> SWIPE_RIGHT = c ->
			new ArrayList<>(Arrays.asList(
					new KeyFrame(Duration.ZERO,
							new KeyValue(c.getView().translateXProperty(), -c.getView().getWidth(), Interpolator.EASE_BOTH),
							new KeyValue(c.getPlaceholder().translateXProperty(), c.getView().getWidth(), Interpolator.EASE_BOTH)),
					new KeyFrame(c.getDuration(),
							new KeyValue(c.getView().translateXProperty(), 0, Interpolator.EASE_BOTH),
							new KeyValue(c.getPlaceholder().translateXProperty(), c.getView().getWidth(), Interpolator.EASE_BOTH))
			));

	private BooleanProperty isInitialView;

	public boolean isIsInitialView() {
		return isInitialView.get();
	}

	public BooleanProperty isInitialViewProperty() {
		return isInitialView;
	}

	/**
	 * Creates a container with the given animation type and duration
	 *
	 * @param duration the duration of the animation
	 */
	public CustomAnimatedFlowContainer(Duration duration) {
		this.root = new StackPane();
		this.root.setAlignment(Pos.CENTER);
		this.duration = duration;
		placeholder = new ImageView();
		placeholder.setPreserveRatio(true);
		placeholder.setSmooth(true);
		historyViews = new Stack<>();
		isInitialView = new SimpleBooleanProperty(true);
	}

	@Override
	public <U> void setViewContext(ViewContext<U> context) {
		updatePlaceholder(context.getRootNode());

		if (animation != null) {
			animation.stop();
		}
		animation = new Timeline();

		Integer hash = context.getController().getClass().hashCode();
		int pos = historyViews.indexOf(hash);
		if (historyViews.size() > 1 && pos != -1 && pos < historyViews.size() - 1) {
			for (int i = historyViews.size() - 1; i > pos; --i)
				historyViews.remove(i);
			animation.getKeyFrames().addAll(SWIPE_RIGHT.apply(this));
		} else {
			animation.getKeyFrames().addAll(SWIPE_LEFT.apply(this));
			historyViews.push(hash);
		}

		animation.getKeyFrames().add(new KeyFrame(duration, e -> clearPlaceholder()));
		animation.play();
		isInitialView.setValue(historyViews.size() == 1);
	}

	/**
	 * Returns the {@link ImageView} instance that is used as a placeholder for the old view in each navigation animation.
	 *
	 * @return
	 */
	public ImageView getPlaceholder() {
		return placeholder;
	}

	/**
	 * Returns the duration for the animation
	 *
	 * @return the duration for the animation
	 */
	public Duration getDuration() {
		return duration;
	}

	@Override
	public StackPane getView() {
		return root;
	}

	private void clearPlaceholder() {
		placeholder.setImage(null);
		placeholder.setVisible(false);
	}

	private void updatePlaceholder(Node newView) {
		if (root.getWidth() > 0 && root.getHeight() > 0) {
			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(Color.TRANSPARENT);
			parameters.setViewport(new Rectangle2D(0, 0, root.getWidth(), root.getHeight()));   // bug fix
			Image placeholderImage = root.snapshot(parameters, new WritableImage((int) root.getWidth(), (int) root.getHeight()));
			placeholder.setImage(placeholderImage);
			placeholder.setFitWidth(placeholderImage.getWidth());
			placeholder.setFitHeight(placeholderImage.getHeight());
		} else {
			placeholder.setImage(null);
		}
		placeholder.setVisible(true);
		placeholder.setOpacity(1.0);
		root.getChildren().setAll(placeholder);
		root.getChildren().add(newView);
		placeholder.toFront();
	}
}
