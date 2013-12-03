package de.fau.screenshotter;

public class SFTPUploader implements ScreenshotUploader {

    final static String sftp_user = "<user name>";
    final static String sftp_host = "<host adress>";
    final static String sftp_wwwfolder = "<put folder here>"; // name path to www-folder resp. htdocs
    final static String sftp_subpath = "/"; // if no subfolders for upload wished, put "/" here; you have to create this folder manually!
    final static String sftp_password = "<put password here>";

    @Override
    public String prefetchUrl(String filename) {
        return "http://" + sftp_user + "." + sftp_host + sftp_subpath + filename;
    }

    @Override
    public String upload(String filename) throws Exception {
        SftpClient sftpClient = new SftpClient(sftp_host, sftp_user, sftp_password);
        sftpClient.upload(filename, sftp_wwwfolder + sftp_subpath + filename);
        sftpClient.close();

        // Build http-link to save in clipboard after upload.
        String fileURL = "http://" + sftp_user + "." + sftp_host + sftp_subpath + filename;
        System.out.println("File-URL: " + fileURL);
        return fileURL;
    }
}
