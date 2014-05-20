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

	void handle(long f);

	default void handle(long f1, int o1, long f2) {
		handle(f2 << o1 | f1);
	}

	default void handle(long f1, int o1, long f2, int o2, long f3) {
		handle((f3 << o2 | f2) << o1 | f1);
	}

	default void handle(long f1, int o1, long f2, int o2, long f3, int o3, long f4) {
		handle(((f4 << o3 | f3) << o2 | f2) << o1 | f1);
	}

	default void handle(long f1, int o1, long f2, int o2, long f3, int o3, long f4, int o4, long f5) {
		handle((((f5 << o4 | f4) << o3 | f3) << o2 | f2) << o1 | f1);
	}

	default void handle(long f1, int o1, long f2, int o2, long f3, int o3, long f4, int o4, long f5, int o5, long f6) {
		handle(((((f6 << o5 | f5) << o4 | f4) << o3 | f3) << o2 | f2) << o1 | f1);
	}
}
