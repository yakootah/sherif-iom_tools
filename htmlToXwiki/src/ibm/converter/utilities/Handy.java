package ibm.converter.utilities;

public class Handy
{

	public static boolean isEmptyString(String data)
	{
		boolean result = true;
		if (data != null)
		{
			if (data.trim().length() > 0)
			{
				result = false;
			}
		}
		return result;
	}
}
