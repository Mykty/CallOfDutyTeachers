package kz.incubator.myktybake.callofdutyteacher.module;


public class StudentToAddDb {
    String qr_code;
    String name;
    String group;
    String imgName;

    public StudentToAddDb() {

    }

    public StudentToAddDb(String qr_code, String name, String group, String imgName){
        this.qr_code = qr_code;
        this.name = name;
        this.group = group;
        this.imgName = imgName;
    }

    public String getQr_code() {
        return qr_code;
    }

    public void setQr_code(String qr_code) {
        this.qr_code = qr_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }
}
