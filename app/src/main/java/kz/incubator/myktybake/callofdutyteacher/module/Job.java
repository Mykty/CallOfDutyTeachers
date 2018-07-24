package kz.incubator.myktybake.callofdutyteacher.module;


public class Job {
    String key;
    String name;
    String status;


    public Job() {

    }

    public Job(String key, String status, String name){
        this.key = key;
        this.status = status;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
