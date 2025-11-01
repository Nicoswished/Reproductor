package dev.reproductor.nico.Manager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlayListManager {
    private final ArrayList<File> canciones = new ArrayList<>();
    private int indiceActual = -1;
    private final List<Integer> randomList = new ArrayList<>();
    private final Random random = new Random();

    public ArrayList<File> getCanciones() { return canciones; }
    public int getIndiceActual() { return indiceActual; }
    public void setIndiceActual(int i) { indiceActual = i; }

    public void agregarCancion(DefaultListModel<String> modeloLista) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de audio", "wav", "mp3", "ogg"));
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            for (File f : fc.getSelectedFiles()) {
                if (!modeloLista.contains(f.getName())) {
                    canciones.add(f);
                    modeloLista.addElement(f.getName());
                }
            }
            resetRandomList();
        }
    }

    public void eliminarCancion(JList<String> lista, DefaultListModel<String> modelo, PlayerManager player) {
        int i = lista.getSelectedIndex();
        if (i != -1) {
            if (i == indiceActual) player.detenerCancion();
            canciones.remove(i);
            modelo.remove(i);
            resetRandomList();
        }
    }

    public void guardarPlaylist() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("miPlaylist.nicoplaylist"));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(fc.getSelectedFile())) {
                for (File f : canciones) pw.println(f.getAbsolutePath());
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void cargarPlaylist(DefaultListModel<String> modeloLista) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                canciones.clear();
                modeloLista.clear();
                String line;
                while ((line = br.readLine()) != null) {
                    File f = new File(line);
                    if (f.exists()) {
                        canciones.add(f);
                        modeloLista.addElement(f.getName());
                    }
                }
                resetRandomList();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void moverSiguiente(String modo) {
        if (canciones.isEmpty()) return;

        switch (modo) {
            case "Normal" -> indiceActual = (indiceActual + 1) % canciones.size();
            case "Inversa" -> indiceActual = (indiceActual - 1 + canciones.size()) % canciones.size();
            case "Aleatoria" -> {
                if (randomList.isEmpty()) resetRandomList();
                indiceActual = randomList.remove(0);
            }
        }
    }

    public void moverAnterior(String modo) {
        if (canciones.isEmpty()) return;

        switch (modo) {
            case "Normal" -> indiceActual = (indiceActual - 1 + canciones.size()) % canciones.size();
            case "Inversa" -> indiceActual = (indiceActual + 1) % canciones.size();
            case "Aleatoria" -> {
                if (randomList.isEmpty()) resetRandomList();
                indiceActual = randomList.remove(0);
            }
        }
    }

    public void resetRandomList() {
        randomList.clear();
        for (int i = 0; i < canciones.size(); i++) {
            randomList.add(i);
        }
        Collections.shuffle(randomList, random);
    }
}
