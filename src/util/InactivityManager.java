/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;


public final class InactivityManager {
    
    public static long INACTIVITY_MS = 10 * 60 * 1000L;
    
    public static long WARNING_COUNTDOWN_MS = 30 * 1000L;

    private static volatile boolean initialised = false;
    private static volatile long lastActivityAt = System.currentTimeMillis();
    private static Timer idleCheckTimer;
    private static volatile Runnable currentLogout; 
    private static volatile Window currentWindow;   
    private static final AtomicBoolean warningOpen = new AtomicBoolean(false);

    private InactivityManager() {}

    
    public static void attach(Window window, Runnable onLogout) {
        currentWindow = window;
        currentLogout = onLogout;
        installActivityHooks(window);
        initGlobalIdleChecker();
    }

    
    private static void markActivity() {
        lastActivityAt = System.currentTimeMillis();
        
    }

    private static void installActivityHooks(Window window) {
       
        window.addMouseListener(simpleMouse());
        window.addMouseMotionListener(simpleMouseMotion());
        window.addKeyListener(simpleKey());

        
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
                
                if (warningOpen.get()) return;
                if (e instanceof MouseEvent || e instanceof KeyEvent) {
                    markActivity();
                }
            }, AWTEvent.MOUSE_EVENT_MASK
             | AWTEvent.MOUSE_MOTION_EVENT_MASK
             | AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ignored) {}
    }

    private static MouseAdapter simpleMouse() {
        return new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { markActivity(); }
            @Override public void mouseMoved(MouseEvent e)   { markActivity(); }
            @Override public void mouseDragged(MouseEvent e) { markActivity(); }
        };
    }

    private static MouseMotionAdapter simpleMouseMotion() {
        return new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e)   { markActivity(); }
            @Override public void mouseDragged(MouseEvent e) { markActivity(); }
        };
    }

    private static KeyAdapter simpleKey() {
        return new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { markActivity(); }
            @Override public void keyTyped(KeyEvent e)   { markActivity(); }
        };
    }

    private static void initGlobalIdleChecker() {
        if (initialised) return;
        initialised = true;

      
        idleCheckTimer = new Timer(1000, e -> {
            if (warningOpen.get()) return;
            long idleFor = System.currentTimeMillis() - lastActivityAt;
            if (idleFor >= INACTIVITY_MS && currentWindow != null && currentWindow.isShowing()) {
                showWarningDialog();
            }
        });
        idleCheckTimer.setRepeats(true);
        idleCheckTimer.start();
    }

  private static void showWarningDialog() {
    warningOpen.set(true);

    final JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(currentWindow),
            "Inactive Session",
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    dialog.setSize(420, 180);
    dialog.setLocationRelativeTo(currentWindow);

    JPanel panel = new JPanel(new BorderLayout(10,10));
    panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

   
    JLabel msg = new JLabel("You will be logged out due to inactivity in 30 seconds.");
    msg.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(msg, BorderLayout.CENTER);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    JButton stayBtn = new JButton("I'm here");
    JButton logoutBtn = new JButton("Log out");
    buttons.add(stayBtn);
    buttons.add(logoutBtn);
    panel.add(buttons, BorderLayout.SOUTH);

    dialog.setContentPane(panel);

    
    final long[] remaining = { WARNING_COUNTDOWN_MS };
    Timer countdown = new Timer(1000, ev -> {
        remaining[0] -= 1000;
        long secs = Math.max(0, remaining[0] / 1000);
        msg.setText("You will be logged out due to inactivity in " + secs + " seconds.");
        if (remaining[0] <= 0) {
            ((Timer) ev.getSource()).stop();
            safeLogout(dialog);
        }
    });

    stayBtn.addActionListener(ev -> {
        countdown.stop();
        markActivity();
        warningOpen.set(false);
        dialog.dispose();
    });

    logoutBtn.addActionListener(ev -> {
        countdown.stop();
        safeLogout(dialog);
    });

    dialog.addWindowListener(new WindowAdapter() {
        @Override public void windowClosing(WindowEvent e) {
            countdown.stop();
            markActivity();
            warningOpen.set(false);
        }
    });

    countdown.start();
    dialog.setVisible(true);
}


    private static void safeLogout(JDialog dialog) {
        warningOpen.set(false);
        if (dialog != null) dialog.dispose();

      
        SwingUtilities.invokeLater(() -> {
            try {
                if (currentLogout != null) currentLogout.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                
                System.exit(0);
            } finally {
                
                lastActivityAt = System.currentTimeMillis();
            }
        });
    }
}

