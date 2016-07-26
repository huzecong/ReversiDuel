/**
 * Created by kanari on 2016/7/26.
 */

package NetworkUtils;

import javafx.beans.property.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class ConnectionManager {
	MulticastManager manager;
	InetAddress localIP;

	private static int UDPTimeoutInterval = 4000;       // milliseconds

	public ConnectionManager(String profileName, String avatarID, int uniqueID) {
		this.isHost = new SimpleBooleanProperty(false);
		this.profileName = new SimpleStringProperty(profileName);
		this.avatarID = new SimpleStringProperty(avatarID);
		this.uniqueID = new SimpleIntegerProperty(uniqueID);

		try {
			this.localIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.manager = new MulticastManager(this::handleMulticastData, this::multicastMessage);
	}

	/**
	 * UDP multicast handlers
	 */
	private String multicastMessage() {
		return new Date().toString() + "\n" + getProfileName() + "\n" + getAvatarID() + "\n" + getUniqueID() + "\n"
				+ localIP + "\n" + "ReversiDuel";
	}

	private void handleMulticastData(String data) {
		String[] parts = data.split("\\n");
		if (parts.length != 6) return;

		String profileName, avatarID;
		InetAddress IP;
		int uniqueID;
		Date date;

		DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
		try {
			date = format.parse(parts[0]);
			profileName = parts[1];
			avatarID = parts[2];
			uniqueID = Integer.parseInt(parts[3]);
			IP = InetAddress.getByName(parts[4]);
		} catch (Exception e) {
			return;
		}
		if (!parts[5].equals("ReversiDuel")) return;

		updateHostList(new HostData(profileName, avatarID, uniqueID, IP, date));

	}

	/**
	 * List of available hosts, and update functions and callbacks
	 */
	List<HostData> hostList;

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
		manager.startHost();
	}

	public void abortHost() {
		manager.abortHost();
		isHost.set(false);
	}

	StringProperty profileName;
	StringProperty avatarID;

	IntegerProperty uniqueID;

	public String getProfileName() {
		return profileName.get();
	}

	public StringProperty profileNameProperty() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName.set(profileName);
	}

	public String getAvatarID() {
		return avatarID.get();
	}

	public StringProperty avatarIDProperty() {
		return avatarID;
	}

	public void setAvatarID(String avatarID) {
		this.avatarID.set(avatarID);
	}

	public int getUniqueID() {
		return uniqueID.get();
	}

	public IntegerProperty uniqueIDProperty() {
		return uniqueID;
	}

	public void setUniqueID(int uniqueID) {
		this.uniqueID.set(uniqueID);
	}
}
