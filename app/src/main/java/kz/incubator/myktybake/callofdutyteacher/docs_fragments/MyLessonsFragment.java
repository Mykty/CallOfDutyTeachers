package kz.incubator.myktybake.callofdutyteacher.docs_fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import kz.incubator.myktybake.callofdutyteacher.module.Lesson;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;

public class MyLessonsFragment extends Fragment implements View.OnClickListener {
    View view;
    private RecyclerView recyclerView;
    private List<Lesson> cartList;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    RecyclerDataAdapter recyclerDataAdapter;
    HashMap<String, String> lessonKey;
    Dialog lessonEditDialog, addingLesson;
    Button btnLessonDel;
    String emailStr, clickedLName;
    ArrayList<GroupDataItem> groupsList;
    ArrayList<StudentsItem> studentStore;
    Spinner semestrSp, courseSp;
    EditText lessonName, lessonHour;
    Button enterLessonBtn;
    FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_lessons, container, false);
        createView();
        getLessons();

        return view;
    }

    public void createView() {

        cartList = new ArrayList<>();
        lessonKey = new HashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = view.findViewById(R.id.recycler_view);
        lessonEditDialog = new Dialog(getActivity());
        lessonEditDialog.setContentView(R.layout.dialog_lesson_edit);
        btnLessonDel = lessonEditDialog.findViewById(R.id.btnDel);
        btnLessonDel.setOnClickListener(this);

        groupsList = new ArrayList<>();
        fab = view.findViewById(R.id.addNewJob);
        fab.setOnClickListener(this);
        createAddingNewLessonDialog();
    }

    public void modifyAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(groupsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void getLessons() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        emailStr = currentUser.getEmail().toString();
        emailStr = emailStr.substring(0, emailStr.indexOf("@"));

        mDatabase = mDatabase.child("personnel_store").child("store").child(emailStr).child("lessons");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupsList.clear();
                GroupDataItem groupDataItem;
                String semestrStr;

                for (DataSnapshot semestrData : dataSnapshot.getChildren()) {
                    studentStore = new ArrayList<>();
                    semestrStr = semestrData.getKey().toString();

                    for (DataSnapshot lessonData : semestrData.getChildren()) {
                        Lesson l = lessonData.getValue(Lesson.class);
                        String childItem = l.getName() + "\nСағат саны: " + l.getHours();
                        studentStore.add(new StudentsItem(childItem));
                        lessonKey.put(childItem, l.getKey());
                    }

                    groupDataItem = new GroupDataItem(studentStore);
                    groupDataItem.setParentName(semestrStr);
                    groupsList.add(groupDataItem);
                }

                modifyAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void createAddingNewLessonDialog() {

        addingLesson = new Dialog(getActivity(), R.style.CustomDialog);
        addingLesson.setContentView(R.layout.dialog_adding_lesson);
        addingLesson.setTitle(getResources().getString(R.string.addingLesson));
        semestrSp = addingLesson.findViewById(R.id.semestrSpinner);
        courseSp = addingLesson.findViewById(R.id.courseSpinner);
        enterLessonBtn = addingLesson.findViewById(R.id.enterLesson);
        lessonName = addingLesson.findViewById(R.id.lessonName);
        lessonHour = addingLesson.findViewById(R.id.lessonHour);

        String semestrStr[] = getResources().getStringArray(R.array.semestrStore);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, semestrStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.courseStrore, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        semestrSp.setAdapter(adapter);
        courseSp.setAdapter(adapter2);

        enterLessonBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDel:
                String key = lessonKey.get(clickedLName);
                mDatabase.child("personnel_store").child("store").child(emailStr).child("lessons").child("semestr1").child(key).removeValue();
                lessonEditDialog.dismiss();
                break;

            case R.id.addNewJob:
                addingLesson.show();
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
                    String keyStr = mDatabase.child(semestrStr).push().getKey();

                    Lesson lesson = new Lesson(keyStr, courseStr, courseStr + " " + lessontNameStr, Long.parseLong(lessontHourStr));
                    mDatabase.child(semestrStr).child(keyStr).setValue(lesson);

                }

                lessonName.getText().clear();
                lessonHour.getText().clear();
                addingLesson.dismiss();
                break;
        }
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

                //$$$
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
