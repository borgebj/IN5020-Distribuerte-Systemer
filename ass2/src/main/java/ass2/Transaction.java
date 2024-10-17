package ass2;
import java.io.Serializable;
public class Transaction implements Serializable {
    double timestamp;
    String command;
    String uniqueId;

    @Override
    public String toString() {
        return String.format("%.1f:%s%n",timestamp,  command);
    }
}
