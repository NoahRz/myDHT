package helloWorld;

import peersim.edsim.*;

public class Message {

    public final static int HELLOWORLD = 0;

    private int type;
    private String content;
    private Long to;

    Message(int type, String content, Long to) {
        this.type = type;
        this.content = content;
        this.to = to;
    }

    public String getContent() {
        return this.content;
    }

    public int getType() {
        return this.type;
    }

    public Long getTo() {
        return this.to;
    }

}