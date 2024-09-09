
# Assignment 1

## Data flow

1. Program start
2. Server and Client creation
- 5 server, 5 clients, 1 proxy
- 5 zones, 1 client and 1 server each
3. Clients parse instructions, RMI to proxy (?)
4. Proxy processes the requests, forwards requests to appropriate server
5. Server performs function, send result back
6. ...
7. Result ?

## How to run

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

