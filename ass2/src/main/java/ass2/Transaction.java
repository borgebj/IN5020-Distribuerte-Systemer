package ass2;
import java.io.Serializable;
public class Transaction implements Serializable {
    String command;
    String uniqueId;

    @Override
    public String toString() {
        return String.format("%s %s\n", command, uniqueId);
    }
}
