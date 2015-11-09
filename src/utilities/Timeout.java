package utilities;

import java.util.Calendar;
import java.util.Date;

/**
 * @author MDWhite
 */
public class Timeout
{
	private int timeoutMinutes = 10;
	private Calendar firstDate;
	private Calendar currDate;
	private boolean cancel = false;
	
	/**
	 * Class for timing out application if user doesn't do anything
	 */
	public Timeout()
	{
		firstDate = Calendar.getInstance();
		firstDate.setTime(new Date());
		firstDate.add(Calendar.MINUTE, timeoutMinutes);
		Thread tm = new Thread(new MakeTime());
		tm.start();
	}
	
	/**
	 * Updates the previous time
	 */
	public void updateTime()
	{
		firstDate.setTime(new Date());
		firstDate.add(Calendar.MINUTE, timeoutMinutes);
	}
	
	/**
	 * Sets the cancel so that next time the timer checks, it will break the loop and finish
	 */
	public void cancelTime()
	{
		cancel = true;
	}
	
	/**
	 * Thread functionality for checking the time
	 */
	public class MakeTime implements Runnable
	{
		public void run()
		{
			currDate = Calendar.getInstance();
			while (true)
			{
				currDate.setTime(new Date());
				if (firstDate.after(currDate))
				{
					try
					{
						Thread.sleep(120000);
						
						// Check to see if this timer got disabled
						if (cancel)
						{
							break;
						}
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					System.exit(0);
				}
			}
		}
	}
}
