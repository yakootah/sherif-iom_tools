package ibm.converter.main;

import ibm.converter.utilities.FileHandler;

public class Main
{

	public static void main(String[] args)
	{
		try
		{
			FileHandler.convertFile();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
