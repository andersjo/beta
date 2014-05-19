/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
@FunctionalInterface
public interface FeatureHandler {

	void handle(long feature);

	default void handle(long x1, int o1, long x2) {
		assert getNBits(x1) <= o1;
		assert getNBits(x2) + o1 <= 64;
		handle(x2 << o1 | x1);
	}

	default void handle(long x1, int o1, long x2, int o2, long x3) {
		assert getNBits(x1) <= o1;
		assert getNBits(x2) <= o2;
		assert getNBits(x3) + o2 + o1 <= 64;
		handle((x3 << o2 | x2) << o1 | x1);
	}

	default void handle(long x1, int o1, long x2, int o2, long x3, int o3, long x4) {
		assert getNBits(x1) <= o1;
		assert getNBits(x2) <= o2;
		assert getNBits(x3) <= o3;
		assert getNBits(x4) + o3 + o2 + o1 <= 64;
		handle(((x4 << o3 | x3) << o2 | x2) << o1 | x1);
	}

	default void handle(long x1, int o1, long x2, int o2, long x3, int o3, long x4, int o4, long x5) {
		assert getNBits(x1) <= o1;
		assert getNBits(x2) <= o2;
		assert getNBits(x3) <= o3;
		assert getNBits(x4) <= o4;
		assert getNBits(x5) + o4 + o3 + o2 + o1 <= 64;
		handle((((x5 << o4 | x4) << o3 | x3) << o2 | x2) << o1 | x1);
	}

	default void handle(long x1, int o1, long x2, int o2, long x3, int o3, long x4, int o4, long x5, int o5, long x6) {
		assert getNBits(x1) <= o1;
		assert getNBits(x2) <= o2;
		assert getNBits(x3) <= o3;
		assert getNBits(x4) <= o4;
		assert getNBits(x5) <= o5;
		assert getNBits(x6) + o5 + o4 + o3 + o2 + o1 <= 64;
		handle(((((x6 << o5 | x5) << o4 | x4) << o3 | x3) << o2 | x2) << o1 | x1);
	}

	static int getNBits(long x) {
		return (int) Math.ceil(Math.log(x) / Math.log(2));
	}
}
