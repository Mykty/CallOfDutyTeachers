package kz.incubator.myktybake.callofdutyteacher.docs_fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import kz.incubator.myktybake.callofdutyteacher.module.Job;
import kz.incubator.myktybake.callofdutyteacher.module.Lesson;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;


public class JobsFragment extends Fragment implements View.OnClickListener {

    View view;
    ImageView tPhoto;
    TextView tInfo, tProgress, lessonTx;
    DatabaseReference teacherRef;
    BiColoredProgress teacherProgressPhoto;

    private RecyclerView recyclerView;
    private List<Lesson> cartList;
    FirebaseUser currentUser;
    RecyclerDataAdapter recyclerDataAdapter;
    HashMap<String, String> jobsHashMap;
    HashMap<String, Job> jobStoreHashMap;
    Dialog lessonEditDialog, jobEditDialog;
    Button btnLessonDel, btnCheck;
    String clickedLName, mistakeStr, keyJob, parentStr;
    int cNewJobs = 0, cChecking = 0, cFinished = 0;
    boolean firstTime = false;
    BottomNavigationView navigation;
    boolean newJobsPressed = false;

    ArrayList<GroupDataItem> groupsList, groupsListNewJobs, groupsListCheckingJobs, groupsListFinishedJobs;
    ArrayList<StudentsItem> childStoreNewJobs, childStoreCheckingJobs, childStoreFinishedJobs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.jobs_fragment, container, false);
        getActivity().setTitle(getActivity().getResources().getString(R.string.my_jobs));


        createView();

        return view;

    }

    public void createView() {

        cartList = new ArrayList<>();
        jobsHashMap = new HashMap<>();
        jobStoreHashMap = new HashMap<>();

        groupsList = new ArrayList<>();

        tPhoto = view.findViewById(R.id.teacherPhoto);
        tInfo = view.findViewById(R.id.teacherInfo);
        tProgress = view.findViewById(R.id.teacherProgress);
        teacherProgressPhoto = view.findViewById(R.id.twice_colored_progress);

        teacherRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String emailStr = currentUser.getEmail().toString();
        emailStr = emailStr.substring(0, emailStr.indexOf("@"));

        recyclerView = view.findViewById(R.id.recycler_view);
        lessonEditDialog = new Dialog(getActivity());
        lessonEditDialog.setContentView(R.layout.dialog_lesson_edit);
        btnLessonDel = lessonEditDialog.findViewById(R.id.btnDel);
        btnLessonDel.setOnClickListener(this);

        mistakeStr = getResources().getString(R.string.mistakeStr);

        getTeachersData(emailStr);
        createJobEditDialog();

        navigation = view.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    public void createJobEditDialog() {
        jobEditDialog = new Dialog(getActivity());
        jobEditDialog.setContentView(R.layout.dialog_edit_teacher_job);

        btnCheck = jobEditDialog.findViewById(R.id.btnCheck);
        lessonTx = jobEditDialog.findViewById(R.id.lessonTx);

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String jobKey = jobStoreHashMap.get(keyJob).getKey();

                if (clickedLName.contains(mistakeStr)) {
                    clickedLName = clickedLName.substring(0, clickedLName.indexOf(mistakeStr) - 3);
                    teacherRef.child("jobs").child(parentStr).child(jobKey).child("name").setValue(clickedLName);
                }

                teacherRef.child("jobs").child(parentStr).child(jobKey).child("status").setValue("checking");

                jobEditDialog.dismiss();
            }
        });

    }

    public void modifyAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);

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

                        Job job = jobSnapshot.getValue(Job.class);

                        if (job.getStatus().equals("new")) {
                            childStoreNewJobs.add(new StudentsItem("" + job.getName()));
                        } else if (job.getStatus().equals("checking")) {
                            childStoreCheckingJobs.add(new StudentsItem("" + job.getName()));
                        } else {
                            childStoreFinishedJobs.add(new StudentsItem("" + job.getName()));
                        }

                        jobStoreHashMap.put(parentKey + job.getName(), job);
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

        teacherRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Teacher teacher = dataSnapshot.getValue(Teacher.class);

                tInfo.setText(teacher.getInfo());

                Glide.with(getActivity())
                        .load(teacher.getPhoto())
                        .placeholder(R.drawable.user_def)
                        .into(tPhoto);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void countProgress() {

        int all = cFinished + cNewJobs + cChecking;
        if (all == 0) all = 1;

        long res = (cFinished + cChecking) * 100 / all;

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDel:
                String key = jobsHashMap.get(clickedLName);
                //mDatabase.child("personnel_store").child("store").child(emailStr).child("lessons").child("semestr1").child(key).removeValue();
                lessonEditDialog.dismiss();
                break;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.new_jobs:
                    groupsList = groupsListNewJobs;

                    modifyAdapter();
                    newJobsPressed = true;
                    return true;
                case R.id.checking_jobs:
                    groupsList = groupsListCheckingJobs;

                    newJobsPressed = false;
                    modifyAdapter();

                    return true;
                case R.id.finished_jobs:
                    groupsList = groupsListFinishedJobs;
                    newJobsPressed = false;
                    modifyAdapter();

                    return true;
            }
            return false;
        }
    };

    private ArrayList<GroupDataItem> getGroupsData() {
        return groupsList;
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

                if (childName.contains(mistakeStr)) {
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

                    //textViewClicked.setBackgroundColor(getResources().getColor(R.color.light_green));
                    //lessonEditDialog.show();

                    parentStr = textView_parentName.getText().toString();
                    keyJob = parentStr + clickedLName;

                    lessonTx.setText(getResources().getString(R.string.jobName) + ": " + parentStr + "\n" + clickedLName);
                    if(newJobsPressed) jobEditDialog.show();


                    //Toast.makeText(getActivity(), "Yahoo: "+lessonKey.get(clickedSName), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}