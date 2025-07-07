package com.swolo.lpy.pysx.main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.InnerHolder> {
    private static final String TAG = "DepartmentAdapter";

    private List<Object> departmentList = new ArrayList<>();
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Object entity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public DepartmentAdapter() {
        Log.d(TAG, "创建适配器");
    }

    public void setData(List<?> departments) {
        Log.d(TAG, "设置数据: 原始数据大小=" + (departments != null ? departments.size() : 0));
        this.departmentList = new ArrayList<>();
        if (departments != null) {
            for (Object department : departments) {
                if (department instanceof NxDepartmentEntity || department instanceof GbDepartmentEntity) {
                    this.departmentList.add(department);
                }
            }
        }
        Log.d(TAG, "设置数据完成: 当前数据大小=" + departmentList.size());
        notifyDataSetChanged();
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "创建ViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_department, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        Log.d(TAG, "绑定ViewHolder: position=" + position);
        if (departmentList == null || position >= departmentList.size()) {
            return;
        }
        Object item = departmentList.get(position);
        if (item instanceof NxDepartmentEntity) {
            NxDepartmentEntity nxDep = (NxDepartmentEntity) item;
            String displayText = (nxDep.getNxDepartmentPickName() != null ? "(" + nxDep.getNxDepartmentPickName() + ")" : "") + nxDep.getNxDepartmentName();
            holder.departmentName.setText(displayText);
        } else if (item instanceof GbDepartmentEntity) {
            GbDepartmentEntity gbDep = (GbDepartmentEntity) item;
            String displayText = (gbDep.getGbDepartmentAttrName() != null ? "(" + gbDep.getGbDepartmentAttrName() + ")" : "") + gbDep.getGbDepartmentName();
            holder.departmentName.setText(displayText);
        }
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        int count = departmentList != null ? departmentList.size() : 0;
        Log.d(TAG, "获取项目数量: " + count);
        return count;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private TextView departmentName;
        public InnerHolder(View itemView) {
            super(itemView);
            departmentName = itemView.findViewById(R.id.tv_department_name);
        }
    }
}