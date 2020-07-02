package com.kdk.MultichannelReceiver.model;

import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import com.kdk.MultichannelReceiver.dataPersist.ThresholdCrossingEntity;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpectrumDataProcessor implements ReceiverDataConverterListener {

	private RecordService recordService;
	double threshold;// pr�g decyzyjny powy�ej kt�rego wszystkie przekroczenia poziomu widma
						// traktujemy jako sygna�y uzyteczne
	private static final EventListenerSupport<SpectrumDataProcessorListener> spectrumDataProcessorListener = new EventListenerSupport<>(
			SpectrumDataProcessorListener.class);

	@Autowired
	public SpectrumDataProcessor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {

		// serwis analizujacy i zapisujacy dane do bazy
		List<ThresholdCrossingEntity> actualThresholdList = recordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);

		double[] frequency = new double[actualThresholdList.size()];
		double[] signalLevel = new double[actualThresholdList.size()];

		for (int i = 0; i < actualThresholdList.size(); i++) {
			frequency[i] = actualThresholdList.get(i).getFrequency();
			signalLevel[i] = actualThresholdList.get(i).getSignalLevel();
		}
		
		// przekazanie danych do klasy wyświetlającej		
		spectrumDataProcessorListener.fire().onDataProcessed(frequency, signalLevel, seqNumber, timeStamp, threshold);

	}

	public void setRecordService(RecordService recordService) {
		this.recordService = recordService;
	}

	/**
	 * Dodaje słuchcza zdarzeń odebrania danych.
	 *
	 * @param listener - dodawany słuchacz
	 */
	public void addListener(SpectrumDataProcessorListener listener) {
		if (listener != null) {
			spectrumDataProcessorListener.addListener(listener);
		}
	}

	/**
	 * Usuwa słuchacza zdarzeń odebrania danych.
	 *
	 * @param listener - usuwany słychacz
	 */
	public void removeListener(SpectrumDataProcessorListener listener) {
		if (listener != null) {
			spectrumDataProcessorListener.removeListener(listener);
		}
	}
}
