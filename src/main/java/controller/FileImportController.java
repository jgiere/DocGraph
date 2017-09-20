package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.SimpleOrderedMap;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import mdsj.MDSJ;
import model.Document;
import model.Query;
import model.interfaces.IDocument;
import view.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * Handles the import of a new file.
 * 
 * @author Jan-Hendrick Hemmje und Johannes Giere
 *
 */

public class FileImportController implements Initializable {

	private DocumentController docController;

	private ObservableList<File> files = FXCollections.observableArrayList();
	private ObservableList<Document> similarDocuments = FXCollections.observableArrayList();
	private ObservableList<Document> queryDocuments = FXCollections.observableArrayList();

	// START Distanzgraphik
	@FXML
	private ScatterChart<Number, Number> resemblanceScatterChart;

	@FXML
	private Button refreshDistanceChartButton;
	// ENDE Distanzgraphik

	// START Dateiimport
	@FXML
	private Button fileChooserButton;

	@FXML
	private Button importFilesButton;

	@FXML
	private ListView<File> fileListView;
	// ENDE Dateiimport

	// START Dokumentenähnlichkeit
	@FXML
	private ComboBox<Document> similarDocumentComboBox;

	@FXML
	private ListView<Document> similarDocumentListView;

	@FXML
	private Button similarDocumentSearchButton;
	// ENDE Dokumentenähnlichkeit

	// START Suchterm
	@FXML
	private TextField searchtermTextField;

	@FXML
	private ListView<Document> searchtermListView;

	@FXML
	private Button searchtermSearchButton;
	// ENDE Suchterm

	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		// Before the GUI has been rendered, this method is called by JavaFx
		// automatically.
		// At this point the controls have been loaded into their fields.

		this.reloadDocumentsFromSolr();
	}

	@FXML
	public void importFile_Event(MouseEvent event) {
		if (this.files.isEmpty()) {
			return;
		}

		try {
			// Add the files to solr.
			App.SOLR.add(this.files);

			// After the files have been imported successfully, the list has to
			// be cleard.
			this.files.clear();

			this.reloadDocumentsFromSolr();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Event for the user interaction to import a new file.
	 * 
	 * @param mouseEvent
	 */
	@FXML
	public void chooseFile_Event(MouseEvent mouseEvent) {

		// Display the filechooser to the user.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose PDF files");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF files", "*.pdf"),
				new ExtensionFilter("All files", "*.*"));

		// Save the selected files in a list of type file.
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(App.MAIN_STAGE);

		if (selectedFiles == null || selectedFiles.isEmpty()) {
			return;
		}

		this.updateFileListView(selectedFiles);
	}

	@FXML
	public void searchFilesForDocumentSimilarity(MouseEvent event) {
		Document selectedDocument = this.similarDocumentComboBox.getSelectionModel().getSelectedItem();

		Document[] similarDocuments = this.docController.getSimilarDocuments(selectedDocument);

		this.similarDocuments.clear();
		this.similarDocumentListView.getItems().clear();

		this.similarDocuments.addAll(similarDocuments);
		this.similarDocumentListView.setItems(this.similarDocuments);
	}

	@FXML
	public void searchFilesForSearchTerms(MouseEvent event) {
		String text = this.searchtermTextField.getText();
		String[] terms = text.split(",| ");

		// Remove unnecessary whitespaces
		for (int i = 0; i < terms.length; i++) {
			String term = terms[i].trim().toLowerCase();
			if (term.length() == 0) {
				term = null;
			}
			terms[i] = term;

		}

		// Create new query
		IDocument query = new Query(terms);

		Document[] queryDocuments = this.docController.getSimilarDocuments(query);

		this.queryDocuments.clear();
		this.searchtermListView.getItems().clear();

		if (queryDocuments.length == 0) {
			queryDocuments = new Document[1];
			queryDocuments[0] = new Document("Wir konnten kein Dokument mit dem Term finden.",
					new SimpleOrderedMap<>());
		}

		this.queryDocuments.addAll(queryDocuments);
		this.searchtermListView.setItems(this.queryDocuments);
	}

	/**
	 * Refreshes the distance chart.
	 * 
	 * @param event
	 */
	@FXML
	public void refreshDistanceChartButton_ClickEvent(MouseEvent event) {
		this.resemblanceScatterChart.getData().clear();

		double[][] distances = this.docController.createDistanceMatrix();

		if (distances.length > 0) {

			XYChart.Series series = new XYChart.Series();
			series.setName("Documents");
			double[][] data = MDSJ.classicalScaling(distances);

			// Determine the min and max values of both dimensions.
			double[] min = new double[2];
			double[] max = new double[2];

			// x-Dimension
			max[0] = data[0][0];
			min[0] = data[0][0];

			// y-Dimension
			max[1] = data[1][0];
			min[1] = data[1][0];
			for (int i = 0; i < data[0].length; ++i) {
				for (int d = 0; d < 2; ++d) {
					min[d] = Math.min(min[d], data[d][i]);
					max[d] = Math.max(max[d], data[d][i]);
				}
			}

			// Recalculate the dimensions, so that most place of the scatter
			// chart is used.
			double width = this.resemblanceScatterChart.getWidth();
			double height = this.resemblanceScatterChart.getHeight();
			for (int i = 0; i < data[0].length; ++i) {
				int x = (int) ((data[0][i] - min[0]) / (max[0] - min[0]) * (width - 4));
				int y = (int) ((data[1][i] - min[1]) / (max[1] - min[1]) * (height - 4));

				// Add Object to series
				series.getData().add(new XYChart.Data(x, y));
			}

			// Add series to scatter chart.
			this.resemblanceScatterChart.getData().addAll(series);

			// Add tooltips to the nodes
			Series<Number, Number> tmpSeries = this.resemblanceScatterChart.getData().get(0);
			List<Document> documents = new ArrayList<Document>(this.docController.getDocuments().values());

			for (int i = 0; i < tmpSeries.getData().size(); i++) {
				String toolTipText = documents.get(i).getId();

				Tooltip.install(tmpSeries.getData().get(i).getNode(), new Tooltip(toolTipText));
			}
		}
	}

	private void updateFileListView(List<File> selectedFiles) {

		this.files.addAll(selectedFiles);
		this.fileListView.setItems(this.files);
	}

	/**
	 * Reloads the documents and replaces the instance of docController with a
	 * new instance. The documents can be accessed with docController field.
	 */
	private void reloadDocumentsFromSolr() {
		this.docController = new DocumentController(App.SOLR.getDocuments());

		// Get documents and add them into similarDocumentComboBox
		this.similarDocumentComboBox.getItems().clear();
		this.similarDocumentComboBox.getItems().addAll(this.docController.getDocuments().values());
	}

}
