package model;

public class SpectrumDataProcessor implements ReceiverDataConverterListener{

	double threshold;//próg decyzyjny powy¿ej którego wszystkie przekroczenia poziomu widma traktujemy jako sygna³y uzyteczne
	//dodaæ wymagane zmienne na kolekcje danych
	
	public SpectrumDataProcessor() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	//metoda do przetwarzania widma i znajdowania sygna³ów powy¿ej progu decyzyjnego a potem znajdowanie pików (wykorzystaæ ró¿niczkê) i dla tych wartoœci maksymalnych okreœliææ czêstotliwoœci
	public void proceesSpectrum(double[] receivedData) {

		double[] spectrumDataGreaterThanThreshold = new double[receivedData.length];

		for( double signal:receivedData){
			int i = 0;
			if (signal>threshold){
				spectrumDataGreaterThanThreshold[i]=signal;
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
		
		
		
		//wynik poproszêe zwróciæ w zdarzeniu zwrotnym do klasy kontrolera (podobnie jak w SpectrumWateerfall)
		
		
		//dorobuiæ zapis wyników do bazy danych
		
		
		
		
	}

}
