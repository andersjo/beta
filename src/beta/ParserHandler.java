/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.conll.CoNLLReader;
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
			input.forms[i] = CoNLLReader.normalize(input.forms[i]);
			input.lemmas[i] = CoNLLReader.normalize(input.lemmas[i]);
		}

		CoNLLTree best = parser.getBestParse(input);
		for (int i = 0; i < tree.getNNodes(); i++) {
			best.forms[i] = tree.forms[i];
			best.lemmas[i] = tree.lemmas[i];
		}
		
		return best;
	}
}
