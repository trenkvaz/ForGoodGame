package org.trenkvaz.stats;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class Against3bet extends MainStats {


    private final HashMap<Integer,Integer[][][]> map_of_Idplayer_stats = new HashMap<>();
    private static final int select = 0, fold =1, _4bet = 2;
    private static final String[] action_query = new String[]{null,null,"fold","4bet"};
    public String[] getName_of_stat(){ return new String[]{"rfi_v_3bet","integer[][][]"}; }
    public HashMap<Integer,Integer[][][]> getMap_of_Idplayer_stats(){return map_of_Idplayer_stats;}

    public void setIdplayers_stats(Integer idplayer, Array statasql){
        if(statasql==null){
            Integer[][][] data = new Integer[5][6][3];
            for(int a=1; a<6; a++)
                for(int b=0; b<a; b++)
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

    public void count_Stats_for_map(byte[][][] actions_hand,int[] idplayers,float[]stacks,int idHero,byte Seaters,float[][][]posactions,boolean isAdditional){

        if(!isAdditional)add_player_to_map_test(idplayers);

        if(actions_hand[BB]==null||actions_hand[BB][raund_1][action]==0||actions_hand[BB][raund_1][indKolLimpers]>0
                ||actions_hand[BB][raund_1][indPozRaiser]==0||actions_hand[BB][raund_1][indKolCallers]>0)return;



        if(Seaters>2) {
            for(int pos_3beter=1; pos_3beter<6; pos_3beter++){

                if(actions_hand[pos_3beter]==null||actions_hand[pos_3beter][raund_1][action]!=_3BET)continue;

                for(int posAgainst_3bet = 0; posAgainst_3bet<pos_3beter; posAgainst_3bet++){
                    if(actions_hand[posAgainst_3bet]==null||actions_hand[posAgainst_3bet][raund_1][action]!=RAISE)continue;
                    if(actions_hand[posAgainst_3bet].length==1||actions_hand[posAgainst_3bet][raund_1][indKolCallers3beta]>0||actions_hand[posAgainst_3bet][raund_1][indPoz4betera]>0)break;
                    int idplayer = idplayers[posAgainst_3bet];
                    Integer[][][] stata = map_of_Idplayer_stats.get(idplayer);
                    stata[posAgainst_3bet][pos_3beter][select]++;
                    if(actions_hand[posAgainst_3bet][raund_2][action]==FOLD){ stata[posAgainst_3bet][pos_3beter][fold]++;}
                    if(actions_hand[posAgainst_3bet][raund_2][action]==_4BET){ stata[posAgainst_3bet][pos_3beter][_4bet]++;}
                    map_of_Idplayer_stats.put(idplayer,stata);
                    if(idplayers[posAgainst_3bet]!=idHero){
                        int idunknown = getUnknownId(stacks[posAgainst_3bet]);
                        Integer[][][] stata_unknown = map_of_Idplayer_stats.get(idunknown);
                        stata_unknown[posAgainst_3bet][pos_3beter][select]+=stata[posAgainst_3bet][pos_3beter][select];
                        stata_unknown[posAgainst_3bet][pos_3beter][fold]+=stata[posAgainst_3bet][pos_3beter][fold];
                        stata_unknown[posAgainst_3bet][pos_3beter][_4bet]+=stata[posAgainst_3bet][pos_3beter][_4bet];
                        map_of_Idplayer_stats.put(idunknown,stata_unknown);
                    }
                }
                break;
            }
        } else {

            if(actions_hand[BB][raund_1][action]!=_3BET)return;
            int idplayer = idplayers[SB];
            Integer[][][] stata = map_of_Idplayer_stats.get(idplayer);
            stata[0][0][select]++;
            if(actions_hand[SB][raund_2][action]==FOLD){ stata[0][0][fold]++;}
            if(actions_hand[SB][raund_2][action]==_4BET){ stata[0][0][_4bet]++;}

            map_of_Idplayer_stats.put(idplayer,stata);

            if(idplayers[SB]!=idHero){
                int idunknown = getUnknownId(stacks[SB]);
                Integer[][][] stata_unknown = map_of_Idplayer_stats.get(idunknown);
                stata_unknown[0][0][select]+=stata[0][0][select];
                stata_unknown[0][0][fold]+=stata[0][0][fold];
                stata_unknown[0][0][_4bet]+=stata[0][0][_4bet];
                map_of_Idplayer_stats.put(idunknown,stata_unknown);
            }
        }
    }

    private void add_player_to_map_test(int[] idplayers){
        for(int id:idplayers){
            if(id==0)continue;
            if(map_of_Idplayer_stats.get(id)==null){
                Integer[][][] data = new Integer[5][6][3];
                for(int a=1; a<6; a++)
                    for(int b=0; b<a; b++)
                        for (int i=0; i<3; i++) data[b][a][i]=0;
                for (int i=0; i<3; i++) data[0][0][i]=0;

                map_of_Idplayer_stats.put(id,data);
            }
        }
    }

    public static String get_rfi_v_3bet_for_SQL_query(String position_player,String name, String _0_or_position_hero,String select, String names_ranges){
        // пример fold_10B70%4bet_10B20
        // ПЕРЕПИСАТЬ ПОД ОДНУ СТАТУ ПО ИМЕНИ !!!!!!!!!!!!!!!!!!!!
        String[] stats = null;
        String[] name_range = names_ranges.split("%");
        String result = String.format("(rfi_v_3bet[%1$d][%2$d][1]"+select+" AND ",Arrays.asList(positions_for_query).indexOf(position_player),Arrays.asList(positions_for_query).indexOf(_0_or_position_hero));
        for(int i=0; i<name_range.length; i++){
            if(i>0)result+=" AND ";
            stats = name_range[i].split("_");
            String[] beetwen = stats[1].split("B");
            result+= String.format(" (rfi_v_3bet[%1$d][%2$d][%3$d]/ nullif(CAST(rfi_v_3bet[%1$d][%2$d][1] AS FLOAT)/100,0))  BETWEEN "+beetwen[0]+" AND "+beetwen[1]+" ",
                    Arrays.asList(positions_for_query).indexOf(position_player),Arrays.asList(positions_for_query).indexOf(_0_or_position_hero),Arrays.asList(action_query).indexOf(stats[0]));
        }
        result+=")";
        return result;
    }
}
