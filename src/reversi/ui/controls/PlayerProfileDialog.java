/**
 * Created by kanari on 2016/7/31.
 */

package ui.controls;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.validation.NumberValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class PlayerProfileDialog extends ConfirmationDialog {
	@FXML
	private ToggleGroup playerTypeToggleGroup;

	@FXML
	private JFXComboBox AIStrengthComboBox;

	@FXML
	private JFXTextField profileNameText, timeLimitText;

	@FXML
	private GridPane avatarList;

	@FXML
	private Boolean localSelected;

	private static final List<String> avatarNames = Arrays.asList("gomoku.jpg", "gumi.jpg", "ha.gif", "honoka.jpg", "monkey.gif",
			"nanami.jpg", "rabbit.jpg", "sillyb.jpg", "think.gif", "xsk.gif", "young.jpg");

	private static final String ITEM_STYLE = "-fx-background-color: WHITE; -fx-background-radius: 5px";
	private static final String SELECTED_ITEM_STYLE = ITEM_STYLE + "; -fx-border-color: rgba(0, 0, 0, 0.4); -fx-border-width: 2px; -fx-border-radius: 5px";

	private HashMap<String, Pane> avatars;

	private StringProperty avatarID;

	public PlayerProfileDialog() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/PlayerProfileDialog.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int row = 0, col = 0;
		avatars = new HashMap<>();
		for (String filename : avatarNames) {
			Image image = new Image(getClass().getResource("avatar/" + filename).toExternalForm(), 60, 60, true, true);
			ImageView view = new ImageView(image);
			StackPane pane = new StackPane();
			pane.setPadding(new Insets(5, 5, 5, 5));
			pane.getChildren().add(view);
			pane.setStyle("");
			pane.setUserData(filename);
			pane.setOnMouseClicked(e -> avatarID.set((String) pane.getUserData()));
			pane.setStyle(ITEM_STYLE);
			pane.setMinWidth(70);
			pane.setPrefWidth(70);
			pane.setMaxWidth(70);
			pane.setMinHeight(70);
			pane.setPrefHeight(70);
			pane.setMaxHeight(70);
			JFXDepthManager.setDepth(pane, 1);

			avatars.put(filename, pane);
			GridPane.setRowIndex(pane, row);
			GridPane.setColumnIndex(pane, col);
			avatarList.getChildren().add(pane);
			if (++col == 4) {
				col = 0;
				++row;
			}
		}
		for (int i = 0; i < 4; ++i)
			avatarList.getColumnConstraints().add(new ColumnConstraints(70));
		for (int i = 0; i < row; ++i)
			avatarList.getRowConstraints().add(new RowConstraints(i == row - 1 ? 80 : 70));
	}

	private String uniqueID;

	public void show(Properties properties) {
		profileNameText.setText(properties.getProperty("profileName"));
		avatarID = new SimpleStringProperty("");
		avatarID.addListener((observable, oldValue, newValue) -> {
			Pane oldPane = avatars.get(oldValue);
			Pane newPane = avatars.get(newValue);
			if (oldPane != null) {
				JFXDepthManager.setDepth(oldPane, 1);
				oldPane.setStyle(ITEM_STYLE);
			}
			JFXDepthManager.setDepth(newPane, 3);
			newPane.setStyle(SELECTED_ITEM_STYLE);
		});
		avatarID.set(properties.getProperty("avatarID"));
		String playerType = properties.getProperty("playerType");
		for (Toggle toggle : playerTypeToggleGroup.getToggles())
			if (toggle.getUserData().equals(playerType)) {
				toggle.setSelected(true);
				break;
			}
		uniqueID = properties.getProperty("uniqueID");
		timeLimitText.setText(properties.getProperty("timeLimit"));

		super.show();
	}

	public Properties getPlayerProperties() {
		Properties properties = new Properties();
		properties.setProperty("profileName", profileNameText.getText());
		properties.setProperty("avatarID", avatarID.get());
		properties.setProperty("uniqueID", uniqueID);
		properties.setProperty("playerType", (String) playerTypeToggleGroup.getSelectedToggle().getUserData());
		properties.setProperty("timeLimit", timeLimitText.getText());
		return properties;
	}
}
