package org.trenkvaz.stats;

import java.sql.Array;
import java.sql.SQLException;
import java.util.HashMap;

public class Alliners extends MainStats {


    private static final int select_rfi = 0, rfi_all= 1, select_3bet= 2, _3bet_all = 3, stack30 =0, stack70 =1, stack100 = 2;
    private final HashMap<Integer,Integer[][]> map_of_Idplayer_stats = new HashMap<>();
    public String[] getName_of_stat(){ return new String[]{"alliners","integer[][]"}; }

    public HashMap<Integer,Integer[][]> getMap_of_Idplayer_stats(){return map_of_Idplayer_stats;}

    public void setIdplayers_stats(Integer idplayer, Array statasql){
        if(statasql==null){
            Integer[][] data = new Integer[3][4];
            for (int p=0; p<3; p++)
                for (int i=0; i<4; i++) data[p][i]=0;
            map_of_Idplayer_stats.put(idplayer,data);
        } else {
            try {
                Integer[][] stata = (Integer[][])statasql.getArray();
                map_of_Idplayer_stats.put(idplayer,stata);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void add_player_to_map_test(int[] idplayers){
        for(int id:idplayers){
            if(id==0)continue;
            if(map_of_Idplayer_stats.get(id)==null){
                Integer[][] data = new Integer[3][4];
                for (int p=0; p<3; p++)
                    for (int i=0; i<4; i++) data[p][i]=0;
                map_of_Idplayer_stats.put(id,data);
            }
        }
    }

    public void count_Stats_for_map(byte[][][] actions_hand,int[] idplayers,float[]stacks,int idHero,byte Seaters,float[][]posactions,boolean isAdditional){
        //if(idhero==0)idhero=idHero;
        if(Seaters==2)return;
        if(!isAdditional)add_player_to_map_test(idplayers);

        for(int pos=0; pos<6; pos++){

            if(actions_hand[pos]==null||actions_hand[pos][raund_1][action]==0||stacks[pos]<14)continue;
            if(actions_hand[pos][raund_1][indPoz3beter]>0)break;

            int idplayer = idplayers[pos];
            //if(idplayer!=269)return;
            Integer[][] stata = map_of_Idplayer_stats.get(idplayer);
            //System.out.println("player "+idplayer);
            if(actions_hand[pos][raund_1][indPozRaiser]==0){
                if(stacks[pos]<35||stacks[pos]==35){ stata[stack30][select_rfi]++; if(actions_hand[pos][raund_1][action]==RAISE){
                    if(posactions[pos][0]+posactions[pos][1]==stacks[pos]) stata[stack30][rfi_all]++;} }
                if((stacks[pos]<70||stacks[pos]==70)&&stacks[pos]>35){ stata[stack70][select_rfi]++; if(actions_hand[pos][raund_1][action]==RAISE){
                    if(posactions[pos][0]+posactions[pos][1]==stacks[pos]) stata[stack70][rfi_all]++;} }
                if(stacks[pos]>70){ stata[stack100][select_rfi]++; if(actions_hand[pos][raund_1][action]==RAISE){
                    if(posactions[pos][0]+posactions[pos][1]==stacks[pos]) stata[stack100][rfi_all]++;} }
            }
            else {
                if(stacks[pos]<35||stacks[pos]==35){ stata[stack30][select_3bet]++;
                if(actions_hand[pos][raund_1][action]==_3BET){ if(posactions[pos][1]==stacks[pos]) { stata[stack30][_3bet_all]++;}}
                }
                if((stacks[pos]<70||stacks[pos]==70)&&stacks[pos]>35){ stata[stack70][select_3bet]++; if(actions_hand[pos][raund_1][action]==_3BET){
                    if(posactions[pos][1]==stacks[pos]) { stata[stack70][_3bet_all]++;}}
                }
                if(stacks[pos]>70){ stata[stack100][select_3bet]++; if(actions_hand[pos][raund_1][action]==_3BET){
                    if(posactions[pos][1]==stacks[pos]) { stata[stack100][_3bet_all]++;} }
                }
            }
            map_of_Idplayer_stats.put(idplayer,stata);
        }
    }

}
