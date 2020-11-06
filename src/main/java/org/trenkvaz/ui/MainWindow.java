package org.trenkvaz.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import static org.trenkvaz.ui.StartAppLauncher.hud;
import static org.trenkvaz.ui.StartAppLauncher.work_dataBase;

public class MainWindow extends Application {

    static AnchorPane anchorPane;
    public static Stage stage;


    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        primaryStage.setTitle("For Good Game");
        FXMLLoader loadermain = new FXMLLoader();
        loadermain.setLocation(getClass().getResource("/fxml/main_window.fxml"));
        //loadermain.setLocation(new URL("file:\\"+file_address_program_folder+"\\resources\\fxml\\sample.fxml"));
        //loadermain.setLocation(getClass().getResource(System.getProperty("user.dir").toString()+"\\resources\\fxml\\sample.fxml"));

        anchorPane = loadermain.load();
        Scene scene = new Scene(anchorPane);
        primaryStage.setScene(scene);
        primaryStage.setHeight(164);
        primaryStage.setWidth(910);
        primaryStage.setX(900);
        primaryStage.setY(950);
        primaryStage.initStyle(StageStyle.UTILITY);
        stage.setAlwaysOnTop(true);
        //stage.setIconified(false);
        primaryStage.show();
        hud.init_hud(primaryStage);



        //open_hud_on_tables();
        //init_hud(primaryStage);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                work_dataBase.close_DataBase();
                Platform.exit();
                System.exit(0);
            }
        });

    }






    public static void main(String[] args) {



        launch(args);
    }
}
