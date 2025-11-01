package dev.reproductor.nico.Audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class AudioAnalyzer {
    private Clip clip;
    private AudioInputStream stream;
    private byte[] buffer = new byte[4096];
    private float currentEnergy = 0f;

    public AudioAnalyzer(File audioFile) {
        try {
            stream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        clip.start();
        new Thread(this::analyzeLoop).start();
    }

    private void analyzeLoop() {
        try {
            while (clip.isRunning()) {
                int bytesRead = stream.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    currentEnergy = calculateEnergy(buffer, bytesRead);
                }
                Thread.sleep(40);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float calculateEnergy(byte[] data, int length) {
        double sum = 0;
        for (int i = 0; i < length - 1; i += 2) {
            short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xff));
            sum += sample * sample;
        }
        return (float) Math.sqrt(sum / (length / 2));
    }

    public float getCurrentEnergy() {
        return currentEnergy;
    }

    public Clip getClip() {
        return clip;
    }
}