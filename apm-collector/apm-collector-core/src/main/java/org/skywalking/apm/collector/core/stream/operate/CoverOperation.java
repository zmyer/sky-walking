package org.skywalking.apm.collector.core.stream.operate;

import org.skywalking.apm.collector.core.stream.Operation;

/**
 * @author pengys5
 */
public class CoverOperation implements Operation {
    @Override public String operate(String newValue, String oldValue) {
        return newValue;
    }

    @Override public Long operate(Long newValue, Long oldValue) {
        return newValue;
    }

    @Override public Double operate(Double newValue, Double oldValue) {
        return newValue;
    }

    @Override public Integer operate(Integer newValue, Integer oldValue) {
        return newValue;
    }

    @Override public Boolean operate(Boolean newValue, Boolean oldValue) {
        return newValue;
    }

    @Override public byte[] operate(byte[] newValue, byte[] oldValue) {
        return newValue;
    }
}
