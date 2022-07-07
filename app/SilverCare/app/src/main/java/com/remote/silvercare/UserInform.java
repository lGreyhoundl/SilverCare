package com.remote.silvercare;

public class UserInform {
    private String UserId;
    private String UserPwd;
    private String UserEmail;
    private String UserContactProtector;
    private String UserContactElder;
    private String UserPosition;
    private String UserResidence;
    private String UserDevice;
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

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
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

    public String getUserDevice() {
        return UserDevice;
    }

    public void setUserDevice(String userDevice) {
        UserDevice = userDevice;
    }


    public UserInform(){}

    public UserInform(String UserId, String UserPwd, String UserEmail, String UserContactProtector, String UserContactElder, String UserPosition, String UserResidence, String UserDevice, String UserLatitude, String UserLongitude){
        this.UserId = UserId;
        this.UserPwd = UserPwd;
        this.UserEmail = UserEmail;
        this.UserContactProtector = UserContactProtector;
        this.UserContactElder = UserContactElder;
        this.UserPosition = UserPosition;
        this.UserResidence = UserResidence;
        this.UserDevice = UserDevice;
        this.UserLatitude = UserLatitude;
        this.UserLongitude = UserLongitude;
    }
}
