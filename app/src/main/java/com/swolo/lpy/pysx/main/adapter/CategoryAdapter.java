package com.swolo.lpy.pysx.main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerFatherGoodsEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.InnerHolder> {
    private static final String TAG = "CategoryAdapter";

    private List<NxDistributerFatherGoodsEntity> categoryList = new ArrayList<>();
    private int selectedPosition = 0;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(NxDistributerFatherGoodsEntity entity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<NxDistributerFatherGoodsEntity> categories) {
        Log.d(TAG, "设置商品类别数据，数量: " + (categories != null ? categories.size() : 0));
        this.categoryList = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        Log.d(TAG, "设置选中位置: " + position + ", 当前数据大小: " + categoryList.size());
        if (position >= 0 && position < categoryList.size()) {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "创建ViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_shelf, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        if (categoryList == null || position >= categoryList.size()) {
            return;
        }

        NxDistributerFatherGoodsEntity category = categoryList.get(position);
        
        // 设置选中状态的背景
        boolean isSelected = selectedPosition == position;
        holder.itemView.setSelected(isSelected);
        Log.d(TAG, "绑定视图位置: " + position + ", 是否选中: " + isSelected + ", 类别名称: " + category.getNxDfgFatherGoodsName());
        
        // 显示商品类别名称
        String categoryName = category.getNxDfgFatherGoodsName();
        if (categoryName != null && !categoryName.isEmpty()) {
            holder.shelfName.setText(categoryName);
        } else {
            holder.shelfName.setText("类别 " + (position + 1));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            Log.d(TAG, "商品类别被点击，位置: " + adapterPosition);
            
            if (adapterPosition == RecyclerView.NO_POSITION) {
                Log.e(TAG, "无效的点击位置");
                return;
            }
            
            Log.d(TAG, "点击位置: " + adapterPosition + ", 当前选中位置: " + selectedPosition);
            int oldPosition = selectedPosition;
            selectedPosition = adapterPosition;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            
            if (mListener != null) {
                Log.d(TAG, "触发点击回调，类别名称: " + category.getNxDfgFatherGoodsName());
                mListener.onItemClick(category);
            } else {
                Log.e(TAG, "点击监听器未设置");
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private TextView shelfName;

        public InnerHolder(View itemView) {
            super(itemView);
            shelfName = itemView.findViewById(R.id.tv_shelf_name);
        }
    }
} 