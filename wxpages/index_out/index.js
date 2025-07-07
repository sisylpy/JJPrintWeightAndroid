const globalData = getApp().globalData;
var load = require('../../../lib/load.js');
let scrollDdirection = 0; // 用来计算滚动的方向

const tabBarHeight = 50; // 根据实际情况调整
const viewBarHeight = 60;

import apiUrl from '../../../config.js'

import {
 
  stockerGetStockGoods,
  stokerGetToStockGoodsWithDepIds, 
  stockerGetStockGoodsKf,
  stockerGetShelfList,
  stockerGetStockGoodsKfPage,
  stockerGetToStockGoodsWithDepIdsKf,
  giveOrderWeightListForStockAndFinish
} from '../../../lib/apiDepOrder'

import {
  disLoginKf
} from '../../../lib/apiDistributer'

Component({
  data:{
      // 左侧：所有货架
  shelfArr: [],         // List<NxDistributerGoodsShelfEntity>
  // 右侧：当前货架页商品
  shelfGoodsArr: [],    // List<NxRetailerGoodsShelfGoodsEntity>
  // 分页参数
  currentPage: 1,
  limit: 15,            // 或者你喜欢的每页条数
  totalPages: 1,
  isLoading: false,
  selectedShelfId: '',  // 当前选中的货架ID
  positionId: '',       // 右侧滚动定位ID
  hasSelectedCustomer: false, // 是否有选择客户
  },

  pageLifetimes: {

    show() {
     

      const app = getApp();
      const navBarHeight = app.globalData.navBarHeight;
      const screenHeight = app.globalData.screenHeight;
      const screenWidth = app.globalData.screenWidth;
      const rpxRatio = 750 / screenWidth;
      const navBarHeightRpx = navBarHeight * rpxRatio;
      const viewBarHeightRpx = viewBarHeight * rpxRatio;
      const tabBarHeightRpx = 100;
      const contentHeight = (screenHeight - navBarHeight  ) * rpxRatio;

      this.setData({ 
        contentHeight: contentHeight,
        navBarHeight: navBarHeightRpx,

        // tabBarHeight: tabBarHeightRpx,
        // viewBarHeight: viewBarHeightRpx,
        leftMenuWidth: 150, // 左侧菜单宽度，单位 rpx
      });

      this.setData({
        windowWidth: globalData.windowWidth * globalData.rpxR,
        windowHeight: globalData.windowHeight * globalData.rpxR,
        statusBarHeight: globalData.statusBarHeight * globalData.rpxR,
        url: apiUrl.server,
        scrollViewTop: 0,
        selectedSub: 0, // 选中的分类
        scrollHeight: 0, // 滚动视图的高度
        toView: 'position0', // 滚动视图跳转的位置
        scrollTopLeft: 0, //  左边滚动位置随着右边分类而滚动
        outNxDepIds: [],
        outNxDepNames: [],
        outGbDepIds: [],
        outGbDepNames: [],
        hasSelectedCustomer: false, // 初始化客户选择状态
      })

      var value = wx.getStorageSync('userInfo');
      if (value) {
        this.setData({
          userInfo: value,
          disId: value.nxDistributerEntity.nxDistributerId,
        })
        var disValue = wx.getStorageSync('disInfo');
        if (disValue) {
          this.setData({
            disInfo: disValue,
          })
        }

        var idsChangeStock = wx.getStorageSync('idsChangeStock');

        if (idsChangeStock) {
          const hasNxDepIds = idsChangeStock.outNxDepIds && idsChangeStock.outNxDepIds.length > 0;
          const hasGbDepIds = idsChangeStock.outGbDepIds && idsChangeStock.outGbDepIds.length > 0;
          const hasSelectedCustomer = hasNxDepIds || hasGbDepIds;
          
          this.setData({
            outNxDepIds: idsChangeStock.outNxDepIds || [],
            outGbDepIds: idsChangeStock.outGbDepIds || [],
            outNxDepNames: idsChangeStock.outNxDepNames || [],
            outGbDepNames: idsChangeStock.outGbDepNames || [],
            hasSelectedCustomer: hasSelectedCustomer
          })
          this._initNxDataKf();
         
        } else {
          // 确保没有客户选择时状态正确
          this.setData({
            hasSelectedCustomer: false
          })
        }
      }else{
        this._login();
      }

     
      
    },
  },


  methods: {

    _login() {
      var that = this;
      wx.login({
        success: (res) => {
          load.hideLoading();
  
          var disUser = {
            nxDiuCode: res.code,
          }
          disLoginKf(disUser)
            .then((res) => {
              console.log(res.result.data);
              if (res.result.code !== -1) { //登陆成功
                this.setData({
                  userInfo: res.result.data.userInfo,
                  disInfo: res.result.data.disInfo,
                })
                wx.setStorageSync('userInfo', res.result.data.userInfo);
                wx.setStorageSync('disInfo', res.result.data.disInfo);
                this._initShelf()
                // if(res.result.data.disInfo.nxDistributerShelfQuantity > 0){
                //   this.setData({
                //     showType: "shelf"
                //   })
                //   this._initDataKf();
                // }else{
                //   this.setData({
                //     showType: "type"
                //   })
                //   this._initNxData();
                // }
  
                
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
  


    changeShowType(){
      if(this.data.showType == 'shelf'){
        this.setData({
        showType: 'type'
        })
      
      }else{
        this.setData({
          showType: 'shelf'
        })
      }

      if (this.data.outNxDepIds.length > 0 || this.data.outGbDepIds.length > 0) {
        this._initNxDataKf();
       
      } 
      
     
    },
    delSearch(){
      wx.removeStorageSync('idsChangeStock');
      this.setData({
        outNxDepIds: [],
        outGbDepIds: [],
        outNxDepNames: [],
        outGbDepNames: [],
        hasSelectedCustomer: false, // 重置客户选择状态
      })
      if(this.data.showType == 'shelf'){
        this._initShelf();
      }else{
        this._initData();
      }
     
    },

    
    _initData() {
      console.log("aa")
      var that = this;
      load.showLoading("获取数据")
      var data = {
        disId: this.data.disId,
      }
      stockerGetStockGoods(data).then(res => {
        load.hideLoading();
        console.log("stock", res.result.data);
        if (res.result.code == 0) {
          this.setData({
            grandList: res.result.data.grandArr,
            waitDepNx: res.result.data.waitDepNx,         
            depOrdersWait: res.result.data.depOrdersWait,
            stockCountOk: res.result.data.stockCountOk,
          })


         
          that.lisenerScroll();

        } else {
          this.setData({
            goodsList: [],
            grandList: [],
            outNxDepIds: [],
            outNxDepNames: [],
            outGbDepIds: [],
            outGbDepNames: [],
          })
          wx.removeStorageSync('idsChangeStock');
          wx.showToast({
            title: res.result.msg,
            icon: 'none'
          })
        }
      })
    },

    
    _initShelf() {
      // 1. 先拉左侧所有货架
      stockerGetShelfList(this.data.disInfo.nxDistributerId).then(res => {
        if (res.result.code === 0) {
          const shelfList = res.result.data || [];
          if (shelfList.length > 0) {
            this.setData({
              shelfArr: shelfList,
              selectedShelfId: shelfList[0].nxDistributerGoodsShelfId,
              currentPage: 1,
              shelfGoodsArr: []
            }, () => {
              // 2. 拉第一个货架的商品
              this._loadShelfGoodsPage(true);
            });
          }
        }
      });
    },
    
    _loadShelfGoodsPage(isRefresh = false, callback) {
      if (this.data.isLoading) return;
      this.setData({ isLoading: true });
      load.showLoading("获取货架商品…");
    
      // 拿到当前选中的 shelfId
      const shelfId = this.data.selectedShelfId;
    
      const params = {
        disId: this.data.disInfo.nxDistributerId,
        shelfId: shelfId,
        page: this.data.currentPage,
        limit: this.data.limit
      };
    
      stockerGetStockGoodsKfPage(params).then(res => {
        load.hideLoading();
        this.setData({ isLoading: false });
    
        if (res.result.code !== 0) {
          return wx.showToast({ title: res.result.msg, icon: 'none' });
        }
    
        const pageData = res.result.page || {};
        const list = pageData.list || [];
        const total = pageData.totalCount || 0;
        const pages = pageData.totalPage || 1;

        // 生成 viewId
        const base = isRefresh ? 0 : this.data.shelfGoodsArr.length;
        list.forEach((item, idx) => {
          item.viewId = 'shelf_' + (base + idx);
        });
    
        // 如果是刷新，直接替换；否则追加
        const merged = isRefresh ? list : this.data.shelfGoodsArr.concat(list);
    
        this.setData({
          shelfGoodsArr: merged,
          totalPages: pages
        }, () => {
          // 确保商品列表中的商品与当前选中的货架对应
          const target = merged.find(g => g.nxDgsgShelfId === shelfId);
          if (target) {
            this.setData({
              positionId: target.viewId
            });
          }
          
          if (typeof callback === 'function') {
            setTimeout(callback, 50);
          }
        });
      });
    },
    
    
    _resetAllShelfData() {
      this.setData({
        shelfArr: [],
        depOrdersWait: [],
        stockCountOk: [],
        outNxDepIds: [],
        outNxDepNames: [],
        outGbDepIds: [],
        outGbDepNames: []
      });
      wx.removeStorageSync('idsChangeStock');
    },

    loadMoreShelf() {
      if (this.data.isLoading) return;
      if (this.data.currentPage >= this.data.totalPages) {
        return wx.showToast({ title: '没有更多架子了', icon: 'none' });
      }
    
      const nextPage = this.data.currentPage + 1;
      this.setData({ isLoading: true, currentPage: nextPage });
      load.showLoading("加载更多中…");
      
      const params = {
        disId: this.data.disInfo.nxDistributerId,
        page: nextPage,
        limit: this.data.limit
      };
    
      stockerGetStockGoodsKfPage(params).then(res => {
        load.hideLoading();
        this.setData({ isLoading: false });
    
        if (res.result.code === 0) {
          const list = res.result.page.list || [];
          // 追加到已渲染列表
          this.setData({
            shelfGoodsArr: this.data.shelfGoodsArr.concat(list)
          });
          // 追加后可重新计算滚动或渲染
          // this.lisenerScrollShelf();
        } else {
          wx.showToast({ title: res.result.msg, icon: 'none' });
        }
      });
    },
    
    _initNxDataKf() {
      var that = this;
      var nxL = this.data.outNxDepIds.length;
      var nxids = 0;
      if (nxL > 0) {
        nxids = this.data.outNxDepIds;
      }
      var gbL = this.data.outGbDepIds.length;
      var gbids = 0;
      if (gbL > 0) {
        gbids = this.data.outGbDepIds;
      }
      var data = {
        nxDepIds: nxids,
        gbDepIds: gbids,
        nxDisId: this.data.disId,
      }
      console.log("nxnxnxnnxnxnxnxnnxnxnaaaa");
      load.showLoading("获取数据中");
      stockerGetToStockGoodsWithDepIdsKf(data).then(res => {
        load.hideLoading();
        console.log("stockerGetToStockGoodsWithDepIdsKf",res.result.data);
        if (res.result.code == 0) {
          this.setData({    
            shelfArr: res.result.data.shelfArr,
          })
          if(res.result.data.shelfArr.length == 0){
            
            wx.removeStorageSync('idsChangeStock');
            wx.navigateBack({delta: 1})
          }
        
        } else {
          this.setData({
            goodsList: [],
            shelfArr: [],
            outNxDepIds: [],
            outNxDepNames: [],
            outGbDepIds: [],
            outGbDepNames: [],
          })
        }
      })
    },
   

    toBack(){
      wx.navigateBack({delta: 1});
    },

    toStock(){
      // 跳转到出库页面
      wx.navigateTo({
        url: '../stock/stock?disId=' + this.data.disId
      });
    },
    
    /**
     * 获取右边每个分类的头部偏移量
     */
    lisenerScroll() {
      // 获取各分类容器距离顶部的距离
      new Promise(resolve => {
        let query = wx.createSelectorQuery();
        for (let i in this.data.grandList) {
          query.select(`#position${i}`).boundingClientRect();
        }
        query.exec(function (res) {
          resolve(res);
        });
      }).then(res => {
        this.data.grandList.forEach((item, index) => {
          item.offsetTop = res[index].top
        })
        this.setData({
          scrollInfo: res,
          grandList: this.data.grandList
        })
      });
    },


    /**
     * 跳转滚动条位置
     */
    toScrollView(e) {
      // const {
      //   selectedSub
      // } = this.data
      const {
        index
      } = e.currentTarget.dataset
      console.log(index);
      console.log("toScoroviewwwww");
      let left_ = 0
      if (index > 3) {
        left_ = (index - 3) * 50 // 左边侧栏item高度为50，可以根据自己的item高度设置
      }
      this.setData({
        selectedSub: index,
        toView: `position${index}`,
        scrollTopLeft: left_
      })
    },

    toScrollViewShelf(e) {
      const { index } = e.currentTarget.dataset;
      const shelfId = this.data.shelfArr[index].nxDistributerGoodsShelfId;
      
      // 先检查当前货架的商品是否已加载
      const existingGoods = this.data.shelfGoodsArr.filter(g => g.nxDgsgShelfId === shelfId);
      
      if (existingGoods.length > 0) {
        // 如果商品已加载，直接滚动到对应位置
        const target = existingGoods[0];
        this.setData({ 
          selectedShelfId: shelfId,
          positionId: target.viewId
        });
      } else {
        // 如果商品未加载，才需要请求网络
        this.setData({ 
          selectedShelfId: shelfId,
          currentPage: 1,
          shelfGoodsArr: []
        });

        const tryScroll = () => {
          const target = this.data.shelfGoodsArr.find(g => 
            g.nxDgsgShelfId === shelfId
          );
          
          if (target) {
            this.setData({ 
              positionId: target.viewId
            });
          }
          else if (this.data.currentPage < this.data.totalPages) {
            // 补页再试
            this.setData({ currentPage: this.data.currentPage + 1 }, () => {
              this._loadShelfGoodsPage(false, tryScroll);
            });
          }
          else {
            wx.showToast({ title: '该货架暂无商品', icon: 'none' });
          }
        };

        // 加载第一页
        this._loadShelfGoodsPage(true, tryScroll);
      }
    },

    
    showIsOutShelf(e){
      console.log("showIsOutshowIsOut");
      var item = e.currentTarget.dataset.item.nxDistributerGoodsEntity;
      var arr = item.nxDepartmentOrdersEntities;
      var temp = [];
      for(var i = 0; i < arr.length; i++){
        var order = arr[i];
        order.hasChoice = true;
        temp.push(order);
      }
      item.nxDepartmentOrdersEntities = temp;

      // 找到对应的货架索引
      const shelfId = item.nxDgsgShelfId;
      const shelfIndex = this.data.shelfArr.findIndex(shelf => shelf.nxDistributerGoodsShelfId === shelfId);
      
      if (shelfIndex !== -1) {
        // 计算左侧滚动位置
        let left_ = 0;
        if (shelfIndex > 3) {
          left_ = (shelfIndex - 3) * 50;
        }
        
        // 更新选中状态和滚动位置
        this.setData({
          selectedSub: shelfIndex,
          scrollTopLeft: left_
        });
      }

      this.setData({
        showDisOutGoods: true,
        item: item,
      })
    },

    showIsOut(e) {
      var item = e.currentTarget.dataset.item;
      var arr = item.nxDepartmentOrdersEntities;
      var temp = [];
      for(var i = 0; i < arr.length; i++){
        var order = arr[i];
        order.hasChoice = true;
        temp.push(order);
      }
      item.nxDepartmentOrdersEntities = temp;
      this.setData({
        showDisOutGoods: true,
        item: item,
      })
    },


    confirm(e) {
      var that = this;
      var arrNeed = e.detail.item.nxDepartmentOrdersEntities;
      var arr = [];
      if (arrNeed.length > 0) {
        for (var i = 0; i < arrNeed.length; i++) {
          var weightValue = arrNeed[i].nxDoWeight;
          var choice = arrNeed[i].hasChoice;
          if (weightValue !== null && weightValue > 0 && choice) {
            arrNeed[i].nxDoPickUserId = this.data.userInfo.nxDistributerUserId;
            console.log("useriid", arrNeed[i].nxDoPickerUserId)
            arr.push(arrNeed[i]);
          }
        }
      }

      if (arr.length > 0) {
        load.showLoading("保存数据中");
        giveOrderWeightListForStockAndFinish(arr).then(res => {
          load.hideLoading();
          if (res.result.code == 0) { 
            console.log("zoahsuishsissiisiisisisiisi");
            console.log(that.data.outNxDepIds, " a" , that.data.showType);
            that._initNxDataKf();
          }else{
            wx.showToast({
              title: 'res.result.msg',
              icon: 'none'
            })
          }
        })
      }
    },


    toWaitDep(e) {
      console.log("ddd")
      wx.setStorageSync('showType', this.data.showType);
      wx.navigateTo({
        url: '../../../subPackage/pages/prepare/orderDepList/orderDepList?disId=' + this.data.disId ,
      })
    },


    toEditHome() {
      wx.setStorageSync('showType', this.data.showType);
      if(this.data.userInfo.nxDiuAdmin == 0){
        wx.navigateTo({
          url: '../../../subPackage/pages/mangement/homePage/homePage',
        })
      }
     
    },

    toLand() {
      wx.setStorageSync('showType', this.data.showType);
      wx.navigateTo({
        url: '../../../subPackage/pages/prepare/land/land',
      })

    },



    toPrint() {
      console.log("toproiint");
      wx.setStorageSync('showType', this.data.showType);
      wx.navigateTo({
        url: '../../../subPackage/pages/prepare/preparePrint/preparePrint?disId=' + this.data.disId ,
      })
    },


    toWeightPage() {
      wx.setStorageSync('showType', this.data.showType);
      wx.navigateTo({
        url: '../../../subPackage/pages/prepare/weightPage/weightPage?disId=' + this.data.disId 
      })
    },

    onNavButtonTap() {
      console.log("ddd")
      wx.navigateTo({
        url: '../../../subPackage/pages/management/homePage/homePage',
      })
     },

    onRightScroll(e) {
      const query = wx.createSelectorQuery();
      query.selectAll('.shelf-section').boundingClientRect();
      query.selectViewport().scrollOffset();

      query.exec(res => {
        const items = res[0];
        const scrollTop = res[1].scrollTop;
        
        // 计算顶部内容的高度（导航栏 + 按钮栏 + 其他顶部内容）
        const topContentHeight = this.data.navBarHeight + this.data.viewBarHeight;
        
        // 找到第一个完全进入视图的商品
        let currentIndex = -1;
        for (let i = 0; i < items.length; i++) {
          const item = items[i];
          // 考虑顶部内容的高度，调整判断条件
          if (item.top >= topContentHeight && item.top < (topContentHeight + 200) && item.bottom > topContentHeight) {
            currentIndex = i;
            break;
          }
        }

        if (currentIndex !== -1) {
          const currentShelfId = this.data.shelfGoodsArr[currentIndex].nxDgsgShelfId;
          const shelfIndex = this.data.shelfArr.findIndex(
            s => s.nxDistributerGoodsShelfId === currentShelfId
          );
          
          if (shelfIndex !== -1 && currentShelfId !== this.data.selectedShelfId) {
            this.setData({
              selectedSub: shelfIndex,
              selectedShelfId: currentShelfId,
              scrollTopLeft: shelfIndex > 3 ? (shelfIndex - 3) * 50 : 0
            });
          }
        }
      });
    },

    scrollToShelf(e) {
      const scrollTop = e.detail.scrollTop; // 获取滚动位置
      const { shelfArr, selectedSub } = this.data;
      
      console.log('滚动位置:', scrollTop);
      console.log('当前选中项:', selectedSub);
      console.log('货架数组:', shelfArr);
      
      // 获取所有货架区域的位置信息
      const query = wx.createSelectorQuery();
      // 修改选择器,使用id="position0"这样的格式
      const selectors = shelfArr.map((_, index) => `#position${index}`);
      console.log('使用的选择器:', selectors);
      
      selectors.forEach(selector => {
        query.select(selector).boundingClientRect();
      });
      query.selectViewport().scrollOffset();
      
      query.exec(res => {
        const items = res.slice(0, -1); // 最后一个元素是视口信息
        const viewportScrollTop = res[res.length - 1].scrollTop;
        
        console.log('获取到的元素位置信息:', items);
        console.log('视口滚动位置:', viewportScrollTop);
        
        // 找到当前视口中第一个可见的货架
        let currentIndex = -1;
        for (let i = 0; i < items.length; i++) {
          const item = items[i];
          if (!item) continue;
          console.log(`第${i}个元素位置:`, item.top);
          // 判断货架是否在视口中可见
          if (item.top >= 0 && item.top < 200) {
            currentIndex = i;
            console.log('找到可见元素,索引:', i);
            break;
          }
        }
        
        // 如果找到了可见的货架且与当前选中的不同,则更新选中状态
        if (currentIndex !== -1 && currentIndex !== selectedSub) {
          console.log('需要更新选中状态:', currentIndex);
          // 计算左侧滚动位置,保持选中项在合适的位置
          let left_ = 0;
          if (currentIndex > 3) {
            left_ = (currentIndex - 3) * 50; // 50是每个货架项的高度
          }
          
          console.log('计算得到的左侧滚动位置:', left_);
          
          // 更新选中状态和滚动位置
          this.setData({
            selectedSub: currentIndex,
            scrollTopLeft: left_,
            toView: `position${currentIndex}`
          }, () => {
            console.log('更新后的状态:', {
              selectedSub: currentIndex,
              scrollTopLeft: left_,
              toView: `position${currentIndex}`
            });
          });
        }
      });
    },

    // methods
  },






})