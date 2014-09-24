Post Install Instructions:
---------------------------

*** Import Maltego Plugin Certificate ***
Run Server-SSL-Utility shell script on each server you wish to connect Maltego to.
Import the *.cer certificate.

*** Import client.jar, stager,jar, and maltegoext.jar into Pwnbrew server ***
Drag a new Pwnbrew Server entity into Maltego graph
Set the correct IP address and port for the Pwnbrew Server you wish to configure 
Execute the "Configure" transform.
Import the JARs in to the library