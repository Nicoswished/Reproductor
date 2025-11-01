package dev.reproductor.nico.Visual;

import dev.reproductor.nico.Audio.AudioAnalyzer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.util.Random;


public class OpenGLVisualizer implements Runnable {
        private final AudioAnalyzer analyzer;
        private final JFrame parent;
        private final boolean fullscreen;
        private long window;
        private boolean running = false;
        private final Random random = new Random();

    public OpenGLVisualizer(AudioAnalyzer analyzer, JFrame parent, boolean fullscreen){
            this.analyzer = analyzer;
            this.parent = parent;
            this.fullscreen = fullscreen;
        }

        public void start () {
            running = true;
            new Thread(this, "OpenGLVisualizer").start();
            analyzer.start();
        }

        public void stop () {
            running = false;
        }

        @Override
        public void run () {
            initGL();
            loop();
            GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
        }

        private void initGL () {
            if (!GLFW.glfwInit()) throw new IllegalStateException("No se pudo inicializar GLFW");

            int width, height;
            long monitor = 0;

            if (fullscreen) {
                monitor = GLFW.glfwGetPrimaryMonitor();
                var mode = GLFW.glfwGetVideoMode(monitor);
                width = mode.width();
                height = mode.height();
            } else {
                Point loc = parent.getLocationOnScreen();
                width = parent.getWidth();
                height = parent.getHeight();
            }

            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, fullscreen ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE);

            window = GLFW.glfwCreateWindow(width, height, "Visualizer", fullscreen ? monitor : 0, 0);
            if (window == 0) throw new RuntimeException("No se pudo crear ventana");

            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();
            GLFW.glfwSwapInterval(1);
            GLFW.glfwShowWindow(window);
        }

        private void loop () {
            long startTime = System.currentTimeMillis();

            while (running && !GLFW.glfwWindowShouldClose(window)) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                float energy = analyzer.getCurrentEnergy();
                float intensity = Math.min(energy / 20000f, 1f);

                // efectos sencillos: vibración de color
                GL11.glColor3f(random.nextFloat() * intensity, random.nextFloat() * intensity, random.nextFloat() * intensity);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(-1, -1);
                GL11.glVertex2f(1, -1);
                GL11.glVertex2f(1, 1);
                GL11.glVertex2f(-1, 1);
                GL11.glEnd();

                GLFW.glfwSwapBuffers(window);
                GLFW.glfwPollEvents();

                if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) running = false;

                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
