package view;

import java.io.IOException;
import solr.SolrConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class App extends Application {

	public static Stage MAIN_STAGE;

	/*
	 * Instance of the connection to a solr instance.
	 */
	public static final SolrConnection SOLR = new SolrConnection();

	@Override
	public void start(Stage stage) {
		try {
			// Load the FXML-Resource file
			App.MAIN_STAGE = stage;
			Parent rootPanel = FXMLLoader.load(App.class.getClassLoader().getResource("resources.fxml"));

			// Set the scene
			Scene scene = new Scene(rootPanel, 500, 400);

			// Set the title of the stage
			stage.setTitle("Dokumentenähnlichkeiten");
			stage.setScene(scene);
			stage.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		launch(args);
	}
}
