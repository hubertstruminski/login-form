package DAO;

public interface IDatabase {

    boolean selectAllData(String userNameForm, String passwordForm) throws Exception;
    boolean checkUsernameCookie(String cookieUserName) throws Exception;
}
