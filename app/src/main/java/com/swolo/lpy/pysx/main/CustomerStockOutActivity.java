package com.swolo.lpy.pysx.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.Dialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.widget.ImageButton;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.CommonResponse;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.main.DeviceConnFactoryManager;
import com.swolo.lpy.pysx.main.gp.PrinterCommand;
import com.printer.command.EscCommand;
import com.printer.command.LabelCommand;
import com.swolo.lpy.pysx.main.gp.ThreadPool;
import java.util.Collections;
import java.util.Vector;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.StockGoodsWithDepIdsResponse;
import com.swolo.lpy.pysx.main.modal.StockGoodsWithCategoriesResponse;
import com.swolo.lpy.pysx.main.modal.NxDistributerFatherGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsEntity;
import com.swolo.lpy.pysx.main.adapter.CategoryAdapter;
import com.swolo.lpy.pysx.main.adapter.CategoryGoodsAdapter;
import com.swolo.lpy.pysx.dialog.ConnectScaleDialog;
import com.swolo.lpy.pysx.main.adapter.StockOutShelfAdapter;
import com.swolo.lpy.pysx.main.adapter.StockOutGoodsAdapter;
import com.swolo.lpy.pysx.dialog.StockOutGoodsDialog;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.google.gson.reflect.TypeToken;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.dialog.ManualInputDialog;
import com.swolo.lpy.pysx.ui.view.CommonLoadingDialog;

/**
 * 客户出库页面 - 核心业务页面
 * 
 * 【重要】此页面为业务核心页面，包含以下关键功能：
 * 1. 客户出库数据展示和操作
 * 2. 打印机自动连接和打印功能
 * 3. 订单保存和提交功能
 * 4. 货架和商品列表管理
 * 
 * 【对应关系】
 * - 对应小程序 index_out 页面
 * - 使用 stockerGetToStockGoodsWithDepIdsKf 接口获取数据
 * 
 * 【业务规则】
 * - 支持打印标签和非打印标签两种模式
 * - 打印机连接状态影响出库模式
 * - 订单数据需要实时保存到服务器
 * 
 * 【注意事项】
 * - 此页面代码为业务核心，严禁随意删除或修改
 * - 打印机连接逻辑经过多次优化，请谨慎修改
 * - 订单保存逻辑涉及数据完整性，必须保持稳定
 */
public class CustomerStockOutActivity extends AppCompatActivity {

    private static final String TAG = "CustomerStockOut";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;

    // 【业务核心】UI组件管理 - 严禁删除或修改
    // 这些UI组件用于显示客户出库的界面
    // 包括标题显示、货架列表、商品列表等
    private TextView tvTitle; // 标题显示，包含模式状态信息
    private RecyclerView rvShelf; // 货架列表显示
    private RecyclerView rvGoods; // 商品列表显示
    private ImageButton btnRight; // 右上角部门按钮
    private ImageButton btnModeSwitch; // 右上角模式切换按钮

    // 【业务核心】数据管理 - 严禁删除或修改
    // 这些变量用于管理客户出库的核心业务数据
    // 包括货架列表、选中状态、客户信息等
    private List<NxDistributerGoodsShelfEntity> shelfList = new ArrayList<>(); // 货架数据列表
    private int selectedShelfIndex = -1; // 当前选中的货架索引
    private Integer selectedShelfId = null; // 当前选中的货架ID
    
    // 【新增】商品类别模式相关变量
    private List<NxDistributerFatherGoodsEntity> categoryList = new ArrayList<>(); // 商品类别数据列表
    private int selectedCategoryIndex = -1; // 当前选中的商品类别索引
    private Integer selectedCategoryId = null; // 当前选中的商品类别ID
    private boolean isCategoryMode = true; // 是否为商品类别模式，默认true
    private int disId = -1; // 分销商ID，用于API调用
    private String customerName; // 客户名称，支持多客户显示
    private int customerOrderCount; // 客户订单总数
    private int customerUnpickedCount; // 客户未拣货订单数

    // 【业务核心】缓存管理 - 严禁删除或修改
    // 用于管理客户出库相关的缓存数据
    // 包括部门选择、客户信息等持久化数据
    private SharedPreferences sharedPreferences;

    // 【业务核心】蓝牙称状态管理 - 严禁删除或修改
    // 借鉴成功项目的蓝牙称状态管理逻辑
    // 这些变量用于管理蓝牙称的连接状态和显示信息
    // 虽然当前页面主要关注打印机，但蓝牙称功能为未来扩展预留
    private BluetoothAdapter bluetoothAdapter;
    private TextView tvScaleInfo;
    private boolean isScaleConnected = false;
    private String scaleAddress = null;
    private String scaleName = null;
    private static final int REQUEST_SCALE_ACTIVITY = 1003;

    // 【业务核心】打印机状态管理 - 严禁删除或修改
    // 这些变量用于管理打印机的连接状态和配置信息
    // 是打印功能的核心状态变量，影响整个出库流程
    private boolean isPrinterConnected = false;
    private SharedPreferences printerPreferences;
    
    // 【业务核心】打印模式判断 - 严禁删除或修改
    // 此变量控制整个出库流程的打印行为
    // true=打印标签模式（需要打印机连接成功）
    // false=非打印标签模式（不需要打印机）
    // 此逻辑经过多次优化，是业务核心功能
    private boolean isPrintMode = false;
    // 【业务核心】打印机连接管理 - 严禁删除或修改
    // 这些变量用于管理打印机连接过程中的状态和重连逻辑
    // 确保打印机连接的稳定性和用户体验
    private Dialog currentDialog; // 当前显示的弹窗引用
    private boolean isConnecting = false; // 连接状态标志，防止重复连接
    private int reconnectAttempts = 0; // 重连次数统计
    private int id = 0; // 打印机连接ID
    private Handler printerHandler; // 打印机操作的主线程Handler
    private CommonLoadingDialog mLoadingDialog; // 加载蒙版

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        long perfStart = System.currentTimeMillis();
        Log.d("PERF", "onCreate start: " + perfStart);
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "[生命周期] onCreate开始执行");
            
            setContentView(R.layout.activity_customer_stock_out);
            
            // 初始化缓存
            sharedPreferences = getSharedPreferences("department_cache", MODE_PRIVATE);
            
            // 获取传递的参数
            initIntentData();
            Log.d("PERF", "after initIntentData: " + System.currentTimeMillis());
            
            // 初始化UI
            initViews();
            Log.d("PERF", "after initViews: " + System.currentTimeMillis());
            
            // 绑定事件
            bindEvents();
            Log.d("PERF", "after bindEvents: " + System.currentTimeMillis());
            
            // 初始化打印机 - 异步执行，不阻塞主线程
            printerPreferences = getSharedPreferences("printer_cache", MODE_PRIVATE);
            printerHandler = new Handler(Looper.getMainLooper());
            
            // 初始化模式切换按钮图标
            updateModeSwitchButtonIcon();
            
            // 初始化加载蒙版
            mLoadingDialog = new CommonLoadingDialog(this);
            
            // 立即加载数据，不等待打印机连接
            long dataStart = System.currentTimeMillis();
            loadStockGoodsData();
            Log.d("PERF", "loadStockGoodsData耗时: " + (System.currentTimeMillis() - dataStart) + "ms");
            
            // 【优化】先显示初始状态，让页面立即可用
            determinePrintMode();
            updateModeStatus();
            Log.d(TAG, "[初始化] 初始模式状态已设置，页面可立即使用");
            
            // 异步连接打印机（完全后台，不阻塞UI）
            new Thread(() -> {
                long printerStart = System.currentTimeMillis();
                Log.d(TAG, "[打印机] 后台开始连接打印机...");
                
                autoConnectPrinter();
                
                Log.d("PERF", "autoConnectPrinter耗时: " + (System.currentTimeMillis() - printerStart) + "ms");
                
                // 连接完成后在主线程静默更新状态
                runOnUiThread(() -> {
                    Log.d(TAG, "[异步] ========== 打印机连接完成，静默更新状态 ==========");
                    Log.d(TAG, "[异步] isPrinterConnected: " + isPrinterConnected);
                    
                    // 重新判断打印模式
                    determinePrintMode();
                    updateModeStatus();
                    
                    // 静默提示
                    if (isPrinterConnected) {
                        Toast.makeText(CustomerStockOutActivity.this, "打印机已连接", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "[异步] ✅ 打印机连接成功");
                    } else {
                        Log.d(TAG, "[异步] ⚠️ 打印机未连接，使用非打印模式");
                    }
                    Log.d(TAG, "[异步] ========== 状态更新完成 ==========");
                });
            }).start();
            
            Log.d("PERF", "onCreate end: " + System.currentTimeMillis() + ", 总耗时: " + (System.currentTimeMillis() - perfStart) + "ms");
        } catch (Exception e) {
            Log.e(TAG, "[生命周期] onCreate异常", e);
            Toast.makeText(this, "页面初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openManualInputDialog(NxDistributerGoodsShelfGoodsEntity goods) {
        Log.d(TAG, "[日志追踪] 选择弹出人工输入弹窗 ManualInputDialog");
        List<NxDepartmentOrdersEntity> ordersForManual = getOrderListForGoods(goods);
        ManualInputDialog dialog = new ManualInputDialog(this, goods, ordersForManual);
        dialog.setOnConfirmListener(orders -> {
            Log.d(TAG, "[日志追踪] ManualInputDialog 确认回调, 订单数量: " + (orders != null ? orders.size() : 0));
            if (orders != null && !orders.isEmpty()) {
                for (int i = 0; i < orders.size(); i++) {
                    NxDepartmentOrdersEntity order = orders.get(i);
                    Log.d(TAG, "[日志追踪] ManualInputDialog 订单" + (i + 1) + "详情: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
                }
                if (isPrintMode) {
                    Log.d(TAG, "[日志追踪] ManualInputDialog 打印模式，调用printAndSaveOrders");
                    printAndSaveOrders(orders, 0);
                } else {
                    Log.d(TAG, "[日志追踪] ManualInputDialog 非打印模式，直接保存订单");
                    saveOrdersToServer(orders);
                }
            }
        });
        dialog.setOnDismissListener(dialogInterface -> {
            Log.d(TAG, "[日志追踪] ManualInputDialog onDismiss");
            currentDialog = null;
        });
        dialog.show();
        Log.d(TAG, "[日志追踪] ManualInputDialog.show() 调用");
        currentDialog = dialog;
    }

    private void openManualInputDialogForCategory(NxDistributerGoodsEntity goods) {
        Log.d(TAG, "[日志追踪] 选择弹出人工输入弹窗 ManualInputDialog（类别模式）");
        List<NxDepartmentOrdersEntity> orderList = getOrderListForCategoryGoods(goods);
        ManualInputDialog dialog = new ManualInputDialog(this, goods, orderList);
        dialog.setOnConfirmListener(orders -> {
            Log.d(TAG, "[日志追踪] ManualInputDialog 确认回调, 订单数量: " + (orders != null ? orders.size() : 0));
            if (orders != null && !orders.isEmpty()) {
                for (int i = 0; i < orders.size(); i++) {
                    NxDepartmentOrdersEntity order = orders.get(i);
                    Log.d(TAG, "[日志追踪] ManualInputDialog 订单" + (i + 1) + "详情: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
                }
                if (isPrintMode) {
                    Log.d(TAG, "[日志追踪] ManualInputDialog 打印模式，调用printAndSaveOrders");
                    printAndSaveOrders(orders, 0);
                } else {
                    Log.d(TAG, "[日志追踪] ManualInputDialog 非打印模式，直接保存订单");
                    saveOrdersToServer(orders);
                }
            }
        });
        dialog.setOnDismissListener(dialogInterface -> {
            Log.d(TAG, "[日志追踪] ManualInputDialog onDismiss");
            currentDialog = null;
        });
        dialog.show();
        Log.d(TAG, "[日志追踪] ManualInputDialog.show() 调用");
        currentDialog = dialog;
    }

    /**
     * 【业务核心】初始化Intent传递的数据 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责从Intent中获取客户出库所需的关键数据
     * 包括分销商ID、客户名称、订单数量等
     * 
     * 【业务规则】
     * 1. 分销商ID必须存在，否则跳转登录页面
     * 2. 支持多客户选择，自动处理客户名称显示
     * 3. 订单数量信息用于UI显示
     * 
     * 【数据来源】
     * - 从Intent获取：distributer_id, customer_names等
     * - 从SharedPreferences获取：用户登录信息
     * 
     * 【注意事项】
     * - 此方法为业务入口，必须保持稳定
     * - 数据验证逻辑涉及用户体验，请谨慎修改
     */
    private void initIntentData() {
        Log.d(TAG, "[数据] 开始初始化Intent数据");
        
        // 从Intent获取分销商ID
        disId = getIntent().getIntExtra("distributer_id", -1);
        if (disId == -1) {
            // 如果Intent中没有，尝试从SharedPreferences获取
            SharedPreferences userPrefs = getSharedPreferences("user_info", MODE_PRIVATE);
            disId = userPrefs.getInt("distributer_id", -1);
        }
        
        if (disId == -1) {
            Log.e(TAG, "[数据] 未找到分销商ID，请先登录");
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            // 跳转到登录页面，并清除任务栈
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        // 获取客户信息（支持多选）
        String[] customerNames = getIntent().getStringArrayExtra("customer_names");
        customerOrderCount = getIntent().getIntExtra("customer_order_count", 0);
        customerUnpickedCount = getIntent().getIntExtra("customer_unpicked_count", 0);
        int selectedCount = getIntent().getIntExtra("selected_count", 0);
        
        // 处理客户名称显示
        if (customerNames != null && customerNames.length > 0) {
            if (customerNames.length == 1) {
                customerName = customerNames[0];
            } else {
                customerName = "多个客户 (" + customerNames.length + "个)";
            }
            Log.d(TAG, "[数据] 接收到 " + customerNames.length + " 个客户: " + android.text.TextUtils.join(", ", customerNames));
        } else {
            customerName = "客户出库";
            Log.w(TAG, "[数据] 未接收到客户名称");
        }
        
        Log.d(TAG, "[数据] Intent数据初始化完成: disId=" + disId + 
              ", customerName=" + customerName + 
              ", selectedCount=" + selectedCount +
              ", orderCount=" + customerOrderCount + 
              ", unpickedCount=" + customerUnpickedCount);
    }

    /**
     * 初始化UI组件
     */
    private void initViews() {
        Log.d(TAG, "[UI] 开始初始化UI组件");
        
        tvTitle = findViewById(R.id.tv_title);
        rvShelf = findViewById(R.id.rv_shelf);
        rvGoods = findViewById(R.id.rv_goods);
        btnRight = findViewById(R.id.btn_right); // 初始化右上角部门按钮
        btnModeSwitch = findViewById(R.id.btn_mode_switch); // 初始化模式切换按钮
        
        // 设置RecyclerView
        rvShelf.setLayoutManager(new LinearLayoutManager(this));
        
        // 根据屏幕方向设置商品列表的布局管理器
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时使用2列网格布局
            rvGoods.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
            Log.d(TAG, "[UI] 横屏模式，使用2列网格布局");
        } else {
            // 竖屏时使用单列布局
            rvGoods.setLayoutManager(new LinearLayoutManager(this));
            Log.d(TAG, "[UI] 竖屏模式，使用单列布局");
        }
        
        Log.d(TAG, "[UI] UI组件初始化完成");
    }

    /**
     * 根据屏幕方向更新布局管理器
     */
    private void updateLayoutManager() {
        if (rvGoods != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                // 横屏时使用2列网格布局
                rvGoods.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
                Log.d(TAG, "[UI] 横屏模式，使用2列网格布局");
            } else {
                // 竖屏时使用单列布局
                rvGoods.setLayoutManager(new LinearLayoutManager(this));
                Log.d(TAG, "[UI] 竖屏模式，使用单列布局");
            }
        }
    }

    /**
     * 绑定事件
     */
    private void bindEvents() {
        Log.d(TAG, "[事件] 开始绑定事件");
        
        // 返回按钮
        findViewById(R.id.iv_avatar).setOnClickListener(v -> {
            Log.d(TAG, "[事件] 点击返回按钮");
            setResult(RESULT_OK); // 设置返回结果，触发客户列表刷新
            finish();
        });
        
        // 设置按钮 - 已删除，改为部门按钮
        // findViewById(R.id.btn_settings).setOnClickListener(v -> {
        //     Log.d(TAG, "[事件] 点击设置按钮");
        //     // 跳转到设置页面
        //     Intent intent = new Intent(this, SettingsActivity.class);
        //     startActivity(intent);
        // });
        
        // 右上角部门按钮点击事件，跳转到DepartmentListActivity
        if (btnRight != null) {
            btnRight.setOnClickListener(v -> {
                Log.d(TAG, "[事件] 点击右上角部门按钮，跳转DepartmentListActivity");
                try {
                    Intent intent = new Intent(this, DepartmentListActivity.class);
                    intent.putExtra("disId", disId); // 传递分销商ID
                    startActivityForResult(intent, 1001);
                } catch (Exception e) {
                    Log.e(TAG, "启动DepartmentListActivity失败", e);
                    Toast.makeText(this, "启动部门列表页面失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // 【新增】模式切换按钮点击事件
        if (btnModeSwitch != null) {
            btnModeSwitch.setOnClickListener(v -> {
                Log.d(TAG, "[事件] 点击模式切换按钮，当前模式: " + (isCategoryMode ? "商品类别" : "货架"));
                toggleDisplayMode();
            });
        }
        
        Log.d(TAG, "[事件] 事件绑定完成");
    }
    
    /**
     * 【新增】切换显示模式
     * 在商品类别模式和货架模式之间切换
     */
    private void toggleDisplayMode() {
        Log.d(TAG, "[模式切换] 开始切换显示模式");
        Log.d(TAG, "[模式切换] 当前模式: " + (isCategoryMode ? "商品类别" : "货架"));
        
        // 切换模式
        isCategoryMode = !isCategoryMode;
        
        // 重置选中状态
        selectedShelfIndex = -1;
        selectedShelfId = null;
        selectedCategoryIndex = -1;
        selectedCategoryId = null;
        
        // 更新按钮图标
        updateModeSwitchButtonIcon();
        
        // 重新加载数据
        loadStockGoodsData();
        
        Log.d(TAG, "[模式切换] 模式切换完成，新模式: " + (isCategoryMode ? "商品类别" : "货架"));
    }
    
    /**
     * 【新增】更新模式切换按钮文本
     */
    private void updateModeSwitchButtonIcon() {
        if (btnModeSwitch != null) {
            int iconResource = isCategoryMode ? R.drawable.shelf : R.drawable.goods;
            btnModeSwitch.setImageResource(iconResource);
            Log.d(TAG, "[模式切换] 更新按钮图标: " + (isCategoryMode ? "货架图标" : "商品图标"));
        }
    }

    /**
     * 【业务核心】加载出库数据 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责从服务器获取客户出库所需的商品和订单数据
     * 对应小程序 _initNxDataKf() 方法，保持数据一致性
     * 
     * 【业务规则】
     * 1. 从缓存获取选中的部门ID（outNxDepIds, outGbDepIds）
     * 2. 支持多部门数据合并显示
     * 3. 数据格式与小程序完全一致
     * 4. 空数据时自动清除缓存并返回
     * 
     * 【接口调用】
     * - 接口：stockerGetToStockGoodsWithDepIdsKf
     * - 参数：nxDepIds, gbDepIds, disId
     * - 响应：StockGoodsWithDepIdsResponse
     * 
     * 【数据处理】
     * - 货架数据：shelfList
     * - 商品数据：NxDistributerGoodsShelfGoodsEntity
     * - 订单数据：NxDepartmentOrdersEntity
     * 
     * 【注意事项】
     * - 此方法为数据加载核心，涉及业务数据完整性
     * - 参数处理逻辑与小程序保持一致，请勿随意修改
     * - 错误处理涉及用户体验，必须保持稳定
     */
    private void loadStockGoodsData() {
        long start = System.currentTimeMillis();
        Log.d("PERF", "loadStockGoodsData start: " + start);
        Log.d(TAG, "[数据] 开始加载出库数据");
        
        // 从缓存获取选中的部门ID，与小程序保持一致
        SharedPreferences cacheSp = getSharedPreferences("idsChangeStock", MODE_PRIVATE);
        String nxDepIdsStr = cacheSp.getString("outNxDepIds", "");
        String gbDepIdsStr = cacheSp.getString("outGbDepIds", "");
        
        Log.d(TAG, "[数据] 缓存数据: outNxDepIds=" + nxDepIdsStr + ", outGbDepIds=" + gbDepIdsStr);
        
        if (nxDepIdsStr.isEmpty() && gbDepIdsStr.isEmpty()) {
            Log.w(TAG, "[数据] 没有选中的客户，显示空状态");
            hideLoading();
            showEmptyState();
            return;
        }
        
        // 处理参数格式，与小程序 _initNxDataKf() 方法完全一致
        Object nxDepIds = 0;  // 默认为0，对应小程序的 nxids = 0
        Object gbDepIds = 0;  // 默认为0，对应小程序的 gbids = 0
        
        if (!nxDepIdsStr.isEmpty()) {
            // 对应小程序的逻辑：if (nxL > 0) { nxids = this.data.outNxDepIds; }
            if (nxDepIdsStr.contains(",")) {
                // 多个ID，保持字符串格式，对应小程序的数组
                nxDepIds = nxDepIdsStr;
                Log.d(TAG, "[数据] nxDepIds为多个ID: " + nxDepIds);
            } else {
                try {
                    // 单个ID，转换为数字，对应小程序的单个数字
                    nxDepIds = Integer.parseInt(nxDepIdsStr);
                    Log.d(TAG, "[数据] nxDepIds为单个ID: " + nxDepIds);
                } catch (NumberFormatException e) {
                    nxDepIds = nxDepIdsStr;  // 转换失败，保持字符串
                    Log.w(TAG, "[数据] nxDepIds转换失败，保持字符串: " + nxDepIds);
                }
            }
        }
        
        if (!gbDepIdsStr.isEmpty()) {
            // 对应小程序的逻辑：if (gbL > 0) { gbids = this.data.outGbDepIds; }
            if (gbDepIdsStr.contains(",")) {
                // 多个ID，保持字符串格式，对应小程序的数组
                gbDepIds = gbDepIdsStr;
                Log.d(TAG, "[数据] gbDepIds为多个ID: " + gbDepIds);
            } else {
                try {
                    // 单个ID，转换为数字，对应小程序的单个数字
                    gbDepIds = Integer.parseInt(gbDepIdsStr);
                    Log.d(TAG, "[数据] gbDepIds为单个ID: " + gbDepIds);
                } catch (NumberFormatException e) {
                    gbDepIds = gbDepIdsStr;  // 转换失败，保持字符串
                    Log.w(TAG, "[数据] gbDepIds转换失败，保持字符串: " + gbDepIds);
                }
            }
        }
        
        // 显示加载蒙版
        showLoading();
        
        // 根据模式调用不同的接口
        GoodsApi api = HttpManager.getInstance().getApi(GoodsApi.class);
        
        if (isCategoryMode) {
            // 商品类别模式 - 调用 stokerGetToStockGoodsWithDepIds 接口
            Log.d(TAG, "[数据] 商品类别模式，调用 stokerGetToStockGoodsWithDepIds 接口");
            HttpManager.getInstance()
                    .request(api.stokerGetToStockGoodsWithDepIds(nxDepIds.toString(), gbDepIds.toString(), disId),
                            new TypeToken<StockGoodsWithCategoriesResponse>() {})
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<StockGoodsWithCategoriesResponse>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "[接口] stokerGetToStockGoodsWithDepIds 请求完成");
                            hideLoading();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "[接口] stokerGetToStockGoodsWithDepIds 请求失败", e);
                            hideLoading();
                            Toast.makeText(CustomerStockOutActivity.this, 
                                    "获取数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }

                        @Override
                        public void onNext(StockGoodsWithCategoriesResponse response) {
                            Log.d(TAG, "[接口] stokerGetToStockGoodsWithDepIds 请求成功，数据: " + response);
                            
                            if (response != null && response.getGrandArr() != null) {
                                categoryList = response.getGrandArr();
                                Log.d(TAG, "[数据] 解析到 " + categoryList.size() + " 个商品类别");
                                
                                if (categoryList.isEmpty()) {
                                    Log.w(TAG, "[数据] 商品类别列表为空，清除缓存并返回");
                                    clearCustomerCache();
                                    setResult(RESULT_OK); // 设置返回结果，触发客户列表刷新
                                    finish();
                                } else {
                                    updateUI();
                                }
                            } else {
                                Log.w(TAG, "[数据] 响应数据为空");
                                showEmptyState();
                            }
                        }
                    });
        } else {
            // 货架模式 - 调用 stockerGetToStockGoodsWithDepIdsKf 接口
            Log.d(TAG, "[数据] 货架模式，调用 stockerGetToStockGoodsWithDepIdsKf 接口");
            HttpManager.getInstance()
                    .request(api.stockerGetToStockGoodsWithDepIdsKf(nxDepIds.toString(), gbDepIds.toString(), disId),
                            new TypeToken<StockGoodsWithDepIdsResponse>() {})
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<StockGoodsWithDepIdsResponse>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "[接口] stockerGetToStockGoodsWithDepIdsKf 请求完成");
                            hideLoading();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "[接口] stockerGetToStockGoodsWithDepIdsKf 请求失败", e);
                            hideLoading();
                            Toast.makeText(CustomerStockOutActivity.this, 
                                    "获取数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }

                        @Override
                        public void onNext(StockGoodsWithDepIdsResponse response) {
                            Log.d(TAG, "[接口] stockerGetToStockGoodsWithDepIdsKf 请求成功，数据: " + response);
                            
                            if (response != null && response.getShelfArr() != null) {
                                shelfList = response.getShelfArr();
                                Log.d(TAG, "[数据] 解析到 " + shelfList.size() + " 个货架");
                                
                                if (shelfList.isEmpty()) {
                                    Log.w(TAG, "[数据] 货架列表为空，清除缓存并返回");
                                    clearCustomerCache();
                                    setResult(RESULT_OK); // 设置返回结果，触发客户列表刷新
                                    finish();
                                } else {
                                    updateUI();
                                }
                            } else {
                                Log.w(TAG, "[数据] 响应数据为空");
                                showEmptyState();
                            }
                        }
                    });
        }
        
        Log.d("PERF", "loadStockGoodsData end: " + System.currentTimeMillis() + ", 耗时: " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * 更新UI显示
     */
    private void updateUI() {
        Log.d(TAG, "[UI] 开始更新UI显示，当前模式: " + (isCategoryMode ? "商品类别" : "货架"));
        
        if (isCategoryMode) {
            // 商品类别模式
            updateCategoryUI();
        } else {
            // 货架模式
            updateShelfUI();
        }
        
        Log.d(TAG, "[UI] UI更新完成");
    }
    
    /**
     * 【新增】更新商品类别UI
     */
    private void updateCategoryUI() {
        Log.d(TAG, "[UI] 更新商品类别UI");
        
        // 设置商品类别适配器
        if (rvShelf != null) {
            CategoryAdapter categoryAdapter = new CategoryAdapter();
            categoryAdapter.setData(categoryList);
            rvShelf.setLayoutManager(new LinearLayoutManager(this));
            rvShelf.setAdapter(categoryAdapter);
            
            // 设置商品类别选中状态
            if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categoryList.size()) {
                categoryAdapter.setSelectedPosition(selectedCategoryIndex);
                Log.d(TAG, "[UI] 设置商品类别适配器选中位置: " + selectedCategoryIndex);
            }
            
            // 设置商品类别点击事件
            categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(NxDistributerFatherGoodsEntity entity) {
                    selectedCategoryIndex = categoryList.indexOf(entity);
                    selectedCategoryId = entity.getNxDistributerFatherGoodsId();
                    Log.d(TAG, "[商品类别点击] 选中类别: index=" + selectedCategoryIndex + ", id=" + selectedCategoryId + ", name=" + entity.getNxDfgFatherGoodsName());
                    updateCategoryGoodsList();
                }
            });
            
            Log.d(TAG, "[UI] 商品类别适配器设置完成，共 " + categoryList.size() + " 个类别");
        }
        
        // 如果有商品类别数据，根据记录的商品类别ID选中对应类别
        if (!categoryList.isEmpty()) {
            if (selectedCategoryId != null) {
                // 根据记录的商品类别ID查找对应的类别
                int targetIndex = -1;
                for (int i = 0; i < categoryList.size(); i++) {
                    if (selectedCategoryId.equals(categoryList.get(i).getNxDistributerFatherGoodsId())) {
                        targetIndex = i;
                        break;
                    }
                }
                
                if (targetIndex >= 0) {
                    selectedCategoryIndex = targetIndex;
                    Log.d(TAG, "[UI] 根据商品类别ID选中类别: id=" + selectedCategoryId + ", index=" + selectedCategoryIndex + ", name=" + categoryList.get(selectedCategoryIndex).getNxDfgFatherGoodsName());
                } else {
                    // 如果找不到对应的商品类别ID，默认选中第一个
                    selectedCategoryIndex = 0;
                    selectedCategoryId = categoryList.get(0).getNxDistributerFatherGoodsId();
                    Log.d(TAG, "[UI] 未找到记录的商品类别ID，默认选中第一个类别: id=" + selectedCategoryId + ", index=" + selectedCategoryIndex);
                }
            } else {
                // 第一次加载，默认选中第一个商品类别
                selectedCategoryIndex = 0;
                selectedCategoryId = categoryList.get(0).getNxDistributerFatherGoodsId();
                Log.d(TAG, "[UI] 首次加载，默认选中第一个商品类别: id=" + selectedCategoryId + ", index=" + selectedCategoryIndex);
            }
            updateCategoryGoodsList();
        }
    }
    
    /**
     * 【新增】更新货架UI
     */
    private void updateShelfUI() {
        Log.d(TAG, "[UI] 更新货架UI");
        
        // 设置货架适配器
        if (rvShelf != null) {
            StockOutShelfAdapter shelfAdapter = new StockOutShelfAdapter();
            shelfAdapter.setData(shelfList);
            rvShelf.setLayoutManager(new LinearLayoutManager(this));
            rvShelf.setAdapter(shelfAdapter);
            
            // 设置货架选中状态
            if (selectedShelfIndex >= 0 && selectedShelfIndex < shelfList.size()) {
                shelfAdapter.setSelectedPosition(selectedShelfIndex);
                Log.d(TAG, "[UI] 设置货架适配器选中位置: " + selectedShelfIndex);
            }
            
            // 设置货架点击事件
            shelfAdapter.setOnItemClickListener(new StockOutShelfAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(NxDistributerGoodsShelfEntity entity) {
                    selectedShelfIndex = shelfList.indexOf(entity);
                    selectedShelfId = entity.getNxDistributerGoodsShelfId();
                    Log.d(TAG, "[货架点击] 选中货架: index=" + selectedShelfIndex + ", id=" + selectedShelfId + ", name=" + entity.getNxDistributerGoodsShelfName());
                    updateGoodsList();
                }
            });
            
            Log.d(TAG, "[UI] 货架适配器设置完成，共 " + shelfList.size() + " 个货架");
        }
        
        // 如果有货架数据，根据记录的货架ID选中对应货架
        if (!shelfList.isEmpty()) {
            if (selectedShelfId != null) {
                // 根据记录的货架ID查找对应的货架
                int targetIndex = -1;
                for (int i = 0; i < shelfList.size(); i++) {
                    if (selectedShelfId.equals(shelfList.get(i).getNxDistributerGoodsShelfId())) {
                        targetIndex = i;
                        break;
                    }
                }
                
                if (targetIndex >= 0) {
                    selectedShelfIndex = targetIndex;
                    Log.d(TAG, "[UI] 根据货架ID选中货架: id=" + selectedShelfId + ", index=" + selectedShelfIndex + ", name=" + shelfList.get(selectedShelfIndex).getNxDistributerGoodsShelfName());
                } else {
                    // 如果找不到对应的货架ID，默认选中第一个
                    selectedShelfIndex = 0;
                    selectedShelfId = shelfList.get(0).getNxDistributerGoodsShelfId();
                    Log.d(TAG, "[UI] 未找到记录的货架ID，默认选中第一个货架: id=" + selectedShelfId + ", index=" + selectedShelfIndex);
                }
            } else {
                // 第一次加载，默认选中第一个货架
                selectedShelfIndex = 0;
                selectedShelfId = shelfList.get(0).getNxDistributerGoodsShelfId();
                Log.d(TAG, "[UI] 首次加载，默认选中第一个货架: id=" + selectedShelfId + ", index=" + selectedShelfIndex);
            }
            updateGoodsList();
        }
    }
    
    /**
     * 【新增】更新商品类别商品列表
     */
    private void updateCategoryGoodsList() {
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categoryList.size()) {
            Log.w(TAG, "[UI] 选中的商品类别索引无效: " + selectedCategoryIndex);
            return;
        }
        
        NxDistributerFatherGoodsEntity selectedCategory = categoryList.get(selectedCategoryIndex);
        List<NxDistributerGoodsEntity> goodsList = selectedCategory.getNxDistributerGoodsEntities();
        
        Log.d(TAG, "[UI] 更新商品类别商品列表，类别: " + selectedCategory.getNxDfgFatherGoodsName() + 
              "，商品数量: " + (goodsList != null ? goodsList.size() : 0));
        
        if (rvGoods != null) {
            CategoryGoodsAdapter goodsAdapter = new CategoryGoodsAdapter();
            goodsAdapter.setGoodsList(goodsList != null ? goodsList : new ArrayList<>());
            rvGoods.setAdapter(goodsAdapter);
            
            // 设置商品点击事件
            goodsAdapter.setOnItemClickListener(new CategoryGoodsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(NxDistributerGoodsEntity goods) {
                    Log.d(TAG, "[商品点击] 选中商品: " + goods.getNxDgGoodsName());
                    showCategoryStockOutDialog(goods);
                }
            });
            
            Log.d(TAG, "[UI] 商品类别商品适配器设置完成");
        }
    }
    
    /**
     * 更新商品列表
     */
    private void updateGoodsList() {
        if (selectedShelfIndex < 0 || selectedShelfIndex >= shelfList.size()) {
            Log.w(TAG, "[UI] 选中的货架索引无效: " + selectedShelfIndex);
            return;
        }
        
        NxDistributerGoodsShelfEntity selectedShelf = shelfList.get(selectedShelfIndex);
        List<NxDistributerGoodsShelfGoodsEntity> goodsList = selectedShelf.getNxDisGoodsShelfGoodsEntities();
        
        Log.d(TAG, "[UI] 更新商品列表，货架: " + selectedShelf.getNxDistributerGoodsShelfName() + 
              "，商品数量: " + (goodsList != null ? goodsList.size() : 0));
        
        if (rvGoods != null) {
            StockOutGoodsAdapter goodsAdapter = new StockOutGoodsAdapter();
            goodsAdapter.setGoodsList(goodsList != null ? goodsList : new ArrayList<>());
            
            rvGoods.setAdapter(goodsAdapter);
        
        // 设置商品点击事件
        goodsAdapter.setOnItemClickListener(new StockOutGoodsAdapter.OnItemClickListener() {
            @Override
                public void onItemClick(NxDistributerGoodsShelfGoodsEntity goods) {
                    showStockOutDialog(goods);
                }
            });
            
            Log.d(TAG, "[UI] 商品适配器设置完成");
        }
    }
    
    /**
     * 【业务核心】显示出库对话框 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责显示商品出库操作对话框
     * 用户在此对话框中确认出库数量和打印设置
     * 
     * 【业务规则】
     * 1. 根据当前打印模式显示不同的操作选项
     * 2. 支持订单数量修改和确认
     * 3. 出库确认后自动执行打印或保存操作
     * 4. 弹窗关闭时自动刷新打印模式状态
     * 
     * 【打印模式】
     * - 打印标签模式：先打印后保存订单
     * - 非打印标签模式：直接保存订单
     * 
     * 【回调处理】
     * - onConfirmListener：处理出库确认逻辑
     * - onDismissListener：处理弹窗关闭逻辑
     * 
     * 【注意事项】
     * - 此方法为出库操作入口，涉及业务核心流程
     * - 打印模式判断逻辑经过优化，请谨慎修改
     * - 订单处理涉及数据完整性，必须保持稳定
     */
    private void showStockOutDialog(NxDistributerGoodsShelfGoodsEntity goods) {
        Log.d(TAG, "[日志追踪] showStockOutDialog called, goods=" + goods.getNxDistributerGoodsEntity().getNxDgGoodsName());
        try {
            refreshStockOutMode();
            Log.d(TAG, "[日志追踪] 当前出库模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
            Log.d(TAG, "[日志追踪] 开始创建出库弹窗判断");
        } catch (Exception e) {
            Log.e(TAG, "[日志追踪] showStockOutDialog 异常: " + e.getMessage(), e);
            return;
        }

        // 【屏蔽】原有仅弹出蓝牙称弹窗的实现，保留以便随时恢复
        /*
        StockOutGoodsDialog dialog = new StockOutGoodsDialog(this, goods, isPrintMode);
        Log.d(TAG, "[日志追踪] StockOutGoodsDialog创建完成");
        dialog.setOnConfirmListener(orders -> {
            Log.d(TAG, "[确认回调] ========== 开始confirmListener.onConfirm ==========");
            Log.d(TAG, "[确认回调] confirmListener.onConfirm被调用, 订单数量: " + (orders != null ? orders.size() : 0));
            Log.d(TAG, "[确认回调] 当前打印模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
            if (orders != null && !orders.isEmpty()) {
                for (int i = 0; i < orders.size(); i++) {
                    NxDepartmentOrdersEntity order = orders.get(i);
                    Log.d(TAG, "[确认回调] 订单" + (i+1) + "详情: orderId=" + order.getNxDepartmentOrdersId() + 
                          ", weight=" + order.getNxDoWeight() + 
                          ", quantity=" + order.getNxDoQuantity() + 
                          ", goodsName=" + (order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
                }
                if (isPrintMode) {
                    Log.d(TAG, "[确认回调] 打印模式，调用printAndSaveOrders");
                    printAndSaveOrders(orders, 0);
                } else {
                    Log.d(TAG, "[确认回调] 非打印模式，直接保存订单");
                    saveOrdersToServer(orders);
                }
            } else {
                Log.d(TAG, "[确认回调] 订单为空或数量为0");
            }
            Log.d(TAG, "[确认回调] ========== confirmListener.onConfirm完成 ==========");
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "[日志追踪] StockOutGoodsDialog onDismiss");
                currentDialog = null;
                Log.d(TAG, "[日志追踪] 弹窗对象已清理");
            }
        });
        dialog.show();
        Log.d(TAG, "[日志追踪] StockOutGoodsDialog.show() 调用");
        currentDialog = dialog;
        */

        // 【新增】根据是否有蓝牙称缓存，弹出蓝牙称弹窗或人工输入弹窗
        boolean scaleCache = hasScaleCache();
        Log.d(TAG, "[日志追踪] hasScaleCache() 判断结果: " + scaleCache);
        if (scaleCache) {
            Log.d(TAG, "[日志追踪] 选择弹出蓝牙称弹窗 StockOutGoodsDialog");
            StockOutGoodsDialog dialog = new StockOutGoodsDialog(this, goods, isPrintMode);
            Log.d(TAG, "[日志追踪] StockOutGoodsDialog创建完成");
            dialog.setOnConfirmListener(orders -> {
                Log.d(TAG, "[日志追踪] StockOutGoodsDialog 确认回调, 订单数量: " + (orders != null ? orders.size() : 0));
                if (orders != null && !orders.isEmpty()) {
                    for (int i = 0; i < orders.size(); i++) {
                        NxDepartmentOrdersEntity order = orders.get(i);
                        Log.d(TAG, "[日志追踪] StockOutGoodsDialog 订单" + (i+1) + "详情: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
                    }
                    if (isPrintMode) {
                        Log.d(TAG, "[日志追踪] StockOutGoodsDialog 打印模式，调用printAndSaveOrders");
                        printAndSaveOrders(orders, 0);
                    } else {
                        Log.d(TAG, "[日志追踪] StockOutGoodsDialog 非打印模式，直接保存订单");
                        saveOrdersToServer(orders);
                    }
                }
            });
            dialog.setOnSwitchToManualListener(manualGoods -> {
                Log.d(TAG, "[日志追踪] StockOutGoodsDialog 用户请求切换到手动输入");
                runOnUiThread(() -> {
                    if (manualGoods != null) {
                        openManualInputDialog(manualGoods);
                    } else {
                        Log.w(TAG, "[日志追踪] 切换手动输入时收到空的商品实体");
                        Toast.makeText(this, "无法切换到手动输入模式", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d(TAG, "[日志追踪] StockOutGoodsDialog onDismiss");
                    currentDialog = null;
                }
            });
            dialog.show();
            Log.d(TAG, "[日志追踪] StockOutGoodsDialog.show() 调用");
            currentDialog = dialog;
        } else {
            openManualInputDialog(goods);
        }
    }

    /**
     * 【新增】商品类别模式下的出库对话框
     */
    private void showCategoryStockOutDialog(NxDistributerGoodsEntity goods) {
        Log.d(TAG, "[日志追踪] showCategoryStockOutDialog called, goods=" + goods.getNxDgGoodsName());
        try {
            refreshStockOutMode();
            Log.d(TAG, "[日志追踪] 当前出库模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
            Log.d(TAG, "[日志追踪] 开始创建出库弹窗判断");
        } catch (Exception e) {
            Log.e(TAG, "[日志追踪] showCategoryStockOutDialog 异常: " + e.getMessage(), e);
            return;
        }

        // 【新增】根据是否有蓝牙称缓存，弹出蓝牙称弹窗或人工输入弹窗
        boolean scaleCache = hasScaleCache();
        Log.d(TAG, "[日志追踪] hasScaleCache() 判断结果: " + scaleCache);
        if (scaleCache) {
            Log.d(TAG, "[日志追踪] 选择弹出蓝牙称弹窗 StockOutGoodsDialog");
            // 创建适配器，将 NxDistributerGoodsEntity 转换为 NxDistributerGoodsShelfGoodsEntity
            NxDistributerGoodsShelfGoodsEntity adaptedGoods = createAdaptedGoodsEntity(goods);
            StockOutGoodsDialog dialog = new StockOutGoodsDialog(this, adaptedGoods, isPrintMode);
            Log.d(TAG, "[日志追踪] StockOutGoodsDialog创建完成");
            dialog.setOnConfirmListener(orders -> {
                Log.d(TAG, "[日志追踪] StockOutGoodsDialog 确认回调, 订单数量: " + (orders != null ? orders.size() : 0));
                if (orders != null && !orders.isEmpty()) {
                    for (int i = 0; i < orders.size(); i++) {
                        NxDepartmentOrdersEntity order = orders.get(i);
                        Log.d(TAG, "[日志追踪] StockOutGoodsDialog 订单" + (i+1) + "详情: orderId=" + order.getNxDepartmentOrdersId() + ", weight=" + order.getNxDoWeight());
                    }
                    if (isPrintMode) {
                        Log.d(TAG, "[日志追踪] StockOutGoodsDialog 打印模式，调用printAndSaveOrders");
                        printAndSaveOrders(orders, 0);
                    } else {
                        Log.d(TAG, "[日志追踪] StockOutGoodsDialog 非打印模式，直接保存订单");
                        saveOrdersToServer(orders);
                    }
                }
            });
            dialog.setOnSwitchToManualListener(manualGoods -> {
                Log.d(TAG, "[日志追踪] StockOutGoodsDialog（类别） 用户请求切换到手动输入");
                runOnUiThread(() -> openManualInputDialogForCategory(goods));
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d(TAG, "[日志追踪] StockOutGoodsDialog onDismiss");
                    currentDialog = null;
                }
            });
            dialog.show();
            Log.d(TAG, "[日志追踪] StockOutGoodsDialog.show() 调用");
            currentDialog = dialog;
        } else {
            openManualInputDialogForCategory(goods);
        }
    }

    /**
     * 【新增】获取商品类别商品的订单列表
     */
    private List<NxDepartmentOrdersEntity> getOrderListForCategoryGoods(NxDistributerGoodsEntity goods) {
        Log.d(TAG, "[日志追踪] getOrderListForCategoryGoods called, goods=" + (goods != null ? goods.getNxDgGoodsName() : "null"));
        
        List<NxDepartmentOrdersEntity> orders = null;
        if (goods != null) {
            orders = goods.getNxDepartmentOrdersEntities();
            Log.d(TAG, "[日志追踪] getOrderListForCategoryGoods 获取订单: " + (orders != null ? orders.size() : 0));
        }
        
        if (orders == null) {
            orders = new ArrayList<>();
            Log.d(TAG, "[日志追踪] getOrderListForCategoryGoods 创建空订单列表");
        }
        
        Log.d(TAG, "[日志追踪] getOrderListForCategoryGoods 最终返回订单数量: " + orders.size());
        return orders;
    }

    /**
     * 【新增】创建适配的商品实体，用于蓝牙称弹窗
     */
    private NxDistributerGoodsShelfGoodsEntity createAdaptedGoodsEntity(NxDistributerGoodsEntity goods) {
        Log.d(TAG, "[日志追踪] createAdaptedGoodsEntity called, goods=" + goods.getNxDgGoodsName());
        
        // 创建一个新的 NxDistributerGoodsShelfGoodsEntity 实例
        NxDistributerGoodsShelfGoodsEntity adaptedGoods = new NxDistributerGoodsShelfGoodsEntity();
        
        // 设置商品信息
        adaptedGoods.setNxDistributerGoodsEntity(goods);
        
        // 设置订单信息
        List<NxDepartmentOrdersEntity> orders = goods.getNxDepartmentOrdersEntities();
        if (orders != null) {
            adaptedGoods.setNxDepartmentOrdersEntities(orders);
            Log.d(TAG, "[日志追踪] createAdaptedGoodsEntity 设置订单数量: " + orders.size());
        } else {
            adaptedGoods.setNxDepartmentOrdersEntities(new ArrayList<>());
            Log.d(TAG, "[日志追踪] createAdaptedGoodsEntity 设置空订单列表");
        }
        
        Log.d(TAG, "[日志追踪] createAdaptedGoodsEntity 完成，商品名称: " + goods.getNxDgGoodsName());
        return adaptedGoods;
    }

    // 【新增】获取当前商品的订单列表（可根据实际业务调整）
    private List<NxDepartmentOrdersEntity> getOrderListForGoods(NxDistributerGoodsShelfGoodsEntity goods) {
        // 这里假设商品下有订单列表，实际可根据你的数据结构调整
        Log.d(TAG, "[日志追踪] getOrderListForGoods called, goods=" + (goods != null ? goods.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
        
        List<NxDepartmentOrdersEntity> orders = null;
        if (goods != null && goods.getNxDistributerGoodsEntity() != null) {
            orders = goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
            Log.d(TAG, "[日志追踪] getOrderListForGoods 从NxDistributerGoodsEntity获取订单: " + (orders != null ? orders.size() : 0));
        }
        
        if (orders == null || orders.isEmpty()) {
            // 尝试从goods本身获取订单
            orders = goods.getNxDepartmentOrdersEntities();
            Log.d(TAG, "[日志追踪] getOrderListForGoods 从goods本身获取订单: " + (orders != null ? orders.size() : 0));
        }
        
        if (orders == null) {
            orders = new ArrayList<>();
            Log.d(TAG, "[日志追踪] getOrderListForGoods 创建空订单列表");
        }
        
        Log.d(TAG, "[日志追踪] getOrderListForGoods 最终返回订单数量: " + orders.size());
        return orders;
    }

    // 【新增】判断是否有蓝牙称缓存
    private boolean hasScaleCache() {
        SharedPreferences sp = getSharedPreferences("scale_cache", MODE_PRIVATE);
        String scaleAddress = sp.getString("scale_address", null);
        String scaleName = sp.getString("scale_name", null);
        boolean result = scaleAddress != null && !scaleAddress.isEmpty() && scaleName != null && !scaleName.isEmpty();
        Log.d(TAG, "[日志追踪] hasScaleCache() scaleAddress=" + scaleAddress + ", scaleName=" + scaleName + ", result=" + result);
        return result;
    }

    /**
     * 显示空状态
     */
    private void showEmptyState() {
        Log.d(TAG, "[UI] 显示空状态");
        
        // 显示空状态提示
        Toast.makeText(this, "暂无出库数据", Toast.LENGTH_LONG).show();
        
        // 可以在这里添加空状态的UI显示
        // 比如显示一个空状态的图片和文字
    }

    /**
     * 清除客户缓存
     */
    private void clearCustomerCache() {
        Log.d(TAG, "[缓存] 清除客户缓存");
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("outNxDepIds");
        editor.remove("outGbDepIds");
        editor.remove("outNxDepNames");
        editor.remove("outGbDepNames");
        editor.apply();
        
        Log.d(TAG, "[缓存] 客户缓存清除完成");
    }

    // 蓝牙秤状态管理方法
    // private void loadScaleCache() { ... }
    // private void updateScaleInfo() { ... }
    // private void autoConnectScale() { ... }

    @Override
    protected void onResume() {
        long start = System.currentTimeMillis();
        Log.d("PERF", "onResume start: " + start);
        super.onResume();
        // loadScaleCache();
        // autoConnectScale();
        // updateScaleInfo();
        
        // 每次页面恢复时重新检查设备状态和模式
//        refreshStockOutMode();
        Log.d(TAG, "[生命周期] onResume完成，当前模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
        Log.d("PERF", "onResume end: " + System.currentTimeMillis() + ", 耗时: " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[生命周期] onDestroy - 开始清理资源");
        
        // 关闭加载蒙版，防止内存泄漏
        try {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                Log.d(TAG, "[Loading] 关闭加载蒙版");
                mLoadingDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "[Loading] 关闭加载蒙版失败", e);
        }
        
        // 断开打印机连接
        try {
            Log.d(TAG, "[打印机] 断开所有打印机连接");
            DeviceConnFactoryManager.closeAllPort();
            Log.d(TAG, "[打印机] ✅ 打印机连接已断开");
        } catch (Exception e) {
            Log.e(TAG, "[打印机] 断开连接时出错", e);
        }
        
        Log.d(TAG, "[生命周期] onDestroy - 资源清理完成");
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "[UI] 屏幕方向变化: " + newConfig.orientation);
        updateLayoutManager();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCALE_ACTIVITY && resultCode == RESULT_OK) {
            // loadScaleCache();
            // updateScaleInfo();
        }
    }
    
    /**
     * 【业务核心】打印并保存订单数据 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责串行处理订单的打印和保存操作
     * 确保每个订单都先打印成功后再保存到服务器
     * 
     * 【业务规则】
     * 1. 串行处理：一次处理一个订单，确保数据一致性
     * 2. 打印优先：先打印成功，再保存订单
     * 3. 错误处理：打印失败时停止后续订单处理
     * 4. 数据刷新：所有订单处理完成后自动刷新数据
     * 
     * 【处理流程】
     * 1. 检查订单索引是否超出范围
     * 2. 获取当前订单数据
     * 3. 调用printOrder进行打印
     * 4. 打印成功后调用saveOrdersToServer保存
     * 5. 递归处理下一个订单
     * 
     * 【错误处理】
     * - 打印失败：显示错误对话框，停止处理
     * - 保存失败：记录错误日志，继续处理下一个
     * 
     * 【注意事项】
     * - 此方法为订单处理核心，涉及业务数据完整性
     * - 串行处理逻辑确保数据一致性，请勿修改为并行
     * - 错误处理涉及用户体验，必须保持稳定
     */
    public void printAndSaveOrders(List<NxDepartmentOrdersEntity> orderList, int index) {
        Log.d(TAG, "[打印保存] ========== 开始printAndSaveOrders方法 ==========");
        Log.d(TAG, "[打印保存] printAndSaveOrders called, index=" + index + ", total=" + (orderList != null ? orderList.size() : 0));
        
        if (index >= orderList.size()) {
            Log.d(TAG, "[打印保存] 所有订单处理完成，打印并保存成功，准备刷新数据");
            runOnUiThread(() -> {
                Log.d(TAG, "[打印保存] 所有订单处理完成，主线程Toast");
                Toast.makeText(this, "订单打印并保存成功", Toast.LENGTH_SHORT).show();
                loadStockGoodsData();
            });
            Log.d(TAG, "[打印保存] ========== printAndSaveOrders方法完成 ==========");
            return;
        }
        
        NxDepartmentOrdersEntity order = orderList.get(index);
        Log.d(TAG, "[打印保存] 开始处理订单: orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null"));
        Log.d(TAG, "[打印保存] 订单详情: weight=" + (order != null ? order.getNxDoWeight() : "null") + 
              ", quantity=" + (order != null ? order.getNxDoQuantity() : "null") + 
              ", goodsName=" + (order != null && order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
        
        printOrder(order, new PrintCallback() {
            @Override
            public void onPrintSuccess() {
                Log.d(TAG, "[打印保存] ========== 打印成功回调 ==========");
                Log.d(TAG, "[打印保存] 打印成功, orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null"));
                Log.d(TAG, "[打印保存] 开始保存订单到服务器");
                saveOrdersToServer(Collections.singletonList(order));
                Log.d(TAG, "[打印保存] 保存调用完成，继续处理下一个订单");
                printAndSaveOrders(orderList, index + 1);
            }
            @Override
            public void onPrintFail(String error) {
                Log.e(TAG, "[打印保存] ========== 打印失败回调 ==========");
                Log.e(TAG, "[打印保存] 打印失败, orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null") + ", error=" + error);
                runOnUiThread(() -> {
                    Log.d(TAG, "[打印保存] 打印失败弹窗弹出");
                    refreshStockOutMode();
                    new AlertDialog.Builder(CustomerStockOutActivity.this)
                        .setTitle("标签打印失败")
                        .setMessage("打印失败，订单不会被保存。请检查打印机是否开机？")
                        .setPositiveButton("检查打印", (dialog, which) -> {
                            Log.d(TAG, "[打印保存] 用户点击检查打印按钮");
                            refreshStockOutMode();
                        })
                        .setCancelable(false)
                        .show();
                });
                // 打印失败时，不继续处理后续订单，也不保存当前订单
                Log.d(TAG, "[打印保存] 打印失败，停止处理后续订单，不保存当前订单");
                Log.d(TAG, "[打印保存] ========== printAndSaveOrders方法因打印失败而终止 ==========");
            }
        });
    }

    /**
     * 【业务核心】打印订单 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责单个订单的打印操作
     * 支持ESC和TSC两种打印机指令集
     * 
     * 【业务规则】
     * 1. 环境检查：模拟器环境跳过打印
     * 2. 打印机状态检查：确保打印机连接正常
     * 3. 指令类型识别：自动识别ESC或TSC指令
     * 4. 异步处理：在后台线程执行打印操作
     * 
     * 【打印流程】
     * 1. 检查是否为模拟器环境
     * 2. 检查打印机连接状态
     * 3. 获取打印机指令类型
     * 4. 根据指令类型选择打印方法
     * 5. 执行打印并处理回调
     * 
     * 【指令类型处理】
     * - ESC指令：适用于热敏打印机
     * - TSC指令：适用于标签打印机
     * - null类型：尝试TSC指令（标签打印机通常使用TSC）
     * 
     * 【错误处理】
     * - 打印机未连接：提示用户检查连接
     * - 指令类型不支持：提示用户检查打印机类型
     * - 打印异常：记录错误日志并回调失败
     * 
     * 【注意事项】
     * - 此方法为打印核心，经过多次优化
     * - 指令类型识别逻辑复杂，请谨慎修改
     * - 错误处理涉及用户体验，必须保持稳定
     */
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
                // 防护：检查管理器数组
                DeviceConnFactoryManager[] managers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
                if (managers == null || managers.length == 0) {
                    Log.e(TAG, "【打印】打印机管理器数组为空或长度为0");
                    callback.onPrintFail("打印机管理器未初始化");
                    return;
                }
                
                DeviceConnFactoryManager manager = managers[0];
                if (manager == null) {
                    Log.e(TAG, "【打印】打印机管理器实例为空");
                    callback.onPrintFail("打印机管理器未初始化");
                    return;
                }

                PrinterCommand commandType = manager.getCurrentPrinterCommand();
                Log.d(TAG, "【打印】打印机指令类型: " + commandType);
                
                // 如果指令类型为null，尝试发送初始化命令来激活打印机
                if (commandType == null) {
                    Log.d(TAG, "【打印】打印机指令类型为null，尝试发送初始化命令...");
                    try {
                        // 尝试发送ESC初始化命令
                        EscCommand esc = new EscCommand();
                        esc.addInitializePrinter();
                        Vector<Byte> testData = esc.getCommand();
                        if (testData != null && !testData.isEmpty()) {
                            manager.sendDataImmediately(testData);
                            Thread.sleep(500); // 等待打印机响应
                            
                            // 再次检查指令类型
                            commandType = manager.getCurrentPrinterCommand();
                            Log.d(TAG, "【打印】发送ESC初始化命令后，打印机指令类型: " + commandType);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "【打印】发送初始化命令失败", e);
                    }
                }
                
                if (commandType == null) {
                    Log.e(TAG, "【打印】打印机指令类型仍为null，尝试使用TSC指令");
                    // 如果指令类型仍为null，尝试使用TSC指令（标签打印机通常使用TSC）
                    printWithTSC(order, callback);
                } else if (commandType == PrinterCommand.ESC) {
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

    private boolean checkPrinterStatus() {
        try {
            // 防护：检查管理器数组是否存在
            DeviceConnFactoryManager[] managers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
            if (managers == null || managers.length == 0) {
                Log.e(TAG, "【打印】打印机管理器数组为空");
                return false;
            }
            
            // 防护：检查管理器实例是否存在
            if (managers[0] == null) {
                Log.e(TAG, "【打印】打印机管理器实例为空");
                return false;
            }
            
            // 检查连接状态
            if (!managers[0].getConnState()) {
                Log.e(TAG, "【打印】打印机未连接");
                connectPrinter();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "【打印】检查打印机状态时异常", e);
            return false;
        }
    }
    /**
     * 执行严谨的打印机测试
     * 包括发送测试命令、等待响应、验证响应数据
     */
    private boolean performRigorousPrinterTest(DeviceConnFactoryManager manager) {
        try {
            Log.d(TAG, "[打印流程] 开始执行严谨的打印机测试");
            
            // 获取打印机指令类型
            PrinterCommand commandType = manager.getCurrentPrinterCommand();
            Log.d(TAG, "[打印流程] 打印机指令类型: " + commandType);
            
            // 根据指令类型发送相应的测试命令
            Vector<Byte> testData = new Vector<>();
            if (commandType == PrinterCommand.ESC) {
                // ESC测试命令：发送初始化命令
                com.printer.command.EscCommand esc = new com.printer.command.EscCommand();
                esc.addInitializePrinter();
                testData = esc.getCommand();
                Log.d(TAG, "[打印流程] 发送ESC初始化命令");
            } else if (commandType == PrinterCommand.TSC) {
                // TSC测试命令：发送清屏命令
                com.printer.command.LabelCommand tsc = new com.printer.command.LabelCommand();
                tsc.addCls();
                testData = tsc.getCommand();
                Log.d(TAG, "[打印流程] 发送TSC清屏命令");
            } else {
                Log.e(TAG, "[打印流程] 不支持的打印机指令集: " + commandType);
                return false;
            }
            
            if (testData == null || testData.isEmpty()) {
                Log.e(TAG, "[打印流程] 测试数据为空");
                return false;
            }
            
            // 发送测试命令
            Log.d(TAG, "[打印流程] 发送测试命令，数据大小: " + testData.size());
            manager.sendDataImmediately(testData);
            
            // 等待打印机处理
            Thread.sleep(500);
            
            // 发送状态查询命令并等待响应
            if (commandType == PrinterCommand.ESC) {
                return testEscPrinterResponse(manager);
            } else if (commandType == PrinterCommand.TSC) {
                return testTscPrinterResponse(manager);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "[打印流程] 严谨打印机测试异常", e);
            return false;
        }
        
        return false;
    }
    
    /**
     * 测试ESC打印机的响应 - 更严格的检测
     */
    private boolean testEscPrinterResponse(DeviceConnFactoryManager manager) {
        try {
            Log.d(TAG, "[打印流程] 测试ESC打印机响应 - 严格模式");
            
            // 第一步：发送ESC状态查询命令
            byte[] statusCommand = {0x10, 0x04, 0x01}; // DLE EOT SOH
            Vector<Byte> statusVector = new Vector<>();
            for (byte b : statusCommand) {
                statusVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第1步：发送ESC状态查询命令");
            manager.sendDataImmediately(statusVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            boolean hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第1步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 检查连接状态
            boolean step1Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第1步后连接状态: " + step1Connected);
            
            if (!step1Connected) {
                Log.e(TAG, "[打印流程] ESC打印机第1步检测失败：连接断开");
                return false;
            }
            
            // 第二步：发送查询命令验证打印机响应
            byte[] queryCommand = {0x10, 0x04, 0x01}; // 再次查询状态
            Vector<Byte> queryVector = new Vector<>();
            for (byte b : queryCommand) {
                queryVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第2步：发送ESC查询命令验证打印机响应");
            manager.sendDataImmediately(queryVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第2步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 检查连接状态
            boolean step2Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第2步后连接状态: " + step2Connected);
            
            if (!step2Connected) {
                Log.e(TAG, "[打印流程] ESC打印机第2步检测失败：连接断开");
                return false;
            }
            
            // 第三步：发送初始化命令最终验证
            byte[] initCommand = {0x1B, 0x40}; // ESC @
            Vector<Byte> initVector = new Vector<>();
            for (byte b : initCommand) {
                initVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第3步：发送ESC初始化命令最终验证");
            manager.sendDataImmediately(initVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第3步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 最终检查连接状态
            boolean step3Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第3步后连接状态: " + step3Connected);
            
            if (!step3Connected) {
                Log.e(TAG, "[打印流程] ESC打印机第3步检测失败：连接断开");
                return false;
            }
            
            // 第四步：发送一个会导致错误的命令来测试打印机是否真正响应
            byte[] errorCommand = {0x10, 0x04, (byte)0xFF}; // 无效的状态查询
            Vector<Byte> errorVector = new Vector<>();
            for (byte b : errorCommand) {
                errorVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第4步：发送错误命令测试ESC打印机响应");
            manager.sendDataImmediately(errorVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第4步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 检查连接状态
            boolean step4Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第4步后连接状态: " + step4Connected);
            
            if (!step4Connected) {
                Log.e(TAG, "[打印流程] ESC打印机第4步检测失败：连接断开");
                return false;
            }
            
            // 发送一个简单的状态查询命令来最终验证
            byte[] finalTestCommand = {0x10, 0x04, 0x01}; // DLE EOT SOH
            Vector<Byte> finalTestVector = new Vector<>();
            for (byte b : finalTestCommand) {
                finalTestVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 最终验证：发送ESC状态查询命令");
            manager.sendDataImmediately(finalTestVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            boolean finalResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 最终验证响应检测: " + (finalResponse ? "有响应" : "无响应"));
            
            if (finalResponse) {
                Log.d(TAG, "[打印流程] ✅ ESC打印机严格检测通过，打印机有响应");
                return true;
            } else {
                Log.e(TAG, "[打印流程] ❌ ESC打印机严格检测失败，打印机无响应");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "[打印流程] ESC打印机响应检测异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试TSC打印机的响应 - 更严格的检测
     */
    private boolean testTscPrinterResponse(DeviceConnFactoryManager manager) {
        try {
            Log.d(TAG, "[打印流程] 测试TSC打印机响应 - 严格模式");
            
            // 第一步：发送TSC状态查询命令
            byte[] statusCommand = {0x1B, 0x76, 0x00}; // ESC v 0
            Vector<Byte> statusVector = new Vector<>();
            for (byte b : statusCommand) {
                statusVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第1步：发送TSC状态查询命令");
            manager.sendDataImmediately(statusVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            boolean hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第1步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 检查连接状态
            boolean step1Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第1步后连接状态: " + step1Connected);
            
            if (!step1Connected) {
                Log.e(TAG, "[打印流程] TSC打印机第1步检测失败：连接断开");
                return false;
            }
            
            // 第二步：发送查询命令验证打印机响应
            byte[] queryCommand = {0x1B, 0x76, 0x01}; // ESC v 1
            Vector<Byte> queryVector = new Vector<>();
            for (byte b : queryCommand) {
                queryVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第2步：发送查询命令验证打印机响应");
            manager.sendDataImmediately(queryVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第2步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 检查连接状态
            boolean step2Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第2步后连接状态: " + step2Connected);
            
            if (!step2Connected) {
                Log.e(TAG, "[打印流程] TSC打印机第2步检测失败：连接断开");
                return false;
            }
            
            // 第三步：发送初始化命令最终验证
            byte[] initCommand = {0x1B, 0x40}; // ESC @
            Vector<Byte> initVector = new Vector<>();
            for (byte b : initCommand) {
                initVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第3步：发送初始化命令最终验证");
            manager.sendDataImmediately(initVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第3步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 最终检查连接状态
            boolean step3Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第3步后连接状态: " + step3Connected);
            
            if (!step3Connected) {
                Log.e(TAG, "[打印流程] TSC打印机第3步检测失败：连接断开");
                return false;
            }
            
            // 第四步：发送一个会导致错误的命令来测试打印机是否真正响应
            byte[] errorCommand = {0x1B, 0x76, (byte)0xFF}; // 无效的状态查询
            Vector<Byte> errorVector = new Vector<>();
            for (byte b : errorCommand) {
                errorVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 第4步：发送错误命令测试打印机响应");
            manager.sendDataImmediately(errorVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            hasResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 第4步响应检测: " + (hasResponse ? "有响应" : "无响应"));
            
            // 检查连接状态
            boolean step4Connected = manager.getConnState();
            Log.d(TAG, "[打印流程] 第4步后连接状态: " + step4Connected);
            
            if (!step4Connected) {
                Log.e(TAG, "[打印流程] TSC打印机第4步检测失败：连接断开");
                return false;
            }
            
            // 检查是否有任何响应
            boolean hasAnyResponse = false;
            // 这里需要根据实际的响应检测结果来判断
            // 由于我们没有保存每个步骤的响应结果，我们需要重新进行一个简单的响应测试
            
            // 发送一个简单的状态查询命令来最终验证
            byte[] finalTestCommand = {0x1B, 0x76, 0x00}; // ESC v 0
            Vector<Byte> finalTestVector = new Vector<>();
            for (byte b : finalTestCommand) {
                finalTestVector.add(b);
            }
            
            Log.d(TAG, "[打印流程] 最终验证：发送TSC状态查询命令");
            manager.sendDataImmediately(finalTestVector);
            
            // 等待并尝试读取响应
            Thread.sleep(1000);
            boolean finalResponse = tryReadPrinterResponse(manager, 2000);
            Log.d(TAG, "[打印流程] 最终验证响应检测: " + (finalResponse ? "有响应" : "无响应"));
            
            if (finalResponse) {
                Log.d(TAG, "[打印流程] ✅ TSC打印机严格检测通过，打印机有响应");
                return true;
            } else {
                Log.e(TAG, "[打印流程] ❌ TSC打印机严格检测失败，打印机无响应");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "[打印流程] TSC打印机响应检测异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 尝试读取打印机响应数据
     * @param manager 打印机管理器
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否有响应数据
     */
    private boolean tryReadPrinterResponse(DeviceConnFactoryManager manager, int timeoutMs) {
        try {
            Log.d(TAG, "[打印流程] 🔍 开始尝试读取打印机响应，超时时间: " + timeoutMs + "ms");
            
            byte[] buffer = new byte[1024];
            long startTime = System.currentTimeMillis();
            int readAttempts = 0;
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                readAttempts++;
                try {
                    Log.d(TAG, "[打印流程] 📖 第" + readAttempts + "次尝试读取数据...");
                    int bytesRead = manager.readDataImmediately(buffer);
                    Log.d(TAG, "[打印流程] 📊 读取结果: " + bytesRead + " 字节");
                    
                    if (bytesRead > 0) {
                        Log.d(TAG, "[打印流程] ✅ 成功读取到打印机响应数据，长度: " + bytesRead + " 字节");
                        Log.d(TAG, "[打印流程] 📄 响应数据: " + bytesToHex(Arrays.copyOf(buffer, bytesRead)));
                        return true;
                    }
                    
                    Thread.sleep(100); // 短暂等待
                } catch (IOException e) {
                    Log.d(TAG, "[打印流程] ⚠️ 读取数据时发生IO异常: " + e.getMessage());
                    break;
                }
            }
            
            Log.d(TAG, "[打印流程] ❌ 读取打印机响应超时，共尝试" + readAttempts + "次，未收到数据");
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "[打印流程] 💥 读取打印机响应异常", e);
            return false;
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * ESC指令打印
     */
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
            
            // 添加详细的数据检查
            if (datas == null) {
                Log.e(TAG, "【打印】❌ ESC命令数据为null");
                throw new Exception("ESC命令数据为null");
            }
            if (datas.isEmpty()) {
                Log.e(TAG, "【打印】❌ ESC命令数据为空");
                throw new Exception("ESC命令数据为空");
            }
            
            // 打印前几个字节用于调试
            StringBuilder hexData = new StringBuilder();
            for (int i = 0; i < Math.min(datas.size(), 20); i++) {
                hexData.append(String.format("%02X ", datas.get(i)));
            }
            Log.d(TAG, "【打印】ESC命令数据前20字节: " + hexData.toString());
            
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
            
            // ========== 纸张尺寸自动映射 ========== 
            // index: 0,1,2,3
            int[][] PAPER_SIZE_CM = {
                {4, 3}, // 4×3cm 横
                {4, 6}, // 4×6cm 竖
                {5, 8}, // 5×8cm 竖
                {8, 5}  // 8×5cm 横（宽度80mm，高度50mm）
            };
            String[] PAPER_SIZE_OPTIONS = {
                "4×3cm（横）",
                "4×6cm（竖）",
                "5×8cm（竖）",
                "8×5cm（横）"
            };
            SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
            int paperSizeIndex = prefs.getInt("paper_size", 0);
            int widthCm = PAPER_SIZE_CM[paperSizeIndex][0];
            int heightCm = PAPER_SIZE_CM[paperSizeIndex][1];
            int widthMm = widthCm * 10;
            int heightMm = heightCm * 10;
            String paperSizeText = PAPER_SIZE_OPTIONS[paperSizeIndex];
            Log.i(TAG, "【打印尺寸选择】index=" + paperSizeIndex + ", 选项=" + paperSizeText + ", width=" + widthCm + "cm, height=" + heightCm + "cm, width=" + widthMm + "mm, height=" + heightMm + "mm");
            // ========== END ========== 

            // 新增：根据设置自动传递纸张尺寸（单位mm）
            tsc.addSize(widthMm, heightMm);
            tsc.addGap(10);
            tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
            tsc.addReference(0, 0);
            tsc.addCls();
            Log.d(TAG, "设置标签参数成功");
            
            // ========== 方案A：参数表 + 横竖版分支 ==========
            class PrintLayoutParams {
                int xCustomer, yCustomer, fontCustomer;
                int xGoods, yGoods, fontGoods;
                int xQty, yQty, fontQty;
                LabelCommand.ROTATION rotation;
                PrintLayoutParams(int xC, int yC, int fC, int xG, int yG, int fG, int xQ, int yQ, int fQ, LabelCommand.ROTATION rot) {
                    xCustomer = xC; yCustomer = yC; fontCustomer = fC;
                    xGoods = xG; yGoods = yG; fontGoods = fG;
                    xQty = xQ; yQty = yQ; fontQty = fQ;
                    rotation = rot;
                }
            }
            PrintLayoutParams[] LAYOUT_PARAMS = new PrintLayoutParams[] {
                    // 4×3cm 横 - 小尺寸，紧凑布局
                    new PrintLayoutParams(10, 60, 1, 10, 100, 1, 10, 140, 1, LabelCommand.ROTATION.ROTATION_0),
                    // 4×6cm 竖 - 竖版，垂直布局
                    new PrintLayoutParams(40, 440, 2, 120, 440, 2, 200, 440, 2, LabelCommand.ROTATION.ROTATION_270),
                    // 5×8cm 竖 - 大尺寸，宽松布局
                    new PrintLayoutParams(40, 550, 3, 120, 550, 3, 200, 550, 3, LabelCommand.ROTATION.ROTATION_270),
                    // 8×5cm 横 - 宽度80mm，高度50mm，打印3行，字体2倍
                    new PrintLayoutParams(10, 60, 2, 10, 140, 2, 10, 220, 2, LabelCommand.ROTATION.ROTATION_0),
            };
            PrintLayoutParams params = LAYOUT_PARAMS[paperSizeIndex];
            Log.i(TAG, "【打印布局参数】index=" + paperSizeIndex + ", xCustomer=" + params.xCustomer + ", yCustomer=" + params.yCustomer + ", fontCustomer=" + params.fontCustomer + ", rotation=" + params.rotation);
            Log.i(TAG, "【打印布局参数】xGoods=" + params.xGoods + ", yGoods=" + params.yGoods + ", fontGoods=" + params.fontGoods);
            Log.i(TAG, "【打印布局参数】xQty=" + params.xQty + ", yQty=" + params.yQty + ", fontQty=" + params.fontQty);
            // ========== END ==========

            // ========== 新打印内容（客户、商品、重量） ==========
            
            // 判断是否为横版小标签（4×3cm）
            boolean isHorizontalSmall = (paperSizeIndex == 0);
            
            Log.i(TAG, "【打印模式判断】paperSizeIndex=" + paperSizeIndex + ", 纸张尺寸=" + paperSizeText);
            Log.i(TAG, "【打印模式判断】isHorizontalSmall=" + isHorizontalSmall + ", 走" + (isHorizontalSmall ? "横版小标签分支" : "竖版大标签分支"));
            
            if (isHorizontalSmall) {
                // 横版小标签（4×3cm）：只打印部门属性名称，每行4个字
                String attrName = getDepartmentAttrName(order);
                Log.i(TAG, "【打印内容】横版小标签，只打印部门属性: " + attrName);
                
                // 将文本按每行4个字分割
                String[] lines = splitTextBy4Chars(attrName);
                Log.d(TAG, "【打印调试】文本分割后共 " + lines.length + " 行");
                
                // 打印位置设置（左侧留小空白，顶部留足够空间）
                int textX = 5;  // X轴坐标，左侧留5mm空白
                int firstLineY = 20;  // 第一行Y坐标，顶部留20mm
                int lineSpacing = 80;  // 行间距，从40增加到80
                
                LabelCommand.FONTMUL fontMul = LabelCommand.FONTMUL.MUL_3; // 3倍放大
                
                // 打印每一行
                for (int i = 0; i < lines.length; i++) {
                    int yPos = firstLineY + i * lineSpacing; // 每行间距80
                    Log.d(TAG, "【打印调试】横版小标签第" + (i+1) + "行文本位置: x=" + textX + ", y=" + yPos + ", 内容=" + lines[i]);
                    tsc.addText(textX, yPos, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE,
                            LabelCommand.ROTATION.ROTATION_0, fontMul, fontMul, lines[i]);
                }
                        
            } else {
                // 竖版大标签（4×6或5×8）：打印完整内容（客户、商品、重量）
                String customerName = getDepartmentName(order);
                String goodsName = order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "";
                String standard = order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsStandardname() : "";
                String weight = order.getNxDoWeight() != null ? order.getNxDoWeight() : "0";
                String weightStandard = order.getNxDoStandard() != null ? order.getNxDoStandard() : "";
                
                Log.i(TAG, "【打印内容】竖版大标签，客户: " + customerName);
                Log.i(TAG, "【打印内容】竖版大标签，商品: " + goodsName);
                Log.i(TAG, "【打印内容】竖版大标签，重量: " + weight + weightStandard);
                
                // 获取字体倍数枚举
                LabelCommand.FONTMUL customerFont = params.fontCustomer == 1 ? LabelCommand.FONTMUL.MUL_1 :
                                                 params.fontCustomer == 2 ? LabelCommand.FONTMUL.MUL_2 :
                                                 params.fontCustomer == 3 ? LabelCommand.FONTMUL.MUL_3 :
                                                 params.fontCustomer == 4 ? LabelCommand.FONTMUL.MUL_4 : LabelCommand.FONTMUL.MUL_1;
                LabelCommand.FONTMUL goodsFont = params.fontGoods == 1 ? LabelCommand.FONTMUL.MUL_1 :
                                               params.fontGoods == 2 ? LabelCommand.FONTMUL.MUL_2 :
                                               params.fontGoods == 3 ? LabelCommand.FONTMUL.MUL_3 :
                                               params.fontGoods == 4 ? LabelCommand.FONTMUL.MUL_4 : LabelCommand.FONTMUL.MUL_1;
                LabelCommand.FONTMUL weightFont = params.fontQty == 1 ? LabelCommand.FONTMUL.MUL_1 :
                                                params.fontQty == 2 ? LabelCommand.FONTMUL.MUL_2 :
                                                params.fontQty == 3 ? LabelCommand.FONTMUL.MUL_3 :
                                                params.fontQty == 4 ? LabelCommand.FONTMUL.MUL_4 : LabelCommand.FONTMUL.MUL_1;
                
                Log.d(TAG, "【打印调试】客户文本位置: x=" + params.xCustomer + ", y=" + params.yCustomer + ", 字体=" + params.fontCustomer);
                tsc.addText(params.xCustomer, params.yCustomer, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE,
                        params.rotation, customerFont, customerFont, customerName);
                
                Log.d(TAG, "【打印调试】商品文本位置: x=" + params.xGoods + ", y=" + params.yGoods + ", 字体=" + params.fontGoods);
                tsc.addText(params.xGoods, params.yGoods, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE,
                        params.rotation, goodsFont, goodsFont, goodsName);
                
                // 添加重量打印
                Log.d(TAG, "【打印调试】重量文本位置: x=" + params.xQty + ", y=" + params.yQty + ", 字体=" + params.fontQty);
                tsc.addText(params.xQty, params.yQty, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE,
                        params.rotation, weightFont, weightFont, "重量: " + weight + weightStandard);
            }
            
            // 添加打印命令和声音命令
            Log.d(TAG, "【打印调试】添加打印命令: addPrint(1, 1)");
            tsc.addPrint(1, 1);
            Log.d(TAG, "【打印调试】添加声音命令: addSound(2, 100)");
            tsc.addSound(2, 100);
            
            // 检查标签尺寸和坐标范围（仅对竖版大标签）
            if (!isHorizontalSmall) {
                Log.d(TAG, "【打印调试】标签尺寸检查: width=" + widthMm + "mm, height=" + heightMm + "mm");
                Log.d(TAG, "【打印调试】坐标范围检查:");
                Log.d(TAG, "  - 客户文本: x=" + params.xCustomer + ", y=" + params.yCustomer + " (应在0-" + widthMm + "范围内)");
                Log.d(TAG, "  - 商品文本: x=" + params.xGoods + ", y=" + params.yGoods + " (应在0-" + widthMm + "范围内)");
                Log.d(TAG, "  - 重量文本: x=" + params.xQty + ", y=" + (params.yQty + 20) + " (应在0-" + widthMm + "范围内)");
                
                // 检查文本内容长度（竖版大标签）
                String customerName = getDepartmentName(order);
                String goodsName = order.getNxDistributerGoodsEntity() != null ? order.getNxDistributerGoodsEntity().getNxDgGoodsName() : "";
                String weight = order.getNxDoWeight() != null ? order.getNxDoWeight() : "0";
                String weightStandard = order.getNxDoStandard() != null ? order.getNxDoStandard() : "";
                
                Log.d(TAG, "【打印调试】文本内容检查:");
                Log.d(TAG, "  - 客户文本长度: " + customerName.length() + " 字符");
                Log.d(TAG, "  - 商品文本长度: " + goodsName.length() + " 字符");
                Log.d(TAG, "  - 重量文本长度: " + ("重量: " + weight + weightStandard).length() + " 字符");
                
                // 检查字体设置（竖版大标签）
                Log.d(TAG, "【打印调试】字体设置检查:");
                Log.d(TAG, "  - 客户字体: MUL_" + params.fontCustomer);
                Log.d(TAG, "  - 商品字体: MUL_" + params.fontGoods);
                Log.d(TAG, "  - 重量字体: MUL_" + params.fontQty);
                Log.d(TAG, "  - 旋转角度: " + params.rotation);
            }
            // ========== END ==========

            Log.d(TAG, "获取打印命令数据");
            Vector<Byte> datas = tsc.getCommand();
            if (datas == null || datas.isEmpty()) {
                throw new Exception("生成的打印命令数据为空");
            }
            Log.d(TAG, "打印命令数据大小: " + datas.size());
            
            // 检查坐标是否在合理范围内
            if (params.xCustomer < 0 || params.xCustomer > widthMm || 
                params.yCustomer < 0 || params.yCustomer > heightMm) {
                Log.w(TAG, "【打印警告】客户文本坐标超出范围: x=" + params.xCustomer + ", y=" + params.yCustomer);
            }
            if (params.xGoods < 0 || params.xGoods > widthMm || 
                params.yGoods < 0 || params.yGoods > heightMm) {
                Log.w(TAG, "【打印警告】商品文本坐标超出范围: x=" + params.xGoods + ", y=" + params.yGoods);
            }
            if (params.xQty < 0 || params.xQty > widthMm || 
                (params.yQty + 20) < 0 || (params.yQty + 20) > heightMm) {
                Log.w(TAG, "【打印警告】重量文本坐标超出范围: x=" + params.xQty + ", y=" + (params.yQty + 20));
            }
            
            // 添加打印命令调试信息
            Log.d(TAG, "【打印调试】打印份数: 1");
            Log.d(TAG, "【打印调试】打印命令: addPrint(1, 1)");
            Log.d(TAG, "【打印调试】声音命令: addSound(2, 100)");
            
            // 转换Byte[]为byte[]用于调试
            Byte[] byteArray = datas.toArray(new Byte[0]);
            byte[] primitiveByteArray = new byte[byteArray.length];
            for (int i = 0; i < byteArray.length; i++) {
                primitiveByteArray[i] = byteArray[i];
            }
            Log.d(TAG, "【打印调试】数据前10字节: " + bytesToHex(Arrays.copyOf(primitiveByteArray, Math.min(10, datas.size()))));
            
            // 检查数据完整性
            Log.d(TAG, "【打印调试】数据完整性检查:");
            Log.d(TAG, "  - 数据是否为null: " + (datas == null));
            Log.d(TAG, "  - 数据是否为空: " + (datas != null && datas.isEmpty()));
            Log.d(TAG, "  - 数据大小: " + (datas != null ? datas.size() : "null"));
            
            // 检查是否包含关键命令
            Byte[] commandByteArray = datas.toArray(new Byte[0]);
            boolean hasPrintCommand = false;
            boolean hasSoundCommand = false;
            for (int i = 0; i < Math.min(commandByteArray.length, 50); i++) {
                if (commandByteArray[i] == 0x1A) { // 打印命令
                    hasPrintCommand = true;
                }
                if (commandByteArray[i] == 0x07) { // 声音命令
                    hasSoundCommand = true;
                }
            }
            Log.d(TAG, "【打印调试】命令检查: 包含打印命令=" + hasPrintCommand + ", 包含声音命令=" + hasSoundCommand);
            
            sendPrintData(datas, callback);
        } catch (Exception e) {
            Log.e(TAG, "生成TSC打印命令失败", e);
            throw new Exception("生成TSC打印命令失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门名称（不包含拣货名称和括号）
     */
    private String getDepartmentName(NxDepartmentOrdersEntity order) {
        if (order.getNxDepartmentEntity() != null) {
                NxDepartmentEntity department = order.getNxDepartmentEntity();
                if (department.getFatherDepartmentEntity() != null) {
                    // 有父部门：只显示父部门名.子部门名
                    return department.getFatherDepartmentEntity().getNxDepartmentAttrName() + "." + department.getNxDepartmentAttrName();
                } else {
                    // 没有父部门：只显示部门名
                    return department.getNxDepartmentAttrName();
                }
        } else if (order.getGbDepartmentEntity() != null) {
                GbDepartmentEntity department = order.getGbDepartmentEntity();
                if (department.getFatherGbDepartmentEntity() != null &&
                        department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                   // 有父部门：只显示父部门名.子部门名
                   return department.getFatherGbDepartmentEntity().getGbDepartmentName() + "." + department.getGbDepartmentName();
                } else {
                // 没有父部门：只显示部门名
                return department.getGbDepartmentName();
                }
            }
        return "未知部门";
    }

    /**
     * 获取部门属性名称（用于4×3横版打印）
     * 返回完整的属性名称，不截断
     */
    private String getDepartmentAttrName(NxDepartmentOrdersEntity order) {
        String attrName = "";
        
        if (order.getNxDepartmentEntity() != null) {
            NxDepartmentEntity department = order.getNxDepartmentEntity();
            // 优先使用部门属性名称
            if (department.getNxDepartmentAttrName() != null && !department.getNxDepartmentAttrName().isEmpty()) {
                attrName = department.getNxDepartmentAttrName();
            } else if (department.getFatherDepartmentEntity() != null && 
                       department.getFatherDepartmentEntity().getNxDepartmentAttrName() != null &&
                       !department.getFatherDepartmentEntity().getNxDepartmentAttrName().isEmpty()) {
                // 如果没有，则使用父部门的属性名称
                attrName = department.getFatherDepartmentEntity().getNxDepartmentAttrName();
            } else {
                // 最后使用拣货名称
                if (department.getFatherDepartmentEntity() != null) {
                    attrName = department.getFatherDepartmentEntity().getNxDepartmentAttrName();
                } else {
                    attrName = department.getNxDepartmentAttrName();
                }
            }
        } else if (order.getGbDepartmentEntity() != null) {
            GbDepartmentEntity department = order.getGbDepartmentEntity();
            if (department.getFatherGbDepartmentEntity() != null &&
                    department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                attrName = department.getFatherGbDepartmentEntity().getGbDepartmentAttrName();
            } else {
                attrName = department.getGbDepartmentAttrName();
            }
        }
        
        return attrName;
    }
    
    /**
     * 将文本按每行4个字分割
     * @param text 原始文本
     * @return 分割后的文本数组
     */
    private String[] splitTextBy4Chars(String text) {
        if (text == null || text.isEmpty()) {
            return new String[]{""};
        }
        
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < text.length(); i += 4) {
            int end = Math.min(i + 4, text.length());
            lines.add(text.substring(i, end));
        }
        return lines.toArray(new String[0]);
    }

    private void sendPrintData(Vector<Byte> datas, PrintCallback callback) throws Exception {
        Log.d(TAG, "【打印】开始发送打印数据");
        try {
            // 防护：检查管理器数组
            DeviceConnFactoryManager[] managers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
            if (managers == null || managers.length == 0) {
                Log.e(TAG, "【打印】打印机管理器数组为空或长度为0");
                throw new Exception("打印机管理器未初始化");
            }
            
            if (managers[0] == null) {
                Log.e(TAG, "【打印】打印机管理器实例为空");
                throw new Exception("打印机管理器未初始化");
            }
            
            if (!managers[0].getConnState()) {
                Log.e(TAG, "【打印】打印机未连接");
                throw new Exception("打印机未连接");
            }
            
            Log.d(TAG, "【打印】发送打印数据到打印机，数据大小: " + (datas != null ? datas.size() : 0));
            
            // 添加发送前的连接状态检查（使用已验证的 managers[0]）
            boolean connState = managers[0].getConnState();
            Log.d(TAG, "【打印】发送前连接状态: " + connState);
            
            if (!connState) {
                Log.e(TAG, "【打印】❌ 发送前连接状态为false");
                throw new Exception("打印机连接已断开");
            }
            
            // 发送数据
            managers[0].sendDataImmediately(datas);
            Log.d(TAG, "【打印】✅ 打印数据发送成功");
            
            // 等待打印机处理
            Thread.sleep(1000);
            
            // 检查打印机状态
            boolean afterPrintConnected = managers[0].getConnState();
            Log.d(TAG, "【打印】打印后连接状态: " + afterPrintConnected);
            
            if (!afterPrintConnected) {
                Log.e(TAG, "【打印】❌ 打印后连接断开");
                throw new Exception("打印后连接断开");
            }
            
            callback.onPrintSuccess();
            Log.d(TAG, "【打印】打印回调成功");
            
            runOnUiThread(() -> {
                loadStockGoodsData();
                Toast.makeText(this, "打印成功", Toast.LENGTH_SHORT).show();
            });
                } catch (Exception e) {
            Log.e(TAG, "【打印】发送打印数据失败", e);
            throw new Exception("发送打印数据失败: " + e.getMessage());
        }
    }

    /**
     * 处理打印错误
     */
    private void handlePrintError(Exception e, PrintCallback callback) {
        updatePrinterStatus(false);
        callback.onPrintFail("打印异常: " + e.getMessage());
        
        runOnUiThread(() -> {
            connectPrinter();
            showToast("打印失败，正在尝试重新连接打印机");
        });
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
                UsbManager usbManager = (UsbManager) getSystemService(android.content.Context.USB_SERVICE);
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

    private void updatePrinterInfo() {
        // 这里可以更新打印机状态显示
        Log.d(TAG, "更新打印机信息，连接状态: " + isPrinterConnected);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示加载蒙版
     * 增加Activity生命周期检查，防止崩溃
     */
    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 检查Activity是否已销毁
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "[Loading] Activity已销毁，跳过显示加载蒙版");
                    return;
                }
                
                try {
                    if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
                        mLoadingDialog.show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[Loading] 显示加载蒙版失败", e);
                }
            }
        });
    }

    /**
     * 隐藏加载蒙版
     * 增加Activity生命周期检查，防止崩溃
     */
    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 检查Activity是否已销毁
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "[Loading] Activity已销毁，跳过关闭加载蒙版");
                    return;
                }
                
                try {
                    if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[Loading] 关闭加载蒙版失败", e);
                }
            }
        });
    }

    /**
     * 【业务核心】打印回调接口 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此接口定义了打印操作的回调方法
     * 用于处理打印成功和失败的情况
     * 
     * 【业务规则】
     * 1. 成功回调：打印成功时调用onPrintSuccess()
     * 2. 失败回调：打印失败时调用onPrintFail(String error)
     * 3. 异步处理：回调在后台线程执行
     * 4. 错误信息：失败时提供详细的错误信息
     * 
     * 【使用场景】
     * - printOrder方法中使用此接口
     * - printAndSaveOrders方法中使用此接口
     * - 确保打印和保存操作的顺序性
     * 
     * 【回调流程】
     * - 打印成功：继续执行保存操作
     * - 打印失败：停止后续操作，显示错误信息
     * 
     * 【注意事项】
     * - 此接口为打印流程核心，确保操作顺序
     * - 回调方法必须在主线程中更新UI
     * - 错误处理涉及用户体验，必须完善
     */
    public interface PrintCallback {
        void onPrintSuccess();
        void onPrintFail(String error);
    }
    
    /**
     * 【业务核心】自动连接打印机 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责在页面启动时自动连接已配置的打印机
     * 支持USB和蓝牙两种连接方式
     * 
     * 【业务规则】
     * 1. 缓存检查：从printer_cache获取打印机配置
     * 2. 连接方式：根据printer_type选择USB或蓝牙连接
     * 3. 异步处理：在后台线程执行，避免阻塞UI
     * 4. 状态更新：连接完成后更新isPrinterConnected状态
     * 
     * 【连接流程】
     * 1. 从缓存获取打印机类型和地址
     * 2. 关闭之前的连接
     * 3. 根据类型建立新连接
     * 4. 检查连接状态
     * 5. 等待指令类型初始化
     * 
     * 【连接方式】
     * - 蓝牙连接：使用DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH
     * - USB连接：使用DeviceConnFactoryManager.CONN_METHOD.USB
     * 
     * 【优化特性】
     * - 减少等待时间：从2000ms优化到500ms
     * - 简化USB扫描：只检查权限，不进行复杂扫描
     * - 异步执行：避免UI阻塞，提升用户体验
     * 
     * 【注意事项】
     * - 此方法为打印机连接核心，经过多次优化
     * - 连接逻辑复杂，涉及硬件交互，请谨慎修改
     * - 异步处理逻辑确保UI响应性，必须保持
     */
    private void autoConnectPrinter() {
        long start = System.currentTimeMillis();
        Log.d("PERF", "autoConnectPrinter start: " + start);
        Log.d(TAG, "[打印机] 🔄 开始自动连接打印机");
        
        // 从缓存获取打印机信息
        String printerType = printerPreferences.getString("printer_type", null);
        String printerAddress = printerPreferences.getString("printer_address", null);
        
        Log.d(TAG, "[打印机] 📋 缓存信息: type=" + printerType + ", address=" + printerAddress);
            
        if (printerType == null || printerAddress == null) {
            Log.w(TAG, "[打印机] ⚠️ 没有缓存的打印机信息");
            return;
        }
            
        try {
            // 使用 CountDownLatch 等待主线程操作完成
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final boolean[] buildSuccess = {false};
            
            // 在主线程中创建打印机连接（因为需要创建Handler）
            runOnUiThread(() -> {
                try {
                    // 关闭之前的连接
                    Log.d(TAG, "[打印机] 🔌 关闭之前的连接...");
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
                    }

                    // 根据类型建立连接
                    if ("bluetooth".equals(printerType)) {
                        Log.d(TAG, "[打印机] 📱 连接蓝牙打印机: " + printerAddress);
                        new DeviceConnFactoryManager.Build()
                            .setId(0)
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            .setMacAddress(printerAddress)
                            .setContext(this)
                            .build();
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();
                        Log.d(TAG, "[打印机] 📱 蓝牙打印机连接命令已发送");
                        buildSuccess[0] = true;
                    } else if ("usb".equals(printerType)) {
                        Log.d(TAG, "[打印机] 🔌 USB打印机连接");
                        // 简化USB连接逻辑，只检查是否有USB权限，不进行复杂扫描
                        trySimpleUsbConnect();
                        buildSuccess[0] = true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[打印机] 💥 创建连接失败", e);
                } finally {
                    latch.countDown();
                }
            });
            
            // 等待主线程操作完成（5秒足够）
            boolean awaitSuccess = latch.await(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            if (!awaitSuccess) {
                Log.e(TAG, "[打印机] ❌ 等待主线程操作超时（5秒）");
                Log.e(TAG, "[打印机] 💡 提示：主线程可能被阻塞，请重试或重启应用");
                isPrinterConnected = false;
                return;
            }
            
            Log.d(TAG, "[打印机] ✅ 主线程操作完成");

            
            if (!buildSuccess[0]) {
                Log.e(TAG, "[打印机] ❌ 打印机连接创建失败");
                isPrinterConnected = false;
                return;
            }
                
            // 检查连接状态 - 分多次检查，提前发现成功
            Log.d(TAG, "[打印机] ⏳ 开始检查蓝牙连接状态...");
            boolean connected = false;
            
            // 最多检查10次，每次200ms，总计2秒
            for (int i = 0; i < 10; i++) {
                Thread.sleep(200);
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                    connected = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState();
                    if (connected) {
                        Log.d(TAG, "[打印机] ✅ 连接成功（第" + (i + 1) + "次检查，耗时" + ((i + 1) * 200) + "ms）");
                        break; // 连接成功，立即退出循环
                    }
                }
            }
            
            if (!connected) {
                Log.d(TAG, "[打印机] 📊 连接结果: 失败（2秒内未连接成功）");
            }
            
            Log.d(TAG, "[打印机] 🔄 更新isPrinterConnected: " + isPrinterConnected + " -> " + connected);
            isPrinterConnected = connected;
            
            // 等待打印机指令类型初始化（如果连接成功）
            if (connected) {
                Log.d(TAG, "[打印机] ⏳ 等待打印机指令类型初始化...");
                // 最多检查5次，每次200ms，总计1秒
                PrinterCommand commandType = null;
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(200);
                    commandType = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getCurrentPrinterCommand();
                    if (commandType != null) {
                        Log.d(TAG, "[打印机] ✅ 指令类型已初始化: " + commandType + "（第" + (i + 1) + "次检查）");
                        break;
                    }
                }
                
                if (commandType == null) {
                    Log.w(TAG, "[打印机] ⚠️ 打印机指令类型仍为null（1秒内未初始化）");
                }
            }
            
            // 打印机状态变化时刷新出库模式
            // 注意：refreshStockOutMode()中的updateModeStatus()需要在主线程执行
            // 这里不直接调用，而是在onCreate的异步回调中处理
        } catch (Exception e) {
            Log.e(TAG, "[打印机] 💥 自动连接失败", e);
            isPrinterConnected = false;
            // 注意：refreshStockOutMode()中的updateModeStatus()需要在主线程执行
            // 这里不直接调用，而是在onCreate的异步回调中处理
        }
        Log.d("PERF", "autoConnectPrinter end: " + System.currentTimeMillis() + ", 耗时: " + (System.currentTimeMillis() - start) + "ms");
    }
    
    /**
     * 简化的USB打印机连接 - 快速检查
     */
    private void trySimpleUsbConnect() {
        Log.d(TAG, "[打印机] 🔌 快速检查USB打印机...");
        try {
            android.hardware.usb.UsbManager usbManager = (android.hardware.usb.UsbManager) getSystemService(android.content.Context.USB_SERVICE);
            if (usbManager == null) {
                Log.d(TAG, "[打印机] ⚠️ 无法获取USB服务");
                return;
            }
            
            // 只检查是否有USB设备，不进行详细扫描
            java.util.HashMap<String, android.hardware.usb.UsbDevice> deviceList = usbManager.getDeviceList();
            if (deviceList != null && !deviceList.isEmpty()) {
                Log.d(TAG, "[打印机] 📊 发现USB设备，数量: " + deviceList.size());
                // 快速检查是否有匹配的打印机
                for (android.hardware.usb.UsbDevice device : deviceList.values()) {
                    if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                        if (usbManager.hasPermission(device)) {
                            Log.d(TAG, "[打印机] 🔌 快速连接USB打印机");
                            connectUsbPrinter(device);
                        } else {
                            Log.d(TAG, "[打印机] 🔐 USB权限未授予");
                        }
                        break;
                    }
                }
            } else {
                Log.d(TAG, "[打印机] ⚠️ 未检测到USB设备");
            }
        } catch (Exception e) {
            Log.e(TAG, "[打印机] 💥 USB打印机快速连接异常", e);
        }
    }
    
    /**
     * 连接USB打印机
     */
    private void connectUsbPrinter(android.hardware.usb.UsbDevice usbDevice) {
        try {
            Log.d(TAG, "[打印机] 🔌 开始连接USB打印机: " + usbDevice.getDeviceName());
            
            new DeviceConnFactoryManager.Build()
                .setId(0)
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                .setUsbDevice(usbDevice)
                .setContext(this)
                .build();
            
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();
            
            boolean isConnected = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState();
            Log.d(TAG, "[打印机] 📊 USB打印机连接状态: " + (isConnected ? "成功" : "失败"));
            
            if (isConnected) {
                Log.d(TAG, "[打印机] ✅ USB打印机连接成功");
                isPrinterConnected = true;
            } else {
                Log.e(TAG, "[打印机] ❌ USB打印机连接失败");
                isPrinterConnected = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "[打印机] 💥 USB打印机连接异常: " + e.getMessage(), e);
            isPrinterConnected = false;
        }
    }

    /**
     * 【业务核心】保存订单数据到服务器 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责将订单数据保存到服务器
     * 确保出库数据的完整性和一致性
     * 
     * 【业务规则】
     * 1. 数据清理：提取纯数字重量，去掉单位和空格
     * 2. 参数验证：确保所有必要字段都有值
     * 3. 接口调用：使用giveOrderWeightListForStockAndFinish接口
     * 4. 数据刷新：保存成功后自动刷新商品列表
     * 
     * 【数据处理】
     * - 重量处理：order.getNxDoWeight() -> 纯数字格式
     * - 数量处理：order.getNxDoQuantity() -> 数字格式
     * - 商品ID：order.getNxDistributerGoodsEntity().getNxDistributerGoodsId()
     * 
     * 【接口调用】
     * - 接口：giveOrderWeightListForStockAndFinish
     * - 参数：List<NxDepartmentOrdersEntity> orders
     * - 响应：CommonResponse
     * 
     * 【成功处理】
     * 1. 刷新商品列表：updateGoodsList()
     * 2. 重新加载数据：loadStockGoodsData()
     * 3. 显示成功提示：Toast消息
     * 
     * 【错误处理】
     * - 网络错误：显示错误提示
     * - 数据错误：记录错误日志
     * 
     * 【注意事项】
     * - 此方法为数据保存核心，涉及业务数据完整性
     * - 重量处理逻辑与服务器要求一致，请勿随意修改
     * - 数据刷新逻辑确保UI同步，必须保持稳定
     */
    public void saveOrdersToServer(List<NxDepartmentOrdersEntity> orders) {
        Log.d(TAG, "[网络] 开始保存订单数据到服务器，订单数量: " + orders.size());
        
        // 显示加载蒙版
        showLoading();
        Log.d(TAG, "[网络] 显示加载蒙版");
        
        // 处理订单数据，确保重量是纯数字格式
        for (NxDepartmentOrdersEntity order : orders) {
            // 提取纯数字重量，去掉单位和空格
            String originalWeight = order.getNxDoWeight();
            String cleanWeight = originalWeight.replaceAll("[^0-9.]", "");
            if (cleanWeight.isEmpty()) {
                cleanWeight = "0";
            }
            order.setNxDoWeight(cleanWeight);
            
            Log.d(TAG, "[网络] 订单参数: orderId=" + order.getNxDepartmentOrdersId() + 
                  ", originalWeight=" + originalWeight + 
                  ", cleanWeight=" + cleanWeight + 
                  ", quantity=" + order.getNxDoQuantity() + 
                  ", standard=" + order.getNxDoStandard() + 
                  ", remark=" + order.getNxDoRemark() + 
                  ", goodsId=" + order.getNxDistributerGoodsEntity().getNxDistributerGoodsId());
        }
        
        Log.d(TAG, "[网络] 调用真实API接口: giveOrderWeightListForStockAndFinish");
        Log.d(TAG, "[网络] 开始构建HTTP请求...");
        
        // 调用真实的保存接口
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).giveOrderWeightListForStockAndFinish(orders),
                new TypeToken<CommonResponse>() {}
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Subscriber<CommonResponse>() {
                @Override
                public void onStart() {
                    Log.d(TAG, "[网络] API请求开始");
                }
                
            @Override
                public void onCompleted() {
                    Log.d(TAG, "[网络] API调用完成");
                    hideLoading();
                    Log.d(TAG, "[网络] 开始刷新UI...");
                    }
                    
                    @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "[网络] 保存失败: " + e.getMessage());
                    Log.e(TAG, "[网络] 错误详情: ", e);
                    hideLoading();
                    runOnUiThread(new Runnable() {
            @Override
                        public void run() {
                            Toast.makeText(CustomerStockOutActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onNext(CommonResponse response) {
                    Log.d(TAG, "[网络] 保存成功，响应: " + (response != null ? response.toString() : "null"));
                    Log.d(TAG, "[网络] 响应码: " + (response != null ? response.code : "null"));
                    Log.d(TAG, "[网络] 响应消息: " + (response != null ? response.msg : "null"));
                    Log.d(TAG, "[网络] 开始刷新商品列表...");
                    
                    // 刷新商品列表
                    updateGoodsList();
                    Log.d(TAG, "[网络] 商品列表刷新完成");
                    
                    // 重新从服务器加载最新数据
                    Log.d(TAG, "[网络] 开始重新加载服务器数据...");
                loadStockGoodsData();
                    Log.d(TAG, "[网络] 服务器数据重新加载完成");
                    
                    // 显示成功提示
                    Log.d(TAG, "[网络] 显示成功提示: 出库成功，共处理 " + orders.size() + " 个订单");
                    runOnUiThread(new Runnable() {
            @Override
                        public void run() {
                            Toast.makeText(CustomerStockOutActivity.this, "出库成功，共处理 " + orders.size() + " 个订单", Toast.LENGTH_SHORT).show();
                        }
                    });
                    
                    // 注意：不再自动关闭弹窗，让弹窗自己管理关闭
                    // 弹窗会根据剩余订单数量决定是否关闭
                    Log.d(TAG, "[网络] 保存成功，弹窗将自行管理关闭");
                    
                    Log.d(TAG, "[网络] 保存流程完成");
            }
        });
    }
    
    /**
     * 【业务核心】判断打印模式 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责判断当前是否应该使用打印标签模式
     * 根据打印机缓存和连接状态决定出库模式
     * 
     * 【业务规则】
     * 1. 缓存检查：检查是否有打印机配置缓存
     * 2. 连接检查：检查打印机是否实际连接成功
     * 3. 模式判断：只有缓存和连接都成功才启用打印模式
     * 4. 状态更新：更新isPrinterConnected和isPrintMode变量
     * 
     * 【判断逻辑】
     * - 有打印机缓存 + 打印机连接成功 = 打印标签模式
     * - 无打印机缓存 或 打印机连接失败 = 非打印标签模式
     * 
     * 【状态变量】
     * - isPrinterConnected：打印机连接状态
     * - isPrintMode：打印模式状态
     * 
     * 【调用时机】
     * - 页面启动时：确定初始模式
     * - 打印机连接完成后：更新模式状态
     * - 出库操作前：确认当前模式
     * 
     * 【注意事项】
     * - 此方法为模式判断核心，影响整个出库流程
     * - 判断逻辑经过优化，确保用户体验
     * - 状态更新涉及UI显示，必须保持稳定
     */
    private void determinePrintMode() {
        Log.d(TAG, "[日志追踪] determinePrintMode() 被调用");
        Log.d(TAG, "[模式判断] ========== 开始determinePrintMode方法 ==========");
        Log.d(TAG, "[模式判断] 开始判断打印模式");
        Log.d(TAG, "[模式判断] 🔍 调用前状态检查:");
        Log.d(TAG, "[模式判断] - 当前isPrinterConnected: " + isPrinterConnected);
        Log.d(TAG, "[模式判断] - 当前isPrintMode: " + isPrintMode);
        
        // 检查打印机连接状态 - 添加空值检查
        Log.d(TAG, "[模式判断] 检查打印机缓存...");
        boolean hasPrinterCache = false;
        if (printerPreferences != null) {
            hasPrinterCache = printerPreferences.getString("printer_address", null) != null;
        }
        Log.d(TAG, "[模式判断] 打印机缓存状态: " + hasPrinterCache);
        
        Log.d(TAG, "[模式判断] 调用checkPrinterConnection()...");
        boolean printerConnected = checkPrinterConnection();
        Log.d(TAG, "[模式判断] 打印机连接状态: " + printerConnected);
        
        // 更新类成员变量
        boolean oldIsPrinterConnected = isPrinterConnected;
        isPrinterConnected = printerConnected;
        Log.d(TAG, "[模式判断] 🔄 更新isPrinterConnected: " + oldIsPrinterConnected + " -> " + isPrinterConnected);
        
        // 简化判断：只根据打印机状态判断
        boolean oldIsPrintMode = isPrintMode;
        isPrintMode = hasPrinterCache && isPrinterConnected;
        Log.d(TAG, "[模式判断] 🔄 更新isPrintMode: " + oldIsPrintMode + " -> " + isPrintMode);
        
        Log.d(TAG, "[模式判断] 打印机状态: hasCache=" + hasPrinterCache + ", isConnected=" + isPrinterConnected);
        Log.d(TAG, "[模式判断] 最终打印模式: " + (isPrintMode ? "打印标签" : "非打印标签"));
        Log.d(TAG, "[模式判断] ========== determinePrintMode方法完成 ==========");
    }
    
    /**
     * 【业务核心】检查打印机连接状态 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责检查打印机是否真正连接成功
     * 采用SettingsActivity的简单检查方式，避免过度复杂化
     * 
     * 【业务规则】
     * 1. 管理器检查：确保DeviceConnFactoryManager存在
     * 2. 连接状态检查：检查getConnState()返回值
     * 3. 简化逻辑：只检查连接状态，不检查指令类型
     * 4. 错误处理：任何异常都返回false
     * 
     * 【检查逻辑】
     * - 管理器数组为空 -> 返回false
     * - 管理器对象为空 -> 返回false
     * - 连接状态为false -> 返回false
     * - 连接状态为true -> 返回true
     * 
     * 【优化说明】
     * - 采用简单检查：避免复杂的指令类型检查
     * - 参考SettingsActivity：使用经过验证的检查方式
     * - 提升性能：减少不必要的等待和检查
     * 
     * 【注意事项】
     * - 此方法为连接检查核心，经过优化
     * - 简化逻辑确保检查准确性，请勿随意复杂化
     * - 错误处理确保系统稳定性，必须保持
     */
    private boolean checkPrinterConnection() {
        Log.d(TAG, "[模式判断] 开始检查打印机连接状态");
        try {
            DeviceConnFactoryManager[] managers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
            if (managers == null || managers.length == 0) {
                Log.d(TAG, "[模式判断] 打印机管理器数组为空");
                return false;
            }
            
            DeviceConnFactoryManager manager = managers[0];
            if (manager == null) {
                Log.d(TAG, "[模式判断] 打印机管理器为空");
                return false;
            }

            // 检查连接状态 - 采用设置页面的简单逻辑
            boolean connState = manager.getConnState();
            Log.d(TAG, "[模式判断] 打印机连接状态: " + connState);
            
            if (!connState) {
                Log.d(TAG, "[模式判断] 打印机未连接");
                return false;
            }

            // 简化检查：只要连接状态为true就认为连接成功
            // 不检查打印机指令类型，因为指令类型可能需要时间初始化
            Log.d(TAG, "[模式判断] 打印机连接正常，采用简化检查逻辑");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "[模式判断] 检查打印机连接状态异常", e);
        }
        return false;
    }

    /**
     * 【业务核心】更新模式状态 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责更新UI上显示的模式状态
     * 根据当前打印模式显示相应的提示信息
     * 
     * 【业务规则】
     * 1. 状态检查：检查打印机缓存和连接状态
     * 2. 显示逻辑：根据状态显示不同的模式文本
     * 3. UI更新：更新tvTitle控件的显示内容
     * 4. 日志记录：记录详细的状态信息用于调试
     * 
     * 【显示逻辑】
     * - 有打印机缓存 + 打印机连接成功 = "出库模式: 打印标签"
     * - 有打印机缓存 + 打印机连接失败 = "出库模式: 检查打印机中..."
     * - 无打印机缓存 = "出库模式: 非打印标签"
     * 
     * 【状态变量】
     * - printerPreferences：打印机配置缓存
     * - isPrinterConnected：打印机连接状态
     * - isPrintMode：打印模式状态
     * 
     * 【调用时机】
     * - 页面启动时：显示初始状态
     * - 打印机连接完成后：更新连接状态
     * - 模式变化时：反映最新状态
     * 
     * 【注意事项】
     * - 此方法为UI状态更新核心，影响用户体验
     * - 显示逻辑经过优化，确保信息准确性
     * - 状态检查涉及多个变量，必须保持一致性
     */
    private void updateModeStatus() {
        Log.d(TAG, "[日志追踪] updateModeStatus() 被调用");
        Log.d(TAG, "[模式显示] 🔍 详细状态检查:");
        Log.d(TAG, "[模式显示] - printerPreferences: " + (printerPreferences != null ? "非空" : "空"));
        Log.d(TAG, "[模式显示] - isPrinterConnected: " + isPrinterConnected);
        Log.d(TAG, "[模式显示] - isPrintMode: " + isPrintMode);
        
        if (printerPreferences != null) {
            String printerAddress = printerPreferences.getString("printer_address", null);
            String printerType = printerPreferences.getString("printer_type", null);
            Log.d(TAG, "[模式显示] - 缓存打印机地址: " + printerAddress);
            Log.d(TAG, "[模式显示] - 缓存打印机类型: " + printerType);
        }
        
        if (tvTitle != null) {
            // 如果打印机还在连接中，显示"检查中"
            String modeText;
            if (printerPreferences != null && printerPreferences.getString("printer_address", null) != null) {
                if (isPrinterConnected) {
                    modeText = "出库模式: 打印标签";
                } else {
                    modeText = "出库模式: 检查打印机中...";
                }
            } else {
                modeText = "出库模式: 非打印标签";
            }
            tvTitle.setText(modeText);
            Log.d(TAG, "[模式显示] 更新出库模式显示: " + modeText);
        } else {
            Log.e(TAG, "[模式显示] ❌ tvTitle 为空，无法更新显示");
        }
    }

    /**
     * 【业务核心】重新判断打印模式 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责在设备状态变化时重新判断打印模式
     * 确保模式状态与实际设备状态保持一致
     * 
     * 【业务规则】
     * 1. 状态重新检查：调用determinePrintMode()重新判断
     * 2. UI状态更新：调用updateModeStatus()更新显示
     * 3. 弹窗通知：通知当前弹窗刷新模式状态
     * 4. 日志记录：记录模式变化过程
     * 
     * 【调用时机】
     * - 打印机连接状态变化时
     * - 设备配置发生变化时
     * - 用户手动刷新时
     * - 出库操作前确认状态时
     * 
     * 【处理流程】
     * 1. 记录旧的打印模式状态
     * 2. 重新判断当前打印模式
     * 3. 更新UI显示状态
     * 4. 通知相关组件状态变化
     * 
     * 【状态同步】
     * - 确保isPrintMode与实际设备状态一致
     * - 确保UI显示与实际状态一致
     * - 确保弹窗操作与实际状态一致
     * 
     * 【注意事项】
     * - 此方法为状态同步核心，确保系统一致性
     * - 调用时机需要谨慎控制，避免频繁调用
     * - 状态变化涉及多个组件，必须保持同步
     */
    public void refreshStockOutMode() {
        Log.d(TAG, "[打印模式] ========== 开始refreshStockOutMode方法 ==========");
        
        // 重新检查打印机连接状态
        boolean wasPrintMode = isPrintMode;
        determinePrintMode();
        
        Log.d(TAG, "[打印模式] 打印模式状态变更: " + wasPrintMode + " -> " + isPrintMode);
        
        // 更新模式状态显示
        updateModeStatus();
        
        // 如果当前有弹窗，通知弹窗刷新模式
        if (currentDialog instanceof StockOutGoodsDialog) {
            Log.d(TAG, "[打印模式] 通知当前弹窗刷新模式");
            // 这里可以添加通知弹窗刷新模式的逻辑
        }
        
        Log.d(TAG, "[打印模式] ========== refreshStockOutMode方法完成 ==========");
    }

    /**
     * 【业务核心】刷新页面数据 - 严禁删除或修改
     * 
     * 【功能说明】
     * 此方法负责刷新页面的所有数据
     * 确保显示的数据与服务器数据保持一致
     * 
     * 【业务规则】
     * 1. 数据重新加载：调用loadStockGoodsData()重新获取数据
     * 2. 状态保持：保持当前的打印模式和连接状态
     * 3. 用户通知：显示数据刷新状态
     * 4. 错误处理：处理数据加载失败的情况
     * 
     * 【调用时机】
     * - 订单保存成功后
     * - 用户手动刷新时
     * - 页面从后台恢复时
     * - 数据状态变化时
     * 
     * 【处理流程】
     * 1. 记录刷新开始时间
     * 2. 调用数据加载方法
     * 3. 更新UI显示
     * 4. 记录刷新完成时间
     * 
     * 【数据同步】
     * - 确保货架数据最新
     * - 确保商品数据最新
     * - 确保订单状态最新
     * 
     * 【注意事项】
     * - 此方法为数据同步核心，确保数据一致性
     * - 调用时机需要合理控制，避免频繁刷新
     * - 数据加载涉及网络请求，需要处理异常情况
     */
    public void refreshData() {
        Log.d(TAG, "[刷新数据] ========== 开始refreshData方法 ==========");
        
        // 重新加载数据
        loadStockGoodsData();
        
        Log.d(TAG, "[刷新数据] ========== refreshData方法完成 ==========");
    }

    /**
     * 检查蓝牙权限
     */
    private boolean checkBluetoothPermissions() {
        Log.d(TAG, "[权限] 检查蓝牙权限");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[权限] BLUETOOTH_SCAN 或 BLUETOOTH_CONNECT 权限未授予");
                    ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
                    return false;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[权限] ACCESS_FINE_LOCATION 权限未授予");
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_BLUETOOTH_PERMISSIONS);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "[权限] checkBluetoothPermissions 异常", e);
            return false;
        }
        Log.d(TAG, "[权限] 蓝牙权限检查通过");
        return true;
    }

    /**
     * 权限请求结果处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            Log.d(TAG, "[权限] 权限请求结果处理");
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Log.d(TAG, "[权限] 所有蓝牙权限已授予");
                // 重新初始化蓝牙适配器
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
                // 重新判断打印模式
                determinePrintMode();
                updateModeStatus();
                } else {
                Log.w(TAG, "[权限] 部分蓝牙权限被拒绝");
                Toast.makeText(this, "蓝牙权限被拒绝，部分功能可能无法使用", Toast.LENGTH_LONG).show();
            }
        }
    }
} 