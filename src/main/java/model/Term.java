package model;

/**
 * A single search term. Please consider that a term belongs to a single
 * document and not multiple documents. The reason to that is that the td value
 * is calculated on the number of times a term appears in a document and the
 * total number of terms in a document. If we want to store the idf value of
 * that term in this class, we have to update every single term in this
 * application.
 * 
 * @author Johannes Giere, Jan-Hendrick Hemmje
 *
 */
public class Term {
	private String name;
	private int amount;
	private double tf;
	private double tfidf;

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public int getAmount() {
		return amount;
	}

	private void setAmount(int amount) {
		this.amount = amount;
	}

	public double getTf() {
		return tf;
	}

	private void setTf(double tf) {
		this.tf = tf;
	}

	public double getTfidf() {
		return this.tfidf;
	}

	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}

	public Term(String name, int amount, int totalNumberOfTerms) {
		this.setName(name);
		this.setAmount(amount);
		this.setTf(((double)this.getAmount()) / totalNumberOfTerms);
	}
}
