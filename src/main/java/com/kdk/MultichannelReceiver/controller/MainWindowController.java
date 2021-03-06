package com.kdk.MultichannelReceiver.controller;

import com.kdk.MultichannelReceiver.Main;
import com.kdk.MultichannelReceiver.controllerCharts.PRK_4ZoomableLineChart_2c_clean;
import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import com.kdk.MultichannelReceiver.model.*;
import com.kdk.MultichannelReceiver.model.utils.PacketConverter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Klasa kontrolera aplikacji odbierajacej dane z odbiornika 
 * @author Kamil Wilgucki k.wilgucki@wil.waw.pl, 
 * @author Kamil Ochnik,
 * @author Damian Garstka
 *
 */
@Component
public class MainWindowController implements ReceiverDataConverterListener, SpectrumWaterfallListener, SpectrumDataProcessorListener{
	private Main main;
	private Stage primaryStage;

	@FXML private Button loadFileBtn;
	@FXML private Button saveFileBtn;
	@FXML private Button receiveBtn;
	@FXML private Button databaseBtn;
	@FXML private Button demoChartBtn;	
	@FXML private Button closeStageBtn;	
	
	@FXML public TextField tresholdField;
	@FXML private TextField fMarkerField;
	@FXML private TextField fStartField;
	@FXML private TextField fStopField;
	@FXML private TextField fStepField;
	@FXML private TextField seqNumberField;
	@FXML private TextField timeStampField;
	@FXML private TextField tStatusField;
	@FXML private ImageView imageView;
	@FXML private BorderPane rightPane;
	@FXML private LineChart lineChart;
	
	//&&:
	@FXML private VBox vboxCharts;

	
	@FXML private TableView <ProcessedDataForTableView> tableView;
	@FXML private TableColumn <ProcessedDataForTableView, Double> timeStampColumn;
	@FXML private TableColumn <ProcessedDataForTableView, Double> signalsNumberColumn;
	@FXML private TableColumn <ProcessedDataForTableView, Double> freqColumn;

	ConcurrentLinkedQueue<PacketConverter> blockingSpectrumDataQueue = new ConcurrentLinkedQueue<>();
	//BlockingQueue<PacketConverter> blockingSpectrumDataQueue = new LinkedBlockingQueue<>();//kolejka FIFO blokująca - zapewnia dostęp do odebranych danych dla wielu wątków
	ReceiverDataConverter dataConverter = new ReceiverDataConverter(blockingSpectrumDataQueue);
	SpectrumWaterfall spectrumWaterfall = new SpectrumWaterfall(256) ;
	SpectrumDataProcessor spectrumProcessor = new SpectrumDataProcessor();
	
	PacketConverter spectrumDataPacket = new PacketConverter();//wspólna struktura z odebranymi danymi
	
	
	//wątek odbiorczy
	ReceiverUDPClient udpClientThread = null;
	boolean bReception = false;

	
	
	Thread tSimulator;
	boolean bSimulation = false;
	
	private ObservableList<ProcessedDataForTableView> processedDataList = FXCollections.observableArrayList();

	
	
	public void setMain(Main main, Stage primaryStage) {
		this.main = main;
		this.primaryStage=primaryStage;

		//Damian dodal linie:
		PRK_4ZoomableLineChart_2c_clean charts = new PRK_4ZoomableLineChart_2c_clean(dataConverter, vboxCharts,
				spectrumProcessor);
		

		
		//vboxCharts = charts.chartsVbox;

		//dodajemy s�uchaczy odbieraj�cych dane 
		dataConverter.addListener(this);		
		dataConverter.addListener(spectrumWaterfall);
		dataConverter.addListener(spectrumProcessor);
		spectrumWaterfall.addListener(this);
		spectrumProcessor.addListener(this);
	
//		spectrumProcessor.setThreshold(Double.parseDouble(tresholdField.getText())); //commented by Damian
		processedDataList.add(new ProcessedDataForTableView());
		tableView.setItems(processedDataList);
		
		
		timeStampColumn.setCellValueFactory(new PropertyValueFactory<ProcessedDataForTableView, Double>("timeStamp"));
		signalsNumberColumn.setCellValueFactory(new PropertyValueFactory<ProcessedDataForTableView, Double>("signalLevel"));
		freqColumn.setCellValueFactory(new PropertyValueFactory<ProcessedDataForTableView, Double>("frequency"));


		

		lineChart.setAnimated(false); // disable animations
		lineChart.setCreateSymbols(false);
		lineChart.getYAxis().setAnimated(false);
		lineChart.getXAxis().setAnimated(false);
		//lineChart.getYAxis().setMaxHeight(0);
		//lineChart.getYAxis().setMinHeight(-100);
		lineChart.getYAxis().setAutoRanging(true);
	
		imageView.setFitWidth(lineChart.getWidth());
		
		this.primaryStage.setOnCloseRequest(mouseEvent->System.exit(0));
		
/*	//commented by Damian:
		tresholdField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
	        if (newPropertyValue) {

	        } else {
	            if (tresholdField.getText().isEmpty() || tresholdField.getText() == null
	                    || Integer.parseInt(tresholdField.getText()) > 100) {
	            	tresholdField.setText("100");
	            }
	            System.out.println("tresholdField 1 out focus " + tresholdField.getText());
	            spectrumProcessor.setThreshold(Double.parseDouble(tresholdField.getText()));
	        }

	    });
		
		tresholdField.setOnKeyPressed(event->{
			KeyCode keyCode = event.getCode();
			if(keyCode==KeyCode.ENTER) {
				System.out.println("tresholdField changed" + tresholdField.getText());
				spectrumProcessor.setThreshold(Double.parseDouble(tresholdField.getText()));
			}
		});
*/		
		
		
		
	}

	public void setRecordService(RecordService recordService) {
		spectrumProcessor.setRecordService(recordService);
	}


	@FXML
	public void closeStageBtnHandler(){
		
		tStatusField.setText("Zamykanie aplikacji");
		
		if (bSimulation) {
			tSimulator.interrupt();
			try {
				tSimulator.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bSimulation = false;
		}
		
		dataConverter.removeListener(this);
		dataConverter.removeListener(spectrumWaterfall);
		spectrumWaterfall.removeListener(this);
		spectrumProcessor.removeListener(this);
		
		
		primaryStage.close();
		System.exit(0);
	}
	
	private void setTable() {
		;
	}

	public void initialize() {
		;
	}
	/**
	 * Wyświetla DialogWindow O programie
	 */
	@FXML
	public void aboutMenuItemHandler(){
		final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(5);
        dialogVbox.setAlignment(Pos.BOTTOM_CENTER);
        dialogVbox.getChildren().add(new Text("O programie"));
        dialogVbox.getChildren().add(new Separator());
        dialogVbox.getChildren().add(new Separator());
        dialogVbox.getChildren().add(new Text("Aplikacja klienta odbiornika radiowego."));
        dialogVbox.getChildren().add(new Separator());
        dialogVbox.getChildren().add(new Text("Autorzy: Damian Garsta, Kamil Ochnik, Kamil Wilgucki"));
        dialogVbox.getChildren().add(new Separator());
        dialogVbox.getChildren().add(new Separator());
       
        
        Button closeDialogBtn = new Button("Zamknij");
        closeDialogBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	dialog.close();
            }
        });
        dialogVbox.getChildren().add(closeDialogBtn);
        Scene dialogScene = new Scene(dialogVbox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.show();
		
	}

	/**
	 * Uruchamia odbiór danych z odbiornika
	 */
	@FXML
	public void connectMenuItemHandler(){
		
		if(bReception) {//nic nie robi			
			;
		}
		else {//uruchomienie odbioru pakietów UDP
			try {
				//udpClientThread = new ReceiverUDPClient("192.168.11.2", 4445, spectrumDataPacket, dataConverter, blockingSpectrumDataQueue);
				udpClientThread = new ReceiverUDPClient("localhost", 4445, spectrumDataPacket, dataConverter, blockingSpectrumDataQueue);
				udpClientThread.start();
				System.out.println("UDPClient started");
				receiveBtn.setText("Zatrzymaj");
				bReception = true;
				tStatusField.setText("Podłączenie do odbiornika");
				
				//dataConverter.startReceiving();
				
			} catch (SocketException | UnknownHostException e1) {
				// TODO Auto-generated catch block
				System.out.println("UDP_Test1: " + e1.getMessage());
			}
		}
		
		

	}


	/**
	 * Zatrzymuje odbiór danych z odbiornika
	 */
	@FXML 
	public void disconnectMenuItemHandler(){
		
		if(bReception) {//zatrzymanie odbioru
			
			udpClientThread.interrupt();
			try {
				udpClientThread.join();
				System.out.println("UDPClient stopped");
				receiveBtn.setText("Odbieraj");
				
				//dataConverter.stopReceiving();
				
				System.out.println("DataConverter stopped, queue size: " + blockingSpectrumDataQueue.size());
				blockingSpectrumDataQueue.clear();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bReception = false;
			tStatusField.setText("Odłączenie od odbiornika");
			
		}
	}
	/**
	 * Uruchamia odbiór danych z odbiornika i go zatrzymuje jeżeli dane są odbierane
	 */
	@FXML 
	public void receiveBtnHandler(){
		
		if(bReception) {//zatrzymanie odbioru
			
			udpClientThread.interrupt();
			try {
				udpClientThread.join();
				System.out.println("UDPClient stopped");
				receiveBtn.setText("Odbieraj");
				
				//dataConverter.stopReceiving();
				
				System.out.println("DataConverter stopped, queue size: " + blockingSpectrumDataQueue.size());
				blockingSpectrumDataQueue.clear();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bReception = false;
			tStatusField.setText("Odłączenie od odbiornika");
			
		}
		else {//uruchomienie odbioru pakietów UDP
			try {
				//udpClientThread = new ReceiverUDPClient("192.168.11.2", 4445, spectrumDataPacket, dataConverter, blockingSpectrumDataQueue);
				udpClientThread = new ReceiverUDPClient("localhost", 4445, spectrumDataPacket, dataConverter, blockingSpectrumDataQueue);
				udpClientThread.start();
				System.out.println("UDPClient started");
				receiveBtn.setText("Zatrzymaj");
				bReception = true;
				tStatusField.setText("Podłączenie do odbiornika");
				
				//dataConverter.startReceiving();
				
			} catch (SocketException | UnknownHostException e1) {
				// TODO Auto-generated catch block
				System.out.println("UDP_Test1: " + e1.getMessage());
			}
		}
		
		

	}
	//wczytuje dane nieprzetworzone z bazy danych i uruchamia ich odtwarzanie 
	//ponowne naciśniecie zatrzymuj odtwarzanie
	
	@FXML 
	public void databaseBtnHandler(){
		;
	}
	//Metoda testowa - do poprawy
	@FXML
	public void loadFileHandler() {
		
		//przyk�ad odczytu przes�anego pliku z danymi widma 
		Scanner in = null;
		FileChooser fileChooser = new FileChooser();

		File file = fileChooser.showOpenDialog(primaryStage);
		if (file != null) {

			try {
				in = new Scanner(Paths.get(file.getAbsolutePath()));

				while (in.hasNext()) {
					
					double freq = in.nextDouble();
					double spectrum = in.nextDouble();
					
					//tu doda� klas� przechowuj�c� dane spectrum 

					System.out.printf("frequency: %15f, spectrum: %12f\n", freq, spectrum);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (in != null) {
					in.close();
				}
			}

		}
	}
	//Metoda testowa
	@FXML
	public void saveFileHandler() {
		;
	}
	
	/**
	 * 	Wyświetla dane testowe lokalnie generowane losowo 
	 */
	@FXML
	public void demoChartBtnHandler() {	
		
		if(bSimulation) {//wyłączenie symulacji
			
			tSimulator.interrupt(); 
			try {
				tSimulator.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bSimulation = false;
			tStatusField.setText("Wyłączenie symulacji");
		}
		else {
			tStatusField.setText("Uruchomienie symulacji");
			Runnable runnableDataSimulator = () -> {
				boolean End = false;
				try {
					while (!End) {
						Platform.runLater(()->dataConverter.generateDemoData());
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					System.out.println("\nInterrupted from main, Simulation Stop");
					End = true;
				}
			};
			
			tSimulator = new Thread(runnableDataSimulator);
			tSimulator.start();// generowanie losowych danych i przekazywanie ich do klas nas�uchuj�cych
			bSimulation = true;

			
		}	
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Zdarzenie onDataReceived - do przekazywania odebranych danych widma z odbiornika.
	 *
	 * @param receivedData - odebrane dane widma z odbiornika
	 * @param dataSize - rozmiar danych widma (ilość próbek częstotliwości)
	 * @param seqNumber - numer sekwencyjny ostatniego pakietu danych
	 * @param timeStamp - znacznik czasu ostatniego pakietu danych
	 * @param freqStart - częstotliwosć startowa danych ostatniego pakietu danych
	 * @param freqStep - krok częstotliwosci dla danych ostatniego pakietu danych
	 */
	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {		
		//przyk�adowe wy�wietleie danych
		System.out.println("lineChart - onDataReceived");
		tStatusField.setText("Odbiór danych");
		seqNumberField.setText(Integer.toString(seqNumber));
		timeStampField.setText(Double.toString(timeStamp));
		fStartField.setText(Double.toString(freqStart));
		fStepField.setText(Double.toString(freqStep));
		fStopField.setText(Double.toString(freqStart + dataSize*freqStep));
//		Task task = new Task<Void> () {
//		    @Override public Void call() {
//		    	
//				lineChart.getData().clear();
//				XYChart.Series dataSeries1 = new XYChart.Series();
//				dataSeries1.setName("WYKRES WIDMA");
//				for(int i = 0; i<receivedData.length; i++) {
//					dataSeries1.getData().add(new XYChart.Data( Double.toString(freqStart+ i*freqStep), receivedData[i]));
//				}
//				
//				lineChart.getData().add(dataSeries1);
//		    	
//		    	
//		        
//		        return null;
//		    }
//		};
//		new Thread(task).start();
		
		
		
		
		//tu można odpalić wątek do odświeżania wykresu widma
//		lineChart.getData().clear();
//		XYChart.Series dataSeries1 = new XYChart.Series();
//		dataSeries1.setName("WYKRES WIDMA");
//		for(int i = 0; i<receivedData.length; i++) {
//			dataSeries1.getData().add(new XYChart.Data( Double.toString(freqStart+ i*freqStep), receivedData[i]));
//		}
//		
//		lineChart.getData().add(dataSeries1);
		
	}

	/**
	 * Zdarzenie onImageProcessed - do przekazywania odebranych przetworzonego obrazu do wyświetlenia.
	 *
	 * @param waterfallImage - obraz waterfall z danymi widma z odbiornika do wyświetlenia
	 * @param seqNumber - numer sekwencyjny ostatniego pakietu danych
	 * @param timeStamp - znacznik czasu ostatniego pakietu danych
	 * @param freqStart - częstotliwosć startowa danych ostatniego pakietu danych
	 * @param freqStep - krok częstotliwosci dla danych ostatniego pakietu danych
	 */
	@Override
	public void onImageProcessed(WritableImage waterfallImage, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		
		imageView.setImage(waterfallImage);
		//imageView.setFitWidth(lineChart.getWidth());
		//imageView = new ImageView(waterfallImage);	
		//System.out.println("new waterfallImage");
		
	}

	/**
	 * Zdarzenie onDataProcessed - do przekazywania wykrytych sygnałów do wyświetlenia.
	 *
	 * @param frequency - tablica wykrytych częstotliwości do wyświetlenia
	 * @param signalLevel - tablica poziomów sygnału wykrytych częstotliwości do wyświetlenia
	 * @param seqNumber - numer sekwencyjny ostatniego pakietu danych
	 * @param timeStamp - znacznik czasu ostatniego pakietu danych
	 * @param threshold - poziom progowania powyżej którego są wykrywane sygnały
	 */
	@Override
	public void onDataProcessed(double[] frequency, double[] signalLevel, int seqNumber, double timeStamp,
                                double threshold) {
		// TODO Auto-generated method stub
		
		
		System.out.println("onDataProcessed, frequency.lenght: "  + frequency.length + " signalLevel.lenght: " + signalLevel.length);
		
		processedDataList.clear();
		
		for(int i = 0; i< frequency.length; i++) {
			ProcessedDataForTableView tmpData = new ProcessedDataForTableView();
			tmpData.setFrequency(frequency[i]);
			tmpData.setSeqNumbe(seqNumber);
			tmpData.setSignalLevel(signalLevel[i]);
			tmpData.setThreshold(threshold);
			tmpData.setTimeStamp(timeStamp);			
			processedDataList.add(tmpData);
		}
	
		
		
	}
	
	
		
		
		
	



}