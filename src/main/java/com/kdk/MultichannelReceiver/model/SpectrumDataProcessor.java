package com.kdk.MultichannelReceiver.model;

import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpectrumDataProcessor implements ReceiverDataConverterListener {

    private RecordService recordService;
    double threshold;//pr�g decyzyjny powy�ej kt�rego wszystkie przekroczenia poziomu widma traktujemy jako sygna�y uzyteczne
    //doda� wymagane zmienne na kolekcje danych

    @Autowired
    public SpectrumDataProcessor() {
        super();
        // TODO Auto-generated constructor stub
    }

    //metoda do przetwarzania widma i znajdowania sygna��w powy�ej progu decyzyjnego a potem znajdowanie pik�w (wykorzysta� r�niczk�) i dla tych warto�ci maksymalnych okre�li�� cz�stotliwo�ci
    public void proceesSpectrum(double[] receivedData) {

        double[] spectrumDataGreaterThanThreshold = new double[receivedData.length];

        for (double signal : receivedData) {
            int i = 0;
            if (signal > threshold) {
                spectrumDataGreaterThanThreshold[i] = signal;
                i++;
            }
        }
        //TODO zapisac dane do bazy, moze zrobic jakis obiekt z polami (cos jak tabela w bazie SQL )

    }

    @Override
    public void onError(String error) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
                               double freqStep) {
        // TODO Auto-generated method stub
        //tutaj odbieramy i przetwarzamy dane widma
        proceesSpectrum(receivedData);


        //wynik poprosz�e zwr�ci� w zdarzeniu zwrotnym do klasy kontrolera (podobnie jak w SpectrumWateerfall)


        //dorobui� zapis wynik�w do bazy danych
        recordService.addRecord(receivedData, dataSize, seqNumber, timeStamp, freqStart, freqStep, threshold);

    }

    public void setRecordService(RecordService recordService) {
        this.recordService= recordService;
    }
}
