/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package amc;

import java.awt.*;

public class WindowPositioner {
   
    private static Point lastLocation = null;

   
    public static void apply(Frame frame) {
       
        if (lastLocation == null) {
            
            frame.setLocationRelativeTo(null);
        } else {
            frame.setLocation(lastLocation);
        }

       
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                lastLocation = frame.getLocationOnScreen();
            }
        });
    }
}
