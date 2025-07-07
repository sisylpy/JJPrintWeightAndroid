package com.swolo.lpy.pysx.main;

import java.util.ArrayList;
import java.util.List;

public class MockData {
    public static List<Object> mockUnpickedData() {
        List<Object> list = new ArrayList<>();
        DepOutOrderGroupAdapter.GreatGrand gg = new DepOutOrderGroupAdapter.GreatGrand();
        gg.fatherGoodsEntities = new ArrayList<>();
        DepOutOrderGroupAdapter.FatherGoodsEntities grand = new DepOutOrderGroupAdapter.FatherGoodsEntities();
        grand.nxDfgFatherGoodsName = "特菜";
        grand.nxDistributerPurchaseGoodsEntities = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            DepOutOrderGroupAdapter.PurchaseGoodsEntities pur = new DepOutOrderGroupAdapter.PurchaseGoodsEntities();
            pur.nxDistributerGoodsEntity = new DepOutOrderGroupAdapter.DistributerGoodsEntity();
            pur.nxDistributerGoodsEntity.nxDgGoodsName = i == 0 ? "秋葵" : "芦苇";
            pur.nxDistributerGoodsEntity.nxDgGoodsStandardname = "斤";
            pur.nxDepartmentOrdersEntities = new ArrayList<>();
            com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order = new com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity();
            order.indexStr = String.valueOf(i + 1) + ".";
            order.nxDoQuantity = i == 0 ? "2" : "5";
            order.nxDoStandard = "斤";
            order.nxDoWeight = i == 0 ? "2" : "5";
            pur.nxDepartmentOrdersEntities.add(order);
            grand.nxDistributerPurchaseGoodsEntities.add(pur);
        }
        gg.fatherGoodsEntities.add(grand);
        list.add(gg);
        return list;
    }
    public static List<Object> mockPickedData() {
        List<Object> list = new ArrayList<>();
        DepOutOrderGroupAdapter.GreatGrand gg = new DepOutOrderGroupAdapter.GreatGrand();
        gg.fatherGoodsEntities = new ArrayList<>();
        DepOutOrderGroupAdapter.FatherGoodsEntities grand1 = new DepOutOrderGroupAdapter.FatherGoodsEntities();
        grand1.nxDfgFatherGoodsName = "叶花菜";
        grand1.nxDistributerPurchaseGoodsEntities = new ArrayList<>();
        DepOutOrderGroupAdapter.PurchaseGoodsEntities pur1 = new DepOutOrderGroupAdapter.PurchaseGoodsEntities();
        pur1.nxDistributerGoodsEntity = new DepOutOrderGroupAdapter.DistributerGoodsEntity();
        pur1.nxDistributerGoodsEntity.nxDgGoodsName = "油菜";
        pur1.nxDistributerGoodsEntity.nxDgGoodsStandardname = "斤";
        pur1.nxDepartmentOrdersEntities = new ArrayList<>();
        com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order1 = new com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity();
        order1.indexStr = "1.";
        order1.nxDoQuantity = "7";
        order1.nxDoStandard = "斤";
        order1.nxDoWeight = "7";
        pur1.nxDepartmentOrdersEntities.add(order1);
        grand1.nxDistributerPurchaseGoodsEntities.add(pur1);
        gg.fatherGoodsEntities.add(grand1);
        DepOutOrderGroupAdapter.FatherGoodsEntities grand2 = new DepOutOrderGroupAdapter.FatherGoodsEntities();
        grand2.nxDfgFatherGoodsName = "根茎";
        grand2.nxDistributerPurchaseGoodsEntities = new ArrayList<>();
        DepOutOrderGroupAdapter.PurchaseGoodsEntities pur2 = new DepOutOrderGroupAdapter.PurchaseGoodsEntities();
        pur2.nxDistributerGoodsEntity = new DepOutOrderGroupAdapter.DistributerGoodsEntity();
        pur2.nxDistributerGoodsEntity.nxDgGoodsName = "花生芽";
        pur2.nxDistributerGoodsEntity.nxDgGoodsStandardname = "盒";
        pur2.nxDepartmentOrdersEntities = new ArrayList<>();
        com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order2 = new com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity();
        order2.indexStr = "1.";
        order2.nxDoQuantity = "5";
        order2.nxDoStandard = "盒";
        order2.nxDoWeight = "5";
        pur2.nxDepartmentOrdersEntities.add(order2);
        grand2.nxDistributerPurchaseGoodsEntities.add(pur2);
        gg.fatherGoodsEntities.add(grand2);
        list.add(gg);
        return list;
    }
} 