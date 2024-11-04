package ass3.protocol;

import java.util.LinkedHashSet;

/**
 * This class prints the response of the lookup. This is class prints the names of the nodes whose finger table
 * has been checked, the destination node index, it's name and hop count.
 */
public class LookUpResponse {
    public LinkedHashSet<String> peers_looked_up;
    public int node_index;
    public String node_name;

    public LookUpResponse(LinkedHashSet<String> peers_looked_up, int node_index,String node_name){
        this.peers_looked_up = peers_looked_up;
        this.node_index = node_index;
        this.node_name = node_name;
    }

    public String toString(){
        String result = "";
        result = result.concat(node_name+":"+node_index);
        result = result.concat("\t hop count: "+peers_looked_up.size());
        result = result.concat("\t route: ");
        for(String peer: peers_looked_up){
            result = result.concat(peer+"\t");
        }
        return  result;
    }
}
