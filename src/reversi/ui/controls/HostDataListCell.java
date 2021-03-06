/**
 * Created by kanari on 2016/7/25.
 */

package ui.controls;

import network.HostData;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class HostDataListCell extends AnchorPane {
	@FXML
	private ImageView imageView;

	@FXML
	private Label name, caption;

	@FXML
	private MaterialIconView glyph;

	private HostData hostData;

	public HostDataListCell() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/HostDataListCell.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUsingHostData(HostData hostData) {
		setUsingHostData(hostData, true);
	}

	public void setUsingHostData(HostData hostData, boolean displayTime) {
		this.hostData = hostData;
		setName(hostData.getProfileName());
		if (displayTime) {
			setCaption("IP: " + hostData.getIP() + "\nTime Limit = " + hostData.getTimeLimit() + " seconds");
		} else {
			setCaption("IP: " + hostData.getIP());
		}
		setIcon(hostData.getAvatarID());
	}

	public HostData getHostData() {
		return hostData;
	}

	public Image getIcon() {
		return iconProperty().get();
	}

	public void setIcon(Image icon) {
		iconProperty().set(icon);
	}

	public void setIcon(String avatarID) {
		iconProperty().set(new Image(getClass().getResource("avatar/" + avatarID).toExternalForm()));
	}

	public ObjectProperty<Image> iconProperty() {
		if (imageView == null) imageView = new ImageView();
		return imageView.imageProperty();
	}

	public String getName() {
		return nameProperty().get();
	}

	public void setName(String value) {
		nameProperty().set(value);
	}

	public StringProperty nameProperty() {
		return name.textProperty();
	}

	public String getCaption() {
		return captionProperty().get();
	}

	public void setCaption(String value) {
		captionProperty().set(value);
	}

	public StringProperty captionProperty() {
		return caption.textProperty();
	}

	public boolean isGlyphVisible() {
		return glyph.isVisible();
	}

	public void setGlyphVisible(boolean visibile) {
		glyph.setVisible(visibile);
	}

	public BooleanProperty glyphVisibleProperty() {
		return glyph.visibleProperty();
	}
}
