
// A basic text editor in Groovy
// Written by TBA

// TODO
//  Undo, Redo
//  Search, Replace
//  Save warning on close
//  More editor functions (may require more advanced input-handling)
//  Zoom in/out with Ctrl+Mouse wheel
//  Persistent text highlighter
//  Command line support
// TODO 2.0
//  File browser
//  Multiple editor tabs

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.CaretStyle
import org.fife.ui.rtextarea.ConfigurableCaret
import org.fife.ui.rtextarea.RTextScrollPane

import javax.swing.*
import java.awt.Font
import java.awt.event.*
import java.awt.Toolkit
import javax.swing.plaf.metal.*

class EditorRSyntax extends JFrame implements ActionListener {

    RSyntaxTextArea rSyntaxTextArea
    JFrame jFrame
    Font font

    def cmd = [file: "File",
               new: "New", open: "Open", save: "Save", print: "Print", close: "Close",
               edit: "Edit",
               copy: "Copy", paste: "Paste", cut: "Cut",
               preferences: "Preferences",
               zoomIn: "Zoom in", zoomOut: "Zoom out", syntaxHighlight: "Toggle syntax highlighting"]

    def msg = [title: "Editor",
               allGood: "All good.",
               errLookAndFeel: "Could not set look and feel and window theme."]

    def cfg = [currentDir: ".",
               width: 1200,
               height: 825,
               fontName: "IBMPlexMono-Regular.ttf",
               fontSize: 13f,
               lookAndFeel: "javax.swing.plaf.metal.MetalLookAndFeel",
               theme: new OceanTheme(),
               syntaxHighlight: true,
               tabWidth: 4,
               tabEmulated: true,
               wordWrap: true,
               codeFolding: false]

    // =======================================================================
    // Panic out of the program
    def panic(String str) {
        println str
        System.exit(0)
    }

    // =======================================================================
    EditorRSyntax() {
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
        InputStream is = EditorRSyntax.class.getResourceAsStream(cfg.fontName)
        font = Font.createFont(Font.TRUETYPE_FONT, is)
        Font sizedFont = font.deriveFont(cfg.fontSize)

        // -------------------------------------------------

        // Text component
        rSyntaxTextArea = new RSyntaxTextArea()
        // Basic config
        rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY)
        rSyntaxTextArea.setCodeFoldingEnabled(cfg.codeFolding)
        rSyntaxTextArea.setFont(sizedFont)
        rSyntaxTextArea.setTabSize(cfg.tabWidth)
        rSyntaxTextArea.setTabsEmulated(cfg.tabEmulated)
        rSyntaxTextArea.setLineWrap(cfg.wordWrap)
        rSyntaxTextArea.setCaretStyle(RSyntaxTextArea.INSERT_MODE, CaretStyle.BLOCK_STYLE)
        // TODO: Custom scheme, B&W, bold highlighting, common elements like numbers,
        //  braces, maybe strings
        def rTextScrollPane = new RTextScrollPane(rSyntaxTextArea)

        //==================================================
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

        // Create preferences menu -------------------------
        def mPreferences = new JMenu(cmd.preferences)

        mPreferences.setMnemonic('P')

        // Create menu items
        def miZoomIn = new JMenuItem(cmd.zoomIn)
        def miZoomOut = new JMenuItem(cmd.zoomOut)

        miZoomIn.setMnemonic('I')
        miZoomOut.setMnemonic('O')

        // For the sake of supporting most keyboard sizes and layouts, Alt+I and Alt+O
        miZoomIn.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK))
        miZoomOut.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK))

        // Add action listener
        miZoomIn.addActionListener(this)
        miZoomOut.addActionListener(this)

        mPreferences.add(miZoomIn)
        mPreferences.add(miZoomOut)

        // -------------------------------------------------

        menuBar.add(mFile)
        menuBar.add(mEdit)
        menuBar.add(mPreferences)

        //==================================================

        jFrame.setJMenuBar(menuBar)
        jFrame.add(rTextScrollPane)
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
                textBuffer += "\n"

                // Set the text in the editor
                rSyntaxTextArea.setText(textBuffer)
                // Cursor back to top
                rSyntaxTextArea.setCaretPosition(0)
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

                bufWriter.write(rSyntaxTextArea.getText())

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
            rSyntaxTextArea.print()
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
    def zoomIn() {
        cfg.fontSize += 1f
        Font sizedFont = font.deriveFont((float) cfg.fontSize)
        rSyntaxTextArea.setFont(sizedFont)
    }

    // =======================================================================
    def zoomOut() {
        if (cfg.fontSize >= 6f) cfg.fontSize -= 1f
        Font sizedFont = font.deriveFont((float) cfg.fontSize)
        rSyntaxTextArea.setFont(sizedFont)
    }

    // =======================================================================
    // Menu functions
    void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand()

        switch(command) {
            case cmd.new: rSyntaxTextArea.setText(""); break
            case cmd.open: openFile(); break
            case cmd.save: saveFile(); break
            case cmd.print: printFile(); break
            case cmd.close: close(); break

            case cmd.copy: rSyntaxTextArea.copy(); break
            case cmd.paste: rSyntaxTextArea.paste(); break
            case cmd.cut: rSyntaxTextArea.cut(); break

            case cmd.zoomIn: zoomIn(); break
            case cmd.zoomOut: zoomOut(); break
        }
    }

    static void main(String... args) {
        def editor = new EditorRSyntax()
    }

}
