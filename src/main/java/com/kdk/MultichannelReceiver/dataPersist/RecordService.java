package com.kdk.MultichannelReceiver.dataPersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/***
 * Klasa zajmujaca się przetwarzaniem odebranych danych pomiarowych
 * i zapisem ich do odpowiednich repozytoriów oraz odczytem z repozutoriów
 * @author Kamil Ochnik
 */
@Component
public class RecordService {

    private final RecordEntityRepository recordEntityRepository;
    private final FrequencyTable frequencyTable;

    private final ReceivedRecordEntityRepository receivedRecordEntityRepository;
    private final ThresholdCrossingEntityRepository thresholdCrossingEntityRepository;

    //UWAGA  opcja zapisu do bazy danych (v2) zapis w 1 linii (krotce)
    private final ReceivedRecordOneRawEntityRepository receivedRecordOneRawEntityRepository;
    private final ThresholdCrossingEntityOneRawRepository thresholdCrossingEntityOneRawRepository;

    /***
     * Konstruktor Klasy.
     * @param recordEntityRepository repozytorium zapisanych pomiarów
     * @param receivedRecordEntityRepository repozytorium zapisanych rekordów
     * @param thresholdCrossingEntityRepository repozytorium zapisanych rekordów które przekraczają próg
     * @param frequencyTable tabela częstotliwości
     * @param receivedRecordOneRawEntityRepository repozytorium zapisanych rekordów, zapis w 1 linii (krotce)
     * @param thresholdCrossingEntityOneRawRepository repozytorium zapisanych rekordów które przekraczają próg, zapis w 1 linii (krotce)
     */
    @Autowired
    public RecordService(RecordEntityRepository recordEntityRepository,
                         ReceivedRecordEntityRepository receivedRecordEntityRepository,
                         ThresholdCrossingEntityRepository thresholdCrossingEntityRepository,
                         FrequencyTable frequencyTable,
                         ReceivedRecordOneRawEntityRepository receivedRecordOneRawEntityRepository,
                         ThresholdCrossingEntityOneRawRepository thresholdCrossingEntityOneRawRepository) {
        this.recordEntityRepository = recordEntityRepository;
        this.receivedRecordEntityRepository = receivedRecordEntityRepository;
        this.thresholdCrossingEntityRepository = thresholdCrossingEntityRepository;
        this.frequencyTable = frequencyTable;
        this.receivedRecordOneRawEntityRepository = receivedRecordOneRawEntityRepository;
        this.thresholdCrossingEntityOneRawRepository = thresholdCrossingEntityOneRawRepository;
    }

    /***
     * Metoda zapisujaca do repozytorium:pojedynczy pomiar,
     * rekord zawierajacy wszystkie odebrane czestotliwosci i poziomy sygnalu,
     * rekord zawierajacy tylko czestotliwości których poziom sygnału przekraczaja próg.
     * @param receivedData tablica zawierajaca poziomy sygnałów
     * @param dataSize ilość częstotliwości
     * @param seqNumber numer sekwencji
     * @param timeStamp znacznik czasu
     * @param freqStart początkowa częstotliwość
     * @param freqStep krok miedzy czestotliwosciami
     * @param threshold próg detekcji
     * @return metoda zwraca obiekt zawierający dwie tablice
     * (tablicę czestotliwosci i analogicznie tablicę poziomów sygnału)
     * w ktorych znajdują się tylko cześstotliwośći które przekroczyły próg,
     * tablicę czestotliwosci i analogicznie tablicę poziomów sygnału.
     */
    public ThresholdsTables addRecord(double[] receivedData, int dataSize, int seqNumber, double timeStamp,
                                      double freqStart, double freqStep, double threshold) {

        frequencyTable.generateFrequencyTable(dataSize, freqStart, freqStep);

        long idFromDB = saveRecordEntityInDb(timeStamp, seqNumber, threshold);

        saveReceivedRecordOneRowEntityInDb(dataSize, frequencyTable, receivedData, idFromDB);

        return saveThresholdCrossingOneRawEntityInDb(dataSize, frequencyTable, receivedData, idFromDB, threshold);
    }

    /***
     * Metoda zapisujaca rekord zawierajacy tylko czestotliwości których poziom sygnału przekraczaja próg.
     * @param dataSize ilość częstotliwości
     * @param frequencyTable tabela częstotliwości
     * @param receivedData tablica zawierajaca poziomy sygnałów
     * @param idFromDB id zapisanego recordu w bazie danych
     * @param threshold próg detekcji
     * @return metoda zwraca obiekt zawierający dwie tablice
     * (tablicę czestotliwosci i analogicznie tablicę poziomów sygnału)
     * w ktorych znajdują się tylko cześstotliwośći które przekroczyły próg,
     * tablicę czestotliwosci i analogicznie tablicę poziomów sygnału.
     */
    private ThresholdsTables saveThresholdCrossingOneRawEntityInDb(int dataSize, FrequencyTable frequencyTable, double[] receivedData, long idFromDB, double threshold) {
        String frequencyThreshold = "";
        String signalThreshold = "";

        for (int i = 0; i < dataSize; i++) {

            if (receivedData[i] > threshold) {
                frequencyThreshold += frequencyTable.getFrequency(i) + " ";
                signalThreshold += receivedData[i] + " ";
            }
        }

        if (frequencyThreshold.length() > 1) {
            frequencyThreshold = frequencyThreshold.substring(0, frequencyThreshold.length() - 1);
            signalThreshold = signalThreshold.substring(0, signalThreshold.length() - 1);
        }

        // UWAGA v2 zmiana zapisu danych w DB wersja z 1 linijka dla calego recordu
        if (frequencyThreshold.length() > 1) {
            ThresholdCrossingOneRawEntity thresholdCrossingOneRawEntity = ThresholdCrossingOneRawEntity
                    .builder()
                    .frequencyList(frequencyThreshold)
                    .signalLevelList(signalThreshold)
                    .recordId(idFromDB)
                    .build();

            thresholdCrossingEntityOneRawRepository.save(thresholdCrossingOneRawEntity);
        }

        String[] czestotliwosci = frequencyThreshold.split(" ");
        String[] sygnaly = signalThreshold.split(" ");

        double[] frequencyThresholdDoubleTable = new double[czestotliwosci.length];
        double[] signalThresholdDoubleTable = new double[sygnaly.length];

        if (frequencyThresholdDoubleTable.length > 1) {
            for (int i = 0; i < czestotliwosci.length; i++) {
                frequencyThresholdDoubleTable[i] = Double.parseDouble(czestotliwosci[i]);
                signalThresholdDoubleTable[i] = Double.parseDouble(sygnaly[i]);
            }
            return new ThresholdsTables(frequencyThresholdDoubleTable, signalThresholdDoubleTable);
        } else return new ThresholdsTables(new double[0], new double[0]);
    }

    /***
     * Metoda zapisujaca rekord zawierajacy wszystkie odebrane czestotliwosci i poziomy sygnalu.
     * @param dataSize ilość częstotliwości
     * @param frequencyTable tabela częstotliwości
     * @param receivedData tablica zawierajaca poziomy sygnałów
     * @param idFromDB id zapisanego rekordu w bazie danych
     */
    private void saveReceivedRecordOneRowEntityInDb(int dataSize, FrequencyTable frequencyTable, double[] receivedData, long idFromDB) {
        String allFrequency = "";
        String allSignal = "";

        for (int i = 0; i < dataSize; i++) {
            allFrequency += frequencyTable.getFrequency(i) + " ";
            allSignal += receivedData[i] + " ";
        }

        allFrequency = allFrequency.substring(0, allFrequency.length() - 1);
        allSignal = allSignal.substring(0, allSignal.length() - 1);


        // UWAGA v2 zmiana zapisu danych w DB wersja z 1 linijka dla calego recordu
        ReceivedRecordOneRowEntity receivedRecordOneRowEntity = ReceivedRecordOneRowEntity
                .builder()
                .frequencyList(allFrequency)
                .signalLevelList(allSignal)
                .recordId(idFromDB)
                .build();

        receivedRecordOneRawEntityRepository.save(receivedRecordOneRowEntity);

    }

    /***
     * Metoda zapisujaca do repozytorium:pojedynczy pomiar.
     * @param timeStamp znacznik czasu
     * @param seqNumber numer sekwencji
     * @param threshold próg detekcji
     * @return zwraca id zapisanego recordu w bazie danych.
     */
    private long saveRecordEntityInDb(double timeStamp, int seqNumber, double threshold) {

        RecordEntity newRecord = RecordEntity.builder()
                .timeStamp(timeStamp)
                .seqNumber(seqNumber)
                .threshold(threshold)
                .build();

        recordEntityRepository.save(newRecord);

        RecordEntity recordEntityFromDb = recordEntityRepository
                .findByTimeStamp(timeStamp)
                .orElseThrow(IllegalArgumentException::new);

        return recordEntityFromDb.getId();
    }

    /***
     * Metoda pobiera dane z repozytorium pomiarów i je zwraca w postaci listy obiektów RecordEntity.
     * @return zwraca listę wszystkich pomiarów.
     */
    public List<RecordEntity> getAllRecords() {
        return recordEntityRepository.findAll()
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());
    }

    /***
     * metoda kasuje wszystkie pomiary ktore znajdują się w lokalnej bazie danych.
     */
    public void deleteAllRecords() {
        List<RecordEntity> allRecords = recordEntityRepository.findAll()
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());

        for (RecordEntity re : allRecords
        ) {
            recordEntityRepository.delete(re);
        }
    }

    /***
     * Metoda pobiera dane z repozytorium pomiarów,
     * filtruje je po parametrze seqNumber.
     * Zwraca w postaci listy obiektów RecordEntity.
     * @param seqNumber numer sekwencji
     * @return zwraca listę obiektów zawierajacych dany parametr: numer sekwencji.
     */
    public List<RecordEntity> getRecordsBySeqNumber(int seqNumber) {
        return recordEntityRepository.findBySeqNumber(seqNumber)
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());
    }

    /***
     * Metoda kasuje z repozytorium wszystkie pomiary o podanym parametrze: numer sekwencji.
     * @param seqNumber numer sekwencji
     */
    public void deleteRecordsBySeqNumber(int seqNumber) {
        List<RecordEntity> allRecordsBySeqNumber = recordEntityRepository.findBySeqNumber(seqNumber)
                .stream()
                .map(it -> RecordEntity.builder()
                        .id(it.getId())
                        .timeStamp(it.getTimeStamp())
                        .seqNumber(it.getSeqNumber())
                        .threshold(it.getThreshold())
                        .build())
                .collect(Collectors.toList());

        for (RecordEntity re : allRecordsBySeqNumber
        ) {
            recordEntityRepository.delete(re);
        }
    }

    /***
     * Metoda wyszukuje w repozytorium pomiaru o podanym parametrze: znacznik czasu.
     * @param timeStamp znacznik czasu
     * @return zwraca obiekt o podanym parametrze: znacznik czasu.
     */
    public RecordEntity findRecordByTimeStamp(double timeStamp) {

        RecordEntity record = recordEntityRepository.findByTimeStamp(timeStamp).orElseThrow(IllegalArgumentException::new);
        return RecordEntity.builder().id(record.getId())
                .timeStamp(record.getTimeStamp())
                .seqNumber(record.getSeqNumber())
                .threshold(record.getThreshold())
                .build();
    }

    /***
     * Metoda kasuje z repozytorium pomiar o podanym parametrze: znacznik czasu.
     * @param timeStamp znacznik czasu
     */
    public void deleteRecordByTimeStamp(double timeStamp) {
        RecordEntity record = recordEntityRepository.findByTimeStamp(timeStamp).orElseThrow(IllegalArgumentException::new);
        recordEntityRepository.delete(RecordEntity.builder().id(record.getId())
                .timeStamp(record.getTimeStamp())
                .seqNumber(record.getSeqNumber())
                .threshold(record.getThreshold())
                .build());
    }

    /***
     * Metoda wyszukuje w repozytorium zapisanych pomiarów pomiarów o podanym parametrze: indentyfikator pomiaru.
     * @param recordId indentyfikator pomiaru
     * @return zwraca listę obiektów o podanym parametrze: indentyfikator pomiaru.
     * @deprecated ze względu na niską wydajność zastąpiona prez metodę @see getReceivedRecordOneRawByRecordId
     */
    public List<ReceivedRecordEntity> getReceivedRecordByRecordId(long recordId) {
        return receivedRecordEntityRepository.findByRecordId(recordId)
                .stream()
                .map(it -> ReceivedRecordEntity.builder()
                        .id(it.getId())
                        .frequency(it.getFrequency())
                        .signalLevel(it.getSignalLevel())
                        .recordId(it.getRecordId())
                        .build())
                .collect(Collectors.toList());
    }

    /***
     * Metoda wyszukuje w repozytorium zapisanych rekordów, zapis w 1 linii (krotce) pomiaru o podanym parametrze: indentyfikator pomiaru.
     * @param recordId indentyfikator pomiaru
     * @return zwraca obiekt o podanym parametrze: indentyfikator pomiaru.
     */
    public ReceivedRecordOneRowEntity getReceivedRecordOneRawByRecordId(long recordId) {
        ReceivedRecordOneRowEntity entity = receivedRecordOneRawEntityRepository.findByRecordId(recordId);
        return ReceivedRecordOneRowEntity
                .builder()
                .id(entity.getId())
                .frequencyList(entity.getFrequencyList())
                .signalLevelList(entity.getSignalLevelList())
                .recordId(entity.getRecordId())
                .build();
    }

    /***
     * Metoda wyszukuje w repozytorium zapisanych rekordów które przekraczają próg pomiarów o podanym parametrze: indentyfikator pomiaru.
     * @param recordId indentyfikator pomiaru
     * @return zwraca listę obiektów o podanym parametrze: indentyfikator pomiaru.
     * @deprecated ze względu na niską wydajność zastąpiona prez metodę @see getThresholdCrossingRecordOneRawByRecordId
     */
    public List<ThresholdCrossingEntity> getThresholdCrossingRecordByRecordId(long recordId) {
        return thresholdCrossingEntityRepository.findByRecordId(recordId)
                .stream()
                .map(it -> ThresholdCrossingEntity.builder()
                        .id(it.getId())
                        .frequency(it.getFrequency())
                        .signalLevel(it.getSignalLevel())
                        .recordId(it.getRecordId())
                        .build())
                .collect(Collectors.toList());
    }

    /***
     * Metoda wyszukuje w repozytorium zapisanych rekordów które przekraczają próg, zapis w 1 linii (krotce)
     * pomiaru o podanym parametrze: indentyfikator pomiaru
     * @param recordId indentyfikator pomiaru
     * @return zwraca obiekt o podanym parametrze: indentyfikator pomiaru.
     */
    public ThresholdCrossingOneRawEntity getThresholdCrossingRecordOneRawByRecordId(long recordId) {
        ThresholdCrossingOneRawEntity entity = thresholdCrossingEntityOneRawRepository.findByRecordId(recordId);
        return ThresholdCrossingOneRawEntity.builder()
                .id(entity.getId())
                .frequencyList(entity.getFrequencyList())
                .signalLevelList(entity.getSignalLevelList())
                .recordId(entity.getRecordId())
                .build();
    }
}
