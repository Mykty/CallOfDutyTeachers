package kz.incubator.myktybake.callofdutyteacher.moderator_files;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.RecyclerItemClickListener;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

import static android.app.Activity.RESULT_OK;

public class TeacherListFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
    DatabaseReference mDatabaseRef;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager linearLayoutManager;
    ArrayList<Teacher> teachersStore, teachersStoreCopy;
    TeacherListAdapter tListAdapter;
    View view;
    RelativeLayout relativeLayout;
    FloatingActionButton fab;
    Dialog dAddNewTeacher;
    Button choosePhotoBtn, btnCancel, btnOk;
    EditText editName, editEmail, editPhone, editPassword;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseAuth mAuth;
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    boolean photoSelected = false;
    private final int CAMERA_REQUEST = 77;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.teachers_list_fragment, container, false);

        setupViews();
        fillTeachers();

        return view;

    }

    private void setupViews() {
        getActivity().setTitle(getResources().getString(R.string.teacher_list));

        relativeLayout = view.findViewById(R.id.relaviteL);
        fab = view.findViewById(R.id.addBtn);

        dAddNewTeacher = new Dialog(getActivity());
        dAddNewTeacher.setTitle(getResources().getString(R.string.addingNewTeacher2));
        dAddNewTeacher.setContentView(R.layout.dialog_add_new_teacher);

        editName = dAddNewTeacher.findViewById(R.id.editName);
        editEmail = dAddNewTeacher.findViewById(R.id.editEmail);
        editPhone = dAddNewTeacher.findViewById(R.id.editPhone);
        editPassword = dAddNewTeacher.findViewById(R.id.editPassword);
        choosePhotoBtn = dAddNewTeacher.findViewById(R.id.choosePhoto);
        btnCancel = dAddNewTeacher.findViewById(R.id.btnCancel);
        btnOk = dAddNewTeacher.findViewById(R.id.btnOk);

        fab.setOnClickListener(this);
        choosePhotoBtn.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = mDatabaseRef.child("personnel_store").child("store");
        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        teachersStore = new ArrayList<>();
        teachersStoreCopy = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        tListAdapter = new TeacherListAdapter(getActivity(), teachersStore);

        recyclerView.setAdapter(tListAdapter);
        setupSwipeRefresh();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent t = new Intent(getActivity(), TeacherJobActivity.class);
                        t.putExtra("teacher_key", teachersStore.get(position).getKey());
                        startActivity(t);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


    }

    public void fillTeachers() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teachersStore.clear();

                for (DataSnapshot teachersSnapshot : dataSnapshot.getChildren()) {
                    Teacher teacher = teachersSnapshot.getValue(Teacher.class);
                    teachersStore.add(teacher);
                }


                teachersStoreCopy = (ArrayList<Teacher>) teachersStore.clone();
                tListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void refreshItems() {

        if (!checkInetConnection()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();

        } else {

        }

        onItemsLoadComplete();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        filter(newText);
        return false;
    }

    public void filter(String text) {
        teachersStore.clear();

        if (text.isEmpty()) {
            teachersStore.addAll(teachersStoreCopy);
        } else {
            text = text.toLowerCase();
            for (Teacher item : teachersStoreCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toUpperCase().contains(text)) {
                    teachersStore.add(item);
                }
            }
        }

        tListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            mAuth.signOut();
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choosePhoto:

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.photo)), PICK_IMAGE_REQUEST);


                /*
                Photo From Camera

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.photo)), CAMERA_REQUEST);

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                            CAMERA_REQUEST);

                }*/


                break;
            case R.id.btnOk:

                boolean tOk = true;
                String tName = editName.getText().toString();
                String tEmail = editEmail.getText().toString();
                String tPhone = editPhone.getText().toString();
                String tPassword = editPassword.getText().toString();

                if (tName.length() == 0) {
                    editName.setError(getResources().getString(R.string.fill_mistake));
                    tOk = false;
                }
                if (tEmail.length() == 0) {
                    editEmail.setError(getResources().getString(R.string.fill_mistake));
                    tOk = false;
                }
                if (tPhone.length() == 0) {
                    editPhone.setError(getResources().getString(R.string.fill_mistake));
                    tOk = false;
                }
                if (tPhone.length() != 11) {
                    editPhone.setError(getResources().getString(R.string.phoneNumberMistake));
                    tOk = false;
                }
                if (tPassword.length() == 0) {
                    editPassword.setError(getResources().getString(R.string.fill_mistake));
                    tOk = false;
                }

                if (!isEmailValid(editEmail.getText().toString())) {
                    editEmail.setError(getResources().getString(R.string.email_mistake));
                    tOk = false;
                }
                if (tPassword.length() < 6) {
                    editPassword.setError(getResources().getString(R.string.passwordMistake));
                    tOk = false;
                }

                //if(!isValidPassword(tPassword)){ editPassword.setError(getResources().getString(R.string.passwordMistake2)); tOk = false; }
                if (!photoSelected) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.photoSelectMistake), Toast.LENGTH_SHORT).show();
                    tOk = false;
                }

                if (tOk) {
                    signUp(tEmail, tPassword, tName, tPhone);
                }
                break;

            case R.id.addBtn:
                if (!checkInetConnection()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
                } else {
                    photoSelected = false;
                    dAddNewTeacher.show();
                }
                break;

            case R.id.btnCancel:
                clearAll();
                break;
        }
    }

    public void clearAll() {
        editName.getText().clear();
        editEmail.getText().clear();
        editPhone.getText().clear();
        editPassword.getText().clear();

        choosePhotoBtn.setText(getResources().getString(R.string.photo));
        choosePhotoBtn.setBackgroundColor(getResources().getColor(R.color.white));
        photoSelected = false;
        dAddNewTeacher.dismiss();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            choosePhotoBtn.setText(getResources().getString(R.string.photoSelected));
            choosePhotoBtn.setBackgroundColor(getResources().getColor(R.color.green));
            photoSelected = true;
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            filePath = data.getData();
            choosePhotoBtn.setText(getResources().getString(R.string.photoSelected));
            choosePhotoBtn.setBackgroundColor(getResources().getColor(R.color.green));
            photoSelected = true;
        }
    }

    ProgressDialog pNewTecherDialog;

    public void signUp(final String email, String password, final String tName, final String tPhone) {
        pNewTecherDialog = new ProgressDialog(getActivity());
        pNewTecherDialog.setMessage(getResources().getString(R.string.addingNewTeacher));
        pNewTecherDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pNewTecherDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                editEmail.setError(getResources().getString(R.string.passwordMistake3));

                            } catch (FirebaseAuthInvalidCredentialsException e) {

                            } catch (FirebaseAuthUserCollisionException e) {
                                editEmail.setError(getResources().getString(R.string.email_duplicate));

                            } catch (Exception e) {
                                Log.e("info", e.getMessage());
                            }

                            pNewTecherDialog.dismiss();

                        } else if (task.isSuccessful()) {

                            uploadImage(email, tName, tPhone);

                        } else {
                            Log.w("info", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    String downloadUri = null;

    private String uploadImage(final String email, final String tName, final String tPhone) {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getResources().getString(R.string.photoLoading));
            progressDialog.show();

            final String photoPath = "images/" + UUID.randomUUID().toString();
            final StorageReference ref = storageReference.child(photoPath);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            downloadUri = taskSnapshot.getDownloadUrl().toString();
                            Log.i("info", "Photo url2: " + downloadUri);

                            String tEmail = email;

                            if (tEmail.contains(".")) tEmail = tEmail.replace('.', '_');
                            tEmail = tEmail.substring(0, tEmail.indexOf("@"));

                            if (downloadUri != null) {
                                Teacher t = new Teacher(tEmail, tName, downloadUri, tPhone, 0);
                                mDatabaseRef.child(tEmail).setValue(t);
                                pNewTecherDialog.dismiss();
                                clearAll();
                                showSuccessToast();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
        return downloadUri;
    }

    public void showSuccessToast() {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view.findViewById(R.id.custom_toast_layout));
//        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
//        text.setText("Жаңа тапсырыс сәтті енгізілді!");

        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    public static boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean checkInetConnection() {
        if (isNetworkAvailable(getActivity())) {
            return true;
        }
        return false;
    }
}