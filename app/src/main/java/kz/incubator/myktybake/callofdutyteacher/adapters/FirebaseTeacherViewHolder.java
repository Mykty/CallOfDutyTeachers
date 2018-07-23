package kz.incubator.myktybake.callofdutyteacher.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Student;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

public class FirebaseTeacherViewHolder extends RecyclerView.ViewHolder{

    View mView;
    Context mContext;

    public FirebaseTeacherViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
    }
    public void bindTeacher(Teacher teacher) {

        ImageView person_photo = (ImageView) mView.findViewById(R.id.person_photo);
        TextView info = (TextView) mView.findViewById(R.id.info);
        TextView d_date = (TextView) mView.findViewById(R.id.duty_date);
        TextView p_number = (TextView) mView.findViewById(R.id.number);

        info.setText(teacher.getInfo());
        d_date.setText(teacher.getDate());
        p_number.setText(teacher.getPhoneNumber());

        Glide.with(mContext)
                .load(teacher.getPhoto())
                .placeholder(R.drawable.t_icon)
                .into(person_photo);
/*
            Picasso.with(mContext)
                    .load(restaurant.getImageUrl())
                    .resize(MAX_WIDTH, MAX_HEIGHT)
                    .centerCrop()
                    .into(restaurantImageView);
        */

    }
}

