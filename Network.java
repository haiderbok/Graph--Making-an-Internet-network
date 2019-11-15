
import java.awt.*;
import java.util.*;

/**
 * A simple Network class to build a network
 *
 * @author Vaastav Arora, arora74@purdue.edu
 */
public class Network {


    /**
     * computerConnections represents list of all inter-computer edges
     * Each edge is an Integer[] of size 3
     * edge[0] = source computer index ( Not IP, it's the Index !)
     * edge[1] = destination computer index ( Not IP, it's the Index !)
     * edge[2] = latency/edge weight
     */
    private LinkedList<Integer[]> computerConnections;
    /**
     * Adjacency List representing computer graph
     */
    private LinkedList<LinkedList<Integer>> computerGraph;
    /**
     * LinkedList of clusters where each cluster is represented as a LinkedList of computer IP addresses
     */
    private LinkedList<LinkedList<Integer>> cluster;
    /**
     * Adjacency List representing router graph
     */
    private LinkedList<LinkedList<Integer[]>> routerGraph;

    Scanner s; // Scanner to read Stdin input

    //Add your own field variables as required
    int computerConnections_iterator = 0; // to iterate through the array in the linked list

    private Point[] ucheck = null; //array that has IP and index assigned - the x has the value and the y has the index assigned

    private int no_of_unique_indexes = 0;

    private PriorityQueue<Integer[]> pq = null; // Priority queue for Kurskals algorthim

    private UnionFind uf = null; // Union find to use in the Kurskals algorithm

    private LinkedList<Integer> DFS_cluster = new LinkedList<>();

    int[] clusterID = null; // Storing the cluster ID

    int connected = 0;

    Router[] router = null; // a data structure that assigns unique indexes to each router and stores the computers connected to that router

    int shortest_path_latency = 0;

    /**
     * Default Network constructor, initializes data structures
     *
     * @param s Provided Scanner to be used throughout program
     */
    public Network(Scanner s) {

        //TODO

        this.s = s;

        computerConnections = new LinkedList<>();

        computerGraph = new LinkedList<>();

        cluster = new LinkedList<>();

        routerGraph = new LinkedList<>();
    }

    /**
     * Method to parse Stdin input and generate inter-computer edges
     * Edges are stored within computerConnections
     * <p>
     * First line of input => Number of edges
     * All subsequent lines => [IP address of comp 1] [IP address of comp 2] [latency of connection]
     */
    public void buildComputerNetwork() {


        // The number of edges
        int m = s.nextInt();
        System.out.println(m);
        boolean flag = true;
        int transverse = 0;
        int index = 0;
        int index1 = 0;
        Integer[] arr = null;
        // an array to check if the u is unique and v also
        ucheck = new Point[2 * m];


        // Run a loop and read int the values from stdin
        for (int i = 0; i < m; i++) {
            flag = true;
            arr = new Integer[3];

            index1 = 0;

            int u = s.nextInt();
            int v = s.nextInt();
            int l = s.nextInt();


            // run a loop and check if the the index have been already been seen if not
            //give both of them unique index and save them in the array
            for (int j = 0; j < transverse; j++) {

                if (u == ucheck[j].x) {
                    flag = false;
                    index1 = ucheck[j].y;
                    break;
                }
            }

            if (flag) {
                ucheck[transverse] = new Point(u, index);
                index1 = index;
                no_of_unique_indexes = index;
                index++;
                transverse++;
            }

            flag = true;
            arr[0] = index1;
            //.....

            for (int j = 0; j < transverse; j++) {

                if (v == ucheck[j].x) {
                    flag = false;
                    index1 = ucheck[j].y;
                    break;
                }
            }

            if (flag) {
                ucheck[transverse] = new Point(v, index);
                index1 = index;
                no_of_unique_indexes = index;
                index++;
                transverse++;
            }
            arr[1] = index1;
            arr[2] = l;

            computerConnections.add(arr);

        }
    }

    private class MyException extends Exception{

        public MyException () {
            super("Cannot create clusters");
        }
    }
    /**
     * Method to generate clusters from computer graph
     * Throws Exception when cannot create required clusters
     *
     * @param k number of clusters to be created
     */
    public void buildCluster(int k) throws MyException {

        System.out.println("k " + k);
        if (k < 1 || k > ucheck.length  || computerConnections.isEmpty()) {
            throw new MyException();
        }




        pq = new PriorityQueue<Integer[]>(computerConnections.size(), new Comparator<Integer[]>() {
            public int compare(Integer integer1[], Integer integer2[]) {
                return integer1[2] - integer2[2];
            }

        });

        uf = new UnionFind(no_of_unique_indexes + 1);

        LinkedList<Integer>[] adj = new LinkedList[no_of_unique_indexes + 1];

        //Initializing the linked list
        for (int i = 0; i < adj.length; i++) {
            adj[i] = new LinkedList<>();
        }

        Stack<Point> stack = new Stack<>();

        // Populating the pq with the latencies in increasing orders
        for (int i = 0; i < computerConnections.size(); i++) {

            pq.add(computerConnections.get(i));

        }


        // -------- Kurskals algorithm --------
        // run until the priority is empty or we have our no of forests (k)

        while (!pq.isEmpty()) {

            Integer[] arr = pq.remove();
            int v = arr[0];
            int u = arr[1];

            if (uf.connected(v, u)) continue;
            uf.union(u, v);

            // add the elements in the stack to keep
            stack.push(new Point(u, v));
            adj[u].add(v);
            adj[v].add(u);

        }

        if (adj.length < k){
            throw new MyException();
        }

        // Getting k trees
        connected = 1;

        boolean[] marked = new boolean[no_of_unique_indexes + 1];


        while (connected != k) {


            Point point = stack.pop();


            for (int j = 0; j < adj[point.x].size(); j++) {
                if (point.y == adj[point.x].get(j)) {
                    adj[point.x].remove(j);
                }
            }

            for (int j = 0; j < adj[point.y].size(); j++) {
                if (point.x == adj[point.y].get(j)) {
                    adj[point.y].remove(j);
                }
            }

            connected++;
            /*
            for (int i = 0; i < marked.length - 1 ; i++) {
                if (!marked[i]) {
                    DFS_connectedComponents(adj,marked,i);

                    i = 0;
                }
            }
            */
        }

        for (int j = 0; j < adj.length; j++) {

            LinkedList<Integer> linkedList = new LinkedList<>();

            for (int l = 0; l < adj[j].size(); l++) {

                linkedList.add(adj[j].get(l));
            }

            computerGraph.add(linkedList);

        }


        // Populating the cluster

        for (int i = 0; i < marked.length; i++) {
            marked[i] = false;
        }

        //  clusterID = new int[marked.length];
        int count = 1;
        for (int i = 0; i < marked.length; i++) {
            if (!marked[i]) {
                DFS_connectedComponents(adj, marked, i);
                i = 0;
            }

            if (!DFS_cluster.isEmpty() && count <= connected) {
                //   clusterID[transverse++] = DFS_cluster.get(0);
                cluster.add(DFS_cluster);
                DFS_cluster = new LinkedList<>();
                count++;
            }

        }


        // Creating Routers
        int runner = 0;
        router = new Router[cluster.size()];
        while (runner < cluster.size()) {
            int max = cluster.get(runner).get(0);
            for (int i = 0; i < cluster.get(runner).size(); i++) {

                if (max < cluster.get(runner).get(i)) {
                    max = cluster.get(runner).get(i);
                }
            }
            router[runner] = new Router(max);

            LinkedList<Integer> list = new LinkedList<>();

            for (int i = 0; i < cluster.get(runner).size(); i++) {
                list.add(cluster.get(runner).get(i));
            }


            for (int i = 0; i < list.size(); i++) {
                router[runner].addComp(list.get(i));
            }

            runner++;
        }

/*
        // ********************De-bugging*************************


        //Printing after getting  the computerGraph
        System.out.println("computerGraph");
        for (int j = 0; j < computerGraph.size(); j++) {

            LinkedList<Integer> linkedList = new LinkedList<>();

            linkedList = computerGraph.get(j);

            System.out.print("Index" + j + ": {");
            for (int l = 0; l < linkedList.size(); l++) {
                Integer p = linkedList.get(l);
                System.out.print(p + ",");

            }
            System.out.print("}");
            System.out.println();
        }


        //  printing out the clusters and cluster IDs

        System.out.println("Clusters");
        for (int i = 0; i < cluster.size(); i++) {
            //   System.out.println(cluster.size());

            LinkedList<Integer> linkedList = cluster.get(i);
            System.out.print("Index" + i + ": {");
            for (int j = 0; j < linkedList.size(); j++) {

                System.out.print(linkedList.get(j) + " , ");
            }
            System.out.print("}");
            System.out.println();
            // System.out.println("clusterID " + clusterID[i]);
        }


        //Printing out the Routers to check if they are correct
        System.out.println("Routers");
        for (int i = 0; i < router.length; i++) {
            Router router1 = router[i];

            System.out.println("Index" + i + " IPPrefix: " + router1.getIPPrefix() + " {" + router1.getComputers() + "}");
        }





*/



/*
    printing out the stack after Kurskals algorithm
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            System.out.println(p.x + " , " + p.y);
        }

        */


        /*
        Printing out to check the behaviour of the queue
        while(!pq.isEmpty()){
            Integer [] arr = new Integer[pq.size()];
            arr = pq.remove();

            System.out.println(arr[2]);
        }
        */
    }


    void DFS_connectedComponents(LinkedList<Integer> adj[], boolean[] marked, int v) {


        marked[v] = true;
        DFS_cluster.add(ucheck[v].x);

        for (int i = 0; i < adj[v].size(); i++) {

            int j = adj[v].get(i);

            if (!marked[j])
                DFS_connectedComponents(adj, marked, j);
        }
/*
        connected++;

        for (int i = 0; i <marked.length ; i++) {
            if (marked[i] == false)
                DFS_connectedComponents(adj,marked,i);
        }
        return connected;
        */
    }


    /**
     * Method to parse Stdin input and generate inter-router edges
     * Graph is stored within routerGraph as an adjacency list
     * <p>
     * First line of input => Number of edges
     * All subsequent lines => [IP address of Router 1] [IP address of Router 2] [latency of connection]
     */
    public void connectCluster() {
        int m = 0;
        //Initializing list in routerGraph
        for (int j = 0; j < router.length; j++) {
            LinkedList<Integer[]> list = new LinkedList<>();
            routerGraph.add(j, list);
        }
        m = s.nextInt();

        for (int i = 0; i < m; i++) {

            int u = s.nextInt();
            int v = s.nextInt();
            int l = s.nextInt();


            int v_index = 0;
            int u_index = 0;


            // Populating routerGraph LinkedList<LinkedList<Integer[]>>

            for (int j = 0; j < router.length; j++) {
                if (v == router[j].getIPPrefix()) {
                    v_index = j;
                    break;
                }
            }

            for (int j = 0; j < router.length; j++) {
                if (u == router[j].getIPPrefix()) {
                    Integer[] arr = new Integer[2];
                    arr[0] = v_index;
                    arr[1] = l;
                    routerGraph.get(j).add(arr);
                    u_index = j;
                    break;
                }
            }

            Integer[] arr = new Integer[2];
            arr[0] = u_index;
            arr[1] = l;
            routerGraph.get(v_index).add(arr);
        }

/*
            //************De bugging Router-graph************
         // Printing out the router graph

        for (int i = 0; i < routerGraph.size() ; i++) {

            LinkedList<Integer[]> list = new LinkedList<>();
            System.out.println("Index" + i + ": ");
            for (int j = 0; j < routerGraph.get(i).size(); j++) {
            list.add(routerGraph.get(i).get(j));

            Integer [] arr = new Integer[2];
            arr = list.get(0);

                System.out.print("{" +  arr[0] + ", " + arr[1]+"} ");
            }

            System.out.println();
        }
*/
    }

    /**
     * Method to take a traversal request and find the shortest path for that traversal
     * Traversal request passed in through parameter test
     * Format of Request => [IP address of Source Router].[IP address of Source Computer] [IP address of Destination Router].[IP address of Destination Computer]
     * Eg. 123.456 128.192
     * 123 = IP address of Source Router
     * 456 = IP address of Source Computer
     * 128 = IP address of Destination Router
     * 192 = IP address of Destination Computer
     *
     * @param test String containing traversal input
     * @return Shortest traversal distance between Source and Destination Computer
     */
    public int traversNetwork(String test) {

        // get the data from the string
        String[] data = test.split(" ", 2);
        // Source data
        String[] sourceData_String = new String[2];
        sourceData_String[0] = data[0].substring(0, data[0].indexOf('.'));
        sourceData_String[1] = data[0].substring(data[0].indexOf('.') + 1);
        Integer[] sourceData_Integer = new Integer[2];
        sourceData_Integer[0] = Integer.valueOf(sourceData_String[0]);
        sourceData_Integer[1] = Integer.valueOf(sourceData_String[1]);

        // Target Data
        String[] targetData_String = new String[2];
        targetData_String[0] = data[1].substring(0, data[1].indexOf('.'));
        targetData_String[1] = data[1].substring(data[1].indexOf('.') + 1);
        Integer[] targetData_Integer = new Integer[2];
        targetData_Integer[0] = Integer.valueOf(targetData_String[0]);
        targetData_Integer[1] = Integer.valueOf(targetData_String[1]);

        //Validation: Checking if the computers exist and are in that router

        // ------------Source validation------------

        // Run through the router  and find the index of the IP address

        int source_index = 0;
        for (int i = 0; i < router.length; i++) {
            if (sourceData_Integer[0].equals(router[i].getIPPrefix())) {
                source_index = i;
                break;
            }
            /*if (i == router.length) {
                return -1;
            }*/

        }

        LinkedList<Integer> list = cluster.get(source_index);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(sourceData_Integer[1])) {
                break;
            }

            if (i == list.size() - 1) {
                return -1;
            }

        }

        // -------Target Validation----------


        // Run through the router  and find the index of the IP address

        int target_index = 0;
        for (int i = 0; i < router.length; i++) {
            if (targetData_Integer[0].equals( router[i].getIPPrefix())) {
                target_index = i;
                break;
            }
            /*if (i == router.length) {
                return -1;
            }*/

        }

        list = cluster.get(target_index);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(targetData_Integer[1])) {
                break;
            }

            if (i == list.size() - 1) {
                return -1;
            }

        }

        // Finding the shortest path between the given routers using

        //----------------Dijkstra's Algorithm--------------

        Integer[] prev = new Integer[routerGraph.size()];
        Integer[] distTo = new Integer[routerGraph.size()];
        PriorityQueue<Integer[]> pq = new PriorityQueue<>(new Comparator<Integer[]>() {
            @Override
            public int compare(Integer[] o1, Integer[] o2) {
                return o1[1] - o2[1];
            }
        });

        // Initialing the two Data structures: pq and disTo
        for (int i = 0; i < routerGraph.size(); i++) {

            distTo[i] = Integer.MAX_VALUE;

            prev[i] = -1;

        }

        // Insert the source index and the distance (weight) as 0
        Integer[] arr1 = new Integer[2];
        arr1[0] = source_index;
        arr1[1] = 0;


        // Set the distance of the source in source_index to 0
        distTo[source_index] = 0;

        //Push the source vertex into the queue
        pq.add(arr1);


        // Relaxation of the vertices
        while (!pq.isEmpty()) {


            // poll out the the least edge
            Integer[] temp = pq.poll();

            /*
            // check if the target index is reached stop save the distance at that index and break out
            if (target_index == temp[0]){

               // return the shortest distance
                return distTo[target_index];
                // distTo[temp[0]] = temp[1];
            }
            */

            // Store teh current vertex in the integer u
            Integer u = temp[0];

            // run a loop that checks adjacent vertices of this router LinkedList<LinkedList<Integer[]>>

            for (int i = 0; i < routerGraph.get(u).size(); i++) {
                Integer[] arr = routerGraph.get(u).get(i);
                int alt = distTo[u] + arr[1];

                if (alt < distTo[arr[0]]) {
                    distTo[arr[0]] = alt;
                    prev[arr[0]] = u;
                    // If queue already has the
                    if (pq.contains(arr)) {
                        pq.remove(arr);
                    }
                    // Create a new array because the last one is referencing the routerGraph
                    // changing the value here changes the value in the router graph
                    Integer[] add_in_queue = new Integer[2];
                    int v = arr[0];
                    add_in_queue[0] = v;
                    add_in_queue[1] = alt;
                    pq.add(add_in_queue);

                }

            }
        }


        shortest_path_latency = distTo[target_index];

        return shortest_path_latency;
    }

    //Add your own methods as required

//    private void printGraph(LinkedList< LinkedList<Integer[]>> graph){
//        for (var i:graph) {
//            for (var j: i){
//                System.out.print(j[0]+" "+j[1]);
//            }
//            System.out.println();
//        }
//    }

    public LinkedList<Integer[]> getComputerConnections() {
        return computerConnections;
    }

    public LinkedList<LinkedList<Integer>> getComputerGraph() {
        return computerGraph;
    }

    public LinkedList<LinkedList<Integer>> getCluster() {
        return cluster;
    }

    public LinkedList<LinkedList<Integer[]>> getRouterGraph() {
        return routerGraph;
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        Network n = new Network(s);

        n.buildComputerNetwork();
        Integer[] arr = new Integer[3];

        for (int i = 0; i < 50; i++) {
            arr = n.computerConnections.get(i);
            for (int j = 0; j < 3; j++) {
                System.out.print(arr[j] + " ");
            }
            System.out.println();
        }

        try {
            n.buildCluster(26);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        n.connectCluster();



        // Basic graph transverse Network calls
//        System.out.println(n.traversNetwork("50.50 50.500"));
//        System.out.println(n.traversNetwork("50.50 98.39"));
//        System.out.println(n.traversNetwork("50.50 83.42"));
//        System.out.println(n.traversNetwork("50.50 51.51"));
//        System.out.println(n.traversNetwork("50.50 84.16"));
//        System.out.println(n.traversNetwork("98.63 50.50"));
//        System.out.println(n.traversNetwork("98.98 98.63"));
//        System.out.println(n.traversNetwork("98.63 83.83"));
//        System.out.println(n.traversNetwork("98.98 51.51"));
//        System.out.println(n.traversNetwork("98.39 84.84"));
//        System.out.println(n.traversNetwork("83.62 50.50"));
//        System.out.println(n.traversNetwork("83.42 98.68"));
//        System.out.println(n.traversNetwork("83.42 83.83"));
//        System.out.println(n.traversNetwork("83.42 51.51"));
//        System.out.println(n.traversNetwork("83.83 84.77"));
//        System.out.println(n.traversNetwork("51.51 50.19"));
//        System.out.println(n.traversNetwork("51.51 98.98"));
//        System.out.println(n.traversNetwork("51.51 83.42"));
//        System.out.println(n.traversNetwork("51.51 51.51"));
//        System.out.println(n.traversNetwork("51.51 84.26"));
//        System.out.println(n.traversNetwork("84.18 50.19"));
//        System.out.println(n.traversNetwork("84.18 98.68"));
//        System.out.println(n.traversNetwork("84.77 83.62"));
//        System.out.println(n.traversNetwork("84.77 51.51"));
//        System.out.println(n.traversNetwork("84.18 84.840"));


        System.out.println(n.traversNetwork("35.35 35.35"));
        System.out.println(n.traversNetwork("35.35 197.197"));
        System.out.println(n.traversNetwork("35.35 102.102"));
        System.out.println(n.traversNetwork("35.35 136.136"));
        System.out.println(n.traversNetwork("35.35 174.174"));
        System.out.println(n.traversNetwork("35.35 78.78"));
        System.out.println(n.traversNetwork("35.35 145.145"));
        System.out.println(n.traversNetwork("35.35 210.131"));
        System.out.println(n.traversNetwork("35.35 50.50"));
        System.out.println(n.traversNetwork("35.35 179.89"));
        System.out.println(n.traversNetwork("35.35 149.149"));
        System.out.println(n.traversNetwork("35.35 214.214"));
        System.out.println(n.traversNetwork("35.35 215.144"));
        System.out.println(n.traversNetwork("35.35 217.208"));
        System.out.println(n.traversNetwork("35.35 126.126"));
        System.out.println(n.traversNetwork("197.197 35.35"));
        System.out.println(n.traversNetwork("197.197 197.197"));
        System.out.println(n.traversNetwork("197.197 102.102"));
        System.out.println(n.traversNetwork("197.197 136.136"));
        System.out.println(n.traversNetwork("197.197 174.174"));
        System.out.println(n.traversNetwork("197.197 78.78"));
        System.out.println(n.traversNetwork("197.197 145.145"));
        System.out.println(n.traversNetwork("197.197 210.36"));
        System.out.println(n.traversNetwork("197.197 50.50"));
        System.out.println(n.traversNetwork("197.197 179.179"));
        System.out.println(n.traversNetwork("197.197 149.61"));
        System.out.println(n.traversNetwork("197.197 214.214"));
        System.out.println(n.traversNetwork("197.197 215.86"));
        System.out.println(n.traversNetwork("197.197 217.208"));
        System.out.println(n.traversNetwork("197.197 126.126"));
        System.out.println(n.traversNetwork("102.102 35.35"));
        System.out.println(n.traversNetwork("102.102 197.197"));
        System.out.println(n.traversNetwork("102.102 102.102"));
        System.out.println(n.traversNetwork("102.102 136.136"));
        System.out.println(n.traversNetwork("102.102 174.174"));
        System.out.println(n.traversNetwork("102.102 78.78"));
        System.out.println(n.traversNetwork("102.102 145.145"));
        System.out.println(n.traversNetwork("102.102 210.121"));
        System.out.println(n.traversNetwork("102.102 50.50"));
        System.out.println(n.traversNetwork("102.102 179.89"));
        System.out.println(n.traversNetwork("102.102 149.149"));
        System.out.println(n.traversNetwork("102.102 214.214"));
        System.out.println(n.traversNetwork("102.102 215.134"));
        System.out.println(n.traversNetwork("102.102 217.54"));
        System.out.println(n.traversNetwork("102.102 126.126"));
        System.out.println(n.traversNetwork("136.136 35.35"));
        System.out.println(n.traversNetwork("136.136 197.197"));
        System.out.println(n.traversNetwork("136.136 102.102"));
        System.out.println(n.traversNetwork("136.136 136.136"));
        System.out.println(n.traversNetwork("136.136 174.174"));
        System.out.println(n.traversNetwork("136.136 78.78"));
        System.out.println(n.traversNetwork("136.136 145.145"));
        System.out.println(n.traversNetwork("136.136 210.81"));
        System.out.println(n.traversNetwork("136.136 50.50"));
        System.out.println(n.traversNetwork("136.136 179.49"));
        System.out.println(n.traversNetwork("136.136 149.149"));
        System.out.println(n.traversNetwork("136.136 214.214"));
        System.out.println(n.traversNetwork("136.136 215.94"));
        System.out.println(n.traversNetwork("136.136 217.54"));
        System.out.println(n.traversNetwork("136.136 126.126"));
        System.out.println(n.traversNetwork("174.174 35.35"));
        System.out.println(n.traversNetwork("174.174 197.197"));
        System.out.println(n.traversNetwork("174.174 102.102"));
        System.out.println(n.traversNetwork("174.174 136.136"));
        System.out.println(n.traversNetwork("174.174 174.174"));
        System.out.println(n.traversNetwork("174.174 78.78"));
        System.out.println(n.traversNetwork("174.174 145.145"));
        System.out.println(n.traversNetwork("174.174 210.115"));
        System.out.println(n.traversNetwork("174.174 50.50"));
        System.out.println(n.traversNetwork("174.174 179.179"));
        System.out.println(n.traversNetwork("174.174 149.61"));
        System.out.println(n.traversNetwork("174.174 214.214"));
        System.out.println(n.traversNetwork("174.174 215.175"));
        System.out.println(n.traversNetwork("174.174 217.54"));
        System.out.println(n.traversNetwork("174.174 126.126"));
        System.out.println(n.traversNetwork("78.78 35.35"));
        System.out.println(n.traversNetwork("78.78 197.197"));
        System.out.println(n.traversNetwork("78.78 102.102"));
        System.out.println(n.traversNetwork("78.78 136.136"));
        System.out.println(n.traversNetwork("78.78 174.174"));
        System.out.println(n.traversNetwork("78.78 78.78"));
        System.out.println(n.traversNetwork("78.78 145.145"));
        System.out.println(n.traversNetwork("78.78 210.150"));
        System.out.println(n.traversNetwork("78.78 50.50"));
        System.out.println(n.traversNetwork("78.78 179.179"));
        System.out.println(n.traversNetwork("78.78 149.149"));
        System.out.println(n.traversNetwork("78.78 214.214"));
        System.out.println(n.traversNetwork("78.78 215.134"));
        System.out.println(n.traversNetwork("78.78 217.54"));
        System.out.println(n.traversNetwork("78.78 126.126"));
        System.out.println(n.traversNetwork("145.145 35.35"));
        System.out.println(n.traversNetwork("145.145 197.197"));
        System.out.println(n.traversNetwork("145.145 102.102"));
        System.out.println(n.traversNetwork("145.145 136.136"));
        System.out.println(n.traversNetwork("145.145 174.174"));
        System.out.println(n.traversNetwork("145.145 78.78"));
        System.out.println(n.traversNetwork("145.145 145.145"));
        System.out.println(n.traversNetwork("145.145 210.105"));
        System.out.println(n.traversNetwork("145.145 50.50"));
        System.out.println(n.traversNetwork("145.145 179.179"));
        System.out.println(n.traversNetwork("145.145 149.61"));
        System.out.println(n.traversNetwork("145.145 214.214"));
        System.out.println(n.traversNetwork("145.145 215.215"));
        System.out.println(n.traversNetwork("145.145 217.217"));
        System.out.println(n.traversNetwork("145.145 126.126"));
        System.out.println(n.traversNetwork("210.207 35.35"));
        System.out.println(n.traversNetwork("210.121 197.197"));
        System.out.println(n.traversNetwork("210.112 102.102"));
        System.out.println(n.traversNetwork("210.201 136.136"));
        System.out.println(n.traversNetwork("210.181 174.174"));
        System.out.println(n.traversNetwork("210.167 78.78"));
        System.out.println(n.traversNetwork("210.148 145.145"));
        System.out.println(n.traversNetwork("210.154 210.72"));
        System.out.println(n.traversNetwork("210.101 50.50"));
        System.out.println(n.traversNetwork("210.76 179.179"));
        System.out.println(n.traversNetwork("210.26 149.149"));
        System.out.println(n.traversNetwork("210.192 214.214"));
        System.out.println(n.traversNetwork("210.153 215.168"));
        System.out.println(n.traversNetwork("210.196 217.118"));
        System.out.println(n.traversNetwork("210.44 126.126"));
        System.out.println(n.traversNetwork("50.50 35.35"));
        System.out.println(n.traversNetwork("50.50 197.197"));
        System.out.println(n.traversNetwork("50.50 102.102"));
        System.out.println(n.traversNetwork("50.50 136.136"));
        System.out.println(n.traversNetwork("50.50 174.174"));
        System.out.println(n.traversNetwork("50.50 78.78"));
        System.out.println(n.traversNetwork("50.50 145.145"));
        System.out.println(n.traversNetwork("50.50 210.143"));
        System.out.println(n.traversNetwork("50.50 50.50"));
        System.out.println(n.traversNetwork("50.50 179.49"));
        System.out.println(n.traversNetwork("50.50 149.149"));
        System.out.println(n.traversNetwork("50.50 214.214"));
        System.out.println(n.traversNetwork("50.50 215.175"));
        System.out.println(n.traversNetwork("50.50 217.125"));
        System.out.println(n.traversNetwork("50.50 126.126"));
        System.out.println(n.traversNetwork("179.179 35.35"));
        System.out.println(n.traversNetwork("179.49 197.197"));
        System.out.println(n.traversNetwork("179.89 102.102"));
        System.out.println(n.traversNetwork("179.49 136.136"));
        System.out.println(n.traversNetwork("179.49 174.174"));
        System.out.println(n.traversNetwork("179.179 78.78"));
        System.out.println(n.traversNetwork("179.49 145.145"));
        System.out.println(n.traversNetwork("179.49 210.70"));
        System.out.println(n.traversNetwork("179.179 50.50"));
        System.out.println(n.traversNetwork("179.179 179.49"));
        System.out.println(n.traversNetwork("179.49 149.61"));
        System.out.println(n.traversNetwork("179.179 214.214"));
        System.out.println(n.traversNetwork("179.49 215.144"));
        System.out.println(n.traversNetwork("179.89 217.208"));
        System.out.println(n.traversNetwork("179.49 126.126"));
        System.out.println(n.traversNetwork("149.61 35.35"));
        System.out.println(n.traversNetwork("149.61 197.197"));
        System.out.println(n.traversNetwork("149.149 102.102"));
        System.out.println(n.traversNetwork("149.61 136.136"));
        System.out.println(n.traversNetwork("149.61 174.174"));
        System.out.println(n.traversNetwork("149.61 78.78"));
        System.out.println(n.traversNetwork("149.149 145.145"));
        System.out.println(n.traversNetwork("149.149 210.177"));
        System.out.println(n.traversNetwork("149.61 50.50"));
        System.out.println(n.traversNetwork("149.61 179.49"));
        System.out.println(n.traversNetwork("149.61 149.149"));
        System.out.println(n.traversNetwork("149.61 214.214"));
        System.out.println(n.traversNetwork("149.149 215.215"));
        System.out.println(n.traversNetwork("149.61 217.54"));
        System.out.println(n.traversNetwork("149.149 126.126"));
        System.out.println(n.traversNetwork("214.214 35.35"));
        System.out.println(n.traversNetwork("214.214 197.197"));
        System.out.println(n.traversNetwork("214.214 102.102"));
        System.out.println(n.traversNetwork("214.214 136.136"));
        System.out.println(n.traversNetwork("214.214 174.174"));
        System.out.println(n.traversNetwork("214.214 78.78"));
        System.out.println(n.traversNetwork("214.214 145.145"));
        System.out.println(n.traversNetwork("214.214 210.31"));
        System.out.println(n.traversNetwork("214.214 50.50"));
        System.out.println(n.traversNetwork("214.214 179.179"));
        System.out.println(n.traversNetwork("214.214 149.149"));
        System.out.println(n.traversNetwork("214.214 214.214"));
        System.out.println(n.traversNetwork("214.214 215.94"));
        System.out.println(n.traversNetwork("214.214 217.54"));
        System.out.println(n.traversNetwork("214.214 126.126"));
        System.out.println(n.traversNetwork("215.168 35.35"));
        System.out.println(n.traversNetwork("215.134 197.197"));
        System.out.println(n.traversNetwork("215.215 102.102"));
        System.out.println(n.traversNetwork("215.168 136.136"));
        System.out.println(n.traversNetwork("215.215 174.174"));
        System.out.println(n.traversNetwork("215.94 78.78"));
        System.out.println(n.traversNetwork("215.144 145.145"));
        System.out.println(n.traversNetwork("215.215 210.114"));
        System.out.println(n.traversNetwork("215.175 50.50"));
        System.out.println(n.traversNetwork("215.144 179.89"));
        System.out.println(n.traversNetwork("215.134 149.61"));
        System.out.println(n.traversNetwork("215.134 214.214"));
        System.out.println(n.traversNetwork("215.215 215.175"));
        System.out.println(n.traversNetwork("215.86 217.118"));
        System.out.println(n.traversNetwork("215.86 126.126"));
        System.out.println(n.traversNetwork("217.217 35.35"));
        System.out.println(n.traversNetwork("217.21 197.197"));
        System.out.println(n.traversNetwork("217.217 102.102"));
        System.out.println(n.traversNetwork("217.21 136.136"));
        System.out.println(n.traversNetwork("217.217 174.174"));
        System.out.println(n.traversNetwork("217.118 78.78"));
        System.out.println(n.traversNetwork("217.217 145.145"));
        System.out.println(n.traversNetwork("217.21 210.42"));
        System.out.println(n.traversNetwork("217.208 50.50"));
        System.out.println(n.traversNetwork("217.208 179.89"));
        System.out.println(n.traversNetwork("217.125 149.149"));
        System.out.println(n.traversNetwork("217.125 214.214"));
        System.out.println(n.traversNetwork("217.21 215.175"));
        System.out.println(n.traversNetwork("217.21 217.217"));
        System.out.println(n.traversNetwork("217.217 126.126"));
        System.out.println(n.traversNetwork("126.126 35.35"));
        System.out.println(n.traversNetwork("126.126 197.197"));
        System.out.println(n.traversNetwork("126.126 102.102"));
        System.out.println(n.traversNetwork("126.126 136.136"));
        System.out.println(n.traversNetwork("126.126 174.174"));
        System.out.println(n.traversNetwork("126.126 78.78"));
        System.out.println(n.traversNetwork("126.126 145.145"));
        System.out.println(n.traversNetwork("126.126 210.165"));
        System.out.println(n.traversNetwork("126.126 50.50"));
        System.out.println(n.traversNetwork("126.126 179.89"));
        System.out.println(n.traversNetwork("126.126 149.149"));
        System.out.println(n.traversNetwork("126.126 214.214"));
        System.out.println(n.traversNetwork("126.126 215.134"));
        System.out.println(n.traversNetwork("126.126 217.217"));
        System.out.println(n.traversNetwork("126.126 126.126"));
    }


}
