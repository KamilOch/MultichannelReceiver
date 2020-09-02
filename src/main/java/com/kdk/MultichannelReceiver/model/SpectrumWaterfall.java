package com.kdk.MultichannelReceiver.model;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * Klasa SpectrumWaterfall do tworzenia obrazu 2D widma w funkcji czasu
 * @author Kamil Wilgucki k.wilgucki@wil.waw.pl
 *
 */
public class SpectrumWaterfall implements ReceiverDataConverterListener {

	WritableImage waterfallImage;
	PixelWriter writer;
	PixelReader reader;
	int waterfallLength;
	int lastImageLineToWrite;
	int maxImageLines = 150;
	private static final EventListenerSupport<SpectrumWaterfallListener> spectrumWaterfallListeners = new EventListenerSupport<>(
			SpectrumWaterfallListener.class);
	
	/**
     * Konstruktor Klasy SpectrumWaterfall.
     * @param spectrumDataSize - długość danych widma do wyświetlenia - czyli szerokość wyświetlanego obrazu
     */
	public SpectrumWaterfall(int spectrumDataSize) {
		super();
		this.waterfallLength = spectrumDataSize;
		this.lastImageLineToWrite = 0;
		// Creating a writable image
		waterfallImage = new WritableImage(waterfallLength, maxImageLines);
		writer = waterfallImage.getPixelWriter();
		reader = waterfallImage.getPixelReader();

	}
		
	/**
     * Konstruktor Klasy SpectrumWaterfall.
     * @param waterfallLength - długość danych widma do wyświetlenia - czyli szerokość wyświetlanego obrazu
     * @param maxImageLines - wysokość obrazu, inaczej liczba wyświetlanych linii widma w obrazie (wielkość okna czasowego)
     */
	public SpectrumWaterfall(int waterfallLength, int maxImageLines) {
		super();
		this.waterfallLength = waterfallLength;
		this.maxImageLines = maxImageLines;
		this.lastImageLineToWrite = 0;

		// Creating a writable image
		waterfallImage = new WritableImage(waterfallLength, maxImageLines);
		writer = waterfallImage.getPixelWriter();
		reader = waterfallImage.getPixelReader();
	}

	/**
	 * Dodaje słuchcza zdarzeń odebrania danych.
	 *
	 * @param listener - dodawany słuchacz
	 */
	public void addListener(SpectrumWaterfallListener listener) {
		if (listener != null) {
			spectrumWaterfallListeners.addListener(listener);
		}
	}

	/**
	 * Usuwa słuchacza zdarzeń odebrania danych.
	 *
	 * @param listener - usuwany słychacz
	 */
	public void removeListener(SpectrumWaterfallListener listener) {
		if (listener != null) {
			spectrumWaterfallListeners.removeListener(listener);
		}
	}

	/**
	 * Wpisuje nową linię odebranych danych na dole obrazu waterfall.
	 *
	 * @param receivedData - odebrane dane widma z odbiornika
	 */
	private void fillImageLine(double[] receivedData) {
		// TO DO
		// poprawi� - wpisywanie w odwrotnej kolejno�ci na g�rze najnowsze dane, starsze
		// linie przesuwane s� w d�, najstarsze dane s� usuwane

		// Writing the color of the image
		int imageWidth = ((waterfallLength-1) <= receivedData.length) ? (waterfallLength-1) : receivedData.length;
		for (int x = 0; x < imageWidth; x++) {
			// Setting the color to the writable image
			writer.setColor(x, lastImageLineToWrite, Color.gray(Math.abs(130 + receivedData[x]) / 150));//-130dBm + 20dBm
		}
		lastImageLineToWrite = ((lastImageLineToWrite + 1) == maxImageLines) ? maxImageLines - 1
				: lastImageLineToWrite + 1;

		//System.out.println("lastImageLineToWrite: " + lastImageLineToWrite);
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub

	}
 
	/**
	 * Zdarzenie onDataReceived - do przekazywania odebranych danych widma z odbiornika.
	 *
	 * @param receivedData - odebrane dane widma z odbiornika
	 * @param dataSize - rozmiar danych widma (ilość próbek częstotliwości)
	 * @param seqNumber - numer sekwencyjny ostatniego pakietu danych
	 * @param timeStamp - znacznik czasu ostatniego pakietu danych
	 * @param freqStart - częstotliwosć startowa danych ostatniego pakietu danych
	 * @param freqStep - krok częstotliwosci dla danych ostatniego pakietu danych
	 */
	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {

		// dopisywanie danych do waterfallImage
		if (dataSize == waterfallLength) {
			// Writing the color of the image
			

			if (lastImageLineToWrite == maxImageLines-1) {
				fillImageLine(receivedData);
				copyImage();
			}
			else
				fillImageLine(receivedData);
				

		}
		// utworznie nowego obrazu
		else {
			this.waterfallLength = dataSize;

			// Creating a writable image
			this.waterfallImage = new WritableImage(waterfallLength, maxImageLines);
			this.writer = waterfallImage.getPixelWriter();
			this.reader = waterfallImage.getPixelReader();
			
			// Writing the color of the image
			fillImageLine(receivedData);
		}

		// przekazanie obrazu do klasy wy�wietlaj�cej
		spectrumWaterfallListeners.fire().onImageProcessed(waterfallImage, seqNumber, timeStamp, freqStart, freqStep);

	}

	/**
	 * Metoda kopiująca obraz.
	 *
	 * @param image - obraz
	 */
	public static WritableImage copyImage(Image image) {
		int height = (int) image.getHeight();
		int width = (int) image.getWidth();
		PixelReader pixelReader = image.getPixelReader();
		WritableImage writableImage = new WritableImage(width, height);
		PixelWriter pixelWriter = writableImage.getPixelWriter();

		for (int y = 0; y < height - 1; y++) {
			for (int x = 0; x < width; x++) {
				Color color = pixelReader.getColor(x, y);
				pixelWriter.setColor(x, y, color);
			}
		}
		return writableImage;
	}
	
	/**
	 * Metoda kopiująca obraz z wyjątkiem pierwszej linni (najstarszych danych).
	 *
	 *
	 */
	public void copyImage() {
		int height = this.maxImageLines;
		int width = this.waterfallLength;

		for (int y = 1; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = this.reader.getColor(x, y);// from
				this.writer.setColor(x, y - 1, color);
			}
		}

	}



}
