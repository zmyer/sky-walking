package org.skywalking.apm.collector.agentstream.jetty.handler.reader;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import org.skywalking.apm.network.proto.TraceSegmentReference;

/**
 * @author pengys5
 */
public class ReferenceJsonReader implements StreamJsonReader<TraceSegmentReference> {

    private UniqueIdJsonReader uniqueIdJsonReader = new UniqueIdJsonReader();

    private static final String PARENT_TRACE_SEGMENT_ID = "ts";
    private static final String PARENT_APPLICATION_ID = "ai";
    private static final String PARENT_SPAN_ID = "si";
    private static final String PARENT_SERVICE_ID = "vi";
    private static final String PARENT_SERVICE_NAME = "vn";
    private static final String NETWORK_ADDRESS_ID = "ni";
    private static final String NETWORK_ADDRESS = "nn";
    private static final String ENTRY_APPLICATION_INSTANCE_ID = "ea";
    private static final String ENTRY_SERVICE_ID = "ei";
    private static final String ENTRY_SERVICE_NAME = "en";
    private static final String REF_TYPE_VALUE = "rv";

    @Override public TraceSegmentReference read(JsonReader reader) throws IOException {
        TraceSegmentReference.Builder builder = TraceSegmentReference.newBuilder();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case PARENT_TRACE_SEGMENT_ID:
                    builder.setParentTraceSegmentId(uniqueIdJsonReader.read(reader));
                    break;
                case PARENT_APPLICATION_ID:
                    builder.setParentApplicationInstanceId(reader.nextInt());
                    break;
                case PARENT_SPAN_ID:
                    builder.setParentSpanId(reader.nextInt());
                    break;
                case PARENT_SERVICE_ID:
                    builder.setParentServiceId(reader.nextInt());
                    break;
                case PARENT_SERVICE_NAME:
                    builder.setParentServiceName(reader.nextString());
                    break;
                case NETWORK_ADDRESS_ID:
                    builder.setNetworkAddressId(reader.nextInt());
                    break;
                case NETWORK_ADDRESS:
                    builder.setNetworkAddress(reader.nextString());
                    break;
                case ENTRY_APPLICATION_INSTANCE_ID:
                    builder.setEntryApplicationInstanceId(reader.nextInt());
                    break;
                case ENTRY_SERVICE_ID:
                    builder.setEntryServiceId(reader.nextInt());
                    break;
                case ENTRY_SERVICE_NAME:
                    builder.setEntryServiceName(reader.nextString());
                    break;
                case REF_TYPE_VALUE:
                    builder.setRefTypeValue(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return builder.build();
    }
}
