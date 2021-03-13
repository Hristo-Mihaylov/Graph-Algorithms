package comp26120;

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;

public class weight_t {
	static Long _WINF = Long.MAX_VALUE;
	static Long _WNINF = Long.MIN_VALUE;
	public static Long WEIGHT_MIN = Long.MIN_VALUE + 1;
	public static Long WEIGHT_MAX = Long.MAX_VALUE - 1;

	long __w;

	public weight_t() {
	}

	public weight_t(long w) {
		if (w == _WINF || w == _WNINF) {
			System.err.println("Weight overflow");
			System.exit(-1);
		}
		__w = w;
	}

	public weight_t(double w) throws Exception {
		this(Math.round(w));
	}

	static class weight_inf extends weight_t {
		public weight_inf() {
			__w = _WINF;
		}
	}

	static class weight_neg_inf extends weight_t {
		public weight_neg_inf() {
			__w = _WNINF;
		}
	}

	static class weight_zero extends weight_t {
		public weight_zero() {
			super(0);
		}

	}

	public boolean weight_is_inf() {
		return __w == _WINF;
	}

	public boolean weight_is_neg_inf() {
		return __w == _WNINF;
	}

	public boolean weight_is_finite() {
		return !weight_is_inf() && !weight_is_neg_inf();
	}

	public long weight_to_int() {
		assert (weight_is_finite()) : "Weight must be finite";
		return __w;
	}

	public static final weight_t weight_add(weight_t a, weight_t b) {
		if (a.weight_is_inf()) {
			assert (!b.weight_is_neg_inf()) : "inf + -inf undefined";
			return new weight_inf();
		} else if (a.weight_is_neg_inf()) {
			assert (!b.weight_is_inf()) : "-inf + inf undefined";
			return new weight_neg_inf();
		} else if (b.weight_is_inf()) {
			return new weight_inf();
		} else if (b.weight_is_neg_inf()) {
			return new weight_neg_inf();
		} else {
			long res = a.__w + b.__w;
			try {
				weight_t res_w = new weight_t(res);
				return res_w;
			} catch (Exception e) {
				System.err.println("Sum of weights exceeded int max value");
				System.exit(-1);
			}
			return null;
		}
	}

	public weight_t weight_sub(weight_t a, weight_t b) {
		if (a.weight_is_inf()) {
			assert (!b.weight_is_inf()) : "inf -inf undefined";
			return new weight_inf();
		} else if (a.weight_is_neg_inf()) {
			assert (!b.weight_is_neg_inf()) : "-inf - -inf undefined";
			return new weight_neg_inf();
		} else if (b.weight_is_inf()) {
			return new weight_neg_inf();
		} else if (b.weight_is_neg_inf()) {
			return new weight_inf();
		} else {
			long res = a.__w - b.__w;
			try {
				weight_t res_w = new weight_t(res);
				return res_w;
			} catch (Exception e) {
				System.err.println("Sub of weights exceeded int max value");
				System.exit(-1);
			}
			return null;
		}
	}

	public static boolean weight_less(weight_t a, weight_t b) {
		return a.__w < b.__w;
	}

	public static boolean weight_eq(weight_t a, weight_t b) {
		return a.__w == b.__w;
	}

	public void print_weight(OutputStream os) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(os);
			if (weight_is_inf()) {
				writer.write("inf");
			} else if (weight_is_neg_inf()) {
				writer.write("-inf");
			} else {
				String s = String.format("%d", weight_to_int());
				writer.write(s);
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Error Message: " + e.getMessage());
			System.exit(-1);
		}
	}

	public void print_weight() {
		if (weight_is_inf()) {
			System.err.println("inf");
		} else if (weight_is_neg_inf()) {
			System.err.println("-inf");
		} else {
			String s = String.format("%d", weight_to_int());
			System.err.println(s);
		}
	}

}
