
##Architecture

###Resilience of Data Loss
The application is resilient to data loss as long as the application is up and running by using the Java's BlockingQueue as messaging mechanism to persist the data from client. There is a slight chance when the program is killed and crashed while there are still elements left in the BlockingQueue and not written to the output file yet. To overcome the issue and to archive six-sigma level no-data-loss guarantee, a second backup server is designed and can be run as a separate process on different port (4001). This backup server could be run in a separate machine to further reduce the single-point failure. The backup server does not do any particular processing, just record all valid inputs into log file "backup.log".

###Number Uniqueness
Since using the database is not possible to detect uniqueness of the received numbers. The uniqueness detection is achieved by storing and comparing it in HashMap object in memory.  This brings a limitation. The application depends on the maximum memory the running machine can allocate to it.

###Performance and Scalability
Each client connected to the server will run in its own thread. All the received numbers from the 5 clients are first put into the BlockQueue. These 5 clients act as Producers by adding the numbers to the queue. The processing is done in the ListeningThread.java.  Acting as a Consumer, MergeToFileProcessor takes out the numbers from the BlockingQueue and writes it to the output file "number.log".  The application's performance can be enhanced by adjusting the maximum threads allowed through the configuration file 
  
###Configuration
Application can be configured through app.properties for the following parameter
1. output directory
2. output filename
3. max clients allowed
4. listening port for app server
5. backup enabler flag
6. hostname for the backup server
7. listening port for backup server
8. backup output filename

###File Structure

src: main source code directory
#####Server:  contains the files related to server
- **ServerStarter.java**:    a wrapper file to start the application server
- **TcpServer.java**:        an actual file contains all server's execution logics
- **ListeningThread.java**:  an object to process incoming client message
- **MergeToFileProcessor.java**:  an object to read from blockingueue and write to the output file
#####Client:  contains the files related to client
- **TcpClient.java**:        an object to represent client message sender
- **TcpClientAuto.java**:    an object to simulate a client, enter the numbers through random generation
- **TcpClientManual.java**:  an object to simulate a client, enter the numbers manually through console 
 
##Design Misc.
When client enters a valid input and sends to server successfully, an "ACK" will be returned from Server
When invalid input entered from client, the server will stop the socket
Server sends a PNG to the existing client at a certain interval to check if the client is alive. If not, it will close the socket
When socket is closed on the sever side, the server will stop the thread to leave a room for new threads/client
When "terminate" signal is received on server side, the server will wait until all elements in the BlockingQueue is processed and then terminate the server, including by sending the termination signal to the backup server

##How to Use This Application
- install gradle 6.6.1
- install java 8 at minimum
- check out source code 
- In command line, run "gradle clean build"
- In command line, run "gradle run" to start the server side of the application or
                   run "java -cp build\libs\NewRelic-1.0.jar com.relic.numapp.server.ServerStarter"
- In command line, run "gradle test --tests com.relic.numapp.TcpClientTest.testFunctionality" to have a sanity test
- In command line, run "gradle test --tests com.relic.numapp.TcpClientTest.testClientAuto" to start a client in auto mode, which will feeds the server 1000 numbers or
                   run "java -cp build\libs\NewRelic-1.0.jar com.relic.numapp.client.TcpClientAuto" or
                   run "java -cp build\libs\NewRelic-1.0.jar com.relic.numapp.client.TcpClientAuto 2" to feed 2 million numbers to the server
- Check the output/number.log to get the run result
- To run tests, start the application server first
- 

##Application is coded base on the following assumptions
1. Once the maximum of 5 clients has accessed the tcpServer, the new clients will be waiting for connecting to the server instead of being rejected
2. Once the number is sent from client, an acknowldege message will be sent from server to client
3. The output file "numbers.log" is holding all the unique numbers. This file is not on rolling base, and currently there is no upper limit for the file size
4. The application is running on the single machine rather than in a distributed environment
5. No backend database server is available
6. No messaging or streaming server is available

##TODO
1. Check if java nio and nio2 package can enhance the performance
2. Make output file "numbers.log" and "backup.log" on rolling base when certain file size limit is reached
3. Change and make application runnable in a distributed environment (multiple servers)
4. More Junit test to cover more cases

