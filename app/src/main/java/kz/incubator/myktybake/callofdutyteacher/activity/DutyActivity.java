package kz.incubator.myktybake.callofdutyteacher.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.fragments.AbsentFragment;
import kz.incubator.myktybake.callofdutyteacher.fragments.LatecomersFragment;
import kz.incubator.myktybake.callofdutyteacher.fragments.PunishedReportFragment;
import kz.incubator.myktybake.callofdutyteacher.fragments.TeacherDayFragment;
import kz.incubator.myktybake.callofdutyteacher.fragments.TeacherFridayFragment;
import kz.incubator.myktybake.callofdutyteacher.fragments.WeeklyReportFragment;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_GROUP;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_NAME;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_Q_ID;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME2_STUDENTS;

public class DutyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    LatecomersFragment latecomersFragment;
    AbsentFragment absentFragment;
    TeacherDayFragment teachersDutyFragment;
    TeacherFridayFragment teacherFridayFragment;
    WeeklyReportFragment weeklyReportFragment;
    PunishedReportFragment punishedReportFragment;
    DatabaseReference mDatabase;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String TAG = "info";
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        //addChildListener();
        //writeNewTeacher();
        //navigationView.getMenu().getItem(2).setActionView(R.layout.menu_dot);

        createFragments();
        changeFragment(latecomersFragment);

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

//        teachersDutyFragment = new TeacherDayFragment();
//        changeFragment(teachersDutyFragment);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        checkInetConnection();
        checkVersion();

    }

    public void createFragments(){
        latecomersFragment = new LatecomersFragment();
        absentFragment = new AbsentFragment();
        teachersDutyFragment = new TeacherDayFragment();
        teacherFridayFragment = new TeacherFridayFragment();
        weeklyReportFragment = new WeeklyReportFragment();
        punishedReportFragment = new PunishedReportFragment();
    }


    public void checkVersion(){
        Query myTopPostsQuery = mDatabase.child("current_student_version");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String newVersion = dataSnapshot.getValue().toString();
                if (!geStudentCurrentVersion().equals(newVersion)) {

                    System.out.println("onDataChange: ");
                    updateCurrentVersion(newVersion);
                    getAllStudents();

                    Toast.makeText(DutyActivity.this, "Students updated", Toast.LENGTH_SHORT).show();
                    // fillDayTeachers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    Query myTopPostsQuery;
    ValueEventListener listener;

    public void getAllStudents(){
        storeDb.cleanStudentsTable(sqdb);

        System.out.println("getAllStudents: ");
        myTopPostsQuery = mDatabase.child("groups").orderByKey();

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot groups: dataSnapshot.getChildren()) {

                    String group = groups.getKey();

                    System.out.println("group: "+group);

                    for(DataSnapshot student: groups.getChildren()){

                        String sName = student.child("name").getValue().toString();
                        String sPhoto = student.child("photo").getValue().toString();
                        String sQrCode = student.child("qr_code").getValue().toString();

                        ContentValues sValues = new ContentValues();
                        sValues.put(COLUMN_Q_ID, sQrCode);
                        sValues.put(COLUMN_NAME, sName);
                        sValues.put(COLUMN_GROUP, group);
                        sValues.put(COLUMN_PHOTO, sPhoto);

                        sqdb.insert(TABLE_NAME2_STUDENTS, null, sValues);

                    }

                }

                myTopPostsQuery.removeEventListener(listener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        };

        myTopPostsQuery.addValueEventListener(listener);
    }


    public String geStudentCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT current_student_version FROM version_table ", null);
        res.moveToNext();
        String version = res.getString(0);
        System.out.println("CurrentVersion: "+version);
        return version;
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("current_student_version", newVersion);
        sqdb.update("version_table", versionValues, "current_student_version=" + geStudentCurrentVersion(), null);
    }


    private void writeNewTeacher() {

        ArrayList<Teacher> teachersDay = new ArrayList<>();
        teachersDay.add(new Teacher("Понедельник, 26.03", "Габдуллин Бақытжан", "87771234567", "url"));
        teachersDay.add(new Teacher("Вторник, 27.03", "Прназаров Бауыржан", "87771234567", "url"));
        teachersDay.add(new Teacher("Среда, 28.03", "Султамуратова Аида", "87771234567", "url"));
        teachersDay.add(new Teacher("Четверг, 29.03", "Сахиева Жулдыз", "87771234567", "url"));
        teachersDay.add(new Teacher("Пятница, 30.03", "Ауесова Ляззат", "87771234567", "url"));

        for(Teacher teacher: teachersDay){
            String newKey = mDatabase.push().getKey();
            mDatabase.child("duty_teachers_list").child(newKey).setValue(teacher);
        }

        Toast.makeText(DutyActivity.this, "New Duty teachers added", Toast.LENGTH_SHORT).show();

        mDatabase.child("current_day_version").setValue(77);

        ArrayList<Teacher> teachersFriday = new ArrayList<>();
        teachersFriday.add(new Teacher("Пятница, 30.03", "Исмаилова Хуршида", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 30.03", "Нурадинов Жават", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 06.04", "Атамқұл Жаннур", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 06.04", "Прназаров Бауыржан", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 13.04", "Шагирова Жулдыз", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 13.04", "Усупова Дана", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 20.04", "Қанаша Қымбат", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 20.04", "Бурхан Шыңғыс", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 27.04", "Варол Сумейра", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 27.04", "Болатбек Салтанат", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 04.05", "Жамал Молдир", "87771234567", "url"));
        teachersFriday.add(new Teacher("Пятница, 04.05", "Жамал Молдир", "87771234567", "url"));

        for(Teacher teacher: teachersFriday){
            String newKey = mDatabase.push().getKey();
            mDatabase.child("friday_duty_teachers_list").child(newKey).setValue(teacher);
        }

        mDatabase.child("current_friday_version").setValue(77);

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public void checkInetConnection(){
        if(isNetworkAvailable(this)){
            //Toast.makeText(MainActivity.this, "Inet is working", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(DutyActivity.this, "There is no inet connection", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //writeNewTeacher();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.latecomers) {
            changeFragment(latecomersFragment);

        } else if (id == R.id.absent) {
            changeFragment(absentFragment);

        } else if (id == R.id.duty_day) {
            changeFragment(teachersDutyFragment);

        } else if (id == R.id.duty_friday) {
            changeFragment(teacherFridayFragment);

        } else if (id == R.id.report) {
            changeFragment(weeklyReportFragment);

        } else if (id == R.id.punished) {
            changeFragment(punishedReportFragment);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(Fragment cfragment){
        Fragment fragment = cfragment;
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }

    public void addChildListener(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.i(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Teacher teacher = dataSnapshot.getValue(Teacher.class);
                Toast.makeText(DutyActivity.this, "onChildAdded", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.i(TAG, "onChildChanged:" + dataSnapshot.getKey());

                Teacher newComment = dataSnapshot.getValue(Teacher.class);
                String commentKey = dataSnapshot.getKey();

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.i(TAG, "onChildMoved:" + dataSnapshot.getKey());

                Teacher movedComment = dataSnapshot.getValue(Teacher.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(DutyActivity.this, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.addChildEventListener(childEventListener);
    }
}
