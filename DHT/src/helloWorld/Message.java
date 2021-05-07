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

    /**
     * 
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * 
     * @return the type
     */
    public int getType() {
        return this.type;
    }

    /**
     * 
     * @return the uuid of the receiver of the message
     */
    public Long getTo() {
        return this.to;
    }

}