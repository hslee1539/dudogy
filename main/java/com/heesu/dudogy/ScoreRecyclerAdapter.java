package com.heesu.dudogy;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;

public class ScoreRecyclerAdapter extends RecyclerView.Adapter<ScoreRecyclerAdapter.ViewHolder> implements Comparator<String> {
    private ArrayList<String> data;

    ScoreRecyclerAdapter(){
        this.data = new ArrayList<>();
    }

    public void pull(String score){
        this.data.add(score);
        this.data.sort(this);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflator.inflate(R.layout.score_item, parent, false);

        return new ScoreRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = data.get(position);
        holder.textView_scoreItem.setText(text);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    @Override
    public int compare(String o1, String o2) {
        return Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2)) * -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView_scoreItem;
        TextView textView_rankItem;

        ViewHolder(View itemView){
            super(itemView);
            textView_scoreItem = itemView.findViewById(R.id.textView_scoreItem);
            textView_rankItem = itemView.findViewById(R.id.textView_rankItem);
            textView_rankItem.setText(Integer.toString(data.size()));
        }
    }
    public class Decoration extends RecyclerView.ItemDecoration{
        public Decoration(){

        }
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1)
                outRect.bottom = parent.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.textView_rankItem).getHeight() + 3;
        }
    }
}
