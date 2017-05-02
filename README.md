# stable-matching
Algorithms to find stable matchings securely

To run algorithm 3 (ObliviousGaleShapley), add the file to the src/main/java/dk/alexandra/fresco/demo directory in FRESCO. Modify the mainClass tag on line 158 of pom.xml to be

dk.alexandra.fresco.demo.ObliviousGaleShapley

From the root directory of FRESCO, run

mvn clean compile assembly:single

This creates a .jar file in directory 'target' containing all FRESCO dependencies and using the specified class as the entry point. Feel free to rename this jar.

To run the class, run

java -jar path/to/jar/nameofjar.jar -i1 -s dummy -p1:localhost:9001