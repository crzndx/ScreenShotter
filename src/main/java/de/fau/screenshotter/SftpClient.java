package de.fau.screenshotter;

import com.jcraft.jsch.*;

public class SftpClient extends JSch {

    private final String host;
    private final String username;
    private final String password;

    private Session sftpSession;
    private ChannelSftp sftpChannel;

    public SftpClient(String host, String username, String password) {
        super();
        this.host = host;
        this.username = username;
        this.password = password;
    }

    protected Session getUnstrictedSftpSession() throws SftpException {
        try {
            Session session = super.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            return session;
        } catch (JSchException e) {
            throw new SftpException(0, "Username or host are invalid.");
        }
    }

    protected ChannelSftp getOpenChannelFrom(Session session) throws SftpException {
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return channel;
        } catch (JSchException e) {
            throw new SftpException(0, "SFTP channel could not be established.");
        }
    }

    public boolean upload(String source, String destination) {
        try {
            sftpSession = getUnstrictedSftpSession();
            sftpChannel = getOpenChannelFrom(sftpSession);
            sftpChannel.put(source, destination);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    public void close() {
        sftpSession.disconnect();
        sftpChannel.disconnect();
    }
}
