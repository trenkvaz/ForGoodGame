package org.trenkvaz.ui;

import org.trenkvaz.database_hands.Work_DataBase;
import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.CreatingHUD;
import org.trenkvaz.newstats.CreateNewHUD;
import org.trenkvaz.newstats.WorkStats;

public class StartAppLauncher {

    public static final String RESET = "\u001b[0m" , RED = "\u001b[31m", BLUE = "\u001b[34m", GREEN = "\u001b[32m";

    public static final String home_folder = System.getProperty("user.dir");
    public static CaptureVideo captureVideo;
    public static Work_DataBase work_dataBase;
    public static HUD hud;
    public static WorkStats workStats;
    public static CreateNewHUD createNewHUD;

    public static final float SB = 0.5f;
    public static final int SCALE = 1;
    public static float totalResultHero = 0;

    //TEST
    public static boolean isTest = true;

    public static boolean isNewHudTest = true;

    public static boolean isNewStats = true;
    // ПОМЕНЯТЬ БАЗУ !

    public static float[] totalStreetHero = new float[4];



    public static void main(String[] args) {
        work_dataBase = new Work_DataBase();
        captureVideo = new CaptureVideo();
        hud = new HUD();
        if(isNewStats){workStats = new WorkStats(false);
        workStats.fullMapNicksMapsNameFilterDataStata("work_");
        createNewHUD = new CreateNewHUD();
        }
        MainWindow.main(args);
    }
}
