package com.kdk.MultichannelReceiver.model;

import com.kdk.MultichannelReceiver.model.utils.PacketConverter;
import javafx.application.Platform;
import org.apache.commons.lang3.event.EventListenerSupport;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Klasa managera umozliwiajaca podlaczenie klas jako sluchaczy zdarzen o odbiorze danych widma z odbiornika
 * 
 * @author Kamil Wilgucki k.wilgucki@wil.waw.pl
 */
public class ReceiverDataConverter {
	
	private static final EventListenerSupport<ReceiverDataConverterListener> receiverDataConverterListeners = new EventListenerSupport<>(ReceiverDataConverterListener.class);
	int repetitionCounter;
	int fixedFreq;
	//BlockingQueue<PacketConverter> blockingQueue;
	ConcurrentLinkedQueue<PacketConverter> blockingQueue;
	private static Thread readingThread; 
	

	/**
     * Konstruktor klasy ReceiverDataConverter
     * @param blockingSpectrumDataQueue - kolejka FIFO z pakitami typu PacketConverter z odbiornika
     */  
	public ReceiverDataConverter(ConcurrentLinkedQueue<PacketConverter> blockingSpectrumDataQueue) {
		this.repetitionCounter = 0;
		this.fixedFreq = 0;
		this.blockingQueue = blockingSpectrumDataQueue;
	}
//	public ReceiverDataConverter(BlockingQueue<PacketConverter> blockingSpectrumDataQueue) {
//		this.repetitionCounter = 0;
//		this.fixedFreq = 0;
//		this.blockingQueue = blockingSpectrumDataQueue;
//	}
	
	public void getDataFromQueue() throws InterruptedException {
		PacketConverter receivedPacket;
	
		//receivedPacket = blockingQueue.take();
		
		//receivedPacket = blockingQueue.poll(50, TimeUnit.MILLISECONDS);
		if(blockingQueue!=null)
			receivedPacket = blockingQueue.poll();
		else
			receivedPacket = null;
//		if(receivedPacket!=null) {
//			notify(receivedPacket.getSpectrumData(), receivedPacket.getDataSize(), receivedPacket.getSequenceNumber(), 
//				receivedPacket.getTimeStamp(), receivedPacket.getFreqStart(), receivedPacket.getFreqStep());
//			
//		}
		if(receivedPacket!=null) {
			Platform.runLater(()->notify(receivedPacket.getSpectrumData(), receivedPacket.getDataSize(), receivedPacket.getSequenceNumber(), 
				receivedPacket.getTimeStamp(), receivedPacket.getFreqStart(), receivedPacket.getFreqStep()));
		}
				
	}
	public void startReceiving() {
		
		Runnable receivingTask = () -> {
			boolean End = false;
			while (!End) {
				
				try {
					getDataFromQueue();
				} catch (InterruptedException ex) {
					System.out.println("receivingTask: Interrupted! - finishing task");
					End = true;
				}
			}
        };
        readingThread = new Thread(receivingTask);  
        readingThread.setUncaughtExceptionHandler((t, ex) -> {
        	System.out.println(ex.getMessage()  + " - exiting task");
        	readingThread.interrupt();
		});
        readingThread.start();

		
	}
	public void stopReceiving() {
		readingThread.interrupt();

		try {
			readingThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	/**
	 * Przekazuje dane z odbiornika do zarejestrowanych sluchaczy klasy
	 */
	public synchronized void notifyData(double[] receivedData, int seqNumber, double timeStamp, double freqStart, double freqStep ) { 
		
		int dataSize = receivedData.length;	
		
		notify(receivedData, dataSize, seqNumber, timeStamp,  freqStart, freqStep);
		
	}
	
	/**
     * Generuje dane losowe w trybie demo i powiadamia sluchaczy
     */  
	public void convertData() {
		
		int dataSize = 256;
		int seqNumber = 1;
		//Kamil O zmiana generowania danych aby Timestamp był aktualny (uzywam go do wyszukiwania Id rekordu w DB)
//		double timeStamp = 58972597;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		double timeStamp = timestamp.getTime();	


		double freqStart = 30000000;
		double freqStep = 10500;
		double[] receivedData = new double[dataSize];
		
		for(int i = 0; i<receivedData.length; i++) {
			double rndnumb = 10 * Math.random();
			receivedData[i] = (rndnumb>9.95) ? rndnumb + 90 * Math.random(): rndnumb;	
			receivedData[i] = receivedData[i] -  120;
			
			if(i == 45) {
				if(repetitionCounter<20) {					
					receivedData[i] = 90 + 5 * Math.random();//stały sygnał na tej samej częstotliwosći					
					receivedData[i] = receivedData[i] -  120;
					repetitionCounter++;
				}
				else {
					//losowanie
					if(100 * Math.random()>95) {
						repetitionCounter = 0;
						fixedFreq = (int)Math.abs(Math.round(dataSize*Math.random()));
					}
				}
			}
			if(i == fixedFreq) {
				if(repetitionCounter<20) {					
					receivedData[i] = 90 + 5 * Math.random();//stały sygnał na tej samej częstotliwosći
					receivedData[i] = receivedData[i] -  120;
				}
			
			}
			
			
		}
		 
		
		notify(receivedData, dataSize, seqNumber, timeStamp,  freqStart, freqStep); 
		
		
	}
	
	
	/**
     * Dodaje sluchcza zdarzen odebrania danych.
     *
     * @param listener - dodawany słuchacz
     */

	public void addListener(ReceiverDataConverterListener listener) {
        if (listener != null) {
        	receiverDataConverterListeners.addListener(listener);
        }
    }

    /**
     * Usuwa sluchacza zdarzen odebrania danych.
     *
     * @param listener - usuwany sluchacz
     */
    public void removeListener(ReceiverDataConverterListener listener) {
        if (listener != null) {
        	receiverDataConverterListeners.removeListener(listener);
        }
    }
    
    /**
     * Notyfikuje wszystkich zarejestowanych sluchaczy o nowych odebranych danych.
     *
	 * @param receivedData tablica zawierajaca poziomy sygnałów
	 * @param dataSize ilość częstotliwości
	 * @param seqNumber - numer sekwencyjny ostatniej linni obrazu
	 * @param timeStamp - znacznik czasu ostatniej linni obrazu
	 * @param freqStart - częstotliwosć startowa danych ostatniej linii obrazu
	 * @param freqStep - krok częstotliwosci dla danych ostatniej linii obrazu
     */

    private static void notify(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart, double freqStep) {
        
            if (receivedData != null) {
            	receiverDataConverterListeners.fire().onDataReceived( receivedData, 
            			dataSize, seqNumber, timeStamp, freqStart, freqStep);    
            }
            else {
            	receiverDataConverterListeners.fire().onError("data reception Error - receivedData == null");
            }

    }
}
