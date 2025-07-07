package com.swolo.lpy.pysx.http;

/**
 * Created by hezhisu on 16/9/5.
 */

public class ResponseException extends Exception{

    private String errorMsg;
    public ResponseException(String errorMsg){
        this.errorMsg = errorMsg;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
