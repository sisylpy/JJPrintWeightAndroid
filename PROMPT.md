# JJWeightPrint 项目开发指南

## 项目概述

**项目名称**: JJWeightPrint - 智能称重打印系统  
**主要功能**: 蓝牙秤称重 + 打印机打印 + 订单管理  
**目标用户**: 餐饮、零售等需要称重打印标签的场景  
**技术栈**: Android API 18+ (Android 4.3+)

## 核心业务流程

```
商品选择 → 蓝牙称重 → 重量显示(克转斤) → 订单确认 → 打印标签 → 保存订单
```

## 主要功能模块

### 1. 商品管理
- 货架商品展示、商品分类
- 商品信息显示（名称、规格、单位）
- 商品选择与订单关联

### 2. 蓝牙称重
- 自动连接蓝牙秤
- 实时重量显示
- 自动去皮功能
- 重量单位转换（克转斤）

### 3. 订单处理
- 部门订单管理
- 重量录入与验证
- 订单确认与保存
- 多部门支持（内销部门、国标部门）

### 4. 打印系统
- 支持ESC/TSC指令
- USB/蓝牙打印机
- 标签打印格式
- 打印状态监控

### 5. 部门管理
- 内销部门选择
- 国标部门选择
- 部门权限管理

## 技术架构

### 项目结构
```
app/src/main/java/com/swolo/lpy/pysx/
├── main/                    # 主要业务逻辑
│   ├── StockOutActivity.java    # 主页面(出库管理)
│   ├── ScaleActivity.java       # 蓝牙称重页面
│   ├── adapter/                 # 适配器
│   ├── modal/                   # 数据模型
│   └── presenter/               # 业务逻辑层
├── dialog/                  # 弹窗组件
│   ├── StockOutGoodsDialog.java     # 出库商品弹窗
│   ├── StockOutOrdersAdapter.java   # 订单适配器
│   └── ConnectScaleDialog.java      # 蓝牙连接弹窗
└── ui/                      # 基础UI组件
```

### 核心技术栈
- **Android**: API 18+ (Android 4.3+)
- **蓝牙**: BluetoothGattCallback (API 18+)
- **打印**: ESC/TSC指令支持
- **架构**: MVP模式 + RecyclerView
- **权限**: 运行时权限管理

### 数据模型
```java
// 主要实体类
NxDistributerGoodsShelfEntity          // 货架实体
NxDistributerGoodsShelfGoodsEntity     // 货架商品实体
NxDistributerGoodsEntity               // 分销商商品实体
NxDepartmentOrdersEntity               // 部门订单实体
NxDepartmentEntity                     // 内销部门实体
GbDepartmentEntity                     // 国标部门实体
```

## 开发规范

### 1. 代码规范

#### 日志规范
```java
// 使用TAG和分类前缀
Log.d(TAG, "[蓝牙] 连接成功");
Log.d(TAG, "[打印] 开始打印订单");
Log.d(TAG, "[弹窗] 初始化完成");
Log.d(TAG, "[生命周期] onCreate开始执行");
```

#### 方法命名规范
```java
private void connectToScale()          // 连接蓝牙秤
private void updateWeightAtPosition()  // 更新指定位置重量
private void printAndSaveOrders()      // 打印并保存订单
private void checkBluetoothPermissions() // 检查蓝牙权限
private void autoConnectPrinter()      // 自动连接打印机
```

#### 变量命名规范
```java
private BluetoothGatt bluetoothGatt;   // 蓝牙GATT连接
private double tareWeight;             // 皮重
private boolean autoTareDone;          // 自动去皮完成标志
private String scaleAddress;           // 蓝牙秤地址
private boolean isScaleConnected;      // 蓝牙秤连接状态
```

### 2. 权限管理规范

#### 权限检查方法
```java
private boolean checkBluetoothPermissions() {
    // 对于API 18-22，蓝牙权限在安装时自动授予
    // 对于API 23+，需要运行时权限检查
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        return mContext.checkSelfPermission(android.Manifest.permission.BLUETOOTH) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
               mContext.checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    } else {
        // API 18-22 自动授予蓝牙权限
        return true;
    }
}
```

#### API兼容性检查
```java
// 检查API版本，createBond需要API 19+
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
    device.createBond();
} else {
    Log.w(TAG, "当前API版本不支持createBond，跳过配对");
    showToast("当前系统版本不支持自动配对，请手动配对设备");
}
```

### 3. 蓝牙开发规范

#### 连接流程
```java
// 1. 获取蓝牙适配器
BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

// 2. 获取远程设备
BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

// 3. 连接GATT
bluetoothGatt = device.connectGatt(context, false, gattCallback);

// 4. 发现服务
gatt.discoverServices();

// 5. 设置通知
gatt.setCharacteristicNotification(characteristic, true);
```

#### 重量数据解析
```java
if (data.length == 7 && data[0] == 0x05) {
    final int status = data[1];
    final int rawWeight = ((data[3] & 0xFF) << 16) | 
                         ((data[4] & 0xFF) << 8) | 
                         (data[5] & 0xFF);
    final boolean isStable = (status & 0x40) != 0;
    
    // 自动去皮：首次收到稳定重量时记为皮重
    if (!autoTareDone && isStable) {
        tareWeight = rawWeight;
        autoTareDone = true;
    }
    
    final double realWeight = Math.round((rawWeight - tareWeight) * 10) / 10.0;
}
```

#### 单位转换
```java
// 克转斤，保留两位小数
double weightInJin = weight / 500.0;
String weightText = String.format("%.2f", weightInJin) + "斤";
```

### 4. 打印开发规范

#### 打印机状态检查
```java
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
```

#### 打印指令选择
```java
PrinterCommand commandType = manager.getCurrentPrinterCommand();
if (commandType == PrinterCommand.ESC) {
    // ESC指令打印
    EscCommand esc = new EscCommand();
    esc.addInitializePrinter();
    esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
    esc.addText("商品信息\n");
    esc.addCutPaper();
} else if (commandType == PrinterCommand.TSC) {
    // TSC指令打印
    LabelCommand tsc = new LabelCommand();
    tsc.addSize(50, 80);
    tsc.addGap(10);
    tsc.addText(50, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, 
        LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, 
        LabelCommand.FONTMUL.MUL_2, "商品信息");
}
```

### 5. UI开发规范

#### 弹窗样式
```java
public class CustomDialog extends Dialog {
    public CustomDialog(Context context) {
        super(context, R.style.Theme_dialog);
        
        // 设置弹窗圆角背景和半透明遮罩
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.6f;
            lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }
}
```

#### RecyclerView适配器
```java
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 使用holder.getAdapterPosition()而不是position参数
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;
                // 处理数据更新
            }
        };
    }
    
    // 添加详细的日志记录
    public void updateWeightAtPosition(int position, double weight) {
        Log.d(TAG, "[适配器] updateWeightAtPosition: position=" + position + ", weight=" + weight);
        // 实现数据更新方法
    }
}
```

### 6. 错误处理规范

#### 异常捕获
```java
try {
    // 蓝牙/打印操作
    bluetoothGatt = device.connectGatt(context, false, gattCallback);
} catch (Exception e) {
    Log.e(TAG, "连接失败", e);
    showToast("连接失败: " + e.getMessage());
}
```

#### 空值检查
```java
if (device != null && bluetoothAdapter != null) {
    // 执行操作
    bluetoothGatt = device.connectGatt(context, false, gattCallback);
} else {
    Log.e(TAG, "设备为空");
    return;
}
```

## 业务逻辑要点

### 1. 重量处理
- **自动去皮**: 首次稳定重量记为皮重
- **单位转换**: 克转斤，保留两位小数
- **实时更新**: 重量稳定时自动更新UI
- **数据缓存**: 保存最近称重数据

### 2. 订单管理
- **多部门支持**: 内销部门 + 国标部门
- **订单确认**: 只处理有重量的订单
- **数据同步**: 弹窗数据同步到主页面
- **订单状态**: 跟踪订单处理状态

### 3. 打印流程
- **串行打印**: 一次打印一个订单
- **状态检查**: 打印前检查打印机状态
- **错误处理**: 打印失败不影响保存
- **格式支持**: ESC和TSC两种指令格式

## 开发建议

### 1. 新功能开发
- 参考现有代码结构和命名规范
- 添加详细的日志记录
- 实现完整的错误处理
- 考虑API兼容性
- 保持代码风格一致

### 2. 代码质量
- 使用统一的TAG常量
- 添加适当的注释
- 实现合理的异常处理
- 保持代码风格一致
- 遵循Android最佳实践

### 3. 测试要点
- 蓝牙连接稳定性
- 打印功能完整性
- 权限处理正确性
- 不同API版本兼容性
- 重量数据准确性

### 4. 性能优化
- 避免内存泄漏
- 合理使用线程池
- 优化UI更新频率
- 减少不必要的网络请求

## 常见问题解决

### 1. 蓝牙连接问题
- 检查权限是否授予
- 确认设备是否支持蓝牙
- 验证设备地址是否正确
- 检查蓝牙是否开启

### 2. 打印问题
- 确认打印机连接状态
- 检查打印指令格式
- 验证打印机驱动
- 测试打印数据格式

### 3. 权限问题
- API 18-22: 权限自动授予
- API 23+: 需要运行时权限请求
- 检查权限声明是否正确

### 4. 兼容性问题
- 使用API版本检查
- 提供降级方案
- 测试不同Android版本

## 扩展开发指南

### 1. 添加新功能
1. 分析业务需求
2. 设计数据模型
3. 实现业务逻辑
4. 添加UI界面
5. 处理权限和兼容性
6. 添加错误处理
7. 编写测试用例

### 2. 修改现有功能
1. 理解现有代码逻辑
2. 保持接口兼容性
3. 更新相关文档
4. 测试功能完整性

### 3. 性能优化
1. 分析性能瓶颈
2. 优化算法和数据结构
3. 减少内存使用
4. 优化网络请求
5. 改进用户体验

## 接口实现规范

### 1. API接口定义
- 所有API接口统一放在 `app/src/main/java/com/swolo/lpy/pysx/api/` 目录下（如 `GoodsApi.java`, `OrdersApi.java`）。
- 使用 Retrofit + RxJava 风格定义接口，返回值为 `Observable<CommonResponse>`。
- 示例：
```java
@FormUrlEncoded
@POST("api/nxrestrauntorders/weighingGetOrderGoodsType")
Observable<CommonResponse> weighingGetOrderGoodsType(@Field("comId") String comId, @Field("type") String type);
```

### 2. 网络请求统一封装
- 所有网络请求通过 `HttpManager` 单例进行统一管理，位于 `app/src/main/java/com/swolo/lpy/pysx/http/HttpManager.java`。
- 通过 `HttpManager.getInstance().getApi(GoodsApi.class)` 获取API实例。
- 通过 `HttpManager.getInstance().request(observable, typeToken)` 进行数据请求和自动数据解析，返回 `Observable<T>`。

### 3. 数据结构
- 所有API返回统一的 `CommonResponse` 对象（字段有 `code`, `msg`, `data`）。
- 具体数据类型通过 `TypeToken` 传递给 `request` 方法自动解析。
- 示例：
```java
public class CommonResponse {
    public Object data;
    public String msg;
    public Integer code;
}
```

### 4. 典型API调用方式
```java
HttpManager.getInstance()
    .request(
        HttpManager.getInstance().getApi(GoodsApi.class).pickerGetStockGoodsKf(disId, goodsType),
        new com.google.gson.reflect.TypeToken<List<NxDistributerGoodsShelfEntity>>() {}
    )
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Subscriber<List<NxDistributerGoodsShelfEntity>>() {
        @Override
        public void onCompleted() {}
        @Override
        public void onError(Throwable e) { /* 错误处理 */ }
        @Override
        public void onNext(List<NxDistributerGoodsShelfEntity> data) { /* 成功处理 */ }
    });
```

### 5. 错误处理
- 如果 `CommonResponse.code == -1`，自动抛出异常，走 `onError`。
- 网络错误、解析错误也会走 `onError`。
- 建议在 `onError` 里统一处理提示和日志。

### 6. 数据解析
- 通过 `Gson` 自动将 `CommonResponse.data` 转为目标数据类型（由 `TypeToken` 指定）。
- 不需要手动解析JSON，直接在 `onNext` 得到目标类型数据。

### 7. 代码模板
```java
// 1. 获取API实例
GoodsApi api = HttpManager.getInstance().getApi(GoodsApi.class);
// 2. 发起请求并自动解析数据
HttpManager.getInstance()
    .request(api.pickerGetStockGoodsKf(disId, goodsType),
        new com.google.gson.reflect.TypeToken<List<NxDistributerGoodsShelfEntity>>() {})
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Subscriber<List<NxDistributerGoodsShelfEntity>>() {
        @Override
        public void onCompleted() {}
        @Override
        public void onError(Throwable e) { /* 错误处理 */ }
        @Override
        public void onNext(List<NxDistributerGoodsShelfEntity> data) { /* 成功处理 */ }
    });
```

> **注意：所有新开发的接口和网络请求必须严格按照本规范实现，确保项目风格统一、易于维护和扩展。**

---

**注意**: 本指南基于当前项目版本，后续开发应遵循这些规范和最佳实践，确保代码质量和项目可维护性。

## 资源使用约定

- 所有开发中涉及到的图片、图标、按钮等资源，**优先使用系统自带资源（如@android:drawable/xxx）临时代替**，并在代码中注明"临时资源，后续替换"。
- 页面主功能优先实现，UI细节如图片、图标等可后续统一处理和美化。
- 资源命名、引用、替换要保持一致，便于后期批量替换和维护。

> 例如：顶部栏按钮、功能图标等，开发阶段先用系统图标，后续由UI或产品统一替换为正式设计资源。 

- 每次写新接口，都要在网络请求回调和数据适配等关键位置打印详细日志，便于排查问题。 