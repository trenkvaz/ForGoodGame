package org.trenkvaz.main;

import javafx.scene.paint.Color;
import org.trenkvaz.database_hands.ReadHistoryGetStats;
import org.trenkvaz.database_hands.Work_DataBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.Controller_main_window.controller_main_window;
//import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;


public class CurrentHand {

    int table;
    long time_hand;
    String[] nicks = new String[6];
    String[] cards_hero = {"",""};
    Float[] stacks = new Float[6];
    int poker_position_of_hero = -1;

    int[] poker_positions_by_pos_table_for_nicks;
    boolean is_nicks_filled = false;
    boolean is_preflop_end = false;
    boolean is_start_flop = false;
    boolean is_stacks_filled = false;
    int position_bu_on_table = 0;
    int[] arr_continue_player_flop;



    //float[] first_round_preflop = new float[6];
    ArrayList<ArrayList<Float>> preflop_by_positions = new ArrayList<>(6);

    List<List<Float>> preflop_actions_for_stats = new ArrayList<>(6);
    public record TempHand(long time_hand, short cards_hero, short position_hero, Float[] stacks, String[] nicks){}
    CreatingHUD creatingHUD;


    CurrentHand(int table1,CreatingHUD creatingHUD1){
        creatingHUD = creatingHUD1;
        creatingHUD.clear_MapStats();
        table = table1;
        for(int i=0; i<6; i++){
            preflop_by_positions.add(new ArrayList<Float>());
            if(i<4)preflop_by_positions.get(i).add(0f);
            if(i==4)preflop_by_positions.get(i).add(0.5f);
            if(i==5)preflop_by_positions.get(i).add(1f);
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

      controller_main_window.setMessage_work(currentHand.time_hand+"", Color.BLUE);
      //Integer[] idplayers = get_and_write_NewIdPlayersForNicks(currentHand.nicks);
      Work_DataBase.record_rec_to_TableTempHands(new TempHand(currentHand.time_hand,get_short_CardsHero(currentHand.cards_hero),(short)currentHand.poker_position_of_hero,currentHand.stacks,currentHand.nicks));
    /* for(int i=0; i<6; i++){if(nicks[i]==null)continue;
         System.out.println(nicks[i]+"  "+idplayers[i]);
     }*/

    }


    static short get_short_CardsHero(String[] cards_hero){ return (short)
            ((byte) Arrays.asList(Deck).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(Deck).indexOf(cards_hero[1])); }






    public boolean creat_ActionsInHandForCountStats(){

        for(int f=0; f<6; f++){
            preflop_actions_for_stats.add(new ArrayList<Float>()); preflop_actions_for_stats.get(f).add(0f);
            if(preflop_by_positions.get(f).size()==1)preflop_by_positions.get(f).add(0.0f);
        }

        boolean run = true;int count_round = 0; float befor_action =1; int number_raise = 1, last_max_raise_position = -1, max_raise = 0, folders_to_bb=0;
        boolean rules_1_round_limps = false, rules_1_round_raise = false;
        List<int[]> raunds = new ArrayList<>();
        while (true){ count_round++;
            int[] round_action = new int[6];
            for(int pos=0; pos<6; pos++){
                if(preflop_by_positions.get(pos).size()>=count_round+1){
                    float action = preflop_by_positions.get(pos).get(count_round);
                    //System.out.println(nicks[pos]+" p "+pos+" act "+action);
                    if(count_round==1&&poker_position_of_hero==5&&pos<5&&(action==0||action==-10))folders_to_bb++;
                    if(action==0){ continue;}
                    if(action==befor_action){ if(number_raise==1)rules_1_round_limps=true; round_action[pos]=-number_raise; continue;}
                    if(action==-10){round_action[pos] =-10; if(count_round==1){ preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);}continue; }
                    if(action>befor_action){    round_action[pos] = ++number_raise; befor_action = action;
                        // нужно знать кто рейзил последний херо или оппы, чтобы в многораундовой раздаче правильно расставить неотмеченные фолды
                        if(round_action[pos]>max_raise){max_raise = round_action[pos]; last_max_raise_position = pos;}

                      if(count_round==1)rules_1_round_raise=true;
                        //System.out.println(nicks[pos]+" p "+pos+" act "+action+" raise "+round_action[pos]);
                    }
                }
            }
            raunds.add(round_action);
            /*for(int a:round_action) System.out.print(a+" ");
            System.out.println();*/

            if(count_round==1&&((rules_1_round_limps&&number_raise>1)||number_raise>2))continue;
            if(count_round>2&&Arrays.stream(round_action).filter(c->c>0).count()>1) continue;


           break;
         }


       /* if(raunds.size()>1)
        for (int[] raund:raunds)
            for(int pos = 0; pos<6; pos++){
                if(raund[pos]>max_raise){max_raise = raund[pos]; last_max_raise_position = pos;}

            }*/




        befor_action =1; float size_raise = 1;
        if(raunds.size()==1){
            // проверка на все фолды до бб где херо
            //if(poker_position_of_hero==5&&raunds.get(0)[5]!=-10&&Arrays.stream(raunds.get(0)).filter(c->c==-10).count()==5){ return;}

            // проверка на лимпы до бб чтобы выставить чек бб херо может быть на любой позе главное не фолдить
            boolean limp_bank = false;
            if((rules_1_round_limps&&!rules_1_round_raise)){
                if(poker_position_of_hero==5||preflop_by_positions.get(poker_position_of_hero).get(1)==1)preflop_actions_for_stats.get(5).add(Float.POSITIVE_INFINITY);
                for(int pos=0; pos<5; pos++){
                    float action = preflop_by_positions.get(pos).get(1);
                    if(action==0&&pos==poker_position_of_hero){preflop_actions_for_stats.get(poker_position_of_hero).add(Float.NEGATIVE_INFINITY);break;}
                    if(action==-10){continue;}
                    if(action==1){ // игрок на сб лимпит размером меньше бб
                        preflop_actions_for_stats.get(pos).add(-(action-preflop_by_positions.get(pos).get(0)));
                    }
                }
              //  return;
                limp_bank = true;
            }
            // ситуация когда все фолдят до бб нужна чтобы избежать ситуации когда херо на бб и его выигрыш отмечается как рейз
            if(!limp_bank){ if(folders_to_bb==5)limp_bank=true; }

            if(!is_start_flop&&!limp_bank){
                // без флопа ситуация когда херо фолдит действия оппов в таком случае только до херо
                if(preflop_by_positions.get(poker_position_of_hero).get(1)==0||preflop_by_positions.get(poker_position_of_hero).get(1)==-10) {
                preflop_actions_for_stats.get(poker_position_of_hero).add(Float.NEGATIVE_INFINITY);
                for(int pos=0; pos<6; pos++){
                    if(pos==poker_position_of_hero)continue;
                    float action = preflop_by_positions.get(pos).get(1);
                    if(action==0||action==-10)continue;
                    // на основе шага рейза понять был рейз или кол если рейз то ставка также вносится
                    /*float current_size_raise = action-befor_action;
                    if(current_size_raise>=size_raise){preflop_actions_for_stats.get(pos).add(action); size_raise = current_size_raise;  befor_action = action;}*/
                    if(action>befor_action){preflop_actions_for_stats.get(pos).add(action);}
                    else {
                        // если был кол а он может быть и больше последней ставки но так как меньше шага рейза то считается за кол
                        // ставку кол записывается как действие минус уже вложенные деньги особенно на блайндах
                        preflop_actions_for_stats.get(pos).add(-(action-preflop_by_positions.get(pos).get(0)));
                    }
                    befor_action = action;
                }
             //   return;
            }
            // также без флопа но херо рейзит

                if(preflop_by_positions.get(poker_position_of_hero).get(1)>0){

                    for(int pos=0; pos<6; pos++){
                        //if(pos==poker_position_of_hero)continue;
                        float action = preflop_by_positions.get(pos).get(1);
                        if(action==-10)continue;
                        if(action==0){preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);continue;}
                        // на основе шага рейза понять был рейз или кол если рейз то ставка также вносится
                        //float current_size_raise = action-befor_action;
                        //System.out.println(nicks[pos]+"  "+current_size_raise+"  act "+action);
                        if(action>befor_action){preflop_actions_for_stats.get(pos).add(action);}
                        else {
                            // если был кол а он может быть и больше последней ставки но так как меньше шага рейза то считается за кол
                            // ставку кол записывается как действие минус уже вложенные деньги особенно на блайндах
                            preflop_actions_for_stats.get(pos).add(-(action-preflop_by_positions.get(pos).get(0)));
                        }
                        befor_action = action;
                    }
                }
            }


            if(is_start_flop&&!limp_bank){

                for(int pos=0; pos<6; pos++){
                    //if(pos==poker_position_of_hero)continue;
                    float action = preflop_by_positions.get(pos).get(1);
                    if(action==-10)continue;
                    if(action==0){ // если игрок не играет на флопе то отсутствие действия на префлопе трактуестя как фолд
                        if(arr_continue_player_flop[pos]==0) preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);
                        else {
                            // если игрок играет флоп то отсутствие действия на префлопе трактуется как кол
                            // также проверяется чтобы кол был в рамкам стека так как с флопом уже могут быть оллины и стек может быть меньше предидущего рейза
                            if(befor_action<=stacks[pos]) preflop_actions_for_stats.get(pos).add(-(befor_action-preflop_by_positions.get(pos).get(0)));
                            else preflop_actions_for_stats.get(pos).add(-(stacks[pos]-preflop_by_positions.get(pos).get(0)));
                        }
                        continue;
                    }
                    // на основе шага рейза понять был рейз или кол если рейз то ставка также вносится
                    //float current_size_raise = action-befor_action;
                    //System.out.println(nicks[pos]+"  "+current_size_raise+"  act "+action);
                    if(action>befor_action){preflop_actions_for_stats.get(pos).add(action);}
                    else {
                        // если был кол а он может быть и больше последней ставки но так как меньше шага рейза то считается за кол
                        // ставку кол записывается как действие минус уже вложенные деньги особенно на блайндах
                        preflop_actions_for_stats.get(pos).add(-(action-preflop_by_positions.get(pos).get(0)));
                    }
                    befor_action = action;
                }


                 //return true;
            }

        }
        else {


            if(!is_start_flop){



            outer_cycle: for(int raund =0; raund<raunds.size(); raund++)
                 inside_cycle: for(int pos=0; pos<6; pos++){
                        //if(pos==poker_position_of_hero)continue;
                        if(raunds.get(raund)[pos]==0){
                            for (int[] ints : raunds) if (ints[pos] == -10) continue inside_cycle;
                            // если у херо ноль в первом раунде то это автоматически значит фолд
                            if(pos==poker_position_of_hero&&raund==0){ preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);break outer_cycle;}
                            // если херо рейзил последний значит оппы должны были сфолдить, если не херо,то значит херо фолдит, а оппы без действия остаются пустыми
                            if(last_max_raise_position==poker_position_of_hero) preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);
                            else if(pos==poker_position_of_hero) {preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY); break outer_cycle; }
                            continue;
                        }
                        // проверка что последний рейз не является рейзом самого себя так как последний рейз может быть выигранной суммой а не рейзом
                        if(raund>0&&raunds.get(raund)[pos]>0){ if(raunds.get(raund)[pos]-raunds.get(raund-1)[pos]==1)continue; }
                        float action = preflop_by_positions.get(pos).get(raund+1);
                        if(action==-10){if(raund>0)preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY); continue;}
                        //if(action==0){ if(pos==poker_position_of_hero)preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);continue;}
                        // на основе шага рейза понять был рейз или кол если рейз то ставка также вносится
                        //float current_size_raise = action-befor_action;
                        //System.inside_cycle.println(nicks[pos]+"  "+current_size_raise+"  act "+action);
                        if(action>befor_action){preflop_actions_for_stats.get(pos).add(action);}
                        else {
                            // если был кол а он может быть и больше последней ставки но так как меньше шага рейза то считается за кол
                            // ставку кол записывается как действие минус уже вложенные деньги особенно на блайндах
                            preflop_actions_for_stats.get(pos).add(-(action-Math.abs(preflop_by_positions.get(pos).get(raund))));
                        }
                        befor_action = action;
                    }

            }
            else {


                for(int raund =0; raund<raunds.size(); raund++)
                  out:for(int pos=0; pos<6; pos++){
                        //if(pos==poker_position_of_hero)continue;

                        if(raunds.get(raund)[pos]==0){
                            // проверка сделал ли игрок на предидущих раундах фолд, чтобы обработать его на последнем раунде
                            for (int[] ints : raunds) if (ints[pos] == -10) continue out;
                            // если игрок не играет на флопе то отсутствие действия на префлопе трактуестя как фолд
                            if(arr_continue_player_flop[pos]==0) preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);
                            else {

                               // последний рейзящий пропускается его пустышка ноль не обрабатывается
                               /* if(last_max_raise_position==poker_position_of_hero){if(pos==poker_position_of_hero) continue;}
                                else if(last_max_raise_position==pos)continue;*/
                                if(last_max_raise_position==pos)continue;
                                // проверка что на предидущем раунде уже был кол последнего рейза
                                if(max_raise==Math.abs(raunds.get(raund-1)[pos]))continue;
                                // если игрок играет флоп то отсутствие действия на префлопе трактуется как кол
                                // также проверяется чтобы кол был в рамкам стека так как с флопом уже могут быть оллины и стек может быть меньше предидущего рейза
                                // кол равен разнице между рейзом или стеком и внесенными деньгами на предидущем раунде это рейз или кол, поэтому убирается минус
                                if(befor_action<=stacks[pos]) preflop_actions_for_stats.get(pos).add(-(befor_action-Math.abs(preflop_by_positions.get(pos).get(raund))));
                                else preflop_actions_for_stats.get(pos).add(-(stacks[pos]-Math.abs(preflop_by_positions.get(pos).get(raund))));
                            }
                            continue;
                        }

                        float action = preflop_by_positions.get(pos).get(raund+1);
                        if(action==-10){if(raund>0)preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY); continue;}
                        //if(action==0){ if(pos==poker_position_of_hero)preflop_actions_for_stats.get(pos).add(Float.NEGATIVE_INFINITY);continue;}
                        // на основе шага рейза понять был рейз или кол если рейз то ставка также вносится
                        //float current_size_raise = action-befor_action;
                        //System.out.println(nicks[pos]+"  "+current_size_raise+"  act "+action);
                        if(action>befor_action){preflop_actions_for_stats.get(pos).add(action);}
                        else {
                            // если был кол а он может быть и больше последней ставки но так как меньше шага рейза то считается за кол
                            // ставку кол записывается как действие минус уже вложенные деньги особенно на блайндах
                            preflop_actions_for_stats.get(pos).add(-(action-Math.abs(preflop_by_positions.get(pos).get(raund))));
                        }
                        befor_action = action;
                    }

                //return true;

            }





        }



        /*System.out.println("+++++++++++++++++++++++++++++++++++++");
        System.out.println("raunds "+raunds.size());
        for(int[] r:raunds){
            for(int a:r) System.out.print(a+" ");
            System.out.println();
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++");*/

    // Float[] to float[]
    float[] stacks = new float[6];
    for(int i=0; i<6; i++){
        if(this.stacks[i]==null)continue;
        stacks[i]=this.stacks[i];
    }

    ReadHistoryGetStats.count_StatsCurrentGame(current_map_stats, work_main_stats,nicks,stacks,preflop_actions_for_stats);
    return true;
    }
}
