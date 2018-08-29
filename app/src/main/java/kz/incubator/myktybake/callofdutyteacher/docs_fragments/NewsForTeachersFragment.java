package kz.incubator.myktybake.callofdutyteacher.docs_fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
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
import kz.incubator.myktybake.callofdutyteacher.moderator_files.NewsRecyclerViewAdapter;
import kz.incubator.myktybake.callofdutyteacher.module.News;
import kz.incubator.myktybake.callofdutyteacher.module.RecyclerItemClickListener;

public class NewsForTeachersFragment extends Fragment{
    View view;

    private StaggeredGridLayoutManager sGridLayoutManager;
    RecyclerView recyclerView;
    List<News> listViewItems;
    NewsRecyclerViewAdapter rcAdapter;
    int count = 1;
    DatabaseReference newsRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_teacher_news, container, false);
        getActivity().setTitle(getString(R.string.news));
        setupViews();

        return view;
    }

    public void setupViews() {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        sGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(sGridLayoutManager);


        newsRef = FirebaseDatabase.getInstance().getReference().child("news");

        getListItemData();
        rcAdapter = new NewsRecyclerViewAdapter(getActivity(), listViewItems);
        recyclerView.setAdapter(rcAdapter);
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
