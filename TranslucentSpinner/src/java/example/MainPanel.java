// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final TexturePaint TEXTURE = TextureUtils.createCheckerTexture(4, new Color(0xEE_EE_EE));

  private MainPanel() {
    super(new BorderLayout());
    UIDefaults d = new UIDefaults();
    Painter<JComponent> painter1 = (g, c, w, h) -> {
      g.setColor(new Color(100, 100, 100, 100));
      g.fillRect(0, 0, w, h);
    };
    Painter<JComponent> painter2 = (g, c, w, h) -> {
      g.setColor(new Color(100, 200, 200, 100));
      g.fillRect(0, 0, w, h);
    };
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Enabled].backgroundPainter", painter1);
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Focused].backgroundPainter", painter2);
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Selected].backgroundPainter", painter2);
    // d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Focused+Selected].backgroundPainter", painter2);
    // d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Disabled].backgroundPainter", painter);

    Painter<JComponent> painter3 = (g, c, w, h) -> {
      g.setColor(new Color(100, 100, 200, 100));
      g.fillRect(0, 0, w, h);
    };
    Painter<JComponent> painter4 = (g, c, w, h) -> {
      g.setColor(new Color(120, 120, 120, 100));
      g.fillRect(0, 0, w, h);
    };
    d.put("Spinner:\"Spinner.previousButton\"[Enabled].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.previousButton\"[Focused+MouseOver].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.previousButton\"[Focused+Pressed].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.previousButton\"[Focused].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.previousButton\"[MouseOver].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.previousButton\"[Pressed].backgroundPainter", painter4);

    d.put("Spinner:\"Spinner.nextButton\"[Enabled].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.nextButton\"[Focused+MouseOver].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.nextButton\"[Focused+Pressed].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.nextButton\"[Focused].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.nextButton\"[MouseOver].backgroundPainter", painter3);
    d.put("Spinner:\"Spinner.nextButton\"[Pressed].backgroundPainter", painter4);

    SpinnerModel model = new SpinnerNumberModel(0, 0, 100, 5);
    JSpinner spinner1 = new JSpinner(model);
    // NG: spinner1.putClientProperty("Nimbus.Overrides", d);
    ((JSpinner.DefaultEditor) spinner1.getEditor()).getTextField().putClientProperty("Nimbus.Overrides", d);
    configureSpinnerButtons(spinner1, d);

    JSpinner spinner2 = new JSpinner(model) {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new Color(0x64_FF_00_00, true));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    spinner2.setOpaque(false);
    spinner2.getEditor().setOpaque(false);
    ((JSpinner.DefaultEditor) spinner2.getEditor()).getTextField().setOpaque(false);

    JPanel p = new JPanel(new GridLayout(0, 1, 20, 20));
    p.setOpaque(false);
    p.add(new JSpinner(model));
    p.add(spinner1);
    p.add(spinner2);
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void configureSpinnerButtons(Container comp, UIDefaults d) {
    for (Component c: comp.getComponents()) {
      String name = c.getName();
      if (name.endsWith("Button")) {
        ((JButton) c).putClientProperty("Nimbus.Overrides", d);
      } else if (c instanceof Container) {
        configureSpinnerButtons((Container) c, d);
      }
    }
  }

  @Override public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(TEXTURE);
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class TextureUtils {
  private TextureUtils() {
    /* HideUtilityClassConstructor */
  }

  public static TexturePaint createCheckerTexture(int cs, Color color) {
    int size = cs * cs;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color.brighter());
    g2.fillRect(0, 0, size, size);
    g2.setPaint(color);
    for (int i = 0; i * cs < size; i++) {
      for (int j = 0; j * cs < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }
}
