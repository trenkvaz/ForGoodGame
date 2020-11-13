package org.trenkvaz.stats;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class VpipPFR3bet extends MainStats {


    private static final int select_vpip_pfr = 0, vpip= 1, pfr= 2, select_3bet = 3, _3bet = 4;
    private final HashMap<String,Integer[][]> map_of_Idplayer_stats = new HashMap<>();
    public String[] getName_of_stat(){ return new String[]{"vpip_pfr_3bet","integer[][]"}; }
    public HashMap<String,Integer[][]> getMap_of_Idplayer_stats(){return map_of_Idplayer_stats;}

    public void setIdplayers_stats(String idplayer, Array statasql){
        // от 0-5 позиционные статы 6 объедененные статы
        if(statasql==null){
            Integer[][] data = new Integer[7][5];
            for (int p=0; p<7; p++)
            for (int i=0; i<5; i++) data[p][i]=0;
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
            if(map_of_Idplayer_stats.get(id)==null){
                Integer[][] data = new Integer[7][5];
                for (int p=0; p<7; p++)
                    for (int i=0; i<5; i++) data[p][i]=0;
                map_of_Idplayer_stats.put(id,data);
            }
        }
    }

    public void count_Stats_for_map(byte[][][] actions_hand,String[] idplayers,float[]stacks,byte Seaters,float[][]posactions,boolean isAdditional){
        //if(idhero==0)idhero=idHero;
        if(Seaters==2)return;
        if(!isAdditional)add_player_to_map_test(idplayers);

        for(int pos=0; pos<6; pos++){

             if(actions_hand[pos]==null||actions_hand[pos][raund_1][action]==0)continue;
             /*for(int p=0; p<6; p++){
                 System.out.print(" "+p);
                 for (byte [] r:actions_hand[p]){if(r==null){
                     System.out.println("null r");continue;}
                    for(byte a:r) System.out.print(" "+a);
                     System.out.println();
                 }
             }
             for(int i:idplayers) System.out.println(" "+i);*/

             Integer[][] stata = map_of_Idplayer_stats.get(idplayers[pos]);
             if(stata==null)continue;
            /*System.out.println("id "+idplayer);
            for(Integer[] A:stata){
                if(A==null){
                    System.out.println(" null "); continue;
                }
                for(Integer B:A) System.out.println(" "+B);
            }*/
                    stata[pos][select_vpip_pfr]++; stata[6][select_vpip_pfr]++;
                    if(actions_hand[pos][raund_1][indPozRaiser]>0&&actions_hand[pos][raund_1][indPoz3beter]==0)
                    {stata[pos][select_3bet]++;stata[6][select_3bet]++; if(actions_hand[pos][raund_1][action]==_3BET) {stata[pos][_3bet]++;stata[6][_3bet]++;}}
                    if(actions_hand[pos][raund_1][action]!=FOLD&&actions_hand[pos][raund_1][action]!=CHEK){stata[pos][vpip]++;stata[6][vpip]++;}
                    if(actions_hand[pos][raund_1][action]>0&&actions_hand[pos][raund_1][action]<10){stata[pos][pfr]++;stata[6][pfr]++;}

             map_of_Idplayer_stats.put(idplayers[pos],stata);
        }
    }

    public static String get_rfi_v_3bet_for_SQL_query(String position_player, String name, String select, String range, String _0_or_position_hero){
        // если _0_or_position_hero =0 то общая стата index = 7;
        // select >10
        // range 20B30

        int position_player_sql = 7;
        if(!_0_or_position_hero.equals("0")) position_player_sql =Arrays.asList(positions_for_query).indexOf(position_player);
        String[] beetwen = range.split("B");
        String result = "";
            if(name.equals("vpip")||name.equals("pfr"))
                result += String.format("(vpip_pfr_3bet[%1$d][%2$d]"+select, position_player_sql,1);
            if(name.equals("3bet")) result += String.format("(vpip_pfr_3bet[%1$d][%2$d]"+select, position_player_sql,4);
            result +=" AND ";
            if(name.equals("vpip"))
                result +=String.format(" (vpip_pfr_3bet[%1$d][%2$d]/ nullif(CAST(vpip_pfr_3bet[%1$d][%3$d] AS FLOAT)/100,0))  " +
                        "BETWEEN "+beetwen[0]+" AND "+beetwen[1],position_player_sql,2,1);
            if(name.equals("pfr"))
            result +=String.format(" (vpip_pfr_3bet[%1$d][%2$d]/ nullif(CAST(vpip_pfr_3bet[%1$d][%3$d] AS FLOAT)/100,0))  " +
                    "BETWEEN "+beetwen[0]+" AND "+beetwen[1],position_player_sql,3,1);
            if(name.equals("3bet"))
            result +=String.format(" (vpip_pfr_3bet[%1$d][%2$d]/ nullif(CAST(vpip_pfr_3bet[%1$d][%3$d] AS FLOAT)/100,0))  " +
                    "BETWEEN "+beetwen[0]+" AND "+beetwen[1],position_player_sql,5,4);

        result+=")";
        return result;
    }
}
