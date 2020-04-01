
// Groovy Program to create a text editor
// Written by TBA

// TODO
//  Undo, Redo
//  Zoom
//  Save warning on close

import javax.swing.*
import java.awt.Font
import java.awt.event.*
import java.awt.Toolkit
import javax.swing.plaf.metal.*

class Editor extends JFrame implements ActionListener {

    JTextArea jTextArea
    JFrame jFrame

    def cmd = [file: "File",
               new: "New", open: "Open", save: "Save", print: "Print", close: "Close",
               edit: "Edit",
               copy: "Copy", paste: "Paste", cut: "Cut"]

    def msg = [title: "Editor",
               allGood: "All good.",
               errLookAndFeel: "Could not set look and feel and window theme."]

    def cfg = [currentDir: ".",
               width: 800,
               height: 600,
               fontName: "IBMPlexMono-Regular.ttf",
               fontSize: 12f,
               lookAndFeel: "javax.swing.plaf.metal.MetalLookAndFeel",
               theme: new OceanTheme()]

    // =======================================================================
    // Panic out of the program
    def panic(String str) {
        println str
        System.exit(0)
    }

    // =======================================================================
    Editor() {
        jFrame = new JFrame(msg.title)
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE)

        try {
            // Set metal look and feel
            UIManager.setLookAndFeel(cfg.lookAndFeel)

            // Set theme to ocean
            MetalLookAndFeel.setCurrentTheme(cfg.theme)
        } catch (Exception ex) {
            panic(msg.errLookAndFeel)
        }

        // Load font
        InputStream is = Editor.class.getResourceAsStream(cfg.fontName)
        Font font = Font.createFont(Font.TRUETYPE_FONT, is)
        Font sizedFont = font.deriveFont(cfg.fontSize)

        // -------------------------------------------------

        // Text component
        jTextArea = new JTextArea()
        jTextArea.setFont(sizedFont)
        def jScrollPane = new JScrollPane(jTextArea)

        // Create a menu bar
        def menuBar = new JMenuBar()

        // Create file menu --------------------------------
        def mFile = new JMenu(cmd.file)

        mFile.setMnemonic('F')

        // Create file menu items
        def miNew = new JMenuItem(cmd.new)
        def miOpen = new JMenuItem(cmd.open)
        def miSave = new JMenuItem(cmd.save)
        def miPrint = new JMenuItem(cmd.print)
        def miClose = new JMenuItem(cmd.close)

        miNew.setMnemonic('N')
        miOpen.setMnemonic('O')
        miSave.setMnemonic('S')
        miPrint.setMnemonic('P')
        miClose.setMnemonic('C')

        // getMenuShortcutKeyMaskEx() returns the system modifier key,
        // Ctrl on Windows and Linux, Cmd on Mac
        miNew.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))
        miOpen.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))
        miSave.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))
        miPrint.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))
        miClose.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK))

        // Add action listener
        miNew.addActionListener(this)
        miOpen.addActionListener(this)
        miSave.addActionListener(this)
        miPrint.addActionListener(this)
        miClose.addActionListener(this)

        mFile.add(miNew)
        mFile.add(miOpen)
        mFile.add(miSave)
        mFile.add(miPrint)
        mFile.add(miClose)

        // Create edit menu --------------------------------
        def mEdit = new JMenu(cmd.edit)

        mEdit.setMnemonic('E')

        // Create menu items
        def miCopy = new JMenuItem(cmd.copy)
        def miPaste = new JMenuItem(cmd.paste)
        def miCut = new JMenuItem(cmd.cut)

        miCopy.setMnemonic('C')
        miPaste.setMnemonic('P')
        miCut.setMnemonic('u')

        miCopy.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))
        miPaste.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))
        miCut.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()))

        // Add action listener
        miCopy.addActionListener(this)
        miPaste.addActionListener(this)
        miCut.addActionListener(this)

        mEdit.add(miCopy)
        mEdit.add(miPaste)
        mEdit.add(miCut)

        // -------------------------------------------------

        menuBar.add(mFile)
        menuBar.add(mEdit)

        jFrame.setJMenuBar(menuBar)
        jFrame.add(jScrollPane)
        jFrame.setSize(cfg.width, cfg.height)
        jFrame.setVisible(true)
    }

    // =======================================================================
    def openFile() {
        def jFileChooser = new JFileChooser(cfg.currentDir)

        int ret = jFileChooser.showOpenDialog(null)

        // If the user selects a file
        if (ret == JFileChooser.APPROVE_OPTION) {
            def file = new File(jFileChooser.getSelectedFile().getAbsolutePath())

            try {
                String line, textBuffer

                FileReader fileReader = new FileReader(file)
                BufferedReader bufReader = new BufferedReader(fileReader)

                // Read lines from the file
                textBuffer = bufReader.readLine()
                while ((line = bufReader.readLine()) != null) {
                    textBuffer += "\n" + line
                }

                // Set the text in the editor
                jTextArea.setText(textBuffer)
                // Cursor back to top
                jTextArea.setCaretPosition(0)
            } catch (Exception evt) {
                JOptionPane.showMessageDialog(jFrame, evt.getMessage())
            }
        }
    }

    // =======================================================================
    def saveFile() {
        def fileChooser = new JFileChooser(cfg.currentDir)

        int ret = fileChooser.showSaveDialog(null)

        if (ret == JFileChooser.APPROVE_OPTION) {
            def file = new File(fileChooser.getSelectedFile().getAbsolutePath())

            try {
                FileWriter fileWriter = new FileWriter(file, false)
                BufferedWriter bufWriter = new BufferedWriter(fileWriter)

                bufWriter.write(jTextArea.getText())

                bufWriter.flush()
                bufWriter.close()
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(jFrame, ex.getMessage())
            }
        }
    }

    // =======================================================================
    def printFile() {
        try {
            jTextArea.print()
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(jFrame, ex.getMessage())
        }
    }

    // =======================================================================
    def close() {
        jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING))
        panic(msg.allGood)
    }

    // =======================================================================
    // Menu functions
    void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand()

        switch(command) {
            case cmd.new: jTextArea.setText(""); break
            case cmd.open: openFile(); break
            case cmd.save: saveFile(); break
            case cmd.print: printFile(); break
            case cmd.close: close(); break

            case cmd.copy: jTextArea.copy(); break
            case cmd.paste: jTextArea.paste(); break
            case cmd.cut: jTextArea.cut(); break
        }
    }

    static void main(String... args) {
        def editor = new Editor()
    }
}
