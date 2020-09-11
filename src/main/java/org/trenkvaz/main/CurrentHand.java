package org.trenkvaz.main;

import java.util.ArrayList;

public class CurrentHand {

    long number_hand = 0;
    String[] nicks = new String[6];
    boolean is_nicks_filled = false;
    boolean is_preflop_end = false;
    boolean is_start_flop = false;

    int position_of_bu = 0;
    int position_of_hero = -1;
    String cards_hero = "";
    float[] stacks = new float[6];
    //float[] first_round_preflop = new float[6];
    ArrayList<ArrayList<Float>> preflop_by_positions = new ArrayList<>(6);

    CurrentHand(long number_hand,float sb){
        this.number_hand = number_hand;
        /*first_round_preflop[4] = -sb;
        first_round_preflop[5] = -1;*/
        for(int i=0; i<6; i++){
            preflop_by_positions.add(new ArrayList<Float>());
            if(i<4)preflop_by_positions.get(i).add(0f);
            if(i==4)preflop_by_positions.get(i).add(-sb);
            if(i==5)preflop_by_positions.get(i).add(-1f);
        }
    }

    void setIs_nicks_filled(){
        for(int i=1; i<6; i++)if(nicks[i]==null){is_nicks_filled = false; return;}
        is_nicks_filled = true;

    }

   /* void setIs_stacks_and_1_raund_actions_filled(){
        for (int i=0; i<6; i++){
            if(stacks[i]==0){ is_preflop_end = false; return;}
        }
        boolean fold_to_bb = true;
        for(int i=0; i<5; i++){
            if(preflop_by_positions.get(i).get(0)==1_000_000) continue;
            fold_to_bb = false;
        }
        if(fold_to_bb){
            is_preflop_end = true; return;}


        for (int a=0; a<6; a++){
            if(!(preflop_by_positions.get(a).get(0)>0)){ is_preflop_end = false; return;}
        }
        is_preflop_end = true;
    }*/


}
