package IBM.panorama.jdbc.dwr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import IBM.panorama.LoaderFactory;
import IBM.panorama.dbUtility.Category;
import IBM.panorama.jdbc.GeneralService;
import IBM.panorama.jdbc.release_4_4.InvestigationCreator_4_4;
import lombok.Getter;

@Service
@RemoteProxy
@Getter
public class Consumer
{

	@Autowired
	InvestigationCreator_4_4 inv;

	@Autowired
	GeneralService generalService;

	@Autowired
	LoaderFactory factory;

	private static String EMPTY = "";

	@Value("${sql.query.adjustCursors}")
	private String adjustCursorsSql;

	private List<Category> createdCategories = new ArrayList<Category>();

	@RemoteMethod
	public String run(List<Category> categories, String release)
	{
		try
		{
			if (!validateSelections(categories))
			{
				return "Nothing were selected";
			}
			generalService.adjustCursors(adjustCursorsSql);

			createCategories(categories, release);

			clearCreatedCategories();

			generalService.cleanup();
		}

		catch (Exception e)

		{
			return e.getMessage();
		}

		return EMPTY;
	}

	@RemoteMethod
	public String stopProcessing(String release)
	{
		getCreatedCategories().stream().forEach(e -> stopCategory(e, release));
		clearCreatedCategories();
		return EMPTY;
	}

	private void clearCreatedCategories()
	{
		getCreatedCategories().clear();
	}

	private boolean validateSelections(List<Category> categories)
	{
		return categories.stream().anyMatch(e -> e.isSelected());

	}

	private void createCategories(List<Category> categories, String release)
	{
		categories.parallelStream().filter(e -> e.isSelected()).forEach(e -> createCategory(e, release));
	}

	private void createCategory(Category e, String release)
	{
		try
		{
			registerCreatedCategory(e);

			factory.createCategory(e, release);

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e1)
		{
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
	}

	private void registerCreatedCategory(Category category)
	{
		getCreatedCategories().add(category);
	}

	private void stopCategory(Category e, String release)
	{
		try
		{
			factory.getCategory(e, release).interrupt();
		} catch (ClassNotFoundException | IllegalAccessException e1)
		{
			throw new RuntimeException(e1.getMessage());
		}
	}

}