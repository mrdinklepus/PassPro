package userInterface;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import encryption.MasterPassword;

import userManagement.User;

/**
 * Main frame for the PassPro application
 * 
 * @author MDWhite
 */
public class PassProFrame extends JFrame
{
	private User user;
	private JMenu mmenu;
	private JMenuBar menubar;
	private JMenuItem mitem;
	private JTabbedPane tabbedPane;
	private UpdUsrPwPanel comboPanel;
	private EntityMgrPanel managerPanel;
	private JCheckBoxMenuItem histitem;
	private JCheckBoxMenuItem backupitem;
	private JMenuItem timeitem;
	
	/**
	 * Constructor
	 * 
	 * @param user
	 */
	public PassProFrame(User user)
	{
		super("PassPro");
		this.user = user;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.out.println("Cleaning up and Exiting...");
				cleanup();
				dispose();
			}
		});
		
		init();
		user.showBackupReminder();
		toFront();
		repaint();
	}
	
	/**
	 * Init
	 */
	private void init()
	{
		try
		{
			createMenu();
			createTabbedPane();
			setPreferredSize(new Dimension(550,550));
			setMinimumSize(new Dimension(375,425));
			pack();
			setLocationRelativeTo(null); //Should be after setting size
			setVisible(true);
			toFront();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the menu for the frame
	 */
	private void createMenu()
	{
		menubar = new JMenuBar();
		mmenu = new JMenu("File");
		mitem = new JMenuItem("Change Master Password");
		mitem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				user.updateMaster();
			}
		});
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Cleaning up and Exiting...");
				cleanup();
				System.exit(0);
			}
		});
		
		mmenu.add(mitem);
		mmenu.add(exit);
		
		JMenu omenu = new JMenu("Options");
		histitem = new JCheckBoxMenuItem("Save Password History");
		histitem.setSelected(user.getSaveHistory());
		histitem.setToolTipText("Enables keeping a history of all usernames and passwords for entities");
		histitem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doSetHistory();
			}
		});
		
		backupitem = new JCheckBoxMenuItem("Show Backup Reminders");
		backupitem.setSelected(user.getShowBackups());
		backupitem.setToolTipText("Displays a reminder to backup the application every 10 logins");
		backupitem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				user.updateTimer();
				
				if (backupitem.isSelected())
				{
					user.setShowBackups(true);
					JOptionPane.showMessageDialog(null, "Backup reminder enabled!");
				}
				else
				{
					user.setShowBackups(false);
					JOptionPane.showMessageDialog(null, "Backup reminder disabled!");
				}
			}
		});
		
		if (user.getTimeout())
		{
			timeitem = new JMenuItem("Disable Timeout");
		}
		else
		{
			timeitem = new JMenuItem("Enable Timeout");
		}
		timeitem.setToolTipText("Toggles closing the application after 10 minutes of inactivity");
		timeitem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				user.updateTimer();
				
				if (user.getTimeout())
				{
					timeitem.setText("Enable Timeout");
					user.setTimeout(false);
					JOptionPane.showMessageDialog(null, "Timeout Disabled!");
				}
				else
				{
					timeitem.setText("Disable Timeout");
					user.setTimeout(true);
					JOptionPane.showMessageDialog(null, "Timeout Enabled!");
				}
				
				timeitem.updateUI();
			}
		});
		
		JMenu hmenu = new JMenu("Help");
		JMenuItem jmi = new JMenuItem("Launch Help");
		jmi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				user.updateTimer();
				// Launch the help PDF file with default desktop
				File pdfFile = new File("PassProHelp.pdf");
				if (pdfFile.exists())
				{
					try
					{
						if (Desktop.isDesktopSupported())
						{
							Desktop.getDesktop().open(pdfFile);
						}
						else
						{
							System.out.println("Desktop is not supported!");
						}
					}
					catch (Exception ee)
					{
						ee.printStackTrace();
					}
				}
				else
				{
					System.out.println("PassProHelp.pdf File does not exist!");
				}
			}
		});
		hmenu.add(jmi);
		
		omenu.add(histitem);
		omenu.add(backupitem);
		omenu.add(timeitem);
		menubar.add(mmenu);
		menubar.add(omenu);
		menubar.add(Box.createHorizontalGlue());
		menubar.add(hmenu);
		setJMenuBar(menubar);
		
	}
	
	/**
	 * Make the tabs for the main window
	 */
	private void createTabbedPane()
	{
		tabbedPane = new JTabbedPane();
		comboPanel = new UpdUsrPwPanel(user, this);
		managerPanel = new EntityMgrPanel(user, this);
		tabbedPane.addTab("Password Management", comboPanel);
		tabbedPane.addTab("Entity Managment", managerPanel);
		
		// Open the entity tab first if they don't have any already
		if (!user.hasEntities())
		{
			tabbedPane.setSelectedComponent(managerPanel);
		}
		
		add(tabbedPane);
	}
	
	/**
	 * Prompts user for confirmation, then delete all but current password
	 */
	private void doSetHistory()
	{
		user.updateTimer();
		
		if (user.getSaveHistory())
		{
			Object[] options = {"Continue", "Cancel"};
			int n = JOptionPane.showOptionDialog(null,
				    "Turning off password history will delete all but the current password\n" +
				    "for each entity. It cannot be undone!\n\n" +
				    "Are You Sure You Want to Continue?",
				    "Turn off password history?",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.WARNING_MESSAGE,
				    null,
				    options,
				    options[1]);
		
			// Go For it!
			if (n == 0)
			{
				user.deleteAllButCurrent();
				user.setSaveHistory(false);
				comboPanel.resetSelection();
			}
			else
			{
				histitem.setSelected(true);
			}
		}
		else
		{	
			user.setSaveHistory(true);
			JOptionPane.showMessageDialog(null, "Password History will now be saved!");
		}
	}
	
	/**
	 * @return managerPanel
	 */
	public EntityMgrPanel getMgrPanel()
	{
		return managerPanel;
	}
	
	/**
	 * @return comboPanel
	 */
	public UpdUsrPwPanel getUPPanel()
	{
		return comboPanel;
	}
	
	private void cleanup()
	{
		MasterPassword.clearPassword(user.getMaster());
	}
	
	
	public static void main(String[] args)
	{
		PassProFrame fr = new PassProFrame(null);
	}
	
}
