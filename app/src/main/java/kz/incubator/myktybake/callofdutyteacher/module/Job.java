package kz.incubator.myktybake.callofdutyteacher.module;


public class Job {
    String name;
    String status;


    public Job() {

    }

    public Job(String status, String name){
        this.status = status;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
