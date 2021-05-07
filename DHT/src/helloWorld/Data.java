package helloWorld;

import java.util.UUID;

public class Data {

    private long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    private String content;

    public Data(String content) {
        this.content = content;
    }

    /**
     * 
     * @return the uuid
     */
    public long getUUID() {
        return this.uuid;
    }

    /**
     * 
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

}
