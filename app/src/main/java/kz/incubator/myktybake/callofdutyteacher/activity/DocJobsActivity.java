package kz.incubator.myktybake.callofdutyteacher.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.docs_fragments.JobsFragment;
import kz.incubator.myktybake.callofdutyteacher.docs_fragments.MyLessonsFragment;
import kz.incubator.myktybake.callofdutyteacher.docs_fragments.NewsForTeachersFragment;
import kz.incubator.myktybake.callofdutyteacher.docs_fragments.ShagymdarFragment;
import kz.incubator.myktybake.callofdutyteacher.moderator_files.NewsFragment;

public class DocJobsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    JobsFragment jobsFragment;
    MyLessonsFragment myLessonsFragment;
    NewsForTeachersFragment newsFragment;
    ShagymdarFragment shagymdarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        createFragments();
        changeFragment(myLessonsFragment);
        checkInetConnection();
    }

    public void createFragments(){
        jobsFragment = new JobsFragment();
        myLessonsFragment = new MyLessonsFragment();
        newsFragment = new NewsForTeachersFragment();
        shagymdarFragment = new ShagymdarFragment();
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public void checkInetConnection(){
        if(isNetworkAvailable(this)){

        }else{
            Toast.makeText(DocJobsActivity.this, "There is no inet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //writeNewTeacher();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.lessons){
            changeFragment(myLessonsFragment);

        }else if (id == R.id.jobs) {
            changeFragment(jobsFragment);

        }else if (id == R.id.news) {
            changeFragment(newsFragment);

        }else if (id == R.id.shagymdar) {
            changeFragment(shagymdarFragment);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(Fragment cfragment){
        Fragment fragment = cfragment;
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }
}
