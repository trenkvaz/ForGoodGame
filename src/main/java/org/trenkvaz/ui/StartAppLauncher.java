package org.trenkvaz.ui;

import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.CreatingHUD;

public class StartAppLauncher {

    public static CaptureVideo captureVideo;
    public static CreatingHUD creatingHUD;
    public static HUD hud;

    public static void main(String[] args) {
        captureVideo = new CaptureVideo();
        creatingHUD = new CreatingHUD();
        hud = new HUD();
        MainWindow.main(args);
    }
}
