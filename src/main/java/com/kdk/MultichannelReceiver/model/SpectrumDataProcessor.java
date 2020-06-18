package com.kdk.MultichannelReceiver.model;

import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import com.kdk.MultichannelReceiver.dataPersist.ThresholdCrossingEntity;

import java.util.List;

import org.apache.commons.lang3.event.EventListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	// metoda do przetwarzania widma i znajdowania sygna��w powy�ej progu
	// decyzyjnego a potem znajdowanie pik�w (wykorzysta� r�niczk�) i dla tych
	// warto�ci maksymalnych okre�li�� cz�stotliwo�ci
//    public void proceesSpectrum(double[] receivedData) {
//
//        double[] spectrumDataGreaterThanThreshold = new double[receivedData.length];
//
//        for (double signal : receivedData) {
//            int i = 0;
//            if (signal > threshold) {
//                spectrumDataGreaterThanThreshold[i] = signal;
//                i++;
//            }
//        }
//        //TODO zapisac dane do bazy, moze zrobic jakis obiekt z polami (cos jak tabela w bazie SQL )
//
//    }

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		// TODO Auto-generated method stub
		// tutaj odbieramy i przetwarzamy dane widma
//        proceesSpectrum(receivedData);

		// tutaj odbieramy i przetwarzamy dane widma : double receivedData , double
		// frequency, double signalLevel, int seqNumber, double timeStamp, double
		// threshold
		// dorobui� zapis wynik�w do bazy danych
		recordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);

		// wynik poprosz�e zwr�ci� w zdarzeniu zwrotnym do klasy kontrolera (podobnie
		// jak w SpectrumWateerfall)
		// TODO wyslać tylko przefiltrowane dane!!!
		// przekazanie danych do klasy wyświetlającej

		List<ThresholdCrossingEntity> list = recordService.thresholdFrequencyList();
		double[] frequency = new double[list.size()];
		double[] signalLevel = new double[list.size()];

		for (int i = 0; i < list.size(); i++) {
			frequency[i] = list.get(i).getFrequency();
			System.out.println("Czestotlowosc= " + list.get(i).getFrequency());
			signalLevel[i] = list.get(i).getSignalLevel();
			System.out.println("Poziom sygnału= " + list.get(i).getSignalLevel());
		}

		spectrumDataProcessorListener.fire().onDataProcess(frequency, signalLevel, seqNumber, timeStamp, threshold);

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
