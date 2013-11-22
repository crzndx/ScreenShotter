package de.fau.screenshotter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SFTPUploader implements ScreenshotUploader {

    final static String sftp_user = "<user name>";
    final static String sftp_host = "<host adress>";
    final static String sftp_wwwfolder = "<put folder here>"; // name path to www-folder resp. htdocs
    final static String sftp_subpath = "/"; // if no subfolders for upload wished, put "/" here; you have to create this folder manually!
    final static String sftp_password = "<put password here>";

    @Override
    public String upload(String filename) throws Exception {
        JSch js = new JSch();
        Session ses = js.getSession(sftp_user, sftp_host, 22);
        ses.setPassword(sftp_password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        ses.setConfig(config);
        ses.connect();

        ChannelSftp channel = (ChannelSftp) ses.openChannel("sftp");
        channel.connect();

        StopWatch watch = StopWatch.start();

        channel.put(filename, sftp_wwwfolder + sftp_subpath + filename);

        System.out.println(String.format("Successfully uploaded screenshot in %d ms", watch.time()));
        channel.disconnect();
        ses.disconnect();

        // Build http-link to save in clipboard after upload.

        String fileURL = "http://" + sftp_user + "." + sftp_host + sftp_subpath + filename;
        System.out.println("File-URL: " + fileURL);
        return fileURL;
    }
}
