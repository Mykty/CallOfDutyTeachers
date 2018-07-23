package kz.incubator.myktybake.callofdutyteacher.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kz.incubator.myktybake.callofdutyteacher.MainActivity;
import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.StoreDatabase;
import kz.incubator.myktybake.callofdutyteacher.module.Student;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String STUDENT_LIST = "students_list";
    DateFormat dateF, timeF, dateFr;
    String date, time, firebaseDate;
    private DatabaseReference mDatabase;
    TextView studentName;
    Button btOk;
    ImageView imageV;
    //String new_latecomer = "", entered_qr_code = "";
    Dialog dialog;
    HashMap<String, Integer> checkerHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        manageDate();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.scann_res);

        imageV = dialog.findViewById(R.id.imageV);

        btOk = (Button) dialog.findViewById(R.id.buttonOk);
        Button btCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        studentName = (TextView) dialog.findViewById(R.id.sName);
        btOk.setEnabled(false);
        checkerHashMap = new HashMap<>();
        updateWeeklyLatecomers();

    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ScannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText().toString();
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        findStudent(myResult);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = timeF.format(Calendar.getInstance().getTime());
                String lateMin = lateMinute(time);
                insertLatecomer(myResult, lateMin);

                scannerView.resumeCameraPreview(ScannerActivity.this);

                dialog.dismiss();
                scannerView.stopCamera();

                Intent t = new Intent(ScannerActivity.this, MainActivity.class);
                startActivity(t);
            }
        });
    }

    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + STUDENT_LIST + " WHERE qr_code=?", new String[]{qr_code});
        return res;
    }

    public Student findStudent(String qr_code) {
        dialog.show();

        Cursor res = getStudentByQrCode(qr_code);
        Student student = new Student();

        if (((res != null) && (res.getCount() > 0))) {
            while (res.moveToNext()) {
                Log.i("student", res.getString(1));

                student.setInfo(res.getString(1));
                student.setGroup(res.getString(2));
                student.setPhoto(res.getString(3));

                studentName.setText(res.getString(1));
                btOk.setEnabled(true);

                Glide.with(ScannerActivity.this)
                        .load(res.getString(3))
                        .placeholder(R.drawable.t_icon)
                        .into(imageV);
            }
        } else {
            Snackbar snackbar = Snackbar.make(scannerView, "Can not find student on Database!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        return student;
    }

    public boolean insertLatecomer(String qr_code, String lateMin) {
        if(checkerHashMap.containsKey(qr_code)) {
            int lateCount = checkerHashMap.get(qr_code);

            if (lateCount >= 2) {
                mDatabase.child("punished").child("" + qr_code).setValue("friday");
            }
        }

        mDatabase.child("latecomers").child(firebaseDate).child("" + qr_code).child("time").setValue(lateMin);

        return true;
    }

    public void updateWeeklyLatecomers() {
        Query myTopPostsQuery = mDatabase.child("latecomers");

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataDates : dataSnapshot.getChildren()) {
                    for (DataSnapshot students : dataDates.getChildren()) {

                        String qr_code = students.getKey();

                        if (checkerHashMap.containsKey(qr_code)) {
                            int n = checkerHashMap.get(qr_code);
                            checkerHashMap.put(qr_code, n + 1);
                        } else {
                            checkerHashMap.put(qr_code, 1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd_MM_yyyy");//2001.07.04
        dateFr = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        timeF = new SimpleDateFormat("HH:mm");//14:08

        date = dateF.format(Calendar.getInstance().getTime());
        firebaseDate = dateFr.format(Calendar.getInstance().getTime());
        time = timeF.format(Calendar.getInstance().getTime());

    }

    public String lateMinute(String time) {
        Date t8_30 = null;
        Date currentTime = null;
        String dateStart = "08:30";
        String text = "0";

        try {
            t8_30 = timeF.parse(dateStart);
            currentTime = timeF.parse(time);

            long diff = currentTime.getTime() - t8_30.getTime();

            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffMinutes = diff / (60 * 1000) % 60;

            if (diffHours > 0 || diffMinutes >= 15) {
                //otrabotka 80 min
                text = "" + (diffHours * 60 + diffMinutes);

            } else if (diffMinutes <= 10) {
                //otrabotka 40 min
                text = "" + diffMinutes;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

}

//Meirjan 104201721
//Abdurasul 410201720
//Narkiz 201201742


        /*

        if (result.getText().equals("104201721")) {
            new_latecomer = "Meirjan";
        } else if (result.getText().equals("410201720")) {
            new_latecomer = "Abdurasul";
        } else if (result.getText().equals("201201742")) {
            new_latecomer = "Narkiz";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(ScannerActivity.this);
            }
        });
        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myResult));
                startActivity(browserIntent);
            }
        });
        builder.setMessage(result.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();
        */