# Installation via docker
Install the maven java project and run application with sample `leads.json` file.
*     git clone git@github.com:dustinbrown/dedupe.git
*     docker build -t dedupe .
*     docker run dedupe:latest

Install the maven java project and run application with custom file and debug logging.  The example shows mounting in a single file but a directory could be mounted in as well.
*     git clone git@github.com:dustinbrown/dedupe.git
*     docker build -t dedupe .
*     docker run -v ${PWD}/customOne.json:/usr/src/app/customOne.json -e FILE=customOne.json -e _JAVA_OPTIONS=-Dlogging.level.dedupe=DEBUG dedupe:latest
