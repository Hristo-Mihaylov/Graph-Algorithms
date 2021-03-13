package comp26120;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class airports {
	public static final int R = 6371;
	public static final double TO_RAD = (3.1415926536 / 180);

	public static double dist(double lat1, double lng1, double lat2, double lng2) {
		double dx, dy, dz;
		lng1 -= lng2;
		lng1 *= TO_RAD;
		lat1 *= TO_RAD;
		lat2 *= TO_RAD;

		dz = Math.sin(lat1) - Math.sin(lat2);
		dx = Math.cos(lng1) * Math.cos(lat1) - Math.cos(lat2);
		dy = Math.sin(lng1) * Math.cos(lat1);

		return Math.asin(Math.sqrt(dx * dx + dy * dy + dz * dz) / 2) * 2 * R;
	}

	public class airport_t {
		char[] code = new char[3]; // We use the empty string to indicate unassigned ids
		double lat;
		double lng;

		public airport_t(char[] code, double lat, double lng) {
			this.code = code;
			this.lat = lat;
			this.lng = lng;
		}

		public airport_t() {
			this.code[0] = 0;
			this.lat = 0;
		}
	}

	public static class apid_t extends node_t {
		public apid_t(int i) {
			super(i);
		}

		public apid_t(node_t u) {
			super(u.i);
		}

		@Override
		public int hashCode() {
			return this.i;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof node_t) {
				return this.i == ((node_t) o).i;
			}
			return false;
		}
	}

	int num_apids = 0;
	ArrayList<airport_t> airports;
	hashmap_t code_id_map = null;

	graph_t route_g;

	public int ap_get_num_ids() {
		return num_apids;
	}

	public void ap_read(File f) {
		int n;

		assert (airports == null && code_id_map == null);
		try {
			Scanner scanIn = new Scanner(f);
			num_apids = scanIn.nextInt();
			n = scanIn.nextInt();

			code_id_map = new hashmap_t(num_apids, hashmap_t.HashingModes.HASH_1_LINEAR_PROBING);
			airports = new ArrayList<airport_t>();
			for (int i = 0; i < num_apids; ++i) {
				airports.add(new airport_t());
			}

			for (int i = 0; i < n; ++i) {
				apid_t id;
				char[] code = new char[3];
				double lat;
				double lng;

				id = new apid_t(scanIn.nextInt());

				String code_string = scanIn.next();

				for (int j = 0; j < 3; j++) {
					code[j] = code_string.charAt(j);
				}

				lat = scanIn.nextDouble();
				lng = scanIn.nextDouble();

				if (!(id.i < num_apids)) {
					sp_algorithms.error("Invalid airport ID: " + id);
				}

				// code[3] = 0;
				airports.set(id.i, new airport_t(code, lat, lng));

				code_id_map.hashmap_insert(new hashmap_t.hashmap_key_t(code), new hashmap_t.hashmap_value_t(id.i));

			}

			scanIn.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
			System.out.println(e.getMessage());
			System.exit(-1);
		}

	}

	public void ap_read_routes(File f) {
		try {
			Scanner scanIn = new Scanner(f);
			int num_edges = scanIn.nextInt();

			assert (route_g == null);
			route_g = new graph_t(ap_get_num_ids());

			for (int i = 0; i < num_edges; ++i) {
				// System.out.println("Where : " + i + " " + num_edges);
				apid_t id1 = ap_check_id(new apid_t(scanIn.nextInt()));
				apid_t id2 = ap_check_id(new apid_t(scanIn.nextInt()));

				weight_t dist = ap_get_dist(id1, id2);

				// System.out.println("adding edge : " + id1.i + " " + dist.__w + " " + id2.i);
				route_g.graph_add_edge(id1, dist, id2);
			}

			scanIn.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
			System.out.println(e.getMessage());
			System.exit(-1);
		}

	}

	public void ap_std_init() {
		try {
			File f = new File("../data/airports.txt");
			ap_read(f);

			f = new File("../data/routes.txt");
			ap_read_routes(f);

		} catch (Exception e) {
			System.err.println("Error opening files");
			System.exit(-1);
		}

	}

	public graph_t ap_get_graph() {
		return route_g;
	}

	public weight_t ap_get_dist(apid_t id1, apid_t id2) {
		return new weight_t(Math.round(ap_get_dist_dbl(id1, id2)));
	}

	public boolean ap_is_valid_id(apid_t id) {
		return (id.i < num_apids && airports.get(id.i).code[0] != 0);
	}

	public void ap_invalidate_id(apid_t id) {
		assert (id.i < num_apids);
		// Note: As our hashtables do not support delete, we insert a validity check
		// afte retreiving a code.
		airports.get(id.i).code[0] = 0;
	}

	public apid_t ap_check_id(apid_t id) {
		if (!ap_is_valid_id(id)) {
			sp_algorithms.error("Invalid airport id: " + id.i);
		}
		return id;
	}

	public char[] ap_get_code(apid_t id) {
		assert (ap_is_valid_id(id));
		return airports.get(id.i).code;
	}

	public double ap_get_lat(apid_t id) {
		assert (ap_is_valid_id(id));
		return airports.get(id.i).lat;
	}

	public double ap_get_lng(apid_t id) {
		assert (ap_is_valid_id(id));
		return airports.get(id.i).lng;
	}

	public apid_t ap_get_id_aux(char[] code) {
		return new apid_t(code_id_map.hashmap_lookup(new hashmap_t.hashmap_key_t(code)).value);
	}

	public boolean ap_is_valid_code(char[] code) {
		apid_t res = null;
		res = ap_get_id_aux(code);
		return (res != null && ap_is_valid_id(res));
	}

	public apid_t ap_get_id(char[] code) {
		apid_t res = ap_get_id_aux(code);
		if (res == null || !ap_is_valid_id(res)) {
			sp_algorithms.error("Uknown airport ID: " + code);
		}

		return res;
	}

	public double ap_get_dist_dbl(apid_t id1, apid_t id2) {
		return (dist(ap_get_lat(id1), ap_get_lng(id1), ap_get_lat(id2), ap_get_lng(id2)));
	}

}
