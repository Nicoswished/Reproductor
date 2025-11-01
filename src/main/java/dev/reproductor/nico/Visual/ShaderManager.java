package dev.reproductor.nico.Visual;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ShaderManager  extends JPanel {

    public enum EffectType { NONE, FOG, VIBRATION, PULSE, FLASH }

    private EffectType currentEffect = EffectType.NONE;
    private float opacity = 0f;
    private final Random random = new Random();

    public void setEffect(EffectType type) {
        currentEffect = type;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        applyEffect((Graphics2D) g);
    }

    private void applyEffect(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        switch (currentEffect) {
            case FOG -> {
                g2.setColor(new Color(200, 200, 255, 40));
                g2.fillRect(0, 0, w, h);
            }
            case VIBRATION -> {
                int shake = random.nextInt(6) - 3;
                g2.translate(shake, shake);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRect(0, 0, w, h);
                g2.translate(-shake, -shake);
            }
            case PULSE -> {
                opacity += 0.08f;
                if (opacity > 1) opacity = 0;
                g2.setColor(new Color(255, 100, 100, (int) (opacity * 100)));
                g2.fillRect(0, 0, w, h);
            }
            case FLASH -> {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fillRect(0, 0, w, h);
            }
        }
    }
}