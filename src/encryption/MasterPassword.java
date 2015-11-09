package encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * Class to handle hashing of the Master Password and other stuff
 * 
 * @author MDWhite
 */
public class MasterPassword
{
	// Random (but static) salt
	private static byte[] salt = {(byte)0x15,(byte)0x14,(byte)0x03,(byte)0x23,(byte)0x22,(byte)0x19,(byte)0x29,(byte)0x32};
	
	/**
	 * Hash the given password
	 * 
	 * @param clearPassword
	 * @return hashed password
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String doHash(char[] clearPassword) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		digest.reset();
		digest.update(salt);
		byte[] bytePW = new byte[clearPassword.length];
		
		for (int i = 0; i < bytePW.length; i++)
		{
			bytePW[i] = (byte) clearPassword[i];
		}

		byte[] input = digest.digest(bytePW);
		
		for (int i = 0; i < 10000; i++) 
		{
			digest.reset();
			input = digest.digest(input);
		}
		
		BASE64Encoder enc = new BASE64Encoder();
		return enc.encode(input);
	}
	
	/**
	 * Clears the given char array by writing zeros to each position
	 * @param pw
	 */
	public static void clearPassword(char[] pw)
	{
		for (int i = 0; i < pw.length; i++)
		{
			pw[i] = 0;
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			System.out.println(doHash(new char[] {'a','b','C','D','3',','}));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
