package gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

public class StartWindow extends JFrame{
	public JFrame myFrame = this;
	
	public StartWindow() {
		this.setSize(500, 300);
		this.setTitle("Supply Chain App");
		init();
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void init() {
		this.setLayout(new BorderLayout());

		JLabel welcome = new JLabel("");

		JTabbedPane JTab = new JTabbedPane(JTabbedPane.TOP);
		JPanel  panel_loadup = new JPanel();
		JPanel  panel_loadin = new JPanel();

		panel_loadup.setLayout(new BorderLayout());
		JPanel loadup_content = new JPanel();
	//	loadup_content.setPreferredSize(new Dimension(500,0));
		Box box = Box.createVerticalBox();
		JLabel pk_label = new JLabel("Private Key:");
		JTextField pk_text = new JTextField(20);
		box.add(Box.createVerticalStrut((int) (this.getHeight()*0.2)));
		box.add(pk_label);
		box.add(pk_text);
		box.add(Box.createVerticalGlue());
		loadup_content.add(box);
		panel_loadup.add(loadup_content);
		JButton loadup_button = new JButton("Load Up");
		loadup_button.setPreferredSize(new Dimension(100,30));
		Box sbox = Box.createHorizontalBox();
		sbox.add(Box.createHorizontalGlue());
		sbox.add(loadup_button);
		sbox.add(Box.createHorizontalGlue());
		panel_loadup.add(sbox, BorderLayout.SOUTH);

		panel_loadin.setLayout(new BorderLayout());
		JPanel loadin_content = new JPanel();
		Box box2 = Box.createVerticalBox();
		JLabel pk_label2 = new JLabel("Private Key:");
		JTextField pk_text2 = new JTextField(20);
		JLabel name_label = new JLabel("Name:");
		JTextField name_text = new JTextField(20);
		JLabel type_label = new JLabel("Type:");
		String[] combobox_pattern = { "company", "bank", "arbitrator"};
  		JComboBox combobox = new JComboBox(combobox_pattern);
		box2.add(Box.createVerticalGlue());
		box2.add(pk_label2);
		box2.add(pk_text2);
		box2.add(Box.createVerticalGlue());
		box2.add(name_label);
		box2.add(name_text);
		box2.add(type_label);
		box2.add(combobox);
		loadin_content.add(box2);
		panel_loadin.add(loadin_content);
		JButton loadin_button = new JButton("Load In");
		loadin_button.setPreferredSize(new Dimension(100,30));
		Box sbox2 = Box.createHorizontalBox();
		sbox2.add(Box.createHorizontalGlue());
		sbox2.add(loadin_button);
		sbox2.add(Box.createHorizontalGlue());
		panel_loadin.add(sbox2, BorderLayout.SOUTH);
		
		JTab.add("Load Up", panel_loadup);
		JTab.add("Load In", panel_loadin);

		this.add(JTab);

		loadup_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow mainWindow;
				try {
					mainWindow = new MainWindow(myFrame, pk_text.getText(), "", "", true);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		
		loadin_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					MainWindow mainWindow =  new MainWindow(myFrame, pk_text2.getText(), name_text.getText(), combobox.getSelectedItem().toString(), false);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
	}
}