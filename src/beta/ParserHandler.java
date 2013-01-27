/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class ParserHandler {

	private final Parser parser;

	public ParserHandler(Parser parser) {
		this.parser = parser;
	}

	public CoNLLTree next(CoNLLTree tree) {
		CoNLLTree input = new CoNLLTree(tree);
		for (int i = 0; i < input.getNNodes(); i++) {
			input.heads[i] = 0;
			input.deprels[i] = Model.UNKNOWN_LABEL;
		}
		return parser.getBestParse(input);
	}
}
