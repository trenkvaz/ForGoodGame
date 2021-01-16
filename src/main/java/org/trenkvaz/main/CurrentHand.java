package org.trenkvaz.main;

import org.trenkvaz.database_hands.ReadHistoryGetStats;
import org.trenkvaz.database_hands.Work_DataBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;
//import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;


public class CurrentHand {

    int testTable;
    long time_hand;
    String[] nicks = new String[6], cards_hero = {"",""};
    Float[] startStacks = new Float[6];
    float[][] startStacksAtStreets = {new float[6],new float[6],new float[6],new float[6],new float[6]};


    int pokerPosHero = -1;

    //int[] poker_positions_by_pos_table_for_nicks;
    boolean is_nicks_filled = false, is_stacks_filled = false;


    int position_bu_on_table = 0;


    int streetAllIn = -1;
    int[] firstBetPostflopPokerPos = {-1,-1,-1,-1};
    int[] playersFoldOrAllIn = new int[6];
    boolean[] isFinishedStreets = {false,false,false,false};
    boolean[] isStartStreets =  {false,false,false,false,false};
    int startAmountPlayers = 0;

    float[] resultsAllin = new float[6];
    float[] winLosePlayers = new float[6];

    List<List<Float>> preflopActionsStats = new ArrayList<>(6);
    List<List<Float>> flopActionsStats = new ArrayList<>(6);
    List<List<Float>> turnActionsStats = new ArrayList<>(6);
    List<List<Float>> riverActionsStats = new ArrayList<>(6);

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
            flopActionsStats.add(new ArrayList<>());turnActionsStats.add(new ArrayList<>());riverActionsStats.add(new ArrayList<>());
            startStacks[i] = 0f;
        }
        time_hand =  get_HandTime();
        position_bu_on_table = ocr.current_bu;
        nicks[0] = NICK_HERO;
        cards_hero[0] = ocr.current_hero_cards[0];
        cards_hero[1] = ocr.current_hero_cards[1];
        //poker_positions_by_pos_table_for_nicks = ocr.pokerPosIndWithNumOnTable.clone();
    }



    void setIs_nicks_filled(){
        creatingHUD.send_current_hand_to_creating_hud(nicks,ocr.pokerPosIndWithNumOnTable, pokerPosHero);
        // проверка что все ники распознаны, чтобы не обращатся к методу распознавания ников
        for(int i=1; i<6; i++){
            if(ocr.frameTable.whoPlayOrNo()[i]==0)continue;
            if(nicks[i]==null){is_nicks_filled = false; return;}
        }
        is_nicks_filled = true;
    }


    public void finalCurrendHand(){
        // расстановка ников по покерным позициям
        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            if(ocr.pokerPosIndWithNumOnTable[i]==0)continue;
            if(nicks[ocr.pokerPosIndWithNumOnTable[i]-1]==null)continue;
            nicks_by_positions[i] = nicks[ocr.pokerPosIndWithNumOnTable[i]-1];
        }
        nicks = nicks_by_positions;
        countResultHand();
        if(let_SaveTempHandsAndCountStatsCurrentGame){
            float[] stacks = new float[6];
            for(int i=0; i<6; i++){ stacks[i]=this.startStacks[i]; }
            ReadHistoryGetStats.count_StatsCurrentGame(current_map_stats, work_main_stats,nicks,stacks,preflopActionsStats,startAmountPlayers);
            Work_DataBase.record_rec_to_TableTempHands(new TempHand(time_hand,get_short_CardsHero(cards_hero),(short) pokerPosHero, startStacks,nicks));
        }
    }



    static short get_short_CardsHero(String[] cards_hero){ return (short)
            ((byte) Arrays.asList(DECK).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(DECK).indexOf(cards_hero[1])); }


    private void countResultHand(){
        if(streetAllIn!=-1){
            for(int pokerPos =0; pokerPos<6; pokerPos++){
                if(ocr.curActsOrInvests[pokerPos]==-10||ocr.pokerPosIndWithNumOnTable[pokerPos]==0)continue;
                float invest = startStacks[pokerPos];
                if(ocr.curActsOrInvests[pokerPos]!=-100)invest = ocr.curActsOrInvests[pokerPos];
                winLosePlayers[pokerPos] = resultsAllin[pokerPos]-invest;
            }
        }


    }

}
