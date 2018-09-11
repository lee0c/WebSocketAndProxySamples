## WebsocketSample

A sample Java app to test Service Bus Java SDK with Websocket support. 
Based on the code found 
[here](https://github.com/Azure/azure-service-bus/tree/master/samples/Java/azure-servicebus/QueuesGettingStarted).

### How to run

This can be run with either an environment variable or a command line 
option.

* If using environment variables, set a variable 
`SERVICE_BUS_CONNECTION_STRING` to the connection string found on your
Azure portal.
* If using command line options, use `-c` followed by the connection string.

The app is currently set to use a queue titled "BasicQueue." If you want to
change that, edit the code on line 24 of `src/main/java/WebsocketSample.java`.