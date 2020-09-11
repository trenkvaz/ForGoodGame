package org.trenkvaz.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.HUD;
import org.trenkvaz.main.OCR;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Controller_main_window {

    @FXML public Button start_stop_capture_video;

    CaptureVideo captureVideo;


    @FXML public void initialize() {
        //captureVideo = new CaptureVideo();

       // new HUD();
    }


    @FXML public void capture_video(){

          if(start_stop_capture_video.getText().equals("Start")){ start_stop_capture_video.setText("Stop");
           captureVideo.start_thread();

          }
          else {start_stop_capture_video.setText("Start");
           captureVideo.stop_tread();
              for(OCR ocr:captureVideo.ocrList_1)ocr.stop();
              //for(OCR ocr:captureVideo.ocrList_2)ocr.stop();
              System.out.println("stop");



          }
    }

    public static void main(String[] args) {

    }
}
