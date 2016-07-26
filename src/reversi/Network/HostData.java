/**
 * Created by kanari on 2016/7/26.
 */

package Network;

import java.net.InetAddress;
import java.util.Date;

public class HostData {
	String profileName;
	String avatarID;
	int uniqueID;
	InetAddress IP;
	Date date;

	public String getProfileName() {
		return profileName;
	}

	public String getAvatarID() {
		return avatarID;
	}

	public int getUniqueID() {
		return uniqueID;
	}

	public String getIP() {
		return IP.getHostAddress();
	}

	public Date getDate() {
		return date;
	}

	public HostData(String profileName, String avatarID, int uniqueID, InetAddress IP, Date date) {
		this.profileName = profileName;
		this.IP = IP;
		this.avatarID = avatarID;
		this.uniqueID = uniqueID;
		this.date = date;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HostData)) return false;
		HostData rhs = (HostData) obj;
		return this.profileName.equals(rhs.profileName)
				&& this.IP.equals(rhs.IP)
				&& this.avatarID.equals(rhs.avatarID)
				&& this.uniqueID == rhs.uniqueID;
	}
}