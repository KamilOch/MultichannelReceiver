package com.kdk.MultichannelReceiver.model;

import javafx.scene.image.WritableImage;

/**
 * Interfejs dla słuchaczy SpectrumWaterfall do wykorzystania w MainWindowController i innych klasach.
 * @author Kamil Wilgucki k.wilgucki@wil.waw.pl
 */
public interface SpectrumWaterfallListener {

	/**
	 * Interfejs dla słuchaczy SpectrumWaterfall do wykorzystania w MainWindowController i innych klasach.
	 * Zdarzenie onImageProcessed jest generowane po odbiorze każdego nowego pakietu z danymi widma z odbiornika.
	 * Nowa linia w obrazie (dane z jednego pakietu widma) jest dodawana na dole i po wypełnieniu całego zdefiniowanego obszaru usuwana jest stara linia z góry obrazu.
	 * @param waterfallImage - obraz z danymi waterfall
	 * @param seqNumber - numer sekwencyjny ostatniej linni obrazu
	 * @param timeStamp - znacznik czasu ostatniej linni obrazu
	 * @param freqStart - częstotliwosć startowa danych ostatniej linii obrazu
	 * @param freqStep - krok częstotliwosci dla danych ostatniej linii obrazu
	 */
	public void onImageProcessed(WritableImage waterfallImage, final int seqNumber, final double timeStamp, final double freqStart, final double freqStep);

	
}
