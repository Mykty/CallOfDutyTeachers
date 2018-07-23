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
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.Student;

public class AbsentListAdapter extends RecyclerView.Adapter<AbsentListAdapter.MyViewHolder> {
    private Context context;
    private List<Student> cartList;
    StorageReference storageRef;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView info, group;
        final public ImageView thumbnail;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.info);
            group = view.findViewById(R.id.group);

            thumbnail = view.findViewById(R.id.thumbnail);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);

            storageRef = FirebaseStorage.getInstance().getReference();
        }
    }


    public AbsentListAdapter(Context context, List<Student> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.absent_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Student item = cartList.get(position);
        holder.info.setText(item.getInfo());
        holder.group.setText(item.getGroup());

        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.students)
                .into(holder.thumbnail);
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