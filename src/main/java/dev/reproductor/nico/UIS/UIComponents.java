package dev.reproductor.nico.UIS;

import javax.swing.*;
import java.awt.*;

public class UIComponents {
    public static JButton boton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.decode("#2e2e2e"));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return b;
    }

    public static JButton botonIcon(String ruta) {
        java.net.URL url = UIComponents.class.getResource(ruta);
        JButton b = (url == null) ? new JButton("?") : new JButton(new ImageIcon(url));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        return b;
    }

    public static JSlider slider(int min, int max, int val) {
        JSlider s = new JSlider(min, max, val);
        s.setBackground(Color.decode("#2e2e2e"));
        s.setForeground(Color.WHITE);
        return s;
    }
}
