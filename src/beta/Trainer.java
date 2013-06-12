/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public interface Trainer {

	abstract public void update(CoNLLTree graph);

	abstract public void averageWeightVector();

	abstract public double[] getAveragedWeightVector();
}
