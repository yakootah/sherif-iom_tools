package ibm.converter.main;

import ibm.converter.utilities.FileHandler;

public class Main
{

	public static void main(String[] args)
	{
		try
		{
			FileHandler.convertFiles();
			FileHandler.generateFailureFiles();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
