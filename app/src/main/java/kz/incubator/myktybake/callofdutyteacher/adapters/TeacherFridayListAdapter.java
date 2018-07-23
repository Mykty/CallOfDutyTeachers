package kz.incubator.myktybake.callofdutyteacher.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

public class TeacherFridayListAdapter extends RecyclerView.Adapter<TeacherFridayListAdapter.MyTViewHolder> {
    private Context context;
    private List<Teacher> teacherList;

    public class MyTViewHolder extends RecyclerView.ViewHolder {
        public ImageView person_photo;
        public TextView duty_date, info, phone_number;
        public RelativeLayout relativeLayout, viewForeground;

        public MyTViewHolder(View view) {
            super(view);
            person_photo = view.findViewById(R.id.person_photo);
            duty_date = view.findViewById(R.id.duty_date);
            info = view.findViewById(R.id.info);
            phone_number = view.findViewById(R.id.number);
        }
    }

    public TeacherFridayListAdapter(Context context, List<Teacher> teacherList) {
        this.context = context;
        this.teacherList = teacherList;

    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friday_duty_item, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, final int position) {
        final Teacher item = teacherList.get(position);


        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.t_icon)
                .into(holder.person_photo);

        holder.duty_date.setText(item.getDate());
        holder.info.setText(item.getInfo());
        holder.phone_number.setText(item.getPhoneNumber());
    }

    public String getFridayDate() {

        Calendar fridayDate = Calendar.getInstance();
        fridayDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        SimpleDateFormat dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        String friday = dateF.format(fridayDate.getTime());

        return friday;

    }
    @Override
    public int getItemCount() {
        return teacherList.size();
    }

}