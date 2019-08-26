package com.fgtit.entities;

/**
 * Created by Abdoul on 27-06-2016.
 */
public class User {

    String idNum, uName,finger1, finger2, uStatus,card;
    int uId,shifts_id,shift_type,costCenterId;

    //Constructor

    public User(){

    }

    public User(String idNum,String name, String finger1, String finger2, String status,int shifts_id,int shift_type,int costCenterId,String card){

        this.idNum = idNum;
        this.uName = name;
        this.finger1 = finger1;
        this.finger2 = finger2;
        this.uStatus = status;
        this.costCenterId = costCenterId;
        this.shift_type = shift_type;
        this.shifts_id = shifts_id;
        this.card = card;
    }

    public User(int id,String idNum,String name, String finger1, String finger2, String status,int shifts_id,int shift_type,int costCenterId,String card){

        this.uId = id;
        this.idNum = idNum;
        this.uName = name;
        this.finger1 = finger1;
        this.finger2 = finger2;
        this.uStatus = status;
        this.costCenterId = costCenterId;
        this.shift_type = shift_type;
        this.shifts_id = shifts_id;
        this.card = card;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getFinger1() {
        return finger1;
    }

    public void setFinger1(String finger1) {
        this.finger1 = finger1;
    }

    public String getFinger2() {
        return finger2;
    }

    public void setFinger2(String finger2) {
        this.finger2 = finger2;
    }

    public String getuStatus() {
        return uStatus;
    }

    public void setuStatus(String uStatus) {
        this.uStatus = uStatus;
    }

    public int getuId() {
        return uId;
    }

    public void setuId(int uId) {
        this.uId = uId;
    }

    public int getShifts_id() {
        return shifts_id;
    }

    public void setShifts_id(int shifts_id) {
        this.shifts_id = shifts_id;
    }

    public int getShift_type() {
        return shift_type;
    }

    public void setShift_type(int shift_type) {
        this.shift_type = shift_type;
    }

    public int getCostCenterId() {
        return costCenterId;
    }

    public void setCostCenterId(int costCenterId) {
        this.costCenterId = costCenterId;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }
}
