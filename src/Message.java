import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID=1L;

    MessageTypes type; // msg type
    int index; // index of the sender
    int value; // value carried by the msg

    public Message(MessageTypes type, int index, int value) {
        this.type = type;
        this.index = index;
        this.value = value;
    }
}
