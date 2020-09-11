package org.trenkvaz.main;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import static org.trenkvaz.main.CaptureVideo.coord_left_up_of_tables;

public class HUD {

    public static HUD[] all_huds;
    int[] coord_of_table;
    Stage mainstage;
    private static double xOffset;
    private static double yOffset;
    public Stage[] hud_players;
    int table;
    final int COUNT_TABLES;
    Label[] nicks_labels;


    public HUD(Stage stage1,int table1,int count_tables){
        mainstage = stage1;
        coord_of_table = coord_left_up_of_tables[table1];
        table = table1;
        COUNT_TABLES = count_tables;
        hud_players = new Stage[6];
        nicks_labels = new Label[6];
        init_hud_players();
    }

    public void setAll_huds(HUD[] huds){ all_huds = huds; }


    private void init_hud_players() {

        FXMLLoader loader = null;
        Scene scene = null;
        for(int i=0; i<6; i++)
        try {
            hud_players[i] = new Stage();
            hud_players[i].setTitle("Player"+i);
            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/hud_window.fxml"));
            AnchorPane anchorPanehere = loader.load();
            nicks_labels[i] = (Label) anchorPanehere.lookup("#nick");
            loader.setController(this);
            loader.setRoot(mainstage);
            hud_players[i].setResizable(false);
            scene = new Scene(anchorPanehere);
            hud_players[i].setScene(scene);
            hud_players[i].initModality(Modality.NONE);
            hud_players[i].initOwner(mainstage.getScene().getWindow());
            hud_players[i].setX(coord_of_table[0]+(10*i));
            hud_players[i].setY(coord_of_table[1]+100+(10*i));
            final int number_player =i;
            scene.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = hud_players[number_player].getX() - event.getScreenX();
                    yOffset = hud_players[number_player].getY() - event.getScreenY();
                }
            });
            scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    int X_first_table = (int)(event.getScreenX()-coord_of_table[0]);
                    int Y_first_table = (int)(event.getScreenY()-coord_of_table[1]);
                    for(int i=0; i<COUNT_TABLES; i++){
                        all_huds[i].hud_players[number_player].setX(X_first_table+coord_left_up_of_tables[i][0] + xOffset);
                        all_huds[i].hud_players[number_player].setY(Y_first_table+coord_left_up_of_tables[i][1] + yOffset);}
                }
            });


            hud_players[i].initStyle(StageStyle.UNDECORATED);
            hud_players[i].show();

/*//            hud_players[i].setOnCloseRequest(new EventHandler<WindowEvent>() {
//                @Override
//                public void handle(WindowEvent t) {
//                    hud_players[i].close();
//                }
//            });*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNicks(String[] nicks_str){
        Platform.runLater(() -> {
           for(int i=0; i<6; i++)nicks_labels[i].setText(nicks_str[i]);
        });
    }


}
