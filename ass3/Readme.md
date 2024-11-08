
# Assignment 3

## How to run 

__1. Build maven__
- run `mvn clean packag` in terminal

__2. Run the protocol__
- run `java -cp target/solution.jar ass3.Simulator <n> <m>` in terminal
- Where n = node count, and m = bit-length of identifiers used in the protocol.

### Examples
- Following 3 examples show the 3 required running configurations for assignment 4.

Example:
- `java -cp target/solution.jar ass3.Simulator 10 10`
- `java -cp target/solution.jar ass3.Simulator 100 20`
- `java -cp target/solution.jar ass3.Simulator 1000 20`

## Design Implementations

### Building the Overlay Network
---
This solution establishes a distributed hash ring topology using consistent hashing, enabling efficient data distribution and routing within a distributed system.

### Step 1: Node Hashing and ID Assignment

The solution iterates through each node in the `topology` map, performing the following steps:

1. **Node Name Retrieval and Hash Calculation**:  
   For each node, retrieve its name and generate a unique hash using a hash function. This hash serves as the node’s ID, determining its position on the ring.

2. **Setting Node ID**:  
   The generated hash value is assigned as the node’s ID.

3. **Storing Node ID for Successor Linking**:  
   The hash ID is added to a temporary list, `sortedIndexes`, which will be used to link each node to its successor.

### Step 2: Sorting Nodes in the Ring Order

After assigning a unique hash ID to each node, the `sortedIndexes` list is sorted by hash values. Sorting arranges the nodes in the correct order, ensuring proper routing in the hash ring.

### Step 3: Linking Nodes to Form the Ring

To complete the hash ring structure, each node is linked to its successor:

1. **Iterating through Sorted Node IDs**:  
   We iterate over the sorted list of node IDs and link each node to the next. The last node is linked back to the first node to form a closed loop.

2. **Adding Successor as Neighbor**:  
   Each node adds its successor as a neighbor using the `addNeighbor()` function. This step ensures that each node can communicate with its next node in the ring.

This ring structure enables efficient routing and data lookups based on hash values.


### Finger Table Construction
---

The `buildFingerTable` function constructs a finger table for each node in the network, optimizing request routing across the hash ring.

### Overview

For each node in the `topology`:

1. **Initialize the Finger Table**:  
   - Create an array of `Finger` objects, with length `m`, for each node. Each `Finger` represents an interval of hash values on the ring.

2. **Calculate Intervals**:
   - For each entry \(i\) in the finger table:
     - Set the interval’s **start** as:
       $`\
       \text{finger.start} = (\text{nodeIdx} + 2^{i-1}) \mod 2^m
       `$
     - Set the **end** of the interval as:
       $`\ 
       \text{finger.end} = (\text{nodeIdx} + 2^i) \mod 2^m - 1
       `$
     - For the last entry, wrap around so `finger.end` is set just before the start of the first finger.

3. **Determine Successors**:
   - The successor for each interval is the first node with an ID within the range \([finger.start, finger.end]\). If no node fits, the first node in the ring is chosen.

4. **Set the Routing Table**:
   - After populating the finger table, assign the `fingers` array to the node’s routing table, allowing efficient lookups across the hash ring.



### Lookup Function
---
The `lookUp` function efficiently searches for data across the hash ring, using each node's finger table to minimize hops. Given a `keyIndex` and starting node `startNode`, it locates the node holding the data and returns the visited nodes and data location if found.

### Steps in the Lookup Process

1. **Initialize Starting Node**:  
   - Begin the lookup at `startNode`, tracking visited nodes in `peersLookedUp` to monitor the search path.

2. **Check for Data on Each Node**:
   - For each node:
     - **Data Check**: If the node holds `keyIndex` in its data, return the visited nodes and the current node name.
     - **Finger Table Navigation**:  
       If the data is not found, use the node's finger table to locate the interval that contains `keyIndex`. The successor for this interval becomes the next node.
     - **Fallback to First Finger**:  
       If no matching interval is found, default to the first finger’s successor.

3. **Loop Detection**:
   - If the search returns to `startNode`, it means the entire ring has been traversed without finding the data. The function concludes with the final node reached.




## Project Contributions

The project implementation was a collaborative effort, with each member contributing to multiple areas, including bug fixes, code refactoring, and documentation. Key contributions were as follows:

### Main Function Implementations
- **`buildOverlayNetwork`**: *vegarhje* – Designed and implemented network overlay setup in chord.
- **`buildFingerTable`**: *sellebaf* – Developed the finger table structure for the node routing.
- **`lookUp`**: *borgebj* – Implemented the lookup logic to search for specific keys in the overlay network.

### Documentation and Supporting Tasks
- **README Documentation**: *borgebj* – Wrote the README with instructions, setup, and usage details.
- **Implementation Description**: *sellebaf* – Wrote an overview of the required functionality for chord.
- **Output to File**: *vegarhje* – Implemented the logic to log results to an output file.




## Data flow / How it works

1. The _simulator_ class starts the entire process
2. The Network is created given nodecount n, which creates a list holding nodes indexes from 1 to n+1. This holds both node label and the node-object.
3. The chord protocol is started, building the structure of the protocol
4. the build consists of assigning the keys, as well as building the overlay and fingertable.
5.  Key generation and assigning is done by hashing keynames, and assigns the key to peer responsible for it. Peer responsible is the closest next node after the key index.
6.  The overlay is built by retrieveing nodes from the network and assigning indexes based on consistent hashing for each. Then successor nodes are assigned, and a local list is made.
7.  The fingertable is built for each node by creating m intervals in the table. These intervals are used for efficient routing to a few other nodes.
8.  Iteration through every key, and call on `lookup` which goes through intervals in fingertable to find node responsible.
9.  Results are written to a file indicated with nodecount and bit-length, as well as averages of each hop. 
