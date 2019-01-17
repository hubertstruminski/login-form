package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static DAO.ConnectorDatabase.connect;

public class Database implements IDatabase{

    //not used
    public void dropTable(String tableName) throws Exception{
        Statement stmt = null;
        Connection c = null;
        try {
            c = connect();

            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS " + tableName;

            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }finally{
            stmt.close();
            c.close();
        }
    }

    //not used
    public void createTable() throws Exception{
        dropTable("Users");

        Statement stmt = null;
        Connection c = null;
        try {
            c = connect();

            stmt = c.createStatement();

            String seq = "CREATE SEQUENCE users_serial";

            String sql = "CREATE TABLE public.\"Users\" (" +
                    "id INT DEFAULT nextval('users_serial')   UNIQUE   PRIMARY KEY, " +
                    "username	VARCHAR(45)	NOT NULL, " +
                    "password	VARCHAR(30)	NOT NULL) " +
                    "WITH (" +
                    "OIDS = FALSE)";

            stmt.executeUpdate(seq);
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }finally{
            stmt.close();
            c.close();
        }
    }

    //not used
    public void insertRecordsWithAccounts() throws Exception{
        Connection c = null;
        Statement stmt = null;
        try{
            c = connect();
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String sql = "INSERT INTO public.\"Users\" (username, password) VALUES (?, ?);";
            stmt.executeUpdate(sql);

            c.commit();
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }finally{
            c.close();
            stmt.close();
        }
    }

    public boolean selectAllData(String userNameForm, String passwordForm) throws Exception{
        Statement stmt = null;
        Connection c = null;
        ResultSet rs = null;
        try {
            c = connect();
            c.setAutoCommit(false);

            stmt = c.createStatement();
            rs = stmt.executeQuery( "SELECT username, password FROM public.\"Users\";");
            while ( rs.next() ) {
                String  userNameDb = rs.getString("username");
                String passwordDb  = rs.getString("password");

                if(userNameDb.equals(userNameForm) && passwordDb.equals(passwordForm)){
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+ ": " + e.getMessage());
        }finally{
            rs.close();
            stmt.close();
            c.close();
        }
        return false;
    }

    public boolean checkUsernameCookie(String cookieUserName) throws Exception{
        Statement stmt = null;
        Connection c = null;
        ResultSet rs = null;
        try {
            c = connect();
            c.setAutoCommit(false);

            stmt = c.createStatement();
            rs = stmt.executeQuery( "SELECT username FROM public.\"Users\";");
            while (rs.next()) {
                String  userNameDb = rs.getString("username");

                if(userNameDb.equals(cookieUserName)){
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }finally {
            rs.close();
            stmt.close();
            c.close();
        }
        return false;
    }
}
