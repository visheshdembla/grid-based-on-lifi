# Grid-Based-On-LiFi
Implementation of a Virtual Grid Enabled Via Li-Fi Communication based on RS-232


Implemented as a final year project.


# Architecture

The project is based upon a client - server architecture. The clients are part of the grid and server takes care of administration. The clients can choose to use resources or donate resources to the grid. Server provides all the corresponding functionalities. 

# Client as DONOR:

Client, when a donor, can share

 1. Storage Space
 
 2. Computational Power
 
 In case of donation, the server dynamically allocates some percentage of the donors' resources into the grid ( = 10 % by default), so that the client doesn't undergo performance or storage bottleneck when acting as a donor. The client user can get all the resources back from the grid resource pool at any given instance.
 
 
 # Client as RESOURCE CONSUMER:
 
 While acting as a resource consumer, client can:
 
 1. Store files on the grid, without having to worry about the location of the file
 
 2. Execute JAVA codes remotely on the grid.
 
 The system is limited to only one resource consumer at a time due to constraints of Li-Fi communication.
 
 
 # Server :
 
 The server accepts new client connections and new task requests. Server dynamically calculates the appropriate donor to use resource from when a task is to be performed. Server is the actual entity that contains all the logic corresponding to  resource and task co-ordination as well as client management.

# Li-Fi :

Li-Fi circuit is based upon RS - 232 Serial Communication Protocol. Hardware used for the project was FTDI FT 232 Serial Communication Chips.
The Li-Fi cirucit is capable of communicating at a maximum of 9600 baud/sec and uses On - Off Keying modulation technique to perform the communication.




