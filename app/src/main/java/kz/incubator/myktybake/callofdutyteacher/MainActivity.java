package kz.incubator.myktybake.callofdutyteacher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kz.incubator.myktybake.callofdutyteacher.activity.DocJobsActivity;
import kz.incubator.myktybake.callofdutyteacher.activity.DutyActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Changed from Acer

    Button btnJobs, btnDuty;
    FirebaseAuth mAuth;
    DatabaseReference teacherRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnJobs = findViewById(R.id.btnJobs);
        btnDuty = findViewById(R.id.btnDuty);

        btnJobs.setOnClickListener(this);
        btnDuty.setOnClickListener(this);

        teacherRef = FirebaseDatabase.getInstance().getReference();
        teacherRef = teacherRef.child("personnel_store").child("store");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String cEmail = currentUser.getEmail().toString();
        checkCookieUserExist(cEmail);

    }

    public void checkCookieUserExist(String tEmail) {
        final String emailStr = tEmail.substring(0, tEmail.indexOf("@"));

        teacherRef.child(emailStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.notfoundUser), Toast.LENGTH_SHORT).show();
                    btnJobs.setEnabled(false);
                    btnDuty.setEnabled(false);

                    btnJobs.setBackgroundColor(getResources().getColor(R.color.black_grey));
                    btnDuty.setBackgroundColor(getResources().getColor(R.color.black_grey));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnJobs:
                startActivity(new Intent(MainActivity.this, DocJobsActivity.class));
                break;

            case R.id.btnDuty:
                startActivity(new Intent(MainActivity.this, DutyActivity.class));

                break;

        }
    }

    @Override
    public void onBackPressed() {

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

            mAuth.signOut();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
