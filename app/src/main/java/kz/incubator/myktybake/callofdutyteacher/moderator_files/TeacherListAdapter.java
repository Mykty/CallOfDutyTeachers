package kz.incubator.myktybake.callofdutyteacher.moderator_files;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

public class TeacherListAdapter extends RecyclerView.Adapter<TeacherListAdapter.MyTViewHolder> {
    private Context context;
    private List<Teacher> teacherList;
    DatabaseReference mDatabaseRef;

    public class MyTViewHolder extends RecyclerView.ViewHolder {
        public ImageView person_photo;
        public TextView info, phone_number, progress;
        public RelativeLayout relativeLayout;

        public MyTViewHolder(View view) {
            super(view);
            person_photo = view.findViewById(R.id.person_photo);
            info = view.findViewById(R.id.info);
            phone_number = view.findViewById(R.id.number);
            progress = view.findViewById(R.id.progress);

            relativeLayout = view.findViewById(R.id.realL);
        }
    }

    public TeacherListAdapter(Context context, List<Teacher> teacherList) {
        this.context = context;
        this.teacherList = teacherList;

    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_list, parent, false);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = mDatabaseRef.child("personnel_store").child("store");

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        final Teacher item = teacherList.get(position);

        final String phoneNumber = item.getPhoneNumber();

        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.t_icon)
                .into(holder.person_photo);

        holder.info.setText(item.getInfo());
        holder.phone_number.setText(phoneNumber);

        long progressLong = item.getProgress();
        String progStr = context.getResources().getString(R.string.progress);
        holder.progress.setText(progStr.substring(0,progStr.indexOf(":")) +": "+progressLong+" %");

        if (progressLong < 50) {
            holder.progress.setTextColor(context.getResources().getColor(R.color.red));
        } else if (progressLong >= 50 && progressLong <= 75) {
            holder.progress.setTextColor(context.getResources().getColor(R.color.orange));
        } else {
            holder.progress.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }

    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

}