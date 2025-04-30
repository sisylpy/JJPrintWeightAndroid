package com.swolo.lpy.pysx.main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.widget.CheckBox;
import android.support.annotation.NonNull;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;

import java.util.ArrayList;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.InnerHolder> {
    private static final String TAG = "DepartmentAdapter";
    
    private List<Object> departmentList = new ArrayList<>();
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Object entity);
    }

    public DepartmentAdapter() {
        Log.d(TAG, "创建适配器");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        Log.d(TAG, "设置点击监听器");
        this.mListener = listener;
    }

    public void setData(List<?> departments) {
        Log.d(TAG, "设置数据: 原始数据大小=" + (departments != null ? departments.size() : 0));
        
        // 验证数据类型
        if (departments != null && !departments.isEmpty()) {
            for (Object department : departments) {
                if (!(department instanceof NxDepartmentEntity) && !(department instanceof GbDepartmentEntity)) {
                    Log.e(TAG, "数据类型不匹配: 期望NxDepartmentEntity或GbDepartmentEntity，实际是" + department.getClass().getSimpleName());
                    return;
                }
            }
        }
        
        this.departmentList = departments != null ? new ArrayList<>(departments) : new ArrayList<>();
        Log.d(TAG, "设置数据完成: 当前数据大小=" + departmentList.size());
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getData() {
        Log.d(TAG, "获取数据: 当前数据大小=" + departmentList.size());
        return (List<T>) departmentList;
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

        Object department = departmentList.get(position);
        if (department instanceof NxDepartmentEntity) {
            NxDepartmentEntity nxDep = (NxDepartmentEntity) department;
            holder.departmentName.setText(nxDep.getNxDepartmentName());
            holder.departmentAttrName.setText(nxDep.getNxDepartmentAttrName());
            Boolean isSelected = nxDep.getSelected();
            Log.d(TAG, "内销部门: " + nxDep.getNxDepartmentName() + ", 选中状态: " + isSelected);
            holder.checkBox.setChecked(isSelected != null && isSelected);
        } else if (department instanceof GbDepartmentEntity) {
            GbDepartmentEntity gbDep = (GbDepartmentEntity) department;
            holder.departmentName.setText(gbDep.getGbDepartmentName());
            holder.departmentAttrName.setText(gbDep.getGbDepartmentAttrName());
            Boolean isSelected = gbDep.getSelected();
            Log.d(TAG, "国标部门: " + gbDep.getGbDepartmentName() + ", 选中状态: " + isSelected);
            holder.checkBox.setChecked(isSelected != null && isSelected);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "点击部门: position=" + position);
            if (mListener != null) {
                mListener.onItemClick(department);
            }
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
        private TextView departmentAttrName;
        private CheckBox checkBox;

        public InnerHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "初始化ViewHolder");
            departmentName = itemView.findViewById(R.id.tv_department_name);
            departmentAttrName = itemView.findViewById(R.id.tv_order_count);
            checkBox = itemView.findViewById(R.id.btn_select);
            checkBox.setClickable(false);  // 让整个item处理点击事件
            Log.d(TAG, "ViewHolder初始化完成");
        }
    }
}