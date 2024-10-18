package ass2;

// utility
import java.util.ArrayList;

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

    /**
     * Given a transaction-object and a message, process it by executing the corresponding command
     *
     * @param tx Transaction containing command
     * @param msg message containing sender
     */
    private void process(Transaction tx, SpreadMessage msg) {

        // perform the requested action
        switch (tx.command.split(" ")[0]) {
            case "deposit":
                client.addToAccount(tx, false);
                break;

            case "addinterest":
                client.addToAccount(tx, true);
                break;

            // performs only for the receiver who sendt it
            case "getsyncedbalance":
                if (msg.getSender().toString().equals(spreadIdentifier)) {
                    client.getQuickBalance(true, true);
                    client.removeFromOutstanding(tx);
                    client.outstanding_counter--; // because getSynced adds one
                }
        }
    }


    /**
     * Called every time a message is picked up (multicasted)
     * Processes commands in the message-list
     *
     * @param message multicasted message
     */
    @Override
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

    /**
     * Called every time a member has joined the connection
     *
     * @param spreadMessage message containing member info
     */
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

    /**
     * @param member A spreadgroup member
     * @return the ID of given member
     */
    public String getIdFromMemberShipInfo(SpreadGroup member ){
        return member.toString().split("#")[1];
    }

    /**
     * @return members of current Spread-session through memberinfo
     */
    public int getMembers() {
        return groupMembers.length;
    }
}