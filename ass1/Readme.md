# Assignment 1

## How to run

Everything is done in the folder "ass1", so open terminal here.

1) Build maven workspace, in terminal type:
-> 'mvn clean package'

### From here, more terminals may be needed

* run **rmiregistry** in ./target/classes
    - `cd ./target/classes`
    - `rmiregistry`

* run the **server**, in terminal type:
    - `java -cp .target/solution.jar ass1.server.Server`

* run the **client**, in terminal type:
    - `java -cp ./target/solution.jar ass1.client.Client`