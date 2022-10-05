package com.remote.silvercare;

public class UserInform {
    private String UserId;
    private String UserPwd;
    private String UserContactProtector;
    private String UserContactElder;
    private String UserPosition;
    private String UserResidence;
    private String UserLatitude;
    private String UserLongitude;

    public String getUserContactProtector() {
        return UserContactProtector;
    }

    public void setUserContactProtector(String userContactProtector) {
        UserContactProtector = userContactProtector;
    }

    public String getUserContactElder() {
        return UserContactElder;
    }

    public void setUserContactElder(String userContactElder) {
        UserContactElder = userContactElder;
    }

    public String getUserLatitude() {
        return UserLatitude;
    }

    public void setUserLatitude(String userLatitude) {
        UserLatitude = userLatitude;
    }

    public String getUserLongitude() {
        return UserLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        UserLongitude = userLongitude;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserPwd() {
        return UserPwd;
    }

    public void setUserPwd(String userPwd) {
        UserPwd = userPwd;
    }

    public String getUserPosition() {
        return UserPosition;
    }

    public void setUserPosition(String userPosition) {
        UserPosition = userPosition;
    }

    public String getUserResidence() {
        return UserResidence;
    }

    public void setUserResidence(String userResidence) {
        UserResidence = userResidence;
    }


    public UserInform(){}

    public UserInform(String UserId, String UserPwd, String UserContactProtector, String UserContactElder,
                      String UserPosition, String UserResidence, String UserLatitude, String UserLongitude){
        this.UserId = UserId;
        this.UserPwd = UserPwd;
        this.UserContactProtector = UserContactProtector;
        this.UserContactElder = UserContactElder;
        this.UserPosition = UserPosition;
        this.UserResidence = UserResidence;
        this.UserLatitude = UserLatitude;
        this.UserLongitude = UserLongitude;
    }
}
