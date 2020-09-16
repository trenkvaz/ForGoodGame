package org.trenkvaz.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.OCR;

import static org.trenkvaz.ui.StartAppLauncher.captureVideo;
import static org.trenkvaz.ui.StartAppLauncher.hud;

public class Controller_main_window {

    @FXML public Button start_stop_capture_video, show_hide_hud;
    @FXML public Label message_work;
    //CaptureVideo captureVideo;
    public static Controller_main_window controller_main_window;

    @FXML public void initialize() {
        //captureVideo = new CaptureVideo();
        controller_main_window = this;
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

    @FXML public void set_show_hud(){
        if(show_hide_hud.getText().equals("Show HUD")){
            show_hide_hud.setText("Stop HUD");
            hud.show_hud();
        } else {
            show_hide_hud.setText("Show HUD");
            hud.stop_show_hud();
        }
    }

    public void setMessage_work(String message){
        Platform.runLater(() -> {
            message_work.setText(message);
        });
    }

    public static void main(String[] args) {

    }
}
