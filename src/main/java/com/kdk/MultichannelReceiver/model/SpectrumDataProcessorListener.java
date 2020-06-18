package com.kdk.MultichannelReceiver.model;

import javafx.scene.image.WritableImage;

public interface SpectrumDataProcessorListener {
	/**
	 * Interfejs dla słuchaczy SpectrumDataProcessor. Do pózniejszych zastosowań.
	 * 
	 * @author Kamil Ochnik
	 */
	public void onDataProcess(double[] frequency, double[] signalLevel, int seqNumber, double timeStamp,
			double threshold);
}
