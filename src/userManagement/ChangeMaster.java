package userManagement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.LineBorder;

import encryption.EncryptedProperties;
import encryption.MasterPassword;

/**
 * Class for handling the Master Password change
 * 
 * @author MDWhite
 */
public class ChangeMaster extends JDialog
{
	private boolean userCreated = false;
	private JPasswordField cpwTextField;
	private JPasswordField pTextField;
	private JPasswordField pTextField2;
	private char[] pw1;
	private char[] pw2;
	private JLabel currPassword;
	private JLabel lPassword;
	private JLabel lPassword2;
	private JButton btnUpdate;
	private JButton btnCancel;
	private User user;
	
	/**
	 * Constructor
	 * 
	 * @param usr
	 */
	public ChangeMaster(User usr)
	{
		super(new JFrame(), "Change Master Password", true);
		
		this.user = usr;
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});
		
		try
		{
			init();
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	/**
	 * Initialize the components
	 */
	private void init()
	{		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		currPassword = new JLabel("Current Password: ");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		panel.add(currPassword, gbc);
		
		cpwTextField = new JPasswordField(20);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		panel.add(cpwTextField, gbc);
		
		lPassword = new JLabel("New Password: ");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		panel.add(lPassword, gbc);
		
		pTextField = new JPasswordField(20);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		panel.add(pTextField, gbc);
		
		lPassword2 = new JLabel("Verify Password: ");
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		panel.add(lPassword2, gbc);
		
		pTextField2 = new JPasswordField(20);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		panel.add(pTextField2, gbc);
		
		JLabel lPassword3 = new JLabel("(12-20 length, 3 of these: upper, lower, number, or special character)");
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		panel.add(lPassword3, gbc);
		
		panel.setBorder(new LineBorder(Color.GRAY));
		
		pTextField2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doChange();
			}
		});
		
		btnUpdate = new JButton("Update Password");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doChange();
			}
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		
		JPanel bp = new JPanel();
		bp.add(btnUpdate);
		bp.add(btnCancel);
		
		add(panel, BorderLayout.CENTER);
		add(bp, BorderLayout.PAGE_END);
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		cpwTextField.requestFocusInWindow();
		setVisible(true);
	}
	
	/**
	 * Validate input, then changes Master Password
	 */
	private void doChange()
	{
		try
		{
			user.updateTimer();
			
			if (validateInput())
			{
				String tmp = getTmpusermp();
				
				if (updateEntities())
				{
					storeUserId(tmp);
					store();
				}
				
				if (userCreated)
				{
					JOptionPane.showMessageDialog(null, "Master Password updated!\n**IMPORTANT** Don't forget to remember the master password you just created.\nThere is no way to recover it if you forget!", "Change Master Password", JOptionPane.INFORMATION_MESSAGE);
					dispose();
				}
			}
			else
			{
				MasterPassword.clearPassword(pw1);
				MasterPassword.clearPassword(pw2);
				cpwTextField.setText("");
				cpwTextField.requestFocus();
				pTextField.setText("");
				pTextField2.setText("");
			}
		} 
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error updating password!" + e.getMessage(), "Update Master Password", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Validate the user entered data
	 * 
	 * @return true if data is ok, false if not
	 */
	private boolean validateInput()
	{
		pw1 = pTextField.getPassword();
		pw2 = pTextField2.getPassword();
		
		// First verify the current password
		try
		{			
			if (Arrays.equals(cpwTextField.getPassword(), user.getMaster()))
			{
				//check the passwords match
				if (!Arrays.equals(pw1, pw2))
				{
					JOptionPane.showMessageDialog(null, "New Passwords don't match!", "Update Master Password", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				//check password length
				if (pw1.length < 12 || pw1.length > 20)
				{
					JOptionPane.showMessageDialog(null, "Passwords must be 12 - 20 characters in length!", "Update Master Password", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				//start
				int test = 0;
				Pattern numPattern = Pattern.compile("((?=.*\\d).{1,1})");
				Pattern lowerPattern = Pattern.compile("((?=.*[a-z]).{1,1})");
				Pattern upperPattern = Pattern.compile("((?=.*[A-Z]).{1,1})");
				Pattern specPattern = Pattern.compile("((?=.*\\p{Punct}).{1,1})");
				Matcher matcher;
				StringBuffer sb = new StringBuffer();
				sb.append(":");
				
				// Test for a number
				for (int i = 0; i < pw1.length; i++)
				{
					sb.setCharAt(0, pw1[i]);
					matcher = numPattern.matcher(sb);
					
					if (matcher.matches())
					{
						test++;
						break;
					}
				}
				
				// Test for a lowercase
				for (int i = 0; i < pw1.length; i++)
				{
					sb.setCharAt(0, pw1[i]);
					matcher = lowerPattern.matcher(sb);
					
					if (matcher.matches())
					{
						test++;
						break;
					}
				}
				
				// Test for an uppercase
				for (int i = 0; i < pw1.length; i++)
				{
					sb.setCharAt(0, pw1[i]);
					matcher = upperPattern.matcher(sb);
					
					if (matcher.matches())
					{
						test++;
						break;
					}
				}
				
				// Test for a special character
				for (int i = 0; i < pw1.length; i++)
				{
					sb.setCharAt(0, pw1[i]);
					matcher = specPattern.matcher(sb);
					
					if (matcher.matches())
					{
						test++;
						break;
					}
				}
				
				// Throw an error if they don't have at least 3 of the 4
				if (test < 3)
				{
					//JLabel lab = new JLabel()
					JOptionPane.showMessageDialog(null, "Password must have 3 of the following: uppercase, lowercase, number, or special character!", "Update Master Password", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				//end
				
				//check password characters
//				if (!Pattern.matches("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{12,20})", pw1))
//				{
//					JOptionPane.showMessageDialog(null, "Password must have 1 uppercase, 1 lowercase, 1 number, and 1 special character!", "Create User", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Current Password does not match!", "Update Master Password", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "There was an error reading current user Property file! " + e.getMessage(), "Update Master Password", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		MasterPassword.clearPassword(pw2);
		return true;
	}
	
	/**
	 * Store the new password hash
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void store() throws FileNotFoundException,
										IOException, NoSuchAlgorithmException
	{
		String passHash = MasterPassword.doHash(pw1);
		EncryptedProperties ep = new EncryptedProperties(".//config//user.file", null, false);
		ep.updateProperty(user.getUsername(), passHash);
		userCreated = true;
	}
	
	/**
	 * Store the userid map file
	 */
	private void storeUserId(String temp)
	{
		EncryptedProperties epnew = new EncryptedProperties(".//config//user.mp", pw1);
		epnew.updateProperty(user.getUsername(), pw1[0] + pw1[1] + pw1[2] + temp.substring(3));
	}
	
	/**
	 * Re-encrypt all the entities using the new password
	 */
	private boolean updateEntities()
	{
		return user.updateMasterForAllEntities(pw1);
	}
	
	private String getTmpusermp()
	{
		// Create a entprop file for user.mp because it needs to be done before we update the masterpw
		EncryptedProperties ep = new EncryptedProperties(".//config//user.mp", user.getMaster());
		return ep.getPropertyValue(user.getUsername());
	}
	
	
	public static void main(String[] args)
	{
		new ChangeMaster(null);
	}
}
