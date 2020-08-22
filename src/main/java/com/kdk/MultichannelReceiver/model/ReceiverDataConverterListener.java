/**
 * 
 */
package com.kdk.MultichannelReceiver.model;


/**
 * Interfejs dla sluchaczy klasy ReceiverDataConverter. 
 * Zdrzenie onError - do przekazywania bledow typu String.
 * Zdarzenie onDataReceived - do przekazywania odebranych danych widma z odbiornika.   
 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
 */

public interface ReceiverDataConverterListener {
	
	public void onError(final String error);
	public void onDataReceived(final double[] receivedData, final int dataSize, final int seqNumber, final double timeStamp, final double freqStart, final double freqStep);

}