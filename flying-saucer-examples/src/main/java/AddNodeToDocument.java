/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xhtmlrenderer.demo.browser.FSScrollPane;
import org.xhtmlrenderer.simple.HtmlNamespaceHandler;
import org.xhtmlrenderer.util.NodeHelper;

import com.github.danfickle.flyingsaucer.swing.XHTMLPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * This sample shows how to add nodes to a DOM Document object that has already been rendered to the screen, and
 * render the updates.
 */
public class AddNodeToDocument {
    private JFrame frame;
    private XHTMLPanel panel;
    private Document domDocument;
    private Element documentRoot;

    public static void main(final String[] args) {
        new AddNodeToDocument().run();
    }

    private void run() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initFrame();

                initPanel();

                // here's where the real work takes place
                @SuppressWarnings("serial")
                final
				Action addDocAction = new AbstractAction("Add Node") {
                    public void actionPerformed(final ActionEvent event) {
                        // we'll add a single node on each click
                    	final Text textNode = documentRoot.getOwnerDocument().createTextNode("adding node at " + new Date());
                    	documentRoot.appendChild(textNode);
                        documentRoot.appendChild(domDocument.createElement("br"));

                        // note that we just pass in the same DOM instance we already
                        // rendered; no need to pass through the XML parser again
                        panel.setDocument(domDocument);

                        // alternately, this will work as well--doDocumentLayout() followed by repaint()
                        // setDocument() is still the recommended approach in this case, as it's a top-level entry
                        // point which ensures that proper setup takes place for the layout and render 
                        // panel.doDocumentLayout(panel.getGraphics());
                        // panel.repaint();
                    }
                };

                final JButton btn = new JButton(addDocAction);
                frame.getContentPane().add(BorderLayout.SOUTH, btn);

                frame.pack();
                frame.setSize(1024, 768);
                frame.setVisible(true);
            }
        });
    }

    private void initPanel() {
        panel = new XHTMLPanel();

        try {
            panel.setDocumentFromString(
                    "<html style='position: absolute;'>" +
                            "This line was in the original document. Press the button to add a new node to " +
                            "the same document <br />" +
                            "</html>",
                    null,
                    new HtmlNamespaceHandler());

            // our panel already has a DOM Document; this is what we'll modify
            domDocument = panel.getDocument();

            // root element of the document--you could grab any other element
            // by traversing, XPath, etc.
            documentRoot = NodeHelper.getBody(domDocument).get();
        } catch (final Exception e) {
            messageAndExit("Could not render page: " + e.getMessage(), -1);
        }

        final FSScrollPane fsScrollPane = new FSScrollPane(panel);
        frame.getContentPane().add(BorderLayout.CENTER, fsScrollPane);
    }

    private void initFrame() {
        frame = new JFrame("XHTMLPanel");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void messageAndExit(final String msg, final int rtnCode) {
        System.out.println(msg);
        System.exit(rtnCode);
    }
}
