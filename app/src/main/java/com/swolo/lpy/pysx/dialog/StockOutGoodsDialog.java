package com.swolo.lpy.pysx.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.google.gson.Gson;

import java.util.List;
import java.util.ArrayList;
import android.widget.Toast;

import android.view.ViewGroup;
import android.util.Log;
import android.view.Gravity;
import android.content.SharedPreferences;
import android.bluetooth.le.BluetoothLeScanner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import java.util.UUID;

public class StockOutGoodsDialog extends Dialog {
    private static final int REQUEST_SCALE_ACTIVITY = 1003;
    private static final String TAG = "StockOutGoodsDialog";
    private Context mContext;
    private NxDistributerGoodsShelfGoodsEntity goodsEntity;
    private OnConfirmListener confirmListener;
    private RecyclerView ordersRecyclerView;
    private StockOutOrdersAdapter ordersAdapter;
    private TextView scrollHint;
    private LinearLayout llOrders;
    private TextView tvTitle;
    private TextView tvScaleStatus;
    private String scaleName;
    private String scaleAddress;
    private boolean isScaleConnected;
    private double lastWeight = 0;
    private BluetoothLeScanner bluetoothLeScanner;
    private BroadcastReceiver weightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("StockOutGoodsDialog", "[广播] onReceive: action=" + intent.getAction() + 
                ", intent=" + intent + ", extras=" + intent.getExtras());
            if ("ACTION_SCALE_WEIGHT".equals(intent.getAction())) {
                double weight = intent.getDoubleExtra("weight", 0.0);
                Log.d("StockOutGoodsDialog", "[广播] 收到蓝牙秤重量: " + weight);
                setWeight(weight);
            }
        }
    };

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private double tareWeight = 0;
    private boolean autoTareDone = false;

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "[蓝牙] onConnectionStateChange: status=" + status + ", newState=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "[蓝牙] 设备已连接，开始发现服务");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "[蓝牙] 设备已断开连接");
                closeGatt();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "[蓝牙] onServicesDiscovered: status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "[蓝牙] 服务发现成功，查找特征...");
                for (BluetoothGattService service : gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        int properties = characteristic.getProperties();
                        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                            writeCharacteristic = characteristic;
                            Log.d(TAG, "[蓝牙] 找到写入特征: " + characteristic.getUuid());
                        }
                        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            notifyCharacteristic = characteristic;
                            Log.d(TAG, "[蓝牙] 找到通知特征: " + characteristic.getUuid());
                            gatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                                Log.d(TAG, "[蓝牙] 设置通知描述符成功");
                            } else {
                                Log.e(TAG, "[蓝牙] 未找到通知描述符");
                            }
                        }
                    }
                }
            } else {
                Log.e(TAG, "[蓝牙] 服务发现失败: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "[蓝牙] onCharacteristicChanged: 特征=" + characteristic.getUuid());
            if (characteristic == notifyCharacteristic) {
                byte[] data = characteristic.getValue();
                Log.d(TAG, "[蓝牙] 收到原始数据: " + java.util.Arrays.toString(data));
                if (data.length == 7 && data[0] == 0x05) {
                    final int status = data[1];
                    final int rawWeight = ((data[3] & 0xFF) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
                    final boolean isStable = (status & 0x40) != 0;
                    // 自动去皮：首次收到稳定重量时记为皮重
                    if (!autoTareDone && isStable) {
                        tareWeight = rawWeight;
                        autoTareDone = true;
                        Log.d(TAG, "[蓝牙] 自动去皮完成，皮重: " + tareWeight + "g");
                    }
                    final double realWeight = Math.round((rawWeight - tareWeight) * 10) / 10.0;
                    Log.d(TAG, "[蓝牙] 收到重量: " + realWeight + "g, 是否稳定: " + isStable);
                    if (isStable) {
                        Log.d(TAG, "[蓝牙] 重量稳定，准备写入输入框");
                        setWeight(realWeight);
                    }
                } else {
                    Log.d(TAG, "[蓝牙] 数据格式不正确: 长度=" + data.length);
                }
            }
        }
    };

    public StockOutGoodsDialog(Context context, NxDistributerGoodsShelfGoodsEntity goods) {
        super(context, R.style.Theme_dialog);
        this.mContext = context;
        this.goodsEntity = goods;
        Log.d("StockOutGoodsDialog", "创建对话框，商品: " + 
            (goods != null ? goods.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
        Log.d("StockOutGoodsDialog", "订单数据: " + 
            (goods != null && goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
            "数量=" + goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : "null"));
    }

    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        // 自动重置去皮，每次弹窗都能自动去皮
        autoTareDone = false;
        tareWeight = 0;
    setContentView(R.layout.dialog_stock_out_goods);
        Log.d("StockOutGoodsDialog", "[生命周期] onCreate: 开始注册广播");
        try {
            IntentFilter filter = new IntentFilter("ACTION_SCALE_WEIGHT");
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(weightReceiver, filter);
            Log.d("StockOutGoodsDialog", "[广播] 注册成功: ACTION_SCALE_WEIGHT");
        } catch (Exception e) {
            Log.e("StockOutGoodsDialog", "[广播] 注册异常", e);
        }

    // 设置弹窗圆角背景和半透明遮罩
    Window window = getWindow();
    if (window != null) {
        window.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0.6f;
        // 设置弹窗宽度为屏幕宽度的90%
        lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        // 设置弹窗高度为自适应内容，但不超过屏幕高度的80%
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        }

        // 连接蓝牙秤
        connectToScale();
        
        // 初始化视图
        initView();
    }

    private void initView() {
        Log.d("StockOutGoodsDialog", "[弹窗] 初始化: goodsEntity=" + new Gson().toJson(goodsEntity));
        if (goodsEntity == null) {
            Log.e("StockOutGoodsDialog", "商品实体为空");
            return;
        }
        
        Log.d("StockOutGoodsDialog", "初始化商品对话框: " + goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName());
        // 新增详细日志
        Log.d("StockOutGoodsDialog", "DistributerGoodsEntity: " + new Gson().toJson(goodsEntity.getNxDistributerGoodsEntity()));
        List<NxDepartmentOrdersEntity> debugOrders = null;
        if (goodsEntity.getNxDistributerGoodsEntity() != null) {
            debugOrders = goodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        }
        Log.d("StockOutGoodsDialog", "nxDepartmentOrdersEntities: " + new Gson().toJson(debugOrders));
        Log.d("StockOutGoodsDialog", "订单数量debug: " + (debugOrders != null ? debugOrders.size() : 0));
        
        // 设置标题
        tvTitle = findViewById(R.id.tv_goods_title);
        if (tvTitle != null && goodsEntity != null) {
            String goodsName = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName();
            Log.d("StockOutGoodsDialog", "设置商品标题: " + goodsName);
            tvTitle.setText(goodsName);
        } else {
            Log.e("StockOutGoodsDialog", "商品实体为空或标题视图为空");
        }

        // 新增：弹窗顶部显示蓝牙秤状态
        tvScaleStatus = findViewById(R.id.tv_scale_status);
        SharedPreferences sp = mContext.getSharedPreferences("scale_cache", Context.MODE_PRIVATE);
        scaleName = sp.getString("scale_name", null);
        scaleAddress = sp.getString("scale_address", null);
        isScaleConnected = scaleAddress != null;
        lastWeight = sp.getFloat("last_weight", 0f);
        StringBuilder scaleInfo = new StringBuilder();
        if (isScaleConnected) {
            scaleInfo.append("蓝牙秤: 已连接\n");
            // scaleInfo.append("名称: ").append(scaleName != null ? scaleName : "").append("\n");
            // scaleInfo.append("地址: ").append(scaleAddress != null ? scaleAddress : "");
            if (lastWeight > 0) {
                double weightInJin = lastWeight / 500.0;  // 将克转换为斤
                scaleInfo.append("\n最近称重: ").append(String.format("%.2f", weightInJin)).append("斤");
            }
        } else {
            scaleInfo.append("蓝牙秤: 未连接");
        }
        if (tvScaleStatus != null) {
            tvScaleStatus.setText(scaleInfo.toString());
        }

        // 初始化订单列表
        ordersRecyclerView = findViewById(R.id.rv_orders);
        if (ordersRecyclerView != null) {
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            
            // 只从 DistributerGoodsEntity 解析订单
            List<NxDepartmentOrdersEntity> orders = null;
            if (goodsEntity.getNxDistributerGoodsEntity() != null) {
                orders = goodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
            }
            Log.d("StockOutGoodsDialog", "[弹窗] 订单数量: " + (orders != null ? orders.size() : 0));
            
            if (orders != null && !orders.isEmpty()) {
                ordersAdapter = new StockOutOrdersAdapter((android.app.Activity) mContext, orders, goodsEntity);
        ordersRecyclerView.setAdapter(ordersAdapter);
                Log.d("StockOutGoodsDialog", "订单适配器设置完成");
                for (NxDepartmentOrdersEntity order : orders) {
                    Log.d("StockOutGoodsDialog", "[弹窗] 订单详情: " + new Gson().toJson(order));
                }
                } else {
                Log.d("StockOutGoodsDialog", "没有订单数据");
                // 显示无订单提示
                TextView tvNoOrders = new TextView(getContext());
                tvNoOrders.setText("暂无订单");
                tvNoOrders.setGravity(Gravity.CENTER);
                tvNoOrders.setPadding(0, 20, 0, 20);
                ((ViewGroup) ordersRecyclerView.getParent()).addView(tvNoOrders);
            }
        }

        // 自动赋值称重到第一个订单
        if (lastWeight > 0 && ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
            ordersAdapter.updateWeightAtPosition(0, lastWeight);
        }

        // 设置取消按钮
        TextView btnCancel = findViewById(R.id.btn_cancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                Log.d("StockOutGoodsDialog", "点击取消按钮");
        dismiss();
    });
        }

        // 设置确认按钮
        TextView btnConfirm = findViewById(R.id.btn_confirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                Log.d("StockOutGoodsDialog", "[弹窗] 点击确认: 当前订单数据=" + new Gson().toJson(goodsEntity));
                if (confirmListener != null && goodsEntity != null) {
                    List<NxDepartmentOrdersEntity> orders = null;
                    if (ordersAdapter != null) {
                        orders = ordersAdapter.getUpdatedOrders();
                    }
                    if (goodsEntity.getNxDistributerGoodsEntity() != null) {
                        goodsEntity.getNxDistributerGoodsEntity().setNxDepartmentOrdersEntities(orders);
                    }
                    if (orders != null) {
                        NxDepartmentOrdersEntity firstOrderWithWeight = null;
                        for (NxDepartmentOrdersEntity order : orders) {
                            String weight = order.getNxDoWeight();
                            if (weight != null && !weight.isEmpty() && Double.parseDouble(weight) > 0) {
                                firstOrderWithWeight = order;
                                break;
                            }
                        }
                        if (firstOrderWithWeight != null) {
                            Log.d("StockOutGoodsDialog", "只回传第一个有重量的订单: " + firstOrderWithWeight.getNxDoWeight());
                            printOrder(firstOrderWithWeight);
                            final Integer removeOrderId = firstOrderWithWeight.getNxDepartmentOrdersId();
                            requestOrderFinish(firstOrderWithWeight, () -> {
                                if (ordersAdapter != null) {
                                    java.util.List<NxDepartmentOrdersEntity> list = ordersAdapter.getUpdatedOrders();
                                    java.util.Iterator<NxDepartmentOrdersEntity> it = list.iterator();
                                    while (it.hasNext()) {
                                        NxDepartmentOrdersEntity order = it.next();
                                        if (order.getNxDepartmentOrdersId().equals(removeOrderId)) {
                                            it.remove();
                                            break;
                                        }
                                    }
                                    ordersAdapter.notifyDataSetChanged();
                                }
                                if (ordersAdapter == null || ordersAdapter.getItemCount() == 0) {
        dismiss();
                                }
                            });
                            List<NxDepartmentOrdersEntity> singleList = new java.util.ArrayList<>();
                            singleList.add(firstOrderWithWeight);
                            confirmListener.onConfirm(singleList);
                        } else {
                            android.widget.Toast.makeText(getContext(), "请先称重", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }
    });
}

        Log.d("StockOutGoodsDialog", "视图初始化完成");
    }

    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (requestCode == REQUEST_SCALE_ACTIVITY && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.hasExtra("weight")) {
                double weight = data.getDoubleExtra("weight", 0.0);
                Log.d("StockOutGoodsDialog", "[弹窗] onActivityResult: 获取到蓝牙称重量 weight=" + weight);
                if (ordersAdapter != null) {
                    ordersAdapter.updateWeight(weight);
                    Log.d("StockOutGoodsDialog", "[弹窗] onActivityResult: 调用ordersAdapter.updateWeight, weight=" + weight);
                } else {
                    Log.d("StockOutGoodsDialog", "[弹窗] onActivityResult: ordersAdapter为null");
                }
            } else {
                Log.d("StockOutGoodsDialog", "[弹窗] onActivityResult: data无weight");
            }
        }
    }

    public interface OnConfirmListener {
        void onConfirm(List<NxDepartmentOrdersEntity> orders);
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }

    public void setWeight(double weight) {
        Log.d(TAG, "[弹窗] setWeight: weight=" + weight);
        if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
            Log.d(TAG, "[弹窗] 更新第一条订单重量");
            ordersAdapter.updateWeightAtPosition(0, weight);
        } else {
            Log.e(TAG, "[弹窗] 无法更新重量: ordersAdapter=" + ordersAdapter + ", itemCount=" + (ordersAdapter != null ? ordersAdapter.getItemCount() : 0));
        }
    }

    private void connectToScale() {
        Log.d(TAG, "[蓝牙] 开始连接蓝牙秤，检测缓存...");
        SharedPreferences sp = getContext().getSharedPreferences("scale_cache", Context.MODE_PRIVATE);
        String scaleAddress = sp.getString("scale_address", null);
        Log.d(TAG, "[蓝牙] 读取缓存 scale_address=" + scaleAddress);
        
        if (scaleAddress != null) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(scaleAddress);
            Log.d(TAG, "[蓝牙] 获取到设备: " + (device != null ? device.getAddress() : "null"));
            
            bluetoothGatt = device.connectGatt(getContext(), false, gattCallback);
            Log.d(TAG, "[蓝牙] 开始连接GATT");
        } else {
            Log.e(TAG, "[蓝牙] 未找到蓝牙秤地址，无法连接");
        }
    }

    private void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Override
    public void dismiss() {
        Log.d(TAG, "[生命周期] dismiss: 关闭蓝牙连接");
        closeGatt();
        Log.d("StockOutGoodsDialog", "[生命周期] dismiss: 注销广播");
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(weightReceiver);
            Log.d("StockOutGoodsDialog", "[广播] 注销成功");
        } catch (Exception e) {
            Log.e("StockOutGoodsDialog", "注销广播异常", e);
        }
        super.dismiss();
    }

    private void printOrder(NxDepartmentOrdersEntity order) {
        Log.d("StockOutGoodsDialog", "[弹窗] 打印订单: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
        // 调用主页面的printAndSaveOrders方法
        if (getContext() instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
            com.swolo.lpy.pysx.main.StockOutActivity activity = (com.swolo.lpy.pysx.main.StockOutActivity) getContext();
            java.util.List<NxDepartmentOrdersEntity> list = new java.util.ArrayList<>();
            list.add(order);
            activity.printAndSaveOrders(list, 0);
        } else {
            Log.e("StockOutGoodsDialog", "[弹窗] Context不是StockOutActivity，无法调用printAndSaveOrders");
        }
    }

    private void requestOrderFinish(NxDepartmentOrdersEntity order, Runnable onSuccess) {
        Log.d("StockOutGoodsDialog", "[弹窗] 请求网络完成订单: orderId=" + order.getNxDepartmentOrdersId());
        // TODO: 实际项目中应调用网络接口，这里直接回调onSuccess模拟
        if (onSuccess != null) onSuccess.run();
    }
}