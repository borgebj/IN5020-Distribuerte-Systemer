
# Assignment 1

## Data flow

...

## How to run (current)

Run 'ServerSimulator' for everything to run at once
1. compile files in both /client and /server
2. run `ServerSimulator` to start all threads
3. run `ClientSimulator` to start all clients

## How to run (planned)

Everything is done in the folder "ass1", so open terminal here.

* Build maven workspace, in terminal type
- `mvn clean package`

### From here, more terminals may be needed

* run **rmiregistry** in ./target/classes
    - `cd ./target/classes`
    - `rmiregistry`

* run the **server**, in terminal type:
    - `java -cp .target/solution.jar ass1.server.Server`

* run the **client**, in terminal type:
    - `java -cp ./target/solution.jar ass1.client.Client`

