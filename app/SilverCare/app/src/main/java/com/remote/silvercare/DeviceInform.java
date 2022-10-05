package com.remote.silvercare;

public class DeviceInform {
    private String DeviceName;
    private String DeviceStatus;

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getDeviceStatus() {
        return DeviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        DeviceStatus = deviceStatus;
    }

    public DeviceInform(String DeviceName, String DeviceStatus){
        this.DeviceName = DeviceName;
        this.DeviceStatus = DeviceStatus;
    }
}
