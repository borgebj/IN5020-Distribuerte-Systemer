package ass2;

import spread.*;

import ass2.ClientInterface;

import java.util.ArrayList;
import java.util.List;

public class Listener implements AdvancedMessageListener {
    private int numOfReps;
    private SpreadConnection connection;

    public Listener(int numOfReps, SpreadConnection connection){
        System.out.println("Creates connetion listener");
        this.numOfReps= numOfReps;
        this.connection = connection;

    }

    @Override
    public void regularMessageReceived(SpreadMessage message) {
        String msg = null;
        try {
            msg = (String) message.getObject();

        } catch (SpreadException e) {
            throw new RuntimeException(e);
        }
        System.out.println("listener: " + msg);
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        MembershipInfo membershipInfo =spreadMessage.getMembershipInfo();
        System.out.println(spreadMessage.getMembershipInfo().getMembers());

        SpreadGroup[] currentMembers = membershipInfo.getMembers();

        for (SpreadGroup member : currentMembers) {
            System.out.println(member);
        }

    
    }
}