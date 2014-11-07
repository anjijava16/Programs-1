package org.whu.hjp.ctr.spm;

import java.util.List;

public class FeatureRepresentation {

	private List<Datum> data;
	public final Index featureIndex = new Index();
	public final Index labelIndex = new Index();

	/** constructor */
	public FeatureRepresentation(List<Datum> data) {
		this.data = data;
		for (Datum datum : data) {
			labelIndex.add(datum.label);
			Object label = datum.label;
			Object previousLabel = datum.previousLabel;
			for (Object f : datum.features) {
				// featureIndex.add(label + " " + f);
				featureIndex.add(f);
			}
			// featureIndex.add(previousLabel + " " + label);
		}
	}

}