package kz.incubator.myktybake.callofdutyteacher.module;


public class Student {
    String info;
    String group;
    String time;
    String photo;

    public Student() {

    }

    public Student(String info, String group, String time, String photo){
        this.info = info;
        this.group = group;
        this.time = time;
        this.photo = photo;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String thumbnail) {
        this.photo = thumbnail;
    }


}
