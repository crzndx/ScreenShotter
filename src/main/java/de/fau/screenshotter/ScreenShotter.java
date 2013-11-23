package de.fau.screenshotter;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

// TODO http://stackoverflow.com/questions/5953525/run-java-application-at-windows-startup
// TODO add GUI
// TODO add other upload clients
// TODO Java Properties benutzen um die sftp daten zu uebergeben.

public class ScreenShotter {

    private static final Logger logger = LoggerFactory.getLogger(ScreenShotter.class);

	/* 			NECESSARY CONFIGURATIONS
     * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Configure SFTP account data for file upload
	 * Login must be right, does not working otherwise!
	 */

    /*
     * Play a sound when uploaded and copied to clipboard?
     */
    final static boolean playTune = true;

    /*
     * Google URL Shortener API Key must be declared here.
     * Create API Key here: https://code.google.com/apis/console
     * and get the key here: https://code.google.com/apis/console#access
     */
    final static String googUrl = "https://www.googleapis.com/urlshortener/v1/url?shortUrl=http://goo.gl/fbsS&key=AIzaSyDXmdjeMFOzIjz8n8AS0d031N6LQ86o2Ts";

    public static void main(String[] args) {
        // We want logging
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        // We want a tray icon to indicate we're running
        final PopupMenu trayPopup = new PopupMenu();
        final SystemTray systemTray = SystemTray.getSystemTray();
        final Image trayIconImage = Util.getImage("icons/tray-16.png");
        final TrayIcon trayIcon = new TrayIcon(trayIconImage, "Screenshotter", trayPopup);

        MenuItem menuExit = new MenuItem("Exit ScreenShotter");
        menuExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Don't listen for input anymore
                GlobalScreen.unregisterNativeHook();
                // If you use tray icons you have to remove them from the system bar - otherwise, the JVM won't quit.
                systemTray.remove(trayIcon);
                logger.info("ScreenShotter shut down via Tray.");
            }
        });
        trayPopup.add(menuExit);

        try {
            systemTray.add(trayIcon);
            logger.info("Tray icon added.");
        } catch (AWTException ignored) {
            logger.error("System tray is not supported.");
        }

        // Run the machinery
        Set<Integer> triggerKeys = new HashSet<>();
        triggerKeys.add(KeyEvent.VK_LEFT);
        triggerKeys.add(KeyEvent.VK_RIGHT);
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.getInstance().addNativeKeyListener(new GlobalKeyListener(triggerKeys));
        } catch (NativeHookException ex) {
            logger.error("There was a problem registering the native hook in the operating system.");
            systemTray.remove(trayIcon);
        }
    }

    /*
     * Trigger (keys got presed) got activated, perform action e.g.
     * make screenshot, afterwards upload.
     */
    public static void triggerActivated() throws Exception {
        StopWatch watch = StopWatch.start();

        linkToClipboard("Sorry, the program needs some time to upload. Try to paste link again in some seconds...");

        String filename = makeAndSaveScreenShot();

        ScreenshotUploader upl = new SFTPUploader();

        String httplink = upl.upload(filename);

        String shorted = shortenURL(httplink);

        linkToClipboard(shorted);

        if (playTune) playSound();

        System.out.println(String.format("Done in %d ms", watch.time()));
    }

    public static String makeAndSaveScreenShot() throws AWTException, IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");

        Calendar now = Calendar.getInstance();
        Robot robot = new Robot();
	        
	        /* put together filename*/
        String filename = "screenshot-" + formatter.format(now.getTime());
        filename = encrypt(filename) + ".png";

        try {
            BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
	        	/* add ./ to filename to specify save path */
            ImageIO.write(screenShot, "PNG", new File("./" + filename));
        } catch (Exception e) {
            e.getMessage();
            System.err.println("Problems occured while processing the screenshot");
        }

        System.out.println(formatter.format(now.getTime()));

        return filename;
    }

    public static String encrypt(String source) {
        String md5;
        try {
            MessageDigest mdEnc = MessageDigest.getInstance("MD5"); // Encryption algorithm
            mdEnc.update(source.getBytes(), 0, source.length());
            md5 = new BigInteger(1, mdEnc.digest()).toString(16); // Encrypted string
        } catch (Exception ex) {
            return null;
        }
        return md5;
    }

    /*
     * Copy the generated link into the clipboard
     */
    private static void linkToClipboard(String httplink) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(httplink), null
        );
        System.out.println("Link copied to clipboard.");
    }

    /*
     * Stolen from: http://www.glodde.com/blog/default.aspx?id=51&t=Java-Use-googl-URL-shorten-from-Java
     */
    private static String shortenURL(String longUrl) {
        String shortUrl = "";

        try {
            URLConnection conn = new URL(googUrl).openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter wr =
                    new OutputStreamWriter(conn.getOutputStream());
            wr.write("{\"longUrl\":\"" + longUrl + "\"}");
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                if (line.contains("id")) {
                    // I'm sure there's a more elegant way of parsing
                    // the JSON response, but this is quick/dirty =)
                    shortUrl = line.substring(8, line.length() - 2);
                    break;
                }
            }

            wr.close();
            rd.close();
        } catch (IOException ex) {
            ex.getMessage();
        }

        System.out.println("Short URL: " + shortUrl);

        return shortUrl;
    }

    /*
     * Play a sound, when link is copied to clipboard successfully
     */
    private static void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }
}
