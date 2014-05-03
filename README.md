# Beta

Beta is a simple first-order dependency parser. It is a (partial) reimplementation of the excellent [MSTParser](http://sourceforge.net/projects/mstparser/), with a focus on simplicity, teachability, and speed.

Beta was written by [Marco Kuhlmann](http://www.ida.liu.se/~marku61/).

Note that you should most probably *not* use Beta if you want a parser with optimal accuracy. There are many other systems out there that serve this purpose. I especially recommend [MaltParser](http://www.maltparser.org/), which also features a comprehensive documentation. The short user guide that follows was heavily inspired by that documentation.

## Start using Beta

This section describes the basic usage of Beta.

### Cloning Beta

To start using Beta, clone it at a location of your choice:

```
$ git clone https://github.com/liu-nlp/beta.git
```

This will create a new directory ``beta``. Enter that directory:

```
$ cd beta
```

Beta is built using [Gradle](http://www.gradle.org).

```
$ gradle jar
```

You should now be able to run Beta by typing the command

```
$ bin/beta
```

This command is actually a shell script that calls Java. Therefore, the command will only work if your system both supports shell scripts, and has Java properly installed.

### Training a parsing model

You are now ready to train your first parsing model. To do so, you need training data (dependency trees) in the [CoNLL-X format](http://ilk.uvt.nl/conll/#dataformat).

For testing purposes, the Beta distribution includes the dependency version of the Talbanken part of the [Swedish Treebank](http://stp.lingfil.uu.se/~nivre/swedish_treebank/). To train a default parsing model with the training section of that data, type the following command:

```
$ bin/beta train -i data/talbanken-dep-train.conll -m MODEL
```

This command tells Beta to create a parsing model from the input data (``-i``) in ``data/talbanken-dep-train.conll`` and store the model (``-m``) in the file ``MODEL``.

You should see output similar to the following:

```
Reading from data/talbanken-dep-train.conll ... done.
Found 4941 trees, 13913 word forms, 26 tags, and 60 edge labels.
Extracted 923387 features.
Training ...
Iteration 1 of 1.
.......... (1000)
.......... (2000)
.......... (3000)
.......... (4000)
.........
Finished training.
Training took 0:00:44.
Saving the final model ... MODEL
```

### Parsing with the trained model

You are now ready to apply the trained parser to new data. Even this data needs to be formatted according to the CoNLL-X format; however, the columns ``HEAD`` and ``DEPREL`` can be filled with dummy values. The task of the parser is to compute sensible values for these columns.

To apply your trained model to the testing section of the Talbanken data, type the following command:

```
$ bin/beta parse -m MODEL -i data/talbanken-dep-test.conll -o out.conll
```

This command tells Beta to read a parsing model (``-m``) from ``MODEL``, use this model to parse input data (``-i``) from ``data/talbanken-dep-test.conll``, and write its output (``-o``) to ``out.conll``.

You should see output similar to the following:

```
Loading the model ... done.
Parsing ...
.......... (1000)
..
Finished parsing.
Parsing took 0:00:10.
```

### Evaluating parser accuracy

You may now want to evaluate the accuracy of your trained parser. The Beta distribution includes the official evaluation script that was used in the [CoNLL 2007 Shared Task on Dependency Parsing](http://nextens.uvt.nl/depparse-wiki/SoftwarePage). The following command calls this script to compute the accuracy of the output of your system (``-s``) with respect to the gold-standard data (``-g``) data in ``data/talbanken-dep-test.conll``:

```
$ perl bin/eval07.pl -s out.conll -g data/talbanken-dep-test.conll -q
```

You should see the following output:

```
  Labeled   attachment score: 15238 / 20376 * 100 = 74.78 %
  Unlabeled attachment score: 16963 / 20376 * 100 = 83.25 %
  Label accuracy score:       16347 / 20376 * 100 = 80.23 %
```

## Optimization

At this time, Beta is very basic. It only supports the standard first-order feature model of MSTParser and supports neither morphological features, non-projective parsing, nor any other of the enhancements that has shown to increase parsing accuracy. (In case you want to implement any of these, just go ahead!)

The one thing you can try in order to improve parser accuracy at this point is to tweak the number of iterations over the training data. By default, the parser does 1 iteration; but larger values may increase the parsing accuracy – typically up to some local maximum, after which accuracy will decrease again.

The number of iterations over the training data is specified using the ``-n`` flag. For example, the following command will train the parser using 9 iterations:

```
$ bin/beta train -i data/talbanken-dep-train.conll -m MODEL -n 9
```

When trying to find the best value for ``-n``, you may find it convenient to train with a large number of iterations (say 10) and save the intermediate models after each iteration so that afterwards you can pick the best one. To do this, you can specify the ``-s`` option. For example, the following command will train using 10 iterations, and will save the intermediate models as ``MODEL.01``, ``MODEL.02``, and so on:

```
$ bin/beta train -i data/talbanken-dep-train.conll -m MODEL -n 10 -s
```

## License

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons Lizenzvertrag" style="border-width:0" src="http://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" href="http://purl.org/dc/dcmitype/Text" property="dct:title" rel="dct:type">Beta</span> von <a xmlns:cc="http://creativecommons.org/ns#" href="http://github.com/liu-nlp/beta" property="cc:attributionName" rel="cc:attributionURL">Marco Kuhlmann</a> ist lizenziert unter einer <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Namensnennung 4.0 International Lizenz</a>.
