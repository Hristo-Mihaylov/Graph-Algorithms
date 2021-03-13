package comp26120;

import java.util.ArrayList;

public class pq implements PriorityQueue {
	class hidx_t {
		int i;

		public hidx_t(int i) {
			this.i = i;
		}

		public int idx_of() {
			return i - 1;
		}

		public boolean hidx_is_invalid() {
			return i == 0;
		}

		// 1-based operations
		public hidx_t parent() {
			return new hidx_t(Math.round(i / 2));
		}

		public hidx_t left() {
			return new hidx_t(i * 2);
		}

		public hidx_t right() {
			return new hidx_t(i * 2 + 1);
		}

		public boolean has_parent() {
			return parent().i > 0;
		}

		public boolean has_left(int n) {
			return left().i <= n;
		}

		public boolean has_right(int n) {
			return right().i <= n;
		}

	}

	public hidx_t INVALID_HIDX = new hidx_t(0);
	public hidx_t hidx_first = new hidx_t(1);

	int num_elem;
	int heap_size;
	weight_t[] D;
	node_t[] H;
	hidx_t[] I;

	public pq(int num_elem) {
		this.num_elem = num_elem;
		this.heap_size = 0;

		this.D = new weight_t[num_elem];
		this.H = new node_t[num_elem];
		this.I = new hidx_t[num_elem];

		for (int i = 0; i < num_elem; ++i) {
			this.I[i] = INVALID_HIDX;
			this.D[i] = new weight_t.weight_inf();
		}
	}

	public ArrayList<weight_t> DPQ_dist_free() {
		ArrayList<weight_t> res = new ArrayList<weight_t>();
		for (int i = 0; i < num_elem; i++) {
			res.add(D[i]);
		}

		return res;
	}

	public weight_t DPQ_prio(node_t u) {
		assert (u.i < this.num_elem);
		return D[u.i];
	}

	weight_t _DPQ_hprio(hidx_t i) {
		return DPQ_prio(H[i.idx_of()]);
	}

	void _DPQ_adjustI(hidx_t i) {
		I[H[i.idx_of()].i] = i;
	}

	void _DPQ_swap(hidx_t i, hidx_t j) {
		node_t t = H[i.idx_of()];
		H[i.idx_of()] = H[j.idx_of()];
		H[j.idx_of()] = t;
		_DPQ_adjustI(i);
		_DPQ_adjustI(j);
	}

	void _DPQ_sift_up(hidx_t i) {
		while (i.has_parent() && weight_t.weight_less(_DPQ_hprio(i), _DPQ_hprio(i.parent()))) {
			_DPQ_swap(i, i.parent());
			i = i.parent();
		}
	}

	void _DPQ_sift_down(hidx_t i) {
		while (i.has_right(heap_size)) {
			weight_t w = _DPQ_hprio(i);
			weight_t lw = _DPQ_hprio(i.left());
			weight_t rw = _DPQ_hprio(i.right());

			if (!weight_t.weight_less(lw, w) && !weight_t.weight_less(rw, w)) {
				return;
			}

			if (weight_t.weight_less(lw, rw)) {
				_DPQ_swap(i, i.left());
				i = i.left();
			} else {
				_DPQ_swap(i, i.right());
				i = i.right();
			}
		}

		if (i.has_left(heap_size)) {
			weight_t w = _DPQ_hprio(i);
			weight_t lw = _DPQ_hprio(i.left());
			if (weight_t.weight_less(lw, w)) {
				_DPQ_swap(i, i.left());
			}
		}
	}

	public boolean DPQ_contains(node_t u) {
		assert (u.i < num_elem);
		return (!I[u.i].hidx_is_invalid());
	}

	hidx_t _DPQ_last_idx() {
		return new hidx_t(heap_size);
	}

	public void DPQ_insert(node_t u, weight_t w) {
		assert (u.i < num_elem);
		assert (!DPQ_contains(u));
		D[u.i] = w;
		heap_size++;
		hidx_t i = _DPQ_last_idx();
		H[i.idx_of()] = u;
		_DPQ_adjustI(i);
		_DPQ_sift_up(i);
	}

	public boolean DPQ_is_empty() {
		return heap_size == 0;
	}

	public node_t DPQ_pop_min() {
		assert (!DPQ_is_empty());
		hidx_t li = _DPQ_last_idx();
		_DPQ_swap(hidx_first, li);
		node_t res = H[li.idx_of()];
		I[res.i] = INVALID_HIDX;
		heap_size--;
		_DPQ_sift_down(hidx_first);
		return res;
	}

	public void DPQ_decrease_key(node_t u, weight_t w) {
		assert (u.i < num_elem);
		assert (DPQ_contains(u));
		assert (weight_t.weight_less(w, D[u.i]));
		D[u.i] = w;

		hidx_t i = I[u.i];
		_DPQ_sift_up(i);
	}

	public void print() {
		System.out.format("BinaryHeap has %d elements with %d size\n", num_elem, heap_size);
		for (int i = 0; i < num_elem; i++) {
			System.out.println(I[i].i);
			D[i].print_weight();
		}
	}

}
