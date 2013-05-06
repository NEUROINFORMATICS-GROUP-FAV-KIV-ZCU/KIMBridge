KIMBridge
=========

KIMBridge is a service that is able to download documents from remote repositories (Google Drive, LinkedIn discussions)
and store and annotate them in the KIM Platform.


Compiling
---------

The service can be compiled using the Apache Ant:

    ant -f kimbridge.xml


Running
-------

Service can run either from directly from command-line or as an service using the `jsvc` utility.

Service initscript is located in the init.d directory.


License
-------

This software is relased under the MIT License. For details, see LICENSE.md.

For licenses for the third-party libraries, please see the license files under the lib/ folder.
