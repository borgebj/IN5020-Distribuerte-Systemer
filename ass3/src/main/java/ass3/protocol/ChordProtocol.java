package ass3.protocol;


import ass3.crypto.ConsistentHashing;
import ass3.p2p.Finger;
import ass3.p2p.NetworkInterface;
import ass3.p2p.Node;
import ass3.p2p.NodeInterface;


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

        // Step 1: Create a list to hold the node indices
        List<Integer> sortedIndexes = new ArrayList<>();
        Map<Integer, NodeInterface> indexedNodes = new HashMap<>(); // Map to store nodes by index

        System.out.println("=========== CREATING NodeIndexes: =================================");

        // Step 2: Assign each node an index based on consistent hashing
        for (Map.Entry<String, NodeInterface> nodeInfo : this.network.getTopology().entrySet()) {
            String key = nodeInfo.getKey();
            NodeInterface node = nodeInfo.getValue();
            String nodeName = node.getName();

            // calculate hash, set ID
            int nodeIndex = ch.hash(nodeName);
            node.setId(nodeIndex);
            sortedIndexes.add(nodeIndex);
            indexedNodes.put(nodeIndex, node);

            System.out.printf("%s - %d\n", nodeName, nodeIndex);
        }

        // Step 3: Sort the indices to determine the ring order
        Collections.sort(sortedIndexes);
        System.out.println("Sorted Node Indexes: " + sortedIndexes);
        System.out.println("=====================================================================");

        // Step 4: Link each node to its successor in the sorted list to form the ring
        for (int i = 0; i < sortedIndexes.size(); i++) {
            int currentIdx = sortedIndexes.get(i);
            int nextIdx = sortedIndexes.get((i + 1) % sortedIndexes.size());
            int prevIdx = sortedIndexes.get((i - 1 + sortedIndexes.size()) % sortedIndexes.size());

            // finds current, previous, and next node
            NodeInterface currentNode = indexedNodes.get(currentIdx);
            NodeInterface prevNode = indexedNodes.get(prevIdx);
            NodeInterface nextNode = indexedNodes.get(nextIdx);

            // adds neighbors
            currentNode.addNeighbor(prevNode.getName(), prevNode);
            currentNode.addNeighbor(nextNode.getName(), nextNode);

            System.out.printf("%s (idx %d) \t -> \t Neighbor: %s\n", currentNode.getName(), currentIdx, currentNode.getNeighbors());
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

        int maxId = (int) Math.pow(2, m);
        //int startValue = n + Math.pow(2, (i-1)) % Math.pow(2, m);
        
        for(Map.Entry<String, NodeInterface> nodeInfo : this.network.getTopology().entrySet()){

            Finger[] fingers = new Finger[this.m];
            NodeInterface currentNode= nodeInfo.getValue() ;
            int currentNodeId = currentNode.getId();

            
            for (int i = 1; i < m; i++) {
                fingers[i] = new Finger();
                fingers[i].node  =  currentNode;
                fingers[i].start =  (int) (currentNodeId + Math.pow(2, (i-1)) % maxId);
                fingers[i].intervalEnd = (int) (currentNodeId + Math.pow(2, (i)) % maxId)-1;
            }
            currentNode.setRoutingTable(fingers);   
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
    public LookUpResponse lookUp(int keyIndex, String startNode){
        /*
        implement this logic
         */

        System.out.println(network.getTopology());

        return null;
    }



}
