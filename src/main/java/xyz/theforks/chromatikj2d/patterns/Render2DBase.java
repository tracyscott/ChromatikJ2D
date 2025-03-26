package xyz.theforks.chromatikj2d.patterns;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

abstract public class Render2DBase extends LXPattern {
  protected BufferedImage renderImage;
  protected Graphics2D graphics;

  protected int width;
  protected int height;

  public Render2DBase(LX lx) {
    super(lx);
  }

  protected void initialize(int width, int height) {
    this.width = width;
    this.height = height;
    renderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    graphics = renderImage.createGraphics();
  }
  
  abstract protected void renderFrame(double deltaMs);

  @Override
  protected void run(double deltaMs) {
    // Clear the image to black
    // TODO(tracy): support fading here if possible.  We would want the fill color to have some percentage of
    // alpha.
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, width, height);

    renderFrame(deltaMs);

    for (LXPoint p : model.points) {
      colors[p.index] = pixelFromBufferedImageST(renderImage, p.xn, p.yn);
    }
  }

  static public int pixelFromBufferedImageST(BufferedImage image, float s, float t) {
    return image.getRGB((int)(s*(image.getWidth()-1)), (int)(t * (image.getHeight()-1)));
  }
}