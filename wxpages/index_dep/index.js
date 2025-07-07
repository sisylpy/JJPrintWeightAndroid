const globalData = getApp().globalData;
var load = require('../../../lib/load.js');

import apiUrl from '../../../config.js'

const tabBarHeight = 50; // 根据实际情况调整
const viewBarHeight = 60;

import {
  stockerGetWaitStockGoodsDeps,
} from '../../../lib/apiDistributer.js'

import {
  disLoginKf
} from '../../../lib/apiDistributer'

Page({
  data: {
    firstLoading: true,
    onPurchaseRefresh: false,
    update: false,
    changeIds: false
  },

  onShow() {
    //tabBar
    if (typeof this.getTabBar === 'function' &&
      this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      })
    }

    var value = wx.getStorageSync('userInfo');
    if (value) {
      this.setData({
        userInfo: value,
        disId: value.nxDistributerEntity.nxDistributerId,
        disInfo: value.nxDistributerEntity,
      })
      this._getTodayCustomer();
     
    } else {
      this._login();
    }

    const app = getApp();
    const navBarHeight = app.globalData.navBarHeight;
    const screenHeight = app.globalData.screenHeight;
    const screenWidth = app.globalData.screenWidth;
    const rpxRatio = 750 / screenWidth;
    const navBarHeightRpx = navBarHeight * rpxRatio;
    const viewBarHeightRpx = viewBarHeight * rpxRatio;
    const tabBarHeightRpx = 100;
    const contentHeight = (screenHeight - navBarHeight - tabBarHeight - viewBarHeight) * rpxRatio;
    this.setData({
      contentHeight: contentHeight,
      navBarHeight: navBarHeightRpx,
      tabBarHeight: tabBarHeightRpx,
      viewBarHeight: viewBarHeightRpx,
      leftMenuWidth: 150, // 左侧菜单宽度，单位 rpx
    });
    
    console.log("godosoososso", globalData);
    this.setData({
      windowWidth: globalData.windowWidth * globalData.rpxR,
      windowHeight: globalData.windowHeight * globalData.rpxR,
      statusBarHeight: globalData.statusBarHeight * globalData.rpxR,
      navBarHeight: globalData.navBarHeight * globalData.rpxR,
      url: apiUrl.server,
    })
  },

  // 页面级别的下拉刷新处理函数
  onPullDownRefresh() {
    this.onPurchaseRefresh();
  },

  // Purchase页面刷新
  onPurchaseRefresh() {
    console.log("onPurchaseRefreshonPurchaseRefresh")
    load.showLoading("获取今日订单");
    var that = this;
    disGetTodayOrderCustomer(this.data.disId).then(res => {
      load.hideLoading();
      console.log(res.result.data)
      if (res.result.code == 0) {
        this.setData({
          nxDepArr: res.result.data.deps.nxDep,
          gbDisArr: res.result.data.deps.gbDisArr,
          gbDisArrApp: res.result.data.deps.gbDisArrApp,
          unPayCount: res.result.data.unPayCount,
          disInfo: res.result.data.disInfo,
          returnList: res.result.data.returnList,
          unPayGbBills: res.result.data.unPayGbBills,
          firstLoading: false
        })
        wx.stopPullDownRefresh()
        wx.setStorageSync('disInfo', res.result.data.disInfo);
        wx.setStorageSync('numberBooks', res.result.data.books)
        if (res.result.data.disInfo.nxDistributerBuyQuantity < 1) {
          wx.navigateTo({
            url: '../../../subPackage/pages/management/payPage/payPage?type=0',
          })
        }

        that.getTabBar().setData({
          stockCount: res.result.data.stockCount,
          stockCountOk: res.result.data.stockCountOk,
          wxCount: res.result.data.wxCount,
          wxCountOk: res.result.data.wxCountOk,
          prepareCount: res.result.data.preOrders,
          buyOrders: res.result.data.buyOrders,
          buyOrdersOk: res.result.data.buyOrdersOk,
        })
      } else {
        wx.showToast({
          title: res.result.msg,
          icon: 'none'
        })
        wx.stopPullDownRefresh()
      }
    })
  },


  _login1() {
    var that = this;
    // 首次登录
    wx.login({
      success(res) {
        console.log(res);
        if (res.code) {
          var disUser = {
            nxDiuCode: res.code,
          }
          disLogin(disUser)
            .then(res => {
              if (res.result.code !== -1) {
                console.log(res.result)
                //缓存用户信息
                wx.setStorageSync('userInfo', res.result.data.userInfo);
                wx.setStorageSync('disInfo', res.result.data.disInfo);
                that.setData({
                  userInfo: res.result.data.userInfo,
                  disInfo: res.result.data.disInfo,
                  disId: res.result.data.disInfo.nxDistributerId,
                })
                that._getTodayCustomer();
              } else {
                console.log("faileler")
                wx.navigateTo({
                  url: '../../login/login',
                })
              }
            })
        }
      }
    })
    //login finish
  },
  _login() {
    var that = this;
    wx.login({
      success: (res) => {
        load.hideLoading();

        var disUser = {
          nxDiuCode: res.code,
        }
        load.showLoading("登录中")
        disLoginKf(disUser)
          .then((res) => {
            load.hideLoading();
            console.log(res.result.data);
            if (res.result.code !== -1) { //登陆成功
              this.setData({
                userInfo: res.result.data.userInfo,
                disInfo: res.result.data.disInfo,
                disId: res.result.data.disInfo.nxDistributerId,
              })

              wx.setStorageSync('userInfo', res.result.data.userInfo);
              wx.setStorageSync('disInfo', res.result.data.disInfo);
             
              that._getTodayCustomer()
           
              
            } else { // 登陆失败
              // wx.showModal({
              //   title: res.result.msg,
              //   content: "请注册",
              //   showCancel: false,
              //   confirmText: "知道了",
              // })
              wx.redirectTo({
                url: '../../inviteAdmin/inviteAdmin',
              })

            }
          })
      },
      
      fail: (res => {
        load.hideLoading();
        wx.showModal({
          title: res.result.msg,
          showCancel: false,
          confirmText: "知道了",
        })
      })
    })


  },

  /**
   * 获取客户订单
   */
  _getTodayCustomer() {
    var data = {
      disId: this.data.disId,
    }
    load.showLoading("获取数据中");
    stockerGetWaitStockGoodsDeps(data).then(res => {
      load.hideLoading();
      console.log(res.result.data)
      if (res.result.code == 0) {
        this.setData({
          nxDepArr: res.result.data.nxDep,
            gbDepArr: res.result.data.gbDep
        })
        
        // 检查是否有部门被选中
        var hasSelectedDep = this._checkHasSelectedDep();
        this.setData({
          changeIds: hasSelectedDep
        })
        
        this.getTabBar().setData({
          stockCount: res.result.data.depOrdersWait,
          
        })

      
      } else {
        wx.showToast({
          title: res.result.msg,
          icon: 'none'
        })
      }
    })
  },

  
  toStock(){
    this.setData({
      changeIds: false
    })

  
      var nxLeg = this.data.nxDepArr.length;
      var nxIds = [];
      var nxDepName = [];
      if (nxLeg > 0) {
        var nxDepArr = this.data.nxDepArr;
        var nxUn = 0;
        for (var i = 0; i < nxDepArr.length; i++) {
          if (nxDepArr[i].isSelected) {
            nxIds.push(nxDepArr[i].nxDepartmentId);
            nxDepName.push(nxDepArr[i].nxDepartmentName);
          }else{
            nxUn = Number(nxUn) + Number(1);
          }
        }
      }

      var gbLeg = this.data.gbDepArr.length;
      var gbIds = [];
      var gbDepName = [];
      if (gbLeg > 0) {
        var gbDepArr = this.data.gbDepArr;
        var gbUn = 0 ;
        for (var i = 0; i < gbDepArr.length; i++) {
          if (gbDepArr[i].isSelected) {
            gbIds.push(gbDepArr[i].gbDepartmentId);
            gbDepName.push(gbDepArr[i].gbDepartmentName);
          }else{
            gbUn = Number(gbUn) + Number(1);
          }
        }
      }

    console.log("ddd")
    console.log("gbun===", gbUn + "gblend====", gbLeg , "nxun====", nxUn, "nxlieng===", nxLeg);
    if(gbUn !== gbLeg || nxUn !== nxLeg){
      var idsChangeStock = {
        haveIds: true,
        outNxDepIds: nxIds,
        outNxDepNames: nxDepName,
        outGbDepIds: gbIds,
        outGbDepNamews: gbDepName,
      }
      wx.setStorageSync('idsChangeStock', idsChangeStock);
    }
    

    wx.navigateTo({
      url: '../../stock/index/index',
    })

  },  

  toWaitDep(e) {
    this.setData({
      changeIds: false
    })
    wx.navigateTo({
      url: '../../../subPackage/pages/prepare/orderDepList/orderDepList?disId=' + this.data.disId ,
    })
  },


  _updateGbDep(res){
    var idsChangeStock = wx.getStorageSync('idsChangeStock');
    var depTempArr = [];
    var outGbDepIds = idsChangeStock.outGbDepIds;
    if(outGbDepIds.length > 0){
      for(var i = 0; i < outGbDepIds.length;i++){
        var id = outGbDepIds[i];
        var depArr = res.result.data.gbDep;
        if(depArr.length > 0){
          for(var j = 0; j < depArr.length; j++){
            var item  = depArr[j];
            var depId = item.gbDepartmentId;
            if(id == depId){
               item.isSelected = true;
            }
            depTempArr.push(depArr[i]);
          }
        }
        this.setData({
          gbDepArr: depTempArr
        })
      }
    }else{
      var depArr = res.result.data.gbDep;
      var depTempArrNx = [];
      if(depArr.length > 0){
        for(var j = 0; j < depArr.length; j++){
          var item  = depArr[j];
           item.isSelected = false;
           depTempArrNx.push(item);
        }
      }
      this.setData({
        gbDepArr: depTempArrNx
      })
    }

    // 检查是否有部门被选中
    var hasSelectedDep = this._checkHasSelectedDep();
    this.setData({
      changeIds: hasSelectedDep
    })
  },

  _updateNxDep(res){
    var idsChangeStock = wx.getStorageSync('idsChangeStock');
    var depTempArr = [];
    var outNxDepIds = idsChangeStock.outNxDepIds;
    console.log(idsChangeStock.outNxDepIds);
    if(outNxDepIds.length > 0){
        var depArr = res.result.data.nxDep;
        if(depArr.length > 0){
          for(var j = 0; j < depArr.length; j++){
            var item  = depArr[j];
            var depId = item.nxDepartmentId;
            for(var i = 0; i < outNxDepIds.length;i++){
              var id = outNxDepIds[i];
              if(id == depId){
                item.isSelected = true;
             }
            }
            depTempArr.push(item);
          }
        }
      
      this.setData({
        nxDepArr: depTempArr
      })
    }else{
      var depArr = res.result.data.nxDep;
      var depTempArrNx = [];
      if(depArr.length > 0){
        for(var j = 0; j < depArr.length; j++){
          var item  = depArr[j];
           item.isSelected = false;
           depTempArrNx.push(item);
        }
      }
      this.setData({
        nxDepArr: depTempArrNx
      })
    
    }

    // 检查是否有部门被选中
    var hasSelectedDep = this._checkHasSelectedDep();
    this.setData({
      changeIds: hasSelectedDep
    })

  },


  choiceDep(e) {
    var index = e.currentTarget.dataset.index;
    var type = e.currentTarget.dataset.type;
    if (type == 'nx') {
      var depData = "nxDepArr[" + index + "].isSelected";
      var sel = this.data.nxDepArr[index].isSelected;
      if (sel) {
        this.setData({
          [depData]: false
        })
      } else {
        this.setData({
          [depData]: true
        })
      }
    }

    if (type == 'gb') {
      var depData = "gbDepArr[" + index + "].isSelected";
      var sel = this.data.gbDepArr[index].isSelected;
      if (sel) {
        this.setData({
          [depData]: false
        })
      } else {
        this.setData({
          [depData]: true
        })
      }
    }

    // 检查是否有部门被选中
    var hasSelectedDep = this._checkHasSelectedDep();
    this.setData({
      changeIds: hasSelectedDep
    })
  },

  // 检查是否有部门被选中的辅助方法
  _checkHasSelectedDep() {
    var nxDepArr = this.data.nxDepArr || [];
    var gbDepArr = this.data.gbDepArr || [];
    
    // 检查nx部门是否有选中的
    for (var i = 0; i < nxDepArr.length; i++) {
      if (nxDepArr[i].isSelected) {
        return true;
      }
    }
    
    // 检查gb部门是否有选中的
    for (var i = 0; i < gbDepArr.length; i++) {
      if (gbDepArr[i].isSelected) {
        return true;
      }
    }
    
    return false;
  },

  // 已拣货客户详细
  toCarDep(e) {
    console.log("toCarDeptoCarDep");
    if(e.currentTarget.dataset.type == 'nx'){
      var depId = e.currentTarget.dataset.id;
      var name = e.currentTarget.dataset.name;
      wx.navigateTo({
        url: '../depOutOrder/depOutOrder?depFatherId=' + depId
         + '&gbDepFatherId=-1&resFatherId=-1' + '&depName=' + name,
      }) 
    }else{
      var depId = e.currentTarget.dataset.id;
      var name = e.currentTarget.dataset.name;
      wx.navigateTo({
        url: '../depOutOrder/depOutOrder?depFatherId==1&gbDepFatherId='+ depId + '&resFatherId=-1'+'&depName=' + name,
      }) 
    }
      
  },


})