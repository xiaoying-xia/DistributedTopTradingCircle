import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote {
    void handleBroadcast(Message msg) throws RemoteException;
    void handleFlip(Message msg) throws  RemoteException;
    void handleExplore(Message msg) throws RemoteException;
    Message handleSuccReq(Message msg) throws RemoteException;
    void handleCircle(Message msg) throws RemoteException;
    void handleCountCircle(Message msg) throws RemoteException;
    void handleOneStage(Message msg) throws RemoteException;

    void handleOK(Message msg) throws RemoteException;
    void handleRemove(Message msg) throws RemoteException;
    void handleNextStage(Message msg) throws RemoteException;
}
