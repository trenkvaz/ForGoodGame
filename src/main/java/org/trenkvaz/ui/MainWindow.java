package org.trenkvaz.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.trenkvaz.main.HUD;

import java.io.IOException;

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

        anchorPane = (AnchorPane) loadermain.load();
        Scene scene = new Scene(anchorPane);
        primaryStage.setScene(scene);
        primaryStage.setHeight(610);
        primaryStage.setWidth(910);

       // primaryStage.show();

        //open_hud_on_tables();
        init_hud(primaryStage);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

    }


    /*private void open_hud_on_tables(){
        for(int i=1; i<3; i++)
        try {
            Stage modalwindowRange = new Stage();
            modalwindowRange.setTitle("SetRange");
            FXMLLoader loadermain = new FXMLLoader();
            loadermain.setLocation(getClass().getResource("/fxml/hud_window.fxml"));
            //AnchorPane anchorPanehere = FXMLLoader.load(getClass().getResource("/fxml/hud_window.fxml"));
            HUD hud = new HUD(i,modalwindowRange,i*100,i*100);
            //loadermain.setRoot(hud);
            loadermain.setController(hud);
           *//* try {
                fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }*//*
            AnchorPane anchorPanehere = loadermain.load();
            modalwindowRange.setResizable(false);
            modalwindowRange.setScene(new Scene(anchorPanehere));
            modalwindowRange.initModality(Modality.NONE);
            // modalwindowRange.initOwner(((anchorPane)actionEvent.getSource()).getScene().getWindow());
            modalwindowRange.initOwner(stage.getScene().getWindow());
            modalwindowRange.show();
            modalwindowRange.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    modalwindowRange.close();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private void init_hud(Stage mainstage){
        HUD[] huds = new HUD[6];
        for(int i=0; i<6; i++) huds[i] =new HUD(mainstage,i,6);
        huds[0].setAll_huds(huds);
        String[] nicks = {"aaa","bbb","ccc","ddd","eee","fff"};
        for(int i=0; i<6; i++)huds[i].setNicks(nicks);

    }



    public static void main(String[] args) {



        launch(args);
    }
}
