import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class LasVegasCircleStates {

    boolean active; // whether I'm still active
    int inCircle; // -1: undecided, 0: Not in circle, 1: in circle
    int parent; // If I'm in a circle, who's my parent server
    List<Integer> children; // If I'm in a circle, who's my children servers
    int nDecided;
    int nPerRound;
    int coin; // -1: undecided, 1: head, 0: tail
    boolean succActive;
    int succCoin;
    int countCircle;

    public LasVegasCircleStates() {
        this.active = true;
        this.inCircle = -1;
        this.parent = -1;
        this.children = new ArrayList<>();
        this.nDecided = 0;
        this.nPerRound = 0;
        this.coin = -1;
        this.succCoin = -1;
        this.succActive = true;
        this.countCircle = 0;
    }
}

public class Server implements ServerRMI {

    // Peer information
    ReentrantLock mutex; // lock
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    // For the circle finding algo
    LasVegasCircleStates circleStates;
    private boolean broadcasted; // number of broadcast msgs received

    // For the Top Trading Circle algo
    List<Integer> pref; // My house preference list
    Map<Integer, Integer> prefMap; // key: house, value: owner server
    boolean assigned; // whether I've been assigned with a house
    int house; // my current house
    int succ; // the next active server
    int next; // the top server in my current pref
    int countAssigned; // total number of servers assigned
    boolean hasNextStage; // whether to continue TTC


    public Server(String[] peers, int[] ports, int me, List<Integer> pref, int house) {
        this.peers = peers;
        this.ports = ports;
        this.me = me;
        this.pref = new ArrayList<>();
        this.pref.addAll(pref);
        this.prefMap = new HashMap<>();

        this.house = house;
        Registry registry;
        ServerRMI stub;

        this.circleStates = new LasVegasCircleStates();
        this.broadcasted = false;

        this.mutex = new ReentrantLock();
        this.assigned = false;
        this.succ = -1;
        this.next = -1;
        this.countAssigned = 0;
        this.hasNextStage = true;


        // register peers in the system
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]); // Set system-wide hostname
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (ServerRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Server", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // Send messages to peers
    public Message Send(String rmi, Message msg, int id){
        ServerRMI stub;
        try{
            Registry registry = LocateRegistry.getRegistry(this.ports[id]);
            stub = (ServerRMI) registry.lookup("Server"); // stub: the server with id
            if (rmi.equals("handleBroadcast"))
                stub.handleBroadcast(msg);
            else if (rmi.equals("handleFlip"))
                stub.handleFlip(msg);
            else if (rmi.equals("handleExplore"))
                stub.handleExplore(msg);
            else if (rmi.equals("handleSuccReq"))
                return stub.handleSuccReq(msg);
            else if (rmi.equals("handleCircle"))
                stub.handleCircle(msg);
            else if (rmi.equals("handleCountCircle"))
                stub.handleCountCircle(msg);
            else if (rmi.equals("handleOneStage"))
                stub.handleOneStage(msg);
            else if (rmi.equals("handleCountAssigned"))
                stub.handleCountAssigned(msg);
            else if (rmi.equals("handleOK"))
                stub.handleOK(msg);
            else if (rmi.equals("handleRemove"))
                stub.handleRemove(msg);
            else if (rmi.equals("handleNextStage"))
                stub.handleNextStage(msg);
            else if (rmi.equals("handleReset"))
                stub.handleReset(msg);
            else
                System.out.println("Wrong parameters: " + rmi);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void Start() {
        try {
            // Broadcast my house information to everyone
            this.broadcastCurrentHouse();

            while(hasNextStage && countAssigned < peers.length) {
                System.out.println("====================Starting New Stage====================");
                hasNextStage = false;
                // Reset variables for unassigned servers
                for (int i = 0; i < peers.length; i++) {
                    if (me == i) {
                        handleReset(null);
                    } else Send("handleReset", null, i);
                }

                circleStates.countCircle = countAssigned;

                // Keep doing this until everyone knows whether he's in circle
                while (circleStates.countCircle < peers.length) {
                    // Flip a coin for myself, send "flip-coin" request to everyone
                    for (int i = 0; i < peers.length; i++) {
                        if (me == i) {
                            handleFlip(null);
                        } else Send("handleFlip", null, i);
                    }
                    // Now, every active node has flipped a coin
                    // Get the succ information, update my active status
                    // Explore my succ and send "explore" request to everyone
                    for (int i = 0; i < peers.length; i++) {
                        if (me == i) {
                            handleExplore(new Message(MessageTypes.BroadCast, me, -1, false));
                        } else Send("handleExplore", new Message(MessageTypes.BroadCast, me, -1, false), i);
                    }
                }
                System.out.println("=================================");
                System.out.println("This round of circle found");
                System.out.println("servers decided: " + circleStates.countCircle);
                System.out.println("Server: " + me + ", inCircle: " + circleStates.inCircle + ", active: " + circleStates.active);
                System.out.println("next: " + next + ", succ: " + succ);
                System.out.println("My coin: " + circleStates.coin + ", succCoin: " + Send("handleSuccReq", null, succ).value);
                // Now, everyone unassigned knows whether he's in the circle
                // We can move on to the next step: exchange houses for servers in circle
                for (int i = 0; i < peers.length; i++) {
                    if (me == i) {
                        handleOneStage(new Message(MessageTypes.Cycle, me, -1, false));
                    } else Send("handleOneStage", new Message(MessageTypes.Cycle, me, -1, false), i);
                }

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Broadcast my house information to everyone including myself
    public void broadcastCurrentHouse() throws RemoteException {
        Message msgHouse = new Message(MessageTypes.BroadCast, this.me, this.house, false);
        for (int i = 0; i < peers.length; i++) {
            if (i == this.me) { // if broadcast to myself, set broadcasted as true first
                this.broadcasted = true;
                this.handleBroadcast(msgHouse);
            } else {
                Send("handleBroadcast", msgHouse, i);
            }
        }
    }

    // On receiving broadcast message:
    // 1: update my prefMap
    // 2: broadcast my house information if I've not broadcasted
    @Override
    public void handleBroadcast(Message msg) throws RemoteException {
        for (int i = 0; i < pref.size(); i++) {
            if (pref.get(i) == msg.value) { // Find the index of the house
                prefMap.put(msg.value, msg.index);
            }
        }
        if (!this.broadcasted) {
            this.broadcastCurrentHouse();
            this.broadcasted = true;
        }
    }

    // flip a coin for myself
    // Update next and succ
    @Override
    public void handleFlip(Message msg) throws RemoteException {
        // Flip the coin
        if (!assigned && circleStates.active) {
            circleStates.coin = (int) Math.round(Math.random());
            String s = circleStates.coin == 1 ? "head" : "tail";
            System.out.println("Server: " + me + ", Flipping coin... " + s);
        }

    }

    // 1. Request information from succ
    // 2. Explore through succ until meet a succ who is active
    @Override
    public void handleExplore(Message msg) throws RemoteException {
        if (!assigned && circleStates.active) {
            // Request information from succ
            Message succInfo = Send("handleSuccReq", null, succ);
            boolean succActive = succInfo.flag;
            int succCoin = succInfo.value;
            int succSucc = succInfo.index;
            // Put myself as inactive if I'm head and succ is tail
            if (circleStates.coin == 1 && succCoin == 0) circleStates.active = false;

            // Explore through succ until meet a succ who is active
            if (circleStates.active) {
                while (!succActive) {
                    circleStates.children.add(succ);
                    succ = succSucc;
                    succActive = Send("handleSuccReq", null, succ).flag;
                }
                if (succ == me) {
                    circleStates.active = false;
                    circleStates.inCircle = 1;
                    Send("handleCountCircle", null, msg.index);
                }
            }
            // Right place ?????
            if (succ == me) {
                Set<Integer> set = new HashSet<>();
                set.add(me);
                // Send "Circle" message to all children
                for (int child : circleStates.children) {
                    Send("handleCircle", new Message(MessageTypes.Cycle, msg.index, -1, true), child);
                    set.add(child);
                }
                // Send "Not in Circle" message to everyone else (excluding myself)
                for (int i = 0; i < peers.length; i++) {
                    if (!set.contains(i) && i != me) {
                        Send("handleCircle", new Message(MessageTypes.Cycle, msg.index, -1, false), i);
                    }
                }
            }
        }


    }

    @Override
    public Message handleSuccReq(Message msg) throws RemoteException {
        return new Message(MessageTypes.Cycle, succ, circleStates.coin, circleStates.active);
    }

    // Notify all of my children when receiving cycle message
    @Override
    public void handleCircle(Message msg) throws RemoteException {
        if (!assigned) {
            if (msg.flag) {
                circleStates.inCircle = 1;
                for (int child : this.circleStates.children) {
                    Send("handleCircle", msg, child);
                }
            } else {
                circleStates.inCircle = 0;
            }
            Send("handleCountCircle", null, msg.index);
        }
    }

    @Override
    public void handleCountCircle(Message msg) throws RemoteException {
        circleStates.countCircle++;
    }

    @Override
    public void handleOneStage(Message msg) throws RemoteException {
        if (circleStates.inCircle == 1 && !assigned) {
            // Get the house of next
            int houseNext = -1;
            for (Integer h : pref) {
                if (prefMap.get(h) == next) {
                    houseNext = h;
                    break;
                }
            }

            house = houseNext;
            assigned = true;
            System.out.println("Assigning Server: " + me + ", with house: " + house);
            Send("handleCountAssigned", null, msg.index); // send back to caller

            // Broadcast remove(house) to all
            for (int i = 0; i < peers.length; i++) {
                if (me == i) {
                    handleRemove(new Message(MessageTypes.Remove, me, house, false)); // matters: house
                } else Send("handleRemove", new Message(MessageTypes.Remove, me, house, false), i);
            }

            // Send OK to parent if no children
            if (circleStates.children.isEmpty() && circleStates.parent != -1) {
                Send("handleOK", msg, circleStates.parent);
            } else {
                Send("handleNextStage", null, msg.index);
            }

        }
    }

    @Override
    public void handleCountAssigned(Message msg) throws RemoteException {
        countAssigned++;
    }

    @Override
    public void handleOK(Message msg) throws RemoteException {
        // Broadcast OK message upward
        if (circleStates.parent == -1) { // if I'm the root
            Send("handleNextStage", null, msg.index);
        } else { // if I'm not the root
            Send("handleOK", msg, circleStates.parent);
        }
    }

    @Override
    public void handleRemove(Message msg) throws RemoteException {
        int toRemove = msg.value;
        // Remove house from my pref
        for (int i = 0; i < pref.size(); i++) {
            if (pref.get(i) == toRemove) {
                pref.remove(i);
                break;
            }
        }
    }

    @Override
    public void handleNextStage(Message msg) throws RemoteException {
        hasNextStage = true;
    }

    @Override
    public void handleReset(Message msg) throws RemoteException {
        if (!assigned) {
            // Reset active
            circleStates.active = true;
            // Update next
            next = prefMap.get(pref.get(0));
            // Update succ
            succ = next;
        }
    }
}
