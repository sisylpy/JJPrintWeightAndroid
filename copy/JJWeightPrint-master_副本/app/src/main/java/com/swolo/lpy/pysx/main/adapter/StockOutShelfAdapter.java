package com.swolo.lpy.pysx.main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class StockOutShelfAdapter extends RecyclerView.Adapter<StockOutShelfAdapter.InnerHolder> {
   private int selectedPosition = 0;
   private List<NxDistributerGoodsShelfEntity> dataList = new ArrayList<>();
   private OnItemClickListener mListener;
   private static final String TAG = "StockOutShelfAdapter";

   public interface OnItemClickListener {
      void onItemClick(NxDistributerGoodsShelfEntity entity);
   }

   public void setOnItemClickListener(OnItemClickListener listener) {
      this.mListener = listener;
   }

   public int getSelectedPosition() {
      return selectedPosition;
   }

   public void setSelectedPosition(int position) {
      Log.d(TAG, "设置选中位置: " + position + ", 当前数据大小: " + dataList.size());
      if (position >= 0 && position < dataList.size()) {
         int oldPosition = selectedPosition;
         selectedPosition = position;
         notifyItemChanged(oldPosition);
         notifyItemChanged(selectedPosition);
      }
   }

   public void setData(List<NxDistributerGoodsShelfEntity> data) {
      Log.d(TAG, "设置数据，当前选中位置: " + selectedPosition + ", 新数据大小: " + (data != null ? data.size() : 0));
      
      // 保存当前选中的位置
      int oldSelectedPosition = selectedPosition;
      
      this.dataList.clear();
      if (data != null) {
         this.dataList.addAll(data);
      }
      
      // 如果新数据不为空，且之前选中的位置在新数据范围内，保持选中状态
      if (!this.dataList.isEmpty() && oldSelectedPosition < this.dataList.size()) {
         selectedPosition = oldSelectedPosition;
         Log.d(TAG, "保持选中位置: " + selectedPosition);
      } else {
         selectedPosition = 0;
         Log.d(TAG, "重置选中位置为0");
      }
      
      notifyDataSetChanged();
   }

   @Override
   public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_shelf, parent, false);
      return new InnerHolder(view);
   }

   @Override
   public void onBindViewHolder(InnerHolder holder, int position) {
      if (dataList == null || position >= dataList.size()) {
         return;
      }

      NxDistributerGoodsShelfEntity entity = dataList.get(position);
      // 设置选中状态的背景
      boolean isSelected = selectedPosition == position;
      holder.itemView.setSelected(isSelected);
      Log.d(TAG, "绑定视图位置: " + position + ", 是否选中: " + isSelected);
      
      // 直接显示 nxDepartmentAttrName
      String shelfName = entity.getNxDistributerGoodsShelfName();
      if (shelfName != null && !shelfName.isEmpty()) {
         holder.shelfName.setText(shelfName);
      } else {
         holder.shelfName.setText("货架 " + (position + 1));
      }

      holder.itemView.setOnClickListener(v -> {
         int adapterPosition = holder.getAdapterPosition();
         Log.d(TAG, "货架被点击，位置: " + adapterPosition);
         
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
            Log.d(TAG, "触发点击回调，货架名称: " + entity.getNxDistributerGoodsShelfName());
            mListener.onItemClick(entity);
         } else {
            Log.e(TAG, "点击监听器未设置");
         }
      });
   }

   @Override
   public int getItemCount() {
      return dataList != null ? dataList.size() : 0;
   }

   public class InnerHolder extends RecyclerView.ViewHolder {
      private TextView shelfName;

      public InnerHolder(View itemView) {
         super(itemView);
         shelfName = itemView.findViewById(R.id.tv_shelf_name);
      }
   }
}


