package kz.incubator.myktybake.callofdutyteacher.module;


public class News {

    private String desc;
    private String title;
    private String date;

    public News() {

    }
    public News(String title, String desc, String date) {
        this.desc = desc;
        this.title = title;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}