package ass2;

import spread.*;

import java.util.Arrays;

public class Listener implements AdvancedMessageListener {

    SpreadGroup[] groupMembers = new SpreadGroup[0];

    public void regularMessageReceived(SpreadMessage message) {
        String msg = null;
        try {
            msg = (String) message.getObject();
        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("\nmsg received: %s\n", msg);
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        groupMembers = spreadMessage.getMembershipInfo().getMembers();

        System.out.printf("\nmembers updated: %s \t (%d members)\n\n", Arrays.toString(groupMembers), groupMembers.length);
    }

    public int getMembers() {
        return groupMembers.length;
    }
}