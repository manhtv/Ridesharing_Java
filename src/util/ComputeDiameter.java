package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import util.Graph.Vertex;


/**
 * Graph. Could be directed or undirected depending on the TYPE enum.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
class Graph {

    private List<Vertex> verticies = new ArrayList<Vertex>();
    private List<Edge> edges = new ArrayList<Edge>();
    
    public enum TYPE {DIRECTED, UNDIRECTED};
    private TYPE type = TYPE.UNDIRECTED;
    
    public Graph() { }
    
    public Graph(Graph g) {
        //Deep copies
        
        //Copy the vertices (which copies the edges)
        for (Vertex v : g.getVerticies()) {
            this.verticies.add(new Vertex(v));
        }

        //Update the object references
        for (Vertex v : this.verticies) {
            for (Edge e : v.getEdges()) {
                Vertex fromVertex = e.getFromVertex();
                Vertex toVertex = e.getToVertex();
                int indexOfFrom = this.verticies.indexOf(fromVertex);
                e.from = this.verticies.get(indexOfFrom);
                int indexOfTo = this.verticies.indexOf(toVertex);
                e.to = this.verticies.get(indexOfTo);
                this.edges.add(e);
            }
        }

        type = g.getType();
    }
    
    public Graph(TYPE type) {
        this();
        this.type = type;
    }
    
    public Graph(List<Vertex> verticies, List<Edge> edges) {
        this(TYPE.UNDIRECTED,verticies,edges);
    }
    
    public Graph(TYPE type, List<Vertex> verticies, List<Edge> edges) {
        this(type);
        this.verticies.addAll(verticies);
        this.edges.addAll(edges);
        
        for (Edge e : edges) {
            Vertex from = e.from;
            Vertex to = e.to;
            
            if(!this.verticies.contains(from) || !this.verticies.contains(to)) continue;
            
            int index = this.verticies.indexOf(from);
            Vertex fromVertex = this.verticies.get(index);
            index = this.verticies.indexOf(to);
            Vertex toVertex = this.verticies.get(index);
            fromVertex.addEdge(e);
            if (this.type == TYPE.UNDIRECTED) {
                Edge reciprical = new Edge(e.cost, toVertex, fromVertex);
                toVertex.addEdge(reciprical);
                this.edges.add(reciprical);
            }
        }
    }
    
    public TYPE getType() {
        return type;
    }
    
    public List<Vertex> getVerticies() {
        return verticies;
    }
    
    public List<Edge> getEdges() {
        return edges;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Vertex v : verticies) {
            builder.append(v.toString());
        }
        return builder.toString();
    }
    
    public static class Vertex implements Comparable<Vertex> {
        
        private int value = Integer.MIN_VALUE;
        private int weight = 0;
        private List<Edge> edges = new ArrayList<Edge>();

        public Vertex(int value) {
            this.value = value;
        }
        
        public Vertex(int value, int weight) {
            this(value);
            this.weight = weight;
        }
        
        public Vertex(Vertex vertex) {
            this(vertex.value,vertex.weight);
            this.edges = new ArrayList<Edge>();
            for (Edge e : vertex.edges) {
                this.edges.add(new Edge(e));
            }
        }
        
        public int getValue() {
            return value;
        }
        
        public int getWeight() {
            return weight;
        }        
        public void setWeight(int weight) {
            this.weight = weight;
        }

        public void addEdge(Edge e) {
            edges.add(e);
        }
        
        public List<Edge> getEdges() {
            return edges;
        }

        public boolean pathTo(Vertex v) {
            for (Edge e : edges) {
                if (e.to.equals(v)) return true;
            }
            return false;
        }
        
        @Override
        public int compareTo(Vertex v) {
            if (this.value<v.value) return -1;
            if (this.value>v.value) return 1;
            return 0;
        }

        @Override
        public boolean equals(Object v1) {
            if (!(v1 instanceof Vertex)) return false;
            
            Vertex v = (Vertex)v1;

            boolean values = this.value==v.value;
            if (!values) return false;

            boolean weight = this.weight==v.weight;
            if (!weight) return false;

            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("vertex:").append(" value=").append(value).append(" weight=").append(weight).append("\n");
            for (Edge e : edges) {
                builder.append("\t").append(e.toString());
            }
            return builder.toString();
        }
    }
    
    public static class Edge implements Comparable<Edge> {

        private Vertex from = null;
        private Vertex to = null;
        private int cost = 0;

        public Edge(int cost, Vertex from, Vertex to) {
            if (from==null || to==null) throw (new NullPointerException("Both 'to' and 'from' Verticies need to be non-NULL."));
            this.cost = cost;
            this.from = from;
            this.to = to;
        }

        public Edge(Edge e) {
            this(e.cost, e.from, e.to);
        }

        public int getCost() {
            return cost;
        }
        public void setCost(int cost) {
            this.cost = cost;;
        }

        public Vertex getFromVertex() {
            return from;
        }

        public Vertex getToVertex() {
            return to;
        }

        @Override
        public int compareTo(Edge e) {
            if (this.cost<e.cost) return -1;
            if (this.cost>e.cost) return 1;
            return 0;
        }

        @Override
        public boolean equals(Object e1) {
            if (!(e1 instanceof Edge)) return false;
            
            Edge e = (Edge)e1;
            
            boolean costs = this.cost==e.cost;
            if (!costs) return false;
            
            boolean froms = this.from.equals(e.from);
            if (!froms) return false;
            
            boolean tos = this.to.equals(e.to);
            if (!tos) return false;
            
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("edge:").append(" [").append(from.value).append("]")
                   .append(" -> ")
                   .append("[").append(to.value).append("]")
                   .append(" = ")
                   .append(cost)
                   .append("\n");
            return builder.toString();
        }
    }

    public static class CostVertexPair implements Comparable<CostVertexPair> {
        
        private int cost = Integer.MAX_VALUE;
        private Vertex vertex = null;
        
        public CostVertexPair(int cost, Vertex vertex) {
            if (vertex==null) throw (new NullPointerException("vertex cannot be NULL."));

            this.cost = cost;
            this.vertex = vertex;
        }

        public int getCost() {
            return cost;
        }
        public void setCost(int cost) {
            this.cost = cost;
        }

        public Vertex getVertex() {
            return vertex;
        }
        
        @Override
        public int compareTo(CostVertexPair p) {
            if (p==null) throw new NullPointerException("CostVertexPair 'p' must be non-NULL.");
            if (this.cost<p.cost) return -1;
            if (this.cost>p.cost) return 1;
            return 0;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Vertex=").append(vertex.getValue()).append(" cost=").append(cost).append("\n");
            return builder.toString();
        }
    }

    public static class CostPathPair {

        private int cost = 0;
        private Set<Edge> path = null;

        public CostPathPair(int cost, Set<Edge> path) {
            if (path==null) throw (new NullPointerException("path cannot be NULL."));

            this.cost = cost;
            this.path = path;
        }

        public int getCost() {
            return cost;
        }
        public void setCost(int cost) {
            this.cost = cost;
        }

        public Set<Edge> getPath() {
            return path;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Cost = ").append(cost).append("\n");
            for (Edge e : path) {
                builder.append("\t").append(e);
            }
            return builder.toString();
        }
    }
}

/**
 * Bellman-Ford's shortest path. Works on both negative and positive weighted edges. Also detects
 * negative weight cycles. Returns a tuple of total cost of shortest path and the path.
 * 
 * Worst case: O(|V| |E|)
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
class BellmanFord {

    private static Map<Graph.Vertex, Graph.CostVertexPair> costs = null;
    private static Map<Graph.Vertex, Set<Graph.Edge>> paths = null;
    private static boolean containsNegativeWeightCycle = false;

    private BellmanFord() { }

    public static Map<Graph.Vertex, Graph.CostPathPair> getShortestPaths(Graph g, Graph.Vertex start) {
        getShortestPath(g,start,null);
        Map<Graph.Vertex, Graph.CostPathPair> map = new HashMap<Graph.Vertex, Graph.CostPathPair>();
        for (Graph.CostVertexPair pair : costs.values()) {
            int cost = pair.getCost();
            Graph.Vertex vertex = pair.getVertex();
            Set<Graph.Edge> path = paths.get(vertex);
            map.put(vertex, new Graph.CostPathPair(cost,path));
        }
        return map;
    }
    
    public static Graph.CostPathPair getShortestPath(Graph g, Graph.Vertex start, Graph.Vertex end) {
        if (g==null) throw (new NullPointerException("Graph must be non-NULL."));

        containsNegativeWeightCycle = false;
        
        paths = new TreeMap<Graph.Vertex, Set<Graph.Edge>>();
        for (Graph.Vertex v : g.getVerticies()) {
            paths.put(v, new LinkedHashSet<Graph.Edge>());
        }

        costs = new TreeMap<Graph.Vertex, Graph.CostVertexPair>();
        for (Graph.Vertex v : g.getVerticies()) {
            if (v.equals(start)) costs.put(v,new Graph.CostVertexPair(0,v));
            else costs.put(v,new Graph.CostVertexPair(Integer.MAX_VALUE,v));
        }

        boolean negativeCycleCheck = false;
        for (int i=0; i<(g.getVerticies().size()); i++) {
            
            // If it's the last vertices perform a negative weight cycle check. The graph should be 
            // finished by the size()-1 time through this loop.
            if (i==(g.getVerticies().size()-1)) negativeCycleCheck = true;
            
            // Compute costs to all vertices
            for (Graph.Edge e : g.getEdges()) {
                Graph.CostVertexPair pair = costs.get(e.getToVertex());
                Graph.CostVertexPair lowestCostToThisVertex = costs.get(e.getFromVertex());
                
                // If the cost of the from vertex is MAX_VALUE then treat as INIFINITY.
                if (lowestCostToThisVertex.getCost()==Integer.MAX_VALUE) continue;
                
                int cost = lowestCostToThisVertex.getCost() + e.getCost();
                if (cost<pair.getCost()) {
                    if (negativeCycleCheck) {
                        // Uhh ohh... negative weight cycle
                        System.out.println("Graph contains a negative weight cycle.");
                        containsNegativeWeightCycle = true;
                        return null;
                    } else {
                        // Found a shorter path to a reachable vertex
                        pair.setCost(cost);
                        Set<Graph.Edge> set = paths.get(e.getToVertex());
                        set.clear();
                        set.addAll(paths.get(e.getFromVertex()));
                        set.add(e);
                    }
                }
            }
        }

        if (end!=null) {
            Graph.CostVertexPair pair = costs.get(end);
            Set<Graph.Edge> set = paths.get(end);
            return (new Graph.CostPathPair(pair.getCost(),set));
        }
        return null;
    }

    public static boolean containsNegativeWeightCycle() {
        return containsNegativeWeightCycle;
    }
}

/**
 * Dijkstra's shortest path. Only works on non-negative path weights. Returns a tuple of 
 * total cost of shortest path and the path.
 * 
 * Worst case: O(|E| + |V| log |V|)
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
class Dijkstra {

    private static Map<Graph.Vertex, Graph.CostVertexPair> costs = null;
    private static Map<Graph.Vertex, Set<Graph.Edge>> paths = null;
    private static Queue<Graph.CostVertexPair> unvisited = null;

    private Dijkstra() { }

    public static Map<Graph.Vertex, Graph.CostPathPair> getShortestPaths(Graph g, Graph.Vertex start) {
        getShortestPath(g,start,null);
        Map<Graph.Vertex, Graph.CostPathPair> map = new HashMap<Graph.Vertex, Graph.CostPathPair>();
        for (Graph.CostVertexPair pair : costs.values()) {
            int cost = pair.getCost();
            Graph.Vertex vertex = pair.getVertex();
            Set<Graph.Edge> path = paths.get(vertex);
            map.put(vertex, new Graph.CostPathPair(cost,path));
        }
        return map;
    }
    
    public static Graph.CostPathPair getShortestPath(Graph g, Graph.Vertex start, Graph.Vertex end) {
        if (g==null) throw (new NullPointerException("Graph must be non-NULL."));
        
        // Dijkstra's algorithm only works on positive cost graphs
        boolean hasNegativeEdge = checkForNegativeEdges(g.getVerticies());
        if (hasNegativeEdge) throw (new IllegalArgumentException("Negative cost Edges are not allowed.")); 

        paths = new TreeMap<Graph.Vertex, Set<Graph.Edge>>();
        for (Graph.Vertex v : g.getVerticies()) {
            paths.put(v, new LinkedHashSet<Graph.Edge>());
        }

        costs = new TreeMap<Graph.Vertex, Graph.CostVertexPair>();
        for (Graph.Vertex v : g.getVerticies()) {
            if (v.equals(start)) costs.put(v,new Graph.CostVertexPair(0,v));
            else costs.put(v,new Graph.CostVertexPair(Integer.MAX_VALUE,v));
        }
        
        unvisited = new PriorityQueue<Graph.CostVertexPair>();
        unvisited.addAll(costs.values()); // Shallow copy

        Graph.Vertex vertex = start;
        while (true) {
            // Compute costs from current vertex to all reachable vertices which haven't been visited
            for (Graph.Edge e : vertex.getEdges()) {
                Graph.CostVertexPair pair = costs.get(e.getToVertex());
                Graph.CostVertexPair lowestCostToThisVertex = costs.get(vertex);
                int cost = lowestCostToThisVertex.getCost() + e.getCost();
                if (pair.getCost()==Integer.MAX_VALUE) {
                    // Haven't seen this vertex yet
                    pair.setCost(cost);
                    Set<Graph.Edge> set = paths.get(e.getToVertex());
                    set.addAll(paths.get(e.getFromVertex()));
                    set.add(e);
                } else if (cost<pair.getCost()) {
                    // Found a shorter path to a reachable vertex
                    pair.setCost(cost);
                    Set<Graph.Edge> set = paths.get(e.getToVertex());
                    set.clear();
                    set.addAll(paths.get(e.getFromVertex()));
                    set.add(e);
                }
            }

            // Termination conditions
            if (end!=null && vertex.equals(end)) {
                // If we are looking for shortest path, we found it.
                break;
            }  else if (unvisited.size()>0) {
                // If there are other vertices to visit (which haven't been visited yet)
                Graph.CostVertexPair pair = unvisited.remove();
                vertex = pair.getVertex();
                if (pair.getCost() == Integer.MAX_VALUE) {
                    // If the only edge left to explore has MAX_VALUE then it cannot be reached from the starting vertex
                    break;
                }
            } else {
                // No more edges to explore, we are done.
                break;
            }
        }

        if (end!=null) {
            Graph.CostVertexPair pair = costs.get(end);
            Set<Graph.Edge> set = paths.get(end);
            return (new Graph.CostPathPair(pair.getCost(),set));
        }
        return null;
    }

    private static boolean checkForNegativeEdges(List<Graph.Vertex> vertitices) {
        for (Graph.Vertex v : vertitices) {
            for (Graph.Edge e : v.getEdges()) {
                if (e.getCost()<0) return true;
            }
        }
        return false;
    }
}


/**
 * Johnson's algorithm is a way to find the shortest paths between all pairs of vertices in a sparse directed 
 * graph. It allows some of the edge weights to be negative numbers, but no negative-weight cycles may exist.
 * 
 * Worst case: O(V^2 log V + VE)
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
class JohnsonShortestPath {
    
    private JohnsonShortestPath() { }
    
    public static Map<Graph.Vertex, Map<Graph.Vertex, Set<Graph.Edge>>> getAllPairsShortestPaths(Graph g) {
        Map<Graph.Vertex, Map<Graph.Vertex, Set<Graph.Edge>>> allShortestPaths = new HashMap<Graph.Vertex, Map<Graph.Vertex, Set<Graph.Edge>>>();

        // Add the connector Vertex to all edges.
        for (Graph.Vertex v : g.getVerticies()) {
            Graph graph = new Graph(g); // Clone the original graph
            Graph.Vertex connector = new Graph.Vertex(Integer.MAX_VALUE); //Make new Vertex that connects to all Vertices
            graph.getVerticies().add(connector);

            int indexOfV = graph.getVerticies().indexOf(v);
            Graph.Edge e = new Graph.Edge(0, connector, graph.getVerticies().get(indexOfV));
            connector.addEdge(e);
            graph.getEdges().add(e);
            
            Map<Graph.Vertex, Graph.CostPathPair> costs = BellmanFord.getShortestPaths(graph, connector);
            if (BellmanFord.containsNegativeWeightCycle()) {
                System.out.println("Graph contains a negative weight cycle. Cannot compute shortest path.");
                return null;
            }
            for (Graph.Vertex v2 : costs.keySet()) {
                int index = graph.getVerticies().indexOf(v2);
                Graph.Vertex vertexToAdjust = graph.getVerticies().get(index);
                Graph.CostPathPair pair = costs.get(v2);
                vertexToAdjust.setWeight(pair.getCost());
            }
            
            for (Graph.Edge e2 : graph.getEdges()) {
                int startCost = e2.getFromVertex().getWeight();
                int endCode = e2.getToVertex().getWeight();
                int adjCost = e2.getCost() + startCost - endCode;
                e2.setCost(adjCost);
            }
            
            int index = graph.getVerticies().indexOf(connector);
            graph.getVerticies().remove(index);
            index = graph.getEdges().indexOf(e);
            graph.getEdges().remove(index);
            
            Map<Graph.Vertex, Graph.CostPathPair> costPaths = Dijkstra.getShortestPaths(graph, v);
            Map<Graph.Vertex, Set<Graph.Edge>> paths = new HashMap<Graph.Vertex, Set<Graph.Edge>>();
            for (Graph.Vertex v2 : costPaths.keySet()) {
                Graph.CostPathPair  pair = costPaths.get(v2);
                paths.put(v2, pair.getPath());
            }
            allShortestPaths.put(v, paths);
        }
        return allShortestPaths;
    }
}

public class ComputeDiameter{
	public static void main(String[] args){
		compute("C:/Users/v-shuoma/Desktop/workspace/taxi/processed/Taxi_Shanghai/od_merge_300.txt");
	}
	
	
	public static void compute(String mergeable_relation){
		Graph g=buildGraph(mergeable_relation);
		Map<Graph.Vertex, Map<Graph.Vertex, Set<Graph.Edge>>> shortestPaths=JohnsonShortestPath.getAllPairsShortestPaths(g);
		int diameter=0;
		for(Map.Entry<Graph.Vertex, Map<Graph.Vertex, Set<Graph.Edge>>> entryO: shortestPaths.entrySet())
		{
			for(Map.Entry<Graph.Vertex, Set<Graph.Edge>> entryI: entryO.getValue().entrySet()){
				int size=entryI.getValue().size();
				diameter=diameter>size?diameter:size;
			}
		}
		System.out.println("Diameter is "+diameter);
	}
	
	
	private static Graph buildGraph(String s){
		HashSet<Graph.Vertex> v=new HashSet<Graph.Vertex>();
		ArrayList<Graph.Edge> e=new ArrayList<Graph.Edge>();
		try {
			Scanner sc=new Scanner(new File(s));
			while(sc.hasNextLine()){
				String line=sc.nextLine();
				String[] fields=line.substring(0, line.length()-1).split(",");
				v.add(new Graph.Vertex(Integer.parseInt(fields[0])));
				v.add(new Graph.Vertex(Integer.parseInt(fields[1])));
				e.add(new Graph.Edge(1, new Graph.Vertex(Integer.parseInt(fields[0])), new Graph.Vertex(Integer.parseInt(fields[1]))));
			}
			
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		return new Graph(new ArrayList<Graph.Vertex>(v),e);
	}
}


