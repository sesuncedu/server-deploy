package org.protege.owl.server.deploy;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.protege.owl.server.deploy.task.UnzipDistributionTask;

public class Main extends JFrame {
	private JTextField sandBoxUserField;
	private JTextField hostnameField;
	private JTextField javacmdField;
	private JTextField memoryField;
	
	
	public Main() {
		super("Protege OWL Server Installer");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void createUI() {
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(createCenterPanel(), BorderLayout.CENTER);
		container.add(createBottomPanel(), BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}
	
	private JPanel createCenterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,2));
		
		panel.add(new JLabel("Sandbox User:"));
		sandBoxUserField = new JTextField();
		panel.add(sandBoxUserField);
		
		panel.add(new JLabel("Hostname:"));
		hostnameField = new JTextField();
		panel.add(hostnameField);
		
		panel.add(new JLabel("Java Command:"));
		javacmdField = new JTextField();
		panel.add(javacmdField);
		
		panel.add(new JLabel("Memory in megabytes:"));
		memoryField=new JTextField();
		panel.add(memoryField);
		
		Dimension dim1 = new JLabel("/usr/local/java/jdk1.6.034_8888").getPreferredSize();
		Dimension dim2 = javacmdField.getPreferredSize();
		javacmdField.setPreferredSize(new Dimension((int) dim1.getWidth(), (int) dim2.getHeight()));
		
		return panel;
	}
	
	private JPanel createBottomPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,0));
		
		JButton deployButton = new JButton("Deploy");
		deployButton.addActionListener(new DeployActionListener());
		panel.add(deployButton);
		
		JButton installButton = new JButton("Install");
		installButton.addActionListener(new InstallActionListener());
		panel.add(installButton);
		
		JButton undeployButton = new JButton("Undeploy");
		undeployButton.addActionListener(new UndeployActionListener());
		panel.add(undeployButton);
		
		JButton uninstallButton = new JButton("Uninstall");
		uninstallButton.addActionListener(new UninstallActionListener());
		panel.add(uninstallButton);
		
		return panel;
	}
	
	private class InstallActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				createInstaller().install();
				JOptionPane.showMessageDialog(Main.this, "Installed");
				System.exit(0);
			}
			catch (IOException ioe) {
				JOptionPane.showMessageDialog(Main.this, "Install failed: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}
	}
	
	private class DeployActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				createInstaller().deploy();
				JOptionPane.showMessageDialog(Main.this, "Deployed");
				System.exit(0);
			}
			catch (IOException ioe) {
				JOptionPane.showMessageDialog(Main.this, "Deploy failed: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}
	}

	private class UninstallActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				createInstaller().uninstall();
				JOptionPane.showMessageDialog(Main.this, "Uninstalled");
				System.exit(0);
			}
			catch (IOException ioe) {
				JOptionPane.showMessageDialog(Main.this, "Uninstall failed: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}
	}
	
	private class UndeployActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				createInstaller().undeploy();
				JOptionPane.showMessageDialog(Main.this, "Undeployed");
				System.exit(0);
			}
			catch (IOException ioe) {
				JOptionPane.showMessageDialog(Main.this, "Undeploy failed: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}
	}
    
    private Configuration createConfiguration() {
		String sandboxUser = sandBoxUserField.getText();
		String hostname    = hostnameField.getText();
		String javacmd     = javacmdField.getText();
		int memoryMb;
		try {
			memoryMb = Integer.parseInt(memoryField.getText());
		}
		catch (NumberFormatException nfe) {
			memoryMb = 700;
		}
		return new Configuration(sandboxUser, hostname, javacmd, memoryMb);
	}
    
    private Installer createInstaller() {
    	Configuration configuration = createConfiguration();
    	OperatingSystem os = configuration.getOperatingSystem();
    	if (os == null) {
    		throw new IllegalStateException("Unknown operating system");
    	}
    	switch (os) {
    	case LINUX:
    		return new LinuxInstaller(configuration);
    	case WINDOWS_32_BIT:
    	case WINDOWS_64_BIT:
    		return new WindowsInstaller(configuration);
    	case OS_X:
    		return new OSXInstaller(configuration);
    	default:
    		throw new IllegalStateException("Programmer left out the installation for " + os);
    	}
    }
    

    
    public static void test() throws IOException {
    	System.out.println(OperatingSystem.detectOperatingSystem());
    	URL serverDistURL = Main.class.getResource(Configuration.SERVER_DISTRIBUTION);
        UnzipDistributionTask task1 = new UnzipDistributionTask(serverDistURL, new File("build/extracted"));
        task1.setPrefixToRemove(Configuration.PREFIX_TO_REMOVE_FROM_DISTRO);
        task1.run();
        new UnzipDistributionTask(Main.class.getResource(Configuration.UNIX_SCRIPTS), new File("build/extracted/bin")).run();
        Configuration conf = new Configuration("redmond", "localhost", "/usr/bin/java", 1700);
        conf.copyWithReplacements(Main.class.getResource("/unix/protege.defaults"), new File("build/protege.copied"));   	
    }

	public static void main(String[] args) throws IOException {
		Main main = new Main();
		main.createUI();
	}

}
