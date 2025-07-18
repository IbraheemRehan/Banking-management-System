package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;

public class RoundedToggleButtonUI extends BasicToggleButtonUI {
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Graphics2D g2 = (Graphics2D) g.create();

        // Anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Use button's background color
        g2.setColor(b.getBackground());

        // Draw rounded rect (pill shape)
        g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 40, 40);

        // Draw text centered
        FontMetrics fm = g.getFontMetrics();
        String text = b.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int x = (b.getWidth() - textWidth) / 2;
        int y = (b.getHeight() + textHeight) / 2 - 3;

        g2.setColor(b.getForeground());
        g2.drawString(text, x, y);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(120, 40);
    }
}
