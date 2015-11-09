package encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Class to handle reading and writing encryption stuff from file
 * 
 * @author MDWhite
 */
public class EncryptedProperties extends Properties
{
	private static final String TRANSFORMATION = "PBEWithSHA1AndDESede";
	private static final String ALGORITHM = "PBEWithSHA1AndDESede/CBC/PKCS5Padding";
	private Cipher encrypter, decrypter;
	private static byte[] salt = {(byte)0x12,(byte)0x04,(byte)0x16,(byte)0x30,(byte)0x27,(byte)0x21,(byte)0x04,(byte)0x32};
	private int ITERATIONS = 2048;
	private File propFile;
	private boolean isEncrypted = true;
	
	/**
	 * Constructor
	 * 
	 * @param propertiesFile
	 * @param password
	 */
	public EncryptedProperties(String propertiesFile, char[] password)
	{
		this(propertiesFile, password, true);
	}
	
	/**
	 * Constructor
	 * 
	 * @param propertiesFile
	 * @param password
	 * @param encrypted
	 */
	public EncryptedProperties(String propertiesFile, char[] password, boolean encrypted)
	{		
		propFile = new File(propertiesFile);
		
		if (encrypted)
		{
			initCipher(password);
		}
		else
		{
			isEncrypted = false;
		}
		loadProperties();
	}
	
	/**
	 * Empty Constructor for testing
	 */
	public EncryptedProperties()
	{
		try
		{
			// How to tell what algorithms are available
//			for (Provider provider : Security.getProviders())
//			  {
//			   System.out.println("Provider: " + provider.getName());
//			   for (Provider.Service service : provider.getServices())
//			   {
//			    System.out.println("  Algorithm: " + service.getAlgorithm());
//			   }
//			 }
			
			initCipher("password".toCharArray());
			System.out.println(encrypt("dCOgoerXgkE8+C/IwV8p5A\\=\\="));
		}
		catch (Exception e)
		{
			System.out.println(e.getStackTrace());
		}
	}
	
	/**
	 * Initialize all the cipher objects using the password
	 * 
	 * @param password
	 */
	public void initCipher(char[] password)
	{
		try
		{
			PBEParameterSpec spec = new PBEParameterSpec(salt, ITERATIONS);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(TRANSFORMATION);
			SecretKey sKey = skf.generateSecret(new PBEKeySpec(password));
			encrypter = Cipher.getInstance(ALGORITHM);
			decrypter = Cipher.getInstance(ALGORITHM);
			encrypter.init(Cipher.ENCRYPT_MODE, sKey, spec);
			decrypter.init(Cipher.DECRYPT_MODE, sKey, spec);
		}
		catch (Exception e)
		{
			System.out.println("There was a problem initializing the cipher");
		}
	}
	
	/**
	 * Encrypts the given String
	 * 
	 * @param plainTextString
	 * @return encrypted string
	 * @throws UnsupportedEncodingException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	private String encrypt(String plainTextString) throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException
	{
		if (isEncrypted)
		{
			byte[] utf8PlainTextAsBytes = plainTextString.getBytes("UTF-8");
			byte[] encryptedTextAsBytes = encrypter.doFinal(utf8PlainTextAsBytes);
			BASE64Encoder enc = new BASE64Encoder();
			return enc.encode(encryptedTextAsBytes);			
		}
		else
		{
			return plainTextString;
		}
	}
	
	/**
	 * Decrypts the given string
	 * 
	 * @param encryptedString
	 * @return decrypted string
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	private String decrypt(String encryptedString) throws IOException, IllegalBlockSizeException, BadPaddingException
	{
		if (isEncrypted)
		{
			BASE64Decoder dec = new BASE64Decoder();
			byte[] decodedTextAsBytes = dec.decodeBuffer(encryptedString);
			byte[] utf8DecryptedTextAsBytes = decrypter.doFinal(decodedTextAsBytes);
			return new String(utf8DecryptedTextAsBytes, "UTF-8");
		}
		else
		{
			return encryptedString;
		}
	}
	
	/**
	 * Loads all the values from this properties file into memory
	 */
	private void loadProperties()
	{
		if (propFile.exists())
		{
			FileInputStream inStream = null;
			
			try
			{
				inStream = new FileInputStream(propFile);
				load(inStream);
				inStream.close();
			}
			catch (Exception e)
			{
				System.out.println("Error storing property.");
				e.printStackTrace();
				
				try
				{
					inStream.close();
				} catch (Exception ex) {}
			}
		}
		else
		{
			//Message that file doesn't exist
			System.out.println("File " + propFile.getName() + "does not exist!");
		}
	}
	
	/**
	 * Add a new property to this property file and store it
	 * 
	 * @param key
	 * @param val
	 */
	public synchronized void addProperty(String key, String val)
	{
		try
		{
			setProperty(key, encrypt(val));
			storeProperties();
		}
		catch (Exception e)
		{
			System.out.println("Error adding property.  ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a new property to this property file and store it
	 * 
	 * @param key
	 * @param val
	 */
	public synchronized void addUnencryptedProperty(String key, String val)
	{
		try
		{
			setProperty(key, val);
			storeProperties();
		}
		catch (Exception e)
		{
			System.out.println("Error adding property.  ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the given property with the given value and store it
	 * 
	 * @param key
	 * @param val
	 */
	public void updateProperty(String key, String val)
	{
		try
		{
			setProperty(key, encrypt(val));
			storeProperties();
		}
		catch (Exception e)
		{
			System.out.println(e.getStackTrace());
		}
	}
	
	/**
	 * Remove the given property and store it
	 * 
	 * @param key
	 */
	public synchronized void removeProperty(String key)
	{
		remove(key);
		storeProperties();
	}
	
	/**
	 * Write the property values in memory out to the file
	 */
	private synchronized void storeProperties()
	{
		FileOutputStream outStream = null;
		
		try
		{
			outStream = new FileOutputStream(propFile);
			store(outStream, null);
			outStream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error storing property.  ");
			e.printStackTrace();
			
			try
			{
				outStream.close();
			} catch (Exception ex) {}
		}
	}
	
	/**
	 * Get the value associated with the given key
	 * 
	 * @param keyval
	 * @return value that matches this key
	 */
	public String getPropertyValue(String keyval)
	{
		try
		{
			return decrypt(getProperty(keyval));
		}
		catch (Exception e)
		{
			System.out.println("Problem decrypting value.  " + e.getStackTrace());
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Updates the given property in memory only
	 * 
	 * @param key
	 * @param val
	 */
	public void setPropertyWithoutStoring(String key, String val)
	{
		try
		{
			setProperty(key, encrypt(val));
		}
		catch (Exception e)
		{
			System.out.println(e.getStackTrace());
		}
	}
	
	/**
	 * Delete this property file
	 * 
	 * @return true if deleted, false if otherwise
	 */
	public boolean deletePropFile()
	{
		return propFile.delete();
	}
	
	
	public static void main(String[] args)
	{
		//new EncryptedProperties();
		EncryptedProperties e = new EncryptedProperties(".//config//user.mp", "password".toCharArray());
	}
}
