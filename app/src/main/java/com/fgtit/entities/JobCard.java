package com.fgtit.entities;

public class JobCard {

    String name, job_id;

    public JobCard(){}

    public JobCard(String name,String job_id){

        this.name = name;
        this.job_id = job_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }
}
