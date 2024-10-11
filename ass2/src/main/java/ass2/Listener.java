package ass2;

// utility
import java.util.ArrayList;

// spread imports
import spread.SpreadGroup;
import spread.SpreadMessage;
import spread.SpreadException;
import spread.AdvancedMessageListener;


public class Listener implements AdvancedMessageListener {

    int id;
    Client client;
    SpreadGroup[] groupMembers = new SpreadGroup[0];


    public Listener(Client client, int id) {
        this.client = client;
        this.id = id;
    }

    private void process(Transaction tx) {

        // perform the requested action
        switch (tx.command.split(" ")[0]) {
            case "deposit":
                client.addToAccount(tx, false);
                break;

            case "addinterest":
                client.addToAccount(tx, true);
                break;
        }
    }


    public void regularMessageReceived(SpreadMessage message) {
        ArrayList<Transaction> outstanding = null;
        try {
            outstanding = (ArrayList<Transaction>) message.getObject();

            // System.out.printf("from %s = %s\n", message.getSender().toString().split("group")[0], (outstanding));

            // go through outstanding and perform commands
            for (Transaction tx : outstanding) {
                process(tx);
            }

        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        groupMembers = spreadMessage.getMembershipInfo().getMembers();

        // System.out.printf("\nmembers updated: %s \t (%d member/s)\n", Arrays.toString(groupMembers), groupMembers.length);
    }


    public int getMembers() {
        return groupMembers.length;
    }
}