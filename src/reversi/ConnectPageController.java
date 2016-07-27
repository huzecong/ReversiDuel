/**
 * Created by kanari on 2016/7/25.
 */

import Controls.ConfirmationDialog;
import Controls.IconListItem;
import Controls.InformationDialog;
import Network.ConnectionManager;
import Network.HostData;
import Utility.BackgroundColorAnimator;
import Utility.Synchronous;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.datafx.controller.FXMLController;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

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
	private void createMatch() {
		connectionManager.startHost();
		searchClientDialog.show(__rootPane);
		currentDialog = searchClientDialog;
	}

	@FXML
	private void manualIP() {
		manualIPDialog.show(__rootPane);
	}

	private void connectToHost(IconListItem item) {
		connectionManager.connectToHost(item.getHostData());
		hostDataItem.setUsingHostData(item.getHostData());
		connectToHostDialog.show(__rootPane);
		currentDialog = connectToHostDialog;
	}

	private void manualConnectToHost(InetAddress address) {
		connectionManager.manualConnectToHost(address);
		hostDataItem.setUsingHostData(new HostData("<Unknown>", "gomoku.jpg", -1, address, new Date()));
		connectToHostDialog.show(__rootPane);
		currentDialog = connectToHostDialog;
	}

	@FXML
	private ConfirmationDialog manualIPDialog;
	@FXML
	private JFXTextField IPText;
	@FXML
	private GridPane numbersPane;

	@PostConstruct
	public void init() throws UnknownHostException {
		JFXDepthManager.setDepth(rootPane, 1);

		matchList.getFocusModel().focusedItemProperty().addListener((observable, oldValue, newValue) -> {
			try {
				oldValue.getParent().getParent().setStyle("-fx-background-color: WHITE");
				newValue.getParent().getParent().setStyle("-fx-background-color: rgba(242, 242, 242, 1)");
			} catch (NullPointerException ignored) {
			}
		});

		connectionCancelledDialog.setHeading("Connection cancelled");
		connectionCancelledDialog.setOnDialogClosed(e -> {
			if (currentDialog != null && currentDialog.isVisible())
				currentDialog.close();
		});

		searchClientDialog.setOnAccepted(e -> connectionManager.abortHost());
		searchClientDialog.setHeading("Your IP address is: " + InetAddress.getLocalHost().getHostAddress());

		connectToHostDialog.setOnAccepted(e -> connectionManager.abortConnectionToHost());

		String[] avatarNames = {"gomoku.jpg", "gumi.jpg", "ha.gif", "honoka.jpg", "monkey.gif", "nanami.jpg", "rabbit.jpg", "sillyb.jpg", "think.gif", "xsk.gif", "young.jpg"};
		String thisAvatarName = avatarNames[Math.abs(new Random().nextInt()) % avatarNames.length];
		connectionManager = new ConnectionManager(InetAddress.getLocalHost().getHostName() + " " + thisAvatarName, thisAvatarName, new Random().nextInt());
		connectionManager.setOnAddToHostList(hostData -> {
			IconListItem newItem = new IconListItem();
			newItem.setUsingHostData(hostData);
			newItem.setOnMouseClicked(e -> connectToHost(newItem));
			Platform.runLater(() -> matchList.getItems().add(newItem));
		});
		connectionManager.setOnRemoveHostListIndex(index ->
				Platform.runLater(() -> matchList.getItems().remove(index.intValue())));
		connectionManager.setOnCancelConnection(message -> Platform.runLater(() -> {
			connectionCancelledDialog.setContents(message);
			connectionCancelledDialog.show(__rootPane);
		}));
		connectionManager.setOnNewClientJoined(clientData -> {
			Platform.runLater(() -> newClientDataItem.setUsingHostData(clientData));
			Synchronous<Boolean> accepted = new Synchronous<>();
			newClientDialog.setOnAccepted(e -> accepted.setValue(true));
			newClientDialog.setOnDeclined(e -> accepted.setValue(false));
			Platform.runLater(() -> newClientDialog.show(__rootPane));
			currentDialog = newClientDialog;
			return accepted.getValue();
		});
		connectionManager.setOnConnectionConfirmed(hostData ->
				System.out.println("Connection confirmed with " + hostData.getProfileName() + " from " + hostData.getIP()));
		connectionManager.setOnHostDataReceived(hostData -> hostDataItem.setUsingHostData(hostData));

		for (int i = 0; i < 10; ++i) {
			int number = i;
			JFXButton button = new JFXButton(String.valueOf(number));
			button.getStyleClass().add("ip-dial-button");
			button.setOnAction(e -> IPText.insertText(IPText.getCaretPosition(), String.valueOf(number)));
			button.setFocusTraversable(false);
			numbersPane.getChildren().add(button);
			GridPane.setRowIndex(button, i / 5);
			GridPane.setColumnIndex(button, i % 5);
		}
		numbersPane.getChildren().forEach(BackgroundColorAnimator::applyAnimation);

		manualIPDialog.setOnAccepted(event -> {
			InetAddress address;
			try {
				if (IPText.getText().isEmpty()) throw new UnknownHostException("");
				address = InetAddress.getByName(IPText.getText());
			} catch (UnknownHostException e) {
				currentDialog = null;
				connectionManager.getOnCancelConnection().accept("IP address invalid");
				return;
			}
			manualConnectToHost(address);
		});

		for (int i = 0; i < 10; ++i) {
			IconListItem item = new IconListItem();
			item.setName("Item " + (i + 1));
			matchList.getItems().add(item);
		}
	}
}
