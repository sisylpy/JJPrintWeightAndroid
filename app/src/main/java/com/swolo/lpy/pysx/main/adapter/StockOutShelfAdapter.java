package com.swolo.lpy.pysx.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;

import java.util.List;

public class StockOutShelfAdapter extends RecyclerView.Adapter<StockOutShelfAdapter.InnerHolder> {
   private List<NxDistributerGoodsShelfEntity> mData;
   private OnItemClickListener mListener;
   private int selectedPosition = 0; // 添加选中位置标记

   public interface OnItemClickListener {
      void onItemClick(NxDistributerGoodsShelfEntity entity);
   }

   public void setOnItemClickListener(OnItemClickListener listener) {
      this.mListener = listener;
   }

   public void setData(List<NxDistributerGoodsShelfEntity> data) {
      this.mData = data;
      selectedPosition = 0; // 重置选中位置
      notifyDataSetChanged();
   }

   @Override
   public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_shelf, parent, false);
      return new InnerHolder(view);
   }

   @Override
   public void onBindViewHolder(InnerHolder holder, int position) {
      if (mData == null || position >= mData.size()) {
         return;
      }

      NxDistributerGoodsShelfEntity entity = mData.get(position);
      // 设置选中状态的背景
      holder.itemView.setSelected(selectedPosition == position);
      
      // 直接显示 nxDepartmentAttrName
      String shelfName = entity.getNxDistributerGoodsShelfName();
      if (shelfName != null && !shelfName.isEmpty()) {
         holder.shelfName.setText(shelfName);
      } else {
         holder.shelfName.setText("货架 " + (position + 1));
      }

      holder.itemView.setOnClickListener(v -> {
         int adapterPosition = holder.getAdapterPosition();
         if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
         }
         
         int oldPosition = selectedPosition;
         selectedPosition = adapterPosition;
         notifyItemChanged(oldPosition);
         notifyItemChanged(selectedPosition);
         
         if (mListener != null) {
            mListener.onItemClick(entity);
         }
      });
   }

   @Override
   public int getItemCount() {
      return mData != null ? mData.size() : 0;
   }

   public class InnerHolder extends RecyclerView.ViewHolder {
      private TextView shelfName;

      public InnerHolder(View itemView) {
         super(itemView);
         shelfName = itemView.findViewById(R.id.tv_shelf_name);
      }
   }
}


