package comp26120;

class edge_tgt_t {
    weight_t w; /// Edge weight
    node_t v; /// Target node

    public edge_tgt_t(weight_t w, node_t v) {
        this.w = w;
        this.v = v;
    }

    boolean _tgt_is_invalid() {
        return v == node_t.INVALID_NODE;
    }
}
