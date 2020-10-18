package ibm.converter.main;

import ibm.converter.utilities.FileLoader;

public class Main
{

	public static void main(String[] args)
	{
		try
		{
			FileLoader.convertFile();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
