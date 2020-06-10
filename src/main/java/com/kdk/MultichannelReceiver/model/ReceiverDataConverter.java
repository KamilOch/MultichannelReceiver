package com.kdk.MultichannelReceiver.model;

import org.apache.commons.lang3.event.EventListenerSupport;

import java.sql.Timestamp;

/**
 * Klasa managera umo�liwiajaca pod�aczenie klas jako s�uchaczy zdarzen
 * 
 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
 */

public class ReceiverDataConverter {
	
	private static final EventListenerSupport<ReceiverDataConverterListener> receiverDataConverterListeners = new EventListenerSupport<>(ReceiverDataConverterListener.class);

	/**
     * Konstruktor
     */  
	public void ReceiverDataConverter() {
		
	}
	/**
     * Generuje dane i powiadamia s�uchaczy
     */  
	public void convertData() {
		
		int dataSize = 128;
		int seqNumber = 1;
		//Kamil O zmiana generowania danych aby Timestamp był aktualny (uzywam go do wyszukiwania Id rekordu w DB)
//		double timeStamp = 58972597;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		double timeStamp = timestamp.getTime();

		double freqStart = 30000000;
		double freqStep = 10500;
		double[] receivedData = new double[dataSize];
		
		for(int i = 0; i<receivedData.length; i++) {
			receivedData[i] = 100 * Math.random();		
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
     * @param ctx Kontekst wiadomo�ci JMS
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
