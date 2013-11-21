package de.fau.screenshotter;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {

    private int trigger1;
    private int trigger2;
    private boolean trigger1status = false;
    private boolean trigger2status = false;

    GlobalKeyListener(int trigger1, int trigger2) {
        this.trigger1 = trigger1;
        this.trigger2 = trigger2;
    }

    void setTrigger1(int code) {
        this.trigger1 = code;
    }

    void setTrigger2(int code) {
        this.trigger2 = code;
    }


    public void nativeKeyPressed(NativeKeyEvent e) {
        //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

        if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
            GlobalScreen.unregisterNativeHook();
        }

        if (e.getKeyCode() == trigger1) {
            trigger1status = true;
            System.out.println("links");

        }

        if (e.getKeyCode() == trigger2) {
            trigger2status = true;
            System.out.println("rechts");

        }

        if (trigger1status && trigger2status) {
            System.out.println("BEIDE");

                	/* TRIGGER AUSLOESEN WENN LINKE UND RECHTE PFEILTASTE GLEICHZEITIG AKTIV*/

            try {
                ScreenShotter.triggerActivated();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            trigger1status = false;
            trigger2status = false;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

        if (e.getKeyCode() == 37) {
            trigger1status = false;
        }
        if (e.getKeyCode() == 39) {
            trigger2status = false;
        }

    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // do nothing
    }
}