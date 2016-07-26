/**
 * Created by kanari on 2016/7/26.
 */

package Network;

import com.sun.media.sound.InvalidFormatException;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectionManager {
	MulticastManager manager;
	InetAddress localIP;

	private static int UDPTimeoutInterval = 2000;       // milliseconds
	private static int TCPPort = 41013;
	private static int TCPTimeoutInterval = 2000;

	private HostTCPManager hostTCPManager;
	private ClientTCPManager clientTCPManager;

	public ConnectionManager(String profileName, String avatarID, int uniqueID) {
		this.isHost = new SimpleBooleanProperty(false);
		try {
			this.playerData = new HostData(profileName, avatarID, uniqueID, InetAddress.getLocalHost(), new Date());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			this.localIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.hostList = new ArrayList<>();

		this.manager = new MulticastManager(data -> {
			Optional<HostData> result = SignaturedMessageFactory.parseSignaturedMessage(data, "create");
			if (result.isPresent()) updateHostList(result.get());
		}, () -> SignaturedMessageFactory.createSignaturedMessage(playerData, "create"));

		this.hostTCPManager = new HostTCPManager();
		this.hostTCPManager.setOnTimeout(e -> getOnCancelConnection().accept(e.getLocalizedMessage()));
		this.hostTCPManager.setOnNewClientJoined(hostData -> getOnNewClientJoined().apply(hostData));
		this.hostTCPManager.setOnClientAborted(e -> getOnCancelConnection().accept(e.getLocalizedMessage()));
		this.hostTCPManager.setOnConnectionConfirmed(hostData -> getOnConnectionConfirmed().accept(hostData));

		this.clientTCPManager = new ClientTCPManager();
		this.clientTCPManager.setOnTimeout(e -> getOnCancelConnection().accept(e.getLocalizedMessage()));
		this.clientTCPManager.setOnRequestRefused(e -> getOnCancelConnection().accept(e.getLocalizedMessage()));
		this.clientTCPManager.setOnConnectionConfirmed(hostData -> getOnConnectionConfirmed().accept(hostData));
	}

	/**
	 * GUI handlers
	 */
	// void onCancelConnection(String message);  [blocked]
	private Consumer<String> onCancelConnection;
	// boolean onNewClientJoined(HostData clientData);  [blocked]
	private Function<HostData, Boolean> onNewClientJoined;
	// void onConnectionConfirmed(HostData opponentData);  [async]
	private Consumer<HostData> onConnectionConfirmed;

	public Consumer<String> getOnCancelConnection() {
		return onCancelConnection;
	}

	public void setOnCancelConnection(Consumer<String> onCancelConnection) {
		this.onCancelConnection = onCancelConnection;
	}

	public Function<HostData, Boolean> getOnNewClientJoined() {
		return onNewClientJoined;
	}

	public void setOnNewClientJoined(Function<HostData, Boolean> onNewClientJoined) {
		this.onNewClientJoined = onNewClientJoined;
	}

	public Consumer<HostData> getOnConnectionConfirmed() {
		return onConnectionConfirmed;
	}

	public void setOnConnectionConfirmed(Consumer<HostData> onConnectionConfirmed) {
		this.onConnectionConfirmed = onConnectionConfirmed;
	}

	/**
	 * List of available hosts, and update functions and callbacks
	 */
	ArrayList<HostData> hostList;

	Consumer<Integer> onRemoveHostListIndex;
	Consumer<HostData> onAddToHostList;

	public Consumer<Integer> getOnRemoveHostListIndex() {
		return onRemoveHostListIndex;
	}

	public void setOnRemoveHostListIndex(Consumer<Integer> onRemoveHostListIndex) {
		this.onRemoveHostListIndex = onRemoveHostListIndex;
	}

	public Consumer<HostData> getOnAddToHostList() {
		return onAddToHostList;
	}

	public void setOnAddToHostList(Consumer<HostData> onAddToHostList) {
		this.onAddToHostList = onAddToHostList;
	}

	private void updateHostList() {
		Date current = new Date();
		for (int index = 0; index < hostList.size(); ++index) {
			HostData host = hostList.get(index);
			if (current.getTime() - host.date.getTime() > ConnectionManager.UDPTimeoutInterval) {
				onRemoveHostListIndex.accept(index);
				hostList.remove(index--);
			}
		}
	}

	private void updateHostList(HostData hostData) {
		boolean exist = false;
		for (HostData host : hostList)
			if (host.equals(hostData)) {
				exist = true;
				host.date = hostData.date;
				break;
			}
		updateHostList();
		if (!exist) {
			hostList.add(hostData);
			onAddToHostList.accept(hostData);
		}
	}

	/**
	 * Host related
	 */
	BooleanProperty isHost;

	public boolean isHost() {
		return isHost.get();
	}

	public BooleanProperty isHostProperty() {
		return isHost;
	}

	public void setIsHost(boolean isHost) {
		this.isHost.set(isHost);
	}

	public void startHost() {
		isHost.set(true);
		connectionThread = new Thread(hostTCPManager);
		connectionThread.start();
		manager.startHost();
	}

	public void abortHost() {
		manager.abortHost();
		connectionThread.interrupt();
		isHost.set(false);
	}

	/**
	 * Client related
	 */
	public void connectToHost(HostData hostData) {
		clientTCPManager.setHostData(hostData);
		connectionThread = new Thread(clientTCPManager);
		connectionThread.start();
	}

	public void abortConnectionToHost() {
		connectionThread.interrupt();
	}

	/**
	 * Player info
	 */
	private HostData playerData;

	public HostData getPlayerData() {
		return playerData;
	}

	/**
	 * TCP connection manager
	 */
	Thread connectionThread;

	class HostTCPManager implements Runnable {
		ServerSocket serverSocket;

		Consumer<Exception> onTimeout;
		Function<HostData, Boolean> onNewClientJoined;
		Consumer<Exception> onClientAborted;
		Consumer<HostData> onConnectionConfirmed;

		public void setOnTimeout(Consumer<Exception> onTimeout) {
			this.onTimeout = onTimeout;
		}

		public void setOnNewClientJoined(Function<HostData, Boolean> onNewClientJoined) {
			this.onNewClientJoined = onNewClientJoined;
		}

		public void setOnClientAborted(Consumer<Exception> onClientAborted) {
			this.onClientAborted = onClientAborted;
		}

		public void setOnConnectionConfirmed(Consumer<HostData> onConnectionConfirmed) {
			this.onConnectionConfirmed = onConnectionConfirmed;
		}

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(ConnectionManager.TCPPort);
				serverSocket.setSoTimeout(ConnectionManager.TCPTimeoutInterval);
				while (!Thread.interrupted()) {
					try {
						Socket socket = serverSocket.accept();
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

						HostData clientData = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), "join");
						boolean accepted = onNewClientJoined.apply(clientData);
						out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, accepted ? "accept" : "refuse"));
						if (accepted) {
							String message = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), clientData);
							if (!message.equals("confirm"))
								throw new InvalidFormatException("Client should confirm instead of sending " + message);
							Platform.runLater(() -> onConnectionConfirmed.accept(clientData));
							Thread.currentThread().interrupt();
						}
					} catch (InvalidFormatException e) {
						onClientAborted.accept(e);
					} catch (IOException e) {
						onTimeout.accept(e);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ClientTCPManager implements Runnable {
		Socket socket;

		HostData hostData;

		Consumer<Exception> onTimeout;
		Consumer<Exception> onRequestRefused;
		Consumer<HostData> onConnectionConfirmed;

		public HostData getHostData() {
			return hostData;
		}

		public void setHostData(HostData hostData) {
			this.hostData = hostData;
		}

		public void setOnTimeout(Consumer<Exception> onTimeout) {
			this.onTimeout = onTimeout;
		}

		public void setOnRequestRefused(Consumer<Exception> onRequestRefused) {
			this.onRequestRefused = onRequestRefused;
		}

		public void setOnConnectionConfirmed(Consumer<HostData> onConnectionConfirmed) {
			this.onConnectionConfirmed = onConnectionConfirmed;
		}

		@Override
		public void run() {
			try {
				socket = new Socket(hostData.getIP(), ConnectionManager.TCPPort);
				socket.setSoTimeout(ConnectionManager.TCPTimeoutInterval);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "join"));
				String message = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), hostData);
				switch (message) {
					case "accept":
						out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "confirm"));
						Platform.runLater(() -> onConnectionConfirmed.accept(hostData));
						break;
					case "refuse":
						throw new InvalidFormatException("Host refused match");
					default:
						throw new InvalidFormatException("Host neither accepted nor refused request");
				}
			} catch (InvalidFormatException e) {
				Platform.runLater(() -> onRequestRefused.accept(e));
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				Platform.runLater(() -> onTimeout.accept(e));
				Thread.currentThread().interrupt();
			}
		}
	}
}
