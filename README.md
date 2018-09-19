## WebSocketAndProxySamples

Sample Java apps to test Service Bus Java SDK with WebSocket and Proxy support. 
Based somewhat on the code found 
[here](https://github.com/Azure/azure-service-bus/tree/master/samples/Java/azure-servicebus/QueuesGettingStarted).

### How to run

This can be run with either an environment variable or a command line 
option. The values needed depend on which file you are running.

| Value | WebSocket? | Proxy? | Env Var Name | CL Option |
| ----- | ---------- | ------ | ------------ | --------- |
| Connection string | Yes | Yes | `SERVICE_BUS_CONNECTION_STRING` | -c |
| Queue name | Yes | Yes | `QUEUE_NAME` | -q |
| Proxy hostname | No | Yes | `PROXY_HOSTNAME` | -h |
| Proxy port | No | Yes | `PROXY_PORT` | -p |