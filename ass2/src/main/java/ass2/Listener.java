package ass2;

import spread.*;

import java.util.*;

public class Listener implements AdvancedMessageListener {

    SpreadGroup[] groupMembers = new SpreadGroup[0];
    Client client;
    int id;

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
        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }

//         System.out.printf("from %s = %s\n", message.getSender().toString().split("group")[0], client.printOutstanding(outstanding));

        // go through outstanding and perform commands
        for (Transaction tx : outstanding) {

            process(tx);

        }
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        groupMembers = spreadMessage.getMembershipInfo().getMembers();

//        System.out.printf("\nmembers updated: %s \t (%d member/s)\n", Arrays.toString(groupMembers), groupMembers.length);
    }

    public int getMembers() {
        return groupMembers.length;
    }
}