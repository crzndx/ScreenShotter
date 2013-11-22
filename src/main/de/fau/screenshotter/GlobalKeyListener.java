package de.fau.screenshotter;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class GlobalKeyListener implements NativeKeyListener {

    private final Set<Integer> triggerKeys;
    private final Set<Integer> currentlyPressedKeys = new HashSet<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public GlobalKeyListener(Set<Integer> triggerKeys) {
        this.triggerKeys = triggerKeys;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        int key = e.getKeyCode();

        // Don't make a screenshot if the user presses ESC
        if (key == KeyEvent.VK_ESCAPE) {
            GlobalScreen.unregisterNativeHook();
            return;
        }

        currentlyPressedKeys.add(key);
        logger.trace("Key {} was pressed.", key);

        // The user can press more than the defined keys.
        if (currentlyPressedKeys.containsAll(triggerKeys)) {
            try {
                ScreenShotter.triggerActivated();
            } catch (Exception ignored) {
            }

            // Prevents multiple accidental screenshots in case the user
            // presses all triggerKeys and then another key without releasing
            // one or more of the triggerKeys.
            currentlyPressedKeys.clear();
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        int key = e.getKeyCode();
        currentlyPressedKeys.remove(key);
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}