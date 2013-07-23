import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class SFTPUploader implements Uploader {

    /**
     * The username to access your host.
     */
    private String username;

    /**
     * The password to access your host.
     */
    private String password;

    /**
     * The domain or ip of your host.
     */
    private String host;
    private int port;

    /**
     * The path to the www folder (i.e. 'htdocs' or 'html')
     */
    private String www;

    /**
     * If the files should be uploaded to the www folder, put "/" there.
     * This folder has to exist and you must have 'write' access.
     */
    private String subfolder;

    public SFTPUploader(String configFilename) {
        Properties config = loadConfigurationFromFile();
        this.username = config.getProperty("username");
        this.password = config.getProperty("password");
        this.host = config.getProperty("host");
        this.www = config.getProperty("www");
        this.subfolder = config.getProperty("subfolder");
        this.port = 22;
    }

    private Properties loadConfigurationFromFile() {
        Properties config = new Properties();
        String filename = "sftp.properties";
        try {
            config.load(new FileInputStream(filename));
        } catch (IOException ignored) {
            System.err.print("Could not read configuration from " + filename);
        }
        return config;
    }

    private Properties createConnectionConfig() {
        Properties config = new Properties();
        config.setProperty("StrictHostKeyChecking", "no");
        return config;
    }

    private Session createSFTPSession() {
        JSch js = new JSch();
        Session ses;
        try {
            ses = js.getSession(this.username, this.host, this.port);
            ses.setPassword(this.password);
            ses.setConfig(createConnectionConfig());
            ses.connect();
        } catch (JSchException e) {
            return null;
        }

        return ses;
    }

    public String uploadScreenshot(String filename) throws Exception {
        // Initialize the session
        Session sftpSession = createSFTPSession();

        // Establish a channel (link)
        ChannelSftp channel = (ChannelSftp) sftpSession.openChannel("sftp");
        channel.connect();

        // Measure upload duration
        StopWatch watch = StopWatch.start();

        // Upload the file
        channel.put(filename, this.www + this.subfolder + filename);

        // Cleaning up
        channel.disconnect();
        sftpSession.disconnect();

        System.out.println("Successfully uploaded.");

        // How long did the program run?
        long elapsedTime = watch.time();
        System.out.println("Total upload time: " + elapsedTime + "ms");

        // Create link
        String fileURL = String.format("http://%s.%s%s%s", this.username, this.host, this.subfolder, filename);
        System.out.println("File-URL: " + fileURL);
        return fileURL;
    }


}
