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
	int maxImageLines = 150;
	private static final EventListenerSupport<SpectrumWaterfallListener> spectrumWaterfallListeners = new EventListenerSupport<>(
			SpectrumWaterfallListener.class);

	public SpectrumWaterfall(int spectrumDataSize) {
		super();
		this.waterfallLength = spectrumDataSize;
		this.lastImageLineToWrite = 0;
		// Creating a writable image
		waterfallImage = new WritableImage(waterfallLength, maxImageLines);
		writer = waterfallImage.getPixelWriter();
		reader = waterfallImage.getPixelReader();

	}

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
