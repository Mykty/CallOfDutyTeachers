package kz.incubator.myktybake.callofdutyteacher.docs_fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class Semestr1Fragment extends Fragment implements View.OnClickListener{
    View view;
    private RecyclerView recyclerView;
    private List<Lesson> cartList;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    RecyclerDataAdapter recyclerDataAdapter;
    HashMap<String, String> lessonKey;
    Dialog lessonEditDialog;
    Button btnLessonDel;
    String emailStr, clickedLName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_semestr, container, false);
        createView();
        getLessons();

        return view;
    }


    public void createView() {

        cartList = new ArrayList<>();
        lessonKey = new HashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        /*cartList.add(new Lesson("1 course", "1-01 Информатика", "I semestr", "28"));
        cartList.add(new Lesson("1 course", "1-02 Информатика", "I semestr", "28"));
        cartList.add(new Lesson("1 course", "1-03 Информатика", "I semestr", "28"));
        cartList.add(new Lesson("1 course", "1-04 Информатика", "I semestr", "28"));*/

        recyclerView = view.findViewById(R.id.recycler_view);
        lessonEditDialog = new Dialog(getActivity());
        lessonEditDialog.setContentView(R.layout.dialog_lesson_edit);
        btnLessonDel = lessonEditDialog.findViewById(R.id.btnDel);
        btnLessonDel.setOnClickListener(this);
    }

    public void modifyAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }


    public void getLessons() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        emailStr = currentUser.getEmail().toString();
        emailStr = emailStr.substring(0, emailStr.indexOf("@"));

        Query myTopPostsQuery = mDatabase.child("personnel_store").child("store").child(emailStr).child("lessons").child("semestr1");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartList.clear();

                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {
                    Lesson lesson = dataDates.getValue(Lesson.class);
                    cartList.add(lesson);
                }

                modifyAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private ArrayList<GroupDataItem> getGroupsData() {
        String courses[] = {"1 course", "2 course", "3 course"};

        ArrayList<GroupDataItem> groupsList = new ArrayList<>();
        ArrayList<StudentsItem> studentStore;

        GroupDataItem groupDataItem;
        boolean findGroup;

        for (String course : courses) {
            studentStore = new ArrayList<>();
            findGroup = false;

            for (Lesson l : cartList) {
                if (course.equals(l.getCourse())) {
                    String childItem = l.getName()+"\nСағат саны: "+l.getHours();
                    studentStore.add(new StudentsItem(childItem));
                    findGroup = true;
                    lessonKey.put(childItem, l.getKey());
                }
            }
            if (findGroup) {
                groupDataItem = new GroupDataItem(studentStore);
                course = course.replace("course","курс");
                groupDataItem.setParentName(course);
                groupsList.add(groupDataItem);
            }
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
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 140);//LinearLayout.LayoutParams.WRAP_CONTENT);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnDel:
                String key = lessonKey.get(clickedLName);
                mDatabase.child("personnel_store").child("store").child(emailStr).child("lessons").child("semestr1").child(key).removeValue();
                lessonEditDialog.dismiss();
                break;
        }
    }

}
