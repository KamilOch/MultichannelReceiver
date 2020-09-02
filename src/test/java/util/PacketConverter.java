package util;



/* Struktura pakietu danych z odbiornika
 * Numer pola	Szeroko�� 		Warto��			Znaczenie
 *	1.			32bit UINT		0xAAAA5667		Magic Word � znacznik pocz�tku
 *	2.			32bit UINT		0 � 0xFFFFFFFF	Numer sekwencyjny
 *  3.			64bit DOUBLE	0 � 0xFFFFFFFFFFFFFFFF	timeStamp [ms]
 *--4.			32bit UINT		0 � 0xFFFFFFFF	Numer bloku danych - opcjonalnie na razie nie zaimplementowany
 *--5.			32bit INT		-100  � 15	Reference Level [dBm] - opcjonalnie na razie nie zaimplementowany
 *	6.			32bit UINT		1 � 65536		D�ugo�� danych N x 32bit
 * 	7.			64 bit DOUBLE	250 000 � 3 600 000 000	F0 [Hz] � cz�stotliwi�� pocz�tkowa
 *	8.		 	64 bit DOUBLE	1 � 1 000 000	Df [Hz]� krok cz�stotliwo�ci pomi�dzy kolejnymi pr�bkami
 *	9.			64 bit DOUBLE[]	-160 � 160		Dane FFT N x 64 bit
 *  4+4+8+4+8+8+N*8
 *  Maksymalny rozmiar pakietu UDP to 8192 bajty
 *  @author Kamil Wilgucki k.wilgucki@wil.waw.pl
 */

public class PacketConverter {
	int magicWord = 0xAAAA5667;
	int sequenceNumber = 0;
	double timeStamp = 0;
	//int blockNumber = 0;	
	//int refLevel = 0;
	int dataSize = 0;
	double freqStart = 0;
	double freqStep = 0;
	double[] spectrumData;
	
	
	public int getMagicWord() {
		return magicWord;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public double getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(double timeStamp) {
		this.timeStamp = timeStamp;
	}
	public int getDataSize() {
		return dataSize;
	}
	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}
	public double getFreqStart() {
		return freqStart;
	}
	public void setFreqStart(double freqStart) {
		this.freqStart = freqStart;
	}
	public double getFreqStep() {
		return freqStep;
	}
	public void setFreqStep(double freqStep) {
		this.freqStep = freqStep;
	}
	public double[] getSpectrumData() {
		return spectrumData;
	}
	public void setSpectrumData(double[] spectrumData) {
		this.spectrumData = spectrumData;
	}
	public boolean convertData(byte[] inputData) {
		boolean result = false;
		
		return result;
	}
	

}
