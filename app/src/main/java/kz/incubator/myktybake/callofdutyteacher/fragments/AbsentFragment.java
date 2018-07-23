package kz.incubator.myktybake.callofdutyteacher.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.adapters.AbsentListAdapter;
import kz.incubator.myktybake.callofdutyteacher.adapters.RecyclerItemAbsentHelper;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Student;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.AddAbsentsActivity;


public class AbsentFragment extends Fragment implements RecyclerItemAbsentHelper.RecyclerItemTouchHelperListener, OnClickListener {
    private RecyclerView recyclerView;
    private List<Student> cartList;
    private AbsentListAdapter mAdapter;
    private RelativeLayout coordinatorLayout, dialogLayout;
    private DatabaseReference mDatabase;

    FloatingActionButton addBtn;
    String date, time, firebaseDate;
    String qr_code;
    DateFormat dateF, timeF, dateFr;
    TextView tvDate;
    RecyclerView.LayoutManager mLayoutManager;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    Dialog qr_dialog;
    Button btnScanner, btnSearch;

    String STUDENT_LIST = "students_list";
    String TABLE_NAME2_STUDENTS = "students_list";
    String LATE_LIST = "late_list";
    String TAG = "info";
    View view;
    HashMap<String, Integer> checkerHashMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        tvDate = (TextView) view.findViewById(R.id.textViewDate);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        addBtn = (FloatingActionButton) view.findViewById(R.id.addBtn);

        createRecycleView();
        manageDate();
        updateLatecomers();

        addBtn.setOnClickListener(this);
        getActivity().setTitle("Мүлдем келмегендер");
        return view;

    }

    @Override
    public void onClick(View view) {
        if(isNetworkAvailable(getActivity())){

            Intent t = new Intent(getActivity(), AddAbsentsActivity.class);
            startActivity(t);

        }else{
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();
        }
    }

    public void updateLatecomers() {

        Query myTopPostsQuery = mDatabase.child("latecomers").child(firebaseDate).orderByChild("time");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartList.clear();
                for (DataSnapshot qr_codes : dataSnapshot.getChildren()) {


                    String lateMin = qr_codes.child("time").getValue().toString();

                    if(lateMin.equals("day")){

                        String qr_code = qr_codes.getKey();
                        Cursor studentC = getStudentByQrCode(qr_code);
                        if (((studentC != null) && (studentC.getCount() > 0))) {
                            studentC.moveToNext();

                            cartList.add(new Student(studentC.getString(1), studentC.getString(2), lateMin, studentC.getString(3)));
                        }

                    }
                }

                Collections.reverse(cartList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        });
    }

    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + STUDENT_LIST + " WHERE qr_code=?", new String[]{qr_code});
        return res;
    }

    public Cursor getStudentQrCodeByName(String name) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME2_STUDENTS + " WHERE name=?", new String[]{name});
        return res;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof AbsentListAdapter.MyViewHolder) {
            String name = cartList.get(viewHolder.getAdapterPosition()).getInfo();

            Cursor cursor = getStudentQrCodeByName(name);

            if (((cursor != null) && (cursor.getCount() > 0))) {
                cursor.moveToNext();
                qr_code = cursor.getString(0);

                deleteLatecomerFromFirebase(qr_code);
            }

            final Student deletedItem = cartList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            mAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar.make(coordinatorLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);

            snackbar.setAction("UNDO", new OnClickListener() {
                @Override
                public void onClick(View view) {

                    insertLatecomer(qr_code);
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    public boolean insertLatecomer(String qr_code) {
        mDatabase.child("latecomers").child(firebaseDate).child(qr_code).child("time").setValue("day");

        if(checkerHashMap.containsKey(qr_code)) {
            int lateCount = checkerHashMap.get(qr_code);

            if (lateCount >= 2) {
                mDatabase.child("punished").child("" + qr_code).setValue("friday");
            }
        }
        return true;
    }

    public void updateWeeklyLatecomers() {
        Query myTopPostsQuery = mDatabase.child("latecomers");
        checkerHashMap.clear();

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {
                    for (DataSnapshot students : dataDates.getChildren()) {

                        String qr_code = students.getKey();

                        if (checkerHashMap.containsKey(qr_code)) {
                            int n = checkerHashMap.get(qr_code);
                            checkerHashMap.put(qr_code, n + 1);

                            if (n >= 2) {
                                mDatabase.child("punished").child("" + qr_code).setValue("friday");
                            }

                        } else {
                            checkerHashMap.put(qr_code, 1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void deleteLatecomerFromFirebase(String qr_code) {
        mDatabase.child("latecomers").child(firebaseDate).child("" + qr_code).removeValue();
        mDatabase.child("punished").child(qr_code).removeValue();
        updateWeeklyLatecomers();
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd_MM_yyyy");//2001.07.04
        dateFr = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        timeF = new SimpleDateFormat("HH:mm");//14:08

        date = dateF.format(Calendar.getInstance().getTime());
        firebaseDate = dateFr.format(Calendar.getInstance().getTime());
        time = timeF.format(Calendar.getInstance().getTime());

        tvDate.setText(date.replace('_', '.'));
    }

    public void createRecycleView() {

        cartList = new ArrayList<>();
        mAdapter = new AbsentListAdapter(getActivity(), cartList);
        recyclerView = view.findViewById(R.id.recycler_view);
        checkerHashMap = new HashMap<>();

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        itemTouchHelperCallback = new RecyclerItemAbsentHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}