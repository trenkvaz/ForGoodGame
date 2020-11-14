package org.trenkvaz.stats;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class RFI extends MainStats {

    private final HashMap<String,Integer[][]> map_of_Idplayer_stats = new HashMap<>();
    public String[] getName_of_stat(){ return new String[]{"rfi","integer[][]"}; }
    public HashMap<String,Integer[][]> getMap_of_Idplayer_stats(){return map_of_Idplayer_stats;}

    public void setIdplayers_stats(String idplayer, Array statasql){
        if(statasql==null){
            Integer[][] data = new Integer[5][2];
            for (int i=0; i<5; i++)
                for (int a=0; a<2; a++) data[i][a]=0;
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

    private void add_player_to_map_test(String[] idplayers){
        for(String id:idplayers){
            if(id==null)continue;
            if(!map_of_Idplayer_stats.containsKey(id)){
                Integer[][] data = new Integer[5][2];
                for (int i=0; i<5; i++)
                    for (int a=0; a<2; a++) data[i][a]=0;
                map_of_Idplayer_stats.put(id,data);
            }
        }
    }

    public void count_Stats_for_map(byte[][][] actions_hand,String[] idplayers,float[]stacks,byte Seaters,float[][]posactions,boolean isAdditional){
        //if(idhero==0)idhero=idHero;
        if(Seaters==2)return;
        if(!isAdditional)add_player_to_map_test(idplayers);

        for(int pos=0; pos<5; pos++){
            if(actions_hand[pos]==null||actions_hand[pos][raund_1][action]==0)continue;
            if(actions_hand[pos][raund_1][indPoz1Limpera]>0)break;

            Integer[][] stata = map_of_Idplayer_stats.get(idplayers[pos]);
            if(stata==null)continue;
            stata[pos][0]++;
            if(actions_hand[pos][raund_1][action]==RAISE)stata[pos][1]++;
            map_of_Idplayer_stats.put(idplayers[pos],stata);
            if(actions_hand[pos][raund_1][action]==FOLD)continue;
            break;
        }
    }

    public static String get_rfi_stata_SQL_query(String position, String select, String range_stata){

        String[] beetwen = range_stata.split("B");
        return String.format("(rfi[%1$d][1]"+select+
                        " AND (rfi[%1$d][2]/ nullif(CAST(rfi[%1$d][1] AS FLOAT)/100,0))  BETWEEN "+beetwen[0]+" AND "+beetwen[1]+")",
                Arrays.asList(positions_for_query).indexOf(position));

    }

}
