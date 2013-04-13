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

For detailed information on all the programs available, arguments, etc., 
refer to the documentation in the appendices to thesis.pdf.