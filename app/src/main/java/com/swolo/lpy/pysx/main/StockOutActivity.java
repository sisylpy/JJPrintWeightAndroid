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
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.main.adapter.StockOutGoodsAdapter;
import com.swolo.lpy.pysx.main.adapter.StockOutShelfAdapter;
import com.swolo.lpy.pysx.main.gp.Constant;
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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class StockOutActivity extends BaseActivity implements MainContract.StockOutView {

    private RecyclerView leftMenuRecyclerView, goodsRecyclerView;
    private StockOutShelfAdapter shelfAdapter;
    private StockOutGoodsAdapter goodsAdapter;
    private StockOutPresenterImpl stockOutPresenter;

    // 设置默认值
    private Integer disId = 75;  // 根据之前的 curl 请求中看到的默认值
    private Integer goodsType = -1;
    private static final String TAG = "StockOutActivity";
    private static final int CONN_STATE_DISCONN = 0x007;
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private static final int CONN_PRINTER = 0x12;
    private int id = 0;
    private ThreadPool threadPool;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;
    private Button btnRefresh, btnToDep, btnLogout;

    private LinearLayout nxDepContainer;
    private LinearLayout gbDepContainer;
    private TextView tvNxDepNames;
    private TextView tvGbDepNames;
    private TextView tvNxDepOrders;
    private TextView tvGbDepOrders;
    private ImageButton btnClearNxDep;
    private ImageButton btnClearGbDep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "onCreate开始执行");
            setContentView(getContentViewRes());
            Log.d(TAG, "setContentView执行完成");
            initView();
            Log.d(TAG, "initView执行完成");
            initData();
            Log.d(TAG, "initData执行完成");
            bindAction();
            Log.d(TAG, "bindAction执行完成");
            setView();
            Log.d(TAG, "setView执行完成");
        } catch (Exception e) {
            Log.e(TAG, "onCreate发生异常", e);
            e.printStackTrace();
        }
    }

    @Override
    protected void initView() {
        leftMenuRecyclerView = findViewById(R.id.left_menu);
        goodsRecyclerView = findViewById(R.id.goods_list);

        btnRefresh = findViewById(R.id.btn_refresh);
        btnToDep = findViewById(R.id.btn_dep);
        btnLogout = findViewById(R.id.btn_logout);

        // 初始化部门选择显示区域
        nxDepContainer = findViewById(R.id.nx_dep_container);
        gbDepContainer = findViewById(R.id.gb_dep_container);
        tvNxDepNames = findViewById(R.id.tv_nx_dep_names);
        tvGbDepNames = findViewById(R.id.tv_gb_dep_names);
        tvNxDepOrders = findViewById(R.id.tv_nx_dep_orders);
        tvGbDepOrders = findViewById(R.id.tv_gb_dep_orders);
        btnClearNxDep = findViewById(R.id.btn_clear_nx_dep);
        btnClearGbDep = findViewById(R.id.btn_clear_gb_dep);
    }

    @Override
    protected void initData() {
        shelfAdapter = new StockOutShelfAdapter();
        goodsAdapter = new StockOutGoodsAdapter();
        stockOutPresenter = new StockOutPresenterImpl(this);

        // 从Intent获取disId和goodsType
        disId = getIntent().getIntExtra("disId", 75);
        goodsType = getIntent().getIntExtra("goodsType", -1);
        Log.d(TAG, "初始化数据: disId=" + disId + ", goodsType=" + goodsType);

        // 设置出库确认回调
        goodsAdapter.setOnOrderConfirmListener(new StockOutGoodsAdapter.OnOrderConfirmListener() {
            @Override
            public void onOrderConfirm(java.util.List<com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity> orderList) {
                printAndSaveOrders(orderList, 0);
            }
        });
    }

    @Override
    protected void bindAction() {
        shelfAdapter.setOnItemClickListener(new StockOutShelfAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NxDistributerGoodsShelfEntity entity) {
                // 点击货架，展示对应货架下的商品
                goodsAdapter.setGoodsList(entity.getNxDisGoodsShelfGoodsEntities());
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        btnToDep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "点击帮助按钮，准备跳转到部门列表页面");
                Log.d(TAG, "当前disId: " + disId + ", goodsType: " + goodsType);
                
                Intent intent = new Intent(StockOutActivity.this, DepartmentListActivity.class);
                intent.putExtra("disId", disId);
                intent.putExtra("goodsType", goodsType);
                Log.d(TAG, "创建Intent并添加参数: disId=" + disId + ", goodsType=" + goodsType);
                
                try {
                    startActivityForResult(intent, 1001);
                    Log.d(TAG, "成功启动DepartmentListActivity");
                } catch (Exception e) {
                    Log.e(TAG, "启动DepartmentListActivity失败", e);
                    showToast("启动部门列表页面失败: " + e.getMessage());
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除缓存
                SharedPreferences sp = getSharedPreferences("user_cache", MODE_PRIVATE);
                sp.edit().clear().apply();
                // 跳转回登录页
                Intent intent = new Intent(StockOutActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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
    }

    @Override
    protected void setView() {
        leftMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        goodsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        leftMenuRecyclerView.setAdapter(shelfAdapter);
        goodsRecyclerView.setAdapter(goodsAdapter);

        // 加载数据
        // loadData();
    }

    private void loadData() {
        showLoading();  // 显示加载进度条
        Log.d(TAG, "开始加载数据 disId: " + disId + ", goodsType: " + goodsType);
        
        // 从缓存中获取部门选择信息
        SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
        String nxIdsJson = sp.getString("selectedNxIds", "[]");
        String gbIdsJson = sp.getString("selectedGbIds", "[]");
        String nxNamesJson = sp.getString("selectedNxNames", "[]");
        String gbNamesJson = sp.getString("selectedGbNames", "[]");

        Log.d(TAG, "从缓存读取部门信息: nxIds=" + nxIdsJson + ", gbIds=" + gbIdsJson);

        List<Integer> outNxDepIds = new Gson().fromJson(nxIdsJson, new TypeToken<List<Integer>>(){}.getType());
        List<Integer> outGbDepIds = new Gson().fromJson(gbIdsJson, new TypeToken<List<Integer>>(){}.getType());
        List<String> outNxDepNames = new Gson().fromJson(nxNamesJson, new TypeToken<List<String>>(){}.getType());
        List<String> outGbDepNames = new Gson().fromJson(gbNamesJson, new TypeToken<List<String>>(){}.getType());

        // 如果有选中的部门，调用_initNxDataKf
        if ((outNxDepIds != null && !outNxDepIds.isEmpty()) || 
            (outGbDepIds != null && !outGbDepIds.isEmpty())) {
            Log.d(TAG, "检测到已选部门，调用_initNxDataKf" + outNxDepIds);

            _initNxDataKf();
        } else {
            Log.d(TAG, "未检测到已选部门，调用_initDataKf");
            _initDataKf();
        }
    }

    private void _initDataKf() {
        try {
            Log.d(TAG, "开始调用_initDataKf获取数据");
            stockOutPresenter.getStockGoods(disId, goodsType);
        } catch (Exception e) {
            Log.e(TAG, "调用_initDataKf时出错: " + e.getMessage(), e);
            showToast("获取数据失败: " + e.getMessage());
        }
    }

    private void _initNxDataKf() {
        try {
            Log.d(TAG, "开始调用_initNxDataKf获取数据");
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
        return R.layout.activity_stock_out; // 关联布局文件
    }

    // 请求数据成功时调用
    @Override
    public void getStockGoodsSuccess(List<NxDistributerGoodsShelfEntity> outGoods) {
        stopLoading();
        if (outGoods != null && !outGoods.isEmpty()) {
            Log.d("StockOutActivity", "接收到货架数据，数量: " + outGoods.size());
            
            // 更新左侧货架列表
            shelfAdapter.setData(outGoods);
            
            // 默认显示第一个货架的商品
            NxDistributerGoodsShelfEntity firstShelf = outGoods.get(0);
            if (firstShelf.getNxDisGoodsShelfGoodsEntities() != null) {
                goodsAdapter.setGoodsList(firstShelf.getNxDisGoodsShelfGoodsEntities());
            }
        } else {
            showToast("暂无数据");
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
                Toast.makeText(this, "全部订单打印并保存成功", Toast.LENGTH_SHORT).show();
                // 刷新数据
                loadData();
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
                            printAndSaveOrders(orderList, index + 1);
                        }
                        @Override
                        public void onSaveFail(String error) {
                            runOnUiThread(() -> Toast.makeText(StockOutActivity.this, "保存失败: " + error, Toast.LENGTH_SHORT).show());
                        }
                    }
                );
            }
            @Override
            public void onPrintFail(String error) {
                runOnUiThread(() -> Toast.makeText(StockOutActivity.this, "打印失败: " + error, Toast.LENGTH_SHORT).show());
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
        ThreadPool.getInstantiation().addTask(new Runnable() {
            @Override
            public void run() {
                if (isEmulator()) {
                    callback.onPrintFail("当前为模拟器环境，无法连接USB打印机，请用真机测试！");
                    return;
                }
                int id = 0;
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                    !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                    callback.onPrintFail("打印机未连接");
                    return;
                }
                
                try {
                    PrinterCommand commandType = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand();
                    
                    if (commandType == PrinterCommand.ESC) {
                        EscCommand esc = new EscCommand();
                        esc.addInitializePrinter();
                        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
                        
                        // 添加标题

                        if(order.getNxDepartmentEntity() != null){
                            esc.addText(order.getNxDepartmentEntity().getNxDepartmentAttrName() + "\n");
                        }else if (order.getGbDepartmentEntity() != null){
                            esc.addText(order.getGbDepartmentEntity().getGbDepartmentAttrName() + "\n");
                        }
                        esc.addText("----------------\n");
                        
                        // 商品名称（添加空值检查）
                        String name = order.getNxDistributerGoodsEntity().nxDgGoodsName;
                        if (name != null && !name.isEmpty()) {
                            esc.addText("商品: " + name + "\n");
                        }
                        
                        // 出库数量（添加空值检查）
                        String quantity = order.getNxDoQuantity() != null ? order.getNxDoQuantity().toString() : "0";
                        esc.addText("数量: " + quantity + "\n");
                        
                        // 添加订货数量
                        esc.addText("订货: " +  order.nxDoWeight + order.nxDoStandard + "\n");
                        esc.addText("----------------\n");
                        
                        esc.addCutPaper();
                        Vector<Byte> datas = esc.getCommand();
                        
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
                        callback.onPrintSuccess();
                        // 打印完成后刷新数据
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadData();
                            }
                        });
                        
                    } else if (commandType == PrinterCommand.TSC) {
                        LabelCommand tsc = new LabelCommand();
                        tsc.addTear(EscCommand.ENABLE.ON);
                        // 设置标签尺寸为 5*8 厘米 (50*80 毫米)
                        tsc.addSize(50, 80);
                        tsc.addGap(10);
                        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);
                        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON);
                        tsc.addReference(0, 0);
                        tsc.addCls();

                        // 第一行：订货部门名称
                        String departmentName = "";
                        if(order.getNxDepartmentEntity() != null){
                            departmentName = order.getNxDepartmentEntity().getNxDepartmentAttrName();
                        }else if(order.getGbDepartmentEntity() != null){
                            departmentName = order.getGbDepartmentEntity().getGbDepartmentAttrName();
                        }
                        if (departmentName != null && !departmentName.isEmpty()) {
                            tsc.addText(50, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270,
                                    LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, departmentName);
                        }

                        // 第二行：商品名称和出库数量
                        String name = order.getNxDistributerGoodsEntity().nxDgGoodsName;
                        String standardname = order.getNxDistributerGoodsEntity().nxDgGoodsStandardname;
                        String quantity = order.getNxDoWeight() != null ? order.getNxDoWeight().toString() : "0";
                        if (name != null && !name.isEmpty()) {
                            tsc.addText(120, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270,
                                    LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, name + " " + quantity + standardname);
                        }

                        // 第三行：订货数量
                        String orderQuantity = order.getNxDoQuantity() != null ? order.getNxDoQuantity().toString() : "0";
                        tsc.addText(190, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270,
                                LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, "订货: " + orderQuantity + order.getNxDoStandard());

                        // 第四行：订货数量
                        String orderRemark = order.getNxDoRemark();
                        if(orderRemark != null && !orderRemark.isEmpty()){
                            tsc.addText(260, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270,
                                    LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "备注: " + orderRemark);
                        }

                        tsc.addPrint(1, 1);
                        tsc.addSound(2, 100);

                        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
                        Vector<Byte> datas = tsc.getCommand();

                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
                        callback.onPrintSuccess();
                        // 打印完成后刷新数据
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadData();
                            }
                        });
                    } else {
                        callback.onPrintFail("打印机指令集不支持");
                    }
                } catch (Exception e) {
                    Log.e("StockOutActivity", "打印异常: " + e.getMessage());
                    callback.onPrintFail("打印异常: " + e.getMessage());
                }
            }
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

        // 初始化 USB 管理器
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // 获取所有设备，选择满足条件的设备
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                usbConn(device);
            }
        }
    }

    // USB 连接方法
    private void usbConn(UsbDevice usbDevice) {
        new DeviceConnFactoryManager.Build()
                .setId(id)
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                .setUsbDevice(usbDevice)
                .setContext(this)
                .build();
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
    }

    // 广播接收器
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constant.ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                usbConn(device);
                            }
                        }
                    }
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while(deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();
                        if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                            usbConn(device);
                        }
                    }
                    break;

                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    if (id == deviceId) {
                        switch (state) {
                            case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                                showToast("打印机已断开连接");
                                break;
                            case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                                showToast("正在连接打印机...");
                                break;
                            case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                                showToast("打印机已连接");
                                break;
                            case DeviceConnFactoryManager.CONN_STATE_FAILED:
                                showToast("打印机连接失败");
                                break;
                        }
                    }
                    break;
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
                case CONN_PRINTER:
                    showToast("无法连接打印机");
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceConnFactoryManager.closeAllPort();
        if (threadPool != null) {
            threadPool.stopThreadPool();
            threadPool = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("StockOutActivity", "onResume: 页面显示，刷新数据");
        updateDepartmentDisplay();
        loadData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult被调用: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "从部门列表页面返回，结果码为RESULT_OK");
                // 更新部门显示
                updateDepartmentDisplay();
                // 重新加载数据
                loadData();
            } else {
                Log.d(TAG, "从部门列表页面返回，结果码不是RESULT_OK: " + resultCode);
            }
        }
    }

    private void updateDepartmentDisplay() {
        SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
        String nxNamesJson = sp.getString("selectedNxNames", "[]");
        String gbNamesJson = sp.getString("selectedGbNames", "[]");

        Log.d(TAG, "从缓存读取部门名称: nxNamesJson=" + nxNamesJson + ", gbNamesJson=" + gbNamesJson);

        List<String> nxNames = new Gson().fromJson(nxNamesJson, new TypeToken<List<String>>(){}.getType());
        List<String> gbNames = new Gson().fromJson(gbNamesJson, new TypeToken<List<String>>(){}.getType());

        Log.d(TAG, "解析后的部门名称: nxNames=" + nxNames + ", gbNames=" + gbNames);

        // 更新内销部门显示
        if (nxNames != null && !nxNames.isEmpty()) {
            nxDepContainer.setVisibility(View.VISIBLE);
            tvNxDepNames.setText(String.join("、", nxNames));
            tvNxDepOrders.setText("待出库订单");
            Log.d(TAG, "显示内销部门: " + String.join("、", nxNames));
        } else {
            nxDepContainer.setVisibility(View.GONE);
            Log.d(TAG, "隐藏内销部门显示");
        }

        // 更新国标部门显示
        if (gbNames != null && !gbNames.isEmpty()) {
            gbDepContainer.setVisibility(View.VISIBLE);
            tvGbDepNames.setText(String.join("、", gbNames));
            tvGbDepOrders.setText("待出库订单");
            Log.d(TAG, "显示国标部门: " + String.join("、", gbNames));
        } else {
            gbDepContainer.setVisibility(View.GONE);
            Log.d(TAG, "隐藏国标部门显示");
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
}
