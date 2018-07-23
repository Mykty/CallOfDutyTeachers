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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Student;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.MyViewHolder> {
    private Context context;
    private List<Student> cartList;
    StorageReference storageRef;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView info, group, time;
        public ImageView thumbnail;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.info);
            group = view.findViewById(R.id.group);
            time = view.findViewById(R.id.time);

            thumbnail = view.findViewById(R.id.thumbnail);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);

            storageRef = FirebaseStorage.getInstance().getReference();
        }
    }

    public CartListAdapter(Context context, List<Student> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_list_item, parent, false);

        return new MyViewHolder(itemView);
    }
    String url;

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Student item = cartList.get(position);
        holder.info.setText(item.getInfo());
        holder.group.setText(item.getGroup());

        String lateMin = item.getTime();
        holder.time.setText(lateMin);


        /*
        int minInt = Integer.parseInt(lateMin);

        if(minInt <= 15){
            holder.time.setTextColor(context.getResources().getColor(R.color.orange));
        }else{
            holder.time.setTextColor(context.getResources().getColor(R.color.red));
        }*/

        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.s_icon)
                .into(holder.thumbnail);

        /*
        StorageReference storageReference = storageRef.child("students/1-01/"+item.getPhoto()+".png");

        final StorageReference storageReference2 = storageRef.child("students/1-01/ernar.jpg");

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(storageReference2)
                .error(R.drawable.s_icon)
                .into(holder.thumbnail);
        */

        // gs://sdcl-f9f00.appspot.com/students/1-01/img10101.png
//        gs://sdcl-f9f00.appspot.com/students/1-01/user.png
//        gs://sdcl-f9f00.appspot.com/students/1-01/img10104.jpg
//        gs://sdcl-f9f00.appspot.com/students/1-01/реквизит.jpg

        // = "https://firebasestorage.googleapis.com/v0/b/sdcl-f9f00.appspot.com/o/students%2F1-01%2Fimg10101.png?alt=media&token=0232c1bf-137d-48bb-b68a-afc1ac1b64ab";
       // String url2 = "https://firebasestorage.googleapis.com/v0/b/sdcl-f9f00.appspot.com/o/students%2F1-01%2F%D1%80%D0%B5%D0%BA%D0%B2%D0%B8%D0%B7%D0%B8%D1%82.jpg?alt=media&token=3fadf6c6-c9c6-4159-b144-5c9377b83c0b";


//        Glide.with(context)
//                .load(url)
//                .into(holder.thumbnail);

        //holder.thumbnail.setImageResource(item.getThumbnail());


    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public void removeItem(int position) {
        cartList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Student student, int position) {
        cartList.add(position, student);
        notifyItemInserted(position);
    }
}