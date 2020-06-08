package com.kdk.MultichannelReceiver;


import com.kdk.MultichannelReceiver.controller.MainWindowController;
import com.kdk.MultichannelReceiver.dataPersist.RecordService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main extends Application {

    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            springContext = SpringApplication.run(Main.class);
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/view/MainWindowReceiverView.fxml"));
            loader.setControllerFactory(springContext::getBean);

            AnchorPane pane = loader.load();
            primaryStage.setMinWidth(500.00);
            primaryStage.setMinHeight(600.00);
            Scene scene = new Scene(pane);

            //TODO in work
			RecordService recordService = (RecordService) springContext.getBean(RecordService.class);

            MainWindowController mainWindowController = loader.getController();
            mainWindowController.setMain(this, primaryStage);
            mainWindowController.setRecordService(recordService);
            scene.getStylesheets().add(getClass().getResource("/view/application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
