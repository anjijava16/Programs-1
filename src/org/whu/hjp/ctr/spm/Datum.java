package org.whu.hjp.ctr.spm;

import java.util.*;

/**
 * Data Structure for word. Each word has its features, guessLabel and
 * previousLable
 */
public class Datum {

	public final String word;
	public final String label;
	public List<String> features;
	public String guessLabel;
	public String previousLabel;

	public Datum(String word, String label) {
		this.word = word;
		this.label = label;
	}
}