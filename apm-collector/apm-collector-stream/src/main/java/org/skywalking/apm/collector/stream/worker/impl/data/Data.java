package org.skywalking.apm.collector.stream.worker.impl.data;

import org.skywalking.apm.collector.remote.grpc.proto.RemoteData;
import org.skywalking.apm.collector.stream.worker.selector.AbstractHashMessage;

/**
 * @author pengys5
 */
public class Data extends AbstractHashMessage {
    private int defineId;
    private final int stringCapacity;
    private final int longCapacity;
    private final int floatCapacity;
    private final int integerCapacity;
    private String[] dataStrings;
    private Long[] dataLongs;
    private Float[] dataFloats;
    private Integer[] dataIntegers;

    public Data(String id, int defineId, int stringCapacity, int longCapacity, int floatCapacity, int integerCapacity) {
        super(id);
        this.defineId = defineId;
        this.dataStrings = new String[stringCapacity];
        this.dataLongs = new Long[longCapacity];
        this.dataFloats = new Float[floatCapacity];
        this.dataIntegers = new Integer[integerCapacity];
        this.stringCapacity = stringCapacity;
        this.longCapacity = longCapacity;
        this.floatCapacity = floatCapacity;
        this.integerCapacity = integerCapacity;
    }

    public void setDataString(int position, String value) {
        dataStrings[position] = value;
    }

    public void setDataLong(int position, Long value) {
        dataLongs[position] = value;
    }

    public void setDataFloat(int position, Float value) {
        dataFloats[position] = value;
    }

    public void setDataInteger(int position, Integer value) {
        dataIntegers[position] = value;
    }

    public String getDataString(int position) {
        return dataStrings[position];
    }

    public Long getDataLong(int position) {
        return dataLongs[position];
    }

    public Float getDataFloat(int position) {
        return dataFloats[position];
    }

    public Integer getDataInteger(int position) {
        return dataIntegers[position];
    }

    public String id() {
        return dataStrings[0];
    }

    public int getDefineId() {
        return defineId;
    }

    public RemoteData serialize() {
        RemoteData.Builder builder = RemoteData.newBuilder();
        builder.setIntegerCapacity(integerCapacity);
        builder.setFloatCapacity(floatCapacity);
        builder.setStringCapacity(stringCapacity);
        builder.setLongCapacity(longCapacity);

        for (int i = 0; i < dataStrings.length; i++) {
            builder.setDataStrings(i, dataStrings[i]);
        }
        for (int i = 0; i < dataIntegers.length; i++) {
            builder.setDataIntegers(i, dataIntegers[i]);
        }
        for (int i = 0; i < dataFloats.length; i++) {
            builder.setDataFloats(i, dataFloats[i]);
        }
        for (int i = 0; i < dataLongs.length; i++) {
            builder.setDataLongs(i, dataLongs[i]);
        }
        return builder.build();
    }
}