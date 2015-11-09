package entityManagement;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import userManagement.User;
import encryption.EncryptedProperties;

/**
 * Class to handle all entity stuff
 * 
 * @author MDWhite
 */
public class Entity
{
	private User user;
	private String entityID;
	private String entityName;
	private String website;
	private Date mostRecent;
	private String newestString;
	private String currUsername = "";
	private EncryptedProperties entProp;  //<entityid>.dat - datetime=user~~~~~password
	
	private static final String savedFormatString = "yyyyDDDHHmmss";
	private static final String oldFormatString = "MMddyyyyHHmmss";
	private static final String displayFormatString = "dd MMM yyyy HH:mm:ss";
	private static SimpleDateFormat savedDateFormat = new SimpleDateFormat(savedFormatString);
	private static SimpleDateFormat oldSavedDateFormat = new SimpleDateFormat(oldFormatString);
	private static SimpleDateFormat displayedDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
	
	/**
	 * Constructor
	 * 
	 * @param user
	 * @param entityID
	 * @param entityName
	 */
	public Entity(User user, String entityID, String entityName)
	{
		this(user, entityID, entityName, "");
	}
	
	/**
	 * Constructor
	 * 
	 * @param user
	 * @param entityID
	 * @param entityName
	 * @param website
	 */
	public Entity(User user, String entityID, String entityName, String website)
	{
		this.user = user;
		this.entityID = entityID;
		this.entityName = entityName;
		this.website = website;
		entProp = new EncryptedProperties(".//data//" + entityID + ".dat", user.getMaster());
	}
	
	/**
	 * Add a new username/password combination to this entity
	 * 
	 * @param newUser
	 * @param newPw
	 * @return the date/user/pw info in a Vector
	 */
	public Vector<String> addUPCombo(String newUser, String newPw)
	{
		Vector<String> info = new Vector<String>();
		Date newDate = new Date();
		String savedTime = savedDateFormat.format(newDate);
		//String dispTime = displayedDateFormat.format(newDate);
		
		//add to list
		entProp.addProperty(savedTime, newUser + "~~~~~" + newPw);
		info.add(savedTime);
		info.add(newUser);
		info.add(newPw);
		currUsername = newUser;
		return info;
	}
	
	/**
	 * Delete a username/password combination from this entity
	 * 
	 * @param time associated with the username/password combination
	 */
	public void delUPCombo(String time)
	{
		Date date = getPwDate(time);
		
		//remove from file
		if (entProp.containsKey(time))
		{
			entProp.removeProperty(time);
		}
		else
		{
			// Try the old Date format
			entProp.removeProperty(oldSavedDateFormat.format(date));
		}
	}
	
	/**
	 * Get the all the username/password combinations associated with this entity
	 * 
	 * @return all the combinations in a vector of strings
	 */
	public Vector<Vector<String>> getCombos()
	{
		// Grab the keyset of the HashMap and loop through
		Set<Object> hsh = entProp.keySet();
		Iterator<Object> it = hsh.iterator();
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		
		while (it.hasNext())
		{
			// Pull each value out and store it in a vector
			Vector<String> combo = new Vector<String>();
			String key = (String)it.next();
			Date date = getPwDate(key);
			String[] usrpw = entProp.getPropertyValue(key).split("~~~~~");
			
			// Find the most recent combination so we can keep track of the current username
			if (mostRecent == null)
			{
				mostRecent = date;
				currUsername = usrpw[0];
			}
			else
			{
				if (date.after(mostRecent))
				{
					mostRecent = date;
					currUsername = usrpw[0];
				}
			}
			
			// Store the data in the vector
			combo.add(key);
			combo.add(usrpw[0]);
			combo.add(usrpw[1]);
			data.add(combo);
		}
		
		return data;
	}
	
	/**
	 * @return the entityName
	 */
	public String getEntityName()
	{
		return entityName;
	}

	/**
	 * @param entityName new name for this entity
	 */
	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}
	
	/**
	 * @return true if entity file is deleted successfully
	 */
	public boolean deleteEntityFile()
	{
		return entProp.deletePropFile();
	}
	
	/**
	 * @return entityID
	 */
	public String getId()
	{
		return entityID;
	}
	
	/**
	 * Set the website for this entity
	 * 
	 * @param site
	 */
	public void setWeb(String site)
	{
		website = site;
	}
	
	/**
	 * @return latest entered username for this entity
	 */
	public String getCurrUsername()
	{
		return currUsername;
	}

	/**
	 * Update everything of the user password change for the Entity 
	 * @throws IOException 
	 */
	public boolean updateMaster() throws IOException
	{
		// Delete old file if one exists
		File entFile = new File(".//tmp//" + entityID + ".dat");
		if (entFile.exists())
		{
			if (!entFile.delete())
			{
				return false;
			}
		}
		
		// Create a new temp file for copying the data into
		if (!entFile.createNewFile())
		{
			return false;
		}
		
		// Load old prop file and loop through it to get the data out
		EncryptedProperties tmpProp = new EncryptedProperties(".//tmp//" + entityID + ".dat", user.getMaster());
		Set<Object> hsh = entProp.keySet();
		Iterator<Object> it = hsh.iterator();
		
		// Copy the actual values from old file to new file
		while (it.hasNext())
		{
			String key = (String)it.next();
			String usrpw = entProp.getPropertyValue(key);
			tmpProp.addProperty(key, usrpw);
		}

		return true;
	}
	
	/**
	 * replaces the file for master password change
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean replaceFile() throws Exception
	{
		File entFile = new File(".//tmp//" + entityID + ".dat");
		
		try
		{
			// Delete the old Properties file
			File oldProp = new File(".//data//" + entityID + ".dat");
			
			// Rename the new properties file from temp.dat to entityid.dat and refresh entProp with changes
			if (oldProp.delete())
			{
				if (entFile.renameTo(new File(".//data//" + entityID + ".dat")))
				{
					entProp = new EncryptedProperties(".//data//" + entityID + ".dat", user.getMaster());
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				// Give up and delete the newly created file
				entFile.delete();
				return false;
			}
		}
		catch (Exception e)
		{
			// Delete the newly created file
			entFile.delete();
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Delete all the user/password combos except the most recent
	 * 
	 * @return true if successful
	 */
	public void deleteAllButCurrent()
	{
		ArrayList<String> dates = getNewest();
		
		for (String datetime : dates)
		{
			if (!datetime.equals(newestString))
			{
				entProp.removeProperty(datetime);
			}
		}
	}
	
	/**
	 * Finds the newest combo and saves it in newestString.
	 * 
	 * @return arraylist of all the keys (or date Strings)
	 */
	private ArrayList<String> getNewest()
	{
		Date newest = null;
		ArrayList<String> dates = new ArrayList<String>();
		
		// Grab the keyset of the HashMap and loop through
		Set<Object> hsh = entProp.keySet();
		Iterator<Object> it = hsh.iterator();
		
		while (it.hasNext())
		{
			// Add the key to the list and test for the newest combo
			String key = (String)it.next();
			Date date = getPwDate(key);
			dates.add(key);
			
			// Find the most recent combination so we can keep track of the current date
			if (newest == null)
			{
				newest = date;
				newestString = key;
			}
			else
			{
				if (date.after(newest))
				{
					newest = date;
					newestString = key;
				}
			}
		}
		
		return dates;
	}
	
	public Date getPwDate(String savedDateString)
	{
		try
		{
			if (savedDateString.length() == savedFormatString.length())
			{
				return savedDateFormat.parse(savedDateString);
			}
			else if (savedDateString.length() == oldFormatString.length())
			{
				// Try the old format
				return oldSavedDateFormat.parse(savedDateString);
			}
			else if (savedDateString.length() == displayFormatString.length())
			{
				return displayedDateFormat.parse(savedDateString);
			}
		}
		catch (ParseException pe)
		{
			System.out.println("Wrong date format!  ");
			pe.printStackTrace();
		}
		
		return null;
	}
	
	public String getDisplayedDateString(Date d)
	{
		return displayedDateFormat.format(d);
	}
}
