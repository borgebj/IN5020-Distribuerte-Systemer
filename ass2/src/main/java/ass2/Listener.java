package ass2;

import spread.*;

import java.util.Arrays;

public class Listener implements AdvancedMessageListener {

    int clientId;
    public Listener(int clientId) {
        this.clientId = clientId;
    }

    public void regularMessageReceived(SpreadMessage message) {
        String msg = null;
        try {
            msg = (String) message.getObject();
        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("[%d] msg received: %s\n", clientId, msg);
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        System.out.printf("[%d] member: %s\n", clientId, Arrays.toString(spreadMessage.getMembershipInfo().getMembers()));
    }

}