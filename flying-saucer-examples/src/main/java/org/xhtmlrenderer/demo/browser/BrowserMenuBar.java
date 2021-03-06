/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.demo.browser;

import org.xhtmlrenderer.demo.browser.actions.ZoomAction;
import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.util.Uu;

import com.github.danfickle.flyingsaucer.swing.DOMInspector;
import com.github.danfickle.flyingsaucer.swing.ScalableXHTMLPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserMenuBar extends JMenuBar {
    /**
     * Description of the Field
     */
    BrowserStartup root;

    /**
     * Description of the Field
     */
    JMenu file;
    /**
     * Description of the Field
     */
    JMenu edit;
    /**
     * Description of the Field
     */
    JMenu view;
    /**
     * Description of the Field
     */
    JMenu go;
    /**
     * Description of the Field
     */
    JMenuItem view_source;
    /**
     * Description of the Field
     */
    JMenu debug;
    /**
     * Description of the Field
     */
    JMenu demos;
    /**
     *
     */
    private String lastDemoOpened;

    /**
     * Description of the Field
     */
    private Map<String, String> allDemos;
    private JMenu help;

    /**
     * Constructor for the BrowserMenuBar object
     *
     * @param root PARAM
     */
    public BrowserMenuBar(final BrowserStartup root) {
        this.root = root;
    }

    /**
     * Description of the Method
     */
    public void init() {
        file = new JMenu("Browser");
        file.setMnemonic('B');

        debug = new JMenu("Debug");
        debug.setMnemonic('U');

        demos = new JMenu("Demos");
        demos.setMnemonic('D');

        view = new JMenu("View");
        view.setMnemonic('V');

        help = new JMenu("Help");
        help.setMnemonic('H');

        view_source = new JMenuItem("Page Source");
        view_source.setEnabled(false);
        view.add(root.actions.stop);
        view.add(root.actions.refresh);
        view.add(root.actions.reload);
        view.add(new JSeparator());
        final JMenu text_size = new JMenu("Text Size");
        text_size.setMnemonic('T');
        text_size.add(root.actions.increase_font);
        text_size.add(root.actions.decrease_font);
        text_size.add(new JSeparator());
        text_size.add(root.actions.reset_font);
        view.add(text_size);

        go = new JMenu("Go");
        go.setMnemonic('G');
    }


    /**
     * Description of the Method
     */
    public void createLayout() {
        final ScalableXHTMLPanel panel = root.panel.view;

        file.add(root.actions.open_file);
        file.add(new JSeparator());
        file.add(root.actions.export_pdf);
        file.add(new JSeparator());
        file.add(root.actions.quit);
        add(file);

        /*
        // TODO: we can get the document and format it, but need syntax highlighting
        // and a tab or separate window, dialog, etc.
        view_source.setAction(new ViewSourceAction(panel));
        view.add(view_source);
        */

        final JMenu zoom = new JMenu("Zoom");
        zoom.setMnemonic('Z');
        final ScaleFactor[] factors = this.initializeScales();
        final ButtonGroup zoomGroup = new ButtonGroup();
        for (final ScaleFactor factor : factors) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(new ZoomAction(panel, factor));

            if (factor.isNotZoomed()) item.setSelected(true);

            zoomGroup.add(item);
            zoom.add(item);
        }
        view.add(new JSeparator());
        view.add(zoom);
        view.add(new JSeparator());
        view.add(new JCheckBoxMenuItem(root.actions.print_preview));
        add(view);

        go.add(root.actions.forward);
        go.add(root.actions.backward);

        add(go);

        demos.add(new NextDemoAction());
        demos.add(new PriorDemoAction());
        demos.add(new JSeparator());
        allDemos = new LinkedHashMap<String, String>();

        populateDemoList();

        for (final String string : allDemos.keySet()) {
            final String s = (String) string;
            demos.add(new LoadAction(s, allDemos.get(s)));
        }

        add(demos);

        final JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');

        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));
        debugShow.add(new JCheckBoxMenuItem(new FontMetricsAction()));

        final JMenu anti = new JMenu("Anti Aliasing");
        final ButtonGroup anti_level = new ButtonGroup();
        addLevel(anti, anti_level, "None", -1);
        addLevel(anti, anti_level, "Low", 25).setSelected(true);
        addLevel(anti, anti_level, "Medium", 12);
        addLevel(anti, anti_level, "High", 0);
        debug.add(anti);


        debug.add(new ShowDOMInspectorAction());
        debug.add(new AbstractAction("Validation Console") {
            public void actionPerformed(final ActionEvent evt) {
                if (root.validation_console == null) {
                    root.validation_console = new JFrame("Validation Console");
                    final JFrame frame = root.validation_console;
                    final JTextArea jta = new JTextArea();

                    //root.error_handler.setTextArea(jta);

                    jta.setEditable(false);
                    jta.setLineWrap(true);
                    jta.setText("Validation Console: XML Parsing Error Messages");

                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(new JScrollPane(jta), "Center");
                    final JButton close = new JButton("Close");
                    frame.getContentPane().add(close, "South");
                    close.addActionListener(new ActionListener() {
                        public void actionPerformed(final ActionEvent evt) {
                            root.validation_console.setVisible(false);
                        }
                    });

                    frame.pack();
                    frame.setSize(400, 300);
                }
                root.validation_console.setVisible(true);
            }
        });

        debug.add(root.actions.generate_diff);
        add(debug);

        help.add(root.actions.usersManual);
        help.add(new JSeparator());
        help.add(root.actions.aboutPage);
        add(help);
    }

    private void populateDemoList() {
        final List<String> demoList = new ArrayList<String>();
        final URL url = BrowserMenuBar.class.getResource("/demos/file-list.txt");
        InputStream is = null;
        LineNumberReader lnr = null;
        if (url != null) {
            try {
                is = url.openStream();
                final InputStreamReader reader = new InputStreamReader(is);
                lnr = new LineNumberReader(reader);
                try {
                    String line;
                    while ((line = lnr.readLine()) != null) {
                        demoList.add(line);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lnr.close();
                    } catch (final IOException e) {
                        // swallow
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (final IOException e) {
                        // swallow
                    }
                }
            }

            for (final String s : demoList) {
                final String s1[] = s.split(",");
                allDemos.put(s1[0], s1[1]);
            }
        }
    }

    private JRadioButtonMenuItem addLevel(final JMenu menu, final ButtonGroup group, final String title, final int level) {
        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(new AntiAliasedAction(title, level));
        group.add(item);
        menu.add(item);
        return item;
    }


    /**
     * Description of the Method
     */
    public void createActions() {
    }

    private ScaleFactor[] initializeScales() {
        final ScaleFactor[] scales = new ScaleFactor[11];
        int i = 0;
        scales[i++] = new ScaleFactor(1.0d, "Normal (100%)");
        scales[i++] = new ScaleFactor(2.0d, "200%");
        scales[i++] = new ScaleFactor(1.5d, "150%");
        scales[i++] = new ScaleFactor(0.85d, "85%");
        scales[i++] = new ScaleFactor(0.75d, "75%");
        scales[i++] = new ScaleFactor(0.5d, "50%");
        scales[i++] = new ScaleFactor(0.33d, "33%");
        scales[i++] = new ScaleFactor(0.25d, "25%");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_WIDTH, "Page width");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_HEIGHT, "Page height");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_WHOLE, "Whole page");
        return scales;
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class ShowDOMInspectorAction extends AbstractAction {
        /**
         * Description of the Field
         */
        private DOMInspector inspector;
        /**
         * Description of the Field
         */
        private JFrame inspectorFrame;

        /**
         * Constructor for the ShowDOMInspectorAction object
         */
        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            if (inspectorFrame == null) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if (inspector == null) {
                inspector = new DOMInspector(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(500, 600);
                inspectorFrame.show();
            } else {
                inspector.setForDocument(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());
            }
            inspectorFrame.show();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class BoxOutlinesAction extends AbstractAction {
        /**
         * Constructor for the BoxOutlinesAction object
         */
        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_boxes(!root.panel.view.getSharedContext().debugDrawBoxes());
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class LineBoxOutlinesAction extends AbstractAction {
        /**
         * Constructor for the LineBoxOutlinesAction object
         */
        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_line_boxes(!root.panel.view.getSharedContext().debugDrawLineBoxes());
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class InlineBoxesAction extends AbstractAction {
        /**
         * Constructor for the InlineBoxesAction object
         */
        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_inline_boxes(!root.panel.view.getSharedContext().debugDrawInlineBoxes());
            root.panel.view.repaint();
        }
    }

    class FontMetricsAction extends AbstractAction {
        /**
         * Constructor for the InlineBoxesAction object
         */
        FontMetricsAction() {
            super("Show Font Metrics");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_font_metrics(!root.panel.view.getSharedContext().debugDrawFontMetrics());
            root.panel.view.repaint();
        }
    }

    class NextDemoAction extends AbstractAction {

        public NextDemoAction() {
            super("Next Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(final ActionEvent e) {
            navigateToNextDemo();
        }
    }

    public void navigateToNextDemo() {
        String nextPage = null;
        for (final Iterator<String> iter = allDemos.keySet().iterator(); iter.hasNext();) {
            final String s = iter.next();
            if (s.equals(lastDemoOpened)) {
                if (iter.hasNext()) {
                    nextPage = iter.next();
                    break;
                }
            }
        }
        if (nextPage == null) {
            // go to first page
            final Iterator<String> iter = allDemos.keySet().iterator();
            nextPage = iter.next();
        }

        try {
            root.panel.loadPage(allDemos.get(nextPage));
            lastDemoOpened = nextPage;
        } catch (final Exception ex) {
            Uu.p(ex);
        }
    }

    class PriorDemoAction extends AbstractAction {

        public PriorDemoAction() {
            super("Prior Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(final ActionEvent e) {
            navigateToPriorDemo();
        }
    }

    public void navigateToPriorDemo() {
        String priorPage = null;
        for (final Iterator<String> iter = allDemos.keySet().iterator(); iter.hasNext();) {
            final String s = iter.next();
            if (s.equals(lastDemoOpened)) {
                break;
            }
            priorPage = s;
        }
        if (priorPage == null) {
            // go to last page
            final Iterator<String> iter = allDemos.keySet().iterator();
            while (iter.hasNext()) {
                priorPage = iter.next();
            }
        }

        try {
            root.panel.loadPage(allDemos.get(priorPage));
            lastDemoOpened = priorPage;
        } catch (final Exception ex) {
            Uu.p(ex);
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class LoadAction extends AbstractAction {
        /**
         * Description of the Field
         */
        protected String url;

        private final String pageName;

        /**
         * Constructor for the LoadAction object
         *
         * @param name PARAM
         * @param url  PARAM
         */
        public LoadAction(final String name, final String url) {
            super(name);
            pageName = name;
            this.url = url;
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            try {
                root.panel.loadPage(url);
                lastDemoOpened = pageName;
            } catch (final Exception ex) {
                Uu.p(ex);
            }
        }

    }

    @Deprecated
    class AntiAliasedAction extends AbstractAction {
        int fontSizeThreshold;

        AntiAliasedAction(final String text, final int fontSizeThreshold) {
            super(text);
            this.fontSizeThreshold = fontSizeThreshold;
        }

        public void actionPerformed(final ActionEvent evt) {
//            root.panel.view.getSharedContext().getTextRenderer().setSmoothingThreshold(fontSizeThreshold);
//            root.panel.view.repaint();
        }
    }

}


/**
 * Description of the Class
 *
 * @author empty
 */
class EmptyAction extends AbstractAction {
    public EmptyAction(final String name, final Icon icon) {
        this(name, "", icon);
    }

    public EmptyAction(final String name, final String shortDesc, final Icon icon) {
        super(name, icon);
        putValue(Action.SHORT_DESCRIPTION, shortDesc);
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name  PARAM
     * @param accel PARAM
     */
    public EmptyAction(final String name, final int accel) {
        this(name);
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(accel,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name PARAM
     */
    public EmptyAction(final String name) {
        super(name);
    }

    /**
     * Description of the Method
     *
     * @param evt PARAM
     */
    public void actionPerformed(final ActionEvent evt) {
    }
}
