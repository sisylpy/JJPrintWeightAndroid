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

        this.departmentList = new ArrayList<>();

        // 添加标题和部门
        boolean hasNxDepartment = false;
        boolean hasGbDepartment = false;

        if (departments != null) {
            for (Object department : departments) {
                if (department instanceof NxDepartmentEntity) {
                    if (!hasNxDepartment) {
                        this.departmentList.add("配送客户");
                        hasNxDepartment = true;
                    }
                    this.departmentList.add(department);
                } else if (department instanceof GbDepartmentEntity) {
                    if (!hasGbDepartment) {
                        this.departmentList.add("平台客户");
                        hasGbDepartment = true;
                    }
                    this.departmentList.add(department);
                }
            }
        }

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

        Object item = departmentList.get(position);
        if (item instanceof String) {
            // 显示标题
            holder.departmentName.setText((String) item);
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setClickable(false);
            holder.itemView.setBackgroundResource(android.R.color.darker_gray);
            return;
        }

        // 显示部门信息
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.itemView.setClickable(true);
        holder.itemView.setBackgroundResource(android.R.color.white);

        if (item instanceof NxDepartmentEntity) {
            NxDepartmentEntity nxDep = (NxDepartmentEntity) item;
            String displayText = "(" + nxDep.getNxDepartmentPickName() + ")" + nxDep.getNxDepartmentName();
            holder.departmentName.setText(displayText);
            Boolean isSelected = nxDep.getSelected();
            Log.d(TAG, "内销部门: " + nxDep.getNxDepartmentName() + ", 选中状态: " + isSelected);
            holder.checkBox.setChecked(isSelected != null ? isSelected : false);
        } else if (item instanceof GbDepartmentEntity) {
            GbDepartmentEntity gbDep = (GbDepartmentEntity) item;
            String displayText = "(" + gbDep.getGbDepartmentAttrName() + ")" + gbDep.getGbDepartmentName();
            holder.departmentName.setText(displayText);
            Boolean isSelected = gbDep.getSelected();
            Log.d(TAG, "国标部门: " + gbDep.getGbDepartmentName() + ", 选中状态: " + isSelected);
            holder.checkBox.setChecked(isSelected != null ? isSelected : false);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "点击部门: position=" + position);
            if (mListener != null) {
                mListener.onItemClick(item);
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
        private CheckBox checkBox;

        public InnerHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "初始化ViewHolder");
            departmentName = itemView.findViewById(R.id.tv_department_name);
            checkBox = itemView.findViewById(R.id.btn_select);

            // 添加日志检查视图是否找到
            if (departmentName == null) {
                Log.e(TAG, "找不到 tv_department_name TextView");
            } else {
                Log.d(TAG, "成功找到 tv_department_name TextView");
            }

            checkBox.setClickable(false);  // 让整个item处理点击事件
            Log.d(TAG, "ViewHolder初始化完成");
        }
    }
}