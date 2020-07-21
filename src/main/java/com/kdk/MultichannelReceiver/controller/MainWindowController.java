package com.kdk.MultichannelReceiver.controller;

import com.kdk.MultichannelReceiver.Main;
import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import com.kdk.MultichannelReceiver.model.*;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;



import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;


@Component
public class MainWindowController implements ReceiverDataConverterListener, SpectrumWaterfallListener, SpectrumDataProcessorListener{
	private Main main;
	private Stage primaryStage;

	@FXML private Button addButton;
	@FXML private Button removeBtn;
	@FXML private Button loadFileBtn;
	@FXML private Button saveFileBtn;
	@FXML private Button reportBtn;	
	@FXML private Button chartButton;	
	
	@FXML private TextField tresholdField;
	@FXML private TextField firstNameField;
	@FXML private TextField lastNameField;
	@FXML private TextField roomField;
	@FXML private TextField comingHourField;
	@FXML private TextField leavingHourField;
	@FXML private ImageView imageView;
	@FXML private BorderPane rightPane;
	@FXML private LineChart lineChart;
	
	@FXML private TableView <ProcessedDataForTableView> tableView;
	@FXML private TableColumn <ProcessedDataForTableView, Double> timeStampColumn;
	@FXML private TableColumn <ProcessedDataForTableView, Double> signalsNumberColumn;
	@FXML private TableColumn <ProcessedDataForTableView, Double> freqColumn;

	
	
	ReceiverDataConverter dataConverter = new ReceiverDataConverter();
	SpectrumWaterfall spectrumWaterfall = new SpectrumWaterfall(256) ;
	SpectrumDataProcessor spectrumProcessor = new SpectrumDataProcessor();
	
	
	Thread tSimulator;
	boolean bSimulation = false;
	
	private ObservableList<ProcessedDataForTableView> processedDataList = FXCollections.observableArrayList();

	
	public void setMain(Main main, Stage primaryStage) {
		this.main = main;
		this.primaryStage=primaryStage;


		//dodajemy s�uchaczy odbieraj�cych dane 
		dataConverter.addListener(this);		
		dataConverter.addListener(spectrumWaterfall);
		dataConverter.addListener(spectrumProcessor);
		spectrumWaterfall.addListener(this);
		spectrumProcessor.addListener(this);
	
		spectrumProcessor.setThreshold(Double.parseDouble(tresholdField.getText()));
		processedDataList.add(new ProcessedDataForTableView());
		tableView.setItems(processedDataList);
		
		
		timeStampColumn.setCellValueFactory(new PropertyValueFactory<ProcessedDataForTableView, Double>("timeStamp"));
		signalsNumberColumn.setCellValueFactory(new PropertyValueFactory<ProcessedDataForTableView, Double>("signalLevel"));
		freqColumn.setCellValueFactory(new PropertyValueFactory<ProcessedDataForTableView, Double>("frequency"));


		

		lineChart.setAnimated(false); // disable animations
		lineChart.setCreateSymbols(false);
		lineChart.getYAxis().setAnimated(false);
		lineChart.getXAxis().setAnimated(false);
		lineChart.getYAxis().setMaxHeight(120);
		lineChart.getYAxis().setAutoRanging(false);
		
		
	
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
		
	}

	public void setRecordService(RecordService recordService) {
		spectrumProcessor.setRecordService(recordService);
	}


	@FXML
	public void closeStage(){
		
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

	


	
	@FXML 
	public void addBtnHandle(){
		System.out.println("addPersonBtn pressed");
		

	}
	@FXML 
	public void removeBtnHandle(){
		;
	}
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
	@FXML
	public void saveFileHandler() {
		;
	}
	@FXML
	public void reportHandler() {		
		;		
	}	
	
	@FXML
	public void chartBtnHandler() {	
		
		if(bSimulation) {//wyłączenie symulacji
			tSimulator.interrupt(); 
			try {
				tSimulator.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bSimulation = false;
		}
		else {
			Runnable runnableDataSimulator = () -> {
				boolean End = false;
				try {
					while (!End) {
						Platform.runLater(()->dataConverter.convertData());
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

	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {		
		//przyk�adowe wy�wietleie danych
		

		XYChart.Series dataSeries1 = new XYChart.Series();
		dataSeries1.setName("WYKRES WIDMA");
		for(int i = 0; i<receivedData.length; i++) {
			dataSeries1.getData().add(new XYChart.Data( Double.toString(freqStart+ i*freqStep), receivedData[i]));
		}
		lineChart.getData().clear();
		lineChart.getData().add(dataSeries1);
		
	}

	@Override
	public void onImageProcessed(WritableImage waterfallImage, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		
		imageView.setImage(waterfallImage);
		imageView.setFitWidth(lineChart.getWidth());
		//imageView = new ImageView(waterfallImage);	
		System.out.println("new waterfallImage");
		
	}

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