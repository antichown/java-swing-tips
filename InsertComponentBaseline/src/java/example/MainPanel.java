package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        JTextPane textPane = new JTextPane();
        //textPane.setEditable(false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        textPane.replaceSelection(" Default: ");
        textPane.insertComponent(new JCheckBox("CheckBox"));

        JCheckBox check1 = new JCheckBox("JComponent#setAlignmentY(...)");
        Dimension d = check1.getPreferredSize();
        int baseline = check1.getBaseline(d.width, d.height);
        //System.out.println(check1.getAlignmentY());
        check1.setAlignmentY(baseline/(float)d.height);
        textPane.replaceSelection("\n\n Baseline: ");
        textPane.insertComponent(check1);

        JCheckBox check2 = new JCheckBox("setAlignmentY+setCursor+...");
        check2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        check2.setOpaque(false);
        check2.setFocusable(false);
        d = check2.getPreferredSize();
        baseline = check2.getBaseline(d.width, d.height);
        check2.setAlignmentY(baseline/(float)d.height);
        textPane.replaceSelection("\n\n Baseline+Cursor: ");
        textPane.insertComponent(check2);

        add(new JScrollPane(textPane));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
