package org.skywalking.apm.agent.core.context.ids;

import org.skywalking.apm.network.proto.UniqueId;

/**
 * @author wusheng
 */
public class ID {
    private long part1;
    private long part2;
    private long part3;
    private String encoding;
    private boolean isValid;

    public ID(long part1, long part2, long part3) {
        this.part1 = part1;
        this.part2 = part2;
        this.part3 = part3;
        this.encoding = null;
        this.isValid = true;
    }

    public ID(String encodingString) {
        String[] idParts = encodingString.split("\\.", 3);
        this.isValid = true;
        for (int part = 0; part < 3; part++) {
            try {
                if (part == 0) {
                    part1 = Long.parseLong(idParts[part]);
                } else if (part == 1) {
                    part2 = Long.parseLong(idParts[part]);
                } else {
                    part3 = Long.parseLong(idParts[part]);
                }
            } catch (NumberFormatException e) {
                this.isValid = false;
                break;
            }

        }
    }

    public String encode() {
        if (encoding == null) {
            encoding = toString();
        }
        return encoding;
    }

    @Override public String toString() {
        return part1 + "." + part2 + '.' + part3;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ID id = (ID)o;

        if (part1 != id.part1)
            return false;
        if (part2 != id.part2)
            return false;
        return part3 == id.part3;
    }

    @Override public int hashCode() {
        int result = (int)(part1 ^ (part1 >>> 32));
        result = 31 * result + (int)(part2 ^ (part2 >>> 32));
        result = 31 * result + (int)(part3 ^ (part3 >>> 32));
        return result;
    }

    public boolean isValid() {
        return isValid;
    }

    public UniqueId transform() {
        return UniqueId.newBuilder().addIdParts(part1).addIdParts(part2).addIdParts(part3).build();
    }
}
