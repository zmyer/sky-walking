package org.skywalking.apm.collector.storage.define;

import org.skywalking.apm.collector.core.stream.Operation;

/**
 * @author pengys5
 */
public class Attribute {
    private final String name;
    private final AttributeType type;
    private final Operation operation;

    public Attribute(String name, AttributeType type, Operation operation) {
        this.name = name;
        this.type = type;
        this.operation = operation;
    }

    public String getName() {
        return name;
    }

    public AttributeType getType() {
        return type;
    }

    public Operation getOperation() {
        return operation;
    }
}
