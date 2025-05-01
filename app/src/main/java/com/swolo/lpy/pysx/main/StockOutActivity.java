package com.swolo.lpy.pysx.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.app.ProgressDialog;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.adapter.StockOutAdapter;
import com.swolo.lpy.pysx.main.adapter.StockOutGoodsAdapter;
import com.swolo.lpy.pysx.main.adapter.StockOutShelfAdapter;
import com.swolo.lpy.pysx.main.gp.Constant;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;
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

public class StockOutActivity extends BaseActivity implements MainContract.StockOutView {

    private RecyclerView leftMenuRecyclerView, goodsRecyclerView;
    private StockOutShelfAdapter shelfAdapter;
    private StockOutGoodsAdapter goodsAdapter;
    private StockOutPresenterImpl stockOutPresenter;
    private int currentShelfIndex = 0; // 添加当前选中的货架索引

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
    private SwipeRefreshLayout swipeRefreshLayout;
    private StockOutAdapter adapter;

    private BluetoothAdapter bluetoothAdapter;

    private boolean isFromDepartmentSelection = false;

    private ProgressDialog progressDialog;

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
            
            // 初始化蓝牙适配器
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            
            setContentView(getContentViewRes());
            Log.d(TAG, "setContentView执行完成");

            // 隐藏返回按钮
            ImageButton btnBack = findViewById(R.id.btn_back);
            if (btnBack != null) {
                btnBack.setVisibility(View.GONE);
            }

            // 设置标题为打印机信息
            tvPrinterInfo = findViewById(R.id.tv_title);
            if (tvPrinterInfo != null) {
                tvPrinterInfo.setTextSize(12);
                tvPrinterInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
                updatePrinterInfo(); // 立即更新打印机信息
            }

            // 初始化打印机Handler
            printerHandler = new Handler(Looper.getMainLooper());
            Log.d(TAG, "printerHandler初始化完成");

            // 初始化USB权限请求的PendingIntent
            mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(Constant.ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            Log.d(TAG, "mPermissionIntent初始化完成");

            // 初始化所有视图
            initView();
            Log.d(TAG, "initView执行完成");

            // 确保stockOutPresenter在initData之前为null
            Log.d(TAG, "stockOutPresenter初始状态: " + (stockOutPresenter == null ? "null" : "已初始化"));

            initData();
            Log.d(TAG, "initData执行完成, stockOutPresenter状态: " + (stockOutPresenter == null ? "null" : "已初始化"));

            // 设置按钮点击事件
            btnSettings = findViewById(R.id.btn_settings);
            if (btnSettings != null) {
                btnSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "点击设置按钮，准备跳转到设置页面");
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

            bindAction();
            Log.d(TAG, "bindAction执行完成");
            setView();
            Log.d(TAG, "setView执行完成");

            // 自动搜索并连接USB打印机
            autoConnectPrinter();
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
            swipeRefreshLayout = stockOutLayout.findViewById(R.id.swipe_refresh_layout);

            // 设置下拉刷新
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (stockOutPresenter != null) {
                    stockOutPresenter.getStockGoods(disId, goodsType);
                }
            });


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
    public void getStockGoodsSuccess(List<NxDistributerGoodsShelfEntity> outGoods) {
        stopLoading();
        Log.d(TAG, "getStockGoodsSuccess: 收到数据，数量=" + (outGoods != null ? outGoods.size() : 0) + 
              ", 当前选中索引=" + currentShelfIndex);
        
        if (outGoods == null || outGoods.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            shelfAdapter.setData(new ArrayList<>());
            goodsAdapter.setGoodsList(new ArrayList<>());
            // 如果是从部门选择返回，且数据为空，不再继续请求
            if (isFromDepartmentSelection) {
                Log.d(TAG, "getStockGoodsSuccess: 从部门选择返回且数据为空，停止后续请求");
                return;
            }
        } else {
            tvNoData.setVisibility(View.GONE);
            
            // 保存当前选中的货架索引
            int oldShelfIndex = currentShelfIndex;
            Log.d(TAG, "保存旧的货架索引: " + oldShelfIndex);
            
            // 更新数据
            shelfAdapter.setData(outGoods);
            
            // 确保索引在有效范围内
            if (oldShelfIndex >= 0 && oldShelfIndex < outGoods.size()) {
                // 恢复之前选中的货架
                currentShelfIndex = oldShelfIndex;
                Log.d(TAG, "恢复货架索引: " + currentShelfIndex);
                shelfAdapter.setSelectedPosition(currentShelfIndex);
                // 显示对应货架的商品
                goodsAdapter.setGoodsList(outGoods.get(currentShelfIndex).getNxDisGoodsShelfGoodsEntities());
            } else {
                // 如果索引无效，显示第一个货架的商品
                currentShelfIndex = 0;
                Log.d(TAG, "重置货架索引为0");
                shelfAdapter.setSelectedPosition(0);
                goodsAdapter.setGoodsList(outGoods.get(0).getNxDisGoodsShelfGoodsEntities());
            }
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
    private void printAndSaveOrders(List<NxDepartmentOrdersEntity> orderList, int index) {
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
    private void printOrder(NxDepartmentOrdersEntity order, PrintCallback callback) {
        Log.d(TAG, "开始打印订单");
        if (isEmulator()) {
            Log.d(TAG, "模拟器环境，跳过打印");
            callback.onPrintFail("模拟器环境，跳过打印");
            return;
        }

        // 检查打印机状态
        if (!checkPrinterStatus()) {
            Log.e(TAG, "打印机未就绪");
            callback.onPrintFail("打印机未就绪，请检查连接");
            return;
        }

        ThreadPool.getInstantiation().addTask(() -> {
            try {
                PrinterCommand commandType = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getCurrentPrinterCommand();
                Log.d(TAG, "打印机指令类型: " + commandType);
                
                if (commandType == PrinterCommand.ESC) {
                    Log.d(TAG, "使用ESC指令打印");
                    printWithESC(order, callback);
                } else if (commandType == PrinterCommand.TSC) {
                    Log.d(TAG, "使用TSC指令打印");
                    printWithTSC(order, callback);
                } else {
                    Log.e(TAG, "不支持的打印机指令集: " + commandType);
                    callback.onPrintFail("不支持的打印机指令集");
                }
            } catch (Exception e) {
                Log.e(TAG, "打印异常", e);
                handlePrintError(e, callback);
            }
        });
    }

    private boolean checkPrinterStatus() {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] == null) {
            return false;
        }
        
        if (!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState()) {
            connectPrinter();
            return false;
        }
        
        return true;
    }

    private void printWithESC(NxDepartmentOrdersEntity order, PrintCallback callback) throws Exception {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        
        // 添加标题
        String departmentName = getDepartmentName(order);
        esc.addText(departmentName + "\n");
        esc.addText("----------------\n");
        
        // 商品信息
        String goodsName = order.getNxDistributerGoodsEntity().nxDgGoodsName;
        String quantity = order.getNxDoQuantity() != null ? order.getNxDoQuantity().toString() : "0";
        esc.addText("商品: " + goodsName + "\n");
        esc.addText("数量: " + quantity + "\n");
        esc.addText("订货: " + order.nxDoWeight + order.nxDoStandard + "\n");
        esc.addText("----------------\n");
        
        esc.addCutPaper();
        Vector<Byte> datas = esc.getCommand();
        
        sendPrintData(datas, callback);
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
            String goodsName = order.getNxDistributerGoodsEntity().nxDgGoodsName;
            String standardName = order.getNxDistributerGoodsEntity().nxDgGoodsStandardname;
            String quantity = order.getNxDoWeight() != null ? order.getNxDoWeight().toString() : "0";
            Log.d(TAG, "商品信息: " + goodsName + ", 规格: " + standardName + ", 数量: " + quantity);
            
            tsc.addText(120, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
                LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, 
                LabelCommand.FONTMUL.MUL_2, goodsName + " " + quantity + standardName);
            
            // 订货信息
            String orderQuantity = order.getNxDoQuantity() != null ? order.getNxDoQuantity().toString() : "0";
            Log.d(TAG, "订货数量: " + orderQuantity);
            tsc.addText(190, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
                LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, 
                LabelCommand.FONTMUL.MUL_2, "订货: " + orderQuantity + order.getNxDoStandard());
            
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
                            department.getFatherDepartmentEntity().getNxDepartmentAttrName(),
                            department.getFatherDepartmentEntity().getNxDepartmentName(),
                            department.getNxDepartmentName());
                } else {
                    return String.format("(%s)%s",
                            department.getNxDepartmentAttrName(),
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
        Log.d(TAG, "开始发送打印数据");
        try {
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] == null) {
                throw new Exception("打印机管理器未初始化");
            }
            
            if (!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState()) {
                throw new Exception("打印机未连接");
            }
            
            Log.d(TAG, "发送打印数据到打印机");
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].sendDataImmediately(datas);
            Log.d(TAG, "打印数据发送成功");
            
            callback.onPrintSuccess();
            Log.d(TAG, "打印回调成功");
            
            runOnUiThread(() -> {
                loadData();
                Toast.makeText(this, "打印成功", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "发送打印数据失败", e);
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
            Log.d(TAG, "开始连接USB打印机: " + usbDevice.getDeviceName());
            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
            }
            
            new DeviceConnFactoryManager.Build()
                    .setId(id)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                    .setUsbDevice(usbDevice)
                    .setContext(this)
                    .build();
            
            // 直接调用openPort，不检查返回值
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
            
            // 检查连接状态
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                // 保存打印机信息
                SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                sp.edit()
                    .putString("printer_type", "usb")
                    .putString("printer_address", usbDevice.getDeviceName())
                    .apply();
                    
                Log.d(TAG, "USB打印机连接成功");
                isPrinterConnected = true;
                showToast("USB打印机连接成功");
            } else {
                Log.e(TAG, "USB打印机连接失败");
                showToast("USB打印机连接失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "USB打印机连接异常: " + e.getMessage(), e);
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
                switch (deviceManager.getCurrentPrinterCommand()) {
                    case ESC:
                        printerType = "ESC指令";
                        break;
                    case TSC:
                        printerType = "TSC指令";
                        break;
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
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
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
                Log.d(TAG, "没有保存的打印机信息");
                updatePrinterInfo();
                return;
            }

            // 根据打印机类型选择连接方式
            if ("usb".equals(printerType)) {
                autoConnectUsbPrinter();
            } else if ("bluetooth".equals(printerType)) {
                autoConnectBluetoothPrinter(printerAddress);
            } else {
                Log.e(TAG, "不支持的打印机类型: " + printerType);
                showToast("不支持的打印机类型");
            }
        } catch (Exception e) {
            Log.e(TAG, "自动连接打印机失败", e);
            showToast("自动连接打印机失败: " + e.getMessage());
        }
    }

    private void autoConnectBluetoothPrinter(String address) {
        Log.d(TAG, "开始连接蓝牙打印机: " + address);
        try {
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
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.e(TAG, "未找到蓝牙打印机设备");
                showToast("未找到蓝牙打印机设备");
                return;
            }

            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
            }

            // 建立新的连接
            new DeviceConnFactoryManager.Build()
                    .setId(id)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                    .setMacAddress(address)
                    .setContext(this)
                    .build();

            // 打开端口
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();

            // 检查连接状态
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                Log.d(TAG, "蓝牙打印机连接成功");
                isPrinterConnected = true;
                showToast("蓝牙打印机连接成功");
            } else {
                Log.e(TAG, "蓝牙打印机连接失败");
                showToast("蓝牙打印机连接失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "连接蓝牙打印机失败", e);
            showToast("连接蓝牙打印机失败: " + e.getMessage());
        } finally {
            updatePrinterInfo();
        }
    }

    private void autoConnectUsbPrinter() {
        Log.d(TAG, "开始自动搜索USB打印机");
        try {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            if (usbManager == null) {
                Log.e(TAG, "无法获取USB服务");
                return;
            }

            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            if (deviceList == null || deviceList.isEmpty()) {
                Log.d(TAG, "未检测到USB设备");
                updatePrinterInfo();
                return;
            }

            boolean foundPrinter = false;
            for (UsbDevice device : deviceList.values()) {
                Log.d(TAG, "检查USB设备: " + device.getDeviceName() + 
                      ", VendorID: " + device.getVendorId() + 
                      ", ProductID: " + device.getProductId());
                  
                if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                    Log.d(TAG, "找到USB打印机，检查权限");
                    if (usbManager.hasPermission(device)) {
                        Log.d(TAG, "已有USB权限，开始连接");
                        foundPrinter = true;
                        usbConn(device);
                    } else {
                        Log.d(TAG, "请求USB权限");
                        usbManager.requestPermission(device, mPermissionIntent);
                    }
                    break;
                }
            }
            
            if (!foundPrinter) {
                Log.d(TAG, "未找到USB打印机");
                updatePrinterInfo();
            }
        } catch (Exception e) {
            Log.e(TAG, "自动连接USB打印机失败", e);
            showToast("自动连接USB打印机失败: " + e.getMessage());
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
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
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
}
