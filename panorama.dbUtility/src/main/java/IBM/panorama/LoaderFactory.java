package IBM.panorama;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import IBM.panorama.dbUtility.Category;
import IBM.panorama.jdbc.CategoryCreator;

@Component
public class LoaderFactory
{
	private final static String UNDER_SCORE = "_";
	private final static String CREATOR = "Creator";
	private final static String DOT = ".";

	@Autowired
	private ApplicationContext context;

	public void createCategory(Category category, String release)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
	{
		getCategory(category, release).create(category);
	}

	private String buildClassName(Category category, String release)
	{
		String packageName = "IBM.panorama.jdbc.release" + UNDER_SCORE + release;
		return packageName + DOT + category.getName().trim() + CREATOR + UNDER_SCORE + release;
	}

	public CategoryCreator getCategory(Category category, String release)
			throws ClassNotFoundException, IllegalAccessException
	{
		Class<?> myClass = Class.forName(buildClassName(category, release));
		return ((CategoryCreator) context.getBean(myClass));
	}

}
