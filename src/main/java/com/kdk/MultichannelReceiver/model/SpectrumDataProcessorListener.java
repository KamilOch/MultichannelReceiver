package com.kdk.MultichannelReceiver.model;

public interface SpectrumDataProcessorListener {
	/**
	 * Interfejs dla słuchaczy SpectrumDataProcessor. Do pózniejszych zastosowań.
	 * 
	 * @author Kamil Ochnik
	 */
	void onDataProcessed(double[] frequency, double[] signalLevel, int seqNumber, double timeStamp,
						 double threshold);
}
