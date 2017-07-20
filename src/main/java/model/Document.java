package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.solr.common.util.SimpleOrderedMap;

import model.interfaces.IDocument;

public class Document implements IDocument {

	private String id;
	private SimpleOrderedMap rawCounts;
	private Map<String, Term> terms = new HashMap<String, Term>();
	private double euclideanLength = -1;

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	@Override
	public Map<String, Term> getTerms() {
		return this.terms;
	}

	public SimpleOrderedMap getRawCounts() {
		return rawCounts;
	}

	private void setRawCounts(SimpleOrderedMap rawCounts) {
		this.rawCounts = rawCounts;

		this.createTermMap();
	}

	@Override
	public double getEuclideanLength() {
		// The euclidean length should only be computed, when it is necessary.
		if (this.euclideanLength == -1 && this.terms.size() > 0) {
			this.calcEuclideanLength();
		}
		return this.euclideanLength;
	}

	private void setEuclideanLength(double euclideanLength) {
		this.euclideanLength = euclideanLength;
	}

	public Document(String docId, SimpleOrderedMap wordCounts) {
		this.setId(docId);
		this.setRawCounts(wordCounts);
	}

	private void createTermMap() {
		this.terms.clear();

		Map<String, SimpleOrderedMap> map = this.rawCounts.asMap(-1);
		int totalNumberOfTerms = 0;

		// Calculate total number of terms in the document
		for (Map.Entry<String, SimpleOrderedMap> entry : map.entrySet()) {

			totalNumberOfTerms += (int) ((SimpleOrderedMap) entry.getValue()).getVal(0);
		}

		for (Map.Entry<String, SimpleOrderedMap> entry : map.entrySet()) {
			String termName = entry.getKey();

			int amountOfTerm = (int) ((SimpleOrderedMap) entry.getValue()).getVal(0);

			this.terms.put(termName, new Term(termName, amountOfTerm, totalNumberOfTerms));
		}
	}

	private void calcEuclideanLength() {
		if (this.terms.size() == 0) {
			return;
		}
		
		double sum = 0;
		for(Entry<String, Term> term : this.terms.entrySet()) {
			sum += Math.pow(term.getValue().getTfidf(), 2);
		}
		
		double result = Math.sqrt(sum);
		
		this.setEuclideanLength(result);
	}
	
	@Override
	public String toString() {
		return this.getId();
	}
}
