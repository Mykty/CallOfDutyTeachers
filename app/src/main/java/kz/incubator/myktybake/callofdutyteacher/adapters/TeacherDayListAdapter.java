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
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Teacher;

public class TeacherDayListAdapter extends RecyclerView.Adapter<TeacherDayListAdapter.MyTViewHolder> {
    private Context context;
    private List<Teacher> teacherList;
    DateFormat dateF;
    String date;
    String number;

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

            relativeLayout = view.findViewById(R.id.realL);
        }
    }

    public TeacherDayListAdapter(Context context, List<Teacher> teacherList) {
        this.context = context;
        this.teacherList = teacherList;

    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_duty_item, parent, false);
        manageDate();

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        Teacher item = teacherList.get(position);
        //holder.person_photo.setImageResource(item.getPhoto());

        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.t_icon)
                .into(holder.person_photo);

//        String cDate = item.getDate().toString().split(" ")[1];
//
//        int cDateDay = Integer.parseInt(cDate.replace('.', '_').split("_")[0]);
//        int cDateMonth = Integer.parseInt(cDate.replace('.', '_').split("_")[1]);
//
//        int day = Integer.parseInt(date.replace('.', '_').split("_")[0]);//26
//        int month = Integer.parseInt(date.replace('.', '_').split("_")[1]);//03
//
//        if( (month==cDateMonth && cDateDay < day) || (cDateMonth < month) ){
//
//            holder.duty_date.setTextColor(Color.GRAY);
//            holder.info.setTextColor(Color.GRAY);
//
//        }
//
//        if (date.equals(cDate)) {
//            holder.relativeLayout.setBackgroundResource(R.drawable.current_duty_design);
//            System.out.println("cDate: "+cDate);
//        }

        holder.duty_date.setText(item.getDate());
        holder.info.setText(item.getInfo());
        holder.phone_number.setText(item.getPhoneNumber());
        number = holder.phone_number.getText().toString();

    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

}