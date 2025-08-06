package com.swolo.lpy.pysx.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.google.gson.Gson;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Method;
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
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.os.Build;
import com.swolo.lpy.pysx.main.CustomerStockOutActivity;
import com.swolo.lpy.pysx.main.DeviceConnFactoryManager;
import java.util.Vector;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;



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
    // ========== tvScaleStatus变量已删除（2025-07-08）==========
    // 功能说明：蓝牙秤状态显示控件
    // 删除原因：用户要求删除弹窗上面的蓝牙秤状态区域
    // private TextView tvScaleStatus;
    // ========== tvScaleStatus变量删除结束 ==========
    private String scaleName;
    private String scaleAddress;
    private boolean isScaleConnected;
    // 删除lastWeight相关字段，不再需要缓存重量
    private boolean isInitialized = false; // 防止初始化阶段被蓝牙秤数据覆盖
    private BluetoothLeScanner bluetoothLeScanner;
    private BroadcastReceiver weightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "[广播] onReceive: action=" + intent.getAction() + 
                ", intent=" + intent + ", extras=" + intent.getExtras());
            if ("ACTION_SCALE_WEIGHT".equals(intent.getAction())) {
                double weight = intent.getDoubleExtra("weight", 0.0);
                Log.d(TAG, "[广播] 收到蓝牙秤重量: " + weight);
                setWeight(weight);
            }
        }
    };

    private double tareWeight = 0;
    private boolean autoTareDone = false;
    
    // 蓝牙秤连接相关
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    
    // GATT回调 - 简化版本，参考成功项目
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "[蓝牙] onConnectionStateChange: status=" + status + ", newState=" + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "[蓝牙] 连接成功，开始发现服务");
                    isScaleConnected = true;
                    
                    // ========== updateScaleStatus调用已删除（2025-07-08）==========
                    // 功能说明：更新蓝牙秤状态显示
                    // 删除原因：用户要求删除弹窗上面的蓝牙秤状态区域
                    /*
                    // 在主线程中更新UI
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).runOnUiThread(() -> {
                            updateScaleStatus(true, null, scaleAddress);
                        });
                    }
                    */
                    // ========== updateScaleStatus调用删除结束 ==========
                    
                    gatt.discoverServices();
                    
                    // 添加服务发现超时检测
                    new Handler().postDelayed(() -> {
                        Log.d(TAG, "[蓝牙] 服务发现超时检查");
                        if (writeCharacteristic == null || notifyCharacteristic == null) {
                            Log.e(TAG, "[蓝牙] ❌ 服务发现超时！特征为空，可能原因：");
                            Log.e(TAG, "[蓝牙] 1. 设备不支持该服务UUID");
                            Log.e(TAG, "[蓝牙] 2. 设备响应超时");
                            Log.e(TAG, "[蓝牙] 3. 连接不稳定");
                            Log.e(TAG, "[蓝牙] 4. 设备状态异常");
                            
                            // 尝试重新发现服务
                            Log.d(TAG, "[蓝牙] 尝试重新发现服务...");
                            gatt.discoverServices();
                        } else {
                            Log.d(TAG, "[蓝牙] ✅ 服务发现成功，特征已找到");
                        }
                    }, 10000); // 10秒超时
                    
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "[蓝牙] 连接断开");
                    isScaleConnected = false;
                    
                    // ========== updateScaleStatus调用已删除（2025-07-08）==========
                    // 功能说明：更新蓝牙秤状态显示
                    // 删除原因：用户要求删除弹窗上面的蓝牙秤状态区域
                    /*
                    // 在主线程中更新UI
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).runOnUiThread(() -> {
                            updateScaleStatus(false, null, scaleAddress);
                        });
                    }
                    */
                    // ========== updateScaleStatus调用删除结束 ==========
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "[蓝牙] onServicesDiscovered: status=" + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "[蓝牙] 服务发现成功");
                    
                    // 调试：显示所有发现的服务
                    Log.d(TAG, "[蓝牙] 🔍 开始检查所有发现的服务...");
                    List<BluetoothGattService> services = gatt.getServices();
                    Log.d(TAG, "[蓝牙] 📊 发现服务数量: " + (services != null ? services.size() : 0));
                    
                    if (services != null) {
                        for (int i = 0; i < services.size(); i++) {
                            BluetoothGattService service = services.get(i);
                            String serviceUuid = service.getUuid().toString().toLowerCase();
                            Log.d(TAG, "[蓝牙] 服务" + i + ": " + serviceUuid);
                            
                            // 检查特征
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            Log.d(TAG, "[蓝牙] 服务" + i + "特征数量: " + (characteristics != null ? characteristics.size() : 0));
                            
                            if (characteristics != null) {
                                for (int j = 0; j < characteristics.size(); j++) {
                                    BluetoothGattCharacteristic characteristic = characteristics.get(j);
                                    String charUuid = characteristic.getUuid().toString().toLowerCase();
                                    Log.d(TAG, "[蓝牙] 服务" + i + "特征" + j + ": " + charUuid + 
                                          " (属性: " + characteristic.getProperties() + ")");
                                }
                            }
                        }
                    }
                    
                    // 查找特征 - 尝试多种UUID格式
                    BluetoothGattService service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                    if (service == null) {
                        Log.w(TAG, "[蓝牙] ⚠️ 未找到标准UUID服务，尝试其他格式...");
                        // 尝试其他常见的UUID格式
                        service = gatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                        if (service != null) {
                            Log.d(TAG, "[蓝牙] ✅ 找到FFF0服务");
                        } else {
                            service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                            if (service != null) {
                                Log.d(TAG, "[蓝牙] ✅ 找到FFE0服务（大写）");
                            }
                        }
                    }
                    
                    if (service != null) {
                        Log.d(TAG, "[蓝牙] 找到服务");
                        writeCharacteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                        notifyCharacteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                        
                        // 如果标准UUID没找到，尝试其他格式
                        if (writeCharacteristic == null) {
                            Log.w(TAG, "[蓝牙] ⚠️ 未找到标准特征UUID，尝试其他格式...");
                            writeCharacteristic = service.getCharacteristic(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
                            notifyCharacteristic = service.getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
                            if (writeCharacteristic != null) {
                                Log.d(TAG, "[蓝牙] ✅ 找到FFF1/FFF2特征");
                            }
                        }
                        
                        if (writeCharacteristic != null) {
                            Log.d(TAG, "[蓝牙] 找到写特征");
                        }
                        if (notifyCharacteristic != null) {
                            Log.d(TAG, "[蓝牙] 找到通知特征");
                            boolean success = gatt.setCharacteristicNotification(notifyCharacteristic, true);
                            Log.d(TAG, "[蓝牙] 设置通知: " + (success ? "成功" : "失败"));
                            
                            // 设置通知描述符 - 参考成功项目
                            BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                success = gatt.writeDescriptor(descriptor);
                                Log.d(TAG, "[蓝牙] 设置通知描述符: " + (success ? "成功" : "失败"));
                            } else {
                                Log.e(TAG, "[蓝牙] 未找到通知描述符");
                            }
                        }
                    } else {
                        Log.e(TAG, "[蓝牙] 未找到服务");
                    }
                } else {
                    Log.e(TAG, "[蓝牙] 服务发现失败: " + status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "[蓝牙] onCharacteristicChanged - 特征UUID: " + characteristic.getUuid());
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.d(TAG, "[蓝牙] 收到数据: " + bytesToHex(data) + ", 长度: " + data.length);
                    // 解析重量数据
                    parseWeightData(data);
                } else {
                    Log.w(TAG, "[蓝牙] 收到空数据");
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "[蓝牙] 描述符写入: status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "[蓝牙] 描述符写入成功，通知已启用");
            } else {
                Log.e(TAG, "[蓝牙] 描述符写入失败: " + status);
            }
        }
    };

    // 简化的打印模式判断
    private boolean isPrintMode = false; // true=打印标签, false=非打印标签
    
    // 保存被修改的订单列表，确保只传递被修改的订单
    private List<NxDepartmentOrdersEntity> modifiedOrders = new ArrayList<>();

    public StockOutGoodsDialog(Context context, NxDistributerGoodsShelfGoodsEntity goods, boolean isPrintMode) {
        super(context, R.style.Theme_dialog);
        try {
            Log.d(TAG, "[弹窗] ========== 构造方法开始 ==========");
            Log.d(TAG, "[弹窗] 构造方法被调用: context=" + (context != null ? context.getClass().getSimpleName() : "null"));
            this.mContext = context;
            this.goodsEntity = goods;
            this.isPrintMode = isPrintMode;
            Log.d(TAG, "[弹窗] 构造方法: 商品=" + 
                (goods != null ? goods.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
            Log.d(TAG, "[弹窗] 构造方法: 订单数据=" + 
                (goods != null && goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
                "数量=" + goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : "null"));
            Log.d(TAG, "[弹窗] 构造方法: 打印模式=" + (isPrintMode ? "打印标签" : "非打印标签"));
            Log.d(TAG, "[弹窗] ========== 构造方法结束 ==========");
        } catch (Exception e) {
            Log.e(TAG, "[弹窗] 构造方法异常: " + e.getMessage(), e);
        }
    }
    
    // 重载构造函数，用于 StockOutActivity 的调用
    public StockOutGoodsDialog(Context context, NxDistributerGoodsShelfGoodsEntity goods) {
        super(context, R.style.Theme_dialog);
        this.mContext = context;
        this.goodsEntity = goods;
        this.isPrintMode = false; // 默认非打印标签模式
        Log.d(TAG, "创建对话框（StockOutActivity），商品: " + 
            (goods != null ? goods.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
        Log.d(TAG, "订单数据: " + 
            (goods != null && goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
            "数量=" + goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : "null"));
        Log.d(TAG, "打印模式: 默认非打印标签模式");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[弹窗] ========== onCreate开始 ==========");
        Log.d(TAG, "[弹窗] onCreate被调用: savedInstanceState=" + (savedInstanceState != null ? "不为空" : "为空"));
        super.onCreate(savedInstanceState);
        // 自动重置去皮，每次弹窗都能自动去皮
        autoTareDone = false;
        tareWeight = 0;
        
        // 【新增】根据用户设置选择布局文件
        int layoutResId = getLayoutBasedOnOrientation();
        Log.d(TAG, "[弹窗] onCreate: 选择的布局ID: " + layoutResId);
        Log.d(TAG, "[弹窗] onCreate: 布局名称: " + (layoutResId == R.layout.dialog_stock_out_goods ? "竖屏布局" : "横屏布局"));
        Log.d(TAG, "[弹窗] onCreate: 竖屏布局ID: " + R.layout.dialog_stock_out_goods);
        Log.d(TAG, "[弹窗] onCreate: 横屏布局ID: " + R.layout.dialog_stock_out_goods_landscape);
        setContentView(layoutResId);
        Log.d(TAG, "[弹窗] onCreate: setContentView完成，使用布局: " + layoutResId);
        
        // 【新增】应用横竖屏设置
        applyScreenOrientation();
        
        // 注册广播接收器来接收蓝牙秤重量数据
        Log.d(TAG, "[弹窗] onCreate: 开始注册广播");
        try {
            IntentFilter filter = new IntentFilter("ACTION_SCALE_WEIGHT");
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(weightReceiver, filter);
            Log.d(TAG, "[广播] 注册成功: ACTION_SCALE_WEIGHT");
        } catch (Exception e) {
            Log.e(TAG, "[广播] 注册异常", e);
        }
        
        // 初始化视图
        initView();
        Log.d(TAG, "[弹窗] onCreate: initView完成");
        
        // 连接蓝牙秤
        connectToScale();
        Log.d(TAG, "[弹窗] onCreate: 蓝牙秤连接完成");
        
        // 重新检查设备状态并更新模式显示
        Log.e(TAG, "[弹窗] onCreate: 准备调用updateModeStatusWithRealTimeCheck");
        updateModeStatusWithRealTimeCheck();
        Log.e(TAG, "[弹窗] onCreate: updateModeStatusWithRealTimeCheck调用完成");
        Log.d(TAG, "[弹窗] onCreate: 当前模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
        Log.d(TAG, "[弹窗] onCreate: 模式状态显示完成");
    }

    private void initView() {
        Log.d(TAG, "[弹窗] initView: 开始初始化视图");
        if (goodsEntity == null) {
            Log.e(TAG, "[弹窗] initView: 商品实体为空");
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
        TextView tvGoodsStandard = findViewById(R.id.tv_goods_standard);
        TextView tvGoodsBrand = findViewById(R.id.tv_goods_brand);
        
        Log.d(TAG, "🔍 tvTitle: " + (tvTitle != null ? "找到" : "为null"));
        Log.d(TAG, "🔍 tvGoodsStandard: " + (tvGoodsStandard != null ? "找到" : "为null"));
        Log.d(TAG, "🔍 tvGoodsBrand: " + (tvGoodsBrand != null ? "找到" : "为null"));
        Log.d(TAG, "🔍 goodsEntity: " + (goodsEntity != null ? "找到" : "为null"));
        
        if (tvTitle != null && goodsEntity != null) {
            String goodsName = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName();
            Log.d("StockOutGoodsDialog", "设置商品标题: " + goodsName);
            tvTitle.setText(goodsName);
            
            // 设置商品品牌信息（参考微信小程序显示方式）
            if (tvGoodsBrand != null) {
                String brand = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand();
                if (brand != null && !brand.isEmpty() && !"null".equals(brand)) {
                    tvGoodsBrand.setText(brand);
                    tvGoodsBrand.setVisibility(View.VISIBLE);
                    Log.d("StockOutGoodsDialog", "设置商品品牌: " + brand);
                } else {
                    tvGoodsBrand.setVisibility(View.GONE);
                    Log.d("StockOutGoodsDialog", "商品品牌为空，隐藏品牌显示");
                }
            }
            
            // 设置商品规格信息（参考微信小程序显示方式）
            Log.d(TAG, "🔍 开始设置商品规格信息");
            if (tvGoodsStandard != null) {
                String standardWeight = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardWeight();
                String standardName = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname();
                
                Log.d("StockOutGoodsDialog", "=== 规格信息调试 ===");
                Log.d("StockOutGoodsDialog", "standardWeight: '" + standardWeight + "'");
                Log.d("StockOutGoodsDialog", "standardName: '" + standardName + "'");
                Log.d("StockOutGoodsDialog", "standardWeight != null: " + (standardWeight != null));
                Log.d("StockOutGoodsDialog", "standardWeight.isEmpty(): " + (standardWeight != null ? standardWeight.isEmpty() : "N/A"));
                Log.d("StockOutGoodsDialog", "standardWeight.equals('null'): " + (standardWeight != null ? "null".equals(standardWeight) : "N/A"));
                Log.d("StockOutGoodsDialog", "standardName != null: " + (standardName != null));
                Log.d("StockOutGoodsDialog", "standardName.isEmpty(): " + (standardName != null ? standardName.isEmpty() : "N/A"));
                Log.d("StockOutGoodsDialog", "standardName.equals('null'): " + (standardName != null ? "null".equals(standardName) : "N/A"));
                Log.d("StockOutGoodsDialog", "standardName.equals('斤'): " + (standardName != null ? "斤".equals(standardName) : "N/A"));
                
                // 微信小程序逻辑：只有当standardWeight不为空且不为"null"时才显示
                if (standardWeight != null && !standardWeight.isEmpty() && !"null".equals(standardWeight)) {
                    Log.d("StockOutGoodsDialog", "✅ standardWeight条件满足");
                    if (standardName != null && !standardName.isEmpty() && !"null".equals(standardName) && !"斤".equals(standardName)) {
                        String standardText = "(" + standardWeight + "/" + standardName + ")";
                        tvGoodsStandard.setText(standardText);
                        tvGoodsStandard.setVisibility(View.VISIBLE);
                        Log.d("StockOutGoodsDialog", "✅ 设置商品规格: " + standardText);
                    } else {
                        // 如果standardName为空或为"斤"，只显示weight
                        String standardText = "(" + standardWeight + ")";
                        tvGoodsStandard.setText(standardText);
                        tvGoodsStandard.setVisibility(View.VISIBLE);
                        Log.d("StockOutGoodsDialog", "✅ 设置商品规格: " + standardText);
                    }
                } else {
                    tvGoodsStandard.setVisibility(View.GONE);
                    Log.d("StockOutGoodsDialog", "❌ 商品规格weight为空，隐藏规格显示");
                }
                Log.d("StockOutGoodsDialog", "=== 规格信息调试结束 ===");
            } else {
                Log.d("StockOutGoodsDialog", "❌ tvGoodsStandard为null，无法设置规格信息");
            }
        } else {
            Log.e("StockOutGoodsDialog", "商品实体为空或标题视图为空");
        }

        // 【新增】初始化返回按钮（横屏布局）
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "[返回按钮] 用户点击返回，关闭弹窗");
                dismiss();
            });
            Log.d(TAG, "[返回按钮] 返回按钮绑定成功");
        } else {
            Log.d(TAG, "[返回按钮] 返回按钮不存在（竖屏布局）");
        }

        // 【新增】初始化关闭按钮（竖屏布局）
        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) {
            Log.d(TAG, "[关闭按钮] 找到关闭按钮，开始设置");
            Log.d(TAG, "[关闭按钮] 按钮宽度: " + btnClose.getWidth() + ", 高度: " + btnClose.getHeight());
            Log.d(TAG, "[关闭按钮] 按钮背景: " + btnClose.getBackground());
            Log.d(TAG, "[关闭按钮] 按钮图标: " + btnClose.getDrawable());
            Log.d(TAG, "[关闭按钮] 按钮可见性: " + btnClose.getVisibility());
            Log.d(TAG, "[关闭按钮] 按钮padding: " + btnClose.getPaddingLeft() + "," + btnClose.getPaddingTop() + "," + btnClose.getPaddingRight() + "," + btnClose.getPaddingBottom());
            
            btnClose.setOnClickListener(v -> {
                Log.d(TAG, "[关闭按钮] 用户点击关闭，关闭弹窗");
                dismiss();
            });
            Log.d(TAG, "[关闭按钮] 关闭按钮绑定成功");
            
            // 【新增】在布局完成后重新检查按钮尺寸
            btnClose.post(() -> {
                Log.d(TAG, "[关闭按钮] 布局完成后重新检查");
                Log.d(TAG, "[关闭按钮] 按钮宽度: " + btnClose.getWidth() + ", 高度: " + btnClose.getHeight());
                Log.d(TAG, "[关闭按钮] 按钮布局参数: " + btnClose.getLayoutParams());
                if (btnClose.getLayoutParams() != null) {
                    Log.d(TAG, "[关闭按钮] 布局参数宽度: " + btnClose.getLayoutParams().width + ", 高度: " + btnClose.getLayoutParams().height);
                }
            });
        } else {
            Log.d(TAG, "[关闭按钮] 关闭按钮不存在（横屏布局）");
        }

        // 【新增】根据布局类型设置弹窗样式
        if (getWindow() != null) {
            // 读取横竖屏设置
            SharedPreferences prefs = getContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            int screenOrientation = prefs.getInt("screen_orientation", 0); // 默认竖屏
            
            if (screenOrientation == 1) {
                // 横屏模式：设置全屏，去掉所有边距和背景（与手动输入弹窗保持一致）
                getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
                getWindow().setDimAmount(0f); // 去掉蒙版
                getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 去掉背景
                getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
                                   android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // 允许内容超出状态栏
                Log.d(TAG, "[弹窗] 横屏模式：设置全屏模式完成");
            } else {
                // 竖屏模式：保持原有的弹窗样式，有蒙版和背景
                Log.d(TAG, "[弹窗] 竖屏模式：保持原有弹窗样式");
            }
        }

        // ========== 蓝牙秤状态显示代码已删除（2025-07-08）==========
        // 功能说明：弹窗顶部显示蓝牙秤连接状态
        // 删除原因：用户要求删除弹窗上面的蓝牙秤状态区域
        /*
        // 新增：弹窗顶部显示蓝牙秤状态
        tvScaleStatus = findViewById(R.id.tv_scale_status);
        SharedPreferences sp = mContext.getSharedPreferences("scale_cache", Context.MODE_PRIVATE);
        scaleName = sp.getString("scale_name", null);
        scaleAddress = sp.getString("scale_address", null);
        isScaleConnected = scaleAddress != null;
        StringBuilder scaleInfo = new StringBuilder();
        if (isScaleConnected) {
            scaleInfo.append("蓝牙秤: 已连接");
        } else {
            scaleInfo.append("蓝牙秤: 未连接");
        }
        if (tvScaleStatus != null) {
            tvScaleStatus.setText(scaleInfo.toString());
        }
        */
        // ========== 蓝牙秤状态显示代码删除结束 ==========

        // 初始化订单列表
        ordersRecyclerView = findViewById(R.id.rv_orders);
        if (ordersRecyclerView != null) {
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            
            // 只从 DistributerGoodsEntity 解析订单
            List<NxDepartmentOrdersEntity> orders = null;
            if (goodsEntity.getNxDistributerGoodsEntity() != null) {
                orders = goodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
            }
            Log.d(TAG, "[弹窗] initView: 订单数量=" + (orders != null ? orders.size() : 0));
            
            if (orders != null && !orders.isEmpty()) {
                Log.d(TAG, "[弹窗] 开始清空订单重量");
                for (NxDepartmentOrdersEntity order : orders) {
                    Log.d(TAG, "[弹窗] 清空订单重量: orderId=" + order.getNxDepartmentOrdersId() + ", 原重量=" + order.getNxDoWeight());
                    order.setNxDoWeight("");
                    Log.d(TAG, "[弹窗] 已清空订单重量: orderId=" + order.getNxDepartmentOrdersId() + ", 现重量=" + order.getNxDoWeight());
                }
                Log.d(TAG, "[弹窗] 订单重量清空完毕");
                Log.d(TAG, "[弹窗] 创建订单适配器前: orders hash=" + orders.hashCode());
                ordersAdapter = new StockOutOrdersAdapter((android.app.Activity) mContext, orders, goodsEntity, ordersRecyclerView);
                Log.d(TAG, "[弹窗] 创建订单适配器后: ordersAdapter hash=" + ordersAdapter.hashCode());
                ordersRecyclerView.setAdapter(ordersAdapter);
                Log.d(TAG, "订单适配器设置完成");
                
                // 设置输入框为只读状态，因为重量由蓝牙称自动设置
                Log.d(TAG, "[弹窗] 设置输入框为只读状态");
                for (NxDepartmentOrdersEntity order : orders) {
                    // 这里我们无法直接访问输入框，需要在适配器中设置
                    // 我们将在适配器中添加设置输入框状态的方法
                }
                for (NxDepartmentOrdersEntity order : orders) {
                    Log.d(TAG, "[弹窗] 订单详情: " + new Gson().toJson(order));
                }
            } else {
                Log.d(TAG, "没有订单数据");
                // 显示无订单提示
                TextView tvNoOrders = new TextView(getContext());
                tvNoOrders.setText("暂无订单");
                tvNoOrders.setGravity(Gravity.CENTER);
                tvNoOrders.setPadding(0, 20, 0, 20);
                ((ViewGroup) ordersRecyclerView.getParent()).addView(tvNoOrders);
            }
        }

        // 自动赋值称重到第一个订单 - 每次打开弹窗都重置为0
        if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
            Log.d(TAG, "[弹窗] initView: 重置第一个订单重量为0");
            List<NxDepartmentOrdersEntity> orders = ordersAdapter.getOrders();
            if (orders != null && !orders.isEmpty()) {
                Log.d(TAG, "[弹窗] initView: 直接设置第一个订单重量为0.0, 原重量=" + orders.get(0).getNxDoWeight());
                orders.get(0).setNxDoWeight("0.0");
                Log.d(TAG, "[弹窗] initView: 设置后第一个订单重量=" + orders.get(0).getNxDoWeight());
            }
            ordersAdapter.updateWeightAtPosition(0, 0);
        }
        
        // 标记初始化完成，允许蓝牙秤数据更新
        isInitialized = true;
        Log.d(TAG, "[弹窗] initView: 初始化完成，允许蓝牙秤数据更新");

        // 设置确认按钮
        TextView btnConfirm = findViewById(R.id.btn_confirm);
        if (btnConfirm != null) {
            Log.d(TAG, "[确认按钮] 找到确认按钮，设置点击事件");
            Log.d(TAG, "[确认按钮] 按钮ID: " + R.id.btn_confirm);
            Log.d(TAG, "[确认按钮] 按钮对象: " + btnConfirm);
            btnConfirm.setOnClickListener(v -> {
                Log.d(TAG, "[确认按钮] ========== 点击确认按钮开始 ==========");
                Log.d(TAG, "[确认按钮] 按钮被点击了！");
                Log.d(TAG, "[确认按钮] 当前打印模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
                Log.d(TAG, "[确认按钮] confirmListener: " + (confirmListener != null ? "不为空" : "为空"));
                Log.d(TAG, "[确认按钮] goodsEntity: " + (goodsEntity != null ? "不为空" : "为空"));
                Log.d(TAG, "[确认按钮] ordersAdapter: " + (ordersAdapter != null ? "不为空" : "为空"));
                
                if (confirmListener != null && goodsEntity != null) {
                    // 获取订单数据
                    List<NxDepartmentOrdersEntity> orders = null;
                    if (ordersAdapter != null) {
                        orders = ordersAdapter.getUpdatedOrders();
                        Log.d(TAG, "[确认按钮] 订单数量: " + (orders != null ? orders.size() : 0));
                    } else {
                        Log.e(TAG, "[确认按钮] ordersAdapter为空");
                    }
                    
                    if (goodsEntity.getNxDistributerGoodsEntity() != null) {
                        goodsEntity.getNxDistributerGoodsEntity().setNxDepartmentOrdersEntities(orders);
                    }
                    
                    if (orders != null && !orders.isEmpty()) {
                        Log.d(TAG, "[确认按钮] 开始根据出库模式处理订单");
                        
                        // 显示loading
                        Log.d(TAG, "[确认按钮] 显示loading...");
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).runOnUiThread(() -> {
                                Toast.makeText(mContext, "正在处理...", Toast.LENGTH_SHORT).show();
                            });
                        }
                        
                        // 根据出库模式执行不同的操作
                        onConfirmClick(orders);
                    } else {
                        Log.e(TAG, "[确认按钮] 订单列表为空，无法处理");
                        Toast.makeText(getContext(), "没有可处理的订单", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "[确认按钮] confirmListener或goodsEntity为空，无法处理");
                    if (confirmListener == null) {
                        Log.e(TAG, "[确认按钮] confirmListener为空");
                    }
                    if (goodsEntity == null) {
                        Log.e(TAG, "[确认按钮] goodsEntity为空");
                    }
                }
                Log.d(TAG, "[确认按钮] ========== 点击确认按钮结束 ==========");
            });
        } else {
            Log.e(TAG, "[确认按钮] 未找到确认按钮");
        }

        Log.d(TAG, "[弹窗] initView: 视图初始化完成");
    }

    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (requestCode == REQUEST_SCALE_ACTIVITY && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.hasExtra("weight")) {
                    double weight = data.getDoubleExtra("weight", 0.0);
                Log.d(TAG, "[弹窗] onActivityResult: 获取到蓝牙称重量 weight=" + weight);
                if (ordersAdapter != null) {
                    ordersAdapter.updateWeight(weight);
                    Log.d(TAG, "[弹窗] onActivityResult: 调用ordersAdapter.updateWeight, weight=" + weight);
                } else {
                    Log.d(TAG, "[弹窗] onActivityResult: ordersAdapter为null");
                }
            } else {
                Log.d(TAG, "[弹窗] onActivityResult: data无weight");
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
        Log.d(TAG, "[弹窗] setWeight: weight=" + weight + ", isInitialized=" + isInitialized);
        
        // 如果还没初始化完成，忽略蓝牙秤数据
        if (!isInitialized) {
            Log.d(TAG, "[弹窗] setWeight: 初始化未完成，忽略蓝牙秤数据");
            return;
        }
        
        // 防抖机制：如果重量变化很小（小于0.01斤），跳过更新
        if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
            int selectedPosition = ordersAdapter.getSelectedPosition();
            if (selectedPosition >= 0 && selectedPosition < ordersAdapter.getOrders().size()) {
                NxDepartmentOrdersEntity currentOrder = ordersAdapter.getOrders().get(selectedPosition);
                if (currentOrder != null) {
                    String currentWeightStr = currentOrder.getNxDoWeight();
                    if (currentWeightStr != null && !currentWeightStr.isEmpty()) {
                        try {
                            double currentWeight = Double.parseDouble(currentWeightStr.replaceAll("[^0-9.]", ""));
                            double newWeightInJin = weight / 500.0;
                            double weightDiff = Math.abs(newWeightInJin - currentWeight);
                            
                            if (weightDiff < 0.01) {
                                Log.d(TAG, "[弹窗] setWeight: 重量变化太小，跳过更新: current=" + currentWeight + ", new=" + newWeightInJin + ", diff=" + weightDiff);
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "[弹窗] setWeight: 解析当前重量失败", e);
                        }
                    }
                }
            }
        }
        
        // 确保UI更新在主线程中进行
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(() -> {
                if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
                    // 获取当前选中的位置，而不是硬编码位置0
                    int selectedPosition = ordersAdapter.getSelectedPosition();
                    Log.d(TAG, "[弹窗] 更新选中订单重量: position=" + selectedPosition + ", weight=" + weight);
                    ordersAdapter.updateWeightAtPosition(selectedPosition, weight);
                    // ========== updateScaleStatus调用已删除（2025-07-08）==========
                    // 功能说明：更新蓝牙秤状态显示
                    // 删除原因：用户要求删除弹窗上面的蓝牙秤状态区域
                    // updateScaleStatus(isScaleConnected, scaleName, scaleAddress);
                    // ========== updateScaleStatus调用删除结束 ==========
            } else {
                    Log.e(TAG, "[弹窗] 无法更新重量: ordersAdapter=" + ordersAdapter + ", itemCount=" + (ordersAdapter != null ? ordersAdapter.getItemCount() : 0));
            }
            });
        }
    }

    private void printOrder(NxDepartmentOrdersEntity order) {
        printOrder(order, null);
    }
    
    private void printOrder(NxDepartmentOrdersEntity order, PrintCallback callback) {
        Log.d(TAG, "[printOrder] 被调用, mContext: " + (mContext != null ? mContext.getClass().getName() : "null") + ", orderId=" + order.getNxDepartmentOrdersId());
        Log.d(TAG, "[弹窗] 打印订单: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
        
        // 检查Context类型，避免类型转换异常
        if (mContext instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
            com.swolo.lpy.pysx.main.StockOutActivity activity = (com.swolo.lpy.pysx.main.StockOutActivity) mContext;
            // 直接调用Activity的打印方法，让Activity处理打印结果
            activity.printOrder(order, new com.swolo.lpy.pysx.main.StockOutActivity.PrintCallback() {
                @Override
                public void onPrintSuccess() {
                    Log.d(TAG, "[弹窗] StockOutActivity打印成功");
                    if (callback != null) {
                        callback.onPrintSuccess();
                    }
                }
                @Override
                public void onPrintFail(String error) {
                    Log.e(TAG, "[弹窗] StockOutActivity打印失败: " + error);
                    if (callback != null) {
                        callback.onPrintFail(error);
                    }
                }
            });
        } else if (mContext instanceof com.swolo.lpy.pysx.main.CustomerStockOutActivity) {
            // 客户出库页面打印并保存数据
            Log.d(TAG, "[弹窗] 客户出库页面，开始打印");
            com.swolo.lpy.pysx.main.CustomerStockOutActivity activity = (com.swolo.lpy.pysx.main.CustomerStockOutActivity) mContext;
            // 直接调用Activity的打印方法，让Activity处理打印结果
            activity.printOrder(order, new com.swolo.lpy.pysx.main.CustomerStockOutActivity.PrintCallback() {
                @Override
                public void onPrintSuccess() {
                    Log.d(TAG, "[弹窗] CustomerStockOutActivity打印成功");
                    if (callback != null) {
                        callback.onPrintSuccess();
                    }
                }
                @Override
                public void onPrintFail(String error) {
                    Log.e(TAG, "[弹窗] CustomerStockOutActivity打印失败: " + error);
                    if (callback != null) {
                        callback.onPrintFail(error);
                    }
                }
            });
        } else {
            Log.e(TAG, "[弹窗] Context类型不支持: " + (mContext != null ? mContext.getClass().getSimpleName() : "null"));
            if (callback != null) {
                callback.onPrintFail("不支持的页面类型");
            } else {
                Toast.makeText(mContext, "不支持的页面类型", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestOrderFinish(NxDepartmentOrdersEntity order, Runnable onSuccess) {
        Log.d(TAG, "[弹窗] 请求网络完成订单: orderId=" + order.getNxDepartmentOrdersId());
        // TODO: 实际项目中应调用网络接口，这里直接回调onSuccess模拟
        if (onSuccess != null) onSuccess.run();
    }

    @Override
    public void dismiss() {
        Log.d(TAG, "[日志追踪] StockOutGoodsDialog.dismiss() 被调用");
        
        // 【新增】弹窗关闭时恢复屏幕方向设置
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            // 恢复为自动模式，让系统根据用户设置决定方向
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            Log.d(TAG, "[弹窗关闭] 恢复为自动屏幕方向");
        }
        
        closeGatt();
        Log.d(TAG, "[生命周期] dismiss: 注销广播");
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(weightReceiver);
            Log.d(TAG, "[广播] 注销成功");
        } catch (Exception e) {
            Log.e(TAG, "注销广播异常", e);
        }
        
        // 清理所有引用，确保弹窗对象可以被垃圾回收
        ordersAdapter = null;
        goodsEntity = null;
        confirmListener = null;
        mContext = null;
        bluetoothGatt = null;
        writeCharacteristic = null;
        notifyCharacteristic = null;
        
        super.dismiss();
        Log.d(TAG, "[弹窗] dismiss: 弹窗关闭完成，所有引用已清理");
    }

    // 简化的蓝牙秤连接方法，参考成功项目
    private void connectToScale() {
        Log.d(TAG, "[蓝牙] 开始连接蓝牙秤，检测缓存...");
                SharedPreferences sp = getContext().getSharedPreferences("scale_cache", Context.MODE_PRIVATE);
        scaleAddress = sp.getString("scale_address", null);
        Log.d(TAG, "[蓝牙] 读取缓存 scale_address=" + scaleAddress);
        
        if (scaleAddress != null) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(scaleAddress);
                Log.d(TAG, "[蓝牙] 获取到设备: " + (device != null ? device.getAddress() : "null"));
                
                bluetoothGatt = device.connectGatt(getContext(), false, gattCallback);
                Log.d(TAG, "[蓝牙] 开始连接GATT");
            } else {
                Log.e(TAG, "[蓝牙] 设备不支持蓝牙");
            }
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
    
    // ========== updateScaleStatus方法已删除（2025-07-08）==========
    // 功能说明：更新蓝牙秤状态显示
    // 删除原因：用户要求删除弹窗上面的蓝牙秤状态区域
    /*
    private void updateScaleStatus(boolean connected, String name, String address) {
        if (tvScaleStatus != null) {
            StringBuilder scaleInfo = new StringBuilder();
            if (connected) {
                scaleInfo.append("蓝牙秤: 已连接");
            } else {
                scaleInfo.append("蓝牙秤: 未连接");
            }
            tvScaleStatus.setText(scaleInfo.toString());
        }
    }
    */
    // ========== updateScaleStatus方法删除结束 ==========
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // 简化的重量数据解析，参考成功项目
    private void parseWeightData(byte[] data) {
        Log.d(TAG, "[蓝牙] 开始解析重量数据，长度: " + data.length);
        
        // 尝试多种数据格式
        if (data.length == 7 && data[0] == 0x05) {
            // 格式1: 7字节，首字节0x05
            final int status = data[1];
            final int rawWeight = ((data[3] & 0xFF) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
            final boolean isStable = (status & 0x40) != 0;
            
            Log.d(TAG, "[蓝牙] 格式1解析: status=" + status + ", rawWeight=" + rawWeight + ", isStable=" + isStable);
            
            // 手动去皮：只有在用户主动去皮时才设置皮重
            // 移除自动去皮逻辑，让用户手动控制
            if (!autoTareDone && isStable) {
                // 不自动去皮，保持原始重量
                Log.d(TAG, "[蓝牙] 收到首次稳定重量，不自动去皮: " + rawWeight + "g");
            }
            
            final double realWeight = Math.round((rawWeight - tareWeight) * 10) / 10.0;
            Log.d(TAG, "[蓝牙] 收到重量: " + realWeight + "g, 是否稳定: " + isStable);
            
            // 只有在重量稳定时才更新UI，减少闪烁
            if (isStable) {
                Log.d(TAG, "[蓝牙] 重量稳定，准备写入输入框");
                setWeight(realWeight);
            } else {
                Log.d(TAG, "[蓝牙] 重量不稳定，跳过UI更新");
            }
        } else {
            // 格式2: 尝试解析为简单的重量数据
            Log.d(TAG, "[蓝牙] 尝试格式2解析");
            try {
                // 假设数据是ASCII格式的重量字符串
                String weightStr = new String(data).trim();
                Log.d(TAG, "[蓝牙] ASCII解析: " + weightStr);
                
                // 尝试提取数字
                String numericStr = weightStr.replaceAll("[^0-9.]", "");
                if (!numericStr.isEmpty()) {
                    double weight = Double.parseDouble(numericStr);
                    Log.d(TAG, "[蓝牙] 解析到重量: " + weight + "g");
                    setWeight(weight);
                }
            } catch (Exception e) {
                Log.e(TAG, "[蓝牙] ASCII解析失败", e);
            }
        }
    }

    /**
     * 更新模式状态
     */
    private void updateModeStatus() {
        // 不需要更新模式状态，因为布局已无此控件
    }
    
    /**
     * 检查模式是否与页面一致，如果不一致则通知页面刷新
     */
    // 删除checkAndNotifyModeMismatch方法，因为不再需要模式一致性检查
    
    /**
     * 重新检查设备状态并更新模式显示
     */
    private void updateModeStatusWithRealTimeCheck() {
        Log.d(TAG, "[弹窗] updateModeStatusWithRealTimeCheck: 被调用");
        
        // 简单检查：使用传入的模式，不做复杂判断
        Log.d(TAG, "[设备检查] 使用页面传递的模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
        
        // 更新模式状态显示
        updateModeStatus();
    }
    
    // 删除旧的模式处理方法，因为已经简化为打印模式和非打印模式
    
    /**
     * 验证订单重量数据
     */
    private List<NxDepartmentOrdersEntity> validateOrdersWithWeight(List<NxDepartmentOrdersEntity> orders) {
        List<NxDepartmentOrdersEntity> validOrders = new ArrayList<>();
        for (NxDepartmentOrdersEntity order : orders) {
            String weight = order.getNxDoWeight();
            if (weight != null && !weight.isEmpty()) {
                try {
                    // 移除"斤"字，只保留数字部分
                    String cleanWeight = weight.replace("斤", "").trim();
                    if (!cleanWeight.isEmpty() && Double.parseDouble(cleanWeight) > 0) {
                        validOrders.add(order);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "[验证] 无法解析重量: " + weight);
                }
            }
        }
        Log.d(TAG, "[验证] 有效订单数量: " + validOrders.size() + "/" + orders.size());
        return validOrders;
    }
    
    /**
     * 串行处理订单（双设备模式）
     */
    private void processOrdersSequentially(List<NxDepartmentOrdersEntity> orders, int index) {
        Log.d(TAG, "[日志追踪] ========== processOrdersSequentially 方法开始 ==========");
        Log.d(TAG, "[日志追踪] processOrdersSequentially called, index=" + index + ", orders.size=" + orders.size());
        if (index >= orders.size()) {
            Log.d(TAG, "[日志追踪] 所有订单处理完成，准备关闭弹窗");
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(() -> {
                    Log.d(TAG, "[日志追踪] 所有订单处理完成，主线程Toast");
                    Toast.makeText(mContext, "出库成功，共处理 " + orders.size() + " 个订单", Toast.LENGTH_SHORT).show();
                });
            }
            Log.d(TAG, "[日志追踪] dismiss() 被调用（所有订单处理完成）");
            dismiss();
            return;
        }
        NxDepartmentOrdersEntity order = orders.get(index);
        Log.d(TAG, "[日志追踪] 处理订单 " + (index + 1) + "/" + orders.size() + ": " + order.getNxDepartmentOrdersId());
        printOrder(order, new PrintCallback() {
            @Override
            public void onPrintSuccess() {
                Log.d(TAG, "[日志追踪] 订单 " + order.getNxDepartmentOrdersId() + " 打印成功，开始保存");
                saveOrdersToServer(java.util.Arrays.asList(order), new SaveCallback() {
                    @Override
                    public void onSaveSuccess() {
                        Log.d(TAG, "[日志追踪] 订单 " + order.getNxDepartmentOrdersId() + " 保存成功，继续下一个");
                        processOrdersSequentially(orders, index + 1);
                    }
                    @Override
                    public void onSaveFail(String error) {
                        Log.e(TAG, "[日志追踪] 订单 " + order.getNxDepartmentOrdersId() + " 保存失败: " + error);
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).runOnUiThread(() -> {
                                Log.e(TAG, "[日志追踪] 保存失败Toast");
                                Toast.makeText(mContext, "保存失败: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
            @Override
            public void onPrintFail(String error) {
                Log.e(TAG, "[日志追踪] 订单 " + order.getNxDepartmentOrdersId() + " 打印失败: " + error);
                if (mContext instanceof Activity) {
                    ((Activity) mContext).runOnUiThread(() -> {
                        Log.d(TAG, "[日志追踪] 打印失败，显示错误弹窗");
                        // 显示打印失败弹窗
                        new AlertDialog.Builder(mContext)
                            .setTitle("标签打印失败")
                            .setMessage("打印失败，订单不会被保存。请检查打印机是否开机？\n\n错误信息：" + error)
                            .setPositiveButton("检查打印", (dialog, which) -> {
                                Log.d(TAG, "[日志追踪] 用户点击检查打印按钮");
                                // 刷新打印模式状态
                                if (mContext instanceof com.swolo.lpy.pysx.main.CustomerStockOutActivity) {
                                    ((com.swolo.lpy.pysx.main.CustomerStockOutActivity) mContext).refreshStockOutMode();
                                } else if (mContext instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
                                    // StockOutActivity可能需要类似的刷新方法
                                    Log.d(TAG, "[日志追踪] StockOutActivity暂不支持refreshStockOutMode");
                                }
                            })
                            .setNegativeButton("关闭", (dialog, which) -> {
                                Log.d(TAG, "[日志追踪] 用户点击关闭按钮");
                        dismiss();
                            })
                            .setCancelable(false)
                            .show();
                        
                        // 打印失败时，不调用confirmListener，避免保存订单
                        Log.d(TAG, "[日志追踪] 打印失败，不调用confirmListener，防止订单被保存");
                    });
                }
            }
        });
    }
    
    // 删除showManualPrintDialog方法，因为不再需要手动打印提示
    
    // 删除printOrders方法，因为不再需要批量打印
    
    /**
     * 保存订单到服务器
     */
    private void saveOrdersToServer(List<NxDepartmentOrdersEntity> orders, SaveCallback callback) {
        Log.d(TAG, "[保存] ========== 开始saveOrdersToServer方法 ==========");
        Log.d(TAG, "[保存] 开始保存 " + orders.size() + " 个订单到服务器");
        Log.d(TAG, "[保存] Context类型: " + (mContext != null ? mContext.getClass().getSimpleName() : "null"));
        
        // 打印订单详情
        for (int i = 0; i < orders.size(); i++) {
            NxDepartmentOrdersEntity order = orders.get(i);
            Log.d(TAG, "[保存] 订单" + (i+1) + "详情: orderId=" + order.getNxDepartmentOrdersId() + 
                  ", weight=" + order.getNxDoWeight() + 
                  ", quantity=" + order.getNxDoQuantity() + 
                  ", goodsName=" + (order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
        }
        
        // 根据Context类型调用不同的保存方法
        if (mContext instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
            Log.d(TAG, "[保存] 检测到StockOutActivity，调用stockOutPresenter.giveOrderWeightListForStockAndFinish");
            com.swolo.lpy.pysx.main.StockOutActivity activity = (com.swolo.lpy.pysx.main.StockOutActivity) mContext;
            activity.stockOutPresenter.giveOrderWeightListForStockAndFinish(orders, new com.swolo.lpy.pysx.main.presenter.MainContract.SaveCallback() {
                @Override
                public void onSaveSuccess() {
                    Log.d(TAG, "[保存] StockOutActivity保存成功");
                    callback.onSaveSuccess();
                }
                @Override
                public void onSaveFail(String error) {
                    Log.e(TAG, "[保存] StockOutActivity保存失败: " + error);
                    callback.onSaveFail(error);
                }
            });
        } else if (mContext instanceof com.swolo.lpy.pysx.main.CustomerStockOutActivity) {
            Log.d(TAG, "[保存] 检测到CustomerStockOutActivity，调用activity.saveOrdersToServer");
            com.swolo.lpy.pysx.main.CustomerStockOutActivity activity = (com.swolo.lpy.pysx.main.CustomerStockOutActivity) mContext;
            // 调用CustomerStockOutActivity的保存方法
            Log.d(TAG, "[保存] 开始调用CustomerStockOutActivity.saveOrdersToServer");
            activity.saveOrdersToServer(orders);
            Log.d(TAG, "[保存] CustomerStockOutActivity.saveOrdersToServer调用完成");
            // CustomerStockOutActivity的保存方法会处理回调，这里直接调用成功
            Log.d(TAG, "[保存] 调用callback.onSaveSuccess");
            callback.onSaveSuccess();
        } else {
            Log.e(TAG, "[保存] 不支持的Context类型: " + (mContext != null ? mContext.getClass().getSimpleName() : "null"));
            callback.onSaveFail("不支持的页面类型");
        }
        Log.d(TAG, "[保存] ========== saveOrdersToServer方法完成 ==========");
    }
    
    // 回调接口
    public interface PrintCallback {
        void onPrintSuccess();
        void onPrintFail(String error);
    }
    
    public interface SaveCallback {
        void onSaveSuccess();
        void onSaveFail(String error);
    }

    // 简化的业务逻辑处理
    private void onConfirmClick(List<NxDepartmentOrdersEntity> orders) {
        Log.d(TAG, "[确认按钮] ========== 开始onConfirmClick方法 ==========");
        Log.d(TAG, "[确认按钮] 开始处理订单，当前模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
        Log.d(TAG, "[确认按钮] ordersAdapter: " + (ordersAdapter != null ? "非空" : "null"));
        
        // 获取当前选中的订单
        NxDepartmentOrdersEntity selectedOrder = null;
        if (ordersAdapter != null) {
            selectedOrder = ordersAdapter.getSelectedOrder();
            Log.d(TAG, "[确认按钮] 获取到选中订单: " + (selectedOrder != null ? "orderId=" + selectedOrder.getNxDepartmentOrdersId() : "null"));
        } else {
            Log.e(TAG, "[确认按钮] ordersAdapter为null");
        }
        
        if (selectedOrder == null) {
            Log.e(TAG, "[确认按钮] 没有选中的订单");
            Toast.makeText(getContext(), "请选择一个订单", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证选中订单的重量数据
        String weight = selectedOrder.getNxDoWeight();
        Log.d(TAG, "[确认按钮] 选中订单重量: " + weight);
        if (weight == null || weight.isEmpty() || weight.equals("0.0") || weight.equals("0")) {
            Log.e(TAG, "[确认按钮] 选中订单重量无效: " + weight);
            Toast.makeText(getContext(), "请为选中的订单输入重量", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建只包含选中订单的列表
        List<NxDepartmentOrdersEntity> selectedOrders = new ArrayList<>();
        selectedOrders.add(selectedOrder);
        
        Log.d(TAG, "[确认按钮] 准备处理选中订单: orderId=" + selectedOrder.getNxDepartmentOrdersId() + ", weight=" + weight);
        
        // 简化判断：根据打印模式决定是否打印
        if (isPrintMode) {
            // 打印标签模式：先打印再保存
            Log.d(TAG, "[确认按钮] 打印标签模式，调用handleSingleOrderPrintMode");
            handleSingleOrderPrintMode(selectedOrder);
        } else {
            // 非打印标签模式：直接保存
            Log.d(TAG, "[确认按钮] 非打印标签模式，调用handleSingleOrderSaveMode");
            handleSingleOrderSaveMode(selectedOrder);
        }
        Log.d(TAG, "[确认按钮] ========== onConfirmClick方法完成 ==========");
    }
    
    /**
     * 处理单个订单的打印标签模式
     */
    private void handleSingleOrderPrintMode(NxDepartmentOrdersEntity order) {
        Log.d(TAG, "[单个订单打印模式] ========== 开始handleSingleOrderPrintMode方法 ==========");
        Log.d(TAG, "[单个订单打印模式] 处理订单: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
        
        printOrder(order, new PrintCallback() {
            @Override
            public void onPrintSuccess() {
                Log.d(TAG, "[单个订单打印模式] 订单 " + order.getNxDepartmentOrdersId() + " 打印成功，开始保存");
                saveOrdersToServer(java.util.Arrays.asList(order), new SaveCallback() {
                    @Override
                    public void onSaveSuccess() {
                        Log.d(TAG, "[单个订单打印模式] 订单 " + order.getNxDepartmentOrdersId() + " 保存成功");
                        // 保存成功后，移除订单并刷新
                        handleOrderSavedSuccessfully(order);
                    }
                    @Override
                    public void onSaveFail(String error) {
                        Log.e(TAG, "[单个订单打印模式] 订单 " + order.getNxDepartmentOrdersId() + " 保存失败: " + error);
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).runOnUiThread(() -> {
                                Log.e(TAG, "[单个订单打印模式] 保存失败Toast");
                                Toast.makeText(mContext, "保存失败: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
            @Override
            public void onPrintFail(String error) {
                Log.e(TAG, "[单个订单打印模式] 订单 " + order.getNxDepartmentOrdersId() + " 打印失败: " + error);
                if (mContext instanceof Activity) {
                    ((Activity) mContext).runOnUiThread(() -> {
                        Log.d(TAG, "[单个订单打印模式] 打印失败，显示错误弹窗");
                        // 显示打印失败弹窗
                        new AlertDialog.Builder(mContext)
                            .setTitle("标签打印失败")
                            .setMessage("打印失败，订单不会被保存。请检查打印机是否开机？\n\n错误信息：" + error)
                            .setPositiveButton("检查打印", (dialog, which) -> {
                                Log.d(TAG, "[单个订单打印模式] 用户点击检查打印按钮");
                                // 刷新打印模式状态
                                if (mContext instanceof com.swolo.lpy.pysx.main.CustomerStockOutActivity) {
                                    ((com.swolo.lpy.pysx.main.CustomerStockOutActivity) mContext).refreshStockOutMode();
                                } else if (mContext instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
                                    // StockOutActivity可能需要类似的刷新方法
                                    Log.d(TAG, "[单个订单打印模式] StockOutActivity暂不支持refreshStockOutMode");
                                }
                            })
                            .setNegativeButton("关闭", (dialog, which) -> {
                                Log.d(TAG, "[单个订单打印模式] 用户点击关闭按钮");
                                // 不关闭弹窗，让用户继续处理其他订单
                            })
                            .setCancelable(false)
                            .show();
                    });
                }
            }
        });
        Log.d(TAG, "[单个订单打印模式] ========== handleSingleOrderPrintMode方法完成 ==========");
    }
    
    /**
     * 处理单个订单的非打印模式
     */
    private void handleSingleOrderSaveMode(NxDepartmentOrdersEntity order) {
        Log.d(TAG, "[单个订单保存模式] ========== 开始handleSingleOrderSaveMode方法 ==========");
        Log.d(TAG, "[单个订单保存模式] 处理订单: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
        
        // 调用confirmListener让Activity处理保存
        if (confirmListener != null) {
            Log.d(TAG, "[单个订单保存模式] 调用confirmListener");
            confirmListener.onConfirm(java.util.Arrays.asList(order));
            
            // 由于Activity会处理保存，我们需要在这里也处理订单移除
            // 这里假设Activity保存成功后会调用某个回调，或者我们可以直接处理
            // 为了简化，我们先移除订单，如果Activity保存失败，订单会重新加载
            handleOrderSavedSuccessfully(order);
        } else {
            Log.e(TAG, "[单个订单保存模式] confirmListener为null");
            Toast.makeText(getContext(), "保存失败：未设置保存监听器", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "[单个订单保存模式] ========== handleSingleOrderSaveMode方法完成 ==========");
    }
    
    /**
     * 处理订单保存成功后的逻辑
     */
    private void handleOrderSavedSuccessfully(NxDepartmentOrdersEntity savedOrder) {
        Log.d(TAG, "[订单保存成功] ========== 开始handleOrderSavedSuccessfully方法 ==========");
        Log.d(TAG, "[订单保存成功] 处理已保存订单: orderId=" + savedOrder.getNxDepartmentOrdersId());
        
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(() -> {
                // 从订单列表中移除已保存的订单
                if (ordersAdapter != null) {
                    List<NxDepartmentOrdersEntity> currentOrders = ordersAdapter.getOrders();
                    Log.d(TAG, "[订单保存成功] 当前订单列表大小: " + currentOrders.size());
                    
                    // 移除已保存的订单
                    currentOrders.removeIf(order -> order.getNxDepartmentOrdersId().equals(savedOrder.getNxDepartmentOrdersId()));
                    Log.d(TAG, "[订单保存成功] 移除订单后列表大小: " + currentOrders.size());
                    
                    // 更新适配器
                    ordersAdapter.updateOrders(currentOrders);
                    
                    // 显示保存成功提示
                    Toast.makeText(mContext, "订单保存成功", Toast.LENGTH_SHORT).show();
                    
                    // 刷新页面数据
                    refreshPageData();
                    
                    // 检查是否还有订单
                    if (currentOrders.isEmpty()) {
                        Log.d(TAG, "[订单保存成功] 没有剩余订单，关闭弹窗");
                        dismiss();
                    } else {
                        Log.d(TAG, "[订单保存成功] 还有 " + currentOrders.size() + " 个订单，继续显示弹窗");
                        // 自动选择第一个订单
                        if (ordersAdapter != null) {
                            ordersAdapter.setSelectedPosition(0);
                        }
                    }
                }
            });
        }
        Log.d(TAG, "[订单保存成功] ========== handleOrderSavedSuccessfully方法完成 ==========");
    }
    
    /**
     * 刷新页面数据
     */
    private void refreshPageData() {
        Log.d(TAG, "[刷新页面] ========== 开始refreshPageData方法 ==========");
        
        if (mContext instanceof com.swolo.lpy.pysx.main.CustomerStockOutActivity) {
            Log.d(TAG, "[刷新页面] 刷新CustomerStockOutActivity数据");
            com.swolo.lpy.pysx.main.CustomerStockOutActivity activity = (com.swolo.lpy.pysx.main.CustomerStockOutActivity) mContext;
            activity.refreshData();
        } else if (mContext instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
            Log.d(TAG, "[刷新页面] 刷新StockOutActivity数据");
            com.swolo.lpy.pysx.main.StockOutActivity activity = (com.swolo.lpy.pysx.main.StockOutActivity) mContext;
            // 假设StockOutActivity有refreshData方法，如果没有需要添加
            // activity.refreshData();
        }
        
        Log.d(TAG, "[刷新页面] ========== refreshPageData方法完成 ==========");
    }
    
    /**
     * 【新增】根据用户设置选择布局文件
     */
    private int getLayoutBasedOnOrientation() {
        Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 开始选择布局文件");
        
        try {
            // 读取横竖屏设置
            SharedPreferences prefs = getContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            int screenOrientation = prefs.getInt("screen_orientation", 0); // 默认竖屏
            
            Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 读取到横竖屏设置: " + screenOrientation);
            Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: SharedPreferences文件: settings_prefs");
            Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 键名: screen_orientation");
            
            // 新的索引映射：0=竖屏，1=横屏
            if (screenOrientation >= 2) {
                screenOrientation = 0; // 如果索引超出范围，默认使用竖屏
                Log.w(TAG, "[弹窗] getLayoutBasedOnOrientation: 设置值超出范围，重置为竖屏");
            }
            
            int resultLayout;
            switch (screenOrientation) {
                case 0: // 竖屏
                    resultLayout = R.layout.dialog_stock_out_goods;
                    Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 竖屏模式，使用竖屏布局: " + resultLayout);
                    break;
                case 1: // 横屏
                    resultLayout = R.layout.dialog_stock_out_goods_landscape;
                    Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 横屏模式，使用横屏布局: " + resultLayout);
                    break;
                default:
                    resultLayout = R.layout.dialog_stock_out_goods;
                    Log.w(TAG, "[弹窗] getLayoutBasedOnOrientation: 未知设置，使用竖屏布局: " + resultLayout);
                    break;
            }
            
            Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 最终选择的布局: " + resultLayout);
            return resultLayout;
        } catch (Exception e) {
            Log.e(TAG, "[弹窗] getLayoutBasedOnOrientation: 选择布局文件失败", e);
            int defaultLayout = R.layout.dialog_stock_out_goods;
            Log.d(TAG, "[弹窗] getLayoutBasedOnOrientation: 异常情况下使用默认布局: " + defaultLayout);
            return defaultLayout; // 默认使用竖屏布局
        }
    }

    /**
     * 【新增】应用横竖屏设置
     */
    private void applyScreenOrientation() {
        Log.d(TAG, "[弹窗] applyScreenOrientation: 开始应用横竖屏设置");
        
        try {
            // 读取横竖屏设置
            SharedPreferences prefs = getContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            int screenOrientation = prefs.getInt("screen_orientation", 0);
            
            Log.d(TAG, "[弹窗] applyScreenOrientation: 读取到横竖屏设置: " + screenOrientation);
            
            // 获取Activity
            Activity activity = null;
            if (mContext instanceof Activity) {
                activity = (Activity) mContext;
            }
            
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                switch (screenOrientation) {
                    case 0: // 竖屏
                        Log.d(TAG, "[弹窗] applyScreenOrientation: 设置为竖屏模式");
                        activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case 1: // 横屏
                        Log.d(TAG, "[弹窗] applyScreenOrientation: 设置为横屏模式");
                        activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    default:
                        Log.w(TAG, "[弹窗] applyScreenOrientation: 未知的横竖屏设置: " + screenOrientation + "，使用竖屏");
                        activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                }
            } else {
                Log.e(TAG, "[弹窗] applyScreenOrientation: 无法获取Activity实例或Activity已销毁");
            }
        } catch (Exception e) {
            Log.e(TAG, "[弹窗] applyScreenOrientation: 应用横竖屏设置失败", e);
        }
        
        Log.d(TAG, "[弹窗] applyScreenOrientation: 横竖屏设置应用完成");
    }

}