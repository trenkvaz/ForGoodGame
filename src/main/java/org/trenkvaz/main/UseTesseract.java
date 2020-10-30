package org.trenkvaz.main;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;
import static org.trenkvaz.main.Testing.save_image;

public class UseTesseract {
    /*Tesseract tesseract_for_number_hand;
    Tesseract tesseract_for_stacks;
    Tesseract tesseract_for_actions;*/


    ReentrantLock lock = new ReentrantLock();
    Tesseract tesseract_for_nicks;

    UseTesseract(){
        init_tessart();
        //set_engin_mod(1);
    }



    UseTesseract(int sps){
        tesseract_for_nicks = new Tesseract();

        //tesseract_for_nicks.setDatapath("C:\\Users\\Duduka\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\4.5.1\\tess4j-4.5.1\\tessdata");

        tesseract_for_nicks.setDatapath(System.getProperty("user.dir")+"\\tesseract_ltsm");
        tesseract_for_nicks.setOcrEngineMode(1);
        tesseract_for_nicks.setPageSegMode(sps);
        tesseract_for_nicks.setTessVariable("user_defined_dpi", "300");
        tesseract_for_nicks.setTessVariable("tessedit_char_whitelist","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.- _");
        tesseract_for_nicks.setTessVariable ( "edges_max_children_per_outline" , "15" );

        tesseract_for_nicks.setTessVariable( "load_system_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_unambig_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_freq_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_punc_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_number_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_fixed_length_dawgs", "0");
        tesseract_for_nicks.setTessVariable( "wordrec_enable_assoc", "0");
        tesseract_for_nicks.setTessVariable( "tessedit_enable_bigram_correction", "0");
        tesseract_for_nicks.setTessVariable( "assume_fixed_pitch_char_segment", "1");
    }



    private void init_tessart(){
        /*tesseract_for_number_hand = new Tesseract();
        tesseract_for_number_hand.setDatapath("C:\\Users\\Duduka\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\4.5.1\\tess4j-4.5.1\\tessdata");
        tesseract_for_number_hand.setTessVariable("user_defined_dpi", "300");
        tesseract_for_number_hand.setTessVariable("tessedit_char_whitelist","0123456789");*/

        tesseract_for_nicks = new Tesseract();

        tesseract_for_nicks.setDatapath("C:\\Users\\Duduka\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\4.5.1\\tess4j-4.5.1\\tessdata");

        //tesseract_for_nicks.setDatapath(System.getProperty("user.dir")+"\\tesseract_ltsm");
        //tesseract_for_nicks.setOcrEngineMode(1);
        tesseract_for_nicks.setPageSegMode(7);


        tesseract_for_nicks.setTessVariable("user_defined_dpi", "300");
        tesseract_for_nicks.setTessVariable("tessedit_char_whitelist","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.- _");
        tesseract_for_nicks.setTessVariable ( "edges_max_children_per_outline" , "15" );

        tesseract_for_nicks.setTessVariable( "load_system_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_unambig_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_freq_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_punc_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_number_dawg", "0");
        tesseract_for_nicks.setTessVariable( "load_fixed_length_dawgs", "0");
        tesseract_for_nicks.setTessVariable( "wordrec_enable_assoc", "0");
        tesseract_for_nicks.setTessVariable( "tessedit_enable_bigram_correction", "0");
        tesseract_for_nicks.setTessVariable( "assume_fixed_pitch_char_segment", "1");

        //setTessVariable(tessBaseApi, "load_freq_dawg", "0");
        //setTessVariable(tessBaseApi, "load_unambig_dawg", "0");
        //setTessVariable(tessBaseApi, "load_punc_dawg", "0");
        //setTessVariable(tessBaseApi, "load_number_dawg", "0");
        //setTessVariable(tessBaseApi, "load_fixed_length_dawgs", "0");
        //setTessVariable(tessBaseApi, "load_bigram_dawg", "0");
        //setTessVariable(tessBaseApi, "wordrec_enable_assoc", "0");
        //setTessVariable(tessBaseApi, "tessedit_enable_bigram_correction", "0");

        tesseract_for_nicks.setLanguage("eng");



        /*tesseract_for_stacks = new Tesseract();
        tesseract_for_stacks.setDatapath("C:\\Users\\Duduka\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\4.5.1\\tess4j-4.5.1\\tessdata");
        tesseract_for_stacks.setTessVariable("user_defined_dpi", "300");
        tesseract_for_stacks.setTessVariable("tessedit_char_whitelist","0123456789.");*/
        //tesseract_for_stacks.setPageSegMode(13);


        /*tesseract_for_actions = new Tesseract();
        tesseract_for_actions.setDatapath("C:\\Users\\Duduka\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\4.5.1\\tess4j-4.5.1\\tessdata");
        tesseract_for_actions.setTessVariable("user_defined_dpi", "300");
        tesseract_for_actions.setTessVariable("tessedit_char_whitelist","0123456789.");
        tesseract_for_actions.setLanguage("eng");

        tesseract_for_actions.setPageSegMode(7);*/

        //tesseract.setOcrEngineMode(3);
    }

   /* private void set_engin_mod(int type){
        tesseract_for_number_hand.setOcrEngineMode(type);
        tesseract_for_nicks.setOcrEngineMode(type);
        tesseract_for_stacks.setOcrEngineMode(type);
        tesseract_for_actions.setOcrEngineMode(type);
    }*/

    /*private String get_number_hand(BufferedImage bufferedImage){
        String text = "text";
        try {
            text = tesseract_for_number_hand.doOCR(bufferedImage);
        }
        catch (TesseractException e) {
            e.printStackTrace();
        }
        save_image(bufferedImage,"for_ocr_number\\"+text.trim());
        return text;
    }*/

    private String get_nicks(BufferedImage bufferedImage){
        String text = "text";
        try {
            text = tesseract_for_nicks.doOCR(bufferedImage);
        }
        catch (TesseractException e) {
            e.printStackTrace();
        }
        save_image(bufferedImage,"for_ocr\\_"+text.trim());
        if(text.equals(""))return null;

        return text;
    }

   /* private String get_stacks(BufferedImage bufferedImage){
        String text = "text";
        try {
            text = tesseract_for_stacks.doOCR(bufferedImage);
        }
        catch (TesseractException e) {
            e.printStackTrace();
        }
        save_image(bufferedImage,"for_ocr_stacks\\_"+text.trim());
        //System.out.println(text);
        return text;
    }*/

    /*private String get_actions(BufferedImage bufferedImage){
        String text = "text";
        try {
            text = tesseract_for_actions.doOCR(bufferedImage);
        }
        catch (TesseractException e) {
            e.printStackTrace();
        }
        save_image(bufferedImage,"for_ocr_actions\\_"+text.trim());
        return text;
    }*/

    public String get_ocr(BufferedImage bufferedImage){

        if(!lock.tryLock())return null;

        try {
            //if(type.equals("hand"))return get_number_hand(bufferedImage);
            //if(type.equals("nicks"))
                return get_nicks(bufferedImage);
           /* if(type.equals("stacks"))return get_stacks(bufferedImage);
            if(type.equals("actions"))return get_actions(bufferedImage);*/

        } finally {
            lock.unlock();
        }
    }
}
