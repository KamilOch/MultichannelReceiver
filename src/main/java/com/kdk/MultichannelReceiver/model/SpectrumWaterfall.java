package com.kdk.MultichannelReceiver.model;

import org.apache.commons.lang3.event.EventListenerSupport;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SpectrumWaterfall implements ReceiverDataConverterListener {

	WritableImage waterfallImage;
	PixelWriter writer;
	PixelReader reader;
	int waterfallLength;
	int lastImageLineToWrite;
	int maxImageLines = 100;
	private static final EventListenerSupport<SpectrumWaterfallListener> spectrumWaterfallListeners = new EventListenerSupport<>(
			SpectrumWaterfallListener.class);

	public SpectrumWaterfall(int spectrumDataSize) {
		super();
		waterfallLength = spectrumDataSize;
		lastImageLineToWrite = 0;
		// Creating a writable image
		waterfallImage = new WritableImage(waterfallLength, maxImageLines);
		writer = waterfallImage.getPixelWriter();
		reader = waterfallImage.getPixelReader();

	}

	public SpectrumWaterfall(int waterfallLength, int maxImageLines) {
		super();
		this.waterfallLength = waterfallLength;
		this.maxImageLines = maxImageLines;

		// Creating a writable image
		waterfallImage = new WritableImage(waterfallLength, maxImageLines);
		writer = waterfallImage.getPixelWriter();
	}

	/**
	 * Dodaje s�uchcza zdarze� odebrania danych.
	 *
	 * @param listener - dodawany s�uchacz
	 */
	public void addListener(SpectrumWaterfallListener listener) {
		if (listener != null) {
			spectrumWaterfallListeners.addListener(listener);
		}
	}

	/**
	 * Usuwa s�uchacza zdarze� odebrania danych.
	 *
	 * @param listener - usuwany s�ychacz
	 */
	public void removeListener(SpectrumWaterfallListener listener) {
		if (listener != null) {
			spectrumWaterfallListeners.removeListener(listener);
		}
	}

	private void fillImageLine(double[] receivedData) {
		// TO DO
		// poprawi� - wpisywanie w odwrotnej kolejno�ci na g�rze najnowsze dane, starsze
		// linie przesuwane s� w d�, najstarsze dane s� usuwane

		// Writing the color of the image
		for (int x = 0; x < waterfallLength; x++) {
			// Setting the color to the writable image
			writer.setColor(x, lastImageLineToWrite, Color.gray(receivedData[x] / 125));
		}
		lastImageLineToWrite = ((lastImageLineToWrite + 1) == maxImageLines) ? maxImageLines - 1
				: lastImageLineToWrite + 1;

		System.out.println("lastImageLineToWrite: " + lastImageLineToWrite);
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {

		// dopisywanie danych do waterfallImage
		if (dataSize == waterfallLength) {
			// Writing the color of the image
			fillImageLine(receivedData);

			if (lastImageLineToWrite == maxImageLines - 1)
				copyImage();

		}
		// utworznie nowego obrazu
		else {
			waterfallLength = dataSize;

			// Creating a writable image
			waterfallImage = new WritableImage(waterfallLength, maxImageLines);
			writer = waterfallImage.getPixelWriter();
			// Writing the color of the image
			fillImageLine(receivedData);
		}

		// przekazanie obrazu do klasy wy�wietlaj�cej
		spectrumWaterfallListeners.fire().onImageProcessed(waterfallImage, seqNumber, timeStamp, freqStart, freqStep);

	}

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

	public void copyImage() {
		int height = maxImageLines;
		int width = waterfallLength;

		for (int y = 1; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = reader.getColor(x, y);// from
				writer.setColor(x, y - 1, color);
			}
		}

	}

//	@Override
//	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
//			double freqStep) {
//
//		// dopisywanie danych do waterfallImage
//		if (dataSize == waterfallLength) {
//			// Writing the color of the image
//			fillImageLine(receivedData);
//		}
//		// utworznie nowego obrazu
//		else {
//			waterfallLength = dataSize;
//
//			// Creating a writable image
//			waterfallImage = new WritableImage(waterfallLength, maxImageLines);
//			writer = waterfallImage.getPixelWriter();
//			// Writing the color of the image
//			fillImageLine(receivedData);
//		}
//
//		// przekazanie obrazu do klasy wy�wietlaj�cej
//		spectrumWaterfallListeners.fire().onImageProcessed(waterfallImage, seqNumber, timeStamp, freqStart, freqStep);
//
//	}

}
