package ass2;

import spread.*;

import ass2.ClientInterface;

import java.util.ArrayList;
import java.util.List;

public class Listener implements AdvancedMessageListener {

    public void regularMessageReceived(SpreadMessage message) {
        String msg = null;
        try {
            msg = (String) message.getObject();

        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }
        System.out.println(msg);
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        System.out.println(spreadMessage.getMembershipInfo().getMembers());
    }
}