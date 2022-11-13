import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class LasVegasCircleStates {

    boolean active; // whether I'm still active
    boolean inCircle; // whether I'm in a circle
    int parent; // If I'm in a circle, who's my parent server
    List<Integer> children; // If I'm in a circle, who's my children servers

    public LasVegasCircleStates() {
        this.active = true;
        this.inCircle = false;
        this.parent = -1;
        this.children = new ArrayList<>();
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
    List<Integer> prefMap; // mapping from my preferred houses to their owner server indexes
    boolean assigned; // whether I've been assigned with a house
    int house; // my current house
    int succ; // the next active server
    int next; // the top server in my current pref

    public Server(String[] peers, int[] ports, int me, List<Integer> pref, int house) {
        this.peers = peers;
        this.ports = ports;
        this.me = me;
        this.pref = pref;
        this.prefMap = new ArrayList<>();
        prefMap.addAll(pref);

        this.house = house;
        Registry registry;
        ServerRMI stub;

        this.circleStates = new LasVegasCircleStates();
        this.broadcasted = false;

        this.mutex = new ReentrantLock();
        this.assigned = false;
        this.succ = -1;
        this.next = -1;


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
                return stub.handleFlip(msg);
            else if (rmi.equals("handleCircle"))
                stub.handleCircle(msg);
            else if (rmi.equals("handleOK"))
                stub.handleOK(msg);
            else if (rmi.equals("handleRemove"))
                stub.handleRemove(msg);
            else if (rmi.equals("handleNextStage"))
                stub.handleNextStage(msg);
            else
                System.out.println("Wrong parameters!");
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void Start() {
        try {
            this.broadcastCurrentHouse();

            this.findCircle();


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Broadcast my house information to everyone including myself
    public void broadcastCurrentHouse() throws RemoteException {
        Message msg = new Message(MessageTypes.BroadCast, this.me, this.house);
        for (int i = 0; i < peers.length; i++) {
            if (i == this.me) { // if broadcast to myself, set broadcasted as true first
                this.broadcasted = true;
                this.handleBroadcast(msg);
            } else {
                Send("handleBroadcast", msg, i);
            }
        }
    }

    public void findCircle() {
        while (this.circleStates.active) {
            // Flip step
            int myCoin = new Random().nextInt(2); // Generate 0 or 1, 0: head, 1: tail
            int succCoin = Send("handleFlip", null, this.me).value;
            if (myCoin == 0 && succCoin == 1) {
                this.circleStates.active = false;
            }

            // Explore step
            if (this.circleStates.active) {
                boolean succActive = false; // succ.circleStatus.active
                while (!succActive) {
                    this.circleStates.children.add(succ);
//                    this.succ = this.succ.succ;
//                    succActive = succ.circleStatus.active
                }
                if (this.succ == this.me) {
                    this.circleStates.active = false;
//                    this.circleStates.inCircle = true;
                }
            }
        }

        // Notifying Step
        if (this.succ == this.me) {
            Message msg = new Message(MessageTypes.Cycle, -1, -1);
            for (int child : this.circleStates.children) {
                Send("handleCircle", msg, child);
            }
        }


    }

    // On receiving broadcast message:
    // 1: update my prefMap
    // 2: broadcast my house information if I've not broadcasted
    @Override
    public void handleBroadcast(Message msg) throws RemoteException {
        for (int i = 0; i < pref.size(); i++) {
            if (pref.get(i) == msg.value) {
                prefMap.set(i, msg.index);
            }
        }
        if (!this.broadcasted) {
            this.broadcastCurrentHouse();
            this.broadcasted = true;
        }
    }

    @Override
    public Message handleFlip(Message msg) throws RemoteException {
        return null;
    }

    @Override
    public void handleCircle(Message msg) throws RemoteException {
        this.circleStates.inCircle = true;
        for (int child : this.circleStates.children) {
            Send("handleCircle", msg, child);
        }
    }

    @Override
    public void handleOK(Message msg) throws RemoteException {

    }

    @Override
    public void handleRemove(Message msg) throws RemoteException {

    }

    @Override
    public void handleNextStage(Message msg) throws RemoteException {

    }
}
