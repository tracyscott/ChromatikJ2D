package xyz.theforks.chromatikj2d.patterns;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponentName;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory("Form")
@LXComponentName("Asteroids")
public class MiniAsteroids extends Render2DBase {
    private static final int GRID_SIZE = 30;
    private static final int FRAME_DELAY = 150; // Slower for visibility
    private static final Random RANDOM = new Random();
    private Ship ship;
    private List<Asteroid> asteroids;
    private List<Bullet> bullets;

    final private CompoundParameter speedKnob = new CompoundParameter("Speed", 1.0, 0.1, 4.0)
        .setDescription("Speed of the game");
    
    private double currentFrameTime = 0.0;
    
    public MiniAsteroids(LX lx) {
        super(lx);
        
        initGame();
        initialize(GRID_SIZE, GRID_SIZE);
        addParameter("speed", speedKnob);
    }
    
    @Override
    public void onActive() {
        initGame();
    }

    private void initGame() {
        ship = new Ship();
        asteroids = new ArrayList<>();
        bullets = new ArrayList<>();
        
        // Create initial asteroids
        for (int i = 0; i < 4; i++) {
            addAsteroid();
        }
    }
    
    private void addAsteroid() {
        // Create asteroid away from the ship
        int x, y;
        do {
            x = RANDOM.nextInt(GRID_SIZE);
            y = RANDOM.nextInt(GRID_SIZE);
        } while (distance(x, y, ship.x, ship.y) < 10);
        
        int velocityX = RANDOM.nextInt(3) - 1; // -1, 0, or 1
        int velocityY = RANDOM.nextInt(3) - 1; // -1, 0, or 1
        
        // Ensure asteroid is moving
        if (velocityX == 0 && velocityY == 0) {
            velocityX = 1;
        }
        
        asteroids.add(new Asteroid(x, y, velocityX, velocityY));
    }
    
    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
    
    private void update() {
        // Update ship
        ship.update();
        
        // Find closest asteroid for targeting
        Asteroid target = findClosestAsteroid();
        if (target != null) {
            ship.targetAsteroid(target);
        }
        
        // Update bullets
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();
            
            // Remove bullets that are off-screen or expired
            if (bullet.lifetime <= 0 || 
                bullet.x < 0 || bullet.x >= GRID_SIZE || 
                bullet.y < 0 || bullet.y >= GRID_SIZE) {
                bulletIterator.remove();
                continue;
            }
            
            // Check for bullet collisions with asteroids
            Iterator<Asteroid> asteroidIterator = asteroids.iterator();
            while (asteroidIterator.hasNext()) {
                Asteroid asteroid = asteroidIterator.next();
                if (distance(bullet.x, bullet.y, asteroid.x, asteroid.y) < 2) {
                    // Hit asteroid
                    asteroidIterator.remove();
                    bulletIterator.remove();
                    
                    // Split asteroid into two smaller ones if it's big enough
                    if (asteroid.size > 1) {
                        for (int i = 0; i < 2; i++) {
                            int velocityX = RANDOM.nextInt(3) - 1;
                            int velocityY = RANDOM.nextInt(3) - 1;
                            if (velocityX == 0 && velocityY == 0) velocityX = 1;
                            
                            Asteroid newAsteroid = new Asteroid(
                                asteroid.x, asteroid.y, velocityX, velocityY);
                            newAsteroid.size = asteroid.size - 1;
                            asteroids.add(newAsteroid);
                        }
                    }
                    
                    break;
                }
            }
        }
        
        // Update asteroids
        for (Asteroid asteroid : asteroids) {
            asteroid.update();
        }
        
        // Add new asteroids if there are too few
        if (asteroids.size() < 3) {
            addAsteroid();
        }
    }
    
    private Asteroid findClosestAsteroid() {
        if (asteroids.isEmpty()) {
            return null;
        }
        
        Asteroid closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Asteroid asteroid : asteroids) {
            double dist = distance(ship.x, ship.y, asteroid.x, asteroid.y);
            if (dist < minDistance) {
                minDistance = dist;
                closest = asteroid;
            }
        }
        
        return closest;
    }
    
    @Override
    public void renderFrame(double deltaMs) {
        currentFrameTime += deltaMs;
        if (currentFrameTime > FRAME_DELAY * (1.0 / speedKnob.getValue())) {
           currentFrameTime = 0.0;
           update();
        }
    
        // Clear the canvas
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, GRID_SIZE, GRID_SIZE);
        
        // Draw ship
        graphics.setColor(Color.GREEN);
        graphics.fillRect(ship.x, ship.y, 2, 2);
        
        // Draw ship direction indicator
        int dirX = ship.x + ship.dirX;
        int dirY = ship.y + ship.dirY;
        if (dirX >= 0 && dirX < GRID_SIZE && dirY >= 0 && dirY < GRID_SIZE) {
            graphics.setColor(new Color(0, 100, 0)); // Darker green
            graphics.fillRect(dirX, dirY, 1, 1);
        }
        
        // Draw bullets
        graphics.setColor(Color.RED);
        for (Bullet bullet : bullets) {
            graphics.fillRect(bullet.x, bullet.y, 1, 1);
        }
        
        // Draw asteroids
        graphics.setColor(Color.WHITE);
        for (Asteroid asteroid : asteroids) {
            graphics.fillRect(asteroid.x, asteroid.y, 1, 1);
            
            // Draw larger asteroids with extra pixels
            if (asteroid.size > 1) {
                for (int i = 0; i < asteroid.extraPixels.length; i += 2) {
                    int px = asteroid.x + asteroid.extraPixels[i];
                    int py = asteroid.y + asteroid.extraPixels[i + 1];
                    
                    if (px >= 0 && px < GRID_SIZE && py >= 0 && py < GRID_SIZE) {
                        graphics.fillRect(px, py, 1, 1);
                    }
                }
            }
        }
    }
    
    // Game entities
    private class Ship {
        private int x = GRID_SIZE / 2;
        private int y = GRID_SIZE / 2;
        private int dirX = 0;  // Direction X (-1, 0, 1)
        private int dirY = -1; // Direction Y (-1, 0, 1)
        private int shootCooldown = 0;
        
        public void update() {
            // Move ship occasionally for more interesting behavior
            if (RANDOM.nextInt(10) == 0) {
                int newX = x + dirX;
                int newY = y + dirY;
                
                // Only move if within bounds
                if (newX >= 0 && newX < GRID_SIZE && newY >= 0 && newY < GRID_SIZE) {
                    x = newX;
                    y = newY;
                }
            }
            
            // Reduce shoot cooldown
            if (shootCooldown > 0) {
                shootCooldown--;
            }
        }
        
        public void targetAsteroid(Asteroid asteroid) {
            // Calculate direction to asteroid
            int dx = asteroid.x - x;
            int dy = asteroid.y - y;
            
            // Handle screen wrapping for closer path
            if (Math.abs(dx) > GRID_SIZE / 2) {
                dx = -Integer.signum(dx) * (GRID_SIZE - Math.abs(dx));
            }
            if (Math.abs(dy) > GRID_SIZE / 2) {
                dy = -Integer.signum(dy) * (GRID_SIZE - Math.abs(dy));
            }
            
            // Set direction based on the angle to asteroid
            if (Math.abs(dx) > Math.abs(dy)) {
                dirX = Integer.signum(dx);
                dirY = 0;
            } else {
                dirX = 0;
                dirY = Integer.signum(dy);
            }
            
            // For diagonal movement
            if (RANDOM.nextInt(3) == 0) {
                dirX = Integer.signum(dx);
                dirY = Integer.signum(dy);
            }
            
            // Shoot if cooldown allows
            if (shootCooldown <= 0) {
                bullets.add(new Bullet(x, y, dirX, dirY));
                shootCooldown = 5;
            }
        }
    }
    
    private class Asteroid {
        private int x;
        private int y;
        private int velocityX;
        private int velocityY;
        private int size;
        private int[] extraPixels; // For larger asteroids
        
        public Asteroid(int x, int y, int velocityX, int velocityY) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.size = 2; // Start with largest size
            
            // Generate random extra pixels for the asteroid shape
            this.extraPixels = new int[size * 4];
            for (int i = 0; i < extraPixels.length; i += 2) {
                extraPixels[i] = RANDOM.nextInt(3) - 1;     // x offset
                extraPixels[i + 1] = RANDOM.nextInt(3) - 1; // y offset
                
                // Ensure we don't have (0,0) as that's the center pixel
                if (extraPixels[i] == 0 && extraPixels[i + 1] == 0) {
                    extraPixels[i] = 1;
                }
            }
        }
        
        public void update() {
            x += velocityX;
            y += velocityY;
            
            // Wrap around screen edges
            if (x < 0) x = GRID_SIZE - 1;
            if (x >= GRID_SIZE) x = 0;
            if (y < 0) y = GRID_SIZE - 1;
            if (y >= GRID_SIZE) y = 0;
        }
    }
    
    private class Bullet {
        private int x;
        private int y;
        private int velocityX;
        private int velocityY;
        private int lifetime = 10; // How many frames the bullet lasts
        
        public Bullet(int x, int y, int velocityX, int velocityY) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            
            // If velocity is 0, set a default
            if (velocityX == 0 && velocityY == 0) {
                this.velocityY = -1;
            }
        }
        
        public void update() {
            x += velocityX;
            y += velocityY;
            lifetime--;
            
            // Wrap around screen edges
            if (x < 0) x = GRID_SIZE - 1;
            if (x >= GRID_SIZE) x = 0;
            if (y < 0) y = GRID_SIZE - 1;
            if (y >= GRID_SIZE) y = 0;
        }
    }
}