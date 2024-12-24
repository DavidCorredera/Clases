package com.davodamc.managers;

import com.davodamc.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ParticlesManager {

    private final Main plugin;
    private final Map<Player, BukkitTask> particleTasks;

    // RANDOM
    public static final Random random = new Random(System.nanoTime());

    public static Vector getRandomVector() {
        double x = random.nextDouble() * 2.0D - 1.0D;
        double y = random.nextDouble() * 2.0D - 1.0D;
        double z = random.nextDouble() * 2.0D - 1.0D;
        return (new Vector(x, y, z)).normalize();
    }

    public ParticlesManager(Main plugin) {
        this.plugin = plugin;
        this.particleTasks = new HashMap<>();
    }

    public void showParticles(Location loc, Particle particleType, Integer particleCount) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();

            double spread = 3.0;

            for (int i = 0; i < particleCount; i++) {
                double offsetX = Math.random() * spread - (spread / 2);
                double offsetY = Math.random() * spread - (spread / 2);
                double offsetZ = Math.random() * spread - (spread / 2);

                loc.getWorld().spawnParticle(particleType, x + offsetX, y + offsetY, z + offsetZ, 0, 0, 0, 0, null);
            }
        });
    }


    public void explosiveParticles(Location center, Particle particle, int quantity) {
        World world = center.getWorld();
        if (world == null) return; // Asegúrate de que el mundo no sea nulo

        // Crea un círculo de partículas en el centro
        for (double t = 0; t < 2 * Math.PI; t += Math.PI / 32) {
            double x = center.getX() + Math.cos(t) * 0.5; // Radio de 0.5 bloques
            double y = center.getY();
            double z = center.getZ() + Math.sin(t) * 0.5; // Radio de 0.5 bloques
            Location particleLocation = new Location(world, x, y, z);
            world.spawnParticle(particle, particleLocation, quantity);
        }

        // Expande el círculo con cada bucle
        for (int radius = 1; radius <= 2; radius++) {
            for (double t = 0; t < 2 * Math.PI; t += Math.PI / 32) {
                double x = center.getX() + Math.cos(t) * radius;
                double y = center.getY();
                double z = center.getZ() + Math.sin(t) * radius;
                Location particleLocation = new Location(world, x, y, z);
                world.spawnParticle(particle, particleLocation, quantity);
            }
        }
    }

    // CORONAS CIRCULARES ALREDEDOR DEL JUGADOR

    // AUREOLA
    public void createHalo(Player player, int red, int green, int blue, int particleCount, double radius) {
        // Ubicación de la cabeza del jugador (0.5 bloques sobre los pies)
        Location headLocation = player.getLocation().add(0, 2.5, 0);

        // Crear el ángulo entre los puntos del círculo (aureola)
        double angleIncrement = 2 * Math.PI / particleCount;

        // Generar las partículas en círculo alrededor de la cabeza
        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleIncrement;
            double x = headLocation.getX() + radius * Math.cos(angle);
            double z = headLocation.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(player.getWorld(), x, headLocation.getY(), z);

            // Crear las partículas solo visibles para el jugador pasado como argumento
            player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(red, green, blue), 1));
        }
    }

    // ALREDEDOR DEL JUGADOR
    public void createParticleCircle(Location center, double radius, int points, int red, int green, int blue) {
        // Calcula el ángulo entre cada punto
        double angleIncrement = 2 * Math.PI / points;

        // Genera los puntos del círculo
        for (int i = 0; i < points; i++) {
            double angle = i * angleIncrement;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(center.getWorld(), x, center.getY(), z);

            // Emite una partícula en la ubicación del punto
            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(red, green, blue), 1));
        }
    }

    // TUBO DE PARTÍCULAS
    public void createParticleTube(Location center, double radius, int points, Particle particle, int layers, double circleHeight) {
        // Calcula el ángulo entre cada punto del círculo
        double angleIncrement = 2 * Math.PI / points;

        // Genera cada capa del tubo
        for (int layer = 0; layer < layers; layer++) {
            // Calcula la altura para el círculo actual
            double currentY = center.getY() + (layer * circleHeight);

            // Genera los puntos del círculo en la capa actual
            for (int i = 0; i < points; i++) {
                double angle = i * angleIncrement;
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                Location particleLocation = new Location(center.getWorld(), x, currentY, z);

                // Emite una partícula en la ubicación del punto
                center.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
            }
        }
    }


    public void spawnParticleSphere(Player p, Location center, Particle particleType, int red, int green, int blue, double radius, int density, long durationTicks) {

        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(p.getLocation())) return;

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        // Cancelar la tarea anterior del jugador si existe
        cancelParticleTask(p);

        // Programar una nueva tarea para generar la esfera durante la duración especificada
        BukkitTask task = scheduler.runTaskTimerAsynchronously(plugin, new Runnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                if (ticksElapsed < durationTicks) {
                    // Iterar a través de las partículas para crear una esfera rápida
                    for (int i = 0; i < density; ++i) {
                        // Generar un vector aleatorio y multiplicarlo por el radio
                        Vector v = getRandomVector().multiply(radius);

                        // Calcular la ubicación de la partícula dentro de la esfera
                        Location particleLocation = center.clone().add(v);

                        if(particleType.equals(Particle.REDSTONE)) { // ESFERA DEL DESCONTROL, A FUTURO SE PUEDEN HACER 2 MÉTODOS
                            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1,
                                    new Particle.DustOptions(org.bukkit.Color.fromRGB(red, green, blue), 1));
                        } else {

                            // Mostrar la partícula en la ubicación calculada
                            p.getWorld().spawnParticle(particleType, particleLocation, 1);
                        }


                        // Eliminar el vector de la ubicación para la próxima partícula
                        particleLocation.subtract(v);
                    }

                    ticksElapsed++;
                } else {
                    // La duración especificada ha terminado, cancelar la tarea
                    cancelParticleTask(p);
                }
            }
        }, 0L, 1L);

        // Guardar la tarea del jugador en el mapa
        particleTasks.put(p, task);
    }

    public void spawnClosingParticleSphere(Player p, Location center, Particle particleType, double radius, int density, long durationTicks) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        // Cancelar la tarea anterior del jugador si existe
        cancelParticleTask(p);

        // Programar una nueva tarea para generar la esfera durante la duración especificada
        BukkitTask task = scheduler.runTaskTimerAsynchronously(plugin, new Runnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                if (ticksElapsed < durationTicks) {
                    // Calcular el porcentaje de cierre (0 a 1)
                    double closureFactor = 1 - (double) ticksElapsed / durationTicks;

                    // Iterar a través de las partículas para crear la esfera cerrada
                    for (int i = 0; i < density; ++i) {
                        // Generar un ángulo aleatorio para la esfera
                        double theta = Math.random() * Math.PI; // Ángulo desde el eje Y (0 a PI)
                        double phi = Math.random() * 2 * Math.PI; // Ángulo alrededor del eje Y (0 a 2PI)

                        // Calcular las coordenadas en la esfera
                        double x = radius * Math.sin(theta) * Math.cos(phi);
                        double y = radius * Math.cos(theta) * closureFactor; // Aplicar el closureFactor para cerrar la esfera
                        double z = radius * Math.sin(theta) * Math.sin(phi);

                        // Calcular la ubicación de la partícula dentro de la esfera
                        Location particleLocation = center.clone().add(x, y, z);

                        if (particleType.equals(Particle.REDSTONE)) {
                            // Mostrar la partícula de color si es de tipo REDSTONE
                            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1,
                                    new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 255, 0), 1));
                        } else {
                            // Mostrar la partícula en la ubicación calculada
                            p.getWorld().spawnParticle(particleType, particleLocation, 1);
                        }
                    }

                    ticksElapsed++;
                } else {
                    // La duración especificada ha terminado, cancelar la tarea
                    cancelParticleTask(p);
                }
            }
        }, 0L, 1L);

        // Guardar la tarea del jugador en el mapa
        particleTasks.put(p, task);
    }

    public void cancelParticleTask(Player p) {
        // Obtener la tarea del jugador si existe y cancelarla
        BukkitTask task = particleTasks.remove(p);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}