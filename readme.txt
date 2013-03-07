1. Building

This project is built using maven 3.0.x and requires Java SE 6 (or newer).

Before building the project for the first time, you will need to add the "timestools.jar" library to your local mvn repository. You can do this by running this script:

<projectdir>/lib/add2mvn.sh

To build the project run this maven command:

mvn package assembly:single [-DskipTests]

Use the optional -DskipTests argument to skip test failures, and prevent them from halting the build.

This will create a directory:

<projectdir>/target/hdpa-<version>-dist

This contains a fully packaged, runnable version of the project.

2. Running programs

To run any of the programs in the project,

    cd <projectdir>/target/hdpa-<version>-dist

and then run

    run.sh <main-class> <args>

To train an HDPA model, use this command:

    run.sh com.bronzespear.hdpa.TrainHdpa <corpus> <batch size>

where

    <corpus> is the path (full or relative>) to corpus data directory, and
    <batch size> is an optional argument to change the size of training "mini-batches" (default = 1000)

This program will train the model, and then save the model parameters to a file named:

    <corpus path>/<corpus dir>-model.csv

where <corpus dir> is name of the corpus data directory, and <corpus path> is its parent directory.   

To create a formatted list of topics and terms, use this command:

    run.sh com.bronzespear.hdpa.PrintTopics <corpus path>

It will assume that a corresponding results file already exists. The formatted
results will be saved to

    <corpus path>/<corpus dir>-topics.txt

To assign topic to documents using an already-trained model, use this command:

	run.sh com.bronzespear.hdpa.AssignTopics <corpus path>

It will assume that a corresponding results file already exists. The results (a table of
document ids and associated topic weights) will be saved to

    <corpus path>/<corpus dir>-doctopics.csv