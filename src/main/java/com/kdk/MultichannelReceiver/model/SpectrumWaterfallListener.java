package com.kdk.MultichannelReceiver.model;

import javafx.scene.image.WritableImage;

public interface SpectrumWaterfallListener {

	/**
	 * Interfejs dla s�uchaczy SpectrumWaterfall do wykorzystania w MainWindowController i innych klasach. 
	 * Zdarzenie onImageProcessed jest generowane po odbiorze ka�dego nowego pakietu z danymi widma z odbiornika. 
	 * Nowa linia w obrazie (dane z jednego pakietu widma) jest dodawana na dole i po wype�nieniu ca�ego zdefiniowanego obszaru usuwana jest stara linia z g�ry obrazu. 
	 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
	 */
		
	public void onImageProcessed(WritableImage waterfallImage, final int seqNumber, final double timeStamp, final double freqStart, final double freqStep);

	
}
