/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLReader;
import beta.conll.CoNLLTree;
import beta.conll.CoNLLWriter;
import beta.options.Option;
import beta.options.OptionException;
import beta.options.OptionParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Command-line interface.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Main {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Main() {
	}

	/**
	 * @param args the command line arguments
	 * @throws IOException
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
			System.exit(1);
		}
		if (args[0].equals("train")) {
			train(Arrays.copyOfRange(args, 1, args.length));
			System.exit(0);
		}
		if (args[0].equals("parse")) {
			parse(Arrays.copyOfRange(args, 1, args.length));
			System.exit(0);
		}
		usage();
		System.exit(1);
	}

	private static void usage() {
		System.err.println("Usage: beta train [-n it] [-s] -i input -m model");
		System.err.println("       beta parse -m model -i input -o output");
	}

	public static void train(String[] args) {
		TrainOptions options = new TrainOptions();
		OptionParser<TrainOptions> optionParser = new OptionParser<TrainOptions>(options);
		try {
			optionParser.parse(args);
		} catch (OptionException e) {
			System.err.println(e.getMessage());
			optionParser.usage();
			System.exit(1);
		}

		System.err.format("Reading from %s ...", options.inputFileName);

		ModelExtractor modelExtractor = new ModelExtractor();
		try {
			CoNLLReader reader = new CoNLLReader(options.inputFileName);
			CoNLLTree tree;
			while ((tree = reader.read()) != null) {
				modelExtractor.next(tree);
			}
		} catch (FileNotFoundException e) {
			System.err.println();
			failWithFileNotFoundException(options.inputFileName);
		} catch (IOException e) {
			System.err.println();
			failWithIOException(options.inputFileName);
		}

		System.err.println(" done.");

		Model model = modelExtractor.getModel();

		int nTrees = modelExtractor.getNTrees();
		int nWords = model.getNForms();
		int nTags = model.getNPOSTags();
		int nLabels = model.getNDeprels();
		int nFeatures = model.getNFeatures();
		System.err.format("Found %d trees, %d word forms, %d tags, and %d edge labels.%n", nTrees, nWords, nTags, nLabels);
		System.err.format("Extracted %d features.%n", nFeatures);

		System.err.format("Training ...%n");

		Parser parser = new Parser(model);
		
		Trainer trainerHandler = new PerceptronTrainer(model, parser);

		long trainingStarted = System.currentTimeMillis();

		for (int i = 0; i < options.nIterations; i++) {
			System.err.format("Iteration %d of %d.%n", i + 1, options.nIterations);

			ProgressPrinter progressPrinter = new ProgressPrinter();

			try {
				CoNLLReader reader = new CoNLLReader(options.inputFileName);
				CoNLLTree tree;
				while ((tree = reader.read()) != null) {
					trainerHandler.update(tree);
					progressPrinter.update();
				}
				reader.close();
			} catch (FileNotFoundException e) {
				failWithFileNotFoundException(options.inputFileName);
			} catch (IOException e) {
				failWithIOException(options.inputFileName);
			}

			progressPrinter.exit();

			if (options.saveIntermediateModels) {
				System.err.print("Saving the intermediate model ...");
				String intermediateFile = String.format("%s.%02d", options.modelFileName, i + 1);
				double[] averagedWeightVector = trainerHandler.getAveragedWeightVector();
				Model intermediateModel = new Model(model);
				intermediateModel.setWeightVector(averagedWeightVector);
				try {
					intermediateModel.save(intermediateFile);
				} catch (IOException e) {
					System.err.println();
					failWithIOException(intermediateFile);
				}
				System.err.format(" %s%n", intermediateFile);
			}
		}
		trainerHandler.averageWeightVector();
		System.err.println("Finished training.");

		System.err.format("Training took %s.%n", formatTimeDifference(trainingStarted, System.currentTimeMillis()));

		System.err.print("Saving the final model ...");
		try {
			model.save(options.modelFileName);
		} catch (IOException e) {
			System.err.println();
			failWithIOException(options.modelFileName);
		}
		System.err.format(" %s%n", options.modelFileName);
	}

	public static class TrainOptions {

		@Option(name = "-i", argument = "FILE", usage = "Read input data from FILE", required = true)
		public String inputFileName;
		@Option(name = "-m", argument = "FILE", usage = "Store the model in FILE", required = true)
		public String modelFileName;
		@Option(name = "-n", argument = "ITERS", usage = "Train using ITERS iterations")
		public int nIterations = 1;
		@Option(name = "-s", usage = "Save intermediate models")
		public boolean saveIntermediateModels = false;
	}

	public static void parse(String[] args) {
		ParseOptions options = new ParseOptions();
		OptionParser<ParseOptions> optionParser = new OptionParser<ParseOptions>(options);
		try {
			optionParser.parse(args);
		} catch (OptionException e) {
			System.err.println(e.getMessage());
			optionParser.usage();
			System.exit(1);
		}

		System.err.print("Loading the model ...");
		Model model = null;
		try {
			model = Model.load(options.modelFileName);
		} catch (FileNotFoundException e) {
			System.err.println();
			failWithFileNotFoundException(options.modelFileName);
		} catch (IOException e) {
			System.err.println();
			failWithIOException(options.modelFileName);
		}
		System.err.println(" done.");

		System.err.println("Parsing ...");

		long parsingStarted = System.currentTimeMillis();

		Parser parser = new Parser(model);
		ProgressPrinter progressPrinter = new ProgressPrinter();

		CoNLLReader reader = null;
		try {
			reader = new CoNLLReader(options.inputFileName);
		} catch (FileNotFoundException e) {
			failWithFileNotFoundException(options.inputFileName);
		}
		CoNLLWriter writer = null;
		try {
			writer = new CoNLLWriter(options.outputFileName);
		} catch (IOException e) {
			failWithIOException(options.outputFileName);
		}
		try {
			CoNLLTree tree;
			while ((tree = reader.read()) != null) {
				writer.write(parser.getBestParse(tree));
				progressPrinter.update();
			}
		} catch (IOException e) {
			System.err.println();
			failWithIOException(options.inputFileName);
		}

		progressPrinter.exit();

		try {
			reader.close();
		} catch (IOException e) {
			failWithIOException(options.inputFileName);
		}
		try {
			writer.close();
		} catch (IOException e) {
			failWithIOException(options.outputFileName);
		}

		System.err.println("Finished parsing.");

		System.err.format("Parsing took %s.%n", formatTimeDifference(parsingStarted, System.currentTimeMillis()));
	}

	public static class ParseOptions {

		@Option(name = "-m", argument = "FILE", usage = "Read the parser model from FILE", required = true)
		public String modelFileName;
		@Option(name = "-i", argument = "FILE", usage = "Read input data from FILE", required = true)
		public String inputFileName;
		@Option(name = "-o", argument = "FILE", usage = "Write output data to FILE", required = true)
		public String outputFileName;
	}

	private static String formatTimeDifference(long startTime, long endTime) {
		long tmp = endTime - startTime;
		long hours = TimeUnit.MILLISECONDS.toHours(tmp);
		tmp -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(tmp);
		tmp -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(tmp);
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	}

	private static void failWithFileNotFoundException(String fileName) {
		System.err.println();
		System.err.format("File not found: %s%n", fileName);
		System.exit(1);
	}

	private static void failWithIOException(String fileName) {
		System.err.println();
		System.err.format("I/O error while processing %s%n", fileName);
		System.exit(1);
	}
}
