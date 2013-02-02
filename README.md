# Beta

Beta is a simple first-order dependency parser. It is a (partial) reimplementation of the excellent [MSTParser](http://sourceforge.net/projects/mstparser/), with a focus on simplicity, teachability, and speed.

Beta was written by [Marco Kuhlmann](http://stp.lingfil.uu.se/~kuhlmann/).

Note that you should most probably *not* use Beta if you want a parser with optimal accuracy. There are many other systems out there that serve this purpose. I especially recommend [MaltParser](http://www.maltparser.org/), which also features a comprehensive documentation. The short user guide that follows was heavily inspired by that documentation.

## Start using Beta

This section describes the basic usage of Beta.

### Downloading and installing Beta

Binary releases of Beta are available via the [Downloads section](https://bitbucket.org/kuhlmann/beta/downloads) of the project site. Once you have downloaded a release, unpack it at a location of your choice:

```
$ tar xzf beta-VERSION.tar.gz
```

This will create a new directory ``beta-VERSION``. Enter that directory:

```
$ cd beta-VERSION
```

You should now be able to run Beta by typing the command

```
$ bin/beta
```

This command is actually a shell script that calls Java. Therefore, the command only works if your system both supports shell scripts, and has Java properly installed.

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
  Labeled   attachment score: 15385 / 20376 * 100 = 75.51 %
  Unlabeled attachment score: 17070 / 20376 * 100 = 83.78 %
  Label accuracy score:       16420 / 20376 * 100 = 80.59 %
```

## Optimization

At this time, Beta is very basic. It only supports the standard first-order feature model of MSTParser and supports neither morphological features, non-projective parsing, nor any other of the enhancements that has shown to increase parsing accuracy. (In case you want to implement any of these, just go ahead!)

The one thing you can try in order to improve parser accuracy at this point is to tweak the number of iterations over the training data. By default, the parser does 1 iteration; but larger values may increase the parsing accuracy – typically up to some local maximum, after which accuracy will decrease again.

The number of iterations over the training data is specified using the ``-n`` flag. For example, the following command will train the parser using 9 iterations:

```
$ bin/beta train -i data/talbanken-dep-train.conll -m MODEL -n 9
```

(For the sample data, this happens to improve labeled accuracy from 75.51% to 79.03%.)

When trying to find the best value for ``-n``, you may find it convenient to train with a large number of iterations (say 10) and save the intermediate models after each iteration so that afterwards you can pick the best one. To do this, you can specify the ``-s`` option. For example, the following command will train using 10 iterations, and will save the intermediate models as ``MODEL.01``, ``MODEL.02``, and so on:

```
$ bin/beta train -i data/talbanken-dep-train.conll -m MODEL -n 10 -s
```

## License

Copyright (c) 2013 Marco Kuhlmann

Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.