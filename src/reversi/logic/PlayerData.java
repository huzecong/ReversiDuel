/**
 * Created by kanari on 2016/7/29.
 */

package logic;

public class PlayerData {
	String profileName, avatarID;
	int score;

	public PlayerData(String profileName, String avatarID) {
		this.profileName = profileName;
		this.avatarID = avatarID;
		this.score = 0;
	}

	public String getProfileName() {
		return profileName;
	}

	protected void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getAvatarID() {
		return avatarID;
	}

	protected void setAvatarID(String avatarID) {
		this.avatarID = avatarID;
	}

	public int getScore() {
		return score;
	}

	protected void setScore(int score) {
		this.score = score;
	}
}
