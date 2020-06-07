package com.kdk.MultichannelReceiver.model;

import javafx.scene.image.WritableImage;

public interface SpectrumWaterfallListener {

	/**
	 * Interfejs dla s�uchaczy SpectrumWaterfall. Do p�niejszych zastosowa�.
	 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
	 */
		
	public void onImageProcessed(WritableImage waterfallImage, final int seqNumber, final double timeStamp, final double freqStart, final double freqStep);

	
}
