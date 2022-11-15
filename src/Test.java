//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import org.junit.Test;
//
//public class Test {
//
//    private static Server[] initServer(int n, List<List<Integer>> prefs, List<Integer> houses) {
//        String host = "127.0.0.1";
//        String[] peers = new String[n];
//        int[] ports = new int[n];
//        Server[] servers = new Server[n];
//        for(int i = 0 ; i < n; i++){
//            ports[i] = 1100+i;
//            peers[i] = host;
//        }
//        for(int i = 0; i < n; i++){
//            servers[i] = new Server(peers, ports, i, prefs.get(i), houses.get(i));
//        }
//        return servers;
//    }
//
//    private void cleanup(Server[] servers){
//        for(int i = 0; i < servers.length; i++){
//            if(servers[i] != null){
//                servers[i].Kill();
//            }
//        }
//    }
//
////    public static void main(String[] args) {
////        int n = 3;
////        List<List<Integer>> prefs = new ArrayList<>();
//////        prefs.add(Arrays.asList(2, 3, 1));
//////        prefs.add(Arrays.asList(3, 1, 2));
//////        prefs.add(Arrays.asList(3, 1, 2));
////
//////        prefs.add(Arrays.asList(1, 3, 4, 2));
//////        prefs.add(Arrays.asList(1, 3, 4, 2));
//////        prefs.add(Arrays.asList(2, 4, 3, 1));
//////        prefs.add(Arrays.asList(2, 3, 4, 1));
////
////        prefs.add(Arrays.asList(1, 2, 3));
////        prefs.add(Arrays.asList(3, 2, 1));
////        prefs.add(Arrays.asList(2, 1, 3));
////
////
////        List<Integer> ownedHouses = new ArrayList<>(Arrays.asList(1, 2, 3));
////        Server[] servers = initServer(n, prefs, ownedHouses);
////
////        servers[0].Start();
////
////        System.out.println("====================================");
////        System.out.println("Final Assignment:");
////
////        for (Server server : servers) {
////            System.out.println("Server: " + server.me + ", assigned: " + server.assigned + ", house: " + server.house);
////        }
////    }
//
//    @Test
//    public void testSingleCircle() {
//
//    }
//
//
//}
