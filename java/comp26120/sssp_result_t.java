package comp26120;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class sssp_result_t {
	int N;
	node_t src;
	node_t dst;
	ArrayList<node_t> pred = new ArrayList<node_t>();
	boolean has_negative_cycle;
	ArrayList<weight_t> dist = new ArrayList<weight_t>();
	long relax_count;

	public sssp_result_t(int N, node_t src, node_t dst, boolean ncyc, ArrayList<node_t> p, ArrayList<weight_t> d,
			long c) {
		this.N = N;
		this.src = src;
		this.dst = dst;
		this.pred.addAll(p);
		this.has_negative_cycle = ncyc;
		this.dist.addAll(d);
		this.relax_count = c;
	}

	public sp_result_t sssp_to_sp_result(node_t dst) {
		path_t p = null;

		assert (this.dst == node_t.INVALID_NODE || this.dst == dst);

		if (pred.get(dst.i) != node_t.INVALID_NODE) {
			p = new path_t(this.pred, dst);
		}

		sp_result_t r = new sp_result_t(this.src, dst, p, dist.get(dst.i), relax_count);
		return r;
	}

	public void print_sssp_result(OutputStream os) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(os);
			int NN = N < 10 ? N : 10;

			writer.write("Distmap:");
			for (int i = 0; i < NN; ++i) {
				writer.write(" ");
				dist.get(i).print_weight();
			}

			if (N < NN) {
				writer.write("...\n");
			} else {
				writer.write("\n");
			}

		} catch (IOException e) {
			System.err.println("Error Message: " + e.getMessage());
			System.exit(-1);
		}

	}

}
