package ass2;

import spread.*;

import java.util.Arrays;

public class Listener implements AdvancedMessageListener {

    SpreadGroup[] groupMembers = new SpreadGroup[0];
    Client client;

    public Listener(Client client) {
        this.client = client;
    }


    private void performDeposit(double amount) {
        client.addToAccount(amount, false);
    }

    private void performAddInterest(double amount) {
        double multiplier = (1 + amount / 100);
        client.addToAccount(multiplier, true);
    }

    public void regularMessageReceived(SpreadMessage message) {
        String msg = null;
        try {
            msg = (String) message.getObject();
        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }

        String[] args = msg.split(" ");
        String command = args[0];

        switch (command) {
            case "deposit":
                double amount = Double.parseDouble(args[1]);
                performDeposit(amount);
                break;

            case "addinterest":
                double interest = Double.parseDouble(args[1]);
                performAddInterest(interest);
                break;

        }

        // System.out.printf("\nmsg received: %s\n", msg);
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