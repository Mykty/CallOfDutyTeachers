package kz.incubator.myktybake.callofdutyteacher.fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;


public class WeeklyReportFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Student> cartList;
    private CartListAdapter mAdapter;
    private DatabaseReference mDatabase;

    String date, time, firebaseDate;
    String qr_code;
    DateFormat dateF, timeF, dateFr;
    TextView tvDate;
    RecyclerView.LayoutManager mLayoutManager;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String STUDENT_LIST = "students_list";
    String TABLE_NAME2_STUDENTS = "students_list";
    HashMap<String, Integer> checker;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report, container, false);

        getActivity().setTitle("Апталық қорытынды");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        createRecycleView();
        updateWeeklyLatecomers();

        return view;

    }

    public void updateWeeklyLatecomers() {
        Query myTopPostsQuery = mDatabase.child("latecomers");

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //storeDb.cleanWeeklyLatecomersTable(sqdb);

                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {

                    String dates = dataDates.getKey();

                    for (DataSnapshot students : dataDates.getChildren()) {

                        String qr_code = students.getKey();

                        if (checker.containsKey(qr_code)) {
                            int n = checker.get(qr_code);
                            checker.put(qr_code, n + 1);
                        } else {
                            checker.put(qr_code, 1);
                        }
                    }
                }

                cartList.clear();

                for (String qr_code : checker.keySet()) {

                    Cursor studentC = getStudentByQrCode(qr_code);

                    if (((studentC != null) && (studentC.getCount() > 0))) {
                        studentC.moveToNext();
                        int late_count = checker.get(qr_code);

                        cartList.add(new Student(studentC.getString(1), studentC.getString(2), "" + late_count + " рет", studentC.getString(3)));
                    }
                }

//                Collections.reverse(cartList);
//                mAdapter.notifyDataSetChanged();
                modifyAdapter();
                System.out.println(checker);
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

            for(Student s: cartList){
                if(group.equals(s.getGroup())){
                    studentStore.add(new StudentsItem(s.getInfo()+" - "+s.getTime()));
                    findGroup = true;
                }
            }
            if(findGroup) {
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
        mAdapter = new CartListAdapter(getActivity(), cartList);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    public void modifyAdapter(){
        RecyclerDataAdapter recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }
    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + STUDENT_LIST + " WHERE qr_code=?", new String[]{qr_code});
        return res;
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

                }
            }
        }
    }

    public String getMondayDate() {

        Calendar mondayDate = Calendar.getInstance();
        mondayDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat dateF = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        String monday = dateF.format(mondayDate.getTime());

        return monday;

    }

    public String getFridayDate() {

        Calendar fridayDate = Calendar.getInstance();
        fridayDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        SimpleDateFormat dateF = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        String friday = dateF.format(fridayDate.getTime());

        return friday;

    }
}

                        /*
                     M: 30.04.2018
                        31.04.2018
                        01.05.2018
                        02.05.2018
                        03.05.2018
                     F: 04.05.2018

                    String datesSplit[] = dates.split("_");

                    int month = Integer.parseInt(datesSplit[1]);
                    int day = Integer.parseInt(datesSplit[0]);

                    if(month == mMonth && (day >= mDay && day <=fDay)){
                    }else if(month > mMonth && day >= mDay && day <=fDay){

                    }


    int mDay, mMonth, fDay, fMonth;

    String mondaySplit[] = getMondayDate().split("_");
    String fridaySplit[] = getFridayDate().split("_");

    mDay = Integer.parseInt(mondaySplit[0]);
    mMonth = Integer.parseInt(mondaySplit[1]);

    fDay = Integer.parseInt(fridaySplit[0]);
    fMonth = Integer.parseInt(fridaySplit[1]);

                         */
