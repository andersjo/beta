# Beta

Beta is a simple first-order dependency parser. It is a (partial) reimplementation of the excellent [MSTParser](http://sourceforge.net/projects/mstparser/), with a focus on simplicity, teachability, and speed.

## Start Using Beta

This section describes the basic usage of Beta.

Beta can process data in the [CoNLL-X format](http://ilk.uvt.nl/conll/#dataformat).

### Training a parsing model

To train a default parsing model type, type the following command:

```
$ java -Xmx1G -jar beta.jar train -i train.conll -m MODEL
```

This command tells Beta to create a parsing model from the data in ``train.conll`` and store the model in the file ``MODEL``. The option ``-i`` specifies the input file; the option ``-m`` specifies the model file.

Depending on the size of your data, you may want to increase the Java heap space using the ``-Xmx`` option.

### Parse data using your parsing model

To parse with your parsing model, type the following command:

```
$ java -Xmx1G -jar beta.jar -m MODEL -i in.conll -o out.conll
```

This command tells Beta to read a parsing model (``-m``) from ``MODEL``, use this model to parse input data (``-i``) from ``in.conll``, and write its output (``-o``) to ``out.conll``.
