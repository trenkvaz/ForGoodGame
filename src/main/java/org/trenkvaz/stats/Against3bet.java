package org.trenkvaz.stats;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class Against3bet extends MainStats {


    private final HashMap<String,Integer[][][]> map_of_Idplayer_stats = new HashMap<>();
    private static final int select = 0, fold =1, _4bet = 2;
    private static final String[] action_query = new String[]{null,null,"fold","4bet"};
    public String[] getName_of_stat(){ return new String[]{"rfi_v_3bet","integer[][][]"}; }
    public HashMap<String,Integer[][][]> getMap_of_Idplayer_stats(){return map_of_Idplayer_stats;}

    public void setIdplayers_stats(String nick, Array statasql){
        if(statasql==null){
            Integer[][][] data = new Integer[5][6][3];
            for(int a=1; a<6; a++)
                for(int b=0; b<a; b++)
                    for (int i=0; i<3; i++) data[b][a][i]=0;
            for (int i=0; i<3; i++) data[0][0][i]=0;
            map_of_Idplayer_stats.put(nick,data);
        } else {
            try {
                Integer[][][]    stata = (Integer[][][])statasql.getArray();
                map_of_Idplayer_stats.put(nick,stata);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void count_Stats_for_map(byte[][][] actions_hand,String[] nicks,float[]stacks,byte Seaters,float[][]posactions,boolean isAdditional){

        if(!isAdditional)add_player_to_map_test(nicks);

        if(actions_hand[BB]==null||actions_hand[BB][raund_1][action]==0||actions_hand[BB][raund_1][indKolLimpers]>0
                ||actions_hand[BB][raund_1][indPozRaiser]==0||actions_hand[BB][raund_1][indKolCallers]>0)return;



        if(Seaters>2) {
            for(int pos_3beter=1; pos_3beter<6; pos_3beter++){

                if(actions_hand[pos_3beter]==null||actions_hand[pos_3beter][raund_1][action]!=_3BET)continue;

                for(int posAgainst_3bet = 0; posAgainst_3bet<pos_3beter; posAgainst_3bet++){
                    if(actions_hand[posAgainst_3bet]==null||actions_hand[posAgainst_3bet][raund_1][action]!=RAISE)continue;
                    if(actions_hand[posAgainst_3bet].length==1||actions_hand[posAgainst_3bet][raund_1][indKolCallers3beta]>0||actions_hand[posAgainst_3bet][raund_1][indPoz4betera]>0)break;
                    Integer[][][] stata = map_of_Idplayer_stats.get(nicks[posAgainst_3bet]);
                    if(stata==null)continue;
                    stata[posAgainst_3bet][pos_3beter][select]++;
                    if(actions_hand[posAgainst_3bet][raund_2][action]==FOLD){ stata[posAgainst_3bet][pos_3beter][fold]++;}
                    if(actions_hand[posAgainst_3bet][raund_2][action]==_4BET){ stata[posAgainst_3bet][pos_3beter][_4bet]++;}
                    map_of_Idplayer_stats.put(nicks[posAgainst_3bet],stata);
                }
                break;
            }
        } else {

            if(actions_hand[BB][raund_1][action]!=_3BET)return;
            Integer[][][] stata = map_of_Idplayer_stats.get(nicks[SB]);
            if(stata==null)return;
            stata[0][0][select]++;
            if(actions_hand[SB][raund_2][action]==FOLD){ stata[0][0][fold]++;}
            if(actions_hand[SB][raund_2][action]==_4BET){ stata[0][0][_4bet]++;}

            map_of_Idplayer_stats.put(nicks[SB],stata);

        }
    }

    private void add_player_to_map_test(String[] idplayers){
        for(String id:idplayers){
            if(id==null)continue;
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
