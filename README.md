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
There're several synchronization steps in the algorithm, which means every server needs to finish one step before moving forward to the next step. For example, in the Las Vegas Algorithm for finding circles in the functional graph, one server has to wait for its successor server to finish flipping a coin and reply to it, before it can move on to the exploring step. This is like a 'chain-effect', server 0 waits for server 1, server 1 waits for server 2... The consequence is every server needs to wait for all servers to flip a coin before moving on.  

We can solve this problem in a totally distributed way, that is, having every server broadcast a 'Done Flipping Coin' message to every server except itself, and wait for $n - 1$ such messages from everyone else before moving on to the next step. This will take $O(n^2)$ message complexity. Since it is assumed no server could be faulty, we can simplify the message complexity by having one process control the pace and notify everyone when they can enter the next step.

It is natural to have the server from which we invoke the algorithm take the duty. The `Start()` method does this job. First, it'll start broadcasting current house allocation information to everyone, on receiving this message, servers will store the house information, and 

