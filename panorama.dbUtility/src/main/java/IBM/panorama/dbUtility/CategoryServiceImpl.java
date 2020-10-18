package IBM.panorama.dbUtility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import IBM.panorama.jdbc.Handy;

@Component
public class CategoryServiceImpl implements CategoryService
{
	@Autowired
	CategoryRepository repository;

	public List<Category> getAll()
	{
		final List<Category> names = new ArrayList<Category>();
		repository.findAll().forEach(e -> names.add(e));
		names.sort(Handy::compareName);
		names.stream().forEach(e -> sortProperties(e));
		return names;
	}

	private void sortProperties(Category e)
	{
		e.getProperties().sort(Handy::compareName);
	}
}
