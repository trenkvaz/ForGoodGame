package org.trenkvaz.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;
import static org.trenkvaz.ui.StartAppLauncher.work_dataBase;

public class CurrentHand {

    int table;
    String[] nicks = new String[6];
    boolean is_nicks_filled = false;
    boolean is_preflop_end = false;
    boolean is_start_flop = false;
    boolean is_stacks_filled = false;

    int position_bu_on_table = 0;
    int poker_position_of_hero = -1;
    String[] cards_hero = {"",""};
    Float[] stacks = new Float[6];
    //float[] first_round_preflop = new float[6];
    ArrayList<ArrayList<Float>> preflop_by_positions = new ArrayList<>(6);
    public record TempHand(long time_hand, short cards_hero, short position_hero, Float[] stacks, Integer[] idplayers){}

    CurrentHand(int table1,float sb){
        table = table1;
        for(int i=0; i<6; i++){
            preflop_by_positions.add(new ArrayList<Float>());
        }
    }

    void setIs_nicks_filled(){
        for(int i=1; i<6; i++)if(nicks[i]==null){is_nicks_filled = false; return;}
        is_nicks_filled = true;
        creatingHUD.send_current_hand_to_creating_hud(nicks,table);
    }


    public void creat_HandForSaving(){
      long time_hand =  get_TimeNanoSeconds();
      Integer[] idplayers = get_and_write_NewIdPlayersForNicks(nicks);
      work_dataBase.record_rec_to_TableTempHands(new TempHand(time_hand,get_short_CardsHero(),(short)poker_position_of_hero,stacks,idplayers));
    /* for(int i=0; i<6; i++){if(nicks[i]==null)continue;
         System.out.println(nicks[i]+"  "+idplayers[i]);
     }*/

    }


    short get_short_CardsHero(){ return (short) ((byte) Arrays.asList(Deck).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(Deck).indexOf(cards_hero[1])); }
}
