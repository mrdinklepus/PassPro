package userManagement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
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
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import userInterface.LoginDialog;

import encryption.EncryptedProperties;
import encryption.MasterPassword;

/**
 * Class for handling creation of a new user
 * 
 * @author MDWhite
 */
public class CreateUser extends JDialog
{
	private JTextField jt;
	private JTextField pf;
	private boolean userCreated = false;
	private JTextField uTextField;
	private JPasswordField pTextField;
	private JPasswordField pTextField2;
	private char[] pw1;
	private char[] pw2;
	private JLabel lUsername;
	private JLabel lPassword;
	private JLabel lPassword2;
	private JButton btnCreate;
	private JButton btnCancel;
	
	/**
	 * Constructor
	 */
	public CreateUser(JTextField jt, JTextField pf)
	{
		super(new JFrame(), "Create New User", true);
		
		this.jt = jt;
		this.pf = pf;
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				MasterPassword.clearPassword(pw1);
				MasterPassword.clearPassword(pw2);
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
	 * Initialize components
	 */
	private void init()
	{		
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
				System.out.println("Creating user");
				doCreate();
			}
		});
		
		btnCreate = new JButton("Create User");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Creating user");
				doCreate();
			}
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				dispose();
				jt.requestFocus();
			}
		});
		
		JPanel bp = new JPanel();
		bp.add(btnCreate);
		bp.add(btnCancel);
		
		add(panel, BorderLayout.CENTER);
		add(bp, BorderLayout.PAGE_END);
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		uTextField.requestFocusInWindow();
		setVisible(true);
	}
	
	/**
	 * Validate input, then handle the create action
	 */
	private void doCreate()
	{
		try
		{
			if (validateInput())
			{
				store(uTextField.getText().trim());
				storeUserId();
				
				if (userCreated)
				{
					JOptionPane.showMessageDialog(null, "New User Created!\n**IMPORTANT** Don't forget to remember the master password you just created.\nThere is no way to recover it if you forget!", "Create Account", JOptionPane.ERROR_MESSAGE);
					jt.setText(uTextField.getText().trim());
					pf.requestFocus();
					dispose();
				}
			}
			else
			{
				MasterPassword.clearPassword(pw1);
				MasterPassword.clearPassword(pw2);
				pTextField.setText("");
				pTextField2.setText("");
			}
		} 
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error creating new user!" + e.getMessage(), "Create Account", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Validate the user entered data
	 * 
	 * @return true if data is ok, false if not
	 */
	private boolean validateInput()
	{
		String usr = uTextField.getText().trim();
		pw1 = pTextField.getPassword();
		pw2 = pTextField2.getPassword();
		
		//check username length
		if (usr.length() < 5 || usr.length() > 20)
		{
			JOptionPane.showMessageDialog(null, "Username must be 5 - 20 characters in length", "Create User", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//check user characters
		if (Pattern.matches("((?=.*\\W).{5,20})", usr))
		{
			JOptionPane.showMessageDialog(null, "Username must be [a-z][A-Z][0-9]!", "Create User", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//check the passwords match
		if (!Arrays.equals(pw1, pw2))
		{
			JOptionPane.showMessageDialog(null, "Passwords don't match!", "Create User", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//check password length
		if (pw1.length < 12 || pw1.length > 20)
		{
			JOptionPane.showMessageDialog(null, "Password must be 12 - 20 characters in length!", "Create User", JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(null, "Password must have 3 of the following: uppercase, lowercase, number, or special character!", "Create User", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//end
		
//		// Test for a number
//		if (Pattern.matches("((?=.*\\d).{12,20})", pw1))
//		{
//			test++;
//		}
//		
//		// Test for a lowercase
//		if (Pattern.matches("((?=.*[a-z]).{12,20})", pw1))
//		{
//			test++;
//		}
//		
//		// Test for an uppercase
//		if (Pattern.matches("((?=.*[A-Z]).{12,20})", pw1))
//		{
//			test++;
//		}
//		
//		// Test for a special character
//		if (Pattern.matches("((?=.*\\p{Punct}).{12,20})", pw1))
//		{
//			test++;
//		}
		
		//check password characters
//		if (!Pattern.matches("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{12,20})", pw1))
//		{
//			JOptionPane.showMessageDialog(null, "Password must have 1 uppercase, 1 lowercase, 1 number, and 1 special character!", "Create User", JOptionPane.ERROR_MESSAGE);
//			return false;
//		}

		MasterPassword.clearPassword(pw2);
		return true;
	}
	
	/**
	 * Store the hashed password
	 * 
	 * @param uname
	 * @param pw
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void store(String uname) throws FileNotFoundException,
										IOException, NoSuchAlgorithmException
	{
		String passHash = MasterPassword.doHash(pw1);
		EncryptedProperties ep = new EncryptedProperties(".//config//user.file", null, false);
		
		// Make sure there's not already a username
		if (ep.getPropertyValue(uname.toUpperCase()) == null)
		{
			ep.addProperty(uname.toUpperCase(), passHash);
			userCreated = true;
		}
		else
		{
			JOptionPane.showMessageDialog(null, "User Id: " + uTextField.getText().trim() + " is already in use!", 
											"Login", JOptionPane.ERROR_MESSAGE);
			userCreated = false;
		}
	}
	
	/**
	 * Store the userid map file
	 */
	private void storeUserId()
	{
		EncryptedProperties ep = new EncryptedProperties(".//config//user.mp", pw1);
		String newid = getNewId();
		ep.addProperty(uTextField.getText().trim().toUpperCase(), pw1[0] + pw1[1] + pw1[2] + "~~~~~" + newid + "~~~~~TTT~~~~~0");
		
		// Create the new file for storage
		try
		{
			File f = new File(".//data//" + newid + ".dat");
			f.createNewFile();
		}
		catch (IOException e)
		{
			System.out.println("Failure to create new Entity File");
			userCreated = false;
		}
	}
	
	/**
	 * Get a new id
	 * 
	 * @return new id
	 */
	public static String getNewId()
	{
//		Random randomGen = new Random();
//		String num = "";
//		
//		for (int i = 0; i < 12; i++)
//		{
//			num += randomGen.nextInt(10);
//		}
		
		// Just get a UUID and grab the first 14 chararcters
		return java.util.UUID.randomUUID().toString().substring(0, 14);
	}
	
	
	public static void main(String[] args)
	{
		new CreateUser(null, null);
	}
}
