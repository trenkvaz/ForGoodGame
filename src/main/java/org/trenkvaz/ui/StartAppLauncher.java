package org.trenkvaz.ui;

import org.trenkvaz.main.CaptureVideo;

public class StartAppLauncher {

    public static CaptureVideo captureVideo;

    public static void main(String[] args) {
        captureVideo = new CaptureVideo();
        MainWindow.main(args);
    }
}
