package top.misec;

public enum BuyRateEnum {

    BIG(1,"大"),
    SMALL(0,"小"),
    ;
    Integer code;
    String desc;
    BuyRateEnum(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static BuyRateEnum getDescByCode(Integer code) {
        for (BuyRateEnum e: BuyRateEnum.values()) {
            if (e.code.equals(code)) {
                return e ;
            }
        }
        return SMALL;
    }

    public Integer getCode() {
        return code;
    }


    public String getDesc() {
        return desc;
    }

}
