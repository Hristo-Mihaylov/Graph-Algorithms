package comp26120;

public class node_t {
    int i;
    public static node_t INVALID_NODE = new node_t(Integer.MAX_VALUE);

    public node_t(int l) {
        this.i = l;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof node_t) {
            return this.hashCode() == ((node_t) o).hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return i;
    }
}
