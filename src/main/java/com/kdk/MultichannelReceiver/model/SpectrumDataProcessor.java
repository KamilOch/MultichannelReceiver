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
	// doda� wymagane zmienne na kolekcje danych
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
		// TODO Auto-generated method stub
		// tutaj odbieramy i przetwarzamy dane widma


		// tutaj odbieramy i przetwarzamy dane widma : double receivedData , double
		// frequency, double signalLevel, int seqNumber, double timeStamp, double
		// threshold
		// dorobui� zapis wynik�w do bazy danych
		List<ThresholdCrossingEntity> actualThresholdList = recordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);

		double[] frequency = new double[actualThresholdList.size()];
		double[] signalLevel = new double[actualThresholdList.size()];

		for (int i = 0; i < actualThresholdList.size(); i++) {
			frequency[i] = actualThresholdList.get(i).getFrequency();
			//System.out.println("Czestotlowosc= " + list.get(i).getFrequency());
			signalLevel[i] = actualThresholdList.get(i).getSignalLevel();
			//System.out.println("Poziom sygnału= " + list.get(i).getSignalLevel());
		}
		// TODO wyslać tylko przefiltrowane dane!!!
		// przekazanie danych do klasy wyświetlającej
		// wynik poprosz�e zwr�ci� w zdarzeniu zwrotnym do klasy kontrolera (podobnie
		// jak w SpectrumWateerfall)
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
