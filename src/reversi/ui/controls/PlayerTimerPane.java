/**
 * Created by kanari on 2016/7/27.
 */

package ui.controls;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class PlayerTimerPane extends AnchorPane {
	@FXML
	private Label scoreText, nameText, totalTimeText, remainingTimeText;

	@FXML
	private ImageView avatarView;

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
		totalTime = new SimpleIntegerProperty(-1);
		remainingTime = new SimpleIntegerProperty(0);
		name = new SimpleStringProperty("");
	}

	private IntegerProperty score, totalTime, remainingTime;
	private StringProperty name;

	public Image getIcon() {
		return iconProperty().get();
	}

	public void setIcon(Image value) {
		iconProperty().set(value);
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
		scoreText.setText(String.valueOf(score));
	}

	public int getTotalTime() {
		return totalTimeProperty().get();
	}

	public IntegerProperty totalTimeProperty() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		totalTimeProperty().set(totalTime);
		totalTimeText.setText("Total time:\n"
				+ String.format("%02d:%02d", totalTime / 60, totalTime % 60));
	}

	public int getRemainingTime() {
		return remainingTimeProperty().get();
	}

	public IntegerProperty remainingTimeProperty() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		remainingTimeProperty().set(remainingTime);
		remainingTimeText.setText(String.valueOf(remainingTime));
	}

	public String getName() {
		return nameProperty().get();
	}

	public StringProperty nameProperty() {
		return name;
	}

	public void setName(String name) {
		nameProperty().set(name);
		nameText.setText(name);
	}
}
