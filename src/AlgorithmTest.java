import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlgorithmTest {

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

    @Test
    public void test1() {
        System.out.println("Test Case 1: Single Circle");
        int n = 3;
        List<List<Integer>> prefs = new ArrayList<>();
        prefs.add(Arrays.asList(2, 3, 1));
        prefs.add(Arrays.asList(3, 1, 2));
        prefs.add(Arrays.asList(3, 1, 2));
        List<Integer> ownedHouses = new ArrayList<>(Arrays.asList(1, 2, 3));
        int[] res = {2, 1, 3};

        Server[] servers = initServer(n, prefs, ownedHouses);
        servers[0].Start();

        for (int i = 0; i < n; i++) {
            assertTrue("Server: " + servers[i] + " is not assigned", servers[i].assigned);
            assertEquals("Wrong assignment for server: " + i + ", should be: " + res[i] + ", but algo gives: " + servers[i].house, res[i], servers[i].house);
        }
        System.out.println("====================================");
        System.out.println("Result:");
        for (Server server : servers) {
            System.out.println("Server: " + server.me + ", assigned: " + server.assigned + ", house: " + server.house);
        }
        System.out.println();
        System.out.println("...Passed");

    }

    @Test
    public void test2() {
        System.out.println("Test Case 2: Single Circle");
        int n = 3;
        List<List<Integer>> prefs = new ArrayList<>();
        prefs.add(Arrays.asList(2, 3, 1));
        prefs.add(Arrays.asList(3, 1, 2));
        prefs.add(Arrays.asList(1, 2, 3));
        List<Integer> ownedHouses = new ArrayList<>(Arrays.asList(1, 2, 3));
        int[] res = {2, 3, 1};

        Server[] servers = initServer(n, prefs, ownedHouses);
        servers[0].Start();

        for (int i = 0; i < n; i++) {
            assertTrue("Server: " + servers[i] + " is not assigned", servers[i].assigned);
            assertEquals("Wrong assignment for server: " + i + ", should be: " + res[i] + ", but algo gives: " + servers[i].house, res[i], servers[i].house);
        }
        System.out.println("====================================");
        System.out.println("Result:");
        for (Server server : servers) {
            System.out.println("Server: " + server.me + ", assigned: " + server.assigned + ", house: " + server.house);
        }
        System.out.println();
        System.out.println("...Passed");

    }

    @Test
    public void test3() {
        System.out.println("Test Case 3: Single Circle");
        int n = 4;
        List<List<Integer>> prefs = new ArrayList<>();
        prefs.add(Arrays.asList(1, 3, 4, 2));
        prefs.add(Arrays.asList(1, 3, 4, 2));
        prefs.add(Arrays.asList(2, 4, 3, 1));
        prefs.add(Arrays.asList(2, 3, 4, 1));
        List<Integer> ownedHouses = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        int[] res = {1, 3, 2, 4};

        Server[] servers = initServer(n, prefs, ownedHouses);
        servers[0].Start();

        for (int i = 0; i < n; i++) {
            assertTrue("Server: " + servers[i] + " is not assigned", servers[i].assigned);
            assertEquals("Wrong assignment for server: " + i + ", should be: " + res[i] + ", but algo gives: " + servers[i].house, res[i], servers[i].house);
        }
        System.out.println("====================================");
        System.out.println("Result:");
        for (Server server : servers) {
            System.out.println("Server: " + server.me + ", assigned: " + server.assigned + ", house: " + server.house);
        }
        System.out.println();
        System.out.println("...Passed");
    }

    @Test
    public void test4() {
        System.out.println("Test Case 4: Multiple Circle");
        int n = 3;
        List<List<Integer>> prefs = new ArrayList<>();
        prefs.add(Arrays.asList(1, 2, 3));
        prefs.add(Arrays.asList(3, 2, 1));
        prefs.add(Arrays.asList(2, 1, 3));
        List<Integer> ownedHouses = new ArrayList<>(Arrays.asList(1, 2, 3));
        int[] res = {1, 3, 2};

        Server[] servers = initServer(n, prefs, ownedHouses);
        servers[0].Start();

        for (int i = 0; i < n; i++) {
            assertTrue("Server: " + servers[i] + " is not assigned", servers[i].assigned);
            assertEquals("Wrong assignment for server: " + i + ", should be: " + res[i] + ", but algo gives: " + servers[i].house, res[i], servers[i].house);
        }
        System.out.println("====================================");
        System.out.println("Result:");
        for (Server server : servers) {
            System.out.println("Server: " + server.me + ", assigned: " + server.assigned + ", house: " + server.house);
        }
        System.out.println();
        System.out.println("...Passed");
    }
}
