package dev.reproductor.nico.Manager;


import dev.reproductor.nico.Audio.AudioAnalyzer;
import dev.reproductor.nico.Main;
import dev.reproductor.nico.Visual.OpenGLVisualizer;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;

public class PlayerManager {

    private Clip clip;
    private FloatControl controlVolumen;
    private long duracionActual = 0;
    private long tiempoPausado = 0;
    private boolean reproduciendo = false;
    private boolean loop = false;
    private String modo = "Normal";

    public AudioAnalyzer analyzer;
    private OpenGLVisualizer visualizerThread;

    private final Main main;

    public PlayerManager(Main main) {
        this.main = main;
    }

    public void setModoReproduccion(String m) {
        modo = m;
    }

    public void setLoop(boolean l) {
        loop = l;
    }

    public void reproducirIndice(int i) {
        var pm = main.getPlaylistManager();
        if (pm.getCanciones().isEmpty()) return;
        pm.setIndiceActual(i);
        reproducirCancion(pm.getCanciones().get(i));
    }

    public void siguienteCancion() {
        main.getPlaylistManager().moverSiguiente(modo);
        reproducirIndice(main.getPlaylistManager().getIndiceActual());
    }

    public void anteriorCancion() {
        main.getPlaylistManager().moverAnterior(modo);
        reproducirIndice(main.getPlaylistManager().getIndiceActual());
    }

    public void reproducirCancion(File song) {
        try {
            detenerCancion();

            AudioInputStream ais = AudioSystem.getAudioInputStream(song);
            AudioFormat base = ais.getFormat();
            AudioFormat decoded = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(), 16, base.getChannels(),
                    base.getChannels() * 2, base.getSampleRate(), false
            );

            AudioInputStream dais = AudioSystem.getAudioInputStream(decoded, ais);
            clip = AudioSystem.getClip();
            clip.open(dais);

            duracionActual = clip.getMicrosecondLength();
            setVolumen(80);

            clip.start();
            reproduciendo = true;

            main.getNombreCan().setText(song.getName());

            analyzer = new AudioAnalyzer(song);
            analyzer.start();

            if (visualizerThread != null) {
                visualizerThread.stop();
            }
            visualizerThread = new OpenGLVisualizer(analyzer, main, main.fullscreenShader);
            visualizerThread.start();

            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && reproduciendo) {
                    if (loop) reproducirCancion(song);
                    else siguienteCancion();
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(main, "Error al reproducir canción: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void playPauseCancion() {
        if (clip == null) return;

        if (!reproduciendo) {
            clip.setMicrosecondPosition(tiempoPausado);
            clip.start();
            reproduciendo = true;
        } else {
            tiempoPausado = clip.getMicrosecondPosition();
            clip.stop();
            reproduciendo = false;
        }
    }

    public void detenerCancion() {
        try {
            if (clip != null) {
                reproduciendo = false;
                clip.stop();
                clip.close();
            }
            if (visualizerThread != null) {
                visualizerThread.stop();
                visualizerThread = null;
            }
        } catch (Exception ignored) {}
    }

    public void moverSliderTiempo(JSlider slider, boolean moving) {
        if (clip != null && duracionActual > 0) {
            double p = slider.getValue() / 1000.0;
            clip.setMicrosecondPosition((long) (duracionActual * p));
        }
    }

    public void actualizarTiempo(JSlider slider, JLabel label) {
        if (clip != null && reproduciendo && duracionActual > 0) {
            long actual = clip.getMicrosecondPosition();
            int val = (int) ((actual / (double) duracionActual) * 1000);
            slider.setValue(val);

            int seg = (int) (actual / 1_000_000);
            int min = seg / 60;
            seg %= 60;

            int totalSeg = (int) (duracionActual / 1_000_000);
            int totalMin = totalSeg / 60;
            totalSeg %= 60;

            label.setText(String.format("%02d:%02d / %02d:%02d", min, seg, totalMin, totalSeg));
        }
    }

    public void setVolumen(int v) {
        try {
            if (clip != null) {
                controlVolumen = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = controlVolumen.getMaximum() - controlVolumen.getMinimum();
                float gain = (range * v / 100f) + controlVolumen.getMinimum();
                controlVolumen.setValue(gain);
            }
        } catch (Exception ignored) {}
    }

    public void resetRandomListIfNeeded() {
        main.resetRandomListIfNeeded();
    }

    public File getCurrentSongFile() {
        return main.getCurrentSongFile();
    }

    public void restartVisualizer() {
        if (visualizerThread != null) {
            visualizerThread.stop();
            visualizerThread = new OpenGLVisualizer(analyzer, main, main.fullscreenShader);
            visualizerThread.start();
        }
    }
}