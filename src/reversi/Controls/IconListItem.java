/**
 * Created by kanari on 2016/7/25.
 */

package Controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class IconListItem extends AnchorPane {
	@FXML
	private ImageView imageView;

	@FXML
	private Label name, ip;

	public IconListItem() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/IconListItem.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if (name == null) name = new Label();
		return name.textProperty();
	}

	public String getIP() {
		return IPProperty().get();
	}

	public void setIP(String value) {
		IPProperty().set("IP: " + value);
	}

	public StringProperty IPProperty() {
		if (ip == null) ip = new Label();
		return ip.textProperty();
	}
}
