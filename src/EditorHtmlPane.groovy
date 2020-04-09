
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

// TODO JTextArea --> JEditorPane
//  HTML --> text on save
//  Cursor location fix at the end of highlighting process, in edge cases
//  Optional: scroll location fix on text changing events

import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.Font
import java.awt.event.*
import java.awt.Toolkit
import javax.swing.plaf.metal.*
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4

class EditorHtmlPane extends JFrame implements ActionListener {

    JFrame jFrame
    Font font
    JEditorPane jEditorPane
    JScrollPane jScrollPane

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
               caretWidth: 2,
               syntaxHighlight: true,
               tabWidth: 4]

    def nbsp = "&nbsp;"
    def tabString = nbsp * cfg.tabWidth

    // =======================================================================
    // Invokes various tasks on text change, e.g. syntax highlighter
    class EditorDocumentListener implements DocumentListener {

        // Used to switch off event processing while a callback is already running
        private static boolean active = false

        @Override
        public void insertUpdate(DocumentEvent e) {
            changed e
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed e
        }

        // Plain text components don't fire these events
        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        // Our on text changed event handler
        def changed(DocumentEvent e) {
            if (!cfg.syntaxHighlight || EditorDocumentListener.active) {
                return
            } else {
                EditorDocumentListener.active = true
            }
            Runnable doHighlight = new Runnable() {
                @Override
                public void run() {
                    generalSyntaxHighlight()
                    EditorDocumentListener.active = false
                }
            }
            // Because it is Swing
            // invokeLater(Runnable) postpones execution until the Document lock gets released
            SwingUtilities.invokeLater(doHighlight)
        }
    }

    // =======================================================================
    // Panic out of the program
    def panic(String str) {
        println str
        System.exit(0)
    }

    // =======================================================================
    EditorHtmlPane() {
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
        InputStream is = EditorHtmlPane.class.getResourceAsStream(cfg.fontName)
        font = Font.createFont(Font.TRUETYPE_FONT, is)
        Font sizedFont = font.deriveFont(cfg.fontSize)

        // -------------------------------------------------

        // Text component
        jEditorPane = new JEditorPane("text/html", "")
        jEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE)
        jEditorPane.setFont(sizedFont)
        jEditorPane.putClientProperty("caretWidth", cfg.caretWidth)
        jEditorPane.getDocument().addDocumentListener(new EditorDocumentListener())

        jScrollPane = new JScrollPane(jEditorPane)

        // =================================================
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
        def miSyntaxHighlight = new JMenuItem(cmd.syntaxHighlight)

        miZoomIn.setMnemonic('I')
        miZoomOut.setMnemonic('O')
        miSyntaxHighlight.setMnemonic('H')

        // For the sake of supporting most keyboard sizes and layouts, Alt+I and Alt+O
        miZoomIn.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK))
        miZoomOut.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK))
        miSyntaxHighlight.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK))

        // Add action listener
        miZoomIn.addActionListener(this)
        miZoomOut.addActionListener(this)
        miSyntaxHighlight.addActionListener(this)

        mPreferences.add(miZoomIn)
        mPreferences.add(miZoomOut)
        mPreferences.add(miSyntaxHighlight)

        // -------------------------------------------------

        menuBar.add(mFile)
        menuBar.add(mEdit)
        menuBar.add(mPreferences)

        // =================================================

        jFrame.setJMenuBar(menuBar)
        jFrame.add(jScrollPane)
        jFrame.setSize(cfg.width, cfg.height)
        jFrame.setVisible(true)
    }

    // =======================================================================
    // Only special char management
    String text2HtmlSpecialChars(line) {
        line.replaceAll(/ /, nbsp)
                .replaceAll(/\t/, tabString)
                .replaceAll(/\n/, "<br>")
    }

    // =======================================================================
    // Only special char management
//    String html2TextSpecialChars(String html) {
//        br2NewLine(html)
//                // .replaceAll(/&nbsp;/, " ")
//    }


    // =======================================================================
    // Used for text highlighting
    def strong(def str) {
        def text
        text = str
        if (str instanceof List) text = str.get(0)
        "<strong>" + text + "</strong>"
    }

    // =======================================================================
    // Highlights a roughly common set of programming and data elements in the
    // text for better visibility.
    def generalSyntaxHighlight() {
        def text
        def position

        text = jEditorPane.getText()
        position = jEditorPane.getCaretPosition()

        // First remove all styling
        text = text
                .replaceAll(/<strong>/, "")
                .replaceAll(/<\/strong>/, "")
        // Then if highlighting is enabled, reparse the text
        if (cfg.syntaxHighlight) {
            // Bold / strong highlighting

            // Any decimal number
            //  including Java floats, doubles and scientific notation
            //  minus &#nnnnn; strings
            def decNumP = ~/(?<=[\W])(?<![(&#\d{0,3})-])-?\d+(\.\d+)*([fFdD]|[eE]\d+)?(?!\d{0,3};)\b/
            // Braces
            def braceP = ~/[\(\)\{}\[\]]/
            // Common structural programming words from various languages
            def reservedWordsP = ~/\b(def|let|var|final|class|interface|implementation|function|procedure|begin|end)\b/

            text = text
                    .replaceAll(decNumP, { it -> strong(it) })
                    .replaceAll(braceP, { it -> strong(it) })
                    .replaceAll(reservedWordsP, { it -> strong(it) })
        }

        jEditorPane.setText(text)
        jEditorPane.setCaretPosition(position)
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
                    textBuffer += "\n" + escapeHtml4(line)
                }
                // To add a newline to the last line, the space will be converted to &nbsp;
                textBuffer += "\n\n"

                textBuffer = text2HtmlSpecialChars(textBuffer)

                // Set the text in the editor
                jEditorPane.setText(textBuffer)

                // Highlight special text elements
                if (cfg.syntaxHighlight) generalSyntaxHighlight()

                // Cursor back to top
                jEditorPane.setCaretPosition(0)
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

                bufWriter.write(jEditorPane.getText())

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
            jEditorPane.print()
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
        jEditorPane.setFont(sizedFont)
    }

    // =======================================================================
    def zoomOut() {
        if (cfg.fontSize >= 6f) cfg.fontSize -= 1f
        Font sizedFont = font.deriveFont((float) cfg.fontSize)
        jEditorPane.setFont(sizedFont)
    }

    // =======================================================================
    // Menu item callback
    def toggleSyntaxHighlight() {
        if (cfg.syntaxHighlight) {
            // Turn off
            cfg.syntaxHighlight = false
        } else {
            // Turn on
            cfg.syntaxHighlight = true
        }
        generalSyntaxHighlight()
    }

    // =======================================================================
    // Menu functions
    void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand()

        switch(command) {
            case cmd.new: jEditorPane.setText(""); break
            case cmd.open: openFile(); break
            case cmd.save: saveFile(); break
            case cmd.print: printFile(); break
            case cmd.close: close(); break

            case cmd.copy: jEditorPane.copy(); break
            case cmd.paste: jEditorPane.paste(); break
            case cmd.cut: jEditorPane.cut(); break

            case cmd.zoomIn: zoomIn(); break
            case cmd.zoomOut: zoomOut(); break
            case cmd.syntaxHighlight: toggleSyntaxHighlight(); break
        }
    }

    static void main(String... args) {
        def editor = new EditorHtmlPane()
    }
}
