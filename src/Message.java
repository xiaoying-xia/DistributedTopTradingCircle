import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID=1L;
    int index; // index information
    int value; // numeric information
    boolean flag; // boolean information

    public Message(int index, int value, boolean flag) {
        this.index = index;
        this.value = value;
        this.flag = flag;
    }
}
