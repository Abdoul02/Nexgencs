package com.fgtit.models;

import java.util.List;


@SuppressWarnings("all")
public class CustomJobCard {

    String name;
    String jobNo;
    String description;
    String address;
    int supervisorId, id, local_id;
    String progress;
    String fromDate;
    String toDate;
    String customerName;
    String drawingNo;
    String qty;


/*    public ERDjobCard(int id, String name, String jobNo, String description, String address,
                      int supervisorId, String progress, String fromDate, String toDate) {
        this.id = id;

        this.name = name;
        this.jobNo = jobNo;
        this.description = description;
        this.address = address;
        this.supervisorId = supervisorId;
        this.progress = progress;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }*/

    public CustomJobCard() {
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJobNo() {
        return jobNo;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public int getSupervisorId() {
        return supervisorId;
    }

    public String getProgress() {
        return progress;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

}
