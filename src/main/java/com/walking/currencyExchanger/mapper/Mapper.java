package com.walking.currencyExchanger.mapper;

public interface Mapper<F, T> {
    T map(F from);
}
