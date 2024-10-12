package ass2;

// utility
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// spread imports
import spread.SpreadGroup;
import spread.SpreadMessage;
import spread.SpreadException;
import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import java.util.HashSet;

public class Listener implements AdvancedMessageListener {

    int id;
    Client client;
    SpreadGroup[] groupMembers = new SpreadGroup[0];
    int numOfReps;

    
   Set< String> previouslyConnectedMembers = new HashSet<String>();
    public Listener(Client client, int id, int numOfReps) {
        this.client = client;
        this.id = id;
        this.numOfReps = numOfReps;
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
          //  if(message.getObject() instanceof ArrayList)
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
                //client.catchUpClient(newMember); 
            } else {
               // System.out.println("Client joined: " + newMemberName);
                previouslyConnectedMembers.add(newMemberName); 
            }
        }
        for (SpreadGroup member : groupMembers) {
            previouslyConnectedMembers.add(member.toString());
        }
     
        //System.out.println("previously connceted " + Arrays.toString(previouslyConnectedMembers.toArray()));
       // System.out.printf("\nmembers updated: %s \t (%d member/s)\n", Arrays.toString(groupMembers), groupMembers.length);
    }


    public int getMembers() {
        return groupMembers.length;
    }

   
}