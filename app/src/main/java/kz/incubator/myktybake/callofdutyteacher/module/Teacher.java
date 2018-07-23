package kz.incubator.myktybake.callofdutyteacher.module;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Teacher {
    String key;
    String date;
    String info;
    String photo;
    long progress;
    String phoneNumber;

    public Teacher() {

    }

    public Teacher(String key, String info, String photo, String phoneNumber, long progress){
        this.key = key;
        this.info = info;
        this.photo = photo;
        this.phoneNumber = phoneNumber;
        this.progress = progress;
    }

    public Teacher(String date, String info, String phoneNumber, String photo){
        this.date = date;
        this.info = info;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String toString(){
        return "Date: "+date+" Info: "+info+" Number: "+phoneNumber+" Photo: "+photo;
    }
}
