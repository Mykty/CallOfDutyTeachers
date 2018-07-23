package kz.incubator.myktybake.callofdutyteacher.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.adapters.FirebaseTeacherViewHolder;
import kz.incubator.myktybake.callofdutyteacher.adapters.TeacherDayListAdapter;
import kz.incubator.myktybake.callofdutyteacher.module.RecyclerItemClickListener;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_DATE;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_INFO;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME3;


public class TeacherDayFragment extends Fragment {

    DatabaseReference mDatabaseRef, teacherDutyRef;
    FirebaseRecyclerAdapter mFirebaseAdapter;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager linearLayoutManager;

    private List<Teacher> teachers2;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    TeacherDayListAdapter tListAdapter;
    View view;
    RelativeLayout relativeLayout;
    Dialog callSms;
    ImageView tImage;
    TextView tName;
    String phoneNumber;
    DateFormat dateF;
    String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.teachers_duty_fragment, container, false);

        getActivity().setTitle("Күндік борыш");
        relativeLayout = view.findViewById(R.id.relaviteL);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();


        teacherDutyRef = mDatabaseRef.child("duty_teachers_list");

        setUpRecyclerView();
        manageDate();
        setRecScrollPos();
        fillDayTeachers();
        return view;

    }

    public void fillDayTeachers() {
        Cursor dutyCursor = getDayDuty();

        teachers2.clear();

        if (((dutyCursor != null) && (dutyCursor.getCount() > 0))) {
            while (dutyCursor.moveToNext()) {
                teachers2.add(new Teacher(dutyCursor.getString(0), dutyCursor.getString(1), dutyCursor.getString(2), dutyCursor.getString(3)));
            }
        }

        tListAdapter.notifyDataSetChanged();
    }

    private void setUpRecyclerView() {

        teachers2 = new ArrayList<>();
        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        tListAdapter = new TeacherDayListAdapter(getActivity(), teachers2);

        recyclerView.setAdapter(tListAdapter);
        setupSwipeRefresh();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        phoneNumber = teachers2.get(position).getPhoneNumber();

                        new AlertDialog.Builder(getActivity())
                                .setTitle("Мұғалім: " + teachers2.get(position).getInfo())
                                .setMessage("Телефон номері: " + teachers2.get(position).getPhoneNumber())
                                .setPositiveButton("Хабарласу", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse("tel:" + phoneNumber));
                                        startActivity(callIntent);
                                    }
                                })

                                .setNegativeButton("Смс жазу", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Uri uri = Uri.parse("smsto:" + phoneNumber);
                                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                                        smsIntent.putExtra("sms_body", "Сулейман Демирель мектеп-интернат-колледж");
                                        startActivity(smsIntent);
                                    }
                                })
                                .show();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        checkVersion();

    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("current_day_version");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String newVersion = dataSnapshot.getValue().toString();
                if (!getDayCurrentVersion().equals(newVersion)) {

                    updateCurrentVersion(newVersion);
                    refreshTeachersDayDuty();

                }else{
                    fillDayTeachers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshTeachersDayDuty() {
        teachers2.clear();
        storeDb.cleanDayTable(sqdb);

        teacherDutyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot teachersSnapshot : dataSnapshot.getChildren()) {

                    Teacher teacher = teachersSnapshot.getValue(Teacher.class);

                    String cDate = teacher.getDate();
                    String cInfo = teacher.getInfo();
                    String cPhone = teacher.getPhoneNumber();
                    String cPhoto = teacher.getPhoto();

                    ContentValues teacherValue = new ContentValues();
                    teacherValue.put(COLUMN_DATE, cDate);
                    teacherValue.put(COLUMN_INFO, cInfo);
                    teacherValue.put(COLUMN_PHONE, cPhone);
                    teacherValue.put(COLUMN_PHOTO, cPhoto);

                    sqdb.insert(TABLE_NAME3, null, teacherValue);

                    teachers2.add(new Teacher(""+cDate, ""+cInfo, ""+cPhone, ""+cPhoto));
                }

                tListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setRecScrollPos(){
        int scrollPos = 0;
        for(int i = 0; i < teachers2.size(); i++){
            Teacher teacher = teachers2.get(i);
            String dateSplit[] = teacher.getDate().split(" ");
            if(date.equals(dateSplit[1])){
                scrollPos = i;
                break;
            }
        }
        recyclerView.scrollToPosition(scrollPos);
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("current_day_version", newVersion);
        sqdb.update("version_table", versionValues, "current_day_version=" + getDayCurrentVersion(), null);
        System.out.println("Updated version: " + getDayCurrentVersion());
    }

    public Cursor getDayDuty() {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME3, null);
        return res;
    }

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT current_day_version FROM version_table ", null);
        res.moveToNext();
        String version = res.getString(0);

        return version;
    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void refreshItems() {

        if (!isOnline()) {
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();
        } else {
            checkVersion();
        }

        onItemsLoadComplete();
    }

    public void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public boolean isOnline() {
        return true;
    }

    public void createDialog() {
        callSms = new Dialog(getActivity());
        callSms.setContentView(R.layout.dialog_call_sms);
        Button call = callSms.findViewById(R.id.btnCall);
        Button sms = callSms.findViewById(R.id.btnSms);
        tImage = callSms.findViewById(R.id.teacherPhoto);
        tName = callSms.findViewById(R.id.teacherName);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("smsto:" + phoneNumber);
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                smsIntent.putExtra("sms_body", "Сулейман Демирель мектеп-интернат-колледж");
                startActivity(smsIntent);
            }
        });

    }
}


/*

    public void referenseAddListener(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // A new comment has been added, add it to the displayed list
               Teacher teacher = dataSnapshot.getValue(Teacher.class);

                String fkey = dataSnapshot.getKey();

                ContentValues teacherValue = new ContentValues();
                teacherValue.put(COLUMN_FKEY, fkey);
                teacherValue.put(COLUMN_DATE, teacher.getDate());
                teacherValue.put(COLUMN_INFO, teacher.getInfo());
                teacherValue.put(COLUMN_PHONE, teacher.getPhoneNumber());
                teacherValue.put(COLUMN_PHOTO, teacher.getPhoto());

                sqdb.insert(TABLE_NAME3, null, teacherValue);

               System.out.println("onChildAdded: " + teacher.getInfo());
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Teacher changedTeacher = dataSnapshot.getValue(Teacher.class);
                String tkey = dataSnapshot.getKey();

                System.out.println("onChildChanged: " + changedTeacher.getInfo());
                updateTeacherData(tkey, changedTeacher);

//                storeDb.cleanDayTable(sqdb);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String commentKey = dataSnapshot.getKey();
                System.out.println("onChildRemoved: ");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Teacher movedComment = dataSnapshot.getValue(Teacher.class);
                String commentKey = dataSnapshot.getKey();
                System.out.println("onChildMoved: ");


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        teacherDutyRef.addChildEventListener(childEventListener);

    }



        mFirebaseAdapter = new FirebaseRecyclerAdapter<Teacher, FirebaseTeacherViewHolder>
                (Teacher.class, R.layout.day_duty_item, FirebaseTeacherViewHolder.class, teacherDutyRef) {

            @Override
            protected void populateViewHolder(FirebaseTeacherViewHolder viewHolder, Teacher teacher, int position) {
                viewHolder.bindTeacher(teacher);

//                ContentValues teacherValue = new ContentValues();
//                teacherValue.put(COLUMN_DATE, teacher.getDate());
//                teacherValue.put(COLUMN_INFO, teacher.getInfo());
//                teacherValue.put(COLUMN_PHONE, teacher.getPhoneNumber());
//                teacherValue.put(COLUMN_PHOTO, teacher.getPhoto());
//
//                sqdb.insert(TABLE_NAME3, null, teacherValue);

                teachers2.add(new Teacher(""+teacher.getDate(), ""+teacher.getInfo(), ""+teacher.getPhoneNumber(), ""+teacher.getPhoto()));

            }

        };
//        fillDayTeachers();
        recyclerView.setAdapter(mFirebaseAdapter);

 */