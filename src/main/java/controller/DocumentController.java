package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Document;
import model.Term;
import model.interfaces.IDocument;

/**
 * 
 * @author Jan-Hendrick Hemmje und Johannes Giere
 *
 */
public class DocumentController {

	private Map<String, Document> documents = new HashMap<String, Document>();
	private Map<String, List<String>> invertedIndex = new HashMap<String, List<String>>();
	private Map<String, Double> IDF = new HashMap<String, Double>();

	/**
	 * Returns all documents that are currently stored on the server.
	 * 
	 * @return All stored documents
	 */
	public Map<String, Document> getDocuments() {
		return this.documents;
	}

	/**
	 * Cleares the cache and set the documents from the parameter into the
	 * cache.
	 * 
	 * @param documents
	 */
	private void setDocuments(List<Document> documents) {
		// Create hash map
		this.documents.clear();

		for (Document doc : documents) {
			this.documents.put(doc.getId(), doc);
		}
	}

	/**
	 * Computes the inverted index, IDF values and TF-IDF values for the
	 * documents.
	 * 
	 * @param documents
	 */
	public DocumentController(List<Document> documents) {
		this.setDocuments(documents);

		this.calcInvertedIndex();
		this.calcIDF();
		this.calcTFIDF();

	}

	/**
	 * Creates an inverted index. The key is the search term and the value is a
	 * list of all documents, in which the term occurs.
	 */
	private void calcInvertedIndex() {

		this.invertedIndex.clear();

		// Iterate every document
		for (Map.Entry<String, Document> docPair : this.documents.entrySet()) {
			Document doc = docPair.getValue();

			// Iterate every term of document.
			for (Map.Entry<String, Term> termEntry : doc.getTerms().entrySet()) {
				Term term = termEntry.getValue();
				List<String> hashMapDocuments;

				if (this.invertedIndex.containsKey(term.getName())) {
					// If the key already exists, the list of documents is
					// extracted from the value.
					hashMapDocuments = this.invertedIndex.get(term.getName());

					// The document is added to the list.
					hashMapDocuments.add(doc.getId());
				} else {
					// If the key does not exist, a new entry is created.
					hashMapDocuments = new ArrayList<String>();
					hashMapDocuments.add(doc.getId());

					this.invertedIndex.put(term.getName(), hashMapDocuments);
				}
			}
		}
	}

	/**
	 * Computes to every term its IDF value. It is mandatory that the inverted
	 * index has been created and is up to date.
	 */
	private void calcIDF() {
		this.IDF.clear();

		// For every term of the inverted index, the IDF value is computed.
		// Every value is stored in the HashMap "this.IDF".
		for (Map.Entry<String, List<String>> term : this.invertedIndex.entrySet()) {
			this.IDF.put(term.getKey(), this.calcIDF(term.getValue().size()));
		}
	}

	/**
	 * Calculates the IDF value for one term.
	 * 
	 * @param numberOfDocumentsWithTermInIt
	 *            The number the document that hold that term.
	 * @return The IDF value
	 */
	private double calcIDF(int numberOfDocumentsWithTermInIt) {
		double totalNumberOfDocuments = (double) this.documents.size();
		double result = Math.log(totalNumberOfDocuments / numberOfDocumentsWithTermInIt) / Math.log(Math.E);

		return result;
	}

	/**
	 * Calculates for every term in a document its TF-IDF value.
	 */
	private void calcTFIDF() {
		Document doc;
		Term documentsTerm;
		double idf;
		double tf;

		// Iterate over all terms that stored in the inverted index.
		for (Map.Entry<String, List<String>> term : this.invertedIndex.entrySet()) {

			// Because the key of the element is the term's name, we can get the
			// idf value of it through the IDF hash map
			String termName = term.getKey();
			idf = this.IDF.get(termName);

			// Next we look at every document in which the term appears.
			for (String docId : term.getValue()) {

				// Loading the document
				doc = this.documents.get(docId);

				// Getting the term object of that document with its name.
				documentsTerm = doc.getTerms().get(termName);
				tf = documentsTerm.getTf();

				// Set the TF-IDF value.
				documentsTerm.setTfidf(tf * idf);
			}
		}
	}

	/**
	 * Returns all documents that are similar to the query.
	 * 
	 * @param query
	 *            The query.
	 * @return All similar documents
	 */
	@SuppressWarnings("unchecked")
	public Document[] getSimilarDocuments(IDocument query) {

		List<DocumentValue> similarities = new ArrayList<DocumentValue>();

		// Get the euclidean length of the query.
		double denominator_query = query.getEuclideanLength();

		// Get every single document.
		for (Map.Entry<String, Document> doc : this.documents.entrySet()) {

			// Calculate the numerator
			this.calculateSimilarity(doc.getValue(), query, denominator_query, similarities);
		}

		// Sort the similarities
		Collections.sort(similarities);

		// Take the top 10 documents
		int arraySize = similarities.size() > 10 ? 10 : similarities.size();
		Document[] similarDocuments = new Document[arraySize];
		for (int i = 0; i < similarDocuments.length; i++) {
			similarDocuments[i] = similarities.get(i).getDocument();
		}

		return similarDocuments;
	}

	/**
	 * Computes the similarity between one document and the query and adds the
	 * document to the similarites array.
	 * 
	 * @param doc
	 *            Document that is stored and indexed.
	 * @param query
	 *            User query.
	 * @param denominator_query
	 *            The computed denominator of query.
	 * @param similarities
	 *            An array which will hold the documents and the similarity
	 *            value as a DocumentValue object.
	 */
	private void calculateSimilarity(Document doc, IDocument query, double denominator_query,
			List<DocumentValue> similarities) {
		double numerator = this.calculateCosineSimilarityNumerator(doc, query);

		// The denominator is the product of the euclidean lengths of the
		// query and term.
		double denominator_doc = doc.getEuclideanLength();
		double denominator = denominator_query * denominator_doc;

		// Calculate the similarity between the query and document.
		double similarity = numerator / denominator;

		// If the similarity is a number and
		// If the similarity is not like zero, we want to add it to our
		// list.
		if (!Double.isNaN(similarity) && !(similarity < 0.000001 && similarity > -0.000001)) {
			similarities.add(new DocumentValue(similarity, doc));
		}
	}

	/**
	 * Computes the similarity of a document and a query/document.
	 * 
	 * @param doc
	 *            A document.
	 * @param query
	 *            A query or document
	 * @param denominator_query
	 *            The denominator of the query.
	 * @return The similarity value.
	 */
	private double calculateSimilarity(Document doc, IDocument query, double denominator_query) {
		double numerator = this.calculateCosineSimilarityNumerator(doc, query);

		// The denominator is the product of the euclidean lengths of the
		// query and term.
		double denominator_doc = doc.getEuclideanLength();
		double denominator = denominator_query * denominator_doc;

		// Calculate the similarity between the query and document.
		double similarity = numerator / denominator;

		// If the similarity is a number and
		// If the similarity is not like zero, we want to add it to our
		// list.
		if (!Double.isNaN(similarity) && !(similarity < 0.000001 && similarity > -0.000001)) {
			return similarity;
		} else {
			return 0.0;
		}
	}

	/**
	 * This class holds a reference to a document and its similarity value to
	 * given query.
	 *
	 */
	private class DocumentValue implements Comparable {
		private double similarity;
		private Document document;

		public double getSimilarity() {
			return this.similarity;
		}

		private void setSimilarity(double similarity) {
			this.similarity = similarity;
		}

		public Document getDocument() {
			return this.document;
		}

		private void setDocument(Document document) {
			this.document = document;
		}

		public DocumentValue(double similarity, Document document) {
			this.setSimilarity(similarity);
			this.setDocument(document);
		}

		@Override
		public int compareTo(Object o) {
			if (!(o instanceof DocumentValue)) {
				return 0;
			}

			// Sort DESC
			int result = Double.compare(((DocumentValue) o).getSimilarity(), this.getSimilarity());
			return result;
		}
	}

	/**
	 * Creates a distance matrix, based on a list of document vectors.
	 * 
	 * @return Distance matrix
	 */
	public double[][] createDistanceMatrix() {
		List<Document> documents = new ArrayList<Document>(this.documents.values());

		final int n = documents.size();
		double[][] d = new double[n][n];
		for (int i = 0; i < n; ++i) {
			for (int j = i + 1; j < n; ++j) {
				double distance = this.calculateSimilarity(documents.get(i), documents.get(j),
						documents.get(j).getEuclideanLength());

				// Convert similarity to distance.
				distance = 1 - distance;

				d[i][j] = distance;
				d[j][i] = d[i][j];
			}
		}

		return d;
	}

	/**
	 * Computes the cosine similarity numerator number.
	 * 
	 * @param doc
	 *            The document.
	 * @param query
	 *            The query.
	 * @return The numerator value.
	 */
	private double calculateCosineSimilarityNumerator(IDocument doc, IDocument query) {

		double numerator = 0;

		// Calculate the cosine similarity between the query and the document.
		// Get every term, that is been specified by the query.
		for (Map.Entry<String, Term> term_query : query.getTerms().entrySet()) {

			// Check, whether the document has that term.
			Term term_doc = doc.getTerms().get(term_query.getKey());
			if (term_doc == null) {
				// If the document does not contain the term, we
				// skip the calculation and set the value for this term to
				// 0.
				continue;
			}

			// Get the TF-IDF value of the query's term.
			double tfidf_query = term_query.getValue().getTfidf();
			// Get the TF-IDF value of the doc's term.
			double tfidf_doc = term_doc.getTfidf();

			// Multiple both TF-IDF values and add them to the numerator.
			numerator += (tfidf_query * tfidf_doc);
		}

		return numerator;
	}

}
