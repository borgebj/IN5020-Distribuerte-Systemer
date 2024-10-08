package ass2;

import spread.*;

import java.util.Arrays;

public class Listener implements AdvancedMessageListener {

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
        System.out.printf("\nmembers updated: %s\n\n", Arrays.toString(spreadMessage.getMembershipInfo().getMembers()));
    }

}