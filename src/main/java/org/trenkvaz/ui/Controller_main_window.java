package org.trenkvaz.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.OCR;
import org.trenkvaz.main.Testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.MainWindow.anchorPane;
import static org.trenkvaz.ui.MainWindow.stage;
import static org.trenkvaz.ui.StartAppLauncher.*;

public class Controller_main_window {

    @FXML public Button start_stop_capture_video, show_hide_hud, savehands, sb;
    @FXML public Label message_work,timer, labelSB;
    //CaptureVideo captureVideo;
    public static Controller_main_window controller_main_window;
    public static MyTimer mytimer;


    @FXML public void initialize() {
        //captureVideo = new CaptureVideo();
        controller_main_window = this;
        labelSB.setTextFill(Color.BLUE);
        labelSB.setText("SB "+SB);
        //mytimer = new MyTimer();
       // new HUD();
        setStartTotalResultHero();
    }


    @FXML public void capture_video(){

          if(start_stop_capture_video.getText().equals("Start")){ start_stop_capture_video.setText("Stop");
           startStopCapture= captureVideo.new StartStopCapture();
          }
          else {start_stop_capture_video.setText("Start"); stop_CaptureVideo(); }
    }

    public void stop_CaptureVideo(){
        if(startStopCapture!=null){
        startStopCapture.stop_tread();
        for(OCR ocr: CaptureVideo.ocrList_1){
            if(ocr==null)continue;ocr.stop();}}
        //for(OCR ocr:captureVideo.ocrList_2)ocr.stop();
        //System.out.println("Average time "+(testTime/testTimecount)+" count "+testTimecount);
        //if(counttime!=0)System.out.println("Average time "+(alltime/counttime)+" count "+counttime);
        System.out.println("stop");
        Testing.write_LogTest("TOTAL "+totalResultHero+"\r\n","resultHero");
        Testing.write_LogTest("TOTAL "+totalStreetHero[0]+"\r\n","totalPreflop");
        Testing.write_LogTest("TOTAL "+totalStreetHero[1]+"\r\n","totalFlop");
        Testing.write_LogTest("TOTAL "+totalStreetHero[2]+"\r\n","totalTurn");
        Testing.write_LogTest("TOTAL "+totalStreetHero[3]+"\r\n","totalRiver");
    }

    @FXML public void set_show_hud(){
        if(show_hide_hud.getText().equals("Show HUD")){
            show_hide_hud.setText("Stop HUD");
            //hud.show_hud();
            hud.is_hud_on = true;
            //for(int i=0; i<6; i++){ hud.show_hud(i);hud.refresh_hud(i); }
        } else {
            show_hide_hud.setText("Show HUD");
            //hud.stop_show_hud();
            hud.is_hud_on = false;
            for(int i=0; i<6; i++){ hud.stop_show_hud(i); }
        }
        anchorPane.requestFocus();
    }

    public void setMessage_work(String message, Paint color){
        Platform.runLater(() -> {
            message_work.setTextFill(color);
            message_work.setText(message);
        });
    }


    @FXML public void on_off_savehands(){
        if(savehands.getText().equals("Not saving hands")){
            savehands.setTextFill(Color.GREEN);
            savehands.setText("Saving hands");
            let_SaveTempHandsAndCountStatsCurrentGame = true;
        } else {
            savehands.setTextFill(Color.RED);
            savehands.setText("Not saving hands");
            let_SaveTempHandsAndCountStatsCurrentGame = false;
        }
    }


    @FXML public void resetWin(){
        totalResultHero = 0;
        setTestMessage("0",Color.GREEN);
    }


    @FXML public void changeSB(){
        if(labelSB.getText().equals("SB 0.4"))SB = 0.5f;
        else SB = 0.4f;

        labelSB.setText("SB "+SB);
    }

    public void setTestMessage(String message,Paint color){ Platform.runLater(() -> { timer.setTextFill(color);timer.setText(message); }); }



     void setStartTotalResultHero(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(home_folder+"\\all_settings\\capture_video\\tiltBreaker.txt"));
            totalResultHero = Float.parseFloat(br.readLine());
            if(totalResultHero!=0)setTestMessage("NOT ZERO",Color.RED);
            else setTestMessage("0",Color.GREEN);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    class MyTimer implements Runnable {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "dd MMMM yyyy HH:mm:ss", Locale.getDefault());
        boolean time = true;
        public MyTimer(){
            new Thread(this).start();
        }

        @Override
        public void run() {
            while (time){
            String strDate = simpleDateFormat.format(System.currentTimeMillis());
                Platform.runLater(() -> {
                    timer.setText(strDate);
                });
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           }
        }

       public synchronized void stop_timer(){ time = false; }
    }

    public static void main(String[] args) {

    }
}
