package ass2;

// utility
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// spread imports
import spread.SpreadGroup;
import spread.SpreadMessage;
import spread.SpreadException;
import spread.AdvancedMessageListener;
import spread.MembershipInfo;


public class Listener implements AdvancedMessageListener {

    int id;
    Client client;
    String spreadIdentifier; // how client is presented through Spread
    String accountName;    SpreadGroup[] groupMembers = new SpreadGroup[0];


    public Listener(Client client, int id, String accountName) {
        this.client = client;
        this.id = id;
        this.accountName = accountName;
        this.spreadIdentifier = String.format("#%d#%s", id, accountName);
    }

    private void process(Transaction tx, SpreadMessage msg) {

        // perform the requested action
        switch (tx.command.split(" ")[0]) {
            case "deposit":
                client.addToAccount(tx, false);
                break;

            case "addinterest":
                client.addToAccount(tx, true);
                break;

            case "getsyncedbalance":
                if (msg.getSender().toString().equals(spreadIdentifier)) {
                    client.getQuickBalance(true);
                    client.addToAccount(tx, false);  // <-- Makes sure it's removed from outstanding
                }
        }
    }


    public void regularMessageReceived(SpreadMessage message) {
        ArrayList<Transaction> outstanding = null;
        try {
            outstanding = (ArrayList<Transaction>) message.getObject();

            // go through outstanding and perform commands
            for (Transaction tx : outstanding) {
                process(tx, message);
            }

        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

      // Ensure the set is always up-to-date with group members             
        MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
        groupMembers = membershipInfo.getMembers();

        //Prints Ids of joining and leaving members
        if (membershipInfo.isCausedByDisconnect()) {
            SpreadGroup memberDisconnected =  membershipInfo.getLeft();
            System.out.printf("\nClient %s disconnected\n", getIdFromMemberShipInfo(memberDisconnected));
        }
    
        if (membershipInfo.isCausedByJoin()) {
            SpreadGroup newMember = membershipInfo.getJoined();
            System.out.printf("\nClient %s Joined\n", getIdFromMemberShipInfo(newMember));
        }
        
    }

    public String getIdFromMemberShipInfo(SpreadGroup member ){
        return member.toString().split("#")[1];
    }

    public int getMembers() {
        return groupMembers.length;
    }
}