package kz.incubator.myktybake.callofdutyteacher.fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.adapters.CartListAdapter;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Student;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.AddAbsentsActivity;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;

import static kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase.TABLE_NAME2_STUDENTS;


public class PunishedReportFragment extends Fragment implements View.OnClickListener{
    private RecyclerView recyclerView;
    private List<Student> cartList;
    private DatabaseReference mDatabase;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String STUDENT_LIST = "students_list";
    HashMap<String, Integer> checker;
    View view;
    TextView fridayTv;
    ArrayList<String> absentNames;
    DateFormat dateF;
    String date;
    boolean fridayCheck = false;
    FloatingActionButton btnSave;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_punished, container, false);
        fridayTv = view.findViewById(R.id.textViewDate);

        getActivity().setTitle("Жазаланушылар");

//        String s = "<b>Bolded text</b>, <i>italic text</i>, even <u>underlined</u>!";
        String s = "Жұма(" + getFridayDate() + ") күні жазалануға қалған студенттер тізімі";
        fridayTv.setText(Html.fromHtml(s));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        absentNames = new ArrayList<>();
        btnSave = view.findViewById(R.id.btnSave);
        btnSave.setVisibility(View.INVISIBLE);
        btnSave.setOnClickListener(this);

        createRecycleView();
        updatePunishedLatecomers();

        if(getTodayDate().equals(getFridayDate())){
            fridayCheck = true;
        }
        return view;

    }

    public void updatePunishedLatecomers() {
        Query myTopPostsQuery = mDatabase.child("punished");

        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //storeDb.cleanWeeklyLatecomersTable(sqdb);
                cartList.clear();

                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {

                    String qr_code = dataDates.getKey();

                    Cursor studentC = getStudentByQrCode(qr_code);

                    if (((studentC != null) && (studentC.getCount() > 0))) {
                        studentC.moveToNext();
                        cartList.add(new Student(studentC.getString(1), studentC.getString(2), "day", studentC.getString(3)));
                    }
                }

                modifyAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private ArrayList<GroupDataItem> getGroupsData() {
        String groups[] = {"1-01", "1-02", "1-03", "1-04", "2-01", "2-02", "2-03"};

        ArrayList<GroupDataItem> groupsList = new ArrayList<>();
        ArrayList<StudentsItem> studentStore;

        GroupDataItem groupDataItem;
        boolean findGroup = false;

        for (String group : groups) {
            studentStore = new ArrayList<>();
            findGroup = false;

            for (Student s : cartList) {
                if (group.equals(s.getGroup())) {
                    studentStore.add(new StudentsItem(s.getInfo()));
                    findGroup = true;
                }
            }
            if (findGroup) {
                groupDataItem = new GroupDataItem(studentStore);
                groupDataItem.setParentName(group);
                groupsList.add(groupDataItem);
            }
        }

        return groupsList;
    }

    public void createRecycleView() {
        checker = new HashMap<String, Integer>();
        cartList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    public void modifyAdapter() {
        RecyclerDataAdapter recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + STUDENT_LIST + " WHERE qr_code=?", new String[]{qr_code});
        return res;
    }

    @Override
    public void onClick(View view) {
        String qr_code;

        for(String sName: absentNames){
            Cursor qrCursor = getStudentQrCodeByName(sName);
            if (((qrCursor != null) && (qrCursor.getCount() > 0))) {
                qrCursor.moveToNext();
                qr_code = qrCursor.getString(0);
                mDatabase.child("punished").child(qr_code).removeValue();
                mDatabase.child("latecomers").removeValue();
            }
        }

        btnSave.setVisibility(View.INVISIBLE);
    }

    private class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing, parent, false);
            return new RecyclerDataAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerDataAdapter.MyViewHolder holder, int position) {
            GroupDataItem dummyParentDataItem = dummyParentDataItems.get(position);
            holder.textView_parentName.setText(dummyParentDataItem.getParentName());

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

                    TextView textViewClicked = (TextView) view;
                    String clickedSName = textViewClicked.getText().toString();

                    textViewClicked.setBackgroundColor(getResources().getColor(R.color.light_green));


                    if (absentNames.contains(clickedSName)) {
                        textViewClicked.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                        int index = absentNames.indexOf(clickedSName);

                        absentNames.remove(index);

                    } else {
                        absentNames.add(clickedSName);
                    }

                    if(fridayCheck) btnSave.setVisibility(View.VISIBLE);
                    if(absentNames.size()==0) btnSave.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public Cursor getStudentQrCodeByName(String name) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + STUDENT_LIST + " WHERE name=?", new String[]{name});
        return res;
    }

    public String getTodayDate() {
        dateF = new SimpleDateFormat("dd.MM");
        date = dateF.format(Calendar.getInstance().getTime());
        return date;
    }

    public String getFridayDate() {

        Calendar fridayDate = Calendar.getInstance();
        fridayDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        SimpleDateFormat dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        String friday = dateF.format(fridayDate.getTime());

        return friday;

    }
}