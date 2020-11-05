package org.trenkvaz.database_hands;

import org.trenkvaz.stats.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import static org.trenkvaz.main.CaptureVideo.home_folder;

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
                            createTables();
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



    private void createTables(){
        System.out.println("creat tables");
        //String createtable_Hands = "CREATE TABLE "+NameOfTable+" ( "+getStructureTable()+" );";
        String createtable_idplayers_stats = "CREATE TABLE idplayers_stats (idplayer bigint PRIMARY KEY);";
        String createtable_idplayers_nicks = "CREATE TABLE idplayers_nicks (idplayer integer PRIMARY KEY, nicks text );";
        try {
            stmt_of_db.executeUpdate(BEGIN);
            stmt_of_db.executeUpdate(createtable_idplayers_nicks);
            stmt_of_db.executeUpdate(createtable_idplayers_stats);
            stmt_of_db.executeUpdate(COMMIT);
            System.out.println(" sozdana tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        add_column_in_Idstats();
    }

    private void add_column_in_Idstats(){
        // добавление колонок со статами в таблицу idplayers_stats
        // названия и типа колонок берутся из находящихся в массиве Объектов класса МайнСтатс

        String query = "SELECT column_name FROM information_schema.columns WHERE table_name =  'idplayers_stats' ";
        try {
            stmt_of_db.executeUpdate(BEGIN);
            ResultSet rs = stmt_of_db.executeQuery(query);
            ArrayList<String> colomns = new ArrayList<>();
            while (rs.next()) {
                String colomn = rs.getString("column_name");
                System.out.println(colomn);
                colomns.add(colomn);
            }
            stmt_of_db.executeUpdate(COMMIT);
            System.out.println("no stats: ");
            String adding = null;
            for(MainStats stata:stats){
                String[] str_stata = stata.getName_of_stat();
                if(colomns.contains(str_stata[0]))continue;
                System.out.println(str_stata[0]);
                adding = "ALTER TABLE idstats ADD COLUMN "+str_stata[0]+" "+str_stata[1]+" ;";
                stmt_of_db.addBatch(adding);
            }
            stmt_of_db.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void deleteDataBase(String nameDB){
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

    }

    public static void main(String[] args) {
        //deleteDataBase("null");
       /* deleteDataBase("test_db3");
        deleteDataBase("test_db4");*/
        new Work_DataBase();
    }
}
