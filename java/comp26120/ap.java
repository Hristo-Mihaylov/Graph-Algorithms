package comp26120;

import comp26120.airports.apid_t;
import java.util.*;

public class ap {
	airports airport_info = new airports();
	general settings;

	public ap(general settings) {
		this.settings = settings;
	}

	private static char[] code_string_to_code(String code_string) {
		char[] code = new char[3];
		for (int i = 0; i < 3; i++) {
			code[i] = code_string.charAt(i);
		}
		// code[3] = 0;
		return code;
	}

	// method for the recursive dfs algorithm
	public static void dfs(graph_t g, node_t src, boolean[] visited) {

		assert src != node_t.INVALID_NODE : "The source node is invalid";

		if (visited[src.hashCode()] == false) {
			// if we discover a node we update its value in the array
			visited[src.hashCode()] = true;
			for (edge_tgt_t t : g.get_graph_succs(src.hashCode())) {
				dfs(g, t.v, visited); // recursion
			}
		}
	}

	public void count_reachable(String code_string) {
		char[] code = code_string_to_code(code_string);
		int count = 0;

		graph_t g = airport_info.ap_get_graph();

		airports.apid_t src = airport_info.ap_get_id(code);
		// we initialise this array with initial false values
		boolean[] visited = new boolean[airport_info.ap_get_num_ids()];

		dfs(g, src, visited);

		for (int i = 0; i < airport_info.ap_get_num_ids(); i++) {
			if (visited[i] == true) { // i.e if we discovered this node
				count++;
			}
		}

		System.out.format("%d airports reachable from %s\n", count, code_string);
	}

	public void compute_route(String algo, String scode_string, String dcode_string) {
		char[] scode = code_string_to_code(scode_string);
		char[] dcode = code_string_to_code(dcode_string);

		airports.apid_t s = airport_info.ap_get_id(scode);
		airports.apid_t d = airport_info.ap_get_id(dcode);
		graph_t g = airport_info.ap_get_graph();

		// initialise sssp_result_t and sp_result_t objects that will be updated later
		sssp_result_t sssp = new sssp_result_t(0, node_t.INVALID_NODE, node_t.INVALID_NODE, false,
				new ArrayList<node_t>(), new ArrayList<weight_t>(), 0);

		sp_result_t sp = new sp_result_t(node_t.INVALID_NODE, node_t.INVALID_NODE, new path_t(0),
				new weight_t.weight_zero(), 0);

		// since these methods return sssp_result_t object, we separate them from A*
		if (algo.equals("bfs") || algo.equals("bellman-ford") || algo.equals("dijkstra")) {
			if (algo.equals("bfs")) {

				sssp = sp_algorithms.bfs(g, s, d);

			} else if (algo.equals("bellman-ford")) {

				sssp = sp_algorithms.bellman_ford(g, s);

			} else if (algo.equals("dijkstra")) {

				sssp = sp_algorithms.dijkstra(g, s, d);

			}

			// check if the weight of the destination node is infinity, i.e no route
			if (sssp.dist.get(d.hashCode()).weight_is_inf()) {

				System.out.format("No route from %s to %s\n", scode_string, dcode_string);
				System.out.format("LOG: relaxed %d edges\n\n", sssp.relax_count);

			} else {

				path_t path = g.path_from_pred(sssp.pred, d);

				long tot = 0; // total number of edges

				for (int i = 0; i < path.path_len() - 1; i++) {

					// introduce some variables
					apid_t a = new apid_t(path.path_get(i));
					apid_t b = new apid_t(path.path_get(i + 1));

					weight_t dist = airport_info.ap_get_dist(a, b);
					char[] first = airport_info.ap_get_code(a);
					String f = String.valueOf(first);

					char[] second = airport_info.ap_get_code(b);
					String sec = String.valueOf(second);

					tot = tot + dist.weight_to_int();
					System.out.format("%s to %s (%dkm)\n", f, sec, dist.weight_to_int());
				}

				System.out.format("Total = %dkm\n", tot);

				System.out.format("LOG: relaxed %d edges\n\n", sssp.relax_count);
			}
		} else if (algo.equals("astar")) { // since it returns sp_result_t

			int N = 14110;

			// creating heuristics for the path
			weight_t[] h = new weight_t[N];

			for (int i = 0; i < N; ++i) {
				airports.apid_t temp = new airports.apid_t(i);
				if (airport_info.ap_is_valid_id(temp)) {
					h[i] = airport_info.ap_get_dist(d, temp);
				} else {
					h[i] = new weight_t.weight_inf();
				}
			}

			sp = sp_algorithms.astar_search(g, s, d, h);

			path_t path = sp.path;

			if (path == null) { // if the path is null then there is no route from start to destination
				System.out.format("No route from %s to %s\n", scode_string, dcode_string);
				System.out.format("LOG: relaxed %d edges\n\n", sp.relax_count);
			} else {

				long tot = 0;

				for (int i = 0; i < path.path_len() - 1; i++) {

					apid_t a = new apid_t(path.path_get(i));
					apid_t b = new apid_t(path.path_get(i + 1));

					weight_t dist = airport_info.ap_get_dist(a, b);
					char[] first = airport_info.ap_get_code(a);
					String f = String.valueOf(first);

					char[] second = airport_info.ap_get_code(b);
					String sec = String.valueOf(second);
					tot = tot + dist.weight_to_int();
					System.out.format("%s to %s (%dkm)\n", f, sec, dist.weight_to_int());
				}

				System.out.format("Total = %dkm\n", tot);

				System.out.format("LOG: relaxed %d edges\n\n", sp.relax_count);
			}

		}

	}

	public static void main(String[] args) {
		general general_settings = new general();
		general_settings.set_msg_verb(-1);

		ap ap = new ap(general_settings);
		ap.airport_info.ap_std_init();

		if (args.length == 4 && args[0].equals("route")) {
			ap.compute_route(args[1], args[2], args[3]);
		} else if (args.length == 2 && args[0].equals("count")) {
			ap.count_reachable(args[1]);
		} else {
			sp_algorithms.error("Invalid command line");
		}
	}
}

// some commented code

// public void compute_route(String algo, String scode_string, String
// dcode_string) {
// char[] scode = code_string_to_code(scode_string);
// char[] dcode = code_string_to_code(dcode_string);

// airports.apid_t s = airport_info.ap_get_id(scode);
// airports.apid_t d = airport_info.ap_get_id(dcode);
// graph_t g = airport_info.ap_get_graph();

// sssp_result_t sssp = sp_algorithms.dijkstra(g, s, d);

// if (sssp.dist.get(d.hashCode()).weight_is_inf()) {

// System.out.format("No route from %s to %s\n", scode_string, dcode_string);
// System.out.format("LOG: relaxed %d edges\n\n", sssp.relax_count);

// } else {

// path_t path = g.path_from_pred(sssp.pred, d);

// long tot = 0;

// for (int i = 0; i < path.path_len() - 1; i++) {
// apid_t a = new apid_t(path.path_get(i));
// apid_t b = new apid_t(path.path_get(i + 1));
// weight_t dist = airport_info.ap_get_dist(a, b);
// char[] first = airport_info.ap_get_code(a);
// String f = String.valueOf(first);
// char[] second = airport_info.ap_get_code(b);
// String sec = String.valueOf(second);
// tot = tot + dist.weight_to_int();
// System.out.format("%s to %s (%dkm)\n", f, sec, dist.weight_to_int());
// }

// System.out.format("Total = %dkm\n", tot);

// System.out.format("LOG: relaxed %d edges\n\n", sssp.relax_count);
// }

// sssp_result_t sssp = sp_algorithms.dijkstra(g, s, d);

// if (sssp.dist.get(d.hashCode()).weight_is_inf()) {

// System.out.format("No route from %s to %s\n", scode_string, dcode_string);
// System.out.format("LOG: relaxed %d edges\n\n", sssp.relax_count);

// } else {

// path_t path = g.path_from_pred(sssp.pred, d);

// long tot = 0;

// for (int i = 0; i < path.path_len() - 1; i++) {
// apid_t a = new apid_t(path.path_get(i));
// apid_t b = new apid_t(path.path_get(i + 1));
// weight_t dist = airport_info.ap_get_dist(a, b);
// char[] first = airport_info.ap_get_code(a);
// String f = String.valueOf(first);
// char[] second = airport_info.ap_get_code(b);
// String sec = String.valueOf(second);
// tot = tot + dist.weight_to_int();
// System.out.format("%s to %s (%dkm)\n", f, sec, dist.weight_to_int());
// }

// System.out.format("Total = %dkm\n", tot);

// System.out.format("LOG: relaxed %d edges\n\n", sssp.relax_count);
// }

/**
 * Compute the shortest route between s and d, using the specified algorithms!
 * "bfs" should compute a route with minimal hops, all other algorithms compute
 * a route with minimal mileage
 */

// if (algo.equals("bellman-ford")) {
// } else if (algo.equals("dijkstra")) {
// } else if (algo.equals("astar")) {
// } else if (algo.equals("bfs")) {
// } else {
// sp_algorithms.error("Invalid algorithm name: " + algo);
// }

/**
 * Output one line per hop, indicating source, destination, and lenght Finally,
 * output the total lenght
 */

// System.out.format("%s to %s (%dkm)\n", "MAN", "HEL", 1812);
// System.out.format("%s to %s (%dkm)\n", "HEL", "HKG", 7810);
// System.out.format("%s to %s (%dkm)\n", "HKG", "SYD", 7394);
// System.out.format("Total = %dkm\n", 17016);

// /// If there is no route ...
// System.out.format("No route from %s to %s\n", scode_string, dcode_string);

// /// And, in any case, log the number of relaxed/explored edges
// System.out.format("LOG: relaxed %d edges\n\n", 1896);