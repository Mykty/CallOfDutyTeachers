package kz.incubator.myktybake.callofdutyteacher.fragments;

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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.adapters.FirebaseTeacherViewHolder;
import kz.incubator.myktybake.callofdutyteacher.adapters.TeacherFridayListAdapter;
import kz.incubator.myktybake.callofdutyteacher.module.RecyclerItemClickListener;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_DATE;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_INFO;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME3;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME4;


public class TeacherFridayFragment extends Fragment {

    String TAG = "child";
    DatabaseReference mDatabaseRef, teacherDutyRef;
    FirebaseRecyclerAdapter mFirebaseAdapter;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager gridLayoutManager;
    private List<Teacher> teachers2;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    int count = 0;
    TeacherFridayListAdapter tListAdapter;
    View view;
    String phoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.teachers_duty_fragment, container, false);

        getActivity().setTitle("Жұма күнгі борыш");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        teacherDutyRef = mDatabaseRef.child("friday_duty_teachers_list");

        setUpRecyclerView();
        fillFridayTeachers();

        return view;

    }

    public void fillFridayTeachers() {
        teachers2.clear();

        Cursor dutyCursor = getFridayDuty();
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
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        tListAdapter = new TeacherFridayListAdapter(getActivity(), teachers2);
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


                    }
                })
        );
        checkVersion();

    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("current_friday_version");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String newVersion = dataSnapshot.getValue().toString();
                if (!getFridayCurrentVersion().equals(newVersion)) {

                    updateCurrentVersion(newVersion);
                    refreshTeachersDayDuty();
                }else{
                    fillFridayTeachers();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshTeachersDayDuty() {
        storeDb.cleanFridayTable(sqdb);
        teachers2.clear();

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

                    sqdb.insert(TABLE_NAME4, null, teacherValue);

                    teachers2.add(new Teacher(""+cDate, ""+cInfo, ""+cPhone, ""+cPhoto));
                }

                tListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("current_friday_version", newVersion);
        sqdb.update("version_table", versionValues, "current_friday_version=" + getFridayCurrentVersion(), null);
    }

    public Cursor getFridayDuty() {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME4, null);
        return res;
    }

    public String getFridayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT current_friday_version FROM version_table ", null);
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
        //   mFirebaseAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public boolean isOnline() {
        return true;
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
 */