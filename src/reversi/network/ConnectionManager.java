/**
 * Created by kanari on 2016/7/26.
 */

package network;

import util.InvalidFormatException;
import util.Pair;
import javafx.application.Platform;
import javafx.beans.property.*;
import util.TaskScheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.function.*;

public class ConnectionManager {
	private MulticastManager manager;
	private InetAddress localIP;

	private static int UDPTimeoutInterval = 2000;       // milliseconds
	private static int TCPPort = 41013;
	private static int TCPTimeoutInterval = 10000;
	private static int HostListRefreshInterval = 500;

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
		TaskScheduler.repeated(ConnectionManager.HostListRefreshInterval, this::updateHostList);

		this.manager = new MulticastManager(data -> {
			Optional<HostData> result = SignaturedMessageFactory.parseSignaturedMessage(data, "create");
			if (result.isPresent()) updateHostList(result.get());
		}, () -> SignaturedMessageFactory.createSignaturedMessage(playerData, "create"));

		this.hostTCPManager = new HostTCPManager();
		this.hostTCPManager.setOnTimeout(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.hostTCPManager.setOnNewClientJoined(hostData -> onNewClientJoined.apply(hostData));
		this.hostTCPManager.setOnClientAborted(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.hostTCPManager.setOnConnectionConfirmed((hostData, socket, isHost) -> onConnectionConfirmed.accept(hostData, socket, isHost));

		this.clientTCPManager = new ClientTCPManager();
		this.clientTCPManager.setOnTimeout(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.clientTCPManager.setOnRequestRefused(e -> onCancelConnection.accept(e.getLocalizedMessage()));
		this.clientTCPManager.setOnConnectionConfirmed((hostData, socket, isHost) -> onConnectionConfirmed.accept(hostData, socket, isHost));
		this.clientTCPManager.setOnHostDataReceived(hostData -> onHostDataReceived.accept(hostData));
	}

	/**
	 * GUI handlers
	 */
	public interface ConnectionConfirmedHandler {
		void accept(HostData hostData, Socket socket, boolean isHost);
	}

	// void onCancelConnection(String message);  [blocked]
	private Consumer<String> onCancelConnection;
	// boolean onNewClientJoined(HostData clientData);  [blocked]
	private Function<HostData, Boolean> onNewClientJoined;
	// void onConnectionConfirmed(HostData opponentData, Socket socket);  [async]
	private ConnectionConfirmedHandler onConnectionConfirmed;
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

	public ConnectionConfirmedHandler getOnConnectionConfirmed() {
		return onConnectionConfirmed;
	}

	public void setOnConnectionConfirmed(ConnectionConfirmedHandler onConnectionConfirmed) {
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
	private ArrayList<HostData> hostList;

	private Consumer<Integer> onRemoveHostListIndex;
	private Consumer<HostData> onAddToHostList;

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
			if (current.getTime() - host.getDate().getTime() > ConnectionManager.UDPTimeoutInterval) {
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
				host.setDate(hostData.getDate());
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
	private BooleanProperty isHost;

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
		ConnectionConfirmedHandler onConnectionConfirmed;

		void setOnTimeout(Consumer<Exception> onTimeout) {
			this.onTimeout = onTimeout;
		}

		void setOnNewClientJoined(Function<HostData, Boolean> onNewClientJoined) {
			this.onNewClientJoined = onNewClientJoined;
		}

		void setOnClientAborted(Consumer<Exception> onClientAborted) {
			this.onClientAborted = onClientAborted;
		}

		void setOnConnectionConfirmed(ConnectionConfirmedHandler onConnectionConfirmed) {
			this.onConnectionConfirmed = onConnectionConfirmed;
		}

		@Override
		public void run() {
			boolean connectionConfirmed = false;
			Socket socket = null;
			HostData clientData = null;
			try {
				serverSocket = new ServerSocket(ConnectionManager.TCPPort);
				serverSocket.setSoTimeout(500);
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (socket != null) socket.close();
						socket = null;
						while (socket == null) {
							try {
								socket = serverSocket.accept();
							} catch (SocketTimeoutException e) {
								if (Thread.currentThread().isInterrupted()) {
									serverSocket.close();
									return;
								}
							}
						}
						socket.setSoTimeout(ConnectionManager.TCPTimeoutInterval);
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

						if (Thread.currentThread().isInterrupted()) break;
						Pair<HostData, String> clientResult = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine());
						if (clientResult.snd.equals("manual_join")) {
							out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "host_data"));
						} else if (!clientResult.snd.equals("join")) {
							throw new InvalidFormatException("Client's first message was neither \"join\" nor \"manual_join\"");
						}
						clientData = clientResult.fst;
						boolean accepted = onNewClientJoined.apply(clientData);
						out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, accepted ? "accept" : "refuse"));
						if (Thread.currentThread().isInterrupted()) break;
						if (accepted) {
							String message = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), clientData);
							if (!message.equals("confirm"))
								throw new InvalidFormatException("Client should confirm instead of sending " + message);
							connectionConfirmed = true;
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
			} finally {
				try {
					if (connectionConfirmed) {
						assert socket != null && !socket.isClosed();
						HostData finalClientData = clientData;
						Socket finalSocket = socket;
						Platform.runLater(() -> onConnectionConfirmed.accept(finalClientData, finalSocket, true));
					} else {
						if (socket != null) socket.close();
					}
					serverSocket.close();
					serverSocket = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		ConnectionConfirmedHandler onConnectionConfirmed;
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

		void setOnConnectionConfirmed(ConnectionConfirmedHandler onConnectionConfirmed) {
			this.onConnectionConfirmed = onConnectionConfirmed;
		}

		void setOnHostDataReceived(Consumer<HostData> onHostDataReceived) {
			this.onHostDataReceived = onHostDataReceived;
		}

		@Override
		public void run() {
			boolean connectionConfirmed = false;
			if (manual) {
				if (address == null) {
					Platform.runLater(() -> onTimeout.accept(new UnknownHostException("IP address invalid")));
					return;
				}
				hostData = new HostData("", "", 0, address, new Date());
			}

			try {
				if (socket != null) socket.close();
				socket = new Socket(hostData.getIP(), ConnectionManager.TCPPort);
				socket.setSoTimeout(ConnectionManager.TCPTimeoutInterval);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				if (!Thread.currentThread().isInterrupted() && manual) {
					out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "manual_join"));
					hostData = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), "host_data");
					Platform.runLater(() -> onHostDataReceived.accept(hostData));
				} else {
					out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "join"));
				}
				if (!Thread.currentThread().isInterrupted()) {
					String message = SignaturedMessageFactory.parseSignaturedMessageWithException(in.readLine(), hostData);
					if (!Thread.currentThread().isInterrupted()) {
						switch (message) {
							case "accept":
								out.println(SignaturedMessageFactory.createSignaturedMessage(playerData, "confirm"));
								connectionConfirmed = true;
								break;
							case "refuse":
								throw new InvalidFormatException("Host refused match");
							default:
								throw new InvalidFormatException("Host neither accepted nor refused request");
						}
					}
				}
			} catch (InvalidFormatException e) {
				Platform.runLater(() -> onRequestRefused.accept(e));
			} catch (IOException e) {
				Platform.runLater(() -> onTimeout.accept(e));
			} finally {
				if (connectionConfirmed) {
					Platform.runLater(() -> onConnectionConfirmed.accept(hostData, socket, false));
				} else {
					if (socket != null) try {
						socket.close();
						socket = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
