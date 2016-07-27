/**
 * Created by kanari on 2016/7/25.
 */

package Controls;

import Network.HostData;
import Utility.BackgroundColorAnimator;
import com.jfoenix.controls.JFXListCell;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;

public class IconListItem extends AnchorPane {
	@FXML
	private ImageView imageView;

	@FXML
	private Label name, ip;

	@FXML
	private MaterialIconView glyph;

	private HostData hostData;

	public IconListItem() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/IconListItem.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUsingHostData(HostData hostData) {
		this.hostData = hostData;
		setName(hostData.getProfileName());
		setIP(hostData.getIP());
		setIcon(new Image(getClass().getResource("avatar/" + hostData.getAvatarID()).toExternalForm()));
	}

	public HostData getHostData() {
		return hostData;
	}

	public Image getIcon() {
		return iconProperty().get();
	}

	public void setIcon(Image value) {
		iconProperty().set(value);
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

	public String getIP() {
		return IPProperty().get();
	}

	public void setIP(String value) {
		IPProperty().set("IP: " + value);
	}

	public StringProperty IPProperty() {
		return ip.textProperty();
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
