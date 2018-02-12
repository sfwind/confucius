package com.iquanwai.confucius.biz.exception;

/**
 * Created by justin on 8/22/16.
 */
public class WeiXinException extends Exception {
    private String errMsg;
    private int errcode;

    public WeiXinException(int errcode, String errMsg) {
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public WeiXinException(String message, String errMsg, int errcode) {
        super(message);
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public WeiXinException(String message, Throwable throwable, String errMsg, int errcode) {
        super(message, throwable);
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public WeiXinException(Throwable throwable, String errMsg, int errcode) {
        super(throwable);
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    @Override
    public String getMessage(){
        return errMsg;
    }

    public int getErrcode() {
        return errcode;
    }
}
