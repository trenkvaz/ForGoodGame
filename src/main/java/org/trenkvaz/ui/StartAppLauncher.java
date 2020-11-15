package org.trenkvaz.ui;

import org.trenkvaz.database_hands.Work_DataBase;
import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.CreatingHUD;

public class StartAppLauncher {

    public static final String home_folder = System.getProperty("user.dir");
    public static CaptureVideo captureVideo;
    public static Work_DataBase work_dataBase;
    //public static CreatingHUD creatingHUD;
    public static HUD hud;


    public static void main(String[] args) {
        work_dataBase = new Work_DataBase();
        captureVideo = new CaptureVideo();

        hud = new HUD();
        MainWindow.main(args);
    }
}
