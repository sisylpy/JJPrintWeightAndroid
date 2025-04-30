package com.swolo.lpy.pysx.main.modal;

/**
 * Created by hezhisu on 2017/1/9.
 */

public class SortModel extends Contact {


    public SortModel(String name, String number, String sortKey) {
        super(name, number, sortKey);
    }

    public String sortLetters; //显示数据拼音的首字母

    public SortToken sortToken=new SortToken();
}
