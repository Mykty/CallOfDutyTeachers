package kz.incubator.myktybake.callofdutyteacher.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import kz.incubator.myktybake.callofdutyteacher.MainActivity;
import kz.incubator.myktybake.callofdutyteacher.R;

public class LoginActivity extends AppCompatActivity {

    Button go;
    EditText login, password;
    private FirebaseAuth mAuth;
    ProgressDialog progress_spinner;
    DatabaseReference teacherRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        go = findViewById(R.id.buttonOk);
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        teacherRef = FirebaseDatabase.getInstance().getReference();
        teacherRef = teacherRef.child("personnel_store").child("store");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String cEmail = currentUser.getEmail().toString();
            if (cEmail.contains("moderator")){
                Intent t = new Intent(LoginActivity.this, ModeratorActivity.class);
                startActivity(t);
            }else{
                Intent t = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(t);
            }
        }

        progress_spinner = new ProgressDialog(LoginActivity.this);
        progress_spinner.setMessage(getResources().getString(R.string.singing));
        progress_spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {

                    if (login.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
                        progress_spinner.show();
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        checkUserExist2(login.getText().toString(), password.getText().toString());

                    } else {

                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.sing_up_fail),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.inetConnection),
                            Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    public void checkUserExist2(final String tEmail, final String tPassword) {
        final String emailStr = tEmail.substring(0, tEmail.indexOf("@"));

        if (emailStr.equals("moderator")) signIn(tEmail, tPassword);

        else {
            teacherRef.child(emailStr).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        signIn(tEmail, tPassword);
                    } else {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.notfoundUser), Toast.LENGTH_SHORT).show();
                        progress_spinner.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });
        }
    }

    public void signIn(final String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progress_spinner.dismiss();
                            clearLoginForm();

                            Intent t;
                            if (email.contains("moderator"))
                                t = new Intent(LoginActivity.this, ModeratorActivity.class);
                            else t = new Intent(LoginActivity.this, MainActivity.class);

                            startActivity(t);

                        } else {
                            progress_spinner.dismiss();
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.sing_up_fail),
                                    Toast.LENGTH_SHORT).show();
                        }

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
    }

    public void clearLoginForm() {
        login.getText().clear();
        password.getText().clear();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}

/*
    public void signUp(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    */
