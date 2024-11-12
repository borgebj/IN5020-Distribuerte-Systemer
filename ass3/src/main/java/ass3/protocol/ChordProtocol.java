package ass3.protocol;


import ass3.crypto.ConsistentHashing;
import ass3.p2p.Finger;
import ass3.p2p.NetworkInterface;
import ass3.p2p.Node;
import ass3.p2p.NodeInterface;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.*;


/**
 * This class implements the chord protocol. The protocol is tested using the custom built simulator.
 */
public class ChordProtocol implements Protocol{

    
    // length of the identifier that is used for consistent hashing
    public int m;

    // network object
    public NetworkInterface network;

    // consisent hasing object
    public ConsistentHashing ch;

    // key indexes. tuples of (<key name>, <key index>)
    public HashMap<String, Integer> keyIndexes;

    // node indexes. tuples of (<ring index>, <node object>) - marking which index in ring each node is
    Map<Integer, NodeInterface> indexedNodes;
    // sorted node-indexes, used for finding successors
    List<Integer> sortedIndexes = new ArrayList<>();


    public ChordProtocol(int m){
        this.m = m;
        setHashFunction();
        this.keyIndexes = new HashMap<String, Integer>();
    }



    /**
     * sets the hash function
     */
    public void setHashFunction(){
        this.ch = new ConsistentHashing(this.m);
    }

  

    /**
     * sets the network
     * @param network the network object
     */
    public void setNetwork(NetworkInterface network){
        this.network = network;
    }


    /**
     * sets the key indexes. Those key indexes can be used to  test the lookup operation.
     * @param keyIndexes - indexes of keys
     */
    public void setKeys(HashMap<String, Integer> keyIndexes){
        this.keyIndexes = keyIndexes;
    }



    /**
     *
     * @return the network object
     */
    public NetworkInterface getNetwork(){
        return this.network;
    }




    /**
     * This method builds the overlay network.  It assumes the network object has already been set. It generates indexes
     *     for all the nodes in the network. Based on the indexes it constructs the ring and places nodes on the ring.
     *         algorithm:
     *           1) for each node:
     *           2)     find neighbor based on consistent hash (neighbor should be next to the current node in the ring)
     *           3)     add neighbor to the peer (uses Peer.addNeighbor() method)
     */
    public void buildOverlayNetwork(){

        // Step 1: Create lists to hold the node indices
        // lists holding nodes
        sortedIndexes = new ArrayList<>();                        // list will hold sorted Index-values
        indexedNodes = new HashMap<>();                                         // holds (<index>, <Node>)
        HashMap<String, NodeInterface> topology = this.network.getTopology();   // holds topology : list of nodes

        // Step 2: Assign each node an index based on consistent hashing
        for (Map.Entry<String, NodeInterface> nodeInfo : topology.entrySet()) {
            NodeInterface node = nodeInfo.getValue();
            String nodeName = node.getName();

            // calculate hash, set ID
            int nodeIndex = ch.hash(nodeName);
            topology.get(nodeName).setId(nodeIndex);

            // save indices
            sortedIndexes.add(nodeIndex);
            indexedNodes.put(nodeIndex, node);
        }

        // Step 3: Sort the indices to determine the ring order
        Collections.sort(sortedIndexes);

        // Step 4: Link each node to its successor in the sorted list to form the ring
        for (int i = 0; i < sortedIndexes.size(); i++) {
            int currentIdx = sortedIndexes.get(i);
            int nextIdx = sortedIndexes.get((i + 1) % sortedIndexes.size());

            // finds current, previous, and next node
            NodeInterface currentNode = indexedNodes.get(currentIdx);
            NodeInterface nextNode = indexedNodes.get(nextIdx);

            // adds neighbors
            currentNode.addNeighbor(nextNode.getName(), nextNode); // previous

        }
    }


    /**
     * This method builds the finger table. The finger table is the routing table used in the chord protocol to perform
     * lookup operations. The finger table stores m-entries. Each ith entry points to the ith finger of the node.
     * Each ith entry stores the information of it's neighbor that is responsible for indexes ((n+2^i-1) mod 2^m).
     * i = 1,...,m.
     *
     *Each finger table entry should consists of
     *     1) start value - (n+2^i-1) mod 2^m. i = 1,...,m
     *     2) interval - [finger[i].start, finger[i+1].start)
     *     3) node - first node in the ring that is responsible for indexes in the interval
     */
    @Override
    public void buildFingerTable() {

        // holds topology : list of all nodes
        HashMap<String, NodeInterface> topology = this.network.getTopology();

        for(Map.Entry<String, NodeInterface> nodeInfo : topology.entrySet()){

            // one FingerTable for each Node, each having 'm' entries
            Finger[] fingers = new Finger[this.m];
            NodeInterface node = nodeInfo.getValue();
            int nodeIdx = node.getId();

            for (int i = 1; i <= m; i++) {
                Finger finger = new Finger();

                // start and end of interval
                finger.start = (int) (nodeIdx + Math.pow(2, (i - 1))) % (int) Math.pow(2, m);
                finger.end = (int) (nodeIdx + Math.pow(2, i)) % (int) Math.pow(2, m) - 1;
                if (i == m) finger.end = fingers[0].start - 1;

                // finds the successor for current interval [start, end]
                // finds next node idx after start
                for (int idx : sortedIndexes) {
                    if (idx >= finger.start) {
                        finger.successor = indexedNodes.get(idx);
                        break;
                    }
                }

                // no successor, meaning wrap around to first
                if (finger.successor == null) {
                    finger.successor = indexedNodes.get(sortedIndexes.get(0));
                }

                // set the finger in table
                fingers[i-1] = finger;
            }

            // set routingtable for node
            node.setRoutingTable(fingers);
        }
    }



    /**
     * This method performs the lookup operation.
     *  Given the key index, it starts with one of the nodes in the network and follows through the finger table.
     *  The correct successors would be identified and the request would be checked in their finger tables successively.
     *   Finally, the request will reach the node that contains the data item.
     *
     * @param keyIndex index of the key
     * @param startNode start node
     * @return names of nodes that have been searched and the final node that contains the key
     */
    @Override
    public LookUpResponse lookUp(int keyIndex, String startNode) {

        NodeInterface currentNode = this.network.getNode(startNode);
        LinkedHashSet<String> peersLookedUp = new LinkedHashSet<>();

        do {
            // add peer to visited list
            peersLookedUp.add(currentNode.getName());

            // check if current node holds data
            // stops if we are on node with data we are looking for
            LinkedHashSet<Integer> nodeData = (LinkedHashSet<Integer>) currentNode.getData();
            if (nodeData.contains(keyIndex)) {
                return new LookUpResponse(peersLookedUp, keyIndex, currentNode.getName());
            }
            Finger[] fingers = (Finger[]) currentNode.getRoutingTable();
            NodeInterface nextNode = null;

            // go through all fingers and search through interval
            for (Finger finger : fingers) {
                //Checks if finger has interval between start and end, while handling cases where intervals wrap around
                if ((finger.start <= finger.end && keyIndex >= finger.start && keyIndex <= finger.end) || 
                    (finger.start > finger.end && (keyIndex >= finger.start || keyIndex <= finger.end))) {
                    nextNode = finger.successor;
                    break;
                }
            }

            currentNode = nextNode;

            // if we are on node we started, we have looped and not found data
        } while (!currentNode.getName().equals(startNode));

        return new LookUpResponse(peersLookedUp, keyIndex, currentNode.getName());
    }
}
