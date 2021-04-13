package exchange.core.common.api.binary;

import lombok.Getter;

@Getter
public enum BinaryCommandType {

    ADD_ACCOUNTS(1002),
    ADD_SYMBOLS(1003);

    private final int code;

    BinaryCommandType(int code) {
        this.code = code;
    }

    public static BinaryCommandType of(int code) {

        switch (code) {
            case 1002:
                return ADD_ACCOUNTS;
            case 1003:
                return ADD_SYMBOLS;
            default:
                throw new IllegalArgumentException("unknown BinaryCommandType:" + code);
        }

    }

}
