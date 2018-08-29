package kz.incubator.myktybake.callofdutyteacher.module;

public class Lesson {
    String key;
    String course;
    String name;
    String semestr;
    long hours;

    public Lesson() {

    }

    public Lesson(String key, String course, String name, long hours){
        this.key = key;
        this.course = course;
        this.name = name;
        this.hours = hours;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemestr() {
        return semestr;
    }

    public void setSemestr(String semestr) {
        this.semestr = semestr;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }
}
