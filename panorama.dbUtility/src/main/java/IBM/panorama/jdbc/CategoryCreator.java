package IBM.panorama.jdbc;

import java.sql.SQLException;

import IBM.panorama.dbUtility.Category;

public interface CategoryCreator
{
	public static final String ID_TOKEN = "_id";

	public void create(Category info) throws SQLException;

	public void interrupt();

	public default String getId()
	{
		int pos = getClass().getSimpleName().indexOf("Creator");
		return getClass().getSimpleName().substring(0, pos) + ID_TOKEN;
	}
}
