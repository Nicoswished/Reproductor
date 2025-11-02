package dev.reproductor.nico;



import dev.reproductor.nico.Manager.PlayListManager;
import dev.reproductor.nico.Manager.PlayerManager;
import dev.reproductor.nico.UIS.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


public class Main extends JFrame {


    private JList<String> lista_can;
    private DefaultListModel<String> modeloLista;
    private JButton agregar, eliminar, anterior, siguiente, play, detener, guardarPlaylist, cargarPlaylist, loopButton;
    private JSlider volumen, tiempoSlider, barraVisual;
    private JTextField nombre_can;
    private JLabel tiempoLabel;
    private JComboBox<String> tipo_reproduccion;

    private final PlayListManager playlistManager;
    public final PlayerManager player;

    private boolean loop = false;
    private boolean sliderMoving = false;
    private boolean mostrarLista = true;
    private boolean modoOscuro = true;

    private JPanel panel, centro, botonesLista, volPanel, tiempoPanel, controles;
    private JMenuItem mostrarTituloItem, temaItem, volumenPctToggleItem;

    private JLabel volumenPctLabel;

    public Main() {
        setTitle("Reproductor de Música - Nico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        playlistManager = new PlayListManager();

        java.net.URL iconURL = getClass().getResource("/iconos/music.png");
        if (iconURL != null) setIconImage(new ImageIcon(iconURL).getImage());

        initUI();
        player = new PlayerManager(this);
    }

    private void initUI() {
        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(panel);

        JMenuBar menuBar = new JMenuBar();
        JMenu menuOpciones = new JMenu("Opciones");

        mostrarTituloItem = new JMenuItem("Mostrar solo el título (Desactivado)");
        temaItem = new JMenuItem("Cambiar a modo claro");
        volumenPctToggleItem = new JMenuItem("Ocultar % de volumen");

        mostrarTituloItem.addActionListener(e -> toggleTitulo());
        temaItem.addActionListener(e -> toggleTema());
        volumenPctToggleItem.addActionListener(e -> toggleVolumenPctVisibility());

        menuOpciones.add(volumenPctToggleItem);
        menuOpciones.add(mostrarTituloItem);
        menuOpciones.add(temaItem);
        menuBar.add(menuOpciones);
        setJMenuBar(menuBar);

        modeloLista = new DefaultListModel<>();
        lista_can = new JList<>(modeloLista);
        lista_can.setSelectionBackground(Color.decode("#3a85ff"));
        JScrollPane scrollLista = new JScrollPane(lista_can);
        scrollLista.setPreferredSize(new Dimension(200, 0));
        panel.add(scrollLista, BorderLayout.WEST);

        botonesLista = new JPanel(new GridLayout(5,1,5,5));
        agregar = UIComponents.boton("Agregar");
        eliminar = UIComponents.boton("Quitar");
        loopButton = UIComponents.boton("Loop: OFF");
        guardarPlaylist = UIComponents.boton("Guardar Playlist");
        cargarPlaylist = UIComponents.boton("Cargar Playlist");

        botonesLista.add(agregar);
        botonesLista.add(eliminar);
        botonesLista.add(loopButton);
        botonesLista.add(guardarPlaylist);
        botonesLista.add(cargarPlaylist);
        panel.add(botonesLista, BorderLayout.SOUTH);

        centro = new JPanel(new BorderLayout(10,10));

        JPanel infoPanel = new JPanel(new BorderLayout(5,5));
        nombre_can = new JTextField("Reproductor de Nico");
        nombre_can.setEditable(false);
        nombre_can.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nombre_can.setHorizontalAlignment(JTextField.CENTER);
        nombre_can.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tipo_reproduccion = new JComboBox<>(new String[]{"Normal", "Inversa", "Aleatoria"});
        infoPanel.add(nombre_can, BorderLayout.CENTER);
        infoPanel.add(tipo_reproduccion, BorderLayout.EAST);
        centro.add(infoPanel, BorderLayout.NORTH);

        tiempoPanel = new JPanel(new BorderLayout(5,5));
        tiempoSlider = UIComponents.slider(0,1000,0);
        tiempoLabel = new JLabel("00:00 / 00:00", SwingConstants.CENTER);
        tiempoPanel.add(tiempoSlider, BorderLayout.CENTER);
        tiempoPanel.add(tiempoLabel, BorderLayout.SOUTH);

        barraVisual = UIComponents.slider(0,100,0);
        barraVisual.setEnabled(false);
        tiempoPanel.add(barraVisual, BorderLayout.NORTH);

        centro.add(tiempoPanel, BorderLayout.CENTER);

        controles = new JPanel();
        anterior = UIComponents.botonIcon("/iconos/anterior.png");
        play = UIComponents.botonIcon("/iconos/play.png");
        detener = UIComponents.botonIcon("/iconos/pausa.png");
        siguiente = UIComponents.botonIcon("/iconos/siguiente.png");
        controles.add(anterior);
        controles.add(play);
        controles.add(detener);
        controles.add(siguiente);
        centro.add(controles, BorderLayout.SOUTH);

        volPanel = new JPanel(new BorderLayout());
        JLabel volLabel = new JLabel("Volumen", SwingConstants.CENTER);
        volumen = UIComponents.slider(0,100,100);
        // Mostrar porcentaje al lado del slider
        volumenPctLabel = new JLabel(volumen.getValue() + "%", SwingConstants.CENTER);

        JPanel volCenter = new JPanel(new BorderLayout(5,5));
        volCenter.add(volumen, BorderLayout.CENTER);
        volCenter.add(volumenPctLabel, BorderLayout.EAST);

        volPanel.add(volLabel, BorderLayout.NORTH);
        volPanel.add(volCenter, BorderLayout.CENTER);
        panel.add(volPanel, BorderLayout.EAST);

        panel.add(centro, BorderLayout.CENTER);

        // Listeners y acciones
        agregar.addActionListener(e -> playlistManager.agregarCancion(modeloLista));
        eliminar.addActionListener(e -> playlistManager.eliminarCancion(lista_can, modeloLista, player));
        guardarPlaylist.addActionListener(e -> playlistManager.guardarPlaylist());
        cargarPlaylist.addActionListener(e -> playlistManager.cargarPlaylist(modeloLista));

        play.addActionListener(e -> player.playPauseCancion());
        detener.addActionListener(e -> player.detenerCancion());
        siguiente.addActionListener(e -> nextSong());
        anterior.addActionListener(e -> prevSong());

        loopButton.addActionListener(e -> {
            loop = !loop;
            player.setLoop(loop);
            loopButton.setText(loop ? "Loop: ON" : "Loop: OFF");
        });

        tipo_reproduccion.addActionListener(e -> {
            player.setModoReproduccion((String) tipo_reproduccion.getSelectedItem());
            player.resetRandomListIfNeeded();
        });

        lista_can.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    player.reproducirIndice(lista_can.getSelectedIndex());
                    player.resetRandomListIfNeeded();
                }
            }
        });

        tiempoSlider.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e){ sliderMoving = true; }
            public void mouseReleased(MouseEvent e){
                player.moverSliderTiempo(tiempoSlider, sliderMoving);
                sliderMoving = false;
            }
        });

        volumen.addChangeListener(e -> {
            int v = volumen.getValue();
            volumenPctLabel.setText(v + "%");
            player.setVolumen(v); // persistencia y aplicación en PlayerManager
        });

        // Timer para actualizar tiempo de reproducción
        new Timer(500, e -> player.actualizarTiempo(tiempoSlider, tiempoLabel)).start();

        aplicarTema();

        // Inicializar slider con valor desde prefs (PlayerManager lo colocó en constructor)
        // pero por si acaso sincronizamos:
        int initialVol = volumen.getValue();
        volumenPctLabel.setText(initialVol + "%");
    }

    public void resetRandomListIfNeeded() {
        if(playlistManager != null) playlistManager.resetRandomList();
    }

    public File getCurrentSongFile() {
        if(playlistManager == null || playlistManager.getCanciones().isEmpty()) return null;
        int idx = playlistManager.getIndiceActual();
        if(idx < 0 || idx >= playlistManager.getCanciones().size()) return null;
        return playlistManager.getCanciones().get(idx);
    }

    public JTextField getNombreCan() { return nombre_can; }
    public PlayListManager getPlaylistManager() { return playlistManager; }
    public JSlider getBarraVisual() { return barraVisual; }

    // Exponer slider y etiqueta para PlayerManager
    public JSlider getVolumenSlider() { return volumen; }
    public JLabel getVolumenPctLabel() { return volumenPctLabel; }

    private void toggleTitulo() {
        mostrarLista = !mostrarLista;
        boolean soloTitulo = !mostrarLista;

        lista_can.setVisible(!soloTitulo);
        botonesLista.setVisible(!soloTitulo);
        volPanel.setVisible(!soloTitulo);
        controles.setVisible(!soloTitulo);
        tiempoPanel.setVisible(!soloTitulo);
        tipo_reproduccion.setVisible(!soloTitulo);

        mostrarTituloItem.setText(soloTitulo ?
                "Mostrar solo el título (Activado)" :
                "Mostrar solo el título (Desactivado)");
    }

    private void toggleTema() {
        modoOscuro = !modoOscuro;
        aplicarTema();
        temaItem.setText(modoOscuro ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
    }

    private void toggleVolumenPctVisibility() {
        boolean visible = !volumenPctLabel.isVisible();
        volumenPctLabel.setVisible(visible);
        volumenPctToggleItem.setText(visible ? "Ocultar % de volumen" : "Mostrar % de volumen");
    }

    private void aplicarTema() {
        Color fondo = modoOscuro ? Color.decode("#1e1e1e") : Color.decode("#f4f4f4");
        Color texto = modoOscuro ? Color.WHITE : Color.BLACK;
        Color fondoLista = modoOscuro ? Color.decode("#2e2e2e") : Color.WHITE;

        getContentPane().setBackground(fondo);
        panel.setBackground(fondo);
        centro.setBackground(fondo);
        botonesLista.setBackground(fondo);
        volPanel.setBackground(fondo);
        tiempoPanel.setBackground(fondo);
        controles.setBackground(fondo);

        lista_can.setBackground(fondoLista);
        lista_can.setForeground(texto);
        nombre_can.setBackground(fondoLista);
        nombre_can.setForeground(texto);
        tiempoLabel.setForeground(texto);
    }

    private void nextSong() { player.siguienteCancion(); }
    private void prevSong() { player.anteriorCancion(); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}