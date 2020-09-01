/**
 * 
 */
package com.kdk.MultichannelReceiver.model;

/**
 * Interfejs do przekazywania danych widma z odbiornika pomiędzy poszczególnymi klasami
 * @author Kamil Wilgucki / k.wilgucki@wil.waw.pl
 *
 */
public interface ReceiverDataInterface {
	
	double[] getSpectrumData();
	int getSpectrumDataSize();
	double getFrequencyStart();
	double getFrequencyStep();

}
