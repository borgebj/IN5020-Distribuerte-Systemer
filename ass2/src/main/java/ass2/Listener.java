package ass2;

import spread.*;

import java.util.*;

public class Listener implements AdvancedMessageListener {

    SpreadGroup[] groupMembers = new SpreadGroup[0];


    Client client;
    int id;
    int numOfReps;
    private Set<String> previouslyConnectedMembers = new HashSet<>();

    public Listener(Client client, int id, int numOfReps) {
        this.client = client;
        this.id = id;
        this.numOfReps = numOfReps;
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
        Transaction txBalance = null; 
        for (Transaction tx : outstanding) {
            String[] parts = tx.uniqueId.split(" ");
            int uniqueId = Integer.parseInt(parts[1]);
            // removes transaction from outstanding collection, add to executed
           
            if(client.checkTxStatus(uniqueId).equals("Executed")){
                continue;
            }
            removeTransaction(tx);
            client.executedList.add(tx);
            client.order_counter++;

            // performs the command
            performCommand(tx.command);

        }
       
        //performCommand(txBalance.command);
        System.out.println();
    }

    /*
     * 1. Check when a client disconnects
     * 2. Check which client disconnected
     * 3. If that cient or any other client were to connect then catch them up to speed 
     * 4. Need to recognize when the clients are in exection mode 
     * 
     * 
     */


    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
             // Ensure the set is always up-to-date with group members
             
             
        MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
        groupMembers = membershipInfo.getMembers();
      
        
        if (membershipInfo.isCausedByDisconnect()) {
            System.out.printf("\nClient  %s disconnected\n", membershipInfo.getLeft());
        }
    
        if (membershipInfo.isCausedByJoin()) {
            SpreadGroup newMember = membershipInfo.getJoined();
            String newMemberName = newMember.toString();
           
      
    
            // Use direct member name for contains check
            if (previouslyConnectedMembers.contains(newMemberName)) {
                System.out.printf("Client %s reconnected%n", newMemberName);
                client.catchUpClient(newMember); 
            } else {
                System.out.println("Client joined: " + newMemberName);
                previouslyConnectedMembers.add(newMemberName); 
            }
        }
        for (SpreadGroup member : groupMembers) {
            previouslyConnectedMembers.add(member.toString());
        }
     
        //System.out.println("previously connceted " + Arrays.toString(previouslyConnectedMembers.toArray()));
        System.out.printf("\nmembers updated: %s \t (%d member/s)\n", Arrays.toString(groupMembers), groupMembers.length);
    }

    public int getMembers() {
        return groupMembers.length;
    }
}