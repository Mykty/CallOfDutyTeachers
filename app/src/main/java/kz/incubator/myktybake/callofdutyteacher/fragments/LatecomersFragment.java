package kz.incubator.myktybake.callofdutyteacher.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
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
import kz.incubator.myktybake.callofdutyteacher.activity.ScannerActivity;
import kz.incubator.myktybake.callofdutyteacher.adapters.CartListAdapter;
import kz.incubator.myktybake.callofdutyteacher.adapters.RecyclerItemTouchHelper;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Student;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupsRecyclerActivity;


public class LatecomersFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, OnClickListener {
    private RecyclerView recyclerView;
    private List<Student> cartList;
    private CartListAdapter mAdapter;
    private RelativeLayout coordinatorLayout, dialogLayout;
    private DatabaseReference mDatabase;

    FloatingActionButton addBtn;
    String date, time, firebaseDate;
    String qr_code, lateTime;
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
    HashMap<String, Integer> checkerHashMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);

        getActivity().setTitle("Кешігіп келгендер");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        cartList = new ArrayList<>();
        mAdapter = new CartListAdapter(getActivity(), cartList);
        checkerHashMap = new HashMap<>();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        recyclerView = view.findViewById(R.id.recycler_view);
        tvDate = view.findViewById(R.id.textViewDate);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        addBtn = view.findViewById(R.id.addBtn);

        createRecycleView();
        manageDate();
        updateLatecomers();
        createQrDialog();

        addBtn.setOnClickListener(this);
        return view;

    }

    @Override
    public void onClick(View view) {
        if (isNetworkAvailable(getActivity())) {
            qr_dialog.show();
        } else {
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();
        }

    }

    public void updateLatecomers() {

        Query myTopPostsQuery = mDatabase.child("latecomers").child(firebaseDate).orderByChild("time");

        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartList.clear();
                storeDb.cleanLatecomersTable(sqdb);

                for (DataSnapshot qr_codes : dataSnapshot.getChildren()) {

                    String lateMin = qr_codes.child("time").getValue().toString();
                    String qr_code = qr_codes.getKey();
                    insertToDb(qr_code, lateMin);

                    if (!lateMin.equals("day")) {

                        Cursor studentC = getStudentByQrCode(qr_code);

                        if (((studentC != null) && (studentC.getCount() > 0))) {
                            studentC.moveToNext();

                            cartList.add(new Student(studentC.getString(1), studentC.getString(2), lateMin+" минут", studentC.getString(3)));
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

    public boolean insertLatecomer(String qr_code, String lateMin) {

        insertToDb(qr_code, lateMin);
        mDatabase.child("latecomers").child(firebaseDate).child("" + qr_code).child("time").setValue(lateMin);

        if(checkerHashMap.containsKey(qr_code)) {
            int lateCount = checkerHashMap.get(qr_code);

            if (lateCount >= 2) {
                mDatabase.child("punished").child("" + qr_code).setValue("friday");
            }
        }

//        mDatabase.child("latecomers").child(firebaseDate).child("" + qr_code).child("adding_server_time").setValue(ServerValue.TIMESTAMP);

        return true;
    }

    int count = 0;

    public void insertToDb(String qr_code, String lateMin) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("qr_code", qr_code);
        contentValues.put("late_min", lateMin);
        sqdb.insert("late_list", null, contentValues);
        count++;
    }

    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + STUDENT_LIST + " WHERE qr_code=?", new String[]{qr_code});
        return res;
    }

    public Cursor getTimeLateFromLatecomers(String qr_c) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + LATE_LIST + " WHERE qr_code=?", new String[]{qr_c});
        return res;
    }

    public Cursor getStudentQrCodeByName(String name) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME2_STUDENTS + " WHERE name=?", new String[]{name});
        return res;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartListAdapter.MyViewHolder) {
            String name = cartList.get(viewHolder.getAdapterPosition()).getInfo();
            String time = cartList.get(viewHolder.getAdapterPosition()).getTime();
            String slitStore[] = time.split(" ");
            lateTime = slitStore[0];

            Cursor cursor = getStudentQrCodeByName(name);

            if (((cursor != null) && (cursor.getCount() > 0))) {
                cursor.moveToNext();
                qr_code = cursor.getString(0);

                storeDb.deleteLatecomer(sqdb, qr_code);
                deleteLatecomerFromFirebase(qr_code);
            }

            final Student deletedItem = cartList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            mAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar.make(coordinatorLayout, name + " removed from cart! lateTime: " + lateTime, Snackbar.LENGTH_LONG);

            snackbar.setAction("UNDO", new OnClickListener() {
                @Override
                public void onClick(View view) {

                    insertLatecomer(qr_code, lateTime);
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                    updateLatecomers();
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            updateLatecomers();
        }
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
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    }

    public void createQrDialog() {

        qr_dialog = new Dialog(getActivity());
        qr_dialog.setContentView(R.layout.qr_dialog);

        btnScanner = (Button) qr_dialog.findViewById(R.id.btnScanner);
        btnSearch = (Button) qr_dialog.findViewById(R.id.btnSearch);

        dialogLayout = qr_dialog.findViewById(R.id.container);

        btnScanner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent t = new Intent(getActivity(), ScannerActivity.class);
                startActivity(t);
            }
        });

        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent t = new Intent(getActivity(), GroupsRecyclerActivity.class);
                startActivity(t);
            }
        });

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal == 0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}

// String newKey = mDatabase.child(date).child("list").push().getKey();
//   mDatabase.child(date).child("list").child(""+qr_code).child("qr_id").setValue("001");


    /*

        imageStoreRef = FirebaseStorage.getInstance().getReference();

    public void downloadPhoto(){
        imageStoreRef = imageStoreRef.child("teachers/user.png");

        try {
            final File localFile = File.createTempFile("images", "jpg");
            imageStoreRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    mImageView.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e ) {}

    }
    */


/*

       // createQrDialog();

    public void createQrDialog() {

        qr_dialog = new Dialog(getActivity());
        qr_dialog.setContentView(R.layout.qr_dialog);

        qr_button = (Button) qr_dialog.findViewById(R.id.btnQrOk);
        qr_editText = (EditText) qr_dialog.findViewById(R.id.qr_code);

        dialogLayout = qr_dialog.findViewById(R.id.container);

        qr_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                entered_qr_code = qr_editText.getText().toString();
                if (entered_qr_code.length() > 0) {

                   /* time = timeF.format(Calendar.getInstance().getTime());
                    String lateMin = lateMinute(time);
                    insertLatecomer(entered_qr_code, lateMin);

                    qr_editText.setText("");
                            qr_dialog.dismiss();

                            } else {
                            Snackbar snackbar = Snackbar.make(dialogLayout, "Enter qr code", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            }
                            }
                            });
                            }
 */