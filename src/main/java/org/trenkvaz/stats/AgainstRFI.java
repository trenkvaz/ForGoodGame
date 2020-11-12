package org.trenkvaz.stats;

import java.sql.Array;
import java.sql.SQLException;
import java.util.HashMap;

public class AgainstRFI extends MainStats {


    private static final String[] v_rfi = {"v_rfi","integer[][][]"};
    private static final int select=0, fold=0,_3bet = 2;
    private final HashMap<Integer,Integer[][][]> map_of_Idplayer_stats = new HashMap<>();

 //19,39,59,79,99,150,200

    public String[] getName_of_stat(){ return v_rfi; }

    public HashMap<Integer,Integer[][][]> getMap_of_Idplayer_stats(){return map_of_Idplayer_stats;}

    public void setIdplayers_stats(Integer idplayer, Array statasql){
        if(statasql==null){
            Integer[][][] data = new Integer[6][5][3];
            for(int a=0; a<5; a++)
                for(int b=a+1; b<6; b++)
                    for (int i=0; i<3; i++) data[b][a][i]=0;
            for (int i=0; i<3; i++) data[0][0][i]=0;
            map_of_Idplayer_stats.put(idplayer,data);
        } else {
            try {
                Integer[][][]    stata = (Integer[][][])statasql.getArray();
                map_of_Idplayer_stats.put(idplayer,stata);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            }
    }



    public void count_Stats_for_map(byte[][][] actions_hand,int[] idplayers,float[]stacks,int idHero,byte Seaters,float[][]posactions,boolean isAdditional){
        //if(idhero==0)idhero=idHero;
        if(!isAdditional)add_player_to_map_test(idplayers);

        if(actions_hand[BB]==null||actions_hand[BB][raund_1][action]==0||actions_hand[BB][raund_1][indKolLimpers]>0)return;

        if(Seaters>2) {
        for(int posRFI=0; posRFI<5; posRFI++){
            //проверка наличия позы открытия и рейза
            if(actions_hand[posRFI]==null||actions_hand[posRFI][raund_1][action]!=RAISE)continue;
            // если есть то перебираются все позы против открытия
            for(int posAgainstRFI = 1+posRFI; posAgainstRFI<6; posAgainstRFI++){
                if(actions_hand[posAgainstRFI][raund_1][indKolCallers]>0)break;
                int idplayer = idplayers[posAgainstRFI];
                Integer[][][] stata = map_of_Idplayer_stats.get(idplayer);
                stata[posAgainstRFI][posRFI][select]++;
                if(actions_hand[posAgainstRFI][raund_1][action]==FOLD){ stata[posAgainstRFI][posRFI][fold]++;}
                if(actions_hand[posAgainstRFI][raund_1][action]==_3BET){ stata[posAgainstRFI][posRFI][_3bet]++;}
                map_of_Idplayer_stats.put(idplayer,stata);

            }
            break;
        }
        } else {

            if(actions_hand[4][raund_1][action]!=RAISE)return;
            int idplayer = idplayers[5];
            Integer[][][] stata = map_of_Idplayer_stats.get(idplayer);
            stata[0][0][select]++;
            if(actions_hand[5][raund_1][action]==FOLD){ stata[0][0][fold]++;}
            if(actions_hand[5][raund_1][action]==_3BET){ stata[0][0][_3bet]++;}
            //if(actions_hand[5][raund_1][action]==CALL){break;}
            map_of_Idplayer_stats.put(idplayer,stata);

        }
    }

    private void add_player_to_map_test(int[] idplayers){
        for(int id:idplayers){
            if(id==0)continue;
            if(map_of_Idplayer_stats.get(id)==null){
                Integer[][][] data = new Integer[6][5][3];
                for(int a=0; a<5; a++)
                    for(int b=a+1; b<6; b++)
                        for (int i=0; i<3; i++) data[b][a][i]=0;
                for (int i=0; i<3; i++) data[0][0][i]=0;

                map_of_Idplayer_stats.put(id,data);
            }
        }
    }



    public static void main(String[] args) {


    }
}
