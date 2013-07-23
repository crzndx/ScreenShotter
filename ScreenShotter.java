import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;


public class ScreenShotter {

    // TODO http://stackoverflow.com/questions/5953525/run-java-application-at-windows-startup
    // TODO add GUI
    // TODO add other upload clients

	/* 			NECESSARY CONFIGURATIONS
	 * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Configure SFTP account data for file upload
	 * Login must be right, does not working otherwise!
	 */

    // TODO Java Properties benutzen um die sftp daten zu uebergeben.

	/*
	 * Play a sound when uploaded and copied to clipboard?
	 */
    final static boolean playTune = true;
	
	/*
	 * Trigger keys for capturing a screenshot are defined here.
	 */
    final static int triggerKey1 = KeyEvent.VK_LEFT;
    final static int triggerKey2 = KeyEvent.VK_RIGHT;

    public static void main(String[] args) {
        startKeyListener(triggerKey1, triggerKey2);
    }


    /*
     * Starts JNativeListener to listen to
     * background keypresses on a system-wide level
     * calls triggerActivated() if passed keys key1 and key2
     * are pressed simultaneously.
     */
    public static void startKeyListener(int key1, int key2) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

	    /* Create KeyListener via JNativeHook */
        GlobalScreen.getInstance().addNativeKeyListener(new GlobalKeyListener(key1, key2));
    }


    /*
     * Trigger (keys got presed) got activated, perform action e.g.
     * make screenshot, afterwards upload.
     */
    public static void triggerActivated() throws Exception {
        // Measure how long the process takes
        StopWatch watch = StopWatch.start();

        // We have to get the path of the screenshot on the local drive
        String screenshotFile = makeAndSaveScreenShot();

        // The file is then uploaded
        Uploader uploader = new SFTPUploader("sftp.properties");
        String longURL = uploader.uploadScreenshot(screenshotFile);

        // A short URL is much better to send to somebody
        URLShortener shortener = new GoogleShortener();
        String shorted = shortener.shorten(longURL);

        // You can now paste the short URL
        copyToClipboard(shorted);
        if (playTune) {
            playSound();
        }

        // How long did the program run?
        long elapsedTime = watch.time();
        System.out.println("Total time: " + elapsedTime + "ms");
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

    private static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(text), null
        );
        System.out.println("Link '" + text + "' copied to clipboard.");
    }

    private static void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }
}