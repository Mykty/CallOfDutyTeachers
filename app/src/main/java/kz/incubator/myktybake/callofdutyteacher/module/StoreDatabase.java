package kz.incubator.myktybake.callofdutyteacher.module;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import kz.incubator.myktybake.callofdutyteacher.R;

public class StoreDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sdcl.db";
    private static final int DATABASE_VERSION = 26;


    public static final String COLUMN_Q_ID = "qr_code";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_GROUP = "s_group";
    public static final String COLUMN_LATE_MIN = "late_min";
    public static final String COLUMN_PHOTO = "photo";

    public static final String COLUMN_DATE = "duty_date";
    public static final String COLUMN_INFO = "info";
    public static final String COLUMN_PHONE = "phone_number";

    public static final String TABLE_NAME = "late_list";
    public static final String TABLE_NAME2_STUDENTS = "students_list";
    public static final String TABLE_NAME3 = "teachers_day_duty_list";
    public static final String TABLE_NAME4 = "teachers_friday_duty_list";
    public static final String TABLE_NAME5 = "version_table";
    public static final String TABLE_LATECOMERS = "latecomers";
    Context context;

    public StoreDatabase(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_Q_ID + " TEXT, " +
                COLUMN_LATE_MIN + " TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_NAME2_STUDENTS + "(" +
                COLUMN_Q_ID + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_GROUP + " TEXT, " +
                COLUMN_PHOTO  + " INTEGER )");

        db.execSQL("CREATE TABLE " + TABLE_NAME3 + "(" +
                COLUMN_DATE + " TEXT, " +
                COLUMN_INFO + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_PHOTO  + " TEXT )");


        db.execSQL("CREATE TABLE " + TABLE_NAME4 + "(" +
                COLUMN_DATE + " TEXT, " +
                COLUMN_INFO + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_PHOTO  + " TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_NAME5 + "( current_day_version TEXT, current_friday_version TEXT, current_student_version TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_LATECOMERS + "(" +
                COLUMN_DATE + " TEXT, " +
                COLUMN_Q_ID + " TEXT, " +
                COLUMN_LATE_MIN + " TEXT )");

        //addStudents(db);
        addVersions(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME4);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME5);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LATECOMERS);

        onCreate(db);
    }
    public void cleanWeeklyLatecomersTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_LATECOMERS);

    }
    public void cleanLatecomersTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_NAME);

    }

    public void cleanDayTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_NAME3);

    }

    public void cleanFridayTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_NAME4);

    }

    public void cleanStudentsTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_NAME2_STUDENTS);

    }

    public void deleteLatecomer(SQLiteDatabase db, String qr_code){
        db.delete(TABLE_NAME, COLUMN_Q_ID + "=" + qr_code, null);
    }

    public void addVersions(SQLiteDatabase db){

        ContentValues versionValues = new ContentValues();
        versionValues.put("current_day_version", "0");
        versionValues.put("current_friday_version", "0");
        versionValues.put("current_student_version", "0");

        db.insert(TABLE_NAME5, null, versionValues);
    }
    public void addStudents(SQLiteDatabase db){

        ArrayList<StudentToAddDb> g101 = new ArrayList<>();
        g101.add(new StudentToAddDb("101201720", "Айтқазы Аида",    "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201721", "Алғазиева Аида",  "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201722", "Ануарбек Альтаир", "1-01", "img10103"));
        g101.add(new StudentToAddDb("101201723", "Ахметов Мухаметали", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201724", "Бахтиярұлы Ескендір", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201725", "Бақыт Айбек", "1-01", "img10103"));
        g101.add(new StudentToAddDb("101201726", "Бейбітова Жангүл", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201727", "Гунеш Ахмет", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201728", "Дарханұлы Досбол", "1-01", "img10103"));
        g101.add(new StudentToAddDb("101201729", "Камзаев Диас", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201730", "Касимов Ибрахим", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201731", "Құрманов Даруш", "1-01", "img10103"));
        g101.add(new StudentToAddDb("101201732", "Махсутұлы Нұртай", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201733", "Медетханова Дильназ", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201734", "Мейірбек Қажымұқан", "1-01", "img10103"));
        g101.add(new StudentToAddDb("101201735", "Мирзаев Ақниет", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201736", "Отай Абылай", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201737", "Отаралы Ақнұр", "1-01", "img10103"));
        g101.add(new StudentToAddDb("101201738", "Пердебек Асхат", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201739", "Подлесная Светлана", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201740", "Райс Жасия", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201741", "Сейтов Ералы", "1-01", "img10101"));
        g101.add(new StudentToAddDb("101201742", "Уразбай Бақытжан", "1-01", "img10102"));
        g101.add(new StudentToAddDb("101201743", "Ілиясова Асылай", "1-01", "img10103"));

        for(StudentToAddDb s: g101){
            ContentValues sValues = new ContentValues();
            sValues.put(COLUMN_Q_ID, s.getQr_code());
            sValues.put(COLUMN_NAME, s.getName());
            sValues.put(COLUMN_GROUP, s.getGroup());
            sValues.put(COLUMN_PHOTO, s.getImgName());

            db.insert(TABLE_NAME2_STUDENTS, null, sValues);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_Q_ID, "104201721");
        contentValues.put(COLUMN_NAME, "Абдешов Мейіржан");
        contentValues.put(COLUMN_GROUP, "1-04");
        contentValues.put(COLUMN_PHOTO, R.drawable.back);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COLUMN_Q_ID, "410201720");
        contentValues2.put(COLUMN_NAME, "Абдукаримов Абдурасул");
        contentValues2.put(COLUMN_GROUP, "4-10");
        contentValues2.put(COLUMN_PHOTO, R.drawable.icon0);

        ContentValues contentValues3 = new ContentValues();
        contentValues3.put(COLUMN_Q_ID, "201201742");
        contentValues3.put(COLUMN_NAME, "Ізімова Нарқыз");
        contentValues3.put(COLUMN_GROUP, "2-03");
        contentValues3.put(COLUMN_PHOTO, R.drawable.icon0);

        ContentValues versionValues = new ContentValues();
        versionValues.put("current_day_version", "0");
        versionValues.put("current_friday_version", "0");

        db.insert(TABLE_NAME2_STUDENTS, null, contentValues);
        db.insert(TABLE_NAME2_STUDENTS, null, contentValues2);
        db.insert(TABLE_NAME2_STUDENTS, null, contentValues3);
        db.insert(TABLE_NAME5, null, versionValues);

    }

    //Meirjan 104201721
    //Abdurasul 410201720
    //Narkiz 201201742

    /*
    Есеп және аудит(1-01)
№	АТЫ ЖӨНІ	        QR Code

1	Айтқазы Аида	    101201720
2	Алғазиева Аида	    101201721
3	Ануарбек Альтаир	101201722
4	Ахметов Мухаметали	101201723
5	Бахтиярұлы Ескендір	101201724
6	Бақыт Айбек	        101201725
7	Бейбітова Жангүл	101201726
8	Гунеш Ахмет	        101201727
9	Дарханұлы Досбол	101201728
10	Камзаев Диас	    101201729
11	Касимов Ибрахим	    101201730
12	Құрманов Даруш	    101201731
13	Махсутұлы Нұртай	101201732
14	Медетханова Дильназ	101201733
15	Мейірбек Қажымұқан	101201734
16	Мирзаев Ақниет  	101201735
17	Отай Абылай	        101201736
18	Отаралы Ақнұр	    101201737
19	Пердебек Асхат	    101201738
20	Подлесная Светлана	101201739
21	Райс Жасия	        101201740
22	Сейтов Ералы	    101201741
23	Уразбай Бақытжан	101201742
24	Ілиясова Асылай	    101201743

     */

}
