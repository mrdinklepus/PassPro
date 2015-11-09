package userInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import userManagement.CreateUser;
import userManagement.User;
import encryption.EncryptedProperties;
import encryption.MasterPassword;

/**
 * Dialog for logging in to PassPro
 * 
 * @author MDWhite
 */
public class LoginDialog extends JDialog
{
	private JTextField uTextField;
	private JPasswordField pTextField;
	private char[] pw1;
	private JLabel lUsername;
	private JLabel lPassword;
	private JButton btnLogin;
	private JButton btnCancel;
	private JButton btnNewUser;
	
	/**
	 * Constructor - Creates the Dialog
	 */
	public LoginDialog()
	{
		super(new JFrame(), "Login to PassPro", true);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		lUsername = new JLabel("Username: ");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		panel.add(lUsername, gbc);
		
		uTextField = new JTextField(20);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		panel.add(uTextField, gbc);
		
		lPassword = new JLabel("Password: ");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		panel.add(lPassword, gbc);
		
		pTextField = new JPasswordField(20);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		panel.add(pTextField, gbc);
		pTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				new LoginAction();
			}
		});
		
		panel.setBorder(new LineBorder(Color.GRAY));
		
		btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				new LoginAction();
			}
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		btnNewUser = new JButton("New User");
		btnNewUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				new CreateUser(uTextField, pTextField);
			}
		});
		
		JPanel bp = new JPanel();
		bp.add(btnLogin);
		bp.add(btnCancel);
		bp.add(btnNewUser);
		
		add(panel, BorderLayout.CENTER);
		add(bp, BorderLayout.PAGE_END);
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		uTextField.requestFocusInWindow();
	}
	
	/**
	 * Inner class for handling the actual login function
	 * 
	 * @author MDWhite
	 */
	private class LoginAction
	{
		/**
		 * Attempt to log in using user entered credentials
		 */
		public LoginAction()
		{
			try
			{
				// If credentials are valid, create a User object and launch the main window
				if (validateUser())
				{
					final User usr = new User(uTextField.getText().trim(), pw1);
					java.awt.EventQueue.invokeLater(new Runnable() {

						public void run()
						{
							new PassProFrame(usr);
							
						}
						
					});
					
					
					// get rid of this dialog
					dispose();
				} else {
					pTextField.setText("");
				}
			}
			catch (Exception le)
			{
				JOptionPane.showMessageDialog(LoginDialog.this, "There was a problem logging in! "
						+ le.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
				pTextField.setText("");
			}
		}
		
		/**
		 * Retrieves saved password by username, hashes the user given password, and checks that they match
		 * 
		 * @return true if passwords match
		 * @throws NoSuchAlgorithmException
		 * @throws UnsupportedEncodingException
		 */
		private boolean validateUser() throws NoSuchAlgorithmException, UnsupportedEncodingException
		{
			// Read Prop file
			EncryptedProperties ep = new EncryptedProperties(".//config//user.file",null,false);
			String savedHash = ep.getProperty(uTextField.getText().trim().toUpperCase());
			
			// If savedHash is null, user doesn't exist
			if (savedHash == null)
			{
				JOptionPane.showMessageDialog(LoginDialog.this, "User " + uTextField.getText().trim()  
						+ "does not exist!!", "Login", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			pw1 = pTextField.getPassword();
			String testHash = MasterPassword.doHash(pw1);
			
			// Test if passwords match
			if (savedHash != null && savedHash.equals(testHash))
			{
				return true;
			}
			else
			{
				JOptionPane.showMessageDialog(LoginDialog.this, "Invalid password!", "Login", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
	}
	

	public static void main(String[] args)
	{
		new LoginDialog().setVisible(true);
	}
}
