package model;

import org.apache.commons.lang3.event.EventListenerSupport;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SpectrumWaterfall implements ReceiverDataConverterListener{
	
	WritableImage waterfallImage;
	PixelWriter writer;
	PixelReader reader;
	int waterfallLength;
	int lastImageLineToWrite;
	int maxImageLines = 100;
	private static final EventListenerSupport<SpectrumWaterfallListener> spectrumWaterfallListeners = new EventListenerSupport<>(SpectrumWaterfallListener.class);
	
	public SpectrumWaterfall(int spectrumDataSize) {
		super();
		waterfallLength = spectrumDataSize;
		lastImageLineToWrite = 0;
		//Creating a writable image 
	    waterfallImage = new WritableImage(waterfallLength, maxImageLines); 
	    writer = waterfallImage.getPixelWriter();
	    reader = waterfallImage.getPixelReader();
		
	}
	
	public SpectrumWaterfall(int waterfallLength, int maxImageLines) {
		super();
		this.waterfallLength = waterfallLength;
		this.maxImageLines = maxImageLines;
		
		//Creating a writable image 
	    waterfallImage = new WritableImage(waterfallLength, maxImageLines); 
	    writer = waterfallImage.getPixelWriter();
	}
	
	/**
     * Dodaje s³uchcza zdarzeñ odebrania danych.
     *
     * @param listener - dodawany s³uchacz
     */
	public void addListener(SpectrumWaterfallListener listener) {
        if (listener != null) {
        	spectrumWaterfallListeners.addListener(listener);
        }
    }

    /**
     * Usuwa s³uchacza zdarzeñ odebrania danych.
     *
     * @param listener - usuwany s³ychacz
     */
    public void removeListener(SpectrumWaterfallListener listener) {
        if (listener != null) {
        	spectrumWaterfallListeners.removeListener(listener);
        }
    }

	private void fillImageLine(double[] receivedData) {		
		//TO DO 
		//poprawiæ - wpisywanie w odwrotnej kolejnoœci na górze najnowsze dane, starsze linie przesuwane s¹ w dó³, najstarsze dane s¹ usuwane
		
		// Writing the color of the image		
		for (int x = 0; x < waterfallLength; x++) {
			// Setting the color to the writable image
			writer.setColor(x, lastImageLineToWrite, Color.gray(receivedData[x]/125));
		}	
		lastImageLineToWrite = ((lastImageLineToWrite+1)==maxImageLines) ? maxImageLines -1 : lastImageLineToWrite+1;
		
		System.out.println("lastImageLineToWrite: " + lastImageLineToWrite);
	}
	
	
	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		
		//dopisywanie danych do waterfallImage
		if(dataSize == waterfallLength) {
			// Writing the color of the image
			fillImageLine(receivedData);			
		}
		//utworznie nowego obrazu
		else {			
			waterfallLength = dataSize;
			
			//Creating a writable image 
		    waterfallImage = new WritableImage(waterfallLength, maxImageLines); 
		    writer = waterfallImage.getPixelWriter();
		    // Writing the color of the image
		    fillImageLine(receivedData);			
		}
		
		
		//przekazanie obrazu do klasy wyœwietlaj¹cej
		spectrumWaterfallListeners.fire().onImageProcessed( waterfallImage, seqNumber, timeStamp, freqStart, freqStep);  
		
		
	}
	


}
