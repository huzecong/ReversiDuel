/**
 * Created by kanari on 2016/7/25.
 */

import Controls.ConfirmationDialog;
import Controls.IconListItem;
import Controls.InformationDialog;
import Network.ConnectionManager;
import Utility.Synchronous;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.datafx.controller.FXMLController;

import javax.annotation.PostConstruct;

@FXMLController(value = "fxml/ConnectPage.fxml", title = "Select Opponent")
public class ConnectPageController {
	@FXML
	private AnchorPane rootPane;

	@FXML
	private StackPane __rootPane;

	@FXML
	private Label noMatchLabel;

	@FXML
	private JFXListView<IconListItem> matchList;

	@FXML
	private InformationDialog connectionCancelledDialog;

	@FXML
	private ConfirmationDialog newClientDialog, connectToHostDialog, searchClientDialog;

	@FXML
	private IconListItem newClientDataItem, hostDataItem;

	private ConnectionManager connectionManager;

	private JFXDialog currentDialog;

	@FXML
	public void createMatch() {
		connectionManager.startHost();
		searchClientDialog.show(__rootPane);
		currentDialog = searchClientDialog;
	}

	private void connectToHost(IconListItem item) {
		connectionManager.connectToHost(item.getHostData());
		hostDataItem.setUsingHostData(item.getHostData());
		connectToHostDialog.show(__rootPane);
		currentDialog = connectToHostDialog;
	}

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

		matchList.getFocusModel().focusedIndexProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("focus " + newValue);
		});

		connectionCancelledDialog.setHeading("Connection cancelled");
		connectionCancelledDialog.setOnDialogClosed(e -> currentDialog.close());

		searchClientDialog.setOnAccepted(e -> connectionManager.abortHost());

		connectToHostDialog.setOnAccepted(e -> connectionManager.abortConnectionToHost());

		connectionManager = new ConnectionManager("Someone", "ha.gif", 123);
		connectionManager.setOnAddToHostList(hostData -> {
			IconListItem newItem = new IconListItem();
			newItem.setUsingHostData(hostData);
			newItem.setOnMouseClicked(e -> connectToHost(newItem));
			Platform.runLater(() -> matchList.getItems().add(newItem));
		});
		connectionManager.setOnRemoveHostListIndex(index ->
				Platform.runLater(() -> matchList.getItems().remove(index.intValue())));
		connectionManager.setOnCancelConnection(message -> {
			connectionCancelledDialog.setContents(message);
			Platform.runLater(() -> connectionCancelledDialog.show(__rootPane));
		});
		connectionManager.setOnNewClientJoined(clientData -> {
			newClientDataItem.setUsingHostData(clientData);
			Synchronous<Boolean> accepted = new Synchronous<>();
			newClientDialog.setOnAccepted(e -> accepted.setValue(true));
			newClientDialog.setOnDeclined(e -> accepted.setValue(false));
			Platform.runLater(() -> newClientDialog.show(__rootPane));
			currentDialog = newClientDialog;
			return accepted.getValue();
		});
		connectionManager.setOnConnectionConfirmed(hostData -> {
			System.out.println("Connection confirmed with " + hostData.getProfileName() + " from " + hostData.getIP());
		});
	}
}
