package kz.incubator.myktybake.callofdutyteacher.moderator_files;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arbelkilani.bicoloredprogress.BiColoredProgress;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.docs_fragments.JobsFragment;
import kz.incubator.myktybake.callofdutyteacher.module.Job;
import kz.incubator.myktybake.callofdutyteacher.module.Lesson;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;

public class TeacherJobActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView tPhoto;
    TextView tInfo, tProgress;
    DatabaseReference teacherRef, lessonRef;
    BiColoredProgress teacherProgressPhoto;
    List<Lesson> cartList;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    RecyclerDataAdapter recyclerDataAdapter;
    HashMap<String, String> jobsHashMap, lessonHashMap;
    Button btnLessonDel;
    String clickedLName;
    int cNewJobs = 0, cChecking = 0, cFinished = 0, lessonCount = 0;
    FloatingActionButton fab;
    ArrayList<GroupDataItem> groupsList, groupsListNewJobs, groupsListCheckingJobs, groupsListFinishedJobs, semestrGroup;
    ArrayList<StudentsItem> childStoreNewJobs, childStoreCheckingJobs, childStoreFinishedJobs, childLessons;
    String tKey;
    Dialog teacherLessonsDialog, lessonEditDialog, addNewJobDialog;
    RecyclerView jobListRecyclerView, lessonListRecyclerView;
    RecyclerView.LayoutManager linearLayoutManager, linearLayoutManager2;
    JobTypesAdapter jobTypesAdapter, lessonListAdapter;
    String jobTypes[];
    BottomNavigationView navigation;
    Button lessonBtn;
    boolean firstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_job);
        setTitle(getResources().getString(R.string.my_jobs));

        createView();

    }

    public void createView() {

        cartList = new ArrayList<>();
        jobsHashMap = new HashMap<>();
        lessonHashMap = new HashMap<>();
        groupsList = new ArrayList<>();
        semestrGroup = new ArrayList<>();
        jobTypes = getResources().getStringArray(R.array.jobTypes);

        tPhoto = findViewById(R.id.teacherPhoto);
        tInfo = findViewById(R.id.teacherInfo);
        tProgress = findViewById(R.id.teacherProgress);
        teacherProgressPhoto = findViewById(R.id.twice_colored_progress);
        fab = findViewById(R.id.addNewJob);
        lessonBtn = findViewById(R.id.teacherLessonsBtn);
        recyclerView = findViewById(R.id.recycler_view);

        teacherRef = FirebaseDatabase.getInstance().getReference();
        lessonRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager2 = new LinearLayoutManager(this);


        //------------ Lesson List Dialog

        teacherLessonsDialog = new Dialog(this);
        teacherLessonsDialog.setContentView(R.layout.dialog_add_new_job_recycler);
        teacherLessonsDialog.setTitle(getResources().getString(R.string.lessonList));

        lessonListRecyclerView = teacherLessonsDialog.findViewById(R.id.recyclerView);
        lessonListRecyclerView.setHasFixedSize(true);
        lessonListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonListRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //lessonListAdapter = new JobTypesAdapter(this, lessonList);
//        lessonListRecyclerView.setAdapter(lessonListAdapter);

        //------------
        addNewJobDialog = new Dialog(this);
        addNewJobDialog.setContentView(R.layout.dialog_add_new_job_recycler);
        addNewJobDialog.setTitle(getResources().getString(R.string.addingNewJob));
        jobListRecyclerView = addNewJobDialog.findViewById(R.id.recyclerView);
        jobListRecyclerView.setHasFixedSize(true);

        jobListRecyclerView.setLayoutManager(linearLayoutManager);
        jobListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        jobListRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        jobTypesAdapter = new JobTypesAdapter(this, jobTypes);
        jobListRecyclerView.setAdapter(jobTypesAdapter);

        fab.setOnClickListener(this);
        lessonBtn.setOnClickListener(this);

        lessonEditDialog = new Dialog(this);
        lessonEditDialog.setContentView(R.layout.dialog_lesson_edit);

        btnLessonDel = lessonEditDialog.findViewById(R.id.btnDel);
        btnLessonDel.setOnClickListener(this);

        Intent t = getIntent();
        tKey = t.getStringExtra("teacher_key");

        getTeachersData(tKey);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNewJob:
                addNewJobDialog.show();
                break;
            case R.id.teacherLessonsBtn:
                teacherLessonsDialog.show();
                break;

        }
    }


    public void getLessons() {
        lessonRef = teacherRef.child("lessons");
        lessonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                semestrGroup = new ArrayList<>();

                GroupDataItem groupNewJ;
                String parentKey;
                lessonCount = 0;

                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {
                    parentKey = dataDates.getKey();

                    childLessons = new ArrayList<>();

                    for (DataSnapshot lessonSnapshot : dataDates.getChildren()) {
                        String jobKey = lessonSnapshot.getKey();
                        Lesson lesson = lessonSnapshot.getValue(Lesson.class);

                        childLessons.add(new StudentsItem(""+lesson.getName()+"\nСағат саны: "+lesson.getHours()));

                        lessonHashMap.put(lesson.getName(), jobKey);
                    }


                    groupNewJ = new GroupDataItem(childLessons);
                    groupNewJ.setParentName(parentKey);
                    semestrGroup.add(groupNewJ);
                }

                modifyLessonsAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void getTeachersData(String tEmail) {

        teacherRef = teacherRef.child("personnel_store").child("store").child(tEmail);
        Query myTopPostsQuery = teacherRef.child("jobs");

        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupsListNewJobs = new ArrayList<>();
                groupsListCheckingJobs = new ArrayList<>();
                groupsListFinishedJobs = new ArrayList<>();

                GroupDataItem groupNewJ, checkJ, finishedJ;
                String parentKey;
                cNewJobs = 0;
                cChecking = 0;
                cFinished = 0;

                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {
                    parentKey = dataDates.getKey();

                    childStoreNewJobs = new ArrayList<>();
                    childStoreCheckingJobs = new ArrayList<>();
                    childStoreFinishedJobs = new ArrayList<>();

                    for (DataSnapshot jobSnapshot : dataDates.getChildren()) {
                        String jobKey = jobSnapshot.getKey();

                        Job job = jobSnapshot.getValue(Job.class);

                        if (job.getStatus().equals("new")) {
                            childStoreNewJobs.add(new StudentsItem("" + job.getName()));
                        } else if (job.getStatus().equals("checking")) {
                            childStoreCheckingJobs.add(new StudentsItem("" + job.getName()));
                        } else {
                            childStoreFinishedJobs.add(new StudentsItem("" + job.getName()));
                        }

                        jobsHashMap.put(job.getName(), jobKey);
                    }

                    if (childStoreNewJobs.size() != 0) {
                        groupNewJ = new GroupDataItem(childStoreNewJobs);
                        groupNewJ.setParentName(parentKey);
                        groupsListNewJobs.add(groupNewJ);

                        cNewJobs += childStoreNewJobs.size();
                    }

                    if (childStoreCheckingJobs.size() != 0) {
                        checkJ = new GroupDataItem(childStoreCheckingJobs);
                        checkJ.setParentName(parentKey);
                        groupsListCheckingJobs.add(checkJ);
                        cChecking += childStoreCheckingJobs.size();
                    }
                    if (childStoreFinishedJobs.size() != 0) {
                        finishedJ = new GroupDataItem(childStoreFinishedJobs);
                        finishedJ.setParentName(parentKey);
                        groupsListFinishedJobs.add(finishedJ);
                        cFinished += childStoreFinishedJobs.size();
                    }
                }

                if(!firstTime || navigation.getSelectedItemId() == R.id.new_jobs){
                    groupsList = groupsListNewJobs;
                }else  if(navigation.getSelectedItemId() == R.id.checking_jobs){
                    groupsList = groupsListCheckingJobs;
                }else  if(navigation.getSelectedItemId() == R.id.finished_jobs){
                    groupsList = groupsListFinishedJobs;
                }

                modifyAdapter();
                firstTime = true;
                countProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Teacher teacher = dataSnapshot.getValue(Teacher.class);

                tInfo.setText(teacher.getInfo());

                Glide.with(TeacherJobActivity.this)
                        .load(teacher.getPhoto())
                        .placeholder(R.drawable.user_def)
                        .into(tPhoto);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getLessons();

    }

    public void countProgress() {

        int all = cFinished + cNewJobs + cChecking;
        if(all==0) all = 1;

        long res = cFinished * 100 / all;
        teacherRef.child("progress").setValue(res);
        teacherProgressPhoto.setProgress(res);
        teacherProgressPhoto.setAnimated(true, 3000, new BounceInterpolator());

        if (res < 50) {
            tProgress.setTextColor(getResources().getColor(R.color.red));
        } else if (res >= 50 && res <= 75) {
            tProgress.setTextColor(getResources().getColor(R.color.orange));
        } else {
            tProgress.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        tProgress.setText("Productivity: " + (int) res + " %");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.new_jobs:
                    groupsList = groupsListNewJobs;
                    fab.setVisibility(View.VISIBLE);

                    modifyAdapter();
                    return true;
                case R.id.checking_jobs:
                    groupsList = groupsListCheckingJobs;
                    fab.setVisibility(View.INVISIBLE);
                    modifyAdapter();

                    return true;
                case R.id.finished_jobs:
                    groupsList = groupsListFinishedJobs;
                    fab.setVisibility(View.INVISIBLE);
                    modifyAdapter();

                    return true;
            }
            return false;
        }
    };

    public void modifyAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void modifyLessonsAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(getLessonsData());
        lessonListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonListRecyclerView.setAdapter(recyclerDataAdapter);
        lessonListRecyclerView.setHasFixedSize(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            //mAuth.signOut();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<GroupDataItem> getGroupsData() {
        return groupsList;
    }

    private ArrayList<GroupDataItem> getLessonsData() {
        return semestrGroup;
    }

    private class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing2, parent, false);
            return new RecyclerDataAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerDataAdapter.MyViewHolder holder, int position) {
            GroupDataItem dummyParentDataItem = dummyParentDataItems.get(position);
            holder.textView_parentName.setText(dummyParentDataItem.getParentName());

            int noOfChildTextViews = holder.linearLayout_childItems.getChildCount();
            int noOfChild = dummyParentDataItem.getChildDataItems().size();

            holder.tvCount.setText("" + noOfChild);

            if (noOfChild < noOfChildTextViews) {
                for (int index = noOfChild; index < noOfChildTextViews; index++) {
                    TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(index);
                    currentTextView.setVisibility(View.GONE);
                }
            }
            for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(textViewIndex);
                String childName = dummyParentDataItem.getChildDataItems().get(textViewIndex).getChildName();
                childName = childName.replace("semestr","семестр");
                currentTextView.setText(childName);
            }
        }

        @Override
        public int getItemCount() {
            return dummyParentDataItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private Context context;
            private TextView textView_parentName, tvCount;
            private LinearLayout linearLayout_childItems;

            MyViewHolder(View itemView) {
                super(itemView);
                context = itemView.getContext();
                textView_parentName = itemView.findViewById(R.id.tv_parentName);
                tvCount = itemView.findViewById(R.id.tv_count);
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
                    textView.setTextSize(16.0f);
                    textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);//LinearLayout.LayoutParams.WRAP_CONTENT);
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
                    clickedLName = textViewClicked.getText().toString();
                    textViewClicked.setBackgroundColor(getResources().getColor(R.color.light_green));
                    lessonEditDialog.show();


                    //Toast.makeText(getActivity(), "Yahoo: "+lessonKey.get(clickedSName), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
