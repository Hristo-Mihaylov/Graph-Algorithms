//cd "Documents\Uni\Second Year\Algorithms and Data Structures\COMP26120_a06532hm\java"
package comp26120;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;

import comp26120.graph_t.succs_t;
import comp26120.weight_t.weight_zero;

public class sp_algorithms {

	public static final void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	}

	// method for initial estimate - used in every algorithm
	public static void init(ArrayList<node_t> pred, ArrayList<weight_t> dist, int N) {
		for (int i = 0; i < N; ++i) {
			pred.add(node_t.INVALID_NODE);
			dist.add(new weight_t.weight_inf());
		}
	}

	public static sssp_result_t bfs(graph_t g, node_t src, node_t dst) {

		long stat_edges_explored = 0;

		int N = g.graph_get_num_nodes();

		ArrayList<node_t> pred = new ArrayList<node_t>();
		ArrayList<weight_t> dist = new ArrayList<weight_t>();

		// Assertions!
		assert src != node_t.INVALID_NODE : "The source node is invalid";
		assert src.hashCode() < N : "Invalid starting node";

		// use our initial estimate method
		init(pred, dist, N);

		// initialise the queue of Discovered nodes and the set of Finished nodes
		ArrayList<node_t> D = new ArrayList<node_t>();
		HashSet<node_t> F = new HashSet<node_t>();
		D.add(src);
		dist.set(src.hashCode(), new weight_t.weight_zero());
		pred.set(src.hashCode(), src);

		boolean not_found = true;

		while (!D.isEmpty() && not_found) {
			node_t u = D.get(0);
			D.remove(0); // instead of using a queue structure, we always just pop the first element
			F.add(u);
			for (edge_tgt_t t : g.get_graph_succs(u.hashCode())) {
				stat_edges_explored++;

				if (t.v.equals(dst)) {
					// change the bool
					not_found = false;
				}

				// check if this node is already finished or discovered
				if (F.contains(t.v) || D.contains(t.v)) {
					continue;
				}

				D.add(t.v); // add to the set of discovered nodes
				pred.set(t.v.hashCode(), u); // update the predecessor map
				// increment the distance
				dist.set(t.v.hashCode(), weight_t.weight_add(dist.get(u.hashCode()), new weight_t(1)));

			}

		}

		return new sssp_result_t(N, src, dst, false, pred, dist, stat_edges_explored);
	}

	public static sssp_result_t bellman_ford(graph_t g, node_t src) {

		boolean neg_cycle = false;
		long stat_edges_explored = 0;

		int N = g.graph_get_num_nodes();

		ArrayList<node_t> pred = new ArrayList<node_t>();
		ArrayList<weight_t> dist = new ArrayList<weight_t>();

		// Assertions!
		assert src != node_t.INVALID_NODE : "The source node is invalid";
		assert src.hashCode() < N : "Invalid starting node";

		// use our initial estimate method
		init(pred, dist, N);

		dist.set(src.hashCode(), new weight_t.weight_zero());
		pred.set(src.hashCode(), src);

		// this for loop is used to check for negative cycles, since we want another N-1
		// "rounds"
		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < N - 1; i++) { // we want N-1 rounds of relaxing edges

				int count = 0;

				for (int j = 0; j < N; j++) {
					// cycle through all edges outgoing from node with integer j
					for (edge_tgt_t t : g.get_graph_succs(j)) {
						if (t.w.weight_is_inf()) { // if weight is infinity, then continue to next edge
							continue;
						}
						// temporary variables
						weight_t temp = weight_t.weight_add(dist.get(j), t.w);
						weight_t big = dist.get(t.v.hashCode());

						// here we perform relaxation
						if (weight_t.weight_less(temp, big)) {
							count = 1;
							if (k == 0) {
								// we update dist and pred
								dist.set(t.v.hashCode(), temp);
								node_t nod = new node_t(j);
								pred.set(t.v.hashCode(), nod);
							} else {
								// here we do not update pred
								dist.set(t.v.hashCode(), new weight_t.weight_neg_inf());
								neg_cycle = true;
							}
						}
						if (k == 0) {
							stat_edges_explored++; // to avoid counting when we have negative cycles
						}
					}
				}

				if (count == 0) { // if a round made no changes then we are ready
					break;
				}
			}
		}

		return new sssp_result_t(N, src, node_t.INVALID_NODE, neg_cycle, pred, dist, stat_edges_explored);
	}

	public static sssp_result_t dijkstra(graph_t g, node_t src, node_t dst) {

		long stat_edges_explored = 0;
		int N = g.graph_get_num_nodes();

		ArrayList<node_t> pred = new ArrayList<node_t>();
		ArrayList<weight_t> dist = new ArrayList<weight_t>();

		// Assertions!
		assert src != node_t.INVALID_NODE : "The source node is invalid";
		assert src.hashCode() < N : "Invalid starting node";

		// use our initial estimate method
		init(pred, dist, N);

		dist.set(src.hashCode(), new weight_t.weight_zero());
		pred.set(src.hashCode(), src);

		// initialise a priority queue
		pq qu = new pq(N);
		qu.DPQ_insert(src, dist.get(src.hashCode()));

		while (!qu.DPQ_is_empty()) {

			node_t u = qu.DPQ_pop_min(); // pop from the queue

			if (u.equals(dst)) { // check if we found our destination
				break;
			}

			for (edge_tgt_t t : g.get_graph_succs(u.hashCode())) {

				weight_t temp = weight_t.weight_add(dist.get(u.hashCode()), t.w);
				weight_t big = dist.get(t.v.hashCode());
				stat_edges_explored++;

				// relaxation
				if (weight_t.weight_less(temp, big)) {

					dist.set(t.v.hashCode(), temp);
					pred.set(t.v.hashCode(), u);

					// if the queue does not contain the element, we add it
					if (!qu.DPQ_contains(t.v)) {
						qu.DPQ_insert(t.v, dist.get(t.v.hashCode()));
					} else {
						// else if it contains it, we decrease the key
						qu.DPQ_decrease_key(t.v, dist.get(t.v.hashCode()));
					}
				}
			}
		}

		return new sssp_result_t(N, src, dst, false, pred, dist, stat_edges_explored);
	}

	public static sp_result_t astar_search(graph_t g, node_t src, node_t dst, weight_t[] h) {

		long stat_edges_explored = 0;
		int N = g.graph_get_num_nodes();

		ArrayList<node_t> pred = new ArrayList<node_t>();
		ArrayList<weight_t> dist = new ArrayList<weight_t>();

		// Assertions!
		assert src != node_t.INVALID_NODE : "The source node is invalid";
		assert src.hashCode() < N : "Invalid starting node";

		// use our initial estimate method
		init(pred, dist, N);

		dist.set(src.hashCode(), new weight_t.weight_zero());
		pred.set(src.hashCode(), src);

		// initialise a priority queue
		pq qu = new pq(N);
		qu.DPQ_insert(src, dist.get(src.hashCode()));

		while (!qu.DPQ_is_empty()) {

			node_t u = qu.DPQ_pop_min();

			if (u.equals(dst)) {
				break;
			}

			for (edge_tgt_t t : g.get_graph_succs(u.hashCode())) {

				weight_t temp = weight_t.weight_add(dist.get(u.hashCode()), t.w);
				weight_t big = dist.get(t.v.hashCode());
				stat_edges_explored++;

				if (weight_t.weight_less(temp, big)) {

					dist.set(t.v.hashCode(), temp);
					pred.set(t.v.hashCode(), u);

					// we also need to add the heuristics
					if (!qu.DPQ_contains(t.v)) {
						qu.DPQ_insert(t.v, weight_t.weight_add(dist.get(t.v.hashCode()), h[t.v.hashCode()]));
					} else {
						qu.DPQ_decrease_key(t.v, weight_t.weight_add(dist.get(t.v.hashCode()), h[t.v.hashCode()]));
					}
				}
			}
		}

		sssp_result_t sssp = new sssp_result_t(N, src, dst, false, pred, dist, stat_edges_explored);
		// convert our sssp_result_t object to sp_result_t
		sp_result_t sp = sssp.sssp_to_sp_result(dst);

		return sp;
	}

}
