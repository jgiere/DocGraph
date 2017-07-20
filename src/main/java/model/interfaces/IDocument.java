package model.interfaces;

import java.util.Map;

import model.Term;

public interface IDocument {
	Map<String, Term> getTerms();
	double getEuclideanLength();
}
