package controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ReceiverDataConverter;
import model.ReceiverDataConverterListener;
import model.SpectrumDataProcessor;
import model.SpectrumWaterfall;
import model.SpectrumWaterfallListener;


public class MainWindowController implements ReceiverDataConverterListener, SpectrumWaterfallListener{
	private Main main;
	private Stage primaryStage;

	@FXML private Button addButton;
	@FXML private Button removeBtn;
	@FXML private Button loadFileBtn;
	@FXML private Button saveFileBtn;
	@FXML private Button reportBtn;	
	@FXML private Button chartButton;	
	
	@FXML private TextField firstNameField;
	@FXML private TextField lastNameField;
	@FXML private TextField roomField;
	@FXML private TextField comingHourField;
	@FXML private TextField leavingHourField;
	@FXML private ImageView imageView;
	@FXML private BorderPane rightPane;
	@FXML private LineChart lineChart;
	
	
	ReceiverDataConverter dataConverter = new ReceiverDataConverter();
	SpectrumWaterfall spectrumWaterfall = new SpectrumWaterfall(128) ;
	SpectrumDataProcessor spectrumProcessor = new SpectrumDataProcessor();

	
	public void setMain(Main main, Stage primaryStage) {
		this.main = main;
		this.primaryStage=primaryStage;


		//dodajemy s³uchaczy odbieraj¹cych dane 
		dataConverter.addListener(this);		
		dataConverter.addListener(spectrumWaterfall);
		dataConverter.addListener(spectrumProcessor);
		spectrumWaterfall.addListener(this);
	}
	
	@FXML
	public void closeStage(){
		dataConverter.removeListener(this);
		dataConverter.removeListener(spectrumWaterfall);
		spectrumWaterfall.removeListener(this);
		
		
		primaryStage.close();
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
		
		//przyk³ad odczytu przes³anego pliku z danymi widma 
		Scanner in = null;
		FileChooser fileChooser = new FileChooser();

		File file = fileChooser.showOpenDialog(primaryStage);
		if (file != null) {

			try {
				in = new Scanner(Paths.get(file.getAbsolutePath()));

				while (in.hasNext()) {
					
					double freq = in.nextDouble();
					double spectrum = in.nextDouble();
					
					//tu dodaæ klasê przechowuj¹c¹ dane spectrum 

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
		//generowanie losowych danych i przekazywanie ich do klas nas³uchuj¹cych 
		dataConverter.convertData();		
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {		
		//przyk³adowe wyœwietleie danych
		XYChart.Series dataSeries1 = new XYChart.Series();
		dataSeries1.setName("Wykres Widma");

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
		//imageView = new ImageView(waterfallImage);	
		System.out.println("new waterfallImage");
		
	}	
	
	
}