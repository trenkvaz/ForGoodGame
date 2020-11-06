package org.trenkvaz.database_hands;

import org.trenkvaz.main.CaptureVideo;
import org.trenkvaz.main.CurrentHand;
import org.trenkvaz.stats.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class Work_DataBase {

static final String DB_SERVER = "jdbc:postgresql://127.0.0.1:5433/", USER = "postgres", PASS = "admin", BEGIN = "BEGIN;",COMMIT = "COMMIT;";
static Connection connect_to_db, connect_to_server;
static Statement stmt_of_db, stmt_of_server;
public static MainStats[] stats = new MainStats[]{new AgainstRFI(),new Against3bet(),new VpipPFR3bet(),new RFI(),new Alliners()};
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
        String createtable_idplayers_stats = "CREATE TABLE idplayers_stats (idplayers bigint PRIMARY KEY);";
        String createtable_idplayers_nicks = "CREATE TABLE idplayers_nicks (idplayers integer PRIMARY KEY, nicks text );";
        String createtable_temphands = "CREATE TABLE temphands ( time_hand bigint PRIMARY KEY, cards_hero smallint, position_hero smallint, stacks float4[], idplayers integer[] );";
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            stmt_of_db.executeUpdate(createtable_idplayers_nicks);
            stmt_of_db.executeUpdate(createtable_idplayers_stats);
            stmt_of_db.executeUpdate(createtable_temphands);
            //stmt_of_db.executeUpdate(COMMIT);
            System.out.println(" sozdana tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        add_columns_in_TableIdplayersStats();
    }


    private void add_columns_in_TableIdplayersStats(){
        // добавление колонок со статами в таблицу idplayers_stats
        // названия и типа колонок берутся из находящихся в массиве Объектов класса МайнСтатс

        String query = "SELECT column_name FROM information_schema.columns WHERE table_name =  'idplayers_stats' ";
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
            for(MainStats stata:stats){
                String[] str_stata = stata.getName_of_stat();
                if(colomns.contains(str_stata[0]))continue;
                System.out.println(str_stata[0]);
                adding = "ALTER TABLE idplayers_stats ADD COLUMN "+str_stata[0]+" "+str_stata[1]+" ;";
                stmt_of_db.addBatch(adding);
            }
            stmt_of_db.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   public Map<String, Integer> get_map_IdPlayersNicks(){
        Map<String, Integer> LocalNiksMap = new HashMap<>();
        String query = "SELECT idplayers, nicks FROM idplayers_nicks ;";
        int Idplayer = 0; String Nick = null;
        ResultSet rs = null;
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            rs = stmt_of_db.executeQuery(query);
            while (rs.next())
            { Idplayer = rs.getInt("idplayers");
                Nick = rs.getString("nicks");
                if(Idplayer!=0){ LocalNiksMap.put(Nick,Idplayer); }
            }
            //stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LocalNiksMap;
    }


    public void record_listrec_to_TableIdPlayersNicks(List<CaptureVideo.IdPlayer_Nick> rec){

        try {
            //stmt_of_db.executeUpdate(BEGIN);
            for(CaptureVideo.IdPlayer_Nick item :  rec){
                String record = "INSERT INTO idplayers_nicks VALUES ("+item.idplayer()+",$ю$"+item.nick()+"$ю$);";
                stmt_of_db.addBatch(record);
            }
            stmt_of_db.executeBatch();
            //stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void record_rec_to_TableTempHands(CurrentHand.TempHand temphand){

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
            pstmt.setArray(5, connect_to_db.createArrayOf("integer", temphand.idplayers()));
            pstmt.executeUpdate();
            //stmt_of_db.executeUpdate(COMMIT);
            //connect_to_db.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static List<CurrentHand.TempHand> get_list_TempHands(){
        String query = "SELECT * FROM temphands ;";
        List<CurrentHand.TempHand> result = new ArrayList<>();
        ResultSet rs = null;
        try {
            //stmt_of_db.executeUpdate(BEGIN);
            rs = stmt_of_db.executeQuery(query);
            while (rs.next()) {
              result.add(new CurrentHand.TempHand(rs.getLong("time_hand"),rs.getShort("cards_hero"),
                      rs.getShort("position_hero"),(Float[]) rs.getArray("stacks").getArray(),(Integer[]) rs.getArray("idplayers").getArray()));

            }
            //stmt_of_db.executeUpdate(COMMIT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

      return result;
    }

    public static void delete_DataBase(String nameDB){
        connect_ToServer();
        String sql = "DROP DATABASE \""+nameDB+"\"";
        try {
            stmt_of_server.executeUpdate(sql);
            if(stmt_of_server!=null) stmt_of_server.close();
            if(connect_to_server!=null)connect_to_server.close();
        } catch (SQLException e) {
            System.out.println("oshibka in deletedatabase");
            e.printStackTrace();
        }
        System.out.println(nameDB+" is deleted");
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


    public static void main(String[] args) {
        //delete_DataBase("fg_test_db1");

        new Work_DataBase();
        List<CurrentHand.TempHand> list = get_list_TempHands();
        for (CurrentHand.TempHand tempHand:list){
            System.out.println("time "+tempHand.time_hand()+" cards "+tempHand.cards_hero()+" pos_hero "+tempHand.position_hero());
            for(int i=0; i<6; i++)
                System.out.println("idplayer "+tempHand.idplayers()[i]+" stack "+tempHand.stacks()[i]);
        }
        close_DataBase();
    }
}
