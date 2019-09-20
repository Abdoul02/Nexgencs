package com.fgtit.models;

import java.util.List;


@SuppressWarnings("all")
public class ERDjobCard {

    String name;
    String jobNo;
    String description;
    String address;
    int supervisorId, id, local_id;
    String progress;
    String fromDate;
    String toDate;


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

    public ERDjobCard() {
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

}
