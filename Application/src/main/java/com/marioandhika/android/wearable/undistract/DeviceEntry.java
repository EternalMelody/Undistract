package com.marioandhika.android.wearable.undistract;

/**
 * Created by marioandhika on 5/6/15.
 */
public class DeviceEntry {
    public DeviceEntry(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    private String name;
    private String mac;

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean isChecked;
}
