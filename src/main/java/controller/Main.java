package controller;


import dataPersist.RecordService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			FXMLLoader loader= new FXMLLoader(
					Main.class.getResource("/view/MainWindowReceiverView.fxml"));
			
			AnchorPane pane = loader.load();
			primaryStage.setMinWidth(500.00);
			primaryStage.setMinHeight(600.00);
			Scene scene=new Scene(pane);

//			SPRING!!
			ConfigurableApplicationContext springContext;
			springContext = SpringApplication.run(Main.class);
			loader.setControllerFactory(springContext::getBean);

			//TODO to mi nie dziala Kamil O // narazie
			RecordService recordService = (RecordService) springContext.getBean(RecordService.class);




			MainWindowController mainWindowController = loader.getController();
			mainWindowController.setMain(this, primaryStage);
			scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
