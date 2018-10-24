import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class ProxySample extends WebSocketSample {

    private static final String PROXY_HOSTNAME_ENV_VAR = "PROXY_HOSTNAME";
    private static final String PROXY_PORT_ENV_VAR = "PROXY_PORT";

    private String proxyHostname = null;
    private int proxyPort = 0;

    public static void main(String[] args)
    {
        WebSocketSample app = new ProxySample();
        try {
            app.setup(args);
            app.run();
        } catch (Exception e) {
            System.out.printf("%s", e.toString());
        }
        System.exit(0);
    }

    @Override
    protected void setOptions(Options options)
    {
        super.setOptions(options);
        options.addOption(new Option("h", true, "Proxy hostname"));
        options.addOption(new Option( "p", true, "Proxy port"));
    }

    @Override
    protected void getOptions(CommandLine cl)
    {
        super.getOptions(cl);
        proxyHostname = cl.getOptionValue("h");
        proxyPort = cl.getOptionValue("p") == null ? 0 : Integer.valueOf(cl.getOptionValue("p"));
    }

    @Override
    protected void getEnvVars()
    {
        super.getEnvVars();

        String env = System.getenv(PROXY_HOSTNAME_ENV_VAR);
        if (env != null) { proxyHostname = env; }

        env = System.getenv(PROXY_PORT_ENV_VAR);
        if (env != null) { proxyPort = Integer.valueOf(env); }
    }

    @Override
    protected void checkArguments()
    {
        if (connectionString == null || queueName == null || proxyHostname == null || proxyPort == 0)
        {
            System.out.println("Run sample with either command line options -c connectionString -q queueName " +
                    "-h proxyHostname -p proxyPort or with environment variables 'SERVICE_BUS_CONNECTION_STRING', " +
                    "'QUEUE_NAME', 'PROXY_HOSTNAME', and 'PROXY_PORT' set");
            System.exit(1);
        }
    }


    @Override
    protected void createSettings()
    {
        super.createSettings();

        /* CHANGES TO PROXY SETUP HERE */
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                List<Proxy> proxies = new LinkedList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostname, proxyPort)));
                return proxies;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // no-op
            }
        });
    }
}
