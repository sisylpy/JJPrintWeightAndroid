package com.swolo.lpy.pysx.main.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;

public class LinerAdapter extends RecyclerView.Adapter<LinerAdapter.LinearViewHolder> {


    private Context mContext;
    private OnItemClickListener mListener;

    public LinerAdapter(Context context, OnItemClickListener listener){
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    public LinerAdapter.LinearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_order_view,parent,false));
    }

    @Override
    public void onBindViewHolder(LinerAdapter.LinearViewHolder holder, final int position) {
        holder.textView.setText("nihaoma----");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.click(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 10;
    }


    class LinearViewHolder extends RecyclerView.ViewHolder{

        private TextView textView;
        public LinearViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.weigh_text);

        }
    }

    public interface OnItemClickListener{
        void click(int position);
    }


}
