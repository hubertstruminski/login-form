package DAO;

import java.sql.SQLException;

public interface IDatabase {

    boolean selectAllData(String userNameForm, String passwordForm) throws SQLException;
    boolean checkUsernameCookie(String cookieUserName, String cookieSessionID) throws SQLException;
    void setSessionId(String sessionID, String userNameFromForm) throws SQLException;
}
