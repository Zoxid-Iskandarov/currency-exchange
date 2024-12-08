package com.walking.currencyExchanger.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {
    private Long id;
    private String code;
    private String name;
    private String sign;
}
