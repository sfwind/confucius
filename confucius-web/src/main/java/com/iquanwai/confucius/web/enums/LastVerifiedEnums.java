package com.iquanwai.confucius.web.enums;


import lombok.Getter;

@Getter
public enum LastVerifiedEnums {

    LAST_VERIFIED_ZERO(0,"无"),
    LAST_VERIFIED_APPROVAL(1,"通过"),
    LAST_VERIFIED_REJECT(2,"拒绝"),
    LAST_VERIFIED_IGNORE(3,"私信"),
    LAST_VERIFIED_EXPIRED(4,"过期");

    private Integer lastVerifiedCode;

    private String lastVerifiedMsg;

    LastVerifiedEnums(Integer lastVerifiedCode, String lastVerifiedMsg) {
        this.lastVerifiedCode = lastVerifiedCode;
        this.lastVerifiedMsg = lastVerifiedMsg;
    }
}
