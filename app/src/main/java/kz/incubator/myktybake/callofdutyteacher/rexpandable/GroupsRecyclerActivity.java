package kz.incubator.myktybake.callofdutyteacher.rexpandable;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kz.incubator.myktybake.callofdutyteacher.MainActivity;
import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.activity.ScannerActivity;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Student;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME2_STUDENTS;
import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME3;

public class GroupsRecyclerActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private Context mContext;
    private DatabaseReference mDatabase;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    Dialog dialog;
    Cursor sCursor;
    String imgUrl;
    DateFormat dateF, timeF, dateFr;
    String date, time, firebaseDate;
    ArrayList<String> latecomersStore;
    HashMap<String, Integer> checkerHashMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_recycler);

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        mContext = GroupsRecyclerActivity.this;
        mRecyclerView = findViewById(R.id.recyclerView);
        RecyclerDataAdapter recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(recyclerDataAdapter);
        mRecyclerView.setHasFixedSize(true);
        // createResDialog();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        latecomersStore = new ArrayList<>();
        checkerHashMap = new HashMap<>();
        manageDate();
        getAllLateComers();
        updateWeeklyLatecomers();

    }

    private ArrayList<GroupDataItem> getGroupsData() {
        String groups[] = {"1-01", "1-02", "1-03", "1-04", "2-01", "2-02", "2-03", "3-01", "3-02", "3-03", "3-04", "3-09"};

        ArrayList<GroupDataItem> groupsList = new ArrayList<>();
        ArrayList<StudentsItem> studentStore;

        GroupDataItem groupDataItem;

        for (String group : groups) {
            studentStore = new ArrayList<>();
            Cursor cursor = getStudentsByGroup(group);
            if (((cursor != null) && (cursor.getCount() > 0))) {
                while (cursor.moveToNext()) {
                    studentStore.add(new StudentsItem(cursor.getString(1)));
                }
            }
            groupDataItem = new GroupDataItem(studentStore);
            groupDataItem.setParentName(group);
            groupsList.add(groupDataItem);
        }

        return groupsList;
    }

    private class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerDataAdapter.MyViewHolder holder, int position) {
            GroupDataItem dummyParentDataItem = dummyParentDataItems.get(position);
            holder.textView_parentName.setText(dummyParentDataItem.getParentName());
            //
            int noOfChildTextViews = holder.linearLayout_childItems.getChildCount();
            int noOfChild = dummyParentDataItem.getChildDataItems().size();
            if (noOfChild < noOfChildTextViews) {
                for (int index = noOfChild; index < noOfChildTextViews; index++) {
                    TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(index);
                    currentTextView.setVisibility(View.GONE);
                }
            }
            for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(textViewIndex);
                currentTextView.setText(dummyParentDataItem.getChildDataItems().get(textViewIndex).getChildName());

                if(latecomersStore.contains(currentTextView.getText().toString())){
                    currentTextView.setBackgroundColor(getResources().getColor(R.color.red));
                }
            }
        }

        @Override
        public int getItemCount() {
            return dummyParentDataItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private Context context;
            private TextView textView_parentName;
            private LinearLayout linearLayout_childItems;

            MyViewHolder(View itemView) {
                super(itemView);
                context = itemView.getContext();
                textView_parentName = itemView.findViewById(R.id.tv_parentName);
                linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
                linearLayout_childItems.setVisibility(View.GONE);
                int intMaxNoOfChild = 0;

                for (int index = 0; index < dummyParentDataItems.size(); index++) {
                    int intMaxSizeTemp = dummyParentDataItems.get(index).getChildDataItems().size();
                    if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp;
                }
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    TextView textView = new TextView(context);
                    textView.setId(indexView);
                    textView.setPadding(20, 20, 0, 20);
                    textView.setTextSize(20.0f);
                    //textView.setGravity(Gravity.CENTER);
                    textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);//LinearLayout.LayoutParams.WRAP_CONTENT);
                    textView.setOnClickListener(this);

                    linearLayout_childItems.addView(textView, layoutParams);
                }
                textView_parentName.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.tv_parentName) {
                    if (linearLayout_childItems.getVisibility() == View.VISIBLE) {
                        linearLayout_childItems.setVisibility(View.GONE);
                    } else {
                        linearLayout_childItems.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(isNetworkAvailable(GroupsRecyclerActivity.this)){

                        textViewClicked = (TextView) view;
                        textViewClicked.setBackgroundColor(getResources().getColor(R.color.red));
                        clickedSName = textViewClicked.getText().toString();

                        if(!latecomersStore.contains(clickedSName)){
                            createResDialog();
                        }

                    }else{
                        Toast.makeText(GroupsRecyclerActivity.this, "Check internet connection", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    TextView textViewClicked;
    String qr_code;
    String clickedSName;

    public void createResDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.scann_res);

        TextView studentName = dialog.findViewById(R.id.sName);
        ImageView studentImg = dialog.findViewById(R.id.imageV);

        Button btOk = dialog.findViewById(R.id.buttonOk);
        Button btCancel = dialog.findViewById(R.id.buttonCancel);

        studentName.setText(clickedSName);

        sCursor = getStudentsByName(clickedSName);

        if (((sCursor != null) && (sCursor.getCount() > 0))) {
            sCursor.moveToNext();

            qr_code = sCursor.getString(0);
            imgUrl = sCursor.getString(3);
        }

        Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.s_icon)
                .into(studentImg);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                time = timeF.format(Calendar.getInstance().getTime());
                String lateMin = lateMinute(time);
                insertLatecomer(qr_code, lateMin);

                dialog.dismiss();

                Intent t = new Intent(GroupsRecyclerActivity.this, MainActivity.class);
                startActivity(t);

            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //textViewClicked.setBackgroundColor(getResources().getColor(R.color.red));
                textViewClicked.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_sub_module_text));
//                textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
            }
        });

        dialog.show();
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

        mDatabase.child("latecomers").child(firebaseDate).child("" + qr_code).child("time").setValue(lateMin);

        if(checkerHashMap.containsKey(qr_code)) {
            int lateCount = checkerHashMap.get(qr_code);

            if (lateCount >= 2) {
                mDatabase.child("punished").child("" + qr_code).setValue("friday");
            }
        }

        return true;
    }

    public void getAllLateComers(){

        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (((res != null) && (res.getCount() > 0))) {
            while(res.moveToNext()) {
                String lqrCode = res.getString(0);
                Cursor studentC = getStudentByQrCode(lqrCode);

                if (((studentC != null) && (studentC.getCount() > 0))) {
                    studentC.moveToNext();

                    latecomersStore.add(studentC.getString(1));
                }
            }
        }
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd_MM_yyyy");//2001.07.04
        dateFr = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        timeF = new SimpleDateFormat("HH:mm");//14:08

        date = dateF.format(Calendar.getInstance().getTime());
        firebaseDate = dateFr.format(Calendar.getInstance().getTime());
        time = timeF.format(Calendar.getInstance().getTime());

    }

    public String lateMinute(String time) {
        Date t8_30 = null;
        Date currentTime = null;
        String dateStart = "08:30";
        String text = "0";

        try {
            t8_30 = timeF.parse(dateStart);
            currentTime = timeF.parse(time);

            long diff = currentTime.getTime() - t8_30.getTime();

            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffMinutes = diff / (60 * 1000) % 60;

            if (diffHours > 0 || diffMinutes >= 15) {
                //otrabotka 80 min
                text = "" + (diffHours * 60 + diffMinutes);

            } else if (diffMinutes <= 10) {
                //otrabotka 40 min
                text = "" + diffMinutes;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    String TABLE_NAME2_STUDENTS = "students_list";

    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME2_STUDENTS + " WHERE qr_code=?", new String[]{qr_code});
        return res;
    }

    public Cursor getStudentsByGroup(String sgroup) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME2_STUDENTS + " WHERE s_group=?", new String[]{sgroup});
        return res;
    }

    public Cursor getStudentsByName(String sname) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_NAME2_STUDENTS + " WHERE name=?", new String[]{sname});
        return res;
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

//        String imgUrl = sCursor.getString(3);
//        imgUrl = "https://firebasestorage.googleapis.com/v0/b/sdcl-f9f00.appspot.com/o/teachers%2Fuser.png?alt=media&token=4ffef8e6-6327-42dc-8ba2-cddfa63b32a1";
//
//        Glide.with(this)
//                .load(imgUrl)
//                .into(studentImg);