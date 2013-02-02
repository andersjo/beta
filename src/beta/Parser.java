/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.io.CoNLLTree;


/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public interface Parser {

	abstract public CoNLLTree getBestParse(CoNLLTree input);
}
