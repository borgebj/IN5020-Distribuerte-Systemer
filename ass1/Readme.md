# International Statistics Service using java-RMI
## Assignment 1

## How to run #1
either:

1. run `ServerSimulator` to start server and proxy
2. run `ClientSimulator` to start client

   or

1. run `Starter` to run both on a single instance / terminal


## How to run #2

Everything is done in the folder "ass1", so open terminal here.

* Build maven workspace, in terminal type
    - `mvn clean package`

### From here, 2 terminals needed

* run the **ServerSimulator**, in terminal type:
    - `java -cp .target/solution.jar ass1.server.ServerSimulator <true/false>`

* run the **ClientSimulator**, in terminal type:
    - `java -cp ./target/solution.jar ass1.client.ClientSimulator <true/false>`
 
or, run **Starter**, a dedicated class to run both 
    - `java -cp ./target/solution.jar ass1.Starter <none/client/server> <line delay in ms>`
    - Where:
    > 0 = no cache
    > 1 = Client uses cache
    > 2 = Server uses cache

## Data flow

1. ServerSimulator parses dataset -> starts proxy -> starts servers
2. ClientSimulator parses instructions -> starts client
3. client iterates through instructions
4. client asks proxy for server
5. proxy checks workload, returns appropriate server
6. client uses given server and queries its request, waits
7. server add requst to FIFO list
8. servers executor polls and performs request, returns answer
9. client receives response, logs it

## How it works
* The proxy has a table with the workload of each server it knows. Each time a server is assigned, it will check a workload limit of 18 requests before sending a request to the specific server for its workload, which will then be updated.
* This is to say, every 18 request a server has, the proxy sends RMI request for its workload which is updated. This causes the proxy to make more accurate workload balancing.
* The server creates a 'Reqest'-class when receving a request, which contains a lock. The lock is locked when asking for a result immediately, and unlocked after the executor thread has performed its calculations.
* Every request in the client is logged to a text file, where averages are calculated at the end of all instructions.
