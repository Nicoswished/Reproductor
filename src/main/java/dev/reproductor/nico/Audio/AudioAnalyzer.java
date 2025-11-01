package dev.reproductor.nico.Audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class AudioAnalyzer {

    private AudioInputStream stream;
    private Clip clip;
    private byte[] buffer = new byte[4096];
    private float currentEnergy = 0f;

    public AudioAnalyzer(File audioFile) {
        try {
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat baseFormat = originalStream.getFormat();

            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            stream = AudioSystem.getAudioInputStream(decodedFormat, originalStream);

            // Inicializar Clip para reproducción
            clip = AudioSystem.getClip();
            clip.open(stream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // Inicia análisis de energía en un hilo aparte
        new Thread(this::analyzeLoop, "AudioAnalyzer-Thread").start();
    }

    private void analyzeLoop() {
        try {
            AudioInputStream analysisStream = AudioSystem.getAudioInputStream(clip.getFormat(), AudioSystem.getAudioInputStream(clip.getFormat(), AudioSystem.getAudioInputStream(clip.getFormat(), stream)));
            byte[] localBuffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = analysisStream.read(localBuffer, 0, localBuffer.length)) != -1) {
                currentEnergy = calculateEnergy(localBuffer, bytesRead);
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

    // ------------------------
    // Métodos para reproducir audio
    // ------------------------
    public void play() {
        if (clip != null) {
            clip.start();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    public Clip getClip() {
        return clip;
    }
}