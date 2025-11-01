package dev.game.nico;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Main extends Application {

    private final List<Track> tracks = new ArrayList<>();
    private final ListView<String> listView = new ListView<>();
    private MediaPlayer mediaPlayer;
    private Label lblNow = new Label("Nada reproduciendo");
    private Slider slider = new Slider();
    private Label timeLabel = new Label("00:00 / 00:00");
    private ToggleButton loopBtn = new ToggleButton("Loop");
    private Button playBtn = new Button("Play");
    private Button pauseBtn = new Button("Pause");
    private Button stopBtn = new Button("Stop");

    // Carpeta donde almacenamos las copias
    private final Path musicFolder = Paths.get("music");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!Files.exists(musicFolder)) {
                Files.createDirectories(musicFolder);
            }
        } catch (IOException e) {
            showError("No se pudo crear la carpeta de música: " + e.getMessage());
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label title = new Label("Reproductor simple - Java 21 + JavaFX");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox topBox = new HBox(title);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        // Center: lista y controls
        VBox center = new VBox(8);
        center.setPadding(new Insets(8));

        listView.setPrefHeight(240);
        refreshListView();

        HBox listControls = new HBox(8);
        Button addBtn = new Button("Añadir");
        Button removeBtn = new Button("Borrar copia");
        Button renameBtn = new Button("Renombrar copia");
        Button loadBtn = new Button("Cargar (reproducir)");
        listControls.getChildren().addAll(addBtn, loadBtn, renameBtn, removeBtn);

        center.getChildren().addAll(listView, listControls);

        root.setCenter(center);

        // Bottom: reproducción
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        playBtn.setOnAction(e -> playSelected());
        pauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });

        Button prevBtn = new Button("<<");
        Button nextBtn = new Button(">>");
        prevBtn.setOnAction(e -> playAdjacent(-1));
        nextBtn.setOnAction(e -> playAdjacent(1));

        // slider
        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(0);
        slider.setPrefWidth(300);
        slider.valueProperty().addListener((obs, oldV, newV) -> {
            if (slider.isValueChanging() && mediaPlayer != null) {
                Duration total = mediaPlayer.getTotalDuration();
                if (total != null && !total.isUnknown()) {
                    mediaPlayer.seek(total.multiply(newV.doubleValue() / 100.0));
                }
            }
        });

        loopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.setCycleCount(loopBtn.isSelected() ? MediaPlayer.INDEFINITE : 1);
            }
        });

        controls.getChildren().addAll(prevBtn, playBtn, pauseBtn, stopBtn, nextBtn, new Label(" | "), loopBtn, new Label(" | "), lblNow);

        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.CENTER);
        VBox vcontrols = new VBox(6, controls, new HBox(slider, timeLabel));
        vcontrols.setAlignment(Pos.CENTER);
        bottom.getChildren().add(vcontrols);

        root.setBottom(bottom);

        // acciones de lista
        addBtn.setOnAction(e -> addFiles(primaryStage));
        removeBtn.setOnAction(e -> deleteSelectedCopy());
        renameBtn.setOnAction(e -> renameSelected());
        loadBtn.setOnAction(e -> playSelected());

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                playSelected();
            }
        });

        // teclas
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) mediaPlayer.pause();
                else playSelected();
            }
        });

        Scene scene = new Scene(root, 800, 420);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reproductor - Java 21");
        primaryStage.show();
    }

    private void addFiles(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona archivos de audio");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.ogg"),
                new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("OGG", "*.ogg")
        );
        List<File> chosen = chooser.showOpenMultipleDialog(stage);
        if (chosen == null || chosen.isEmpty()) return;

        for (File f : chosen) {
            try {
                String name = generateUniqueName(f.getName());
                Path target = musicFolder.resolve(name);
                Files.copy(f.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                Track t = new Track(name, target.toUri().toString());
                tracks.add(t);
            } catch (IOException ex) {
                showError("Error copiando: " + ex.getMessage());
            }
        }
        refreshListView();
    }

    private String generateUniqueName(String original) {
        try {
            String base = original;
            String nameOnly = base;
            String ext = "";
            int dot = base.lastIndexOf('.');
            if (dot > 0) {
                nameOnly = base.substring(0, dot);
                ext = base.substring(dot);
            }
            String candidate = nameOnly + ext;
            int i = 1;
            while (Files.exists(musicFolder.resolve(candidate))) {
                candidate = nameOnly + "-" + i + ext;
                i++;
            }
            return candidate;
        } catch (Exception e) {
            // fallback
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
            return timestamp + "-" + original;
        }
    }

    private void refreshListView() {
        listView.getItems().clear();
        // carga tracks desde carpeta 'music' (solo si tracks list vacío, se pueden guardar persistencia luego)
        if (tracks.isEmpty()) {
            try {
                if (Files.exists(musicFolder)) {
                    DirectoryStream<Path> ds = Files.newDirectoryStream(musicFolder, "*.{mp3,ogg}");
                    for (Path p : ds) {
                        tracks.add(new Track(p.getFileName().toString(), p.toUri().toString()));
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }
        for (Track t : tracks) listView.getItems().add(t.getDisplayName());
    }

    private Track getSelectedTrack() {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= tracks.size()) return null;
        return tracks.get(idx);
    }

    private void playSelected() {
        Track t = getSelectedTrack();
        if (t == null) {
            // si no hay seleccion, intenta primer elemento
            if (!tracks.isEmpty()) {
                listView.getSelectionModel().select(0);
                t = tracks.get(0);
            } else {
                showInfo("No hay canciones en la lista. Añade archivos.");
                return;
            }
        }
        playTrack(t);
    }

    private void playTrack(Track t) {
        stopPlayerIfAny();
        try {
            Media media = new Media(t.getUri());
            mediaPlayer = new MediaPlayer(media);
            lblNow.setText("Reproduciendo: " + t.getDisplayName());

            // ciclo y control de loop
            mediaPlayer.setCycleCount(loopBtn.isSelected() ? MediaPlayer.INDEFINITE : 1);

            mediaPlayer.setOnReady(() -> {
                Duration total = mediaPlayer.getTotalDuration();
                updateTimeLabel(Duration.ZERO, total);
                // actualizar slider max
                slider.setValue(0);
                // escucha progreso
                mediaPlayer.currentTimeProperty().addListener((ov, oldTime, newTime) -> {
                    updateTimeLabel(newTime, total);
                    if (!slider.isValueChanging() && total != null && !total.isUnknown()) {
                        double progress = newTime.toMillis() / total.toMillis() * 100.0;
                        slider.setValue(progress);
                    }
                });
                mediaPlayer.play();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (mediaPlayer.getCycleCount() != MediaPlayer.INDEFINITE) {
                    // si no loop, avanza al siguiente
                    playAdjacent(1);
                }
            });

            mediaPlayer.setOnError(() -> showError("Error mediaPlayer: " + mediaPlayer.getError().getMessage()));

        } catch (Exception e) {
            showError("No se pudo reproducir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopPlayerIfAny() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void playAdjacent(int delta) {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx < 0 && !tracks.isEmpty()) idx = 0;
        int next = idx + delta;
        if (next < 0) next = tracks.size() - 1;
        if (next >= tracks.size()) next = 0;
        listView.getSelectionModel().select(next);
        playSelected();
    }

    private void deleteSelectedCopy() {
        Track t = getSelectedTrack();
        if (t == null) {
            showInfo("Selecciona una copia para borrar.");
            return;
        }
        // confirm
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Borrar copia: " + t.getDisplayName() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try {
                Path p = Paths.get(Paths.get(new java.net.URI(t.getUri())).getPath());
                stopIfPlayingUri(t.getUri());
                Files.deleteIfExists(p);
                tracks.remove(t);
                refreshListView();
            } catch (Exception e) {
                showError("No se pudo borrar: " + e.getMessage());
            }
        }
    }

    private void stopIfPlayingUri(String uri) {
        if (mediaPlayer != null) {
            Media m = mediaPlayer.getMedia();
            if (m != null && m.getSource().equals(uri)) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
        }
    }

    private void renameSelected() {
        Track t = getSelectedTrack();
        if (t == null) {
            showInfo("Selecciona una copia para renombrar.");
            return;
        }
        TextInputDialog dlg = new TextInputDialog(t.getDisplayName());
        dlg.setTitle("Renombrar copia");
        dlg.setHeaderText("Nuevo nombre (con extensión, p.ej. mi-cancion.mp3):");
        Optional<String> res = dlg.showAndWait();
        if (res.isPresent()) {
            String newName = res.get().trim();
            if (newName.isEmpty()) {
                showInfo("Nombre inválido.");
                return;
            }
            try {
                Path oldPath = Paths.get(new java.net.URI(t.getUri())).getPath();
                Path newPath = oldPath.resolveSibling(newName);
                if (Files.exists(newPath)) {
                    showInfo("Ya existe un archivo con ese nombre en la carpeta music.");
                    return;
                }
                Files.move(oldPath, newPath);
                // actualizar track
                t.setDisplayName(newName);
                t.setUri(newPath.toUri().toString());
                refreshListView();
            } catch (Exception e) {
                showError("Error renombrando: " + e.getMessage());
            }
        }
    }

    private void updateTimeLabel(Duration current, Duration total) {
        String cur = formatDuration(current);
        String tot = (total == null || total.isUnknown()) ? "--:--" : formatDuration(total);
        Platform.runLater(() -> timeLabel.setText(cur + " / " + tot));
    }

    private String formatDuration(Duration d) {
        if (d == null || d.isUnknown()) return "--:--";
        int seconds = (int) Math.floor(d.toSeconds());
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            a.showAndWait();
        });
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            a.showAndWait();
        });
    }

    // Clase interna para representar una pista
    private static class Track {
        private String displayName;
        private String uri;

        public Track(String displayName, String uri) {
            this.displayName = displayName;
            this.uri = uri;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getUri() {
            return uri;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }
}