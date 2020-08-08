package com.kdk.MultichannelReceiver.model;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.kdk.MultichannelReceiver.model.utils.PacketConverter;

import javafx.application.Platform;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Klasa managera umo�liwiajaca pod�aczenie klas jako s�uchaczy zdarzen
 * 
 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
 */

public class ReceiverDataConverter {
	
	private static final EventListenerSupport<ReceiverDataConverterListener> receiverDataConverterListeners = new EventListenerSupport<>(ReceiverDataConverterListener.class);
	int repetitionCounter;
	int fixedFreq;
	//BlockingQueue<PacketConverter> blockingQueue;
	ConcurrentLinkedQueue<PacketConverter> blockingQueue;
	private static Thread readingThread; 
	

	/**
     * Konstruktor
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
		receivedPacket = blockingQueue.poll();
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
	 * Przekazuje dane z odbiornika do słuchaczy
	 */
	public synchronized void notifyData(double[] receivedData, int seqNumber, double timeStamp, double freqStart, double freqStep ) { 
		
		int dataSize = receivedData.length;	
		
		notify(receivedData, dataSize, seqNumber, timeStamp,  freqStart, freqStep);
		
	}
	
	/**
     * Generuje dane i powiadamia s�uchaczy
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
     * Dodaje s�uchcza zdarze� odebrania danych.
     *
     * @param listener - dodawany s�uchacz
     */
	public void addListener(ReceiverDataConverterListener listener) {
        if (listener != null) {
        	receiverDataConverterListeners.addListener(listener);
        }
    }

    /**
     * Usuwa s�uchacza zdarze� odebrania danych.
     *
     * @param listener - usuwany s�ychacz
     */
    public void removeListener(ReceiverDataConverterListener listener) {
        if (listener != null) {
        	receiverDataConverterListeners.removeListener(listener);
        }
    }
    
    /**
     * Przetwarza dane odebrane z SOCKETA.
     *
     * @param ctx Kontekst wiadomo�ci 
     * @param msg Odebrana wiadomo��
     */
//    private static void onNotify(MsgContext ctx, Event msg) {
//
//        EventSocketStatus SocketMsg = (EventSocketStatus)msg;
//        if (null != SocketMsg ) {
//
//        	EventSocketStatus e = (EventSocketStatus) msg;
//            H117gServerCommand sc = H117gServerCommand.valueOf(e.getH117gServerCommand());
//            if (null != sc) {
//                switch (sc) {
//                    case DATA:                        
//                        if (!connectionWithServer.get()) {
//                        	receiverDataConverterListeners.fire().onDataReceived(final double[] receivedData, final int dataSize, int seqNumber, double timeStamp, double freqStart, double freqStep);
//                        }
//                        break;
//                    case ERROR:
//                    	receiverDataConverterListeners.fire().onError(receiverError.valueOf(e.getErrorCode()).getLabel());
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }  else {
//            LOGGER.warn("Unexpected notification:\n{}", msg);
//        }
//
//    }
    private static void notify(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart, double freqStep) {
        
            if (receivedData != null) {
            	receiverDataConverterListeners.fire().onDataReceived( receivedData, 
            			dataSize, seqNumber, timeStamp, freqStart, freqStep);         
                
            }
            else {
            	receiverDataConverterListeners.fire().onError("data reception Error");
            }

    }
}
