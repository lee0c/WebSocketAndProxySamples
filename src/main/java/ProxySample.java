import org.apache.commons.cli.*;

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
            System.exit(0);
        } catch (Exception e) {
            System.out.printf("%s", e.toString());
        }
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

        this.clientSettings.setProxyHostName(proxyHostname);
        this.clientSettings.setProxyHostPort(proxyPort);
    }
}
