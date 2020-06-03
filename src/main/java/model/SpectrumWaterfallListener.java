package model;

import javafx.scene.image.WritableImage;

public interface SpectrumWaterfallListener {

	/**
	 * Interfejs dla s³uchaczy SpectrumWaterfall. Do póŸniejszych zastosowañ.
	 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
	 */
		
	public void onImageProcessed(WritableImage waterfallImage, final int seqNumber, final double timeStamp, final double freqStart, final double freqStep);

	
}
