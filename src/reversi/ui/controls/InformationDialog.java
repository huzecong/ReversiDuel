/**
 * Created by kanari on 2016/7/26.
 */

package ui.controls;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import override.CustomDialog;

import java.io.IOException;

public class InformationDialog extends CustomDialog {
	@FXML
	private
	Label heading, body;

	@FXML
	private
	JFXButton acceptButton;

	public InformationDialog() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/InformationDialog.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		acceptButton.setOnAction(e -> close());
		setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER)
				acceptButton.fire();
		});
	}

	public String getHeading() {
		return this.heading.getText();
	}

	public void setHeading(String heading) {
		this.heading.setText(heading);
	}

	public StringProperty headingProperty() {
		return this.heading.textProperty();
	}

	public String getContents() {
		return this.body.getText();
	}

	public void setContents(String content) {
		this.body.setText(content);
	}

	public StringProperty contentsProperty() {
		return this.body.textProperty();
	}
}
