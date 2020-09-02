package com.kdk.MultichannelReceiver.model;

import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import com.kdk.MultichannelReceiver.dataPersist.ThresholdsTables;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpectrumDataProcessor implements ReceiverDataConverterListener {

	private RecordService recordService;
	double threshold;// próg decyzyjny powyżej którego wszystkie przekroczenia poziomu widma
						// traktujemy jako sygnały uzyteczne
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

		ThresholdsTables actualThresholdList = recordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);

		// przekazanie danych do klasy wyświetlającej
		spectrumDataProcessorListener.fire().onDataProcessed(actualThresholdList.getFrequency(), actualThresholdList.getSignal(), seqNumber, timeStamp, threshold);
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
