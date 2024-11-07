
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

# Contribution

As with previous assignments, the overall project consisted of contribution from every member in multiple areas, specifically fixing of incorrect code, refactoring, and additional code as well as comments.

As for our main contributions, we thought it fitting to assign each member one function each. This was fitting as there was mainly 3 functions that needed the most implementation. The contribution is as follows:

* `buildOverlayNetwork` : vegarhje
* `buildFingerTable` : sellebahn
* `lookUp` : borgebj
* Readme : borgebj
* Implementation description : sellebahn
* Printing to file : vegarhje



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
