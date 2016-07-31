/**
 * Created by kanari on 2016/7/27.
 */

package ui.controls;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import logic.PlayerState;

import java.io.IOException;

public class PlayerTimerPane extends AnchorPane {
	@FXML
	private Label scoreText, nameText, remainingTimeText, colorText;

	@FXML
	private ImageView avatarView, colorPiece;

	public PlayerTimerPane() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/PlayerTimerPane.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		score = new SimpleIntegerProperty(0);
		remainingTime = new SimpleIntegerProperty(0);
		stateProperty = new SimpleObjectProperty<>(PlayerState.NONE);

		score.addListener((observable, oldValue, newValue) ->
				Platform.runLater(() -> scoreText.setText(String.valueOf(newValue))));
		remainingTime.addListener(((observable, oldValue, newValue) ->
				Platform.runLater(() -> remainingTimeText.setText(String.valueOf(newValue)))));
		stateProperty.addListener((observable, oldValue, newValue) ->
				Platform.runLater(() -> {
					if (newValue == PlayerState.BLACK) {
						colorText.setText("BLACK");
						colorPiece.setImage(new Image("image/black_small.png", 20, 20, true, true));
					} else if (newValue == PlayerState.WHITE) {
						colorText.setText("WHITE");
						colorPiece.setImage(new Image("image/white_small.png", 20, 20, true, true));
					}
					colorPiece.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.26), 5, 0.12, 0, 2));
				}));
	}

	private IntegerProperty score, remainingTime;
	private ObjectProperty<PlayerState> stateProperty;

	public Image getIcon() {
		return iconProperty().get();
	}

	public void setIcon(Image icon) {
		iconProperty().set(icon);
	}

	public void setIcon(String avatarID) {
		iconProperty().set(new Image(getClass().getResource("avatar/" + avatarID).toExternalForm()));
	}

	public ObjectProperty<Image> iconProperty() {
		if (avatarView == null) avatarView = new ImageView();
		return avatarView.imageProperty();
	}

	public int getScore() {
		return scoreProperty().get();
	}

	public IntegerProperty scoreProperty() {
		return score;
	}

	public void setScore(int score) {
		scoreProperty().set(score);
	}

	public int getRemainingTime() {
		return remainingTimeProperty().get();
	}

	public IntegerProperty remainingTimeProperty() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		remainingTimeProperty().set(remainingTime);
	}

	public String getName() {
		return nameProperty().get();
	}

	public StringProperty nameProperty() {
		return nameText.textProperty();
	}

	public void setName(String name) {
		nameProperty().set(name);
	}

	public PlayerState getState() {
		return stateProperty().get();
	}

	public ObjectProperty<PlayerState> stateProperty() {
		return stateProperty;
	}

	public void setState(PlayerState stateProperty) {
		this.stateProperty().set(stateProperty);
	}
}
