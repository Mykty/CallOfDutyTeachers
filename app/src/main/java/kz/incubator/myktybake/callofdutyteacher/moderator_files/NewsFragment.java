package kz.incubator.myktybake.callofdutyteacher.moderator_files;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.News;
import kz.incubator.myktybake.callofdutyteacher.module.RecyclerItemClickListener;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

public class NewsFragment extends Fragment implements View.OnClickListener {
    View view;

    private StaggeredGridLayoutManager sGridLayoutManager;
    RecyclerView recyclerView;
    List<News> listViewItems;
    NewsRecyclerViewAdapter rcAdapter;
    int count = 1;
    FloatingActionButton addNews;
    Dialog addNewsDialog;
    Button btnAdd, btnCancel;
    EditText newsTitle, newsDesc;
    TextView addedTime;
    DateFormat dateFr, timeF;
    DatabaseReference newsRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_news, container, false);
        getActivity().setTitle(getString(R.string.news));
        setupViews();

        return view;
    }

    public void setupViews() {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        addNews = view.findViewById(R.id.addNews);
        addNews.setOnClickListener(this);

        sGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(sGridLayoutManager);


        newsRef = FirebaseDatabase.getInstance().getReference().child("news");

        getListItemData();
        rcAdapter = new NewsRecyclerViewAdapter(getActivity(), listViewItems);
        recyclerView.setAdapter(rcAdapter);

        addNewsDialog = new Dialog(getActivity(), R.style.CustomDialog);
        addNewsDialog.setContentView(R.layout.dialog_add_news);
        addNewsDialog.setTitle(getResources().getString(R.string.news_add));

        newsTitle = addNewsDialog.findViewById(R.id.newsTitle);
        newsDesc = addNewsDialog.findViewById(R.id.newsDesc);
        addedTime = addNewsDialog.findViewById(R.id.addedTime);

        btnAdd = addNewsDialog.findViewById(R.id.btnAdd);
        btnCancel = addNewsDialog.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        manageDate();
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                })
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNews:
                addNewsDialog.show();
                break;

            case R.id.btnAdd:
                addNews();
                addNewsDialog.dismiss();
                break;

            case R.id.btnCancel:
                newsTitle.getText().clear();
                newsDesc.getText().clear();
                addNewsDialog.dismiss();
                break;
        }
    }

    public void manageDate() {
        dateFr = new SimpleDateFormat("dd.MM.YY");//2001.07.04
        timeF = new SimpleDateFormat("HH:mm");//14:08

        String date = dateFr.format(Calendar.getInstance().getTime());
        String time = timeF.format(Calendar.getInstance().getTime());
        addedTime.setText(date + " - " + time);
    }

    public void addNews() {
        String key = newsRef.push().getKey().toString();

        String title = newsTitle.getText().toString();
        String desc = newsDesc.getText().toString();
        String time = addedTime.getText().toString();

        News news = new News(title, desc, time);
        newsRef.child(key).setValue(news);

        newsTitle.getText().clear();
        newsDesc.getText().clear();
    }

    private void getListItemData() {
        listViewItems = new ArrayList<>();
        newsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listViewItems.clear();

                for (DataSnapshot newsData : dataSnapshot.getChildren()) {
                    News news = newsData.getValue(News.class);
                    listViewItems.add(news);
                }
                rcAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.news_filter, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        count++;
        if (count == 4) count = 1;

        if (id == R.id.filter) {
            sGridLayoutManager.setSpanCount(count);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
