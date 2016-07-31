/**
 * Created by kanari on 2016/7/25.
 */

package ui;

import com.jfoenix.controls.*;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import logic.LocalPlayer;
import logic.NetworkPlayer;
import network.ConnectionManager;
import network.HostData;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.context.*;
import org.datafx.controller.util.VetoException;
import org.omg.CORBA.MARSHAL;
import ui.controls.*;
import util.*;

import javax.annotation.PostConstruct;
import java.net.*;
import java.util.*;

@FXMLController(value = "fxml/ConnectPage.fxml", title = "Select Opponent")
public class ConnectPageController {
	@FXMLViewFlowContext
	private ViewFlowContext context;

	@ActionHandler
	private FlowActionHandler actionHandler;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private StackPane __rootPane;

	@FXML
	private Label noMatchLabel;

	@FXML
	private JFXListView<HostDataListCell> matchList;

	@FXML
	private InformationDialog connectionCancelledDialog;

	@FXML
	private ConfirmationDialog newClientDialog, connectToHostDialog, searchClientDialog;

	@FXML
	private HostDataListCell newClientDataItem, hostDataItem;

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

	private void connectToHost(HostDataListCell item) {
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

	private void connectionConfirmed(HostData hostData, Socket socket, boolean isHost) {
		System.out.println("Connection confirmed with " + hostData.getProfileName() + " from " + hostData.getIP());
		LocalPlayer localPlayer = new LocalPlayer(connectionManager.getPlayerData().getProfileName(), connectionManager.getPlayerData().getAvatarID());
		NetworkPlayer networkPlayer = new NetworkPlayer(connectionManager.getPlayerData(), hostData, socket);
		context.register("p1TimeLimit", 20);
		context.register("p2TimeLimit", 20);
		if (isHost) {
			context.register("player1", localPlayer);
			context.register("player2", networkPlayer);
		} else {
			context.register("player1", networkPlayer);
			context.register("player2", localPlayer);
		}
		try {
			actionHandler.navigate(OnlineDuelGameBoardController.class);
		} catch (VetoException | FlowException e) {
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void init() throws UnknownHostException {
		JFXDepthManager.setDepth(rootPane, 1);

		{   // dirty but maybe somehow robust (???) approach to vertically fill matchList
			VBox vbox = (VBox) rootPane.getChildren().filtered(node -> node instanceof VBox).get(0);
			ArrayList<Region> subtracted = new ArrayList<>();
			subtracted.add((JFXListView) vbox.getChildren().filtered(node -> node instanceof JFXListView).get(0));
			subtracted.add((Label) vbox.getChildren().filtered(node -> node instanceof Label).get(0));
			subtracted.add((JFXListView) rootPane.getChildren().filtered(node -> node instanceof JFXListView).get(0));
			DoubleBinding fillHeight = null;
			for (Region region : subtracted) {
				if (fillHeight == null) fillHeight = rootPane.heightProperty().subtract(region.heightProperty());
				else fillHeight = fillHeight.subtract(region.heightProperty());
			}

			fillHeight.addListener(((observable, oldValue, newValue) -> {
				for (Region region : subtracted)
					if (region.getHeight() == 0) return;
				matchList.setPrefHeight(newValue.doubleValue());
				matchList.setMinHeight(newValue.doubleValue());
				matchList.setMaxHeight(newValue.doubleValue());
			}));
		}

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

		Properties playerProperties = PreferenceLoader.loadFromPreferences("p1");
		connectionManager = new ConnectionManager(
				playerProperties.getProperty("profileName"),
				playerProperties.getProperty("avatarID"),
				Integer.parseInt(playerProperties.getProperty("uniqueID")));
		connectionManager.setOnAddToHostList(hostData -> {
			HostDataListCell newItem = new HostDataListCell();
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
		connectionManager.setOnConnectionConfirmed(this::connectionConfirmed);
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

		manualIPDialog.setOnDialogOpened(e -> IPText.requestFocus());
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
	}
}
