package helloWorld;

import peersim.edsim.*;

public class Message {

    public final static int HELLOWORLD = 0;

    private int type;
    private String content;
    private long to;

    Message(int type, String content, long to) {
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

    public long getTo() {
        return this.to;
    }

}