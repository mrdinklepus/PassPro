package userManagement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import utilities.Timeout;

import encryption.EncryptedProperties;
import encryption.MasterPassword;
import entityManagement.Entity;

/**
 * Holds all the user info 
 * 
 * @author MDWhite
 */
public class User 
{
	private String userid;
	private String username;
	private char[] masterPW;
	private HashMap<String, String[]> entityMetadata; //Name:[id][web]
	private HashMap<String,Entity> entities = new HashMap<String,Entity>(); //Name:Entity
	private EncryptedProperties entitymp; //<userid>.dat - entityname=masterPW.substring(0,3)~~~~~entityid~~~~~web
	private EncryptedProperties usr; //user.mp - username=masterPW.substring(0,3)~~~~~userid~~~~~history/backups/timeout~~~~~count
	private boolean saveHistory;
	private boolean showBackups;
	private boolean showReminder;
	private boolean timeout;
	private int count;
	private int countInterval = 10;
	private Timeout timer;
	
	/**
	 * Constructor
	 * 
	 * @param username
	 * @param password
	 */
	public User(String username, char[] password)
	{
		this.username = username.toUpperCase();
		this.masterPW = password;
		
		initUser();
	}
	
	/**
	 * Initialize all the data and properties files
	 */
	public void initUser()
	{
		usr = new EncryptedProperties(".//config//user.mp", masterPW);
		String[] uid = usr.getPropertyValue(username).split("~~~~~");
		userid = uid[1];
		saveHistory = string2boolean(uid[2].substring(0, 1));
		showBackups = string2boolean(uid[2].substring(1, 2));
		timeout = string2boolean(uid[2].substring(2, 3));
		count = Integer.parseInt(uid[3]);
		
		// Show a backup reminder if enabled
		if (showBackups)
		{
			if (count == countInterval)
			{
				showReminder = true;
				count = 0;
			}
			else
			{
				count++;
			}
			
			usr.updateProperty(username, uid[0] + "~~~~~" + uid[1] + "~~~~~" + uid[2] + "~~~~~" + count);
		}
		
		entitymp = new EncryptedProperties(".//data//" + userid + ".dat", masterPW);
		entityMetadata = new HashMap<String, String[]>();
		
		readEntityFiles();
		
		// Start timeout
		if (timeout)
		{
			timer = new Timeout();
		}
	}
	
	/**
	 * Fetches and populates Entities and their data for this user
	 */
	public void readEntityFiles()
	{
		Set<Object> hsh = entitymp.keySet();
		Iterator<Object> it = hsh.iterator();
		
		while (it.hasNext())
		{
			String name = (String)it.next();
			String[] temp = entitymp.getPropertyValue(name).split("~~~~~");
			String[] id = new String[2];
			id[0] = temp[1];
			
			if (temp.length == 3)
			{
				id[1] = temp[2];
			}
			else
			{
				id[1] = "";
			}
			
			entityMetadata.put(name, id);
			entities.put(name, new Entity(this, id[0], name, id[1]));
		}
	}
	
	/**
	 * Create a new entity for this user
	 * 
	 * @param entityName
	 * @param web
	 * @param newUserName
	 * @param pw
	 */
	public void createNewEntity(String entityName, String web, String newUserName, String pw)
	{
		//Fetch a new id for this entity and add info to prop file
		String id = CreateUser.getNewId();
		
		//Create a file on the file system for storing the data
		File entFile = new File(".//data//" + id + ".dat");
		
		if (!entFile.exists())
		{
			try
			{
				entFile.createNewFile();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error creating file for new entity!" + e.getMessage());
				return;
			}
		}
		else
		{
			//Get a new id and try again
			id = CreateUser.getNewId();
			entFile = new File(".//data//" + id + ".dat");
			
			try
			{
				entFile.createNewFile();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error creating file for new entity!" + e.getMessage());
				return;
			}
		}
		
		entitymp.addProperty(entityName, masterPW[0] + masterPW[1] + masterPW[2] + "~~~~~" + id + "~~~~~" + web);
		
		Entity ent = new Entity(this, id, entityName, web);
		entities.put(entityName, ent);
		
		String[] idweb = {id, web};
		entityMetadata.put(entityName, idweb);
		
		ent.addUPCombo(newUserName, pw);
	}
	
	/**
	 * Deletes the given entity from this user
	 * 
	 * @param entityName
	 */
	public void deleteEntity(String entityName)
	{
		//First delete file, then remove from all current objects
		if (entities.get(entityName).deleteEntityFile())
		{
			entities.remove(entityName);
			entitymp.removeProperty(entityName);
			entityMetadata.remove(entityName);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Error deleting Entity.  Unable to delete Entity File for " + entityName);
		}		
	}
	
	/**
	 * Updates the Entity website
	 * 
	 * @param entityName - name of entity
	 * @param site - updated website
	 */
	public void updateWebsite(String entityName, String site)
	{
		Entity ent = getEntity(entityName);
		entitymp.updateProperty(entityName, masterPW[0] + masterPW[1] + masterPW[2] + "~~~~~" + ent.getId() + "~~~~~" + site);
		String[] entdata = entityMetadata.get(entityName);
		entdata[1] = site;
		ent.setWeb(site);
	}
	
	/**
	 * Launch the change master password dialog
	 */
	public void updateMaster()
	{		
		new ChangeMaster(this);
	}
	
	/**
	 * Re-encrypts entity property files using the new master password
	 * 
	 * @param newMaster
	 * @return true if successful, false otherwise
	 */
	public boolean updateMasterForAllEntities(char[] newMaster)
	{
		MasterPassword.clearPassword(masterPW);
		masterPW = newMaster;
		
		try
		{
			//create a copy of prop file using new master password
			createTmpFileForUpdate();
			
			// Loop through entities and have them update their property files using the new password
			ArrayList<Entity> ents = new ArrayList<Entity>(entities.values());
			
			// for each entity, create temp file and prep for change
			for (Entity ent: ents)
			{
				if (!ent.updateMaster())
				{
					return false;
				}
			}
			
			// Delete the old Properties file
			File entFile = new File(".//tmp//temp.dat");
			File oldProp = new File(".//data//" + userid + ".dat");
			
			// Rename the new properties file from temp.dat to userid.dat and refresh enititymp with changes
			if (oldProp.delete())
			{
				if (entFile.renameTo(new File(".//data//" + userid + ".dat")))
				{
					entitymp = new EncryptedProperties(".//data//" + userid + ".dat", masterPW);
				}
				else
				{
					return false;
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Error deleting old Entity file.");
				entFile.delete();
				return false;
			}

			// Loop through and replace the old files
			for (Entity ent: ents)
			{
				if (!ent.replaceFile())
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error changing password. Ensure psermissions are set on file directory. " + e.getMessage());
			return false;
		}

		return true;
	}
	
	/*
	 * Creates a temp Properties file (duplicate of entitymp) using new password encryption
	 */
	private boolean createTmpFileForUpdate() throws IOException
	{
		//Create temp file and load it into a properties file
		File entFile = new File(".//tmp//temp.dat");
		if (entFile.exists())
		{
			if (!entFile.delete())
			{
				return false;
			}
		}
		
		if (!entFile.createNewFile())
		{
			return false;
		}
		
		EncryptedProperties tmpProp = new EncryptedProperties(".//tmp//temp.dat", masterPW);
		
		//Iterate through old entitymp and store each value into tmpProp
		Set<Object> hsh = entitymp.keySet();
		Iterator<Object> it = hsh.iterator();
		
		while (it.hasNext())
		{
			String key = (String)it.next();
			String usrpw = entitymp.getPropertyValue(key);
			tmpProp.addProperty(key, usrpw);
		}
		
		return true;
	}
	
	/**
	 * Display a message to the user to back up their data
	 */
	public void showBackupReminder()
	{
		if (showReminder)
		{
			JOptionPane.showMessageDialog(null, "You have logged in " + countInterval + 
				" times since the last backup reminder.\nIt is a good idea to periodically backup your data to an external drive.\n" +
				"Just drag the PassPro folder to a backup location.\n\nTo disable reminders, access the Options menu on the main window.");
		}
	}
	
	/**
	 * Converts "T" or "F" to boolean
	 * 
	 * @param s
	 * @return true if the string is "T"
	 */
	public boolean string2boolean(String s)
	{
		if (s.equals("T"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Convert a boolean into T or F
	 * 
	 * @param b
	 * @return "T" if true, "F" if otherwise
	 */
	public String bool2string(boolean b)
	{
		if (b)
		{
			return "T";
		}
		else
		{
			return "F";
		}
	}
	
	/**
	 * For each entity, delete all but the most current password
	 * 
	 * @return true if all goes well
	 */
	public boolean deleteAllButCurrent()
	{
		ArrayList<Entity> ents = new ArrayList<Entity>(entities.values());
		
		for (Entity ent:ents)
		{
			ent.deleteAllButCurrent();
		}
		
		return true;
	}
	
	public void updateTimer()
	{
		if (timer != null)
		{
			timer.updateTime();
		}
	}
	
	public Entity getEntity(String name)
	{
		return entities.get(name);
	}
	
	public HashMap<String, String[]> getEntitiesMetadata()
	{
		return entityMetadata;
	}
	
	public char[] getMaster()
	{
		return masterPW;
	}
	
	public boolean hasEntities()
	{
		return !entityMetadata.isEmpty();
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getUserIdString()
	{
		return userid;
	}
	
	public boolean getSaveHistory()
	{
		return saveHistory;
	}
	
	public void setSaveHistory(boolean saveHistory)
	{
		this.saveHistory = saveHistory;
		updateUsermp();
	}
	
	public boolean getShowBackups()
	{
		return showBackups;
	}
	
	public void setShowBackups(boolean showBackups)
	{
		this.showBackups = showBackups;
		count = 0;
		updateUsermp();
	}
	
	public boolean getTimeout()
	{
		return timeout;
	}
	
	/**
	 * Updates the timeout flag and sets the timeout variable
	 * @param timeout
	 */
	public void setTimeout(boolean timeout)
	{
		this.timeout = timeout;
		
		if (timeout)
		{
			timer = new Timeout();
		}
		else
		{
			timer.cancelTime();
		}
		
		updateUsermp();
	}
	
	private void updateUsermp()
	{
		usr.updateProperty(username, masterPW[0] + masterPW[1] + masterPW[2] + "~~~~~" + userid + "~~~~~" + bool2string(saveHistory) + 
				bool2string(showBackups) + bool2string(timeout) + "~~~~~" + count);
	}
}
