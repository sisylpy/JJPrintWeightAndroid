# 蓝牙称清零功能实现指南

## 功能概述

在出库对话框中，当弹窗打开时会自动连接已配置的蓝牙称，并在连接成功后自动执行清零（去皮）操作，确保用户可以直接进行称重，无需手动清零。

## 核心功能

### 1. 自动连接蓝牙称
- 弹窗打开时自动检查是否有已配置的蓝牙称
- 如果有配置，自动连接；如果没有，显示连接对话框
- 支持设备配对和GATT连接

### 2. 自动清零功能
- 蓝牙称连接成功后自动执行清零操作
- 清零后重置第一个订单的重量为0
- 显示"蓝牙称已清零"提示

### 3. 实时称重数据接收
- 通过GATT通知接收实时称重数据
- 自动解析重量数据并更新UI
- 保存最新重量到缓存

## 实现流程

### 1. 弹窗打开流程
```onCreate() 
  ↓
autoConnectScale() - 检查已配置的蓝牙称
  ↓
connectToScale() - 连接蓝牙称
  ↓
startGattConnection() - 建立GATT连接
  ↓
GattCallback.onConnectionStateChange() - 连接状态回调
  ↓
GattCallback.onServicesDiscovered() - 服务发现完成
  ↓
performAutoTare() - 自动清零
```

### 2. 清零操作流程
```
performAutoTare()
  ↓
发送清零指令到蓝牙称
  ↓
重置第一个订单重量为0
  ↓
显示清零成功提示
```

### 3. 称重数据接收流程
```
GattCallback.onCharacteristicChanged()
  ↓
parseWeightData() - 解析重量数据
  ↓
setWeight() - 更新UI显示
  ↓
保存到缓存
```

## 关键代码实现

### 1. 自动连接蓝牙称
```java
private void autoConnectScale() {
    // 检查是否有已配置的蓝牙称
    SharedPreferences sp = getContext().getSharedPreferences("scale_cache", Context.MODE_PRIVATE);
    String cachedAddress = sp.getString("scale_address", null);
    
    if (cachedAddress != null && !cachedAddress.isEmpty()) {
        // 自动连接已配置的蓝牙称
        connectToScale(cachedAddress);
    } else {
        // 显示连接对话框
        showConnectScaleDialog();
    }
}
```

### 2. GATT连接和清零
```java
private class GattCallback extends BluetoothGattCallback {
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // 找到称重服务和特征
            // 启用通知
            // 连接成功后自动清零
            new Handler().postDelayed(() -> {
                performAutoTare();
            }, 2000);
        }
    }
}
```

### 3. 自动清零实现
```java
private void performAutoTare() {
    if (!isScaleConnected || writeCharacteristic == null) {
        return;
    }
    
    // 发送清零指令
    byte[] tareCommand = "TARE\n".getBytes();
    writeCharacteristic.setValue(tareCommand);
    bluetoothGatt.writeCharacteristic(writeCharacteristic);
    
    // 重置第一个订单重量为0
    if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
        ordersAdapter.updateWeightAtPosition(0, 0);
    }
    
    Toast.makeText(getContext(), "蓝牙称已清零", Toast.LENGTH_SHORT).show();
}
```

## 蓝牙称协议适配

### 1. 服务UUID
```java
// 称重服务UUID (需要根据实际蓝牙称调整)
BluetoothGattService service = gatt.getService(
    UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
```

### 2. 特征UUID
```java
// 写特征UUID
writeCharacteristic = service.getCharacteristic(
    UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));

// 通知特征UUID
notifyCharacteristic = service.getCharacteristic(
    UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
```

### 3. 清零指令
```java
// 清零指令 (需要根据实际蓝牙称协议调整)
byte[] tareCommand = "TARE\n".getBytes();
```

### 4. 重量数据解析
```java
private double parseWeightData(byte[] data) {
    try {
        String dataStr = new String(data);
        // 提取数字部分
        String weightStr = dataStr.replaceAll("[^0-9.]", "");
        if (!weightStr.isEmpty()) {
            double weight = Double.parseDouble(weightStr);
            // 转换为斤 (假设数据是克)
            return weight / 500.0;
        }
    } catch (Exception e) {
        Log.e(TAG, "解析重量数据失败", e);
    }
    return 0.0;
}
```

## 配置说明

### 1. 蓝牙称配置
- 在设置页面连接蓝牙称后，设备信息会保存到`scale_cache`
- 包含设备地址(`scale_address`)和设备名称(`scale_name`)
- 出库对话框会自动读取这些配置信息

### 2. UUID配置
- 需要根据实际使用的蓝牙称型号调整UUID
- 常见的UUID格式：
  - 服务UUID: `0000fff0-0000-1000-8000-00805f9b34fb`
  - 写特征UUID: `0000fff1-0000-1000-8000-00805f9b34fb`
  - 通知特征UUID: `0000fff2-0000-1000-8000-00805f9b34fb`

### 3. 清零指令配置
- 不同品牌的蓝牙称使用不同的清零指令
- 常见指令：
  - `TARE\n`
  - `T\n`
  - `ZERO\n`
  - `Z\n`

## 测试步骤

### 1. 基本功能测试
1. 在设置页面连接蓝牙称
2. 打开出库对话框
3. 观察是否自动连接蓝牙称
4. 检查是否自动清零
5. 测试称重数据接收

### 2. 清零功能测试
1. 在蓝牙称上放置重物
2. 打开出库对话框
3. 观察是否自动清零
4. 检查第一个订单重量是否重置为0
5. 验证清零提示是否显示

### 3. 数据接收测试
1. 连接蓝牙称后
2. 在称上放置不同重量的物品
3. 观察重量数据是否正确显示
4. 检查数据格式转换是否正确

## 常见问题

### 1. 蓝牙称连接失败
- 检查蓝牙是否开启
- 检查设备是否已配对
- 检查UUID是否正确
- 查看日志确认连接状态

### 2. 清零功能不工作
- 检查清零指令是否正确
- 检查写特征是否找到
- 确认蓝牙称支持清零功能
- 查看日志确认指令发送状态

### 3. 重量数据解析错误
- 检查数据格式是否正确
- 调整解析逻辑
- 查看原始数据日志
- 确认单位转换是否正确

### 4. 自动连接不工作
- 检查是否有已配置的蓝牙称
- 确认配置信息是否正确
- 检查权限是否授予
- 查看连接日志

## 日志监控

### 1. 连接日志
```bash
adb logcat -s StockOutGoodsDialog | grep "自动连接\|连接\|GATT"
```

### 2. 清零日志
```bash
adb logcat -s StockOutGoodsDialog | grep "清零"
```

### 3. 数据接收日志
```bash
adb logcat -s StockOutGoodsDialog | grep "解析\|重量"
```

## 性能优化

### 1. 连接优化
- 使用延迟发现服务，确保连接稳定
- 添加重连机制
- 优化配对流程

### 2. 数据处理优化
- 添加数据过滤，避免无效数据
- 优化解析算法
- 减少UI更新频率

### 3. 资源管理
- 及时断开GATT连接
- 正确清理资源
- 避免内存泄漏

## 扩展功能

### 1. 手动清零
- 添加手动清零按钮
- 支持用户主动清零
- 提供清零状态显示

### 2. 单位转换
- 支持多种重量单位
- 自动单位转换
- 用户可配置单位

### 3. 数据校准
- 支持重量校准
- 补偿值设置
- 精度调整

### 4. 多设备支持
- 支持多个蓝牙称
- 设备切换功能
- 设备管理界面 