
# Assignment 1

## Data flow

1. ServerSimulator parses dataset -> starts proxy -> starts servers
2. ClientSimulator parses instructions -> starts client
3. client iterates through instructions
4. client asks proxy for server
5. proxy checks workload, returns appropriate server
6. client uses given server and queries its requrst
7. server add requst to FIFO list
8. servers executor polls and performs request, returns answer
9. client receives response, logs it

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
    - `java -cp .target/solution.jar ass1.server.ServerSimulator`

* run the **ClientSimulator**, in terminal type:
    - `java -cp ./target/solution.jar ass1.client.ClientSimulator`
