const app = getApp()

function inArray(arr, key, val) {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i][key] === val) {
      return i;
    }
  }
  return -1;
}

function copyArr(arr) {
	let res = []
	for (let i = 0; i < arr.length; i++) {
	 res.push(arr[i])
	}
	return res
}

// ArrayBuffer转16进度字符串示例
function ab2hex(buffer) {
  var hexArr = Array.prototype.map.call(
    new Uint8Array(buffer),
    function (bit) {
      return ('00' + bit.toString(16)).slice(-2)
    }
  )
  return hexArr.join('');
}

Page({
  data: {
    devices: [],
    connected: false,
    chs: [], 
    misScanding: false,
    scandbutName:"搜索蓝牙设备"
  },
  openBluetoothAdapter() {
    var that = this;
    this.misScanding = false
    wx.openBluetoothAdapter({
      success: (res) => {
        console.log('openBluetoothAdapter success', res)
        this.startBluetoothDevicesDiscovery()
      },
      fail: (res) => {
        if (res.errCode === 10001) {
          wx.onBluetoothAdapterStateChange(function (res) {
            console.log('onBluetoothAdapterStateChange', res)
            if (res.available) {
              that.startBluetoothDevicesDiscovery()
            }
          })
        }
      }
    })
  },
  getBluetoothAdapterState() {
    wx.getBluetoothAdapterState({
      success: (res) => {
        console.log('getBluetoothAdapterState', res)
        if (res.discovering) {
          this.onBluetoothDeviceFound()
        } else if (res.available) {
          this.startBluetoothDevicesDiscovery()
        }
      }
    })
  },
  startBluetoothDevicesDiscovery() {
    var that = this;
    if (this._discoveryStarted) {
      this.stopBluetoothDevicesDiscovery()
      return
    }
    this.setData({ misScanding: true, 
    scandbutName: "正在搜索，点击停止", 
      devices: [],
      chs: [],
    })
    //console.log('lisn3188---------- misScanding = ', this.misScanding)
    this._discoveryStarted = true
   
    wx.startBluetoothDevicesDiscovery({
      allowDuplicatesKey: true,
      success: (res) => {
        setTimeout(function () {
          console.log("----BluetoothDevicesDiscovery finish---- ");
          if (that._discoveryStarted){
            that.stopBluetoothDevicesDiscovery()
          }
        }, 20000);
        console.log('startBluetoothDevicesDiscovery success', res)
        this.onBluetoothDeviceFound()
      },
    })
/*
setTimeout(function () {
  console.log("----BluetoothDevicesDiscovery finish---- ");
  if (that._discoveryStarted){
    that.stopBluetoothDevicesDiscovery()
  }}, 20000);
  wx.startBluetoothDevicesDiscovery({ // 开启搜索
    allowDuplicatesKey: true,
    success: function (res) {
      wx.onBluetoothDeviceFound(function (res) {
        console.log("get device " , res);
        res.devices.forEach(device => {
          if (!device.name) {
            return
          }
          //console.log("name " , device.name);
          const foundDevices = that.data.devices
          const idx = inArray(foundDevices, 'deviceId', device.deviceId)
       
          const data = foundDevices//{}
          if (idx === -1) {
            data[`devices[${foundDevices.length}]`] = device
          } else {
            data[`devices[${idx}]`] = device
          }
          that.setData( {devices:data,} )

        })
      })
    }
  });
*/

  },
  stopBluetoothDevicesDiscovery() {
    this._discoveryStarted = false
    wx.stopBluetoothDevicesDiscovery()
    this.setData({ misScanding: false, scandbutName:"重新刷新列表", })
    //console.log('lisn3188---------- misScanding = ', this.misScanding)
  },
  onBluetoothDeviceFound() {
    wx.onBluetoothDeviceFound((res) => {
      res.devices.forEach(device => {
        if (!device.name) {
          return
        }
        const foundDevices = this.data.devices
        const idx = inArray(foundDevices, 'deviceId', device.deviceId)
        const data = copyArr(this.data.devices)
        if (idx == -1) {
          //data[`devices[${foundDevices.length}]`] = device
          data.push(device)
        } else {
          data[`devices[${idx}]`] = device
        }
        this.setData( {devices:data,} )
      })
    })
  },goto_Comm(e){
    app.globalData.ble_device = e.currentTarget.dataset
    this.stopBluetoothDevicesDiscovery()
    wx.navigateTo({
      url: '/pages/scale/scale',
    })
    
  },
  createBLEConnection(e) {
    const ds = e.currentTarget.dataset
    const deviceId = ds.deviceId
    const name = ds.name
    wx.createBLEConnection({
      deviceId,
      success: (res) => {
        this.setData({
          connected: true,
          name,
          deviceId,
        })
        this.getBLEDeviceServices(deviceId)
      }
    })
    this.stopBluetoothDevicesDiscovery()
  },
  closeBLEConnection() {
    wx.closeBLEConnection({
      deviceId: this.data.deviceId
    })
    this.setData({
      connected: false,
      chs: [],
      canWrite: false,
    })
  },
  getBLEDeviceServices(deviceId) {
    wx.getBLEDeviceServices({
      deviceId,
      success: (res) => {
        for (let i = 0; i < res.services.length; i++) {
          if (res.services[i].isPrimary) {
            this.getBLEDeviceCharacteristics(deviceId, res.services[i].uuid)
            return
          }
        }
      }
    })
  },
  getBLEDeviceCharacteristics(deviceId, serviceId) {
    wx.getBLEDeviceCharacteristics({
      deviceId,
      serviceId,
      success: (res) => {
        console.log('getBLEDeviceCharacteristics success', res.characteristics)
        for (let i = 0; i < res.characteristics.length; i++) {
          let item = res.characteristics[i]
          if (item.properties.read) {
            wx.readBLECharacteristicValue({
              deviceId,
              serviceId,
              characteristicId: item.uuid,
            })
          }
          if (item.properties.write) {
            this.setData({
              canWrite: true
            })
            this._deviceId = deviceId
            this._serviceId = serviceId
            this._characteristicId = item.uuid
            this.writeBLECharacteristicValue()
          }
          if (item.properties.notify || item.properties.indicate) {
            wx.notifyBLECharacteristicValueChange({
              deviceId,
              serviceId,
              characteristicId: item.uuid,
              state: true,
            })
          }
        }
      },
      fail(res) {
        console.error('getBLEDeviceCharacteristics', res)
      }
    })
    // 操作之前先监听，保证第一时间获取数据
    wx.onBLECharacteristicValueChange((characteristic) => {
      const idx = inArray(this.data.chs, 'uuid', characteristic.characteristicId)
      const data = {}
      if (idx === -1) {
        data[`chs[${this.data.chs.length}]`] = {
          uuid: characteristic.characteristicId,
          value: ab2hex(characteristic.value)
        }
      } else {
        data[`chs[${idx}]`] = {
          uuid: characteristic.characteristicId,
          value: ab2hex(characteristic.value)
        }
      }
      // data[`chs[${this.data.chs.length}]`] = {
      //   uuid: characteristic.characteristicId,
      //   value: ab2hex(characteristic.value)
      // }
      //this.setData(data)
      that.setData( {devices:data,} )
    })
  },
  writeBLECharacteristicValue() {
    // 向蓝牙设备发送一个0x00的16进制数据
    let buffer = new ArrayBuffer(1)
    let dataView = new DataView(buffer)
    dataView.setUint8(0, Math.random() * 255 | 0)
    wx.writeBLECharacteristicValue({
      deviceId: this._deviceId,
      serviceId: this._deviceId,
      characteristicId: this._characteristicId,
      value: buffer,
    })
  },
  closeBluetoothAdapter() {
    wx.closeBluetoothAdapter()
    this._discoveryStarted = false
  },
  gotoabout: function () {
      wx.navigateTo({
        url: '/pages/about/about',
      })
  },
  gotosetuuid(){
    wx.navigateTo({
      url: '/pages/setuuid/setuuid',
    })
  },
  gotoScale(e) {
    // 假设 e.currentTarget.dataset 里有设备信息
    app.globalData.ble_device = e.currentTarget.dataset
    this.stopBluetoothDevicesDiscovery()
    wx.navigateTo({
      url: '/pages/scale/scale',
    })
  }
})
