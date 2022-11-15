# Distributed Top Trading Circle Algorithm

## Introduction
An implementation of the distributed version of Top Trading Circle Algorithm, which solves the house market problem.  
Original paper: [Parallel and Distributed Algorithms for the housing allocation Problem](https://arxiv.org/abs/1905.03111)

## Dependencies
- JDK 11
- JUnit 4.13.1

## Usage
- Compile the source code
- Run test cases in `AlgorithmTest.java`
- Use your own input:
  - Initilize the servers by calling `initServer()`
  - Call `Start()` from any of the servers
  - Print `server.house` to check the result
  
## House Allocation Problem
We have $n$ agents, every agent holds a house initially, and each of them have a preference list of the houses on the market. Our goal is to find a [core-stable](https://en.wikipedia.org/wiki/Core_(game_theory)) allocation (a re-allocation of houses to agents, such that all mutually-beneficial exchanges have been realized).  

- Input: $n$ servers, their initially owned houses and preference list
- Output: a core-stable re-allocation  

## Example
Say we have 3 servers:
- server 0 initially owns house 1, his preference list is [2, 3, 1]
- server 1 initially owns house 2, his preference list is [3, 1, 2]
- server 2 initially owns house 3, his preference list is [1, 2, 3]

We can easily get the result by giving house 2 to server 0, house 3 to server 1, house 1 to server 2

This case is verified by test case 2 in `AlgorithmTest.java`


## Implementation Details
To make things easier to describe, we assume there're only one circle in the functional graph. But out implementation works well for graphs with multiple circles.

There're several synchronization steps in the algorithm, which means every server needs to finish one step before moving forward to the next step. For example, in the Las Vegas Algorithm for finding circles in the functional graph, one server has to wait for its successor server to finish flipping a coin and reply to it, before it can move on to the exploring step. This is like a 'chain-effect', server 0 waits for server 1, server 1 waits for server 2... The consequence is every server needs to wait for all servers to flip a coin before moving on.  

We can solve this problem in a totally distributed way, that is, having every server broadcast a 'Done Flipping Coin' message to every server except itself, and wait for $n - 1$ such messages from everyone else before moving on to the next step. This will take $O(n^2)$ message complexity. Since it is assumed no server could be faulty, we can simplify the message complexity by having one server control the pace and notify everyone when they can enter the next step.

It is natural to have the server from which we invoke the algorithm take the duty, say this server is $s_0$. `Start()` is the method which does this job. First, $s_0$ will send a broadcast request to everyone. On receiving this, the server starts broadcasting its initial house information. After everyone finishes, $s_0$ notify everyone to enter the Top Trading Circle Algorithm. In this stage, it’ll send ‘’Flip a Coin’ request to every active server, and each of them will flip a coin. After everyone has flipped a coin, $s_0$ will notify everyone to move to the explore step, which is implemented by `handleExplore`. In this step, every server will request three types of information from its successor server: 
1. Active status
2. Coin value
3. Successor id

The server that makes the request will then use this information to search afterward until meet a successor that is active, and put everyone that is inactive into its children set. This step will end when someone’s next active successor is itself. Now it can make sure it is in the circle and the root node of the tree built from all servers in the same circle.

Then, the root node will broadcast `In Circle` message to all its children, and ‘Not in Circle’ message to everyone else (it is slightly different in implementation). Everyone that receive these messages should set their `inCircle` variable and notify $s_0$ that it has been decided. When $s_0$ receives $n$ such notifications, it’ll make everyone into the Top Trading Circle part. And everyone in the circle will be assigned with the house that is its first available top choice. $s_0$ will also memorize how many servers have been assigned a house, and will make this process continue until everyone has been assigned. The rest part remains almost the same as the original paper.



