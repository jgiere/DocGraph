package solr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.annotation.Obsolete;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.SimpleOrderedMap;

import model.Document;

import org.apache.solr.client.solrj.request.AbstractUpdateRequest;

/**
 * This class handles all connections/requests to a solr instance.
 * 
 * @author Jan-Hendrick Hemmje und Johannes Giere
 *
 */
public class SolrConnection {

	/**
	 * Connection string to the solr cluster
	 */
	private static final String CONNECTION_STRING = "localhost:9983";

	/**
	 * Instance of the CloudSolrClient connection object.
	 */
	private static final CloudSolrClient SOLR_INSTANCE = new CloudSolrClient.Builder().withZkHost(CONNECTION_STRING)
			.build();

	/**
	 * Creates an object of the ColrConnection. This class connects to the
	 * specified solr cluster, sets the default collection to "wm" sets the
	 * parser to XMLResponseParser.
	 */
	public SolrConnection() {
		SolrConnection.SOLR_INSTANCE.setDefaultCollection("wm");
		SolrConnection.SOLR_INSTANCE.setParser(new XMLResponseParser());
	}

	/**
	 * Queries the SOLR cluster for documents which suit to {@link q_query}.
	 * 
	 * @param q_query
	 *            The query string
	 * @return A SolrDocumentList with the documents which suit to the query.
	 */
	@Obsolete
	public SolrDocumentList query(String q_query) {
		// Set variables and objects
		SolrQuery query = new SolrQuery();
		SolrDocumentList list = new SolrDocumentList();

		// Set the query
		query.set("q", q_query);
		query.set("qt", "/select");

		try {
			// Query data and save it to list.
			QueryResponse response = SolrConnection.SOLR_INSTANCE.query(query);
			list = response.getResults();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * Queries all documents and its terms. This method is used when
	 * bootstrapping the application. This method returns all indexed documents,
	 * the document's terms and the frequency of terms in the document.
	 * 
	 * @return A list with all documents.
	 */
	@SuppressWarnings("rawtypes")
	public List<Document> getDocuments() {
		List<Document> wordCounts = new ArrayList<Document>();

		// Set variables and objects
		SolrQuery query = new SolrQuery();

		// Set the query
		query.set("q", "*:*");

		// Use the termvector handler
		query.set("qt", "/tvrh");

		// Indicate that we want to retrieve the term frequency of terms per
		// document.
		query.set("tv.tf", true);

		// Set the maximum number of returned rows to the max value of Integer.
		// The default value is 10, which will only return 10 rows in the
		// termvector.
		query.setRows(Integer.MAX_VALUE);

		try {
			// Query data and save it.
			QueryResponse response = SolrConnection.SOLR_INSTANCE.query(query);

			// Get term vectors
			SimpleOrderedMap termVectors = (SimpleOrderedMap) response.getResponse().get("termVectors");
			
			for (int i = 0; i < termVectors.size(); i++) {

				String docName = termVectors.getName(i);

				// Get terms
				SimpleOrderedMap terms = (SimpleOrderedMap) ((SimpleOrderedMap) termVectors.get(docName))
						.get("attr_content");

				wordCounts.add(new Document(docName, terms));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return wordCounts;
	}

	/**
	 * Adds the files from {@link selectedFiled} to the SOLR collection.
	 * 
	 * @param selectedFiles
	 *            List of files, input to SOLR
	 * @throws SolrServerException
	 *             Throws a SOLR server exception, if an error on the solr side
	 *             occurs.
	 * @throws IOException
	 *             Is thrown, if the files cannot been loaded from filesystem.
	 */
	public void add(List<File> selectedFiles) throws SolrServerException, IOException {
		// Check if the list is not empty.
		if (selectedFiles.isEmpty()) {
			return;
		}

		// Map the files to an SolrInputDocument.
		Collection<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>();
		for (File file : selectedFiles) {
			if (file.isFile()) {
				solrDocs.add(this.createSolrInputDocument(file));

				ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");

				up.addFile(file, "application/pdf");

				up.setParam("literal.id", file.getName());
				up.setParam("uprefix", "attr_");
				up.setParam("fmap.content", "attr_content");

				up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);

				// Send the document to SOLR.
				SolrConnection.SOLR_INSTANCE.request(up);
			}
		}
	}

	/**
	 * Maps a File object to a SolrInputDocument
	 * 
	 * @param file
	 * @return
	 */
	private SolrInputDocument createSolrInputDocument(File file) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("name", file.getName());

		return doc;
	}
}
