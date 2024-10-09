package ass2;

import spread.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Listener implements AdvancedMessageListener {

    SpreadGroup[] groupMembers = new SpreadGroup[0];
    Client client;
    int id;

    public Listener(Client client, int id) {
        this.client = client;
        this.id = id;
    }


    public void regularMessageReceived(SpreadMessage message) {
        ArrayList<Transaction> outstanding = null;
        try {
            outstanding = (ArrayList<Transaction>) message.getObject();
        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }

         System.out.printf("[from %s]\n", message.getSender());

        // go through outstanding and perform commands
        for (Transaction tx : outstanding) {
            System.out.println("> " + tx);
        }
        System.out.println();
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        groupMembers = spreadMessage.getMembershipInfo().getMembers();

        System.out.printf("\nmembers updated: %s \t (%d member/s)\n\n", Arrays.toString(groupMembers), groupMembers.length);
    }

    public int getMembers() {
        return groupMembers.length;
    }
}