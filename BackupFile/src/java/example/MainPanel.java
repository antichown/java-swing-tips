// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private static final String FILE_NAME = "example.txt";
  private final SpinnerNumberModel model1 = new SpinnerNumberModel(0, 0, 6, 1);
  private final SpinnerNumberModel model2 = new SpinnerNumberModel(2, 0, 6, 1);
  private final JLabel label = new JLabel("2", SwingConstants.RIGHT);
  private final JTextPane jtp = new JTextPane();

  private MainPanel() {
    super(new BorderLayout());
    jtp.setEditable(false);
    StyledDocument doc = jtp.getStyledDocument();
    // Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
    // Style regular = doc.addStyle(MessageType.REGULAR.toString(), def);
    // StyleConstants.setForeground(error, Color.BLACK);
    // Style error = doc.addStyle(ERROR, regular);
    StyleConstants.setForeground(doc.addStyle(MessageType.ERROR.toString(), def), Color.RED);
    StyleConstants.setForeground(doc.addStyle(MessageType.BLUE.toString(), def), Color.BLUE);

    JButton ok = new JButton("Create new " + FILE_NAME);
    ok.addActionListener(e -> {
      File file = new File(System.getProperty("java.io.tmpdir"), FILE_NAME);
      new BackgroundTask(file, model1.getNumber().intValue(), model2.getNumber().intValue()) {
        @Override protected void process(List<Message> chunks) {
          if (isCancelled()) {
            return;
          }
          if (!isDisplayable()) {
            cancel(true);
            return;
          }
          for (Message m: chunks) {
            append(m);
          }
        }

        @Override public void done() {
          try {
            File nf = get();
            if (Objects.isNull(nf)) {
              append(new Message("バックアップファイルの生成に失敗しました。", MessageType.ERROR));
            } else if (nf.createNewFile()) {
              append(new Message(nf.getName() + "を生成しました。", MessageType.REGULAR));
            } else {
              append(new Message(nf.getName() + "の生成に失敗しました。", MessageType.ERROR));
            }
          } catch (InterruptedException ex) {
            append(new Message(ex.getMessage(), MessageType.ERROR));
            Thread.currentThread().interrupt();
          } catch (ExecutionException | IOException ex) {
            append(new Message(ex.getMessage(), MessageType.ERROR));
          }
          append(new Message("----------------------------------", MessageType.REGULAR));
        }
      }.execute();
    });

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> jtp.setText(""));

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    box.add(Box.createHorizontalGlue());
    box.add(ok);
    box.add(Box.createHorizontalStrut(5));
    box.add(clear);

    JSpinner spinner1 = new JSpinner(model1);
    JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0");
    editor1.getTextField().setEditable(false);
    spinner1.setEditor(editor1);

    JSpinner spinner2 = new JSpinner(model2);
    JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2, "0");
    editor2.getTextField().setEditable(false);
    spinner2.setEditor(editor2);

    ChangeListener cl = e -> label.setText(
        Objects.toString(model1.getNumber().intValue() + model2.getNumber().intValue()));
    model1.addChangeListener(cl);
    model2.addChangeListener(cl);

    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

    // Box northBox = Box.createHorizontalBox();
    JPanel northBox = new JPanel(new GridLayout(3, 2, 5, 5));
    northBox.add(new JLabel("削除しないバックアップの数:", SwingConstants.RIGHT));
    northBox.add(spinner1);
    northBox.add(new JLabel("順に削除するバックアップの数:", SwingConstants.RIGHT));
    northBox.add(spinner2);
    northBox.add(new JLabel("合計バックアップ数:", SwingConstants.RIGHT));
    northBox.add(label);

    JScrollPane scroll = new JScrollPane(jtp);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);

    add(northBox, BorderLayout.NORTH);
    add(scroll);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  protected void append(Message m) {
    StyledDocument doc = jtp.getStyledDocument();
    try {
      doc.insertString(doc.getLength(), m.text + "\n", doc.getStyle(m.type.toString()));
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

enum MessageType {
  REGULAR, ERROR, BLUE
}

class Message {
  public final String text;
  public final MessageType type;

  protected Message(String text, MessageType type) {
    this.text = text;
    this.type = type;
  }
}

class BackgroundTask extends SwingWorker<File, Message> {
  private final File orgFile;
  private final int intold;
  private final int intnew;

  protected BackgroundTask(File file, int intold, int intnew) {
    super();
    this.orgFile = file;
    this.intold = intold;
    this.intnew = intnew;
  }

  @Override public File doInBackground() throws IOException {
    if (!orgFile.exists()) {
      return orgFile;
    }

    String newfilename = orgFile.getAbsolutePath();

    if (intold == 0 && intnew == 0) { // = backup off
      if (orgFile.delete()) {
        return new File(newfilename);
      } else {
        publish(new Message("古いバックアップファイル削除に失敗", MessageType.ERROR));
        return null;
      }
    }

    File tmpFile = renameAndBackup(orgFile, newfilename);
    if (Objects.nonNull(tmpFile)) {
      return tmpFile;
    }

    if (renameAndShiftBackup(orgFile)) {
      return new File(newfilename);
    } else {
      return null;
    }
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private File renameAndBackup(File file, String newfilename) throws IOException {
    boolean simpleRename = false;
    File testFile = null;
    for (int i = 1; i <= intold; i++) {
      testFile = new File(file.getParentFile(), makeBackupFileName(file.getName(), i));
      if (!testFile.exists()) {
        simpleRename = true;
        break;
      }
    }
    if (!simpleRename) {
      for (int i = intold + 1; i <= intold + intnew; i++) {
        testFile = new File(file.getParentFile(), makeBackupFileName(file.getName(), i));
        if (!testFile.exists()) {
          simpleRename = true;
          break;
        }
      }
    }
    if (simpleRename) {
      if (file.renameTo(testFile)) {
        publish(new Message("古い同名ファイルをリネーム", MessageType.REGULAR));
        publish(new Message(String.format("  %s -> %s", file.getName(), testFile.getName()), MessageType.BLUE));
        return new File(newfilename);
      } else {
        publish(new Message("ファイルのリネームに失敗", MessageType.ERROR));
        throw new IOException();
      }
    }
    return null;
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private boolean renameAndShiftBackup(File file) {
    File tmpFile3 = new File(file.getParentFile(), makeBackupFileName(file.getName(), intold + 1));
    publish(new Message("古いバックアップファイルを削除", MessageType.REGULAR));
    publish(new Message("  del:" + tmpFile3.getAbsolutePath(), MessageType.BLUE));
    if (!tmpFile3.delete()) {
      publish(new Message("古いバックアップファイル削除に失敗", MessageType.ERROR));
      return false;
    }
    for (int i = intold + 2; i <= intold + intnew; i++) {
      File tmpFile1 = new File(file.getParentFile(), makeBackupFileName(file.getName(), i));
      File tmpFile2 = new File(file.getParentFile(), makeBackupFileName(file.getName(), i - 1));
      if (!tmpFile1.renameTo(tmpFile2)) {
        publish(new Message("ファイルのリネームに失敗", MessageType.ERROR));
        return false;
      }
      publish(new Message("古いバックアップファイルの番号を更新", MessageType.REGULAR));
      publish(new Message("  " + tmpFile1.getName() + " -> " + tmpFile2.getName(), MessageType.BLUE));
    }
    File tmpFile = new File(file.getParentFile(), makeBackupFileName(file.getName(), intold + intnew));
    publish(new Message("古い同名ファイルをリネーム", MessageType.REGULAR));
    publish(new Message("  " + file.getName() + " -> " + tmpFile.getName(), MessageType.BLUE));
    if (!file.renameTo(tmpFile)) {
      publish(new Message("ファイルのリネームに失敗", MessageType.ERROR));
      return false;
    }
    return true;
  }

  private static String makeBackupFileName(String name, int num) {
    return String.format("%s.%d~", name, num);
  }
}
