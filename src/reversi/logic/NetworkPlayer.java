/**
 * Created by kanari on 2016/7/28.
 */

package logic;

import network.HostData;
import network.SignaturedMessageFactory;
import util.InvalidFormatException;
import util.Synchronous;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

public class NetworkPlayer extends AbstractPlayer {

	private final static int TCPPort = 41013;

	private HostData myData, hostData;
	private Socket socket;
	private PortListener in;
	private PrintWriter out;

	private class PortListener extends Thread {
		Socket socket;
		BufferedReader in;
		Synchronous<String> message;

		PortListener(Socket socket) throws IOException {
			this.socket = socket;
			socket.setSoTimeout(500);
			message = new Synchronous<>();
			message.setValue(null);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					String message = in.readLine();
					if (message == null) { // connection broken
						break;
					}
					if (this.message.isSet()) {
						this.message.setValue(message);
						handleMessage(message);
					} else { // let the process calling "read()" handle it
						this.message.setValue(message);
					}
				} catch (SocketTimeoutException e) {
					System.out.println("timeout");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			message.setValue(null);
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!Thread.currentThread().isInterrupted()) {
				handleConnectionBroken();
			}
		}

		/**
		 * Synchronous read.
		 * @param timeout
		 *     Timeout for read operation. If {@code timeout = 0}, operation does not time out.
		 * @return
		 *     If timed out, returns {@code null}. Otherwise returns the read message.
		 */
		String read(int timeout) {
			message.reset();
			return message.getValue(timeout);
		}

		String read() {
			return read(0);
		}
	}

	private void handleConnectionBroken() {
		System.err.println("connection broken");
		manager.forceExit("Connection broken: your opponent may have gone offline");
	}

	public NetworkPlayer(HostData myData, HostData hostData, Socket socket) {
		super(hostData.getProfileName(), hostData.getAvatarID());
		this.myData = myData;
		this.hostData = hostData;
		this.socket = socket;
		try {
			this.in = new PortListener(socket);
			this.in.start();
			this.out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final List<String> qualifiers = Arrays.asList(
			"undo", "surrender", "exit", "draw", "load", "save", "timeout",
			"accept", "refuse",
			"chat", "ready", "dropPiece"
	);
	private static final List<String> requestQualifiers = Arrays.asList(
			"undo", "surrender", "exit", "draw", "load", "save"
	);

	private Synchronous<Point> timeoutResponse = new Synchronous<>();

	@Override
	public Point timeOut() {
		Point point = timeoutResponse.getValue(2000);
		timeoutResponse.reset();
		return point;
	}

	private void sendMessage(String message) {
		out.println(SignaturedMessageFactory.createSignaturedMessage(myData, message));
	}

	private void handleMessage(String receivedMessage) {
		System.out.println("received: " + receivedMessage);
		System.out.println(SignaturedMessageFactory.createSignaturedMessage(hostData, " "));
		try {
			String message = SignaturedMessageFactory.parseSignaturedMessageWithException(receivedMessage, hostData);
			String[] parts = message.split(" ");
			if (!qualifiers.contains(parts[0]))
				throw new InvalidFormatException("Received invalid qualifier \"" + message + "\"");
			if (parts[0].equals("ready")) {
				manager.ready();
			} else if (parts[0].equals("chat")) {
				if (parts.length < 2) throw new InvalidFormatException();
				manager.sendChat(message.substring(5)); // skip the prefix "chat "
			} else if (parts[0].equals("dropPiece") || parts[0].equals("timeout")) {
				if (parts.length != 3) throw new InvalidFormatException();
				int x = Integer.parseInt(parts[1]);
				int y = Integer.parseInt(parts[2]);
				if (parts[0].equals("dropPiece")) manager.dropPiece(x, y);
				else timeoutResponse.setValue(new Point(x, y));
			} else if (requestQualifiers.contains(parts[0])) {
				boolean result = false;
				if (parts[0].equals("undo")) result = manager.requestUndo();
				else if (parts[0].equals("surrender")) result = manager.requestSurrender();
				else if (parts[0].equals("exit")) result = manager.requestExit();
				else if (parts[0].equals("draw")) result = manager.requestDraw();
				sendMessage(result ? "accept" : "refuse");
			} else throw new InvalidFormatException("Received qualifier \"" + message + "\" when it shouldn't");
		} catch (InvalidFormatException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void newGame(PlayerState state) {
		// does nothing
	}

	@Override
	public void informOpponentMove(Point point, boolean isSkipped, boolean isTimeout) {
		if (point != null) sendMessage((isTimeout ? "timeout" : "dropPiece") + " " + point.x + " " + point.y);
	}

	@Override
	public void gameOver(boolean isWinner, boolean isTie) {
		// does nothing
	}

	@Override
	public void purge() {
		try {
			in.interrupt();
			in.join();
			socket.close();
			out.close();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void opponentIsReady() {
		sendMessage("ready");
	}

	private boolean processRequest(String message) {
		sendMessage(message);
		String response = in.read();
		try {
			if (response == null) throw new InvalidFormatException("Empty response");
			switch (response) {
				case "accept":
					return true;
				case "refuse":
					return false;
				default:
					throw new InvalidFormatException("Response was neither \"accept\" nor \"refuse\"");
			}
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean undoRequested() {
		return processRequest("undo");
	}

	@Override
	public boolean drawRequested() {
		return processRequest("draw");
	}

	@Override
	public boolean surrenderRequested() {
		return processRequest("surrender");
	}

	@Override
	public boolean exitRequested() {
		return processRequest("exit");
	}

	@Override
	public void receivedChat(String message) {
		sendMessage("chat " + message);
	}
}
