package comp26120;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class sp_result_t {
    node_t src;
    node_t dst;
    path_t path;
    weight_t dist;
    long relax_count;

    public sp_result_t(node_t src, node_t dst, path_t p, weight_t d, long c) {
        this.src = src;
        this.dst = dst;
        this.path = p;
        this.dist = d;
        this.relax_count = c;
    }

    public void print_sp_result(OutputStream os) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(os);
            writer.write("Distance: ");
            dist.print_weight(os);
            writer.write("\n");

            writer.write("Path: ");
            path.print_path(os);
            writer.write("\n");

            String s = String.format("# Relaxed nodes: %%ll\n", relax_count);
            writer.write(s);

        } catch (IOException e) {
            System.err.println("Error Message: " + e.getMessage());
            System.exit(-1);
        }

    }
}
