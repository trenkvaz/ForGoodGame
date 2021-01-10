package org.trenkvaz.database_hands;

import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.CurrentHand;
import org.trenkvaz.stats.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;


import static org.trenkvaz.database_hands.GetNicksForHands.get_str_Cards;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class Work_DataBase {

static final String DB_SERVER = "jdbc:postgresql://127.0.0.1:5433/", USER = "postgres", PASS = "admin", BEGIN = "BEGIN;",COMMIT = "COMMIT;";
static Connection connect_to_db, connect_to_server;
static Statement stmt_of_db, stmt_of_server;
public static MainStats[] main_array_of_stats = new MainStats[]{new AgainstRFI(),new Against3bet(),new VpipPFR3bet(),new RFI(),new Alliners()};
static String work_database;

    public Work_DataBase(){
        connect_ToServer();
        init_Working_database();
    }

    static void connect_ToServer(){

        try {
            Class.forName("org.postgresql.Driver");
            connect_to_server = DriverManager.getConnection(DB_SERVER, USER, PASS);
            stmt_of_server = connect_to_server.createStatement();
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
        } catch (SQLException throwables) {
            System.out.println("Failed connect Server");
            throwables.printStackTrace();
        }
        System.out.println("PostgreSQL JDBC Driver successfully connected");

    }




    private void init_Working_database(){
        /* получение имени базы из файла
           проверка наличия базы по имени, если есть, то подключение к базе
           если нет, создание базы с полученным именем, подключение к базе, создание таблиц, отключение от сервера*/
        try {
            BufferedReader br = new BufferedReader(new FileReader(home_folder+"\\all_settings\\database\\name_db.txt"));
            String name_database;
            while ((name_database = br.readLine())!=null){
                if(name_database.endsWith("W")) {work_database = name_database.split(" ")[0]; break;}
            }
            if(work_database==null)work_database = "fg_empty_test_db" ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT datname FROM pg_database;";
            ResultSet databases = stmt_of_server.executeQuery(sql);
            boolean database_exists = false;
            while (databases.next()){
                String databaseName = databases.getString("datname");
                System.out.println("*"+databaseName+"*");
                if (databaseName.equals(work_database)){
                    database_exists = true;
                    break;}
            }

            if(database_exists){
                try {
                    connect_to_db = DriverManager.getConnection(DB_SERVER+work_database, USER, PASS);
                    stmt_of_db = connect_to_db.createStatement();
                    if(stmt_of_db!=null){
                        System.out.println("Connect "+work_database+" DB is successfully...");
                    }
                } catch (SQLException e) {
                    System.out.println("Connection LOCAL DB Failed");
                    e.printStackTrace();
                    return;
                }

            } else {
                System.out.println("Creating database...");
                String sql_creat_db = "CREATE DATABASE \""+work_database+"\"";
                try {
                    stmt_of_server.executeUpdate(sql_creat_db);
                    try {
                        connect_to_db = DriverManager.getConnection(DB_SERVER+work_database, USER, PASS);
                        stmt_of_db = connect_to_db.createStatement();
                        if(stmt_of_db!=null){
                            System.out.println("Connect to created "+work_database+" DB is successfully...");
                            create_Tables();
                        }
                        delete_and_copy_WorkNicksStats();
                    } catch (SQLException e) {
                        System.out.println("Connection LOCAL DB Failed");
                        e.printStackTrace();
                        return;
                    }
                } catch (SQLException e) {
                    System.out.println("CREATE GLOBAL DATABASE Failed");
                    e.printStackTrace();
                }

            }

            if(stmt_of_server!=null) stmt_of_server.close();
            if(connect_to_server!=null)connect_to_server.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



    private void create_Tables(){
        System.out.println("creat tables");
        //String createtable_Hands = "CREATE TABLE "+NameOfTable+" ( "+getStructureTable()+" );";
        /*String createtable_idplayers_stats = "CREATE TABLE idplayers_stats (idplayers integer PRIMARY KEY);";
        String createtable_idplayers_nicks = "CREATE TABLE idplayers_nicks (idplayers integer PRIMARY KEY, nicks text );";*/
        String main_nicks_stats = "CREATE TABLE main_nicks_stats (nicks text UNIQUE );";
        String createtable_temphands = "CREATE TABLE temphands ( time_hand bigint PRIMARY KEY, cards_hero smallint, position_hero smallint, stacks float4[], nicks text[] );";
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            stmt_of_db.executeUpdate(main_nicks_stats);
            stmt_of_db.executeUpdate(createtable_temphands);
            //stmt_of_db.executeUpdate(COMMIT);
            System.out.println(" sozdana tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        add_columns_in_TableIdplayersStats();
    }


    private static void add_columns_in_TableIdplayersStats(){
        // добавление колонок со статами в таблицу idplayers_stats
        // названия и типа колонок берутся из находящихся в массиве Объектов класса МайнСтатс

        String query = "SELECT column_name FROM information_schema.columns WHERE table_name =  'main_nicks_stats' ";
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            ResultSet rs = stmt_of_db.executeQuery(query);
            ArrayList<String> colomns = new ArrayList<>();
            while (rs.next()) {
                String colomn = rs.getString("column_name");
                System.out.println(colomn);
                colomns.add(colomn);
            }
            //stmt_of_db.executeUpdate(COMMIT);
            System.out.println("no stats: ");
            String adding = null;
            for(MainStats stata: main_array_of_stats){
                String[] str_stata = stata.getName_of_stat();
                if(colomns.contains(str_stata[0]))continue;
                System.out.println(str_stata[0]);
                adding = "ALTER TABLE main_nicks_stats ADD COLUMN "+str_stata[0]+" "+str_stata[1]+" ;";
                stmt_of_db.addBatch(adding);
            }
            stmt_of_db.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MainStats[] fill_MainArrayOfStatsFromDateBase(String table){

        String query = "SELECT * FROM "+table+" ;";
        MainStats[] mainStats = new MainStats[]{new AgainstRFI(),new Against3bet(),new VpipPFR3bet(),new RFI(),new Alliners()};
        try {
            stmt_of_db.executeUpdate(BEGIN);
            PreparedStatement ps = connect_to_db.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            //ResultSet rs = stmt_of_db.executeQuery(query);
            String nick = null;
            while(rs.next()) {
                //System.out.println(rs.getInt("idplayers"));
                nick = rs.getString(1);
                for(int i=0; i<mainStats.length; i++){
                    Array statasql = rs.getArray(i+2);
                    mainStats[i].setIdplayers_stats(nick,statasql);
                }
            }

         stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(" read stats");
        return mainStats;
    }


    public static void record_MainArrayOfStatsToDateBase(MainStats[] mainstats){
        long start = System.currentTimeMillis();
        int b =1;
        try {
            connect_to_db.setAutoCommit(false);
            StringBuilder insert = new StringBuilder("INSERT INTO main_nicks_stats VALUES ( ");

            StringBuilder update = new StringBuilder(" ON CONFLICT (nicks) DO UPDATE SET  ");
            int count_stats = mainstats.length;
            for(int i=0; i<count_stats; i++){
                insert.append(" ?,");
                update.append(mainstats[i].getName_of_stat()[0]).append(" = ?");
                if(i==count_stats-1){update.append(" ;"); insert.append(" ?)");}
                else {update.append(", "); }
            }

            //if(b==1)return;
            PreparedStatement pstmt = connect_to_db.prepareStatement(insert.append(update).toString());
            //System.out.println(insert);

            //Array[] arrays_stats = new Array[count_stats];
            for(Object nick: mainstats[2].getMap_of_Idplayer_stats().keySet()){

                pstmt.setString(1,String.valueOf(nick));
                //pstmt.setString(count_stats*2+2,(String) nick);

                for(int i=2; i<count_stats+2; i++){
                    Array arraystata = connect_to_db.createArrayOf("integer",(Object[]) mainstats[i-2].getMap_of_Idplayer_stats().get(nick));
                    //System.out.println(arraystata.toString()+" "+(i+1));
                    pstmt.setArray(i, arraystata);
                    pstmt.setArray(i+count_stats, arraystata);
                }
                //System.out.println(pstmt.toString());
                pstmt.addBatch();
            }
            assert pstmt != null;
          /*int[]  r = pstmt.executeBatch();
          for(int a:r) System.out.println(a);*/
            pstmt.executeBatch();
            connect_to_db.commit();
            connect_to_db.setAutoCommit(true);
            System.out.println(" record stats time "+(System.currentTimeMillis()-start));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public static void record_StatsCurrentGame(MainStats[] mainstats,String[] nicks){

        try {
            connect_to_db.setAutoCommit(false);
            StringBuilder insert = new StringBuilder("INSERT INTO work_nicks_stats VALUES ( ");

            StringBuilder update = new StringBuilder(" ON CONFLICT (nicks) DO UPDATE SET  ");
            int count_stats = mainstats.length;
            for(int i=0; i<count_stats; i++){
                insert.append(" ?,");
                update.append(mainstats[i].getName_of_stat()[0]).append(" = ?");
                if(i==count_stats-1){update.append(" ;"); insert.append(" ?)");}
                else {update.append(", "); }
            }

            //if(b==1)return;
            PreparedStatement pstmt = connect_to_db.prepareStatement(insert.append(update).toString());
           //System.out.println(insert);
            //Array[] arrays_stats = new Array[count_stats];
            for(String nick: nicks){
                if(nick==null)continue;
                pstmt.setString(1,nick);
                //pstmt.setString(count_stats*2+2,(String) nick);

                for(int i=2; i<count_stats+2; i++){
                    Array arraystata = connect_to_db.createArrayOf("integer",(Object[]) mainstats[i-2].getMap_of_Idplayer_stats().get(nick));
                    //System.out.println(arraystata.toString()+" "+(i+1));
                    pstmt.setArray(i, arraystata);
                    pstmt.setArray(i+count_stats, arraystata);
                }
                //System.out.println(pstmt.toString());
                pstmt.addBatch();
            }
            assert pstmt != null;
         /* int[]  r = pstmt.executeBatch();
          for(int a:r) System.out.println(a);*/
            pstmt.executeBatch();
            connect_to_db.commit();
            connect_to_db.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


   public static Array get_stats_of_one_player(String nick,String stata){
        Array result = null;
        String query = "SELECT "+stata+" FROM main_nicks_stats WHERE nicks='"+nick+"'  ;";

       System.out.println(query);
        try {

            ResultSet  rs  = stmt_of_db.executeQuery(query);
            while(rs.next()) {
                result = rs.getArray(stata);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }




    public static synchronized void record_rec_to_TableTempHands(CurrentHand.TempHand temphand){

        try {
            //stmt_of_db.executeUpdate(BEGIN);
//         long time_hand, short cards_hero, short position_hero, float[] stacks, int[] idplayers
            String record = "INSERT INTO temphands VALUES (?,?,?,?,?);";
            PreparedStatement pstmt = connect_to_db.prepareStatement(record);
            pstmt.setLong(1,temphand.time_hand());
            pstmt.setShort(2,temphand.cards_hero());
            pstmt.setShort(3,temphand.position_hero());
            // connect_to_db.createArrayOf("float[]", temphand.stacks())
            pstmt.setArray(4, connect_to_db.createArrayOf("float", temphand.stacks()));
            //pstmt.setObject(4,temphand.stacks());
            //pstmt.setObject(5,temphand.idplayers());
            pstmt.setArray(5, connect_to_db.createArrayOf("text", temphand.nicks()));
            pstmt.executeUpdate();
            //stmt_of_db.executeUpdate(COMMIT);
            //connect_to_db.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /*public record TempHand2(long time_hand, short cards_hero, short position_hero, Float[] stacks, Integer[] nicks){}


    public static List<TempHand2> get_list_TempHandsMinMaxTime_test(long min, long max){
        String query = "SELECT * FROM temphands WHERE time_hand>"+min+" AND time_hand<"+max+";";
        List<TempHand2> result = new ArrayList<>();
        ResultSet rs = null;
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            rs = stmt_of_db.executeQuery(query);
            while (rs.next()) {
              result.add(new TempHand2(rs.getLong("time_hand"),rs.getShort("cards_hero"),
                      rs.getShort("position_hero"),(Float[]) rs.getArray("stacks").getArray(),(Integer[]) rs.getArray("idplayers").getArray()));

            }
            //stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

      return result;
    }*/


    public static List<CurrentHand.TempHand> get_list_TempHandsMinMaxTime(long min, long max){
        String query = "SELECT * FROM temphands WHERE time_hand>"+min+" AND time_hand<"+max+";";
        List<CurrentHand.TempHand> result = new ArrayList<>();
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            ResultSet rs = stmt_of_db.executeQuery(query);
            while (rs.next()) {
                result.add(new CurrentHand.TempHand(rs.getLong("time_hand"),rs.getShort("cards_hero"),
                        rs.getShort("position_hero"),(Float[]) rs.getArray("stacks").getArray(),(String[]) rs.getArray("nicks").getArray()));

            }
            //stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static void delete_DataBase(String nameDB){
        connect_ToServer();
        String sql = "DROP DATABASE "+nameDB+" ";
        try {
            stmt_of_server.executeUpdate(sql);
            if(stmt_of_server!=null) stmt_of_server.close();
            if(connect_to_server!=null)connect_to_server.close();
        } catch (SQLException e) {
            System.out.println("oshibka in deletedatabase");
            e.printStackTrace();
        }
        System.out.println(nameDB+" is deleted");
        close_DataBase();
    }


    public static void delete_and_copy_WorkNicksStats(){
        String delete = "DROP TABLE IF EXISTS work_nicks_stats ";
        //String copy = "CREATE TABLE work_nicks_stats AS TABLE main_nicks_stats INCLUDING INDEXES;";
        String copy2 = "CREATE TABLE work_nicks_stats (LIKE main_nicks_stats INCLUDING ALL);";
        String insert = "INSERT INTO work_nicks_stats SELECT * FROM main_nicks_stats ;";


        try {
            stmt_of_db.executeUpdate(delete);
            stmt_of_db.executeUpdate(copy2);
            stmt_of_db.executeUpdate(insert);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("delete and copy");

    }

    public static void delete_LinesByNick(List<String> nicks_for_delete){
        try {
            connect_to_db.setAutoCommit(false);
            PreparedStatement pstmt = connect_to_db.prepareStatement("DELETE FROM main_nicks_stats WHERE nicks= ? ");
            for(String nick: nicks_for_delete){
                if(nick==null)continue;
                pstmt.setString(1,"$ю$"+nick+"$ю$");
                pstmt.addBatch();
            }
            assert pstmt != null;
          int[]  r = pstmt.executeBatch();
          for(int a:r) System.out.println(a);
            pstmt.executeBatch();
            connect_to_db.commit();
            connect_to_db.setAutoCommit(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    public static void close_DataBase(){
            try {
                if(connect_to_db!=null)connect_to_db.close();
                if(stmt_of_db!=null)stmt_of_db.close();
                System.out.println("close database");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
    }


    static void test_delete_Table(){
        //String delete = "DROP TABLE IF EXISTS idplayers_stats ";
        String createtable_idplayers_stats = "CREATE TABLE temphands_nicks ( time_hand bigint PRIMARY KEY, cards_hero smallint, position_hero smallint, stacks float4[], nicks text[] );";

        try {
            //stmt_of_db.executeUpdate(delete);
            stmt_of_db.executeUpdate(createtable_idplayers_stats);




        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    static void test_select(){
        String q = "SELECT column_name, column_default, data_type \n" +
                "FROM INFORMATION_SCHEMA.COLUMNS \n" +
                "WHERE table_name = 'main_nicks_stats';";
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            ResultSet rs = stmt_of_db.executeQuery(q);
            while (rs.next()) {
                System.out.println(rs.getString("column_name")+"   "+rs.getString("column_default")+"    "+rs.getString("data_type")+"     ");
            }
            //stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
       delete_DataBase("fg_testing");

       /* new Work_DataBase();
        test_select();
        close_DataBase();*/
       /* List<CurrentHand.TempHand> list = get_list_TempHandsMinMaxTime(0,0);
        for (CurrentHand.TempHand tempHand:list){
            System.out.println("time "+tempHand.time_hand()+" cards "+get_str_Cards(tempHand.cards_hero())
                   // +" pos_hero "+tempHand.position_hero()
            );
           *//* for(int i=0; i<6; i++)
                System.out.println("idplayer "+tempHand.idplayers()[i]+" stack "+tempHand.stacks()[i]);*//*
        }*/
        /*List<Long> times = new ArrayList<>();
        for (CurrentHand.TempHand tempHand:list){
            times
        }*/
        //test_delete_Table();
        //add_columns_in_TableIdplayersStats();
       //record_MainArrayOfStatsToDateBase(main_array_of_stats);

       /*long max = Collections.max(list.stream().map(CurrentHand.TempHand::time_hand).collect(Collectors.toList()));

        System.out.println(max);*/


    }
}
