package helloWorld;

import java.util.UUID;

public class Data {

    private long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    private String content;

    public Data(String content) {
        this.content = content;
    }

    public long getUUID() {
        return this.uuid;
    }

    public String getContent() {
        return this.content;
    }

}
