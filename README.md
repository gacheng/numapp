
##Architecture

###Resilience of Data Loss
The application is resilient to data loss. Beside using the Java's BlockingQueue as messaging mechanism to persist the data from client. A second backup server is designed and is running as a separate process on different port. This backup server could be run in a separate machine to further reduce the single-point failure. The backup server does not do any particular processing, just record all the user inputs into log file "raw.log", which contains the 9-digit number and its received timestamp

###Number Uniqueness
Since using the database is not possible to detect uniqueness of the received numbers. The uniqueness detection is achived by writing an empty file to a directory with the received number as its file name. We can detect the duplication if a file with the same number exists in the directory when we create a new file. This directory (with name to be "all") thus contains all the numbers received so far.   

###Performance and Scalability
Since file manipulation is slow, all the received numbers from the 5 clients are first put into the BlockQueue. These 5 clients act as Producers by adding the numbers to the queue. The processing is done in the ListeningThread.java.  We have created a PersistThread.java, which acts as Consumers by taking out the numbers from the BlockingQueue and created the empty file in the directory "all". The same file is also created in the directory "processing".  Another thread which runs "MergeToFileProcessor" will read this "processing" directory and extracts all the file names and append the file names to the final log file "collect.log". Once it is merged to the file "collect.log", the empty file will be deleted from the "processing" directory.
  
###Configuration
Application can be configured through app.properties for the following parameter
1. output directory
2. output filename
3. max client numbers allowed
4. server processing thread number 
5. listening port for processing server
6. listening port for backup server
7. backup output filename

###File Structure
src: main source code directory
#####Server:  contains the files related to server
- **ServerStarter.java**:    a wrapper file to start the application server
- **TcpServer.java**:        an actual file contains all server's execution logics
- **ListeningThread.java**:  an object to process incoming client message
- **PersistThread.java**:    an object to process and persist the client message to disk file
- **MergeToFileProcessor.java**:  an object to extract file name value from disk and append to the output file
#####Client:  contains the files related to client
- **TcpClient.java**:          an object to represent client message sender
- **TcpClientAuto.java**:      an object to simulate a client, enter the numbers through random generation
- **TcpClientAuto.java**:      an object to simulate a client, enter the numbers manually through console 
 
##Misc. Design
When client enters a valid input and sends to server successfully, an "ACK" will be returned from Server
When invalid input entered from client, client code exits with status code 1
When socket is closed on the sever side, client code exits with status code 2
Client sends a PNG to the server at a certain interval to check if the Server is alive. If not, it will shutdown itself 

##How to Use This Application
- install gradle 6.6.1
- install java 8 at minimum
- check out source code 
- In command line, run "gradle clean"
- In command line, run "gradle run" to start the server side of the application
- In command line, run "gradle test --tests com.relic.numapp.TcpClientTest.testClientAuto" to start a client in auto mode, which will feeds the server 100 numbers
- In command line, run "gradle test --tests com.relic.numapp.TcpClientTest.testFunctionality" to have a general test
- Check the output/collect.log to get the run result
- To run tests, start the application server first

##Application is coded base on the following assumptions
1. Once the maximum of 5 clients has accessed the tcpServer, the new clients will be waiting for connecting to the server instead of being rejected
2. Once the number is sent from client, an acknowldege message will be sent from server to client
3. The output file "collect.log" is holding all the unique numbers. This file is not on rolling base, and currently there is no upper limit for the file size
4. When server is shutdown, all clients will be closed itself too automatically
5. The application is running on the single machine rather than in a distributed environment
6. No backend database server is available
7. No messaging or streaming server is available

##TODO
1. Make output file "collect.log" on rolling base when certain file size limit is reached
2. Fully test backupServer functionality 
2. Make application runnable in a distributed environment (multiple servers)
3. More Junit test to cover different cases

