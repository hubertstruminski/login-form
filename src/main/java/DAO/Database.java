package DAO;

import Model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import static DAO.ConnectorDatabase.connect;

public class Database {

    public boolean selectAllData(String userNameForm, String passwordForm){
        Statement stmt = null;
        try {
            Connection c = connect();
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT username, password FROM public.\"Users\";");
            while ( rs.next() ) {
                String  userNameDb = rs.getString("username");
                String passwordDb  = rs.getString("password");

                if(userNameDb.equals(userNameForm) && passwordDb.equals(passwordForm)){
                    return true;
                }
            }

            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println(e.getClass().getName()+ ": " + e.getMessage());
            System.exit(0);
        }

        return false;
    }
}
