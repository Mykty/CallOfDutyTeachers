package kz.incubator.myktybake.callofdutyteacher.moderator_files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;

import kz.incubator.myktybake.callofdutyteacher.R;
import kz.incubator.myktybake.callofdutyteacher.module.News;

public class NewsRecyclerViewAdapter  extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolders> {

    private List<News> itemList;
    private Context context;
    String monthStore[];

    public NewsRecyclerViewAdapter(Context context, List<News> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public ViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, null);
        ViewHolders rcv = new ViewHolders(layoutView);
        monthStore = context.getResources().getStringArray(R.array.monthStore);
        return rcv;
    }

    @Override
    public void onBindViewHolder(ViewHolders holder, int position) {
        holder.title.setText(itemList.get(position).getTitle());
        holder.desc.setText(itemList.get(position).getDesc());

        String dateStr = itemList.get(position).getDate();

        String splitedStore[] = dateStr.split(Pattern.quote("."));
        int monthIndex = Integer.parseInt(splitedStore[1])-1;
        String dateText = splitedStore[0]+" "+monthStore[monthIndex]+" "+splitedStore[2];

        holder.date.setText(dateText);

//        Log.i("info", splitedStore[0]);
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    class ViewHolders extends RecyclerView.ViewHolder{

        public TextView title;
        public TextView desc;
        public TextView date;

        public ViewHolders(View itemView) {
            super(itemView);
            title    = itemView.findViewById(R.id.title);
            desc  = itemView.findViewById(R.id.desc);
            date  = itemView.findViewById(R.id.date);
        }
    }
}