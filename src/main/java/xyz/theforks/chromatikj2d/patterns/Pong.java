package xyz.theforks.chromatikj2d.patterns;

import java.awt.Color;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponentName;

@LXCategory("Form")
@LXComponentName("Pong")
public class Pong extends Render2DBase {
    private static final int SIZE = 30;
    private static final int PADDLE_WIDTH = 4;
    private static final int PADDLE_HEIGHT = 6;
    private static final int BALL_SIZE = 2;
    private static final int PADDLE_SPEED = 1;
    
    // Game state
    private int ballX = SIZE / 2;
    private int ballY = SIZE / 2;
    private int ballVelocityX = 1;
    private int ballVelocityY = 1;
    private int leftPaddleY = SIZE / 2 - PADDLE_HEIGHT / 2;
    private int rightPaddleY = SIZE / 2 - PADDLE_HEIGHT / 2;
    
    public Pong(LX lx) {
        super(lx);
        initialize(SIZE, SIZE);
    }
    
    private void updateGame() {
        // Move the ball
        ballX += ballVelocityX;
        ballY += ballVelocityY;
        
        // Bounce off top and bottom walls
        if (ballY <= 0 || ballY >= SIZE - BALL_SIZE) {
            ballVelocityY = -ballVelocityY;
        }
        
        // AI for left paddle (follows the ball)
        moveLeftPaddle();
        
        // AI for right paddle (follows the ball)
        moveRightPaddle();
        
        // Check for paddle collisions
        if (ballX <= PADDLE_WIDTH && ballY >= leftPaddleY && ballY <= leftPaddleY + PADDLE_HEIGHT) {
            ballVelocityX = -ballVelocityX;
        }
        
        if (ballX >= SIZE - PADDLE_WIDTH - BALL_SIZE && ballY >= rightPaddleY && ballY <= rightPaddleY + PADDLE_HEIGHT) {
            ballVelocityX = -ballVelocityX;
        }
        
        // Reset ball if it goes off the screen
        if (ballX < 0 || ballX > SIZE) {
            ballX = SIZE / 2;
            ballY = SIZE / 2;
            // Randomize direction a bit
            ballVelocityX = (ballVelocityX > 0) ? -1 : 1;
            ballVelocityY = (Math.random() > 0.5) ? 1 : -1;
        }
    }
    
    private void moveLeftPaddle() {
        // Simple AI: follow the ball
        int paddleCenter = leftPaddleY + PADDLE_HEIGHT / 2;
        if (paddleCenter < ballY && leftPaddleY + PADDLE_HEIGHT < SIZE) {
            leftPaddleY += PADDLE_SPEED;
        } else if (paddleCenter > ballY && leftPaddleY > 0) {
            leftPaddleY -= PADDLE_SPEED;
        }
    }
    
    private void moveRightPaddle() {
        // Simple AI: follow the ball
        int paddleCenter = rightPaddleY + PADDLE_HEIGHT / 2;
        if (paddleCenter < ballY && rightPaddleY + PADDLE_HEIGHT < SIZE) {
            rightPaddleY += PADDLE_SPEED;
        } else if (paddleCenter > ballY && rightPaddleY > 0) {
            rightPaddleY -= PADDLE_SPEED;
        }
    }
    
    @Override
    public void renderFrame(double deltaMs) {
        updateGame();
        // Clear the canvas
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, SIZE, SIZE);
        
        // Draw the paddles
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, leftPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        graphics.fillRect(SIZE - PADDLE_WIDTH, rightPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Draw the ball
        graphics.fillRect(ballX, ballY, BALL_SIZE, BALL_SIZE);
        
        // Draw the center line
        graphics.setColor(Color.GRAY);
        for (int y = 0; y < SIZE; y += 4) {
            graphics.fillRect(SIZE / 2, y, 1, 2);
        }
    }
}