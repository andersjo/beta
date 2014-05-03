/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class ProgressPrinter {

	private final int updateIntervalSmall;
	private final int updateIntervalLarge;
	private int nTrees;

	public ProgressPrinter(int updateSmall, int updateLarge) {
		assert updateLarge % updateSmall == 0;
		this.updateIntervalSmall = updateSmall;
		this.updateIntervalLarge = updateLarge;
	}

	public ProgressPrinter() {
		this(100, 1000);
	}

	public void update() {
		nTrees++;
		if (nTrees % updateIntervalSmall == 0) {
			System.err.print(".");
			if (nTrees % updateIntervalLarge == 0) {
				System.err.format(" (%d)%n", nTrees);
			}
		}
	}

	public void exit() {
		if (nTrees % updateIntervalLarge != 0) {
			System.err.println();
		}
	}
}
