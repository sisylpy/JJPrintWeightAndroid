package com.swolo.lpy.pysx.main.presenter;

import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;

import java.util.List;
import android.content.Context;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public interface MainContract {




    interface StockOutView {
        void getStockGoodsSuccess(List<NxDistributerGoodsShelfEntity> outGoods);
        void getStockGoodsFail(String error);
        void onStockOutFinishSuccess();
        void onStockOutFinishFail(String error);
        void showLoading();  // 显示加载遮罩
        void hideLoading();  // 隐藏加载遮罩
    }

    interface StockOutPresenter {
        void getStockGoods(Integer disId, Integer goodsType);
        void giveOrderWeightListForStockAndFinish(List<NxDepartmentOrdersEntity> orderList);

        void giveOrderWeightListForStockAndFinish(List<NxDepartmentOrdersEntity> orderList, SaveCallback callback);
        void getStockGoodsWithDepIds(Context context, int nxDepId, int gbDepId, int disId, int goodsType);
    }

    public interface SaveCallback {
        void onSaveSuccess();
        void onSaveFail(String error);
    }

    interface LoginView {
        void onLoginSuccess(Object data); // data 可以换成你的 User 类型
        void onLoginFail(String error);
    }

    interface LoginPresenter {
        void login(String phone);
    }


    interface DepartmentListView {
        void getDepartmentListSuccess(List<NxDepartmentEntity> nxDeps, List<GbDepartmentEntity> gbDeps);
        void getDepartmentListFail(String error);
    }
    interface DepartmentListPresenter {
        void getDepartmentList(Integer disId, Integer goodsType);
    }

}
