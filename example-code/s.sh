#!/bin/bash

# Requires Java 8 and Gradle.

if [ ! -d jpf-core ]; then
    echo "ERROR: please download jpf-core from the zip in the jpf-dockerize repo. this could also be found at /jpf/jpf-core"
fi
( cd jpf-core
  sed -i 's/public void setJavaObjectInputStreamReadString.*/public void setJavaObjectInputStreamReadString() {/g' src/classes/sun/misc/SharedSecrets.java
  gradle build -x test
)

# Run example
javac src/Demo.java src/Math.java  src/A.java src/MemDemo.java src/C.java
jpf-core/bin/jpf +classpath=./src MemDemo

# Compile listeners and use them
javac -classpath jpf-core/build/jpf.jar src/Listeners/CoverageListener.java src/Listeners/MemListener.java
if [ $? -ne 0 ]; then
        echo "ERROR: compilation of listeners failed"
        exit 1
fi

# commented out for testing
jpf-core/bin/jpf +native_classpath=./src/Listeners +classpath=./src +listener=CoverageListener MemDemo
jpf-core/bin/jpf +native_classpath=./src/Listeners +classpath=./src +listener=MemListener MemDemo