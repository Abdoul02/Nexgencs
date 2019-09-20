package com.fgtit.models;

public class ERDSubTask {


    int id, task_id,jobCardId;

    String name;


    public ERDSubTask() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setJobCardId(int jobCardId) {
        this.jobCardId = jobCardId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getTask_id() {
        return task_id;
    }

    public int getJobCardId() {
        return jobCardId;
    }

    public String getName() {
        return name;
    }
}

