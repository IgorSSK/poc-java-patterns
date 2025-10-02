package com.translation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDataMask {
    private String placeholder;
    private String originalValue;
    private SensitiveDataType type;
    
    public enum SensitiveDataType {
        CPF,
        CNPJ,
        EMAIL,
        PHONE,
        CREDIT_CARD,
        IP_ADDRESS,
        PERSON_NAME,
        ADDRESS
    }
}
