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

        boolean run = true;int count_round = 0;float size_raise = 1; float befor_action =1; int number_raise = 1, limp = 0;
        boolean rules_1_round_limps = false, rules_1_round_raise = false;
        List<int[]> raunds = new ArrayList<>();
        while (true){ count_round++;
            int[] round_action = new int[6];
            for(int pos=0; pos<6; pos++){
                if(preflop_by_positions.get(pos).size()>=count_round+1){
                    float action = preflop_by_positions.get(pos).get(count_round);
                    //System.out.println(nicks[pos]+" p "+pos+" act "+action);
                    if(action==0)continue;
                    if(action==befor_action){ if(number_raise==1)rules_1_round_limps=true; round_action[pos]=-number_raise; continue;}
                    if(action==-10){round_action[pos] =-10; preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY); continue;}
                    if(action>befor_action){    round_action[pos] = ++number_raise; befor_action = action;
                      if(count_round==1)rules_1_round_raise=true;
                        //System.out.println(nicks[pos]+" p "+pos+" act "+action+" raise "+round_action[pos]);
                    }
                }
            }
            raunds.add(round_action);
           /* for(int a:round_action) System.out.print(a+" ");
            System.out.println();*/

            if(count_round==1&&((rules_1_round_limps&&number_raise>1)||number_raise>2))continue;
            if(count_round>2&&Arrays.stream(round_action).filter(c->c>0).count()>1) continue;


           break;
         }
        System.out.println("+++++++++++++++++++++++++++++++++++++");
        for(int[] r:raunds){
            for(int a:r) System.out.print(a+" ");
            System.out.println();
        }

        System.out.println("+++++++++++++++++++++++++++++++++++++");

        if(raunds.size()==1){
            // проверка на все фолды до бб где херо
            if(poker_position_of_hero==5&&raunds.get(0)[5]!=-10&&Arrays.stream(raunds.get(0)).filter(c->c==-10).count()==5){ return;}
            // проверка на лимпы до бб чтобы выставить чек бб херо может быть на любой позе главное не фолдить
            if((rules_1_round_limps&&!rules_1_round_raise)&&(poker_position_of_hero==5||preflop_by_positions.get(poker_position_of_hero).get(1)!=-10)){
                preflop_actions_for_stats.get(5).add(Float.POSITIVE_INFINITY);
                for(int pos=0; pos<5; pos++){
                    float action = preflop_by_positions.get(pos).get(1);
                    if(action==-10){continue;}
                    if(action==1){ // игрок на сб лимпит размером меньше бб
                        if(pos==4) preflop_actions_for_stats.get(pos).add(action-preflop_by_positions.get(pos).get(0));
                    }
                }
                return;
            }
            befor_action =1;
            if(!is_start_flop){if(preflop_by_positions.get(poker_position_of_hero).get(1)==0||preflop_by_positions.get(poker_position_of_hero).get(1)==-10) {
                preflop_actions_for_stats.get(poker_position_of_hero).add(Float.POSITIVE_INFINITY);
                for(int pos=0; pos<5; pos++){
                    if(pos==poker_position_of_hero)continue;
                    float action = preflop_by_positions.get(pos).get(1);
                    if(action==0||action==-10)continue;
                    // на основе шага рейза понять был рейз или кол если рейз то ставка также вносится
                    float current_size_raise = action-befor_action;
                    if(current_size_raise>=size_raise){preflop_actions_for_stats.get(pos).add(action); size_raise = current_size_raise;  befor_action = action;}
                    else {
                        // если был кол а он может быть и больше последней ставки но так как меньше шага рейза то считается за кол
                        // ставку кол записывается как действие минус уже вложенные деньги особенно на блайндах
                        preflop_actions_for_stats.get(pos).add(-(action-preflop_by_positions.get(pos).get(0))); befor_action = action;
                    }
                }
            }



            }




        }




        for(int[] raund:raunds)
            for(int pos=0; pos<6; pos++){


            }






/*
        if(!is_start_flop){
               System.err.println("START count");

            // предидущие действия по позам фолд 0
            // ставка в позу игрока и если ставка выше по индексу 6 то туда
            float[] type_action_befor = new float[7];
           count_round = 0; run = true;
         while(run){ count_round++;
             run = false;
             System.out.println("RAUND "+count_round);
             for(int pos=0; pos<6; pos++){

                 if(preflop_by_positions.get(pos).size()<count_round+1)continue;
                 run = true;
                 float action = preflop_by_positions.get(pos).get(count_round);
                 //System.out.println("size "+preflop_by_positions.get(pos).size()+"  raund+1 "+(count_round+1)+" act "+action);

                     if(action==1_000_000){preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);
                     if(pos==poker_position_of_hero){run=false; break;}
                     else continue;
                     }
                     if(type_action_befor[6]==0&&pos==5){  run=false; break;  }
                     if(action==0){
                         // проверка выставленного нуля в последнем раунде на предмет равно ли последнее действие игрока последнему большому действию
                         // пример был рейз и выставлен нуль в следующий раунд
                         if(type_action_befor[6]==type_action_befor[pos]){run=false; break;}
                         else { preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);  if(pos==poker_position_of_hero){run=false; break;}
                         else continue;
                         }
                     }
                     else { // если есть действие в последнем раунде то также проверяется равно ли последнее действие игрока последнему большому действию
                         // если эти действия равны то значит действия не должно быть оно появилось из выигрыша, его не нужно отмечать как действие
                         if(type_action_befor[6]==type_action_befor[pos]){run=false; break;}
                     }
                     // рассчитывается шаг рейза если он равен или больше предидущему, то это считается рейзом и в таком виде заносится в префлопдействия игрока
                     // шага рейза и действие до обновляется
                     float current_size_raise = action-befor_action;
                     if(current_size_raise>=size_raise){preflop_actions_for_stats.get(pos).add(action); size_raise = current_size_raise;  befor_action = action;
                     // текущиее действие по позиции обновляется, если это действие больше записанного в инд 6 то идет туда
                     type_action_befor[pos]= action; if(action> type_action_befor[6]) type_action_befor[6] = action;
                     // проверка на то что ставка меньше стека, и что нет следующего раунда чтобы внести в следующий раунд 0
                         // проверка стека нужна для того что при оллине игрок уже не может больше совершать действия
                     if(action<stacks[pos]&&preflop_by_positions.get(pos).size()<count_round+2)preflop_by_positions.get(pos).add(0f);
                     continue;
                     }
                     else { preflop_actions_for_stats.get(pos).add(-(action-Math.abs(preflop_by_positions.get(pos).get(count_round-1)))); befor_action = action;
                     if(type_action_befor[6]==0){
                         type_action_befor[pos] = -1;
                     } else type_action_befor[pos] = -type_action_befor[6];
                         preflop_by_positions.get(pos).add(0f);
                     }


                 System.out.println("befor size "+preflop_by_positions.get(pos).size()+"  raund "+(count_round)+" run "+run);
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
        }*/



    }
}
