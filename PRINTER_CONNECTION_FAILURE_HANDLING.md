# 打印机连接失败处理功能

## 功能概述

当客户出库页面（CustomerStockOutActivity）有打印机缓存配置但自动连接失败时，会自动弹出对话框引导用户去设置页面重新连接打印机。

## 修改内容

### 1. 修改 `autoConnectPrinter()` 方法

- 增加了连接失败的处理逻辑
- 当蓝牙打印机连接失败时，调用 `showPrinterConnectionFailedDialog()` 方法

### 2. 修改 `autoConnectBluetoothPrinter()` 方法

- 将返回类型从 `void` 改为 `boolean`
- 在所有失败的情况下返回 `false`
- 在连接成功时返回 `true`

### 3. 新增 `showPrinterConnectionFailedDialog()` 方法

- 显示一个不可取消的对话框
- 标题：打印机连接失败
- 内容：自动连接打印机失败，请前往设置页面重新连接打印机
- 按钮：
  - 去设置：跳转到 SettingsActivity
  - 取消：关闭对话框

## 触发条件

1. 存在打印机缓存配置（类型和地址不为空）
2. 自动连接蓝牙打印机失败，包括：
   - 缺少蓝牙权限
   - 设备不支持蓝牙
   - 蓝牙未开启
   - 无效的蓝牙地址
   - 未找到蓝牙设备
   - 设备配对失败
   - DeviceConnFactoryManager build 失败
   - 连接状态检查失败
   - 其他异常

## 用户体验流程

1. 用户进入客户出库页面
2. 系统检测到有打印机缓存配置
3. 自动尝试连接打印机
4. 如果连接失败，弹出提示对话框
5. 用户点击"去设置"按钮
6. 跳转到设置页面，用户可以重新连接打印机
7. 用户从设置页面返回后，可以继续使用出库功能

## 代码位置

- 文件：`app/src/main/java/com/swolo/lpy/pysx/main/CustomerStockOutActivity.java`
- 相关方法：
  - `autoConnectPrinter()` (第1618行)
  - `autoConnectBluetoothPrinter()` (第1653行)
  - `showPrinterConnectionFailedDialog()` (第1582行)

## 测试建议

1. 测试有打印机缓存但设备不可用的情况
2. 测试蓝牙未开启的情况
3. 测试设备未配对的情况
4. 测试网络异常的情况
5. 验证对话框显示和跳转功能
6. 验证从设置页面返回后的状态 