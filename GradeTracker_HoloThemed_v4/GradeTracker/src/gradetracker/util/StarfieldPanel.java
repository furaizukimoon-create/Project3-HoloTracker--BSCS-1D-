package gradetracker.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * StarfieldPanel — An animated star-field background panel.
 * Used as the base layer for all screens to create the
 * deep-space hololive aesthetic.
 *
 * Stars twinkle and slowly drift downward.
 * A radial gradient provides a subtle nebula glow.
 */
public class StarfieldPanel extends JPanel {

    private static final int STAR_COUNT  = 180;
    private static final int FPS         = 30;

    private final float[] starX, starY, starSize, starAlpha, starSpeed;
    private final Color[] starColors;
    private final Timer   ticker;
    private final Random  rng = new Random();

    public StarfieldPanel() {
        starX     = new float[STAR_COUNT];
        starY     = new float[STAR_COUNT];
        starSize  = new float[STAR_COUNT];
        starAlpha = new float[STAR_COUNT];
        starSpeed = new float[STAR_COUNT];
        starColors = new Color[STAR_COUNT];

        for (int i = 0; i < STAR_COUNT; i++) initStar(i, true);

        ticker = new Timer(1000 / FPS, (ActionEvent e) -> {
            for (int i = 0; i < STAR_COUNT; i++) {
                starY[i] += starSpeed[i];
                // twinkle
                starAlpha[i] += (rng.nextFloat() - 0.5f) * 0.04f;
                starAlpha[i] = Math.max(0.1f, Math.min(1f, starAlpha[i]));
                // respawn at top when off-screen
                if (starY[i] > getHeight()) initStar(i, false);
            }
            repaint();
        });
        ticker.start();
        setOpaque(true);
    }

    private void initStar(int i, boolean randomY) {
        starX[i]     = rng.nextFloat() * 2000;          // spread wider than screen
        starY[i]     = randomY ? rng.nextFloat() * 2000 : 0;
        starSize[i]  = 0.5f + rng.nextFloat() * 2.5f;
        starAlpha[i] = 0.3f + rng.nextFloat() * 0.7f;
        starSpeed[i] = 0.1f + rng.nextFloat() * 0.4f;
        // mostly white/blue, occasional lavender or gold
        int pick = rng.nextInt(10);
        if      (pick < 6) starColors[i] = HoloTheme.TEXT_MAIN;
        else if (pick < 8) starColors[i] = HoloTheme.ACCENT;
        else if (pick < 9) starColors[i] = HoloTheme.ACCENT2;
        else               starColors[i] = HoloTheme.GOLD;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        // deep space gradient
        GradientPaint bg = new GradientPaint(0, 0, HoloTheme.BG_DEEP, w, h, new Color(15, 5, 30));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // nebula glow in center-left
        RadialGradientPaint nebula = new RadialGradientPaint(
            new Point(w / 3, h / 2), w / 2,
            new float[]{ 0f, 1f },
            new Color[]{ new Color(60, 20, 90, 40), new Color(0, 0, 0, 0) }
        );
        g2.setPaint(nebula);
        g2.fillRect(0, 0, w, h);

        // second nebula (top-right)
        RadialGradientPaint nebula2 = new RadialGradientPaint(
            new Point(w * 3/4, h / 4), w / 3,
            new float[]{ 0f, 1f },
            new Color[]{ new Color(20, 60, 100, 30), new Color(0, 0, 0, 0) }
        );
        g2.setPaint(nebula2);
        g2.fillRect(0, 0, w, h);

        // draw stars
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < STAR_COUNT; i++) {
            float sx = starX[i] % w;
            float sy = starY[i] % h;
            Color sc = starColors[i];
            g2.setColor(new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), (int)(starAlpha[i] * 255)));
            float sz = starSize[i];
            g2.fillOval((int)(sx - sz/2), (int)(sy - sz/2), (int)sz, (int)sz);
        }
        g2.dispose();
    }

    public void stopAnimation() { ticker.stop(); }
}
