package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import model.interfaces.IDocument;

public class Query implements IDocument {

	private double euclideanLength = -1;
	private Map<String, Term> terms = new HashMap<String, Term>();

	public Query(String[] terms) {

		for (int i = 0; i < terms.length; i++) {
			Term term = new Term(terms[i], 1, terms.length);
			term.setTfidf(1);
			this.terms.put(terms[i], term);
		}

	}

	@Override
	public Map<String, Term> getTerms() {
		return this.terms;
	}

	@Override
	public double getEuclideanLength() {
		// The euclidean length should only be computed, when it is necessary.
		if (this.euclideanLength == -1 && this.terms.size() > 0) {
			this.calcEuclideanLength();
		}
		return this.euclideanLength;
	}

	private void setEuclideanLength(double result) {
		this.euclideanLength = result;
	}

	private void calcEuclideanLength() {
		if (this.terms.size() == 0) {
			return;
		}

		double sum = 0;
		for (Entry<String, Term> term : this.terms.entrySet()) {
			sum += Math.pow(term.getValue().getTfidf(), 2);
		}

		double result = Math.sqrt(sum);

		this.setEuclideanLength(result);
	}

}
