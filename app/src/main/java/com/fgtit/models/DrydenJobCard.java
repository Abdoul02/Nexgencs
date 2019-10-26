package com.fgtit.models;

public class DrydenJobCard {

    private String jobName;
    private String jobNo;
    private String qcNo;
    private String description;
    private String drawingNo;
    private int supervisorId, id, local_id;
    private String issueDate;
    private int checklistDone;

    public DrydenJobCard() {
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public String getQcNo() {
        return qcNo;
    }

    public void setQcNo(String qcNo) {
        this.qcNo = qcNo;
    }

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public int getChecklistDone() {
        return checklistDone;
    }

    public void setChecklistDone(int checklistDone) {
        this.checklistDone = checklistDone;
    }
}
