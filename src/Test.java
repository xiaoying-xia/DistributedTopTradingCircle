import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {

    private static Server[] initServer(int n, List<List<Integer>> prefs, List<Integer> houses) {
        String host = "127.0.0.1";
        String[] peers = new String[n];
        int[] ports = new int[n];
        Server[] servers = new Server[n];
        for(int i = 0 ; i < n; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < n; i++){
            servers[i] = new Server(peers, ports, i, prefs.get(i), houses.get(i));
        }
        return servers;
    }

    public static void main(String[] args) throws RemoteException {
        int n = 3;
        List<List<Integer>> prefs = new ArrayList<>();
        prefs.add(Arrays.asList(2, 3, 1));
        prefs.add(Arrays.asList(3, 1, 2));
        prefs.add(Arrays.asList(3, 1, 2));

//        prefs.add(Arrays.asList(1, 2, 0));
//        prefs.add(Arrays.asList(2, 0, 1));
//        prefs.add(Arrays.asList(0, 1, 2));
        List<Integer> ownedHouses = new ArrayList<>(Arrays.asList(1, 2, 3));
        Server[] servers = initServer(n, prefs, ownedHouses);

        servers[1].Start();

//        for (Server server : servers) {
//            System.out.println("Server: " + server.me);
//            System.out.println(server.pref.get(0) + " " + server.pref.get(1) + " " + server.pref.get(2));
//            System.out.println(server.prefMap.get(1) + " " + server.prefMap.get(2) + " " + server.prefMap.get(3));
//        }
//        System.out.println("=====================");

//        for (Server server : servers) {
//            System.out.println("----------------");
//            System.out.println("Server: " + server.me);
//            for (int i = 0; i < server.pref.size(); i++) {
//                System.out.print(server.pref.get(i) + " ");
//            }
//            System.out.println();
//            System.out.println(server.circleStates.active);
//        }
//        System.out.println("=====================");

//        for (Server server : servers) {
//            System.out.println("Server: " + server.me + ", inCircle: " + server.circleStates.inCircle);
//        }
        System.out.println("====================================");

        for (Server server : servers) {
            System.out.println("Server: " + server.me + ", assigned: " + server.assigned + ", house: " + server.house);
        }



    }

}
