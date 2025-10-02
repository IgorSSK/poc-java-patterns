package com.translation.domain.port.output;

import com.translation.domain.model.SensitiveDataMask;

import java.util.List;

public interface SensitiveDataDetector {
    List<SensitiveDataMask> detect(String text);
    String mask(String text, List<SensitiveDataMask> masks);
    String unmask(String text, List<SensitiveDataMask> masks);
}
