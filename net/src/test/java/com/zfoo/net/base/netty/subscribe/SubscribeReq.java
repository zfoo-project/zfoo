package com.zfoo.net.base.netty.subscribe;

import java.io.Serializable;

/**
 * @author godotg
 * @version 1.0
 * @since 2017 05.23 16:51
 */
public class SubscribeReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private int reqID;

    private String userName;

    private String productName;

    private String phoneNumber;

    private String address;

    public SubscribeReq(int reqID, String userName, String productName, String phoneNumber, String address) {
        this.reqID = reqID;
        this.userName = userName;
        this.productName = productName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public int getReqID() {
        return reqID;
    }

    public void setReqID(int reqID) {
        this.reqID = reqID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "SubscribeReq{" +
                "reqID=" + reqID +
                ", userName='" + userName + '\'' +
                ", productName='" + productName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
