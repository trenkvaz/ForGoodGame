package org.trenkvaz.main;

import org.trenkvaz.database_hands.Work_DataBase;

import java.util.ArrayList;
import java.util.Arrays;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.Controller_main_window.controller_main_window;
import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;
import static org.trenkvaz.ui.StartAppLauncher.work_dataBase;

public class CurrentHand {

    int table;
    long time_hand;
    String[] nicks = new String[6];
    int[] poker_positions_by_pos_table_for_nicks;
    boolean is_nicks_filled = false;
    //boolean is_preflop_end = false;
    boolean is_start_flop = false;
    boolean is_stacks_filled = false;

    int position_bu_on_table = 0;
    int poker_position_of_hero = -1;
    String[] cards_hero = {"",""};
    Float[] stacks = new Float[6];
    //float[] first_round_preflop = new float[6];
    ArrayList<ArrayList<Float>> preflop_by_positions = new ArrayList<>(6);
    public record TempHand(long time_hand, short cards_hero, short position_hero, Float[] stacks, String[] nicks){}

    CurrentHand(int table1){
        table = table1;
        for(int i=0; i<6; i++){
            preflop_by_positions.add(new ArrayList<Float>());
            stacks[i] = 0f;
        }
        time_hand =  get_HandTime();
    }

    void setIs_nicks_filled(){
        creatingHUD.send_current_hand_to_creating_hud(nicks,table);
        for(int i=1; i<6; i++)if(nicks[i]==null){is_nicks_filled = false; return;}
        is_nicks_filled = true;
    }

    public void set_NicksByPositions(){
        // расстановка ников по покерным позициям

        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            if(nicks[poker_positions_by_pos_table_for_nicks[i]-1]==null)continue;
            nicks_by_positions[i] = nicks[poker_positions_by_pos_table_for_nicks[i]-1];
        }
        nicks = nicks_by_positions;
    }

    public static synchronized void creat_HandForSaving(CurrentHand currentHand){

      controller_main_window.setMessage_work(currentHand.time_hand+"");
      //Integer[] idplayers = get_and_write_NewIdPlayersForNicks(currentHand.nicks);
      Work_DataBase.record_rec_to_TableTempHands(new TempHand(currentHand.time_hand,get_short_CardsHero(currentHand.cards_hero),(short)currentHand.poker_position_of_hero,currentHand.stacks,currentHand.nicks));
    /* for(int i=0; i<6; i++){if(nicks[i]==null)continue;
         System.out.println(nicks[i]+"  "+idplayers[i]);
     }*/

    }


    static short get_short_CardsHero(String[] cards_hero){ return (short) ((byte) Arrays.asList(Deck).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(Deck).indexOf(cards_hero[1])); }
}
