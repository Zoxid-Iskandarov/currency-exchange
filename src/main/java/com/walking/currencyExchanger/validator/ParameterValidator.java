package com.walking.currencyExchanger.validator;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class ParameterValidator {
    private static final int CODE_LENGTH = 3;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int MAX_SIGN_LENGTH = 3;
    private static final int CURRENCIES_CODES_LENGTH = 6;

    public static boolean checkCurrencyParameter(String code, String name, String sign) {
        if (code == null || name == null || sign == null) {
            return true;
        }

        if (code.isEmpty() || name.isEmpty() || sign.isEmpty()) {
            return true;
        }

        return code.length() != CODE_LENGTH || name.length() > NAME_MAX_LENGTH || sign.length() > MAX_SIGN_LENGTH;
    }

    public static boolean checkExchangeParameter(String currencyCodes) {
        return currencyCodes.length() != CURRENCIES_CODES_LENGTH;
    }

    public static boolean checkExchangeParameter(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        if (baseCurrencyCode == null || targetCurrencyCode == null || rate == null) {
            return true;
        }

        return baseCurrencyCode.length() != CODE_LENGTH || targetCurrencyCode.length() != CODE_LENGTH
               || rate.compareTo(BigDecimal.ZERO) < 0;
    }
}
