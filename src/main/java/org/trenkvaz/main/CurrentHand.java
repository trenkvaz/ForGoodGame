package org.trenkvaz.main;

import org.trenkvaz.database_hands.Work_DataBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.Controller_main_window.controller_main_window;
//import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;
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
    CreatingHUD creatingHUD;


    CurrentHand(int table1,CreatingHUD creatingHUD1){
        creatingHUD = creatingHUD1;
        creatingHUD.clear_MapStats();
        table = table1;
        for(int i=0; i<6; i++){
            preflop_by_positions.add(new ArrayList<Float>());
            if(i<4)preflop_by_positions.get(i).add(0f);
            stacks[i] = 0f;
        }
        time_hand =  get_HandTime();
    }

    void setIs_nicks_filled(){
        //current_Stats.set_Stats();
        creatingHUD.send_current_hand_to_creating_hud(nicks,poker_positions_by_pos_table_for_nicks,poker_position_of_hero);
        // проверка что все ники распознаны, чтобы не обращатся к методу распознавания ников
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


    static short get_short_CardsHero(String[] cards_hero){ return (short)
            ((byte) Arrays.asList(Deck).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(Deck).indexOf(cards_hero[1])); }






    public void creat_ActionsInHandForCountStats(){
        List<List<Float>> preflop_actions_for_stats = new ArrayList<>(6);
        for(int f=0; f<6; f++){
            preflop_actions_for_stats.add(new ArrayList<Float>()); preflop_actions_for_stats.get(f).add(0f);
            if(preflop_by_positions.get(f).size()==1)preflop_by_positions.get(f).add(0.0f);
        }


        if(!is_start_flop){
               System.err.println("START count");
            float size_raise = 1; float befor_action =1;
           int round = 0; boolean run = true;
         while(run){ round++;
             run = false;
             System.out.println("RAUND "+round);
             for(int pos=0; pos<6; pos++){

                 if(preflop_by_positions.get(pos).size()<round+1)continue;
                 run = true;
                 float action = preflop_by_positions.get(pos).get(round);
                 //System.out.println("size "+preflop_by_positions.get(pos).size()+"  raund+1 "+(round+1)+" act "+action);
                 if(pos==poker_position_of_hero){
                     if(action==0||action==1_000_000)preflop_actions_for_stats.get(poker_position_of_hero).add(Float.NEGATIVE_INFINITY);
                     float current_size_raise = action-befor_action;
                     if(current_size_raise>=size_raise){preflop_actions_for_stats.get(pos).add(action); size_raise = current_size_raise;  befor_action = action;  continue;}
                     if(pos!=5&&action<2&&action>0)preflop_actions_for_stats.get(pos).add(-action);
                 } else {
                     if(action==1000000){preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);continue;}
                     float current_size_raise = action-befor_action;
                     if(current_size_raise>=size_raise){preflop_actions_for_stats.get(pos).add(action); size_raise = current_size_raise;  befor_action = action;continue;}
                     if(action!=0)preflop_actions_for_stats.get(pos).add(-action);
                 }

                 System.out.println("befor size "+preflop_by_positions.get(pos).size()+"  raund "+(round)+" run "+run);
             }
             System.out.println("run "+run);
         }





           for(int i=0; i<6; i++){
               System.out.print("pos "+i+" ");
               if(nicks[i]==null)continue;
               System.out.print(nicks[i]+"   ");
            for(int a=0; a<preflop_actions_for_stats.get(i).size(); a++)
                System.out.print(preflop_actions_for_stats.get(i).get(a)+" ");
            System.out.println();
        }


        System.out.println("===============================================");
        }



    }
}
