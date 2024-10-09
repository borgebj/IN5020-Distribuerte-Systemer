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

    private void performCommand(String command) {

        System.out.println("Executing " + command);

        // parse action and amount
        String[] args = command.split(" ");
        String action = args[0];
        double amount = Double.parseDouble(args[1]);

        // perform the requested action
        switch (action) {
            case "deposit":
                client.addToAccount(amount, false);
                break;

            case "addinterest":
                client.addToAccount(amount, true);
                break;
        }
    }

    private void removeTransaction(Transaction tx) {
        String removeId = tx.uniqueId;
        Iterator<Transaction> iterator = client.outstandingCollection.iterator();

        while (iterator.hasNext()) {
            Transaction t = iterator.next();
            String currentId = t.uniqueId;

            if (Objects.equals(currentId, removeId)) {
                iterator.remove();
                break;
            }
        }
    }


    public void regularMessageReceived(SpreadMessage message) {
        ArrayList<Transaction> outstanding = null;
        try {
            outstanding = (ArrayList<Transaction>) message.getObject();
        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }

         System.out.printf("from %s = %s\n", message.getSender(), outstanding);

        // go through outstanding and perform commands
        for (Transaction tx : outstanding) {

            // removes transaction from outstanding collection, add to executed
            removeTransaction(tx);
            client.executedList.add(tx);
            client.order_counter++;

            // performs the command
            performCommand(tx.command);
        }
        System.out.println();
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        groupMembers = spreadMessage.getMembershipInfo().getMembers();

        System.out.printf("\nmembers updated: %s \t (%d member/s)\n", Arrays.toString(groupMembers), groupMembers.length);
    }

    public int getMembers() {
        return groupMembers.length;
    }
}