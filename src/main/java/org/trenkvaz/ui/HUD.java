package org.trenkvaz.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.trenkvaz.main.CreatingHUD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.trenkvaz.main.CaptureVideo.COORDS_TABLES;
import static org.trenkvaz.ui.MainWindow.anchorPane;
import static org.trenkvaz.ui.StartAppLauncher.isNewHudTest;

public class HUD {

    int[][] start_coords;
    AnchorPane[][] anchorpanes_huds_each_player;
    Stage[][] stages_huds_each_player;
    private static double xOffset, yOffset;
    public boolean is_hud_on = false;
    //Text[][][] arr_one_table_texts_huds_each_player = new Text[6][][];
    List<List<List<Text>>> list_one_table_texts_huds_each_player = new ArrayList<>(6);


    public HUD(){
        for(int i=0; i<6; i++) list_one_table_texts_huds_each_player.add(null);
        start_coords = CreatingHUD.Setting.read_coords_hud();
        initPanesNewHUD();
    }



    public void init_hud(Stage mainstage){
        anchorpanes_huds_each_player = new AnchorPane[6][6];
        stages_huds_each_player = new Stage[6][6];
        FXMLLoader loader = null;
        Scene scene = null;
        for(int table = 0; table<6; table++)
            for(int player =0; player<6; player++)
                try {
                    stages_huds_each_player[table][player] = new Stage();
                    loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/fxml/hud_window.fxml"));
                    anchorpanes_huds_each_player[table][player]= loader.load();
                    //nicks_labels[i] = (Label) anchorpanes_players[i].lookup("#nick");
                    loader.setController(this);
                    loader.setRoot(mainstage);
                    stages_huds_each_player[table][player].setResizable(false);
                    scene = new Scene(anchorpanes_huds_each_player[table][player]);
                    stages_huds_each_player[table][player].setScene(scene);
                    //stages_huds_each_player[table][player].initModality(Modality.NONE);
                    stages_huds_each_player[table][player].initOwner(mainstage);
                    stages_huds_each_player[table][player].setX(COORDS_TABLES[table][0]+start_coords[player][0]);
                    stages_huds_each_player[table][player].setY(COORDS_TABLES[table][1]+start_coords[player][1]);
                    final int fin_table =table, fin_player = player;
                    scene.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            xOffset = stages_huds_each_player[fin_table][fin_player].getX() - event.getScreenX();
                            yOffset = stages_huds_each_player[fin_table][fin_player].getY() - event.getScreenY();
                        }
                    });
                    scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            int X_first_table = (int)(event.getScreenX()- COORDS_TABLES[fin_table][0]);
                            int Y_first_table = (int)(event.getScreenY()- COORDS_TABLES[fin_table][1]);
                            for(int i=0; i<6; i++){
                                stages_huds_each_player[i][fin_player].setX(X_first_table+ COORDS_TABLES[i][0] + xOffset);
                                stages_huds_each_player[i][fin_player].setY(Y_first_table+ COORDS_TABLES[i][1] + yOffset);}
                            start_coords[fin_player][0] = (int)(X_first_table+xOffset);
                            start_coords[fin_player][1] = (int) (Y_first_table+yOffset);
                            CreatingHUD.Setting.write_coords_hud(start_coords);
                        }

                    });

                    stages_huds_each_player[table][player].initStyle(StageStyle.UNDECORATED);
                    //stages_huds_each_player[table][player].initStyle(StageStyle.UTILITY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
    }



    public void show_hud(int table){
        if(is_hud_on)
            Platform.runLater(() -> {
                for(Stage playerstage:stages_huds_each_player[table])playerstage.show();
                anchorPane.requestFocus();
            });

    }



    public void stop_show_hud(int table){
        //if(!is_hud_on)
            Platform.runLater(() -> {
                for(Stage playerstage:stages_huds_each_player[table])playerstage.hide();
                anchorPane.requestFocus();
            });

    }



    public synchronized void set_hud(List<List<Text>> arr_one_table_texts_huds_each_player, int table){

       //this.arr_one_table_texts_huds_each_player[table] = arr_one_table_texts_huds_each_player;
       list_one_table_texts_huds_each_player.set(table,arr_one_table_texts_huds_each_player);
       refresh_hud(table);
    }

    public void clear_hud(int table){
        //if(arr_one_table_texts_huds_each_player[table]==null)return;
        if(isNewHudTest){ clearNewHUD(table); return; }


        if(list_one_table_texts_huds_each_player.get(table)==null) return;
        Platform.runLater(() -> {
            for(int player = 0; player<6; player++){
                //if(arr_one_table_texts_huds_each_player[table][player]==null)continue;
                if(list_one_table_texts_huds_each_player.get(table).get(player).isEmpty())continue;
                anchorpanes_huds_each_player[table][player].getChildren().clear();
            }
            anchorPane.requestFocus();
        });
    }

    public void refresh_hud(int table){

        if(isNewHudTest){ refreshNewHUD(table); return; }

        if(list_one_table_texts_huds_each_player.get(table)==null) return;
        Platform.runLater(() -> {
            for(int player = 0; player<6; player++){
                //if(arr_one_table_texts_huds_each_player[table][player]==null)continue;
                if(list_one_table_texts_huds_each_player.get(table).get(player).isEmpty())continue;
                anchorpanes_huds_each_player[table][player].getChildren().clear();
                for(Text stata:list_one_table_texts_huds_each_player.get(table).get(player)){
                    //if(stata==null)continue;
                    anchorpanes_huds_each_player[table][player].getChildren().add(stata);
                }
            }
            anchorPane.requestFocus();
        });

    }

    private final Pane[][] panesTablesPlayers = new Pane[6][6];

    private void initPanesNewHUD(){
        for(int i=0; i<6; i++)
            for(int a=0; a<6; a++)panesTablesPlayers[i][a] = new Pane();
    }

    public void setNewHUD(List<Set<Text>> arr_one_table_texts_huds_each_player, int table){
        Platform.runLater(()->{
            for(int i=0; i<6; i++){
                panesTablesPlayers[table][i].getChildren().clear();
                if(arr_one_table_texts_huds_each_player.get(i).isEmpty())continue;
                for(Text text:arr_one_table_texts_huds_each_player.get(i)) panesTablesPlayers[table][i].getChildren().add(text);
            }
        });
        refreshNewHUD(table);
    }

    public void refreshNewHUD(int table){
        //if(panesTablesPlayers[table]==null)return;
        Platform.runLater(() -> {
            for(int player = 0; player<6; player++){
               //if(panesTablesPlayers[table][player]==null)continue;
                anchorpanes_huds_each_player[table][player].getChildren().clear();
                anchorpanes_huds_each_player[table][player].getChildren().add(panesTablesPlayers[table][player]);
            }
            anchorPane.requestFocus();
        });
    }

    public void clearNewHUD(int table){
        //if(panesTablesPlayers[table]==null)return;
        Platform.runLater(() -> {
            for(int player = 0; player<6; player++){
                //if(panesTablesPlayers[table][player]==null)continue;
                anchorpanes_huds_each_player[table][player].getChildren().clear();
            }
            anchorPane.requestFocus();
        });
    }
}

