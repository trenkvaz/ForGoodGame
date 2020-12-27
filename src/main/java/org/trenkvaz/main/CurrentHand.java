package org.trenkvaz.main;

import org.trenkvaz.database_hands.ReadHistoryGetStats;
import org.trenkvaz.database_hands.Work_DataBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OCR.*;
//import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;


public class CurrentHand {

    int testTable;
    long time_hand;
    String[] nicks = new String[6], cards_hero = {"",""};
    Float[] startStacks = new Float[6];


    int poker_position_of_hero = -1;

    int[] poker_positions_by_pos_table_for_nicks;
    boolean is_nicks_filled = false, //is_start_flop = false, is_start_turn = false, is_start_river = false,
            is_stacks_filled = false;


    int position_bu_on_table = 0;


    int streetAllIn = -1;
    int[] firstBetPostflopPokerPos = {-1,-1,-1,-1};
    int[] playersFoldOrAllIn = new int[6];
    boolean[] isFinishedStreets = {false,false,false,false};
    boolean[] isStartStreets =  {false,false,false,false};



    List<List<Float>> preflopActionsStats = new ArrayList<>(6);
    List<List<Float>> flopActionsStats = new ArrayList<>(6);

    public record TempHand(long time_hand, short cards_hero, short position_hero, Float[] stacks, String[] nicks){}
    CreatingHUD creatingHUD;
    //TEST
    OCR ocr;

    CurrentHand(OCR ocr){
        this.ocr = ocr;
        this.creatingHUD = ocr.creatingHUD;
        creatingHUD.clear_MapStats();
        testTable = ocr.table;
        for(int i=0; i<6; i++){
            preflopActionsStats.add(new ArrayList<>()); preflopActionsStats.get(i).add(0.0f);
            flopActionsStats.add(new ArrayList<>());
            startStacks[i] = 0f;
        }
        time_hand =  get_HandTime();
        position_bu_on_table = ocr.current_bu;
        nicks[0] = nick_hero;
        poker_position_of_hero = ocr.current_position_hero;
        cards_hero[0] = ocr.current_hero_cards[0];
        cards_hero[1] = ocr.current_hero_cards[1];
        poker_positions_by_pos_table_for_nicks = ocr.poker_positions_index_with_numbering_on_table.clone();
    }



    void setIs_nicks_filled(){
        creatingHUD.send_current_hand_to_creating_hud(nicks,poker_positions_by_pos_table_for_nicks,poker_position_of_hero);
        // проверка что все ники распознаны, чтобы не обращатся к методу распознавания ников
        for(int i=1; i<6; i++)if(nicks[i]==null){is_nicks_filled = false; return;}
        is_nicks_filled = true;
    }


    public void finalCurrendHand(){
        // расстановка ников по покерным позициям
        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            if(nicks[poker_positions_by_pos_table_for_nicks[i]-1]==null)continue;
            nicks_by_positions[i] = nicks[poker_positions_by_pos_table_for_nicks[i]-1];
        }
        nicks = nicks_by_positions;

        if(let_SaveTempHandsAndCountStatsCurrentGame){
            float[] stacks = new float[6];
            for(int i=0; i<6; i++){ stacks[i]=this.startStacks[i]; }
            ReadHistoryGetStats.count_StatsCurrentGame(current_map_stats, work_main_stats,nicks,stacks,preflopActionsStats);
            Work_DataBase.record_rec_to_TableTempHands(new TempHand(time_hand,get_short_CardsHero(cards_hero),(short)poker_position_of_hero, startStacks,nicks));
        }
    }



    static short get_short_CardsHero(String[] cards_hero){ return (short)
            ((byte) Arrays.asList(Deck).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(Deck).indexOf(cards_hero[1])); }




}
