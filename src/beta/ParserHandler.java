/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.tree.Tree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class ParserHandler {

	private final Parser parser;

	public ParserHandler(Parser parser) {
		this.parser = parser;
	}

	public Tree next(Tree tree) {
		Tree input = new Tree(tree);
		for (int i = 0; i < input.getNNodes(); i++) {
			input.heads[i] = 0;
			input.deprels[i] = Model.UNKNOWN_LABEL;
		}
		return parser.getBestParse(input);
	}
}
