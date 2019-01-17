package DAO;

public interface IDatabase {

    boolean selectAllData(String userNameForm, String passwordForm) throws Exception;
    boolean checkUsernameCookie(String cookieUserName, String cookieSessionID) throws Exception;
    void setSessionId(String sessionID, String userNameFromForm) throws Exception;
}
