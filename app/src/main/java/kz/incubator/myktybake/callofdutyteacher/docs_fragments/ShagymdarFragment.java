package kz.incubator.myktybake.callofdutyteacher.docs_fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Lesson;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.GroupDataItem;
import kz.incubator.myktybake.callofdutyteacher.rexpandable.StudentsItem;

public class ShagymdarFragment extends Fragment implements View.OnClickListener {
    View view;
    private RecyclerView recyclerView, recyclerShTypes;
    private List<Lesson> cartList;
    private DatabaseReference mDatabase;
    FirebaseUser currentUser;
    DatabaseReference teacherRef;
    RecyclerDataAdapter recyclerDataAdapter;
    HashMap<String, String> lessonKey;
    HashMap<String, String> shagymThing;
    Dialog lessonEditDialog, addingShagym, shagymdarDialog;
    Button btnLessonDel;
    String emailStr, clickedLName;
    ArrayList<GroupDataItem> groupsList;
    ArrayList<StudentsItem> studentStore;
    ArrayList<String> shagymTypes;
    Spinner spinnerType, spinnerThing;

    EditText cabNumber, shagymDesc;
    Button enterLessonBtn;
    FloatingActionButton fab;
    TextView shagymTeacher, shagymPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_lessons, container, false);
        createView();
        getShagymdarTypes();

        return view;
    }

    public void createView() {

        cartList = new ArrayList<>();
        shagymTypes = new ArrayList<>();
        shagymThing = new HashMap<>();

        lessonKey = new HashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = view.findViewById(R.id.recycler_view);
        lessonEditDialog = new Dialog(getActivity());
        lessonEditDialog.setContentView(R.layout.dialog_lesson_edit);
        btnLessonDel = lessonEditDialog.findViewById(R.id.btnDel);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        teacherRef = FirebaseDatabase.getInstance().getReference();

        btnLessonDel.setOnClickListener(this);

        groupsList = new ArrayList<>();
        fab = view.findViewById(R.id.addNewJob);
        fab.setOnClickListener(this);
        createShagymdarTypesDialog();
        createAddingNewShagymDialog();
    }

    public void modifyAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(groupsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void getShagymdarTypes() {
        mDatabase.child("shagymdar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupsList.clear();
                shagymTypes.clear();
                shagymThing.clear();

                GroupDataItem groupDataItem;
                String titleStr;

                for (DataSnapshot titles : dataSnapshot.getChildren()) {
                    studentStore = new ArrayList<>();
                    titleStr = titles.getKey().toString();
                    shagymTypes.add(titleStr);

                    for (DataSnapshot content : titles.getChildren()) {
                        String childItem = content.getKey().toString();
                        studentStore.add(new StudentsItem(childItem));
                        shagymThing.put(childItem, titleStr);
                    }

                    groupDataItem = new GroupDataItem(studentStore);
                    groupDataItem.setParentName(titleStr);
                    groupsList.add(groupDataItem);
                }

                modifyShTypesAdapter();
                modifyNewShagymAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void createShagymdarTypesDialog() {
        shagymdarDialog =  new Dialog(getActivity(), R.style.CustomDialog);
        shagymdarDialog.setTitle(getResources().getString(R.string.shagym_types));
        shagymdarDialog.setContentView(R.layout.activity_dynamic_recycler);
        recyclerShTypes = shagymdarDialog.findViewById(R.id.recyclerView);
    }

    public void modifyShTypesAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(groupsList);
        recyclerShTypes.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerShTypes.setAdapter(recyclerDataAdapter);
        recyclerShTypes.setHasFixedSize(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.shahym_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.shagym) {
            shagymdarDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createAddingNewShagymDialog() {
        addingShagym = new Dialog(getActivity(), R.style.CustomDialog);
        addingShagym.setContentView(R.layout.dialog_adding_shagym);
        addingShagym.setTitle(getResources().getString(R.string.addingShagym));

        spinnerType = addingShagym.findViewById(R.id.spinnerType);
        spinnerThing = addingShagym.findViewById(R.id.spinnerThing);

        cabNumber = addingShagym.findViewById(R.id.cabNumber);
        shagymDesc = addingShagym.findViewById(R.id.shagymDesc);
        shagymTeacher = addingShagym.findViewById(R.id.shagymTeacher);
        shagymPhone = addingShagym.findViewById(R.id.shagymPhone);

        enterLessonBtn = addingShagym.findViewById(R.id.enterLesson);
        enterLessonBtn.setOnClickListener(this);

        String tEmail = currentUser.getEmail().toString();

        if (tEmail.contains(".")) tEmail = tEmail.replace('.', '_');
        tEmail = tEmail.substring(0, tEmail.indexOf("@"));

        teacherRef = teacherRef.child("personnel_store").child("store").child(tEmail);
        teacherRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Teacher teacher = dataSnapshot.getValue(Teacher.class);
                shagymTeacher.setText(teacher.getInfo());
                shagymPhone.setText(teacher.getPhoneNumber());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    ArrayList<String> thingsStore;
    public void modifyNewShagymAdapter(){
        String spinnerTypeStore[] = new String[shagymTypes.size()];
        spinnerTypeStore = shagymTypes.toArray(spinnerTypeStore);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerTypeStore);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String gg = shagymTypes.get(pos).toString();
                thingsStore = new ArrayList<>();

                for(String w: shagymThing.keySet()){
                    if(gg.equals(shagymThing.get(w))){
                        Log.i("Content", w);
                        thingsStore.add(w);
                    }
                }

                String spinnerThingStore[] = new String[thingsStore.size()];
                spinnerThingStore = thingsStore.toArray(spinnerThingStore);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerThingStore);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerThing.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
                addingShagym.show();
                break;

            case R.id.enterLesson:

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

                    //Toast.makeText(getActivity(), "Yahoo: "+lessonKey.get(clickedSName), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
