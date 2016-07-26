/**
 * Created by kanari on 2016/7/25.
 */

import Controls.IconListItem;
import NetworkUtils.ConnectionManager;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.effects.JFXDepthManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import org.datafx.controller.FXMLController;

import javax.annotation.PostConstruct;

@FXMLController(value = "fxml/ConnectPage.fxml", title = "Select Opponent")
public class ConnectPageController {
	@FXML
	private AnchorPane rootPane;

	@FXML
	private Label noMatchLabel;

	@FXML
	private JFXListView<IconListItem> matchList;

	private ConnectionManager connectionManager;

	@PostConstruct
	public void init() {
		JFXDepthManager.setDepth(rootPane, 1);
		matchList.getItems().addListener(new ListChangeListener<IconListItem>() {
			@Override
			public void onChanged(Change<? extends IconListItem> c) {
				if (matchList.getItems().size() == 0) {
					noMatchLabel.setVisible(true);
					noMatchLabel.setMaxHeight(Region.USE_COMPUTED_SIZE);
					noMatchLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
					noMatchLabel.setMinHeight(Region.USE_COMPUTED_SIZE);
				} else {
					noMatchLabel.setVisible(false);
					noMatchLabel.setMaxHeight(0);
					noMatchLabel.setPrefHeight(0);
					noMatchLabel.setMinHeight(0);
				}
			}
		});
		IconListItem item = new IconListItem();
		item.setName("果皇");
		item.setIP("127.0.0.1");
		item.setIcon(new Image("avatar/ha.gif"));
		matchList.getItems().add(item);

		matchList.getFocusModel().focusedIndexProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("focus " + newValue);
		});

		connectionManager = new ConnectionManager("Someone", "ha.gif", 123);
		connectionManager.setOnAddToHostList(hostData -> {
			IconListItem newItem = new IconListItem();
			newItem.setName(hostData.getProfileName());
			newItem.setIP(hostData.getIP());
			newItem.setIcon(new Image("avatar/" + hostData.getAvatarID()));
			matchList.getItems().add(newItem);
		});
		connectionManager.setOnRemoveHostListIndex(index -> matchList.getItems().remove(index.intValue()));
	}
}
