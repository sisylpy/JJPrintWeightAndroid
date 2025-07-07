package com.swolo.lpy.pysx.main;

import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.app.ProgressDialog;
import android.Manifest;
import android.content.pm.PackageManager;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.dialog.ConnectScaleDialog;
import com.swolo.lpy.pysx.dialog.StockOutGoodsDialog;
import com.swolo.lpy.pysx.main.adapter.StockOutAdapter;
import com.swolo.lpy.pysx.main.adapter.StockOutGoodsAdapter;
import com.swolo.lpy.pysx.main.adapter.StockOutShelfAdapter;
import com.swolo.lpy.pysx.main.gp.Constant;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.presenter.MainContract;
import com.swolo.lpy.pysx.main.presenter.StockOutPresenterImpl;
import com.swolo.lpy.pysx.ui.BaseActivity;
import com.swolo.lpy.pysx.main.gp.ThreadPool;
import com.swolo.lpy.pysx.main.DeviceConnFactoryManager;
import com.swolo.lpy.pysx.main.gp.PrinterCommand;
import com.printer.command.LabelCommand;
import com.printer.command.EscCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.bluetooth.le.BluetoothLeScanner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.app.AlertDialog;

public class StockOutActivity extends BaseActivity implements MainContract.StockOutView {

    private RecyclerView leftMenuRecyclerView, goodsRecyclerView;
    private StockOutShelfAdapter shelfAdapter;
    private StockOutGoodsAdapter goodsAdapter;
    public StockOutPresenterImpl stockOutPresenter;
    private int currentShelfIndex = 0; // 添加当前选中的货架索引
    private List<NxDistributerGoodsShelfEntity> shelfEntities = new ArrayList<>(); // 添加货架实体列表

    // 设置默认值
    private Integer disId;  // 将从用户信息中获取
    private Integer goodsType = -1;
    private static final String TAG = "StockOutActivity";
    private static final int CONN_STATE_DISCONN = 0x007;
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private int id = 0;
    private ThreadPool threadPool;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;
    private ImageButton btnToDep;
    private ImageButton btnSettings;
    private ImageButton btnRight;
    private boolean isPrinterConnected = false;
    private boolean isConnecting = false;
    private Handler printerHandler;
    private static final int PRINTER_CHECK_INTERVAL = 5000; // 5秒检查一次
    private static final int MAX_RECONNECT_ATTEMPTS = 3; // 最大重连次数
    private int reconnectAttempts = 0;
    private SharedPreferences sharedPreferences;

    private LinearLayout nxDepContainer;
    private LinearLayout gbDepContainer;
    private TextView tvNxDepNames;
    private TextView tvGbDepNames;
    private TextView tvNxDepOrders;
    private TextView tvGbDepOrders;
    private ImageButton btnClearNxDep;
    private ImageButton btnClearGbDep;
    private TextView tvNoData;
    private TextView tvPrinterInfo;
    private RecyclerView rvOrders;
    private LinearLayout contentContainer; // 改为 LinearLayout
    private StockOutAdapter adapter;

    private BluetoothAdapter bluetoothAdapter;

    private boolean isFromDepartmentSelection = false;

    private ProgressDialog progressDialog;

    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1002;
    private static final int REQUEST_SCALE_ACTIVITY = 1003;
    private double currentWeight = 0;

    private Dialog currentDialog;

    private TextView tvScaleInfo;
    private boolean isScaleConnected = false;
    private String scaleAddress = null;
    private String scaleName = null;

    private ImageButton btnMore;
    private ImageButton btnCircle;
    
    // 简化的打印模式判断
    private boolean isPrintMode = false; // true=打印标签, false=非打印标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "onCreate开始执行");
            
            // 初始化SharedPreferences
            sharedPreferences = getSharedPreferences("department_cache", MODE_PRIVATE);
            
            // 从Intent获取分销商ID
            disId = getIntent().getIntExtra("distributer_id", -1);
            if (disId == -1) {
                // 如果Intent中没有，尝试从SharedPreferences获取
                SharedPreferences userPrefs = getSharedPreferences("user_info", MODE_PRIVATE);
                disId = userPrefs.getInt("distributer_id", -1);
            }
            
            if (disId == -1) {
                Log.e(TAG, "未找到分销商ID，请先登录");
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                // 跳转到登录页面，并清除任务栈
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            Log.d(TAG, "获取到分销商ID: " + disId);
            
            // 接收客户信息
            String customerName = getIntent().getStringExtra("customer_name");
            int customerOrderCount = getIntent().getIntExtra("customer_order_count", 0);
            int customerUnpickedCount = getIntent().getIntExtra("customer_unpicked_count", 0);
            int departmentId = getIntent().getIntExtra("department_id", -1);
            
            if (customerName != null) {
                Log.d(TAG, "接收到客户信息: " + customerName + ", 订单数: " + customerOrderCount + ", 未拣货数: " + customerUnpickedCount + ", 部门ID: " + departmentId);
                
                // 将选中的部门信息保存到缓存中，用于后续数据加载
                if (departmentId != -1) {
                    SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
                    List<Integer> nxIds = new ArrayList<>();
                    nxIds.add(departmentId);
                    List<String> nxNames = new ArrayList<>();
                    nxNames.add(customerName);
                    
                    sp.edit()
                        .putString("selectedNxIds", new Gson().toJson(nxIds))
                        .putString("selectedNxNames", new Gson().toJson(nxNames))
                        .apply();
                    
                    Log.d(TAG, "已保存部门信息到缓存: " + customerName + " (ID: " + departmentId + ")");
                }
            }
            
            // 初始化蓝牙适配器
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限，无法初始化蓝牙适配器");
                    showToast("缺少蓝牙连接权限");
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                        REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                } else {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
            } else {
                // API 30及以下版本，蓝牙权限在安装时自动授予
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            
            setContentView(getContentViewRes());
            Log.d(TAG, "setContentView执行完成");

            // 设置标题为打印机信息
            tvPrinterInfo = findViewById(R.id.tv_title);
            if (tvPrinterInfo != null) {
                tvPrinterInfo.setTextSize(12);
                tvPrinterInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
                
                // 如果有客户信息，显示客户名称
                if (customerName != null) {
                    tvPrinterInfo.setText("客户: " + customerName + "\n订单: " + customerOrderCount + "个, 未拣货: " + customerUnpickedCount + "个");
                    Log.d(TAG, "设置标题为客户信息: " + customerName);
                } else {
                    updatePrinterInfo(); // 否则显示打印机信息
                }
            }

            // 初始化打印机Handler
            printerHandler = new Handler(Looper.getMainLooper());
            Log.d(TAG, "printerHandler初始化完成");

            // 初始化USB权限请求的PendingIntent
            mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(Constant.ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            Log.d(TAG, "mPermissionIntent初始化完成");

            // 注册广播接收器
            IntentFilter filter = new IntentFilter(Constant.ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE);
            filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            registerReceiver(receiver, filter);
            Log.d(TAG, "广播接收器注册完成");

            // 初始化所有视图
            initView();
            Log.d(TAG, "initView执行完成");

            // 确保stockOutPresenter在initData之前为null
            Log.d(TAG, "stockOutPresenter初始状态: " + (stockOutPresenter == null ? "null" : "已初始化"));

            initData();
            Log.d(TAG, "initData执行完成, stockOutPresenter状态: " + (stockOutPresenter == null ? "null" : "已初始化"));

            // 设置按钮点击事件
            btnMore = findViewById(R.id.btn_more);
            if (btnMore != null) {
                btnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "点击更多按钮，准备跳转到设置页面");
                        try {
                            Intent intent = new Intent(StockOutActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            Log.d(TAG, "成功启动SettingsActivity");
                        } catch (Exception e) {
                            Log.e(TAG, "启动SettingsActivity失败", e);
                            showToast("启动设置页面失败: " + e.getMessage());
                        }
                    }
                });
            }

            btnCircle = findViewById(R.id.btn_circle);
            if (btnCircle != null) {
                btnCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "点击圆形按钮，准备跳转到部门列表页面");
                        try {
                            Intent intent = new Intent(StockOutActivity.this, DepartmentListActivity.class);
                            intent.putExtra("disId", disId);
                            intent.putExtra("goodsType", goodsType);
                            startActivityForResult(intent, 1001);
                        } catch (Exception e) {
                            Log.e(TAG, "启动DepartmentListActivity失败", e);
                            showToast("启动部门列表页面失败: " + e.getMessage());
                        }
                    }
                });
            }

            btnRight = findViewById(R.id.btn_right);
            if (btnRight != null) {
                btnRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "点击设置按钮，准备跳转到设置页面");
                        try {
                            Intent intent = new Intent(StockOutActivity.this, DepartmentListActivity.class);
                            intent.putExtra("disId", disId);
                            intent.putExtra("goodsType", goodsType);
                            startActivityForResult(intent, 1001);
                        } catch (Exception e) {
                            Log.e(TAG, "启动SettingsActivity失败", e);
                            showToast("启动设置页面失败: " + e.getMessage());
                        }
                    }
                });
            }
            tvPrinterInfo = findViewById(R.id.tv_title);
            if (tvPrinterInfo != null) {
                tvPrinterInfo.setTextSize(12);
                tvPrinterInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
                updatePrinterInfo(); // 立即更新打印机信息
            }

            // 添加蓝牙称按钮
//            ImageButton btnScale = findViewById(R.id.btn_scale);
//            if (btnScale != null) {
//                btnScale.setOnClickListener(v -> {
//                    Log.d(TAG, "点击蓝牙称按钮，准备启动ScaleActivity");
//                    try {
//                        // 检查蓝牙权限
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                                    != PackageManager.PERMISSION_GRANTED) {
//                                Log.e(TAG, "缺少蓝牙连接权限");
//                                showToast("缺少蓝牙连接权限");
//                                ActivityCompat.requestPermissions(this,
//                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
//                                    REQUEST_BLUETOOTH_CONNECT_PERMISSION);
//                                return;
//                            }
//                        }
//
//                        // 检查蓝牙是否开启
//                        if (bluetoothAdapter == null) {
//                            Log.e(TAG, "设备不支持蓝牙");
//                            showToast("设备不支持蓝牙");
//                            return;
//                        }
//
//                        if (!bluetoothAdapter.isEnabled()) {
//                            Log.d(TAG, "蓝牙未开启");
//                            showToast("请先开启蓝牙");
//                            return;
//                        }
//
//                        Log.d(TAG, "蓝牙状态正常，启动ScaleActivity");
//                        Intent intent = new Intent(this, ScaleActivity.class);
//                        startActivityForResult(intent, REQUEST_SCALE_ACTIVITY);
//                        Log.d(TAG, "ScaleActivity启动成功");
//                    } catch (Exception e) {
//                        Log.e(TAG, "启动ScaleActivity失败", e);
//                        showToast("启动蓝牙称页面失败: " + e.getMessage());
//                    }
//                });
//            }

            Button btnConnectScale = findViewById(R.id.btn_connect_scale);
            if (btnConnectScale != null) {
                btnConnectScale.setOnClickListener(v -> {
                    showConnectScaleDialog();
                });
            }

            bindAction();
            Log.d(TAG, "bindAction执行完成");
            setView();
            Log.d(TAG, "setView执行完成");

            // 延迟执行自动连接打印机，确保所有初始化完成
            printerHandler.postDelayed(() -> {
                Log.d(TAG, "开始自动连接打印机");
                autoConnectPrinter();
            }, 1000);

            tvScaleInfo = findViewById(R.id.tv_scale_info);
            loadScaleCache();
            updateScaleInfo();
            autoConnectScale();

            if (isBluetoothScaleConnected()) {
                Log.d(TAG, "蓝牙称连接成功");
                if (!isBluetoothPrinterConnected()) {
                    Log.d(TAG, "蓝牙打印机未连接");
                    showPrinterNotConnectedDialog();
                } else {
                    Log.d(TAG, "蓝牙打印机连接成功，准备打印消息");
                    printSuccessMessage();
                }
            }

            Log.d(TAG, "onCreate执行完成");
        } catch (Exception e) {
            Log.e(TAG, "onCreate发生异常", e);
            e.printStackTrace();
            throw new RuntimeException("Activity初始化失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected void initView() {
        Log.d(TAG, "开始初始化视图");
        try {
            // 获取内容容器
            View contentContainer = findViewById(R.id.content_container);
            if (contentContainer == null) {
                throw new RuntimeException("找不到内容容器");
            }

            // 加载出库布局
            View stockOutLayout = getLayoutInflater().inflate(R.layout.activity_stock_out, (ViewGroup) contentContainer, false);
            ((ViewGroup) contentContainer).addView(stockOutLayout);

            // 从出库布局中查找视图
            leftMenuRecyclerView = stockOutLayout.findViewById(R.id.left_menu);
            goodsRecyclerView = stockOutLayout.findViewById(R.id.rv_stock_out);

            // 设置RecyclerView的布局管理器
            leftMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            goodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // 初始化适配器
            shelfAdapter = new StockOutShelfAdapter();
            goodsAdapter = new StockOutGoodsAdapter();

            // 设置适配器
            leftMenuRecyclerView.setAdapter(shelfAdapter);
            goodsRecyclerView.setAdapter(goodsAdapter);

            // 查找其他视图
            nxDepContainer = stockOutLayout.findViewById(R.id.nx_dep_container);
            gbDepContainer = stockOutLayout.findViewById(R.id.gb_dep_container);
            tvNxDepNames = stockOutLayout.findViewById(R.id.tv_nx_dep_names);
            tvGbDepNames = stockOutLayout.findViewById(R.id.tv_gb_dep_names);
            btnClearNxDep = stockOutLayout.findViewById(R.id.btn_clear_nx_dep);
            btnClearGbDep = stockOutLayout.findViewById(R.id.btn_clear_gb_dep);
            tvNoData = stockOutLayout.findViewById(R.id.tv_no_data);
            contentContainer = stockOutLayout.findViewById(R.id.swipe_refresh_layout);

            Log.d(TAG, "视图初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "initView初始化失败", e);
            throw new RuntimeException("视图初始化失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected void initData() {
        Log.d(TAG, "开始初始化数据");
        try {
            // 初始化Presenter
            if (stockOutPresenter == null) {
                stockOutPresenter = new StockOutPresenterImpl(this);
                Log.d(TAG, "StockOutPresenter初始化完成");
            }

            // 初始化打印机
            if (printerHandler == null) {
                printerHandler = new Handler(Looper.getMainLooper());
                Log.d(TAG, "PrinterHandler初始化完成");
            }

            Log.d(TAG, "数据初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "数据初始化失败", e);
            Toast.makeText(this, "数据初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void bindAction() {
        Log.d(TAG, "开始绑定事件");
        shelfAdapter.setOnItemClickListener(new StockOutShelfAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NxDistributerGoodsShelfEntity entity) {
                // 保存当前选中的货架索引
                currentShelfIndex = shelfAdapter.getSelectedPosition();
                Log.d(TAG, "货架点击，保存索引: " + currentShelfIndex + ", 货架名称: " + entity.getNxDistributerGoodsShelfName());
                // 显示对应货架下的商品
                goodsAdapter.setGoodsList(entity.getNxDisGoodsShelfGoodsEntities());
            }
        });

        // 设置商品点击事件
        goodsAdapter.setOnItemClickListener(new StockOutGoodsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NxDistributerGoodsShelfGoodsEntity entity) {
                Log.d(TAG, "商品点击: " + entity.getNxDistributerGoodsEntity().getNxDgGoodsName());
                showStockOutDialog(entity);
            }
        });

        // 设置商品适配器的订单确认监听器
        goodsAdapter.setOnOrderConfirmListener(orderList -> {
            Log.d(TAG, "收到订单确认，开始打印和保存");
            if (orderList != null && !orderList.isEmpty()) {
                printAndSaveOrders(orderList, 0);
            }
        });

        btnClearNxDep.setOnClickListener(v -> {
            clearNxDepartmentSelection();
            loadData();
        });

        btnClearGbDep.setOnClickListener(v -> {
            clearGbDepartmentSelection();
            loadData();
        });
        Log.d(TAG, "事件绑定完成");
    }

    @Override
    protected void setView() {
        // 加载数据
//        loadData();
    }

    private void loadData() {
        Log.d(TAG, "开始加载数据");
        try {
            // 确保数据已初始化
            if (stockOutPresenter == null) {
                Log.d(TAG, "stockOutPresenter为空，重新初始化");
                initData();
            }

            // 只从Intent获取goodsType
            goodsType = getIntent().getIntExtra("goodsType", -1);
            Log.d(TAG, "加载数据参数: disId=" + disId + ", goodsType=" + goodsType);

            // 检查是否有缓存的部门信息
            SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
            String nxIdsJson = sp.getString("selectedNxIds", "[]");
            String gbIdsJson = sp.getString("selectedGbIds", "[]");
            
            List<Integer> nxIds = new Gson().fromJson(nxIdsJson, new TypeToken<List<Integer>>(){}.getType());
            List<Integer> gbIds = new Gson().fromJson(gbIdsJson, new TypeToken<List<Integer>>(){}.getType());
            
            Log.d(TAG, "缓存的部门信息: nxIds=" + nxIds + ", gbIds=" + gbIds);

            // 根据是否有部门信息决定调用哪个方法
            if ((nxIds != null && !nxIds.isEmpty()) || (gbIds != null && !gbIds.isEmpty())) {
                Log.d(TAG, "有部门信息，调用getStockGoodsWithDepIds");
                stockOutPresenter.getStockGoodsWithDepIds(this, 0, 0, disId, goodsType);
            } else {
                Log.d(TAG, "没有部门信息，调用getStockGoods");
                stockOutPresenter.getStockGoods(disId, goodsType);
            }
        } catch (Exception e) {
            Log.e(TAG, "加载数据失败", e);
            Toast.makeText(this, "加载数据失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void _initDataKf() {
        try {
            Log.d(TAG, "开始调用_initDataKf获取数据");
            if (stockOutPresenter == null) {
                Log.e(TAG, "stockOutPresenter为null，重新初始化");
                stockOutPresenter = new StockOutPresenterImpl(this);
            }
            stockOutPresenter.getStockGoods(disId, goodsType);
        } catch (Exception e) {
            Log.e(TAG, "调用_initDataKf时出错: " + e.getMessage(), e);
            showToast("获取数据失败: " + e.getMessage());
        }
    }

    private void _initNxDataKf() {
        try {
            Log.d(TAG, "开始调用_initNxDataKf获取数据");
            if (stockOutPresenter == null) {
                Log.e(TAG, "stockOutPresenter为null，重新初始化");
                stockOutPresenter = new StockOutPresenterImpl(this);
            }
            // 从缓存中获取选中的部门ID
            SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
            String nxIdsJson = sp.getString("selectedNxIds", "[]");
            String gbIdsJson = sp.getString("selectedGbIds", "[]");

            List<Integer> outNxDepIds = new Gson().fromJson(nxIdsJson, new TypeToken<List<Integer>>(){}.getType());
            List<Integer> outGbDepIds = new Gson().fromJson(gbIdsJson, new TypeToken<List<Integer>>(){}.getType());

            Log.d(TAG, "选中的部门ID: nxDepIds=" + outNxDepIds + ", gbDepIds=" + outGbDepIds);

            // 调用API获取数据，直接传入所有选中的部门ID
            stockOutPresenter.getStockGoodsWithDepIds(this, 0, 0, disId, -1);
            
        } catch (Exception e) {
            Log.e(TAG, "调用_initNxDataKf时出错: " + e.getMessage(), e);
            showToast("获取内销部门数据失败: " + e.getMessage());
        }
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.layout_base; // 使用基础布局
    }

    // 请求数据成功时调用
    @Override
    public void getStockGoodsSuccess(List<NxDistributerGoodsShelfEntity> result) {
        Log.d(TAG, "获取到商品数据，货架数量: " + result.size());
            
            // 保存当前选中的货架索引
            int oldShelfIndex = currentShelfIndex;
            
            // 更新数据
        shelfEntities.clear();
        shelfEntities.addAll(result);
        
        // 检查当前选中的货架索引是否有效
        if (currentShelfIndex >= shelfEntities.size()) {
                currentShelfIndex = 0;
            Log.d(TAG, "当前货架索引无效，重置为0");
        }
        
        // 获取当前货架的商品列表
        List<NxDistributerGoodsShelfGoodsEntity> currentGoods = 
            shelfEntities.get(currentShelfIndex).getNxDisGoodsShelfGoodsEntities();
            
        Log.d(TAG, "当前货架商品数量: " + (currentGoods != null ? currentGoods.size() : 0));
        
        // 检查每个商品的订单数据
        if (currentGoods != null) {
            for (NxDistributerGoodsShelfGoodsEntity goods : currentGoods) {
                Log.d(TAG, "商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName() + 
                      ", 订单数量aaaa: " + goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size());
                // 详细记录订单数据
                if (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null) {
                    for (NxDepartmentOrdersEntity order : goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities()) {
                        Log.d(TAG, "订单详情vbv: " +
                            "部门=" + (order.getNxDepartmentEntity() != null ? 
                                order.getNxDepartmentEntity().getNxDepartmentName() : "null") +
                            ", 数量=" + order.getNxDoQuantity() +
                            ", 单位=" + order.getNxDoStandard());
                    }
                }
            }
        }
        
        // 更新UI
        updateShelfTabs();
        updateGoodsList();
        
        // 如果货架索引发生变化，更新选中状态
        if (oldShelfIndex != currentShelfIndex) {
            updateShelfSelection();
        }
    }

    // 请求数据失败时调用
    @Override
    public void getStockGoodsFail(String error) {
        stopLoading();
        showToast("加载失败: " + error);
    }

    @Override
    public void onStockOutFinishSuccess() {
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        // 可在此处刷新数据等
    }

    @Override
    public void onStockOutFinishFail(String error) {
        Toast.makeText(this, "保存失败: " + error, Toast.LENGTH_SHORT).show();
    }

    // 1. 定义打印回调接口
    public interface PrintCallback {
        void onPrintSuccess();
        void onPrintFail(String error);
    }

    // 2. 串行打印和保存
    public void printAndSaveOrders(List<NxDepartmentOrdersEntity> orderList, int index) {
        if (index >= orderList.size()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "订单打印并保存成功", Toast.LENGTH_SHORT).show();
                // 保存当前选中的货架索引
                int savedIndex = currentShelfIndex;
                Log.d(TAG, "打印完成，保存当前索引: " + savedIndex);
                // 刷新数据
                loadData();
                // 恢复索引
                currentShelfIndex = savedIndex;
                Log.d(TAG, "恢复保存的索引: " + currentShelfIndex);
                // 确保适配器也更新选中位置
                if (shelfAdapter != null) {
                    shelfAdapter.setSelectedPosition(savedIndex);
                }
            });
            return;
        }
        NxDepartmentOrdersEntity order = orderList.get(index);
        printOrder(order, new PrintCallback() {
            @Override
            public void onPrintSuccess() {
                // 打印成功后保存
                stockOutPresenter.giveOrderWeightListForStockAndFinish(
                    Collections.singletonList(order),
                    new MainContract.SaveCallback() {
                        @Override
                        public void onSaveSuccess() {
                            // 保存成功，继续处理下一个订单
                            printAndSaveOrders(orderList, index + 1);
                        }
                        @Override
                        public void onSaveFail(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(StockOutActivity.this, "保存失败: " + error, Toast.LENGTH_SHORT).show();
                                // 保存失败，停止处理
                            });
                        }
                    }
                );
            }
            @Override
            public void onPrintFail(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StockOutActivity.this, "打印失败: " + error + "，订单未保存", Toast.LENGTH_SHORT).show();
                    // 打印失败，停止处理
                });
            }
        });
    }

    // 判断是否为模拟器
    private boolean isEmulator() {
        String fingerprint = android.os.Build.FINGERPRINT;
        String model = android.os.Build.MODEL;
        return fingerprint.contains("generic") ||
                fingerprint.startsWith("unknown") ||
                model.contains("google_sdk") ||
                model.contains("Emulator") ||
                model.contains("Android SDK built for x86");
    }

    // 4. 打印实现（只打印商品名称和出库数量）
    public void printOrder(NxDepartmentOrdersEntity order, PrintCallback callback) {
        Log.d(TAG, "【打印】开始打印订单");
        Log.d(TAG, "【打印】订单信息: " + 
            "商品=" + (order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null") +
            ", 数量=" + order.getNxDoQuantity() +
            ", 重量=" + order.getNxDoWeight() +
            ", 单位=" + order.getNxDoStandard());

        if (isEmulator()) {
            Log.d(TAG, "【打印】模拟器环境，跳过打印");
            callback.onPrintFail("模拟器环境，跳过打印");
            return;
        }

        // 检查打印机状态
        if (!checkPrinterStatus()) {
            Log.e(TAG, "【打印】打印机未就绪");
            callback.onPrintFail("打印机未就绪，请检查连接");
            return;
        }

        ThreadPool.getInstantiation().addTask(() -> {
            try {
                DeviceConnFactoryManager manager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0];
                if (manager == null) {
                    Log.e(TAG, "【打印】打印机管理器为空");
                    callback.onPrintFail("打印机管理器未初始化");
                    return;
                }

                PrinterCommand commandType = manager.getCurrentPrinterCommand();
                Log.d(TAG, "【打印】打印机指令类型: " + commandType);
                
                if (commandType == PrinterCommand.ESC) {
                    Log.d(TAG, "【打印】使用ESC指令打印");
                    printWithESC(order, callback);
                } else if (commandType == PrinterCommand.TSC) {
                    Log.d(TAG, "【打印】使用TSC指令打印");
                    printWithTSC(order, callback);
                } else {
                    Log.e(TAG, "【打印】不支持的打印机指令集: " + commandType);
                    callback.onPrintFail("不支持的打印机指令集");
                }
            } catch (Exception e) {
                Log.e(TAG, "【打印】打印异常", e);
                handlePrintError(e, callback);
            }
        });
    }

    /**
     * 检查打印机状态（严谨版）
     * 1. 先判断连接管理器和连接状态
     * 2. 再尝试发送一条初始化命令（ESC/TSC），捕获异常，确保打印机真正可用
     * 注意：不修改、删除、屏蔽任何打印机和蓝牙的其它业务代码
     */
    private boolean checkPrinterStatus() {
        DeviceConnFactoryManager[] managers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
        if (managers == null || managers.length == 0) return false;
        DeviceConnFactoryManager manager = managers[0];
        if (manager == null) return false;

        // 1. 检查连接状态
        boolean connState = manager.getConnState();
        if (!connState) {
            connectPrinter();
            return false;
        }

        // 2. 尝试发送一条初始化命令，确保打印机真正ready
        try {
            PrinterCommand commandType = manager.getCurrentPrinterCommand();
            Vector<Byte> testData = new Vector<>();
            if (commandType == PrinterCommand.ESC) {
                // ESC打印机：发送初始化命令
                EscCommand esc = new EscCommand();
                esc.addInitializePrinter();
                testData = esc.getCommand();
            } else if (commandType == PrinterCommand.TSC) {
                // TSC打印机：发送清除命令
                LabelCommand tsc = new LabelCommand();
                tsc.addCls();
                testData = tsc.getCommand();
            } else {
                // 其它类型暂不支持
                return false;
            }
            if (testData != null && !testData.isEmpty()) {
                manager.sendDataImmediately(testData);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // 捕获异常，说明打印机不可用
            return false;
        }
    }

    private void printWithESC(NxDepartmentOrdersEntity order, PrintCallback callback) throws Exception {
        Log.d(TAG, "【打印】开始ESC打印");
        try {
            EscCommand esc = new EscCommand();
            esc.addInitializePrinter();
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            
            // 添加标题
            String departmentName = getDepartmentName(order);
            Log.d(TAG, "【打印】部门名称: " + departmentName);
            esc.addText(departmentName + "\n");
            esc.addText("----------------\n");
            
            // 商品信息
            String goodsName = order.getNxDistributerGoodsEntity() != null ? 
                order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "";
            String quantity = order.getNxDoQuantity() != null ? order.getNxDoQuantity().toString() : "0";
            Log.d(TAG, "【打印】商品信息: " + goodsName + ", 数量: " + quantity);
            esc.addText("商品: " + goodsName + "\n");
            esc.addText("数量: " + quantity + "\n");
            
            // 出库重量
            String weight = order.getNxDoWeight() != null ? order.getNxDoWeight() : "0";
            String standard = order.getNxDoStandard() != null ? order.getNxDoStandard() : "";
            Log.d(TAG, "【打印】出库信息: " + weight + standard);
            esc.addText("出库: " + weight + standard + "\n");
            esc.addText("----------------\n");
            
            esc.addCutPaper();
            Vector<Byte> datas = esc.getCommand();
            Log.d(TAG, "【打印】ESC命令数据大小: " + (datas != null ? datas.size() : 0));
            
            sendPrintData(datas, callback);
        } catch (Exception e) {
            Log.e(TAG, "【打印】ESC打印异常", e);
            throw e;
        }
    }

    private void printWithTSC(NxDepartmentOrdersEntity order, PrintCallback callback) throws Exception {
        Log.d(TAG, "开始使用TSC指令打印");
        try {
            LabelCommand tsc = new LabelCommand();
            Log.d(TAG, "创建LabelCommand对象成功");
            
            // 设置标签参数
            tsc.addSize(50, 80);
            tsc.addGap(10);
            tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
            tsc.addReference(0, 0);
            tsc.addCls();
            Log.d(TAG, "设置标签参数成功");
            
            // 部门名称
            String departmentName = getDepartmentName(order);
            Log.d(TAG, "部门名称: " + departmentName);
            tsc.addText(50, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
                LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, 
                LabelCommand.FONTMUL.MUL_2, departmentName);
            
            // 商品信息
            String goodsName = order.getNxDistributerGoodsEntity() != null ? 
                order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "";
            String standardName = order.getNxDistributerGoodsEntity() != null ? 
                order.getNxDistributerGoodsEntity().getNxDgGoodsStandardname() : "";
            String weight = order.getNxDoWeight() != null ? order.getNxDoWeight() : "0";
            String standard = order.getNxDoStandard() != null ? order.getNxDoStandard() : "";
            Log.d(TAG, "商品信息: " + goodsName + ", 规格: " + standardName + ", 数量: " + weight);
            
            tsc.addText(120, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
                LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, 
                LabelCommand.FONTMUL.MUL_2, goodsName + " " + weight + standard);
            
            // 订货信息
            String orderQuantity = order.getNxDoQuantity() != null ? order.getNxDoQuantity().toString() : "0";
            String orderStandard = order.getNxDoStandard() != null ? order.getNxDoStandard() : "";
            Log.d(TAG, "订货数量: " + orderQuantity);
            tsc.addText(190, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
                LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, 
                LabelCommand.FONTMUL.MUL_2, "订货: " + orderQuantity + orderStandard);
            
            // 备注
            String remark = order.getNxDoRemark();
            if (remark != null && !remark.isEmpty()) {
                Log.d(TAG, "备注: " + remark);
                tsc.addText(260, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
                    LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_1, 
                    LabelCommand.FONTMUL.MUL_1, "备注: " + remark);
            }
            
            tsc.addPrint(1, 1);
            tsc.addSound(2, 100);
            tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
            
            Log.d(TAG, "获取打印命令数据");
            Vector<Byte> datas = tsc.getCommand();
            if (datas == null || datas.isEmpty()) {
                throw new Exception("生成的打印命令数据为空");
            }
            Log.d(TAG, "打印命令数据大小: " + datas.size());
            
            sendPrintData(datas, callback);
        } catch (Exception e) {
            Log.e(TAG, "生成TSC打印命令失败", e);
            throw new Exception("生成TSC打印命令失败: " + e.getMessage());
        }
    }

    private String getDepartmentName(NxDepartmentOrdersEntity order) {
        if (order != null) {
            if(order.getNxDepartmentEntity() != null){

                NxDepartmentEntity department = order.getNxDepartmentEntity();
                if (department.getFatherDepartmentEntity() != null) {
                    return String.format("(%s)%s.%s",
                            department.getFatherDepartmentEntity().getNxDepartmentPickName(),
                            department.getFatherDepartmentEntity().getNxDepartmentName(),
                            department.getNxDepartmentName());
                } else {
                    return String.format("(%s)%s",
                            department.getNxDepartmentPickName(),
                            department.getNxDepartmentName());
                }

            }else if(order.getGbDepartmentEntity() != null){
                GbDepartmentEntity department = order.getGbDepartmentEntity();
                if (department.getFatherGbDepartmentEntity() != null &&
                        department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                   return String.format("(%s)%s.%s",
                            department.getFatherGbDepartmentEntity().getGbDepartmentAttrName(),
                            department.getFatherGbDepartmentEntity().getGbDepartmentName(),
                            department.getGbDepartmentName());
                } else {
                    return  String.format("(%s)%s",
                            department.getGbDepartmentAttrName(),
                            department.getGbDepartmentName());
                }
            }
        }

        return "";
    }

    private void sendPrintData(Vector<Byte> datas, PrintCallback callback) throws Exception {
        Log.d(TAG, "【打印】开始发送打印数据");
        try {
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] == null) {
                Log.e(TAG, "【打印】打印机管理器未初始化");
                throw new Exception("打印机管理器未初始化");
            }
            
            if (!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState()) {
                Log.e(TAG, "【打印】打印机未连接");
                throw new Exception("打印机未连接");
            }
            
            Log.d(TAG, "【打印】发送打印数据到打印机，数据大小: " + (datas != null ? datas.size() : 0));
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].sendDataImmediately(datas);
            Log.d(TAG, "【打印】打印数据发送成功");
            
            callback.onPrintSuccess();
            Log.d(TAG, "【打印】打印回调成功");
            
            runOnUiThread(() -> {
                loadData();
                Toast.makeText(this, "打印成功", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "【打印】发送打印数据失败", e);
            throw new Exception("发送打印数据失败: " + e.getMessage());
        }
    }

    private void handlePrintError(Exception e, PrintCallback callback) {
        updatePrinterStatus(false);
        callback.onPrintFail("打印异常: " + e.getMessage());
        
        runOnUiThread(() -> {
            connectPrinter();
            showToast("打印失败，正在尝试重新连接打印机");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter(Constant.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(receiver, filter);
        
        // 尝试自动连接USB打印机
        autoConnectPrinter();
    }

    private void connectPrinter() {
        if (isConnecting) {
            Log.d(TAG, "正在连接中，忽略重复连接请求");
            return;
        }

        isConnecting = true;
        reconnectAttempts++;
        
        try {
            SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            String printerType = sp.getString("printer_type", null);
            String printerAddress = sp.getString("printer_address", null);

            if (printerType == null || printerAddress == null) {
                Log.d(TAG, "没有保存的打印机信息");
                updatePrinterStatus(false);
                return;
            }

            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
            }

            if ("usb".equals(printerType)) {
                Log.d(TAG, "尝试连接USB打印机");
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                boolean found = false;
                for (UsbDevice device : deviceList.values()) {
                    if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                        found = true;
                        usbConn(device);
                        // 立即更新显示
                        updatePrinterInfo();
                        
                        // 延迟检查USB连接状态
                        printerHandler.postDelayed(() -> {
                            boolean isConnected = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null && 
                                                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState();
                            updatePrinterStatus(isConnected);
                            if (!isConnected) {
                                Log.d(TAG, "USB打印机连接失败");
                                showToast("USB打印机连接失败");
                            }
                        }, 1000);
                        break;
                    }
                }
                if (!found) {
                    Log.d(TAG, "未找到USB打印机");
                    updatePrinterStatus(false);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "连接打印机失败", e);
            updatePrinterStatus(false);
            showToast("连接打印机失败: " + e.getMessage());
        } finally {
            isConnecting = false;
        }
    }

    // USB 连接方法
    private void usbConn(UsbDevice usbDevice) {
        try {
            Log.d(TAG, "【打印机】开始连接USB打印机: " + usbDevice.getDeviceName());
            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                Log.d(TAG, "【打印机】关闭之前的连接");
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
            }
            
            Log.d(TAG, "【打印机】创建新的打印机连接");
            new DeviceConnFactoryManager.Build()
                    .setId(id)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                    .setUsbDevice(usbDevice)
                    .setContext(this)
                    .build();
            
            Log.d(TAG, "【打印机】尝试打开端口");
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
            
            // 检查连接状态
            boolean isConnected = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState();
            Log.d(TAG, "【打印机】USB打印机连接状态: " + (isConnected ? "成功" : "失败"));
            
            if (isConnected) {
                // 保存打印机信息
                SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                sp.edit()
                    .putString("printer_type", "usb")
                    .putString("printer_address", usbDevice.getDeviceName())
                    .apply();
                    
                Log.d(TAG, "【打印机】USB打印机连接成功");
                isPrinterConnected = true;
                showToast("USB打印机连接成功");
            } else {
                Log.e(TAG, "【打印机】USB打印机连接失败");
                showToast("USB打印机连接失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "【打印机】USB打印机连接异常: " + e.getMessage(), e);
            showToast("USB打印机连接异常: " + e.getMessage());
        } finally {
            updatePrinterInfo();
        }
    }

    private void updatePrinterInfo() {
        try {
            if (tvPrinterInfo == null) {
                Log.e(TAG, "tvPrinterInfo为null，尝试重新初始化");
                tvPrinterInfo = findViewById(R.id.tv_title);
                if (tvPrinterInfo == null) {
                    Log.e(TAG, "无法找到tvPrinterInfo");
                    return;
                }
            }
            
            StringBuilder info = new StringBuilder();
            info.append("打印机状态: ");
            
            DeviceConnFactoryManager deviceManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0];
            if (deviceManager != null) {
                boolean isConnected = deviceManager.getConnState();
                info.append(isConnected ? "已连接" : "未连接").append("\n");
                
                // 获取连接方式
                String connMethod = "未知";
                if (deviceManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.USB) {
                    connMethod = "USB";
                } else if (deviceManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH) {
                    connMethod = "蓝牙";
                }
                info.append("连接方式: ").append(connMethod).append("\n");
                
                // 获取设备信息
                if (deviceManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.USB) {
                    SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                    String printerAddress = sp.getString("printer_address", null);
                    if (printerAddress != null) {
                        info.append("设备地址: ").append(printerAddress).append("\n");
                    }
                } else if (deviceManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH) {
                    SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                    String printerAddress = sp.getString("printer_address", null);
                    if (printerAddress != null) {
                        info.append("蓝牙地址: ").append(printerAddress).append("\n");
                    }
                }
                
                // 获取打印机类型
                String printerType = "未知";
                PrinterCommand command = deviceManager.getCurrentPrinterCommand();
                if (command != null) {
                    switch (command) {
                        case ESC:
                            printerType = "ESC指令";
                            break;
                        case TSC:
                            printerType = "TSC指令";
                            break;
                    }
                }
                info.append("打印机类型: ").append(printerType);
            } else {
                info.append("未初始化");
            }
            
            tvPrinterInfo.setText(info.toString());
            Log.d(TAG, "打印机信息更新完成: " + info.toString());
        } catch (Exception e) {
            Log.e(TAG, "更新打印机信息失败", e);
            if (tvPrinterInfo != null) {
                tvPrinterInfo.setText("打印机信息更新失败");
            }
        }
    }

    private void updateDepartmentDisplay() {
        try {
            Log.d(TAG, "开始更新部门显示");
            
            // 从SharedPreferences读取部门名称
            String nxDepNamesJson = sharedPreferences.getString("selectedNxNames", "[]");
            String gbDepNamesJson = sharedPreferences.getString("selectedGbNames", "[]");
            
            Log.d(TAG, "读取到的内销部门名称: " + nxDepNamesJson);
            Log.d(TAG, "读取到的国标部门名称: " + gbDepNamesJson);
            
            // 解析JSON字符串
            List<String> nxDepNames = new Gson().fromJson(nxDepNamesJson, new TypeToken<List<String>>(){}.getType());
            List<String> gbDepNames = new Gson().fromJson(gbDepNamesJson, new TypeToken<List<String>>(){}.getType());
            
            // 检查视图是否已初始化
            if (nxDepContainer == null || gbDepContainer == null) {
                Log.e(TAG, "部门容器视图未初始化");
                nxDepContainer = findViewById(R.id.nx_dep_container);
                gbDepContainer = findViewById(R.id.gb_dep_container);
                if (nxDepContainer == null || gbDepContainer == null) {
                    Log.e(TAG, "重新初始化部门容器视图失败");
                    return;
                }
            }
            
            // 更新内销部门显示
            if (nxDepNames != null && !nxDepNames.isEmpty()) {
                String nxNamesText = String.join(", ", nxDepNames);
                Log.d(TAG, "设置内销部门名称: " + nxNamesText);
                nxDepContainer.setVisibility(View.VISIBLE);
                tvNxDepNames.setText(nxNamesText);
            } else {
                nxDepContainer.setVisibility(View.GONE);
            }
            
            // 更新国标部门显示
            if (gbDepNames != null && !gbDepNames.isEmpty()) {
                String gbNamesText = String.join(", ", gbDepNames);
                Log.d(TAG, "设置国标部门名称: " + gbNamesText);
                gbDepContainer.setVisibility(View.VISIBLE);
                tvGbDepNames.setText(gbNamesText);
            } else {
                gbDepContainer.setVisibility(View.GONE);
            }
            
            Log.d(TAG, "部门显示更新完成");
            
        } catch (Exception e) {
            Log.e(TAG, "更新部门显示时出错: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "更新部门显示失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearNxDepartmentSelection() {
        SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("selectedNxIds");
        editor.remove("selectedNxNames");
        editor.apply();
        updateDepartmentDisplay();
    }

    private void clearGbDepartmentSelection() {
        SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("selectedGbIds");
        editor.remove("selectedGbNames");
        editor.apply();
        updateDepartmentDisplay();
    }

    // 广播接收器
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "收到广播: " + action);
            
            try {
                switch (action) {
                    case Constant.ACTION_USB_PERMISSION:
                        synchronized (this) {
                            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (device != null) {
                                    usbConn(device);
                                }
                            } else {
                                Log.d(TAG, "USB权限被拒绝");
                                showToast("USB权限被拒绝");
                            }
                        }
                        break;

                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        isPrinterConnected = false;
                        mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                        break;

                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        if (!isPrinterConnected) {
                            autoConnectPrinter();
                        }
                        break;

                    case DeviceConnFactoryManager.ACTION_CONN_STATE:
                        int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                        int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                        Log.d(TAG, "打印机连接状态变化: state=" + state + ", deviceId=" + deviceId);
                        
                        if (id == deviceId) {
                            switch (state) {
                                case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                                    isPrinterConnected = false;
                                    showToast("打印机已断开连接");
                                    break;
                                case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                                    showToast("正在连接打印机...");
                                    break;
                                case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                                    isPrinterConnected = true;
                                    showToast("打印机已连接");
                                    break;
                                case DeviceConnFactoryManager.CONN_STATE_FAILED:
                                    isPrinterConnected = false;
                                    showToast("打印机连接失败");
                                    break;
                            }
                            updatePrinterInfo();
                        }
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "处理广播消息时发生异常", e);
            }
        }
    };

    // Handler 处理消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN:
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                        showToast("打印机已断开连接");
                    }
                    break;
                case PRINTER_COMMAND_ERROR:
                    showToast("打印机指令错误");
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: 页面停止");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 页面销毁");
        DeviceConnFactoryManager.closeAllPort();
        if (threadPool != null) {
            threadPool.stopThreadPool();
            threadPool = null;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "【回调】onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + (data != null));
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            Log.d(TAG, "从部门选择页面返回，更新部门显示");
            isFromDepartmentSelection = true;  // 标记是从部门选择返回
            try {
                // 确保视图已初始化
                if (nxDepContainer == null || gbDepContainer == null) {
                    Log.d(TAG, "重新初始化部门容器视图");
                    nxDepContainer = findViewById(R.id.nx_dep_container);
                    gbDepContainer = findViewById(R.id.gb_dep_container);
                }
                // 更新部门显示
                updateDepartmentDisplay();
                // 重新加载数据
                loadData();
                Log.d(TAG, "部门显示和数据更新完成");
            } catch (Exception e) {
                Log.e(TAG, "更新部门显示失败", e);
                showToast("更新部门显示失败: " + e.getMessage());
            }
        } else if (requestCode == REQUEST_SCALE_ACTIVITY && resultCode == RESULT_OK) {
            Log.e(TAG, "【蓝牙称】onActivityResult收到蓝牙称连接成功回调");
            if (data != null && data.hasExtra("weight")) {
                double weight = data.getDoubleExtra("weight", 0.0);
                Log.e(TAG, "【蓝牙称】收到重量: " + weight);
                // 如果当前显示的是商品对话框，将重量传递给对话框
                if (currentDialog != null && currentDialog instanceof StockOutGoodsDialog) {
                    Log.e(TAG, "【蓝牙称】将重量传递给StockOutGoodsDialog");
                    ((StockOutGoodsDialog) currentDialog).onActivityResult(requestCode, resultCode, data);
                }
            } else {
                Log.e(TAG, "【蓝牙称】data为null或没有weight字段");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 页面显示，刷新数据");
        Log.d(TAG, "onResume: 当前Activity状态 - isFinishing=" + isFinishing() + ", isDestroyed=");
        
        try {
            // 确保printerHandler已初始化
            if (printerHandler == null) {
                Log.d(TAG, "printerHandler未初始化，重新初始化");
                printerHandler = new Handler(Looper.getMainLooper());
            }
            
            // 更新部门显示
            updateDepartmentDisplay();
            
            // 如果不是从部门选择返回，才执行数据加载
            if (!isFromDepartmentSelection) {
                Log.d(TAG, "onResume: 不是从部门选择返回，执行数据加载");
                loadData();
            } else {
                Log.d(TAG, "onResume: 从部门选择返回，跳过数据加载");
            }
            
            // 重置重连次数
            reconnectAttempts = 0;
            
            // 延迟启动打印机监控，避免立即连接
            printerHandler.postDelayed(() -> {
                // 尝试连接打印机（根据缓存类型自动选择连接方式）
                autoConnectPrinter();
                startPrinterMonitor();
            }, 1000);
            
            loadScaleCache();
            autoConnectScale();
            updateScaleInfo();
            
            Log.d(TAG, "onResume执行完成");
        } catch (Exception e) {
            Log.e(TAG, "onResume发生异常", e);
            showToast("页面恢复失败: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 页面暂停");
        isFromDepartmentSelection = false;  // 重置标记
        if (printerHandler != null) {
            printerHandler.removeCallbacksAndMessages(null);
        }
    }

    private void autoConnectPrinter() {
        Log.d(TAG, "开始自动连接打印机");
        try {
            // 从缓存中获取打印机信息
            SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            String printerType = sp.getString("printer_type", null);
            String printerAddress = sp.getString("printer_address", null);

            if (printerType == null || printerAddress == null) {
                Log.d(TAG, "没有保存的打印机信息，开始搜索可用打印机");
                searchAndConnectPrinter();
                return;
            }

            // 优先尝试缓存的连接方式
            if ("usb".equals(printerType)) {
                Log.d(TAG, "尝试使用缓存的USB打印机连接");
                boolean connected = tryConnectCachedUsbPrinter();
                if (!connected) {
                    Log.d(TAG, "缓存的USB打印机连接失败，开始搜索其他打印机");
                    searchAndConnectPrinter();
                }
            } else if ("bluetooth".equals(printerType)) {
                Log.d(TAG, "尝试使用缓存的蓝牙打印机连接");
                autoConnectBluetoothPrinter(printerAddress);
            } else {
                Log.e(TAG, "不支持的打印机类型: " + printerType);
                searchAndConnectPrinter();
            }
        } catch (Exception e) {
            Log.e(TAG, "自动连接打印机失败", e);
            showToast("自动连接打印机失败: " + e.getMessage());
            searchAndConnectPrinter();
        }
    }

    private boolean tryConnectCachedUsbPrinter() {
        Log.d(TAG, "尝试连接缓存的USB打印机");
        try {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            if (usbManager == null) {
                Log.e(TAG, "无法获取USB服务");
                return false;
            }

            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            if (deviceList == null || deviceList.isEmpty()) {
                Log.d(TAG, "未检测到USB设备");
                return false;
            }

            // 查找匹配的USB设备
            for (UsbDevice device : deviceList.values()) {
                if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                    Log.d(TAG, "找到匹配的USB打印机");
                    if (usbManager.hasPermission(device)) {
                        Log.d(TAG, "已有USB权限，开始连接");
                        usbConn(device);
                        // 检查连接状态
                        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null && 
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                            Log.d(TAG, "缓存的USB打印机连接成功");
                            return true;
                        }
                    } else {
                        Log.d(TAG, "请求USB权限");
                        try {
                            usbManager.requestPermission(device, mPermissionIntent);
                            Log.d(TAG, "USB权限请求已发送");
                        } catch (Exception e) {
                            Log.e(TAG, "请求USB权限失败", e);
                            showToast("请求USB权限失败: " + e.getMessage());
                        }
                    }
                    break;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "连接缓存的USB打印机失败", e);
            return false;
        }
    }

    private void searchAndConnectPrinter() {
        Log.d(TAG, "【打印机】开始搜索可用打印机");
        try {
            // 首先尝试USB打印机
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            if (usbManager != null) {
                Log.d(TAG, "【打印机】获取到USB服务");
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                if (deviceList != null && !deviceList.isEmpty()) {
                    Log.d(TAG, "【打印机】发现USB设备数量: " + deviceList.size());
                    for (UsbDevice device : deviceList.values()) {
                        Log.d(TAG, "【打印机】检查USB设备: vendorId=" + device.getVendorId() + 
                            ", productId=" + device.getProductId() + 
                            ", deviceName=" + device.getDeviceName());
                        if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                            Log.d(TAG, "【打印机】找到匹配的USB打印机");
                            if (usbManager.hasPermission(device)) {
                                Log.d(TAG, "【打印机】已有USB权限，开始连接");
                                usbConn(device);
                                return;
                            } else {
                                Log.d(TAG, "【打印机】请求USB权限");
                                usbManager.requestPermission(device, mPermissionIntent);
                                return;
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "【打印机】未检测到USB设备");
                }
            } else {
                Log.e(TAG, "【打印机】无法获取USB服务");
            }

            // 如果没有找到USB打印机，更新打印机状态
            Log.d(TAG, "【打印机】未找到USB打印机");
            updatePrinterStatus(false);
            showToast("未找到可用打印机");
        } catch (Exception e) {
            Log.e(TAG, "【打印机】搜索打印机失败", e);
            showToast("搜索打印机失败: " + e.getMessage());
        }
    }

    private void autoConnectBluetoothPrinter(String address) {
        Log.d(TAG, "开始连接蓝牙打印机: " + address);
        try {
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限");
                    showToast("缺少蓝牙连接权限");
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                        REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                    return;
                }
            }

            // 检查蓝牙是否开启
            if (bluetoothAdapter == null) {
                Log.e(TAG, "设备不支持蓝牙");
                showToast("设备不支持蓝牙");
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "蓝牙未开启");
                showToast("请先开启蓝牙");
                return;
            }

            // 获取蓝牙设备
            BluetoothDevice device;
            try {
                device = bluetoothAdapter.getRemoteDevice(address);
                Log.d(TAG, "获取到蓝牙设备: " + device.getName());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "无效的蓝牙地址: " + address);
                showToast("无效的蓝牙地址");
                return;
            }

            if (device == null) {
                Log.e(TAG, "未找到蓝牙打印机设备");
                showToast("未找到蓝牙打印机设备");
                return;
            }

            // 检查设备是否已配对
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "设备未配对，尝试配对");
                try {
                    // 检查API版本，createBond需要API 19+
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        device.createBond();
                        // 等待配对完成
                        Thread.sleep(2000);
                    } else {
                        Log.w(TAG, "当前API版本不支持createBond，跳过配对");
                        showToast("当前系统版本不支持自动配对，请手动配对设备");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "设备配对失败", e);
                    showToast("设备配对失败");
                    return;
                }
            }

            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
            }

            // 建立新的蓝牙连接
            Log.d(TAG, "开始建立蓝牙连接");
            new DeviceConnFactoryManager.Build()
                    .setId(0)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                    .setMacAddress(address)
                    .setContext(this)
                    .build();

            // 打开端口
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();
            
            // 检查连接状态
            boolean connected = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState();
            Log.d(TAG, "蓝牙连接结果: " + (connected ? "成功" : "失败"));

            if (connected) {
                Log.d(TAG, "蓝牙打印机连接成功");
                isPrinterConnected = true;
                showToast("蓝牙打印机连接成功");
                
                // 保存打印机信息
                SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                sp.edit()
                    .putString("printer_type", "bluetooth")
                    .putString("printer_address", address)
                    .apply();
            } else {
                Log.e(TAG, "蓝牙打印机连接失败");
                showToast("蓝牙打印机连接失败");
                isPrinterConnected = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "连接蓝牙打印机失败", e);
            showToast("连接蓝牙打印机失败: " + e.getMessage());
            isPrinterConnected = false;
        } finally {
            updatePrinterInfo();
        }
    }

    private void startPrinterMonitor() {
        printerHandler.postDelayed(() -> {
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                boolean currentState = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState();
                if (currentState != isPrinterConnected) {
                    Log.d(TAG, "打印机状态变化: " + (currentState ? "已连接" : "已断开"));
                    updatePrinterStatus(currentState);
                }
            } else if (isPrinterConnected) {
                // 如果管理器为null但状态显示已连接，更新状态
                updatePrinterStatus(false);
            }
            startPrinterMonitor();
        }, PRINTER_CHECK_INTERVAL);
    }

    private void updatePrinterStatus(boolean connected) {
        if (isPrinterConnected == connected) {
            return; // 状态没有变化，不需要更新
        }
        
        isPrinterConnected = connected;
        if (connected) {
            reconnectAttempts = 0; // 连接成功，重置重连次数
        }
        
        runOnUiThread(() -> {
            updatePrinterInfo();
            if (connected) {
                showToast("打印机已连接");
            } else {
                showToast("打印机已断开");
            }
        });
    }

    @Override
    public void stopLoading() {
        // 不再需要处理下拉刷新状态
    }

    @Override
    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载中...");
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "蓝牙连接权限已授予");
                // 重新尝试连接打印机
                SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                String printerType = sp.getString("printer_type", null);
                String printerAddress = sp.getString("printer_address", null);
                if ("bluetooth".equals(printerType) && printerAddress != null) {
                    autoConnectBluetoothPrinter(printerAddress);
                }
            } else {
                Log.e(TAG, "蓝牙连接权限被拒绝");
                showToast("需要蓝牙连接权限才能使用打印机");
            }
        }
    }

    private void showStockOutDialog(NxDistributerGoodsShelfGoodsEntity goods) {
        try {
            Log.d(TAG, "显示出库对话框，商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName());
            Log.d(TAG, "订单数据: " + (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
                "数量=" + goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : "null"));
            
            // 强制关闭之前的弹窗并清理缓存
            if (currentDialog != null) {
                currentDialog.dismiss();
                currentDialog = null;
                Log.d(TAG, "[弹窗创建] 已清理之前的弹窗缓存");
            }
            Log.d(TAG, "[弹窗创建] 开始创建新的StockOutGoodsDialog");
            StockOutGoodsDialog dialog = new StockOutGoodsDialog(this, goods, isPrintMode);
            Log.d(TAG, "[弹窗创建] StockOutGoodsDialog创建完成");
            dialog.setOnConfirmListener(orders -> {
                // 处理确认事件
                if (orders != null && !orders.isEmpty()) {
                    // === 新增：同步订单重量到主页面数据源 ===
                    for (NxDepartmentOrdersEntity updatedOrder : orders) {
                        for (NxDistributerGoodsShelfEntity shelf : shelfEntities) {
                            for (NxDistributerGoodsShelfGoodsEntity g : shelf.getNxDisGoodsShelfGoodsEntities()) {
                                List<NxDepartmentOrdersEntity> orderList = g.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
                                for (NxDepartmentOrdersEntity order : orderList) {
                                    if (order.getNxDepartmentOrdersId().equals(updatedOrder.getNxDepartmentOrdersId())) {
                                        Log.d(TAG, "同步订单重量: orderId=" + order.getNxDepartmentOrdersId() + ", oldWeight=" + order.getNxDoWeight() + ", newWeight=" + updatedOrder.getNxDoWeight());
                                        order.setNxDoWeight(updatedOrder.getNxDoWeight());
                                    }
                                }
                            }
                        }
                    }
                    // 刷新主页面适配器
                    updateGoodsList();
                    goodsAdapter.notifyDataSetChanged();
                    // === 原有打印和保存逻辑 ===
                    printAndSaveOrders(orders, 0);
                }
                // 清理弹窗缓存
                currentDialog = null;
                Log.d(TAG, "[弹窗创建] 确认后清理弹窗缓存");
            });
            // 设置弹窗关闭监听器，确保清理缓存
            dialog.setOnDismissListener(dialogInterface -> {
                currentDialog = null;
                Log.d(TAG, "[弹窗创建] 弹窗关闭，清理缓存");
            });
            currentDialog = dialog;
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "[弹窗创建] 创建弹窗异常: " + e.getMessage(), e);
            Toast.makeText(this, "创建弹窗失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateShelfTabs() {
        if (shelfAdapter != null) {
            shelfAdapter.setData(shelfEntities);
            shelfAdapter.setSelectedPosition(currentShelfIndex);
        }
    }

    private void updateGoodsList() {
        if (shelfEntities != null && !shelfEntities.isEmpty() && currentShelfIndex < shelfEntities.size()) {
            List<NxDistributerGoodsShelfGoodsEntity> goodsList = 
                shelfEntities.get(currentShelfIndex).getNxDisGoodsShelfGoodsEntities();
            if (goodsAdapter != null) {
                goodsAdapter.setGoodsList(goodsList);
            }
        }
    }

    private void updateShelfSelection() {
        if (shelfAdapter != null) {
            shelfAdapter.setSelectedPosition(currentShelfIndex);
        }
    }

    private void loadScaleCache() {
        SharedPreferences sp = getSharedPreferences("scale_cache", MODE_PRIVATE);
        scaleAddress = sp.getString("scale_address", null);
        scaleName = sp.getString("scale_name", null);
        isScaleConnected = scaleAddress != null;
        Log.d(TAG, "[蓝牙秤] loadScaleCache: scaleAddress=" + scaleAddress + ", scaleName=" + scaleName + ", isScaleConnected=" + isScaleConnected);
    }

    private void updateScaleInfo() {
        Log.d(TAG, "[蓝牙秤] updateScaleInfo: isScaleConnected=" + isScaleConnected + ", scaleAddress=" + scaleAddress + ", scaleName=" + scaleName);
        if (tvScaleInfo != null) {
            if (isScaleConnected) {
                tvScaleInfo.setText("蓝牙秤: 已连接\n名称: " + (scaleName != null ? scaleName : "") + "\n地址: " + scaleAddress);
                tvScaleInfo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvScaleInfo.setText("蓝牙秤: 未连接");
                tvScaleInfo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }

    private void autoConnectScale() {
        Log.d(TAG, "[蓝牙秤] autoConnectScale: scaleAddress=" + scaleAddress + ", bluetoothAdapter=" + bluetoothAdapter + ", isEnabled=" + (bluetoothAdapter != null && bluetoothAdapter.isEnabled()));
        if (scaleAddress == null) return;
        
        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "[蓝牙秤] 缺少蓝牙连接权限，无法自动连接");
                isScaleConnected = false;
                updateScaleInfo();
                return;
            }
        }
        
        if (bluetoothAdapter == null) {
            // 检查权限后获取蓝牙适配器
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "[蓝牙秤] 缺少蓝牙连接权限，无法获取适配器");
                    isScaleConnected = false;
                    updateScaleInfo();
                    return;
                }
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            isScaleConnected = false;
            updateScaleInfo();
            return;
        }
        
        try {
            // 检查权限后获取远程设备
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "[蓝牙秤] 缺少蓝牙连接权限，无法获取远程设备");
                    isScaleConnected = false;
                    updateScaleInfo();
                    return;
                }
            }
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(scaleAddress);
            if (device != null) {
                isScaleConnected = true;
            } else {
                isScaleConnected = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "[蓝牙秤] autoConnectScale异常: " + e.getMessage());
            isScaleConnected = false;
        }
        updateScaleInfo();
    }

    private void showConnectScaleDialog() {
        Log.e(TAG, "【用户操作】点击了连接蓝牙称按钮，弹出连接蓝牙称对话框");
        ConnectScaleDialog dialog = new ConnectScaleDialog(this);
        dialog.setOnConnectListener((address, name) -> {
            Log.e(TAG, "【回调】蓝牙秤连接成功，address=" + address + ", name=" + name);
            loadScaleCache();
            updateScaleInfo();
            // 自动打印
            if (!isBluetoothPrinterConnected()) {
                Log.e(TAG, "【打印】蓝牙打印机未连接，无法自动打印");
                showPrinterNotConnectedDialog();
            } else {
                Log.e(TAG, "【打印】蓝牙打印机已连接，自动打印成功消息");
                printSuccessMessage();
            }
        });
        dialog.show();
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isBluetoothScaleConnected() {
        // TODO: Implement logic to check if Bluetooth scale is connected
        Log.d(TAG, "检查蓝牙称连接状态");
        return false; // Placeholder
    }

    private boolean isBluetoothPrinterConnected() {
        Log.d(TAG, "检查蓝牙打印机连接状态");
        try {
            DeviceConnFactoryManager manager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0];
            if (manager != null
                    && manager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH
                    && manager.getConnState()) {
                Log.d(TAG, "蓝牙打印机已连接");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "检查蓝牙打印机连接状态异常", e);
        }
        Log.d(TAG, "蓝牙打印机未连接");
        return false;
    }

    private void printSuccessMessage() {
        Log.d(TAG, "尝试打印成功消息");
        try {
            DeviceConnFactoryManager manager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0];
            if (manager == null || !manager.getConnState()) {
                Log.e(TAG, "打印机未连接，无法打印");
                showToast("打印机未连接，无法打印");
                return;
            }
            PrinterCommand commandType = manager.getCurrentPrinterCommand();
            Vector<Byte> datas = new Vector<>();
            if (commandType == PrinterCommand.ESC) {
                EscCommand esc = new EscCommand();
                esc.addInitializePrinter();
                esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
                esc.addText("蓝牙秤连接成功\n");
                esc.addCutPaper();
                datas = esc.getCommand();
            } else if (commandType == PrinterCommand.TSC) {
                LabelCommand tsc = new LabelCommand();
                tsc.addSize(50, 30);
                tsc.addGap(2);
                tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
                tsc.addReference(0, 0);
                tsc.addCls();
                tsc.addText(20, 20, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                        LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, "蓝牙秤连接成功");
                tsc.addPrint(1, 1);
                datas = tsc.getCommand();
            } else {
                Log.e(TAG, "不支持的打印机指令集");
                showToast("不支持的打印机指令集");
                return;
            }
            manager.sendDataImmediately(datas);
            showToast("已发送打印命令");
        } catch (Exception e) {
            Log.e(TAG, "打印成功消息异常", e);
            showToast("打印失败: " + e.getMessage());
        }
    }

    private void showPrinterNotConnectedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("打印机未连接");
        builder.setMessage("请确保打印机已连接并开启。");
        builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}

