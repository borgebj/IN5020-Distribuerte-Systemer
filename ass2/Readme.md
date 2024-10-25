
# Replicated Bank Account using Spread toolkit

# Assignment 2

## How to run 

__1. Compile spread__
- `cd spread-src-4.0.0`
- `./configure`
- `make`
- `sudo make install`

__2. Compile maven__
- `mvn clean package`

Now go back to directory folder

__3. Run spread__
- `cd ..`
- `spread -l y -n group8 -c spread.conf`

Create multiple client-instances through the 'Starter' class

This must be done on multiple instances / terminals

__4. Run client__
- `java -cp target/solution.jar:spread.jar ass2.Starter <id> [filename]` 

where `<id>` uniquely represents the client, and `[filename]` represents an optional file with queries

'Starter' is the file that starts Client with given parameters

### Examples

Example 1: three clients (without file):
- `Starter 1`
- `Starter 2`
- `Starter 3`

Example 2:  three clients (with replica files):
- `Starter 1 Rep1.txt`
- `Starter 2 Rep2.txt`
- `Starter 3 Rep2.txt`

## Data flow / How it works

1. The spread server starts ( 3 above )
2. Client is initiated through Starter ( 4 above )
   - Client is initiated for either file-reading or user-input
        - For file-reading:  input is read through file every $`s \in [0.5, 1.5]`$ seconds
   - Client connects to the spread server, as well as create a listener for itself
   - Group is joined and client awaits all replicas
3. Once all replicas are connected, the client initiates a scheduler broadcaster, that repeats a function multicasting outstandingCollection every 10 seconds

- Each client can now perform commands, whilst constantly updating each 10 seconds with actions from other replicas.
