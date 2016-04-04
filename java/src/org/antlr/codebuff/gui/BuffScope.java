package org.antlr.codebuff.gui;

import javax.swing.*;
import java.awt.*;

public class BuffScope {
	private JSplitPane codeAndResultsSplitPane;
	private JSplitPane origVsBuffedSplitPane;
	protected JPanel overallPanel;
	protected JTextArea injectNLConsole;
	protected JTextPane origTextPane;
	protected JTextPane formattedTextPane;
	public JTextArea alignConsole;
	public JTabbedPane analysisTabbedPane;
	public JPanel injectNLTab;
	public JPanel alignTab;
	public JTextArea injectWSConsole;
	public JPanel injectWSTab;

	public JTextArea getInjectNLConsole() {
		return injectNLConsole;
	}

	public JTextPane getFormattedTextPane() {
		return formattedTextPane;
	}

	public JTextPane getOrigTextPane() {
		return origTextPane;
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		overallPanel = new JPanel();
		overallPanel.setLayout(new BorderLayout(0, 0));
		overallPanel.setPreferredSize(new Dimension(800, 800));
		codeAndResultsSplitPane = new JSplitPane();
		codeAndResultsSplitPane.setContinuousLayout(true);
		codeAndResultsSplitPane.setOrientation(0);
		codeAndResultsSplitPane.setResizeWeight(0.5);
		overallPanel.add(codeAndResultsSplitPane, BorderLayout.CENTER);
		origVsBuffedSplitPane = new JSplitPane();
		origVsBuffedSplitPane.setContinuousLayout(true);
		origVsBuffedSplitPane.setDividerLocation(400);
		origVsBuffedSplitPane.setResizeWeight(0.0);
		codeAndResultsSplitPane.setLeftComponent(origVsBuffedSplitPane);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout(0, 0));
		origVsBuffedSplitPane.setLeftComponent(panel1);
		final JLabel label1 = new JLabel();
		label1.setHorizontalAlignment(0);
		label1.setText("Original source");
		panel1.add(label1, BorderLayout.NORTH);
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, BorderLayout.CENTER);
		origTextPane = new JTextPane();
		origTextPane.setEditable(false);
		origTextPane.setMinimumSize(new Dimension(200, 100));
		origTextPane.setText("Buffed text");
		scrollPane1.setViewportView(origTextPane);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		origVsBuffedSplitPane.setRightComponent(panel2);
		final JLabel label2 = new JLabel();
		label2.setHorizontalAlignment(0);
		label2.setText("Formatted output");
		panel2.add(label2, BorderLayout.NORTH);
		final JScrollPane scrollPane2 = new JScrollPane();
		panel2.add(scrollPane2, BorderLayout.CENTER);
		formattedTextPane = new JTextPane();
		formattedTextPane.setEditable(false);
		formattedTextPane.setMinimumSize(new Dimension(200, 100));
		formattedTextPane.setText("original");
		scrollPane2.setViewportView(formattedTextPane);
		analysisTabbedPane = new JTabbedPane();
		codeAndResultsSplitPane.setRightComponent(analysisTabbedPane);
		injectNLTab = new JPanel();
		injectNLTab.setLayout(new BorderLayout(0, 0));
		analysisTabbedPane.addTab("Inject \\n", injectNLTab);
		final JScrollPane scrollPane3 = new JScrollPane();
		scrollPane3.setVerticalScrollBarPolicy(20);
		injectNLTab.add(scrollPane3, BorderLayout.CENTER);
		injectNLConsole = new JTextArea();
		injectNLConsole.setEditable(false);
		scrollPane3.setViewportView(injectNLConsole);
		alignTab = new JPanel();
		alignTab.setLayout(new BorderLayout(0, 0));
		analysisTabbedPane.addTab("Alignment", alignTab);
		final JScrollPane scrollPane4 = new JScrollPane();
		alignTab.add(scrollPane4, BorderLayout.CENTER);
		alignConsole = new JTextArea();
		scrollPane4.setViewportView(alignConsole);
		injectWSTab = new JPanel();
		injectWSTab.setLayout(new BorderLayout(0, 0));
		analysisTabbedPane.addTab("Inject whitespace", injectWSTab);
		final JScrollPane scrollPane5 = new JScrollPane();
		injectWSTab.add(scrollPane5, BorderLayout.CENTER);
		injectWSConsole = new JTextArea();
		injectWSConsole.setEditable(false);
		scrollPane5.setViewportView(injectWSConsole);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return overallPanel;
	}
}
