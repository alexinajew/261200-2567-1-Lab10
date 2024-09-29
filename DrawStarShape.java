import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class DrawStarShape {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DrawStarShape::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DrawStarShapePanel panel = new DrawStarShapePanel();
        f.getContentPane().add(panel);
        f.setSize(600, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        // Start drawing the shapes concurrently
        panel.startDrawingShapes();
    }
}

class DrawStarShapePanel extends JPanel {
    private Shape shape1;
    private Shape shape2;
    private Shape shape3;

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the shapes only when they are ready
        if (shape1 != null) {
            g.setColor(Color.BLUE); // Added color to shape1
            g.fill(shape1); // Fill the shape
            g.setColor(Color.BLACK);
            g.draw(shape1); // Draw the outline
        }

        if (shape2 != null) {
            g.setColor(Color.RED); // Color for shape2
            g.fill(shape2);
        }

        if (shape3 != null) {
            g.setPaint(new RadialGradientPaint(
                new Point(200, 400), 50, new float[] { 0, 0.3f, 1 },
                new Color[] { Color.RED, Color.YELLOW, Color.ORANGE }));
            g.fill(shape3);
        }
    }

    public void startDrawingShapes() {
        // Use SwingWorkers to draw shapes with delays
        SwingWorker<Shape, Void> worker1 = new SwingWorker<Shape, Void>() {
            @Override
            protected Shape doInBackground() throws Exception {
                return createDefaultStar(50, 200, 200);
            }

            @Override
            protected void done() {
                try {
                    shape1 = get();
                    repaint(); // Trigger a repaint when done
                    Thread.sleep(1000); // Wait for 1 second before drawing the next shape
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        SwingWorker<Shape, Void> worker2 = new SwingWorker<Shape, Void>() {
            @Override
            protected Shape doInBackground() throws Exception {
                return createStar(400, 400, 40, 60, 10, 0);
            }

            @Override
            protected void done() {
                try {
                    shape2 = get();
                    repaint(); // Trigger a repaint when done
                    Thread.sleep(1000); // Wait for 1 second before drawing the next shape
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        SwingWorker<Shape, Void> worker3 = new SwingWorker<Shape, Void>() {
            @Override
            protected Shape doInBackground() throws Exception {
                return createStar(200, 400, 40, 50, 20, 0);
            }

            @Override
            protected void done() {
                try {
                    shape3 = get();
                    repaint(); // Trigger a repaint when done
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Execute the workers
        worker1.execute();
        worker1.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                worker2.execute(); // Start worker2 after worker1 is done
            }
        });

        worker2.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                worker3.execute(); // Start worker3 after worker2 is done
            }
        });

        worker3.execute(); // Start worker3 directly after worker2
    }

    private static Shape createDefaultStar(double radius, double centerX, double centerY) {
        return createStar(centerX, centerY, radius, radius * 2.63, 5, Math.toRadians(-18));
    }

    private static Shape createStar(double centerX, double centerY, double innerRadius, double outerRadius, int numRays, double startAngleRad) {
        Path2D path = new Path2D.Double();
        double deltaAngleRad = Math.PI / numRays;
        for (int i = 0; i < numRays * 2; i++) {
            double angleRad = startAngleRad + i * deltaAngleRad;
            double ca = Math.cos(angleRad);
            double sa = Math.sin(angleRad);
            double relX = ca;
            double relY = sa;
            if ((i & 1) == 0) {
                relX *= outerRadius;
                relY *= outerRadius;
            } else {
                relX *= innerRadius;
                relY *= innerRadius;
            }
            if (i == 0) {
                path.moveTo(centerX + relX, centerY + relY);
            } else {
                path.lineTo(centerX + relX, centerY + relY);
            }
        }
        path.closePath();
        return path;
    }
}
