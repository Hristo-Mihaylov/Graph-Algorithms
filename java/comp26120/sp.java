package comp26120;

import java.util.Random;
import java.util.ArrayList;

public class sp {
	public static Random rand = new Random();
	public general settings;

	public sp(general settings) {
		this.settings = settings;
	}

	public static int randraweight(int mn, int mx) throws Exception {
		assert (weight_t.WEIGHT_MIN <= mn && mn <= mx && mx <= weight_t.WEIGHT_MAX);
		int res = rand.nextInt((mx - mn) + 1) + mn;
		assert (mn <= res && res <= mx);
		return res;
	}

	public static weight_t randweight(double p_inf, int mn, int mx) throws Exception {
		if (rand.nextDouble() < p_inf) {
			return new weight_t.weight_inf();
		} else {
			return new weight_t(randraweight(mn, mx));
		}

	}

	public static String randname(int len) {
		String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String s = "";

		for (int i = 0; i < len; ++i) {
			s += alpha.charAt(rand.nextInt(alpha.length()));
		}

		s += "0";
		return s;

	}

	public graph_t rand_graph(int N, double p_edge, int min_weight, int max_weight) {
		graph_t g = new graph_t(N);

		for (int u = 0; u < N; ++u) {
			for (int v = 0; v < N; ++v) {
				if (u != v && rand.nextDouble() < p_edge) {
					weight_t w;
					if (max_weight > min_weight) {
						w = new weight_t(rand.nextInt(max_weight - min_weight) + min_weight);
					} else {
						w = new weight_t(min_weight);
					}
					g.graph_add_edge(new node_t(u), w, new node_t(v));
				}
			}
		}

		return g;
	}

	class point_t {
		double x;
		double y;

		public point_t(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	class map_graph_t {
		graph_t g;
		ArrayList<point_t> coords;

		public map_graph_t(graph_t g, ArrayList<point_t> coords) {
			this.g = g;
			this.coords = coords;
		}
	}

	public static weight_t coords_dist(ArrayList<point_t> coords, node_t u, node_t v) {
		double dx = coords.get(u.i).x - coords.get(v.i).x;
		double dy = coords.get(u.i).y - coords.get(v.i).y;

		return new weight_t(Math.round(Math.sqrt(dx * dx + dy * dy)));
	}

	public map_graph_t rand_map_graph(int N, double dim, double p_edge) {
		graph_t g = new graph_t(N);
		ArrayList<point_t> coords = new ArrayList<point_t>(N);

		for (int u = 0; u < N; ++u) {
			coords.add(new point_t(rand.nextDouble() * dim, rand.nextDouble() * dim));
		}

		for (int u = 0; u < N; ++u) {
			for (int v = 0; v < N; ++v) {
				if (u != v && rand.nextDouble() < p_edge) {
					double dx = coords.get(u).x - coords.get(v).x;
					double dy = coords.get(u).y - coords.get(v).y;

					weight_t w = new weight_t(Math.round(Math.sqrt(dx * dx + dy * dy)));
					g.graph_add_edge(new node_t(u), w, new node_t(v));
				}
			}
		}

		return new map_graph_t(g, coords);
	}

	public static void test_hashmap(hashmap_t.HashingModes mode) {
		int N = 1000;
		String[] keys = new String[N];

		hashmap_t m = new hashmap_t(2, mode);

		for (int i = 0; i < N; ++i) {
			String name = randname(rand.nextInt(50) + 1);
			name += i;

			keys[i] = name;
			m.hashmap_insert(new hashmap_t.hashmap_key_t(keys[i]), new hashmap_t.hashmap_value_t(i));
			if (!m.hashmap_contains(new hashmap_t.hashmap_key_t(keys[i]))) {
				System.out.format("CONTAINS-KEY: %s\n", keys[i]);
				m.hashmap_print_set();

				assert (false);
			}
		}

		assert (m.hashmap_get_size() == N);

		for (int i = 0; i < N; ++i) {
			assert (m.hashmap_contains(new hashmap_t.hashmap_key_t(keys[i])));
			hashmap_t.hashmap_value_t v = new hashmap_t.hashmap_value_t(Integer.MAX_VALUE);
			v = m.hashmap_lookup(new hashmap_t.hashmap_key_t(keys[i]));

			if (v == null) {
				assert (false);
			}
			assert (v.value == i);
		}

		for (int i = 0; i < N; ++i) {
			String name = randname(rand.nextInt(50) + 1);
			assert (!m.hashmap_contains(new hashmap_t.hashmap_key_t(name)));
		}
	}

	public void compute_weight_matrix(graph_t g, int N, weight_t[][] w) {
		for (int u = 0; u < N; ++u) {
			for (int v = 0; v < N; ++v) {
				w[u][v] = new weight_t.weight_inf();

				for (edge_tgt_t tgt : g.get_graph_succs(u)) {
					w[u][tgt.v.i] = tgt.w;
				}

			}
		}
	}

	class path_info_t {
		weight_t weight;
		int len;
		node_t src;
		node_t dst;

		public path_info_t(weight_t w, int len, node_t src, node_t dst) {
			this.weight = w;
			this.len = len;
			this.src = src;
			this.dst = dst;
		}
	}

	public path_info_t compute_path_info(graph_t g, path_t p) {
		path_info_t res = new path_info_t(new weight_t.weight_zero(), 0, node_t.INVALID_NODE, node_t.INVALID_NODE);
		int N = g.graph_get_num_nodes();

		if (p.path_len() == 0) {
			return res;
		}

		// Build weight matrix
		weight_t[][] w = new weight_t[N][N];

		compute_weight_matrix(g, N, w);

		node_t u = p.path_get(0);

		res.src = u;

		for (int i = 1; i < p.path_len(); ++i) {
			node_t v = p.path_get(i);
			++res.len;
			res.weight = weight_t.weight_add(res.weight, w[u.i][v.i]);
			u = v;
		}

		res.dst = u;
		return res;
	}

	public void testDPQ() throws Exception {
		int N = 1000;
		pq pq = new pq(N);

		// Fill queue
		weight_t total = new weight_t(0);

		for (int i = 0; i < N; ++i) {
			node_t node_i = new node_t(i);
			weight_t tmpw = pq.DPQ_prio(node_i);
			assert ((pq.DPQ_prio(node_i)).weight_is_inf()) : "Initially, all priorities must be infinite";

			if (i % 10 != 0) {
				weight_t w = randweight(.01, -10000, 10000);
				if (!w.weight_is_inf()) {
					total = weight_t.weight_add(total, w);
				}
				pq.DPQ_insert(node_i, w);
			}
		}
		if (settings.get_verb() > 1) {
			pq.print();
		}

		// Check membership, and total value

		weight_t total2 = new weight_t(0);
		for (int i = 0; i < N; ++i) {
			node_t node_i = new node_t(i);
			assert (pq.DPQ_contains(node_i) == (i % 10 != 0));
			if (pq.DPQ_contains(node_i)) {
				weight_t w = pq.DPQ_prio(node_i);
				if (!w.weight_is_inf()) {
					total2 = weight_t.weight_add(total2, w);
				}
			}
		}
		assert (weight_t.weight_eq(total2, total));

		// Decrease some keys

		total = new weight_t(0);
		for (int i = 1; i < N; ++i) {
			node_t node_i = new node_t(i);
			if (pq.DPQ_contains(node_i)) {
				weight_t w = pq.DPQ_prio(node_i);
				boolean ch = false;

				if (rand.nextDouble() < .5) {
					if (w.weight_is_inf()) {
						w = randweight(0, -10000, 10000);
					} else {
						int w_int = (int) w.weight_to_int();
						w = randweight(0, -11000, w_int - 1);
					}
					ch = true;
				}

				if (!w.weight_is_inf()) {
					total = weight_t.weight_add(total, w);
				}
				if (ch) {
					pq.DPQ_decrease_key(node_i, w);
				}

				assert (weight_t.weight_eq(pq.DPQ_prio(node_i), w));
			}
		}

		// Check membership and total value
		total2 = new weight_t(0);
		for (int i = 0; i < N; ++i) {
			node_t node_i = new node_t(i);
			if (pq.DPQ_contains(node_i)) {
				weight_t w = pq.DPQ_prio(node_i);
				if (!w.weight_is_inf()) {
					total2 = weight_t.weight_add(total2, w);
				}
			}
		}

		assert (weight_t.weight_eq(total2, total));

		// Pop keys
		total2 = new weight_t(0);

		assert (!pq.DPQ_is_empty());

		node_t u = pq.DPQ_pop_min();
		weight_t last = pq.DPQ_prio(u);

		if (!last.weight_is_inf()) {
			total2 = weight_t.weight_add(total2, last);
		}

		while (!pq.DPQ_is_empty()) {
			u = pq.DPQ_pop_min();
			weight_t w = pq.DPQ_prio(u);
			assert (!weight_t.weight_less(w, last));
			last = w;
			if (!last.weight_is_inf()) {
				total2 = weight_t.weight_add(total2, last);
			}
		}

		assert (weight_t.weight_eq(total2, total));

	}

	// test_hashmap

	public void check_sp_result(graph_t g, node_t src, node_t dst, weight_t real_dist, sp_result_t r) {
		assert (weight_t.weight_eq(r.dist, real_dist));

		assert (src.equals(r.src) && dst.equals(r.dst));

		if (r.path == null && r.dist.weight_is_inf()) {
			assert (src.i != dst.i);
			// No check for actual unreachabiliy
			return;
		}

		// If reachable, we expect a path and a finite distance
		assert (r.path != null && !r.dist.weight_is_inf());

		path_info_t pi = compute_path_info(g, r.path);

		// Path must go from src to dst
		assert (pi.src.equals(src) && pi.dst.equals(dst));

		// Its weight must match the claimed distance
		String s = String.format("Path weight must match claimed distance: %d != %d", pi.weight.__w, r.dist.__w);
		assert (weight_t.weight_eq(pi.weight, r.dist)) : s;
	}

	public void check_sssp_result(graph_t g, node_t src, sssp_result_t r) {
		int N = g.graph_get_num_nodes();
		assert (r.N == N);

		settings.msg(1, "check-apsp");

		assert (r.dist != null && r.pred != null);

		assert (src.equals(r.src) && r.dst == node_t.INVALID_NODE);

		/*
		 * Certification Algorithm
		 * 
		 * === Reachability === A node is reachable, iff its distance is not +inf. To
		 * verify reachability, we assert that there is no edge from a reachable to an
		 * unreachable node, and that src is reachable.
		 * 
		 * === Distance not too long === The distance of the start node must be 0, or
		 * -inf. We check that no edge can be relaxed, i.e., that for every edge (u,v),
		 * we have dist[u] + w(u,v) >= dist[v]. This property guarantees that the
		 * specified distances are not too long. NOTE: This also works for nodes on
		 * negative cycles: Setting their distance to -inf is the only way that every
		 * node on the negative cycle can be stable.
		 * 
		 * === Pred map correct === pred[u] must be INVALID_NODE for unreachable nodes.
		 * for reachable nodes, (pred[u],u) must be an edge. EXCEPTION: Start node, if
		 * finite distance: Here, pred[src]=src
		 * 
		 * === Distances not too short (finite) === For nodes u with finite distance, we
		 * check that dist[pred[u]] + w(pred[u],u) == dist[u]. EXCEPTION: Start node
		 * with finite distance
		 * 
		 * 
		 * === Distances not too short (negative weight cycles) === For nodes with -inf
		 * distance, we actually follow the predecessor map to find a negative weight
		 * cycle
		 * 
		 */

		weight_t[][] w = new weight_t[N][N];
		compute_weight_matrix(g, N, w);

		if (r.has_negative_cycle) {
			settings.msg(1, "Negative cycle reported");
		}

		assert (r.dist.get(src.i).weight_is_neg_inf()
				|| weight_t.weight_eq(r.dist.get(src.i), new weight_t.weight_zero()))
				: "Distance of start node can only be 0, or -inf";

		boolean has_neg_inf_dist = false; // Will be set if we encounter node with -inf distance. We'll compare that to
											// result's negative weight cycle.

		for (int u = 0; u < N; ++u) {
			for (edge_tgt_t tgt : g.get_graph_succs(u)) {
				node_t v = tgt.v;
				weight_t ew = tgt.w;

				assert (r.dist.get(u).weight_is_inf() || !(r.dist.get(v.i).weight_is_inf()))
						: "Edge from reachable to unreachable node";

				assert (!ew.weight_is_inf()); // If this fails, something is fishy with the graph: Edges shouldn't have
												// infinite weight

				weight_t rd = weight_t.weight_add(r.dist.get(u), ew); // Relax distance for node v

				assert (!weight_t.weight_less(rd, r.dist.get(v.i)))
						: "Edge " + u + " - (" + ew.weight_to_int() + ")-> " + v.i + " still relaxable (dist[u]="
								+ r.dist.get(u).__w + ") " + rd.__w + " < " + r.dist.get(v.i).__w; // If edge can still
																									// be relaxed,
																									// dist-map is
																									// fishy!
			}

			// Check for pred-map

			if (r.dist.get(u).weight_is_inf()) {
				// Unreachable node
				assert (r.pred.get(u) == node_t.INVALID_NODE);
			} else if (!r.dist.get(u).weight_is_neg_inf()) {
				// Reachable node, not over negative-weight cycle
				node_t pu = r.pred.get(u);
				assert (pu.i < N);

				if (u == src.i)
					assert (pu.equals(src)); // Special case: pred of sources points to itself
				else {
					weight_t ew = w[pu.i][u];
					assert (ew.weight_is_finite())
							: "Pred over non-edge: " + pu.i + " -> " + u + " (w=" + w[pu.i][u].__w + ")";
					weight_t rd = weight_t.weight_add(r.dist.get(pu.i), ew); // Relax distance for node u

					assert (weight_t.weight_eq(rd, r.dist.get(u))); // Estimates for edges on sortest path must be
																	// precise
				}

			} else {
				// Reachable node, over negative-weight cycle
				has_neg_inf_dist = true;

				node_t pu = r.pred.get(u);
				weight_t ew = w[pu.i][u];
				assert (pu.i < N);

				assert (ew.weight_is_finite())
						: "Pred over non-edge: " + pu.i + " -> " + u + " (w=" + w[pu.i][u].__w + ")";

				// Follow the cycle.
				// Take N steps to make sure we are actually on the cycle
				for (int i = 0; i < N; ++i) {
					pu = r.pred.get(pu.i);
					assert (pu.i < N);
				}

				// Now do another round through the cycle, computing its weight
				weight_t pw = new weight_t.weight_zero();
				node_t v = pu;
				pw = weight_t.weight_add(pw, w[r.pred.get(v.i).i][v.i]);
				v = r.pred.get(v.i);
				while (v != pu) {
					pw = weight_t.weight_add(pw, w[r.pred.get(v.i).i][v.i]);
					v = r.pred.get(v.i);
				}

				assert (weight_t.weight_less(pw, new weight_t.weight_zero()));

			}
		}
		assert (has_neg_inf_dist == r.has_negative_cycle);
	}

	public void check_sssp_result_compat(sssp_result_t r1, sssp_result_t r2) {
		assert (r1.N == r2.N);

		int N = r1.N;
		for (int u = 0; u < N; ++u) {
			assert (weight_t.weight_eq(r1.dist.get(u), r2.dist.get(u)));
		}
	}

	public static final int TEST_HASHMAP = 1;
	public static final int TEST_PQ = 2;
	public static final int TEST_BFS = 4;
	public static final int TEST_BELLMAN_FORD = 8;
	public static final int TEST_BELLMAN_FORD_NEG = 16;
	public static final int TEST_DIJKSTRA = 32;
	public static final int TEST_ASTAR = 64;

	int do_tests = 0;

	public weight_t check_sssp_algos(graph_t g, node_t src, node_t dst, boolean negative) {
		sssp_result_t r1 = null;
		sssp_result_t r2 = null;

		weight_t d = new weight_t.weight_inf();

		if (!negative && ((do_tests & TEST_DIJKSTRA) != 0)) {
			settings.msg(1, "Dijkstra");
			r1 = sp_algorithms.dijkstra(g, src, node_t.INVALID_NODE);
			check_sssp_result(g, src, r1);
			d = r1.dist.get(dst.i);
		}

		if ((do_tests & TEST_BELLMAN_FORD) != 0) {
			settings.msg(1, "Bellman-Ford");
			r2 = sp_algorithms.bellman_ford(g, src);
			check_sssp_result(g, src, r2);
			d = r2.dist.get(dst.i);
		}

		if (r1 != null && r2 != null) {
			settings.msg(1, "Results-Compat-Check");
			check_sssp_result_compat(r1, r2);
		}

		return d;

	}

	public graph_t graph_unweighted_of(graph_t g) {
		int N = g.graph_get_num_nodes();
		graph_t gg = new graph_t(N);

		for (int u = 0; u < N; ++u) {
			for (edge_tgt_t tgt : g.get_graph_succs(u)) {
				gg.graph_add_edge(new node_t(u), new weight_t(1), tgt.v);
			}
		}

		return gg;
	}

	public void do_graph_check(int N, double p, int mn, int mx) {
		String s = String.format("graph check %d %f [%d < %d]", N, p, mn, mx);
		settings.msg(1, s);

		graph_t g = rand_graph(N, p, mn, mx);

		if (settings.get_verb() > 1) {
			g.graph_write(System.out);
		}

		check_sssp_algos(g, new node_t(0), new node_t(0), mn < 0);

		if ((do_tests & TEST_BFS) == TEST_BFS) {
			settings.msg(1, "BFS");
			graph_t ug = graph_unweighted_of(g);
			sssp_result_t r = sp_algorithms.bfs(ug, new node_t(0), node_t.INVALID_NODE);
			check_sssp_result(ug, new node_t(0), r);
		}
	}

	public void do_map_graph_check(int N, double p, int src, int dst) {
		assert (src < N && dst < N);

		if ((do_tests & TEST_ASTAR) == 0)
			return;

		if ((do_tests & (TEST_DIJKSTRA | TEST_BELLMAN_FORD)) == 0) {
			sp_algorithms.error("For testing A*, you must also tst Dijkstra and/or Bellman-Ford");
		}

		String s = String.format("map graph check %d %f % d ->* %d", N, p, src, dst);
		settings.msg(1, s);

		map_graph_t mg = rand_map_graph(N, 10000, p);

		weight_t real_dist = check_sssp_algos(mg.g, new node_t(src), new node_t(dst), false);

		weight_t[] h = new weight_t[N];
		for (int u = 0; u < N; ++u) {
			h[u] = coords_dist(mg.coords, new node_t(u), new node_t(dst));
		}

		settings.msg(1, "A*");
		sp_result_t r = sp_algorithms.astar_search(mg.g, new node_t(src), new node_t(dst), h);

		check_sp_result(mg.g, new node_t(src), new node_t(dst), real_dist, r);

	}

	public static void reseed_rand() {
		long seed = 789231 & 0xFFFFFFFF;
		seed = (seed << 16) | 0x330E;
		rand.setSeed(seed);
	}

	public static void main(String[] args) {

		general general_settings = new general();
		general_settings.set_msg_verb(-1);
		sp test_rig = new sp(general_settings);

		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("hashmap")) {
				test_rig.do_tests |= TEST_HASHMAP;
			} else if (args[i].equals("pq")) {
				test_rig.do_tests |= TEST_PQ;
			} else if (args[i].equals("bfs")) {
				test_rig.do_tests |= TEST_BFS;
			} else if (args[i].equals("bellman-ford")) {
				test_rig.do_tests |= TEST_BELLMAN_FORD;
			} else if (args[i].equals("bellman-ford-neg")) {
				test_rig.do_tests |= TEST_BELLMAN_FORD | TEST_BELLMAN_FORD_NEG;
			} else if (args[i].equals("dijkstra")) {
				test_rig.do_tests |= TEST_DIJKSTRA;
			} else if (args[i].equals("astar")) {
				test_rig.do_tests |= TEST_ASTAR;
			} else if (args[i].equals("-v")) {
				general_settings.set_msg_verb(0);
			} else if (args[i].equals("-vv")) {
				general_settings.set_msg_verb(1);
			} else if (args[i].equals("-vvv")) {
				general_settings.set_msg_verb(2);
			} else {
				System.err.format("Unknown command line option: %s", args[i]);
				System.exit(-1);
			}
		}

		if ((test_rig.do_tests & TEST_HASHMAP) != 0) {
			general_settings.msg(0, "Testing Hashmap");
			reseed_rand();
			for (int i = 0; i < 200; ++i) {
				test_hashmap(hashmap_t.HashingModes.HASH_1_LINEAR_PROBING);
				test_hashmap(hashmap_t.HashingModes.HASH_1_DOUBLE_HASHING);
				test_hashmap(hashmap_t.HashingModes.HASH_1_QUADRATIC_PROBING);
			}
		}

		if ((test_rig.do_tests & TEST_PQ) != 0) {
			general_settings.msg(0, "Testing PQ");
			reseed_rand();
			for (int i = 0; i < 1000; ++i) {
				try {
					test_rig.testDPQ();
				} catch (Exception e) {
					general_settings.msg(0, "Something went Wrong!");
					general_settings.msg(0, e.getMessage());
					System.exit(-1);

				}
			}
		}

		int GRAPH_TESTS = TEST_ASTAR | TEST_BELLMAN_FORD | TEST_BFS | TEST_DIJKSTRA;
		if ((test_rig.do_tests & GRAPH_TESTS) != 0) {
			general_settings.msg(0, "Graph checks");

			double eprob = 1.0;

			reseed_rand();
			eprob = 1.0;
			for (int i = 0; i < 8; ++i) {
				test_rig.do_graph_check(100, eprob, 0, 10000);
				eprob = eprob / 2;
			}

			if ((test_rig.do_tests & TEST_BELLMAN_FORD_NEG) != 0) {
				general_settings.msg(0, "Negative weights");
				reseed_rand();
				eprob = 1.0;
				for (int i = 0; i < 8; ++i) {
					test_rig.do_graph_check(100, eprob, -10000, 10000);
					eprob = eprob / 2;
				}

				general_settings.msg(0, "Corner case: All negative");
				reseed_rand();
				eprob = 1.0;
				for (int i = 0; i < 8; ++i) {
					test_rig.do_graph_check(100, eprob, -10000, 0);
					eprob = eprob / 2;
				}
			}

			general_settings.msg(0, "Corner case: All zero");
			reseed_rand();
			eprob = 1.0;
			for (int i = 0; i < 8; ++i) {
				test_rig.do_graph_check(100, eprob, 0, 0);
				eprob = eprob / 2;
			}

			general_settings.msg(0, "Corner case: Singleton graph");
			reseed_rand();
			test_rig.do_graph_check(1, 0, 0, 0);
			test_rig.do_map_graph_check(1, 0, 0, 0);

			if ((test_rig.do_tests & TEST_ASTAR) != 0) {
				general_settings.msg(0, "A*");
				reseed_rand();
				eprob = 1.0;
				for (int i = 0; i < 8; ++i) {
					test_rig.do_map_graph_check(100, eprob, 0, 1);
					eprob = eprob / 2;
				}

			}

		}
	}

}
