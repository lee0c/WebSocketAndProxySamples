// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.TransportType;
import com.microsoft.azure.servicebus.primitives.Util;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.google.gson.Gson;

import static java.nio.charset.StandardCharsets.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.cli.*;

public class WebSocketSample {

    private static final String CONNECTION_STRING_ENV_VAR = "SERVICE_BUS_CONNECTION_STRING";
    private static final String QUEUE_NAME_ENV_VAR = "QUEUE_NAME";

    String connectionString = null;
    String queueName = null;

    private static final Gson GSON = new Gson();

    private ConnectionStringBuilder connectionStringBuilder;
    ClientSettings clientSettings;

    public static void main(String[] args)
    {
        WebSocketSample app = new WebSocketSample();
        try {
            app.setup(args);
            app.run();
            System.exit(0);
        } catch (Exception e) {
            System.out.printf("%s", e.toString());
        }
    }

    protected void setup(String[] args) throws ParseException {
        this.readArguments(args);
        this.checkArguments();
        this.createSettings();
    }

    private void readArguments(String[] args) throws ParseException {
        // set up to get command line args
        Options options = new Options();
        this.setOptions(options);

        CommandLineParser clp = new DefaultParser();
        CommandLine cl = clp.parse(options, args);

        // get command line args
        this.getOptions(cl);

        // then check for environment variables
        this.getEnvVars();
    }

    protected void setOptions(Options options)
    {
        options.addOption(new Option("c", true, "Connection string"));
        options.addOption(new Option( "q", true, "Queue name"));
    }

    protected void getOptions(CommandLine cl)
    {
        connectionString = cl.getOptionValue("c");
        queueName = cl.getOptionValue("q");
    }

    protected void getEnvVars()
    {
        String env = System.getenv(CONNECTION_STRING_ENV_VAR);
        if (env != null) { connectionString = env; }

        env = System.getenv(QUEUE_NAME_ENV_VAR);
        if (env != null) { queueName = env; }
    }

    protected void checkArguments()
    {
        if (connectionString == null || queueName == null)
        {
            System.out.println("Run sample with either command line options -c connectionString -q queueName or with " +
                    "environment variables 'SERVICE_BUS_CONNECTION_STRING' and 'QUEUE_NAME' set");
            System.exit(1);
        }
    }

    protected void createSettings()
    {
        connectionStringBuilder = new ConnectionStringBuilder(connectionString);
        if (connectionStringBuilder.getTransportType() == TransportType.AMQP)
            { connectionStringBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS); }
        clientSettings = Util.getClientSettingsFromConnectionStringBuilder(connectionStringBuilder);
    }

    protected void run() throws Exception
    {
        // Create a QueueClient instance for receiving using the connection string builder
        // We set the receive mode to "PeekLock", meaning the message is delivered
        // under a lock and must be acknowledged ("completed") to be removed from the queue
        QueueClient receiveClient = new QueueClient(
                connectionStringBuilder.getEndpoint(),
                queueName,
                clientSettings,
                ReceiveMode.PEEKLOCK);
        this.registerReceiver(receiveClient);

        // Create a QueueClient instance for sending and then asynchronously send messages.
        // Close the sender once the send operation is complete.
        QueueClient sendClient = new QueueClient(
                connectionStringBuilder.getEndpoint(),
                queueName,
                clientSettings,
                ReceiveMode.PEEKLOCK);
        this.sendMessagesAsync(sendClient).thenRunAsync(sendClient::closeAsync);

        // wait for ENTER or 10 seconds elapsing
        waitForEnter(10);

        // shut down receiver to close the receive loop
        receiveClient.close();
    }

    private void registerReceiver(QueueClient queueClient) throws Exception {

        // register the RegisterMessageHandler callback
        queueClient.registerMessageHandler(new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                // receives message is passed to callback
                if (message.getLabel() != null &&
                    message.getContentType() != null &&
                    message.getLabel().contentEquals("Scientist") &&
                    message.getContentType().contentEquals("application/json")) {

                byte[] body = message.getBody();
                Map scientist = GSON.fromJson(new String(body, UTF_8), Map.class);

                System.out.printf(
                    "\n\t\t\t\tMessage received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                           "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ firstName = %s, name = %s ]\n",
                    message.getMessageId(),
                    message.getSequenceNumber(),
                    message.getEnqueuedTimeUtc(),
                    message.getExpiresAtUtc(),
                    message.getContentType(),
                    scientist != null ? scientist.get("firstName") : "",
                    scientist != null ? scientist.get("name") : "");
                }
                return CompletableFuture.completedFuture(null);
            }

            // callback invoked when the message handler has an exception to report
            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
               System.out.printf(exceptionPhase + "-" + throwable.getMessage());
            }
        },
        // 1 concurrent call, messages are auto-completed, auto-renew duration
        new MessageHandlerOptions(1, true, Duration.ofMinutes(1)));
    }

    private CompletableFuture<Void> sendMessagesAsync(QueueClient sendClient) {
        List<HashMap<String, String>> data =
                GSON.fromJson(
                        "[" +
                                "{'name' = 'Einstein', 'firstName' = 'Albert'}," +
                                "{'name' = 'Heisenberg', 'firstName' = 'Werner'}," +
                                "{'name' = 'Curie', 'firstName' = 'Marie'}," +
                                "{'name' = 'Hawking', 'firstName' = 'Steven'}," +
                                "{'name' = 'Newton', 'firstName' = 'Isaac'}," +
                                "{'name' = 'Bohr', 'firstName' = 'Niels'}," +
                                "{'name' = 'Faraday', 'firstName' = 'Michael'}," +
                                "{'name' = 'Galilei', 'firstName' = 'Galileo'}," +
                                "{'name' = 'Kepler', 'firstName' = 'Johannes'}," +
                                "{'name' = 'Kopernikus', 'firstName' = 'Nikolaus'}" +
                                "]",
                        new TypeToken<List<HashMap<String, String>>>() {}.getType());

        List<CompletableFuture> tasks = new ArrayList<>();
        for (int i = 0; i < data.size(); i++)
        {
            final String messageId = Integer.toString(i);
            Message message = new Message(GSON.toJson(data.get(i), Map.class).getBytes(UTF_8));
            message.setContentType("application/json");
            message.setLabel("Scientist");
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(2));
            System.out.printf("\nMessage sending: Id = %s", message.getMessageId());
            tasks.add(
                sendClient.sendAsync(message).thenRunAsync(() -> System.out.printf("\n\tMessage acknowledged: Id = %s", message.getMessageId()))
            );
        }
        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[tasks.size()]));
    }

    private void waitForEnter(int seconds) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            executor.invokeAny(Arrays.asList(() -> {
                System.in.read();
                return 0;
            }, () -> {
                Thread.sleep(seconds * 1000);
                return 0;
            }));
        } catch (Exception e) {
            // absorb
        }
    }
}