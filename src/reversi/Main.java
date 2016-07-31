import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.datafx.controller.context.ViewMetadata;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowHandler;
import org.datafx.controller.flow.container.DefaultFlowContainer;
import org.datafx.controller.flow.context.ViewFlowContext;
import ui.BaseController;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Flow flow = new Flow(BaseController.class);
		ViewFlowContext context = new ViewFlowContext();
		context.register("stage", stage);
		FlowHandler handler = flow.createHandler(context);
		DefaultFlowContainer container = new DefaultFlowContainer();
		StackPane pane = handler.start(container);

		handler.getCurrentViewMetadata().addListener((e) -> {
			stage.titleProperty().unbind();
			ViewMetadata metadata = handler.getCurrentViewMetadata().get();
			if (metadata != null) {
				stage.titleProperty().bind(metadata.titleProperty());
			}
		});
		stage.titleProperty().unbind();
		ViewMetadata metadata = handler.getCurrentViewMetadata().get();
		if (metadata != null) {
			stage.titleProperty().bind(metadata.titleProperty());
		}

//		JFXDecorator decorator = new JFXDecorator(stage, container.getView(), false, false, true);
		stage.setResizable(false);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setScene(new Scene(pane, 1200, 900));
//		stage.setScene(new Scene(decorator, 1200, 900));
		stage.getScene().getStylesheets().add(Main.class.getResource("resources/css/jfoenix-design.css").toExternalForm());
		stage.getScene().getStylesheets().add(Main.class.getResource("resources/css/jfoenix-fonts.css").toExternalForm());
		stage.getScene().getStylesheets().add(Main.class.getResource("css/jfoenix-components.css").toExternalForm());

		stage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
