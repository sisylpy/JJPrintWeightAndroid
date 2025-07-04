Page({
  data: {
    deviceName: '',
    deviceId: '',
    connectStatus: '未连接',
    weightG: '--', // 显示克（g）
    rawHex: '',
    showRaw: false,
    connected: false,
    serviceId: '',
    writeCharId: '',
    notifyCharId: '',
    tareWeight: null, // 以克为单位
    statusText: '',
    autoTareDone: false,
  },

  onLoad() {
    const app = getApp();
    this.data.device = app.globalData.ble_device;
    if (!this.data.device) {
      this.setData({ connectStatus: '未连接' });
      return;
    }
    this.setData({
      deviceId: this.data.device.deviceId,
      deviceName: this.data.device.name || this.data.device.localName || '',
      connectStatus: '正在连接...'
    });
    this.connectToDevice(this.data.device.deviceId);
  },

  connectToDevice(deviceId) {
    wx.createBLEConnection({
      deviceId,
      success: () => {
        console.log('BLE连接成功', deviceId);
        this.setData({ connectStatus: '已连接', connected: true });
        this.getServices(deviceId);
      },
      fail: () => {
        console.log('BLE连接失败', deviceId);
        this.setData({ connectStatus: '连接失败' });
      }
    });
  },

  getServices(deviceId) {
    wx.getBLEDeviceServices({
      deviceId,
      success: (res) => {
        console.log('获取服务', res.services);
        res.services.forEach(service => {
          wx.getBLEDeviceCharacteristics({
            deviceId,
            serviceId: service.uuid,
            success: (resChar) => {
              resChar.characteristics.forEach(item => {
                console.log('特征属性', item.uuid, item.properties, '服务:', service.uuid);
                if ((item.properties.write || item.properties.writeWithoutResponse) && !this.data.writeCharId) {
                  this.setData({ writeCharId: item.uuid, serviceId: service.uuid });
                }
                if (item.properties.notify && !this.data.notifyCharId) {
                  this.setData({ notifyCharId: item.uuid, serviceId: service.uuid });
                  wx.notifyBLECharacteristicValueChange({
                    deviceId,
                    serviceId: service.uuid,
                    characteristicId: item.uuid,
                    state: true
                  });
                  wx.onBLECharacteristicValueChange(this.onBLEData.bind(this));
                }
              });
            }
          });
        });
      }
    });
  },

  getWeight() {
    console.log('点击获取重量');
    const cmd = new Uint8Array([0x05, 0xa9, 0x00, 0x00, 0x00, 0xae]);
    this.sendCmd(cmd);
  },

  zero() {
    console.log('点击清零');
    const cmd = new Uint8Array([0x05, 0x86, 0x00, 0x00, 0x00, 0x8B]);
    this.sendCmd(cmd);
    if (this.lastWeightG !== undefined) {
      this.setData({ tareWeight: this.lastWeightG });
      console.log('清零，设置皮重为', this.lastWeightG, 'g');
    } else {
      this.setData({ tareWeight: null });
    }
  },

  sendCmd(cmd) {
    console.log('发送指令', cmd, 'connected:', this.data.connected, 'writeCharId:', this.data.writeCharId);
    if (!this.data.connected || !this.data.writeCharId) return;
    wx.writeBLECharacteristicValue({
      deviceId: this.data.deviceId,
      serviceId: this.data.serviceId,
      characteristicId: this.data.writeCharId,
      value: cmd.buffer,
      success: (res) => {
        console.log('指令发送成功', res);
      },
      fail: (err) => {
        console.error('指令发送失败', err);
      }
    });
  },

  onBLEData(res) {
    const buf = new Uint8Array(res.value);
    if (buf.length === 7 && buf[0] === 0x05) {
      const status = buf[1];

      // 🟢 正确处理：rawWeight 本身就是“克”单位
      const rawWeight = (buf[3] << 16) | (buf[4] << 8) | buf[5];
      const weightG = rawWeight; // ✅ 不再乘10
      this.lastWeightG = weightG;

      if (!this.data.autoTareDone) {
        this.setData({ tareWeight: weightG, autoTareDone: true });
        console.log('自动去皮，设置皮重为', weightG, 'g');
      }

      let tare = this.data.tareWeight || 0;
      let realWeight = weightG - tare;
      realWeight = Math.round(realWeight * 10) / 10; // 保留1位小数

      let statusText = '';
      statusText += (status & 0x20) ? '超载 ' : '';
      statusText += (status & 0x40) ? '稳定 ' : '不稳定 ';
      statusText += (status & 0x10) ? '负重 ' : '正重 ';
      statusText += (status & 0x80) ? '信息回传' : '重量数据';

      this.setData({
        weightG: realWeight,
        statusText: statusText.trim(),
        rawHex: Array.from(buf).map(b => b.toString(16).padStart(2, '0')).join(' ').toUpperCase()
      });

      console.log('原始重量:', weightG, 'g，皮重:', tare, 'g，实际重量:', realWeight, 'g，状态:', statusText);
    }
  },

  toggleRaw() {
    this.setData({ showRaw: !this.data.showRaw });
  },

  gotoScale(e) {
    const app = getApp();
    app.globalData.ble_device = e.currentTarget.dataset;
    this.stopBluetoothDevicesDiscovery();
    wx.navigateTo({
      url: '/pages/scale/scale',
    });
  }
});
