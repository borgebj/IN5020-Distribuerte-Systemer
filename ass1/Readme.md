
# Assignment 1

## Data flow

...

## How to run #1

Run 'ServerSimulator' for everything to run at once
1. compile files in both /client and /server
2. run `ServerSimulator` to start all threads
3. run `ClientSimulator` to start all clients

## How to run #2

Everything is done in the folder "ass1", so open terminal here.

* Build maven workspace, in terminal type
    - `mvn clean package`

### From here, 2 terminals needed

* run the **ServerSimulator**, in terminal type:
    - `java -cp .target/solution.jar ass1.server.ServerSimulator`

* run the **ClientSimulator**, in terminal type:
    - `java -cp ./target/solution.jar ass1.client.ClientSimulator`
