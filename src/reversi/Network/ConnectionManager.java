/**
 * Created by kanari on 2016/7/26.
 */

package Network;

import com.sun.media.sound.InvalidFormatException;
import com.sun.tools.javac.util.Pair;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectionManager {
	MulticastManager manager;
	InetAddress localIP;

	private static int UDPTimeoutInterval = 2000;       // milliseconds
	private static int TCPPort = 41013;
	private static int TCPTimeoutInterval = 10000;
	private static int HostListRefreshInterval = 500;

	private Timer hostListRefreshTimer;

	private class RefreshListTask extends TimerTask {

		Runnable task;
		RefreshListTask(Runnable task) {
			this.task = task;
		}

		@Override
		public void run() {
			task.run();
		}

	}
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
		this.hostListRefreshTimer = new Timer();
		this.hostListRefreshTimer.scheduleAtFixedRate(new RefreshListTask(this::updateHostList),
				new Date(), ConnectionManager.HostListRefreshInterval);

		this.manager = new MulticastManager(data -> {
			Optional<HostData> result = SignaturedMessageFactory.parseSignaturedMessage(data, "create");
			if (result.isPresent()) updateHostList(result.get());
		}, () -> SignaturedMessageFactory.createSignaturedMessage(playerData, "create"));

		this.hostTCPManager = new HostTCPManager();
		this.hostTCPManager.setOnTimeout(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.hostTCPManager.setOnNewClientJoined(hostData -> onNewClientJoined.apply(hostData));
		this.hostTCPManager.setOnClientAborted(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.hostTCPManager.setOnConnectionConfirmed(hostData -> onConnectionConfirmed.accept(hostData));

		this.clientTCPManager = new ClientTCPManager();
		this.clientTCPManager.setOnTimeout(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.clientTCPManager.setOnRequestRefused(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.clientTCPManager.setOnConnectionConfirmed(hostData -> onConnectionConfirmed.accept(hostData));
		this.clientTCPManager.setOnHostDataReceived(hostData -> onHostDataReceived.accept(hostData));
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
	// void onHostDataReceived(HostData hostData);  [async]
	private Consumer<HostData> onHostDataReceived;

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

	public Consumer<HostData> getOnHostDataReceived() {
		return onHostDataReceived;
	}

	public void setOnHostDataReceived(Consumer<HostData> onHostDataReceived) {
		this.onHostDataReceived = onHostDataReceived;
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
//		System.out.println("hostData: " + hostData.getProfileName() + " " + hostData.getIP());
		if (hostData.getIP().equals(this.localIP.getHostAddress())) return;
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
		clientTCPManager.setManual(false);
		clientTCPManager.setHostData(hostData);
		connectionThread = new Thread(clientTCPManager);
		connectionThread.start();
	}

	public void manualConnectToHost(InetAddress address) {
		clientTCPManager.setManual(true);
		clientTCPManager.setAddress(address);
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
	private Thread connectionThread;

	private class HostTCPManager implements Runnable {
		ServerSocket serverSocket;

		Consumer<Exception> onTimeout;
		Function<HostData, Boolean> onNewClientJoined;
		Consumer<Exception> onClientAborted;
		Consumer<HostData> onConnectionConfirmed;

		void setOnTimeout(Consumer<Exception> onTimeout) {
			this.onTimeout = onTimeout;
		}

		void setOnNewClientJoined(Function<HostData, Boolean> onNewClientJoined) {
			this.onNewClientJoined = onNewClientJoined;
		}

		void setOnClientAborted(Consumer<Exception> onClientAborted) {
			this.onClientAborted = onClientAborted;
		}

		void setOnConnectionConfirmed(Consumer<HostData> onConnectionConfirmed) {
			this.onConnectionConfirmed = onConnectionConfirmed;
		}

		@Override
		public void run() {
			try {
				if (serverSocket != null) serverSocket.close();
				serverSocket = new ServerSocket(ConnectionManager.TCPPort);
				serverSocket.setSoTimeout(500);
				Socket socket = null;
				while (!Thread.interrupted()) {
					try {
						socket = null;
						while (socket == null) {
							try {
								socket = serverSocket.accept();
							} catch (SocketTimeoutException e) {
								if (Thread.interrupted()) {
									serverSocket.close();
									return;
								}
							}
						}
						socket.setSoTimeout(ConnectionManager.TCPTimeoutInterval);
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

						if (Thread.interrupted()) break;
						Pair<HostData, String> clientResult = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine());
						if (clientResult.snd.equals("manual_join")) {
							out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "host_data"));
						} else if (!clientResult.snd.equals("join")) {
							throw new InvalidFormatException("Client's first message was neither \"join\" nor \"manual_join\"");
						}
						HostData clientData = clientResult.fst;
						boolean accepted = onNewClientJoined.apply(clientData);
						out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, accepted ? "accept" : "refuse"));
						if (Thread.interrupted()) break;
						if (accepted) {
							String message = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), clientData);
							if (!message.equals("confirm"))
								throw new InvalidFormatException("Client should confirm instead of sending " + message);
							Platform.runLater(() -> onConnectionConfirmed.accept(clientData));
							Thread.currentThread().interrupt();
						}
						socket.close();
					} catch (InvalidFormatException e) {
						onClientAborted.accept(e);
					} catch (IOException e) {
						onTimeout.accept(e);
					} finally {
						if (socket != null) socket.close();
					}
				}
				if (socket != null) socket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ClientTCPManager implements Runnable {
		Socket socket;

		boolean manual;
		InetAddress address;
		HostData hostData;

		Consumer<Exception> onTimeout;
		Consumer<Exception> onRequestRefused;
		Consumer<HostData> onConnectionConfirmed;
		Consumer<HostData> onHostDataReceived;  // for manual connections only

		void setManual(boolean manual) {
			this.manual = manual;
		}

		void setAddress(InetAddress address) {
			this.address = address;
		}

		public HostData getHostData() {
			return hostData;
		}

		void setHostData(HostData hostData) {
			this.hostData = hostData;
		}

		void setOnTimeout(Consumer<Exception> onTimeout) {
			this.onTimeout = onTimeout;
		}

		void setOnRequestRefused(Consumer<Exception> onRequestRefused) {
			this.onRequestRefused = onRequestRefused;
		}

		void setOnConnectionConfirmed(Consumer<HostData> onConnectionConfirmed) {
			this.onConnectionConfirmed = onConnectionConfirmed;
		}

		void setOnHostDataReceived(Consumer<HostData> onHostDataReceived) {
			this.onHostDataReceived = onHostDataReceived;
		}

		@Override
		public void run() {
			if (manual) {
				if (address == null) {
					Platform.runLater(() -> onTimeout.accept(new UnknownHostException("IP address invalid")));
					return ;
				}
				hostData = new HostData("", "", 0, address, new Date());
			}

			try {
				if (socket != null) socket.close();
				socket = new Socket(hostData.getIP(), ConnectionManager.TCPPort);
				socket.setSoTimeout(ConnectionManager.TCPTimeoutInterval);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				if (!Thread.interrupted() && manual) {
					out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "manual_join"));
					hostData = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), "host_data");
					Platform.runLater(() -> onHostDataReceived.accept(hostData));
				} else {
					out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "join"));
				}
				if (!Thread.interrupted()) {
					String message = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), hostData);
					if (!Thread.interrupted()) {
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
					}
				}
				socket.close();
			} catch (InvalidFormatException e) {
				Platform.runLater(() -> onRequestRefused.accept(e));
			} catch (IOException e) {
				Platform.runLater(() -> onTimeout.accept(e));
			} finally {
				if (socket != null) try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
