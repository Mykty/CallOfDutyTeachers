package kz.incubator.myktybake.callofdutyteacher.moderator_files;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import kz.incubator.myktybake.callofdutyteacher.module.Job;
import kz.incubator.myktybake.callofdutyteacher.module.Lesson;
import kz.incubator.myktybake.callofdutyteacher.module.RecyclerItemClickListener;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;

public class TeacherJobActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView tPhoto;
    TextView tInfo, tProgress, lessonTx;
    Button btnLessonDel;
    FloatingActionButton fab;
    Button lessonBtn, addNewLessonBtn, enterLessonBtn, btnCheck, btnDel, btnFinished;
    Spinner semestrSp, courseSp, lessonsSpn;
    EditText lessonName, lessonHour, editDesc;
    TextInputLayout tvInput1;

    String clickedLName;
    String tKey, jobType, keyJob, parentStr, mistakeStr;
    String jobTypes[];
    boolean firstTime = false, lessonJob = false, checkMenuPressed = false;
    int cNewJobs = 0, cChecking = 0, cFinished = 0, lessonCount = 0;

    FirebaseUser currentUser;
    DatabaseReference teacherRef, lessonRef;

    ArrayList<GroupDataItem> groupsList, groupsListNewJobs, groupsListCheckingJobs, groupsListFinishedJobs, semestrGroup;
    ArrayList<StudentsItem> childStoreNewJobs, childStoreCheckingJobs, childStoreFinishedJobs, childLessons;
    ArrayList<String> lessonList;
    HashMap<String, String> lessonHashMap;
    HashMap<String, Job> jobStoreHashMap;
    List<Lesson> cartList;

    RecyclerView recyclerView;
    RecyclerLessonDataAdapter recyclerLessonDataAdapter;
    RecyclerDataAdapter recyclerDataAdapter;
    RecyclerView jobListRecyclerView, lessonListRecyclerView;

    Dialog teacherLessonsDialog, lessonEditDialog, addingLesson, newJobConfigDialog, jobEditDialog, addNewJobDialog;
    JobTypesAdapter jobTypesAdapter;
    BottomNavigationView navigation;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_job2);
        setTitle(getResources().getString(R.string.my_jobs));

        createView();

    }

    public void createView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cartList = new ArrayList<>();
        lessonHashMap = new HashMap<>();
        jobStoreHashMap = new HashMap<>();
        groupsList = new ArrayList<>();
        semestrGroup = new ArrayList<>();
        lessonList = new ArrayList<>();
        jobTypes = getResources().getStringArray(R.array.jobTypes);

        tPhoto = findViewById(R.id.teacherPhoto);
        tInfo = findViewById(R.id.teacherInfo);
        tProgress = findViewById(R.id.teacherProgress);
//        teacherProgressPhoto = findViewById(R.id.twice_colored_progress);
        progressBar = findViewById(R.id.progressBar);

        fab = findViewById(R.id.addNewJob);
        lessonBtn = findViewById(R.id.teacherLessonsBtn);
        recyclerView = findViewById(R.id.recycler_view);

        teacherRef = FirebaseDatabase.getInstance().getReference();
        lessonRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        createTeacherLessonsDialog();
        createAddingNewLessonDialog();
        createAddingNewJobDialog();
        createJobEditDialog();

        fab.setOnClickListener(this);
        lessonBtn.setOnClickListener(this);

        lessonEditDialog = new Dialog(this);
        lessonEditDialog.setContentView(R.layout.dialog_lesson_edit);

        btnLessonDel = lessonEditDialog.findViewById(R.id.btnDel);
        btnLessonDel.setOnClickListener(this);

        Intent t = getIntent();
        tKey = t.getStringExtra("teacher_key");

        getTeachersData(tKey);


    }

    public void createJobEditDialog(){
        jobEditDialog = new Dialog(this);
        jobEditDialog.setContentView(R.layout.dialog_edit_job);

        btnDel = jobEditDialog.findViewById(R.id.btnDel);
        btnCheck = jobEditDialog.findViewById(R.id.btnCheck);
        lessonTx = jobEditDialog.findViewById(R.id.lessonTx);
        btnFinished = jobEditDialog.findViewById(R.id.btnFinished);
        mistakeStr = getResources().getString(R.string.mistakeStr);

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String jobKey = jobStoreHashMap.get(keyJob).getKey();

                if(checkMenuPressed){
                    teacherRef.child("jobs").child(parentStr).child(jobKey).child("name").setValue(clickedLName+" - "+mistakeStr);
                    teacherRef.child("jobs").child(parentStr).child(jobKey).child("status").setValue("new");
                }

                else{

                    if(clickedLName.contains(mistakeStr)) clickedLName = clickedLName.substring(0, clickedLName.indexOf(mistakeStr)-3);
                    teacherRef.child("jobs").child(parentStr).child(jobKey).child("name").setValue(clickedLName);
                    teacherRef.child("jobs").child(parentStr).child(jobKey).child("status").setValue("checking");
                }


                jobEditDialog.dismiss();
            }
        });

        btnFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String jobKey = jobStoreHashMap.get(keyJob).getKey();

                if(clickedLName.contains(mistakeStr)) clickedLName = clickedLName.substring(0, clickedLName.indexOf(mistakeStr)-3);

                teacherRef.child("jobs").child(parentStr).child(jobKey).child("name").setValue(clickedLName);
                teacherRef.child("jobs").child(parentStr).child(jobKey).child("status").setValue("finished");
                jobEditDialog.dismiss();
            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String jobKey = jobStoreHashMap.get(keyJob).getKey();
                teacherRef.child("jobs").child(parentStr).child(jobKey).removeValue();
                jobEditDialog.dismiss();
            }
        });

    }


    public void createAddingNewJobDialog() {

        addNewJobDialog = new Dialog(this, R.style.CustomDialog);

        addNewJobDialog.setContentView(R.layout.dialog_add_new_job_recycler);
        addNewJobDialog.setTitle(getResources().getString(R.string.addingNewJob));
        jobListRecyclerView = addNewJobDialog.findViewById(R.id.recyclerView);
        jobListRecyclerView.setHasFixedSize(true);

        jobListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        jobListRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        jobTypesAdapter = new JobTypesAdapter(this, jobTypes);
        jobListRecyclerView.setAdapter(jobTypesAdapter);

        jobListRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        lessonJob = false;

                        if (position <= 7){
                            lessonJob = true;
                            lessonsSpn.setVisibility(View.VISIBLE);
                            editDesc.setVisibility(View.INVISIBLE);
                            tvInput1.setVisibility(View.INVISIBLE);

                        }else{
                            lessonsSpn.setVisibility(View.INVISIBLE);
                            editDesc.setVisibility(View.VISIBLE);
                            tvInput1.setVisibility(View.VISIBLE);
                        }

                        jobType = jobTypes[position];
                        newJobConfigDialog.setTitle(jobType);

                        newJobConfigDialog.show();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

    }

    public void createLessonJobConfigDialog() {
        newJobConfigDialog = new Dialog(this, R.style.CustomDialog);
        newJobConfigDialog.setContentView(R.layout.dialog_job_config);

        Button enterJob = newJobConfigDialog.findViewById(R.id.enterJob);
        lessonsSpn = newJobConfigDialog.findViewById(R.id.lessonSpinner);
        editDesc = newJobConfigDialog.findViewById(R.id.editDesc);
        tvInput1 = newJobConfigDialog.findViewById(R.id.tvInput1);

        String lessonListStore[] = new String[lessonList.size()];
        lessonListStore = lessonList.toArray(lessonListStore);

        ArrayAdapter<String> adapterL = new ArrayAdapter<>(TeacherJobActivity.this, android.R.layout.simple_spinner_item, lessonListStore);
        adapterL.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        enterJob.setOnClickListener(this);
        lessonsSpn.setAdapter(adapterL);
    }

    public void createAddingNewLessonDialog() {

        addingLesson = new Dialog(this, R.style.CustomDialog);
        addingLesson.setContentView(R.layout.dialog_adding_lesson);
        addingLesson.setTitle(getResources().getString(R.string.addingLesson));
        semestrSp = addingLesson.findViewById(R.id.semestrSpinner);
        courseSp = addingLesson.findViewById(R.id.courseSpinner);
        enterLessonBtn = addingLesson.findViewById(R.id.enterLesson);
        lessonName = addingLesson.findViewById(R.id.lessonName);
        lessonHour = addingLesson.findViewById(R.id.lessonHour);

        String semestrStr[] = getResources().getStringArray(R.array.semestrStore);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semestrStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.courseStrore, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        semestrSp.setAdapter(adapter);
        courseSp.setAdapter(adapter2);

        enterLessonBtn.setOnClickListener(this);
    }

    public void createTeacherLessonsDialog() {
        teacherLessonsDialog = new Dialog(this, R.style.CustomDialog);
        teacherLessonsDialog.setContentView(R.layout.dialog_lesson_list);
        teacherLessonsDialog.setTitle(getResources().getString(R.string.lessonList));

        lessonListRecyclerView = teacherLessonsDialog.findViewById(R.id.recyclerView);
        lessonListRecyclerView.setHasFixedSize(true);
        lessonListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonListRecyclerView.setItemAnimator(new DefaultItemAnimator());

        addNewLessonBtn = teacherLessonsDialog.findViewById(R.id.addNewLesson);
        addNewLessonBtn.setOnClickListener(this);
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

            case R.id.addNewLesson:
                addingLesson.show();
                break;

            case R.id.enterJob:
                String keyU = teacherRef.child("jobs").child(jobType).push().getKey().toString();
                String jobName;

                if(lessonJob) jobName = lessonsSpn.getSelectedItem().toString();
                else jobName = editDesc.getText().toString();

                Job job = new Job(keyU, "new", jobName);
                teacherRef.child("jobs").child(jobType).child(keyU).setValue(job);

                addNewJobDialog.dismiss();
                newJobConfigDialog.dismiss();
                editDesc.getText().clear();

                break;

            case R.id.enterLesson:
                String semestrStr = semestrSp.getSelectedItem().toString();
                String courseStr = courseSp.getSelectedItem().toString();
                String lessontNameStr = lessonName.getText().toString();
                String lessontHourStr = lessonHour.getText().toString();

                if (lessontNameStr.length() == 0)
                    lessonName.setError(getResources().getString(R.string.fill_mistake));
                if (lessontHourStr.length() == 0)
                    lessonName.setError(getResources().getString(R.string.fill_mistake));

                if (lessontNameStr.length() > 0 && lessontHourStr.length() > 0) {
                    String key = lessonRef.child(semestrStr).push().getKey();

                    Lesson lesson = new Lesson(key, courseStr, courseStr + " " + lessontNameStr, Long.parseLong(lessontHourStr));
                    lessonRef.child(semestrStr).child(key).setValue(lesson);

                }

                lessonName.getText().clear();
                lessonHour.getText().clear();
                addingLesson.dismiss();
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
                lessonList.clear();

                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {
                    parentKey = dataDates.getKey();

                    childLessons = new ArrayList<>();

                    for (DataSnapshot lessonSnapshot : dataDates.getChildren()) {
                        String jobKey = lessonSnapshot.getKey();
                        Lesson lesson = lessonSnapshot.getValue(Lesson.class);
                        String itemL = "" + lesson.getName() + "\nСағат саны: " + lesson.getHours();
                        childLessons.add(new StudentsItem(itemL));

                        lessonHashMap.put(lesson.getName(), jobKey);
                        lessonList.add(parentKey + " : " + lesson.getName());
                    }


                    groupNewJ = new GroupDataItem(childLessons);
                    groupNewJ.setParentName(parentKey);
                    semestrGroup.add(groupNewJ);
                }

                modifyLessonsAdapter();
                createLessonJobConfigDialog();
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

                        jobStoreHashMap.put(parentKey+job.getName(), job);
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

                if (!firstTime || navigation.getSelectedItemId() == R.id.new_jobs) {
                    groupsList = groupsListNewJobs;
                } else if (navigation.getSelectedItemId() == R.id.checking_jobs) {
                    groupsList = groupsListCheckingJobs;
                } else if (navigation.getSelectedItemId() == R.id.finished_jobs) {
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
        if (all == 0) all = 1;

        long res = (cFinished+cChecking) * 100 / all;
        teacherRef.child("progress").setValue(res);

        ObjectAnimator progressAnimator;
        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0,0,(int)res);
        progressAnimator.setDuration(1000);
        progressAnimator.start();

        Drawable progressDrawable = progressBar.getProgressDrawable().mutate();

        if (res < 50) {
            tProgress.setTextColor(getResources().getColor(R.color.red));
            progressDrawable.setColorFilter(getResources().getColor(R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);


        } else if (res >= 50 && res <= 75) {
            tProgress.setTextColor(getResources().getColor(R.color.orange));
            progressDrawable.setColorFilter(getResources().getColor(R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {

            tProgress.setTextColor(getResources().getColor(R.color.colorPrimary));
            progressDrawable.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        progressBar.setProgressDrawable(progressDrawable);

        String progStr = getResources().getString(R.string.progress);
        progStr = progStr.substring(0,progStr.indexOf(":")) +": "+(int) res+" %";

        tProgress.setText(progStr);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.new_jobs:
                    groupsList = groupsListNewJobs;
//                    fab.setVisibility(View.VISIBLE);
                    checkMenuPressed = false;
                    modifyAdapter();
                    return true;
                case R.id.checking_jobs:
                    groupsList = groupsListCheckingJobs;
                    checkMenuPressed = true;
//                    fab.setVisibility(View.INVISIBLE);
                    modifyAdapter();

                    return true;
                case R.id.finished_jobs:
                    groupsList = groupsListFinishedJobs;
                    checkMenuPressed = true;
//                    fab.setVisibility(View.INVISIBLE);
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
        recyclerLessonDataAdapter = new RecyclerLessonDataAdapter(getLessonsData());
        lessonListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonListRecyclerView.setAdapter(recyclerLessonDataAdapter);
        lessonListRecyclerView.setHasFixedSize(true);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            this.finish();
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

    private class RecyclerLessonDataAdapter extends RecyclerView.Adapter<RecyclerLessonDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerLessonDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerLessonDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing2, parent, false);
            return new RecyclerLessonDataAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerLessonDataAdapter.MyViewHolder holder, int position) {
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
                currentTextView.setText(childName);

                if(childName.contains(mistakeStr)){
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

                    //textViewClicked.setBackgroundColor(getResources().getColor(R.color.red));

                    parentStr = textView_parentName.getText().toString();
                    keyJob  = parentStr+clickedLName;

                    if(checkMenuPressed){
                        btnCheck.setText(getResources().getString(R.string.mistakeStr));
                    }else {
                        btnCheck.setText(getResources().getString(R.string.checkStr));
                    }

//                    jobEditDialog.setTitle(parentStr + "\n" + clickedLName);
                    lessonTx.setText(getResources().getString(R.string.jobName)+": "+parentStr + "\n" + clickedLName);
                    jobEditDialog.show();
                    //Toast.makeText(TeacherJobActivity.this, "key: "+jobStoreHashMap.get(keyJob).getKey(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
