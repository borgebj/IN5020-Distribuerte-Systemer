
# Assignment 2

## How to run 

__1. Compile spread__
- `cd spread-src-4.0.0`
- `./configure`
- `make`
- `sudo make install`

Now go back to directory folder

__2. Run spread__
- `cd ..`
- `spread -l y -n group8 -c spread.conf`

Create multiple client-instances through the 'Starter' class

This must be done on multiple instances / terminals

__3. Run client__
- `java Starter <id> [filename]` 

where `<id>` uniquely represents the client, and `[filename]` represents an optional file with queries

### Examples

Example 1: three clients (without file):
- `Starter 1`
- `Starter 2`
- `Starter 3`

Example 2:  three clients (with file=example.txt):
- `Starter 1 example.txt`
- `Starter 2 example.txt`
- `Starter 3 example.txt`

## Data flow / Wow it works

1. The spread server starts (point 2 above)
2. Client is initiated through Starter (point 3 above)
   - Client is initiated for either file-reading or user-input
        - For file-reading:  input is read through file every $`s \in [0.5, 1.5] seconds`$
   - Client connects to the spread server, as well as create a listener for itself
   - Group is joined and client awaits all replicas
3. Once all replicas are connected, the client initiates a scheduler broadcaster, that repeats a function multicasting outstandingCollection every 10 seconds

- Each client can now perform commands, whilst constantly updating each 10 seconds with actions from other replicas.
