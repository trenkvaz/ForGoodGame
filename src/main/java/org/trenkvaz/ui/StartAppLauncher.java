package org.trenkvaz.ui;

import javafx.scene.paint.Color;
import org.trenkvaz.database_hands.Work_DataBase;
import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.newstats.CreateNewHUD;
import org.trenkvaz.newstats.WorkStats;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.trenkvaz.ui.Controller_main_window.controller_main_window;

public class StartAppLauncher {

    public static final String RESET = "\u001b[0m" , RED = "\u001b[31m", BLUE = "\u001b[34m", GREEN = "\u001b[32m";

    public static final String home_folder = System.getProperty("user.dir");
    public static CaptureVideo captureVideo;
    public static Work_DataBase work_dataBase;
    public static HUD hud;
    public static WorkStats workStats;
    public static CreateNewHUD createNewHUD;

    public static float SB = 0.4f;
    public static final int SCALE = 1;
    public static float totalResultHero = 0;
    public static float stopWin = 999;
    public static float stopLoss = -300;

    //TEST
    public static boolean isTestDBandStats = false;

    public static boolean isTestNicks = false;


    public static float[] totalStreetHero = new float[4];


    public static void main(String[] args) {
        work_dataBase = new Work_DataBase();
        captureVideo = new CaptureVideo();
        hud = new HUD();
        workStats = new WorkStats(true);
        workStats.fullMapNicksMapsNameFilterDataStata("work_");
        createNewHUD = new CreateNewHUD();

        MainWindow.main(args);
    }













    public static synchronized void tiltBreaker(){
        if(stopLoss!=0&&totalResultHero<stopLoss)controller_main_window.setTestMessage("LOSE !!! "+totalResultHero, Color.RED);
        if(stopWin!=0&&totalResultHero>stopWin)controller_main_window.setTestMessage("WIN !!! "+totalResultHero, Color.GREEN);

        try {
            OutputStream os = new FileOutputStream(new File(home_folder+"\\all_settings\\capture_video\\tiltBreaker.txt"), false);
            os.write(Float.toString(totalResultHero).getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
