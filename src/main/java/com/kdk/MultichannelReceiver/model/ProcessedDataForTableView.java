package com.kdk.MultichannelReceiver.model;

/**
 * Klasa przechowująca jedno pole przetworzonych danych spectrum po progowaniu - do wyświetlenia w tabeli
 * 
 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
 */

public class ProcessedDataForTableView {
	Double frequency; 
	String  frequencyStr;
	Double signalLevel;
	int seqNumbe;
	Double timeStamp;
	String timeStampStr;
	double threshold;

	public String getFrequencyStr() {
		return frequencyStr;
	}
	public void setFrequencyStr(String frequencyStr) {
		this.frequencyStr = frequencyStr;
	}
	public ProcessedDataForTableView(Double frequency, Double signalLevel, int seqNumbe, Double timeStamp, double threshold) {
		super();
		this.frequency = frequency;
		this.frequencyStr = frequency.toString();
		this.signalLevel = signalLevel;
		this.seqNumbe = seqNumbe;
		this.timeStamp = timeStamp;
		this.timeStampStr = timeStamp.toString();
		//setTimeStampStr(Double.toHexString(timeStamp));
		this.threshold = threshold;
	}
	public String getTimeStampStr() {
		return timeStampStr;
	}
	public void setTimeStampStr(String timeStampStr) {
		this.timeStampStr = timeStampStr;
	}
	public ProcessedDataForTableView() {
		super();
	
		
		
	}
	public Double getFrequency() {
		return frequency;
	}
	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}
	public Double getSignalLevel() {
		return signalLevel;
	}
	public void setSignalLevel(Double signalLevel) {
		this.signalLevel = signalLevel;
	}
	public int getSeqNumbe() {
		return seqNumbe;
	}
	public void setSeqNumbe(int seqNumbe) {
		this.seqNumbe = seqNumbe;
	}
	public Double getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Double timeStamp) {
		this.timeStamp = timeStamp;
	}
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
}
	
	
