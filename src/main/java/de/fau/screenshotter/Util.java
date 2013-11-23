package de.fau.screenshotter;

import java.awt.*;
import java.net.URL;

public class Util {
    /**
     * @param pathAndFileName The resource name, relative to /src/main/resources/
     * @return An image from the specified path
     * @see <a href="http://stackoverflow.com/a/10465041/488265">http://stackoverflow.com/a/10465041/488265</a>
     */
    public static Image getImage(final String pathAndFileName) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
        if (null == url) {
            return null;
        }
        return Toolkit.getDefaultToolkit().getImage(url);
    }
}
