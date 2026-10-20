package com.example.score.common;

/**
 * 业务异常：消息会直接展示给用户，因此文案要写成用户能看懂的话。
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BizException(String message) {
        super(message);
    }
}
