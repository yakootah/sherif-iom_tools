package IBM.panorama.dbUtility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import IBM.panorama.jdbc.Handy;

@Component
public class ReleaseServiceImpl implements ReleaseService
{
	@Autowired
	ReleaseRepository repository;

	public List<Release> getAll()
	{
		final List<Release> releases = new ArrayList<Release>();
		repository.findAll().forEach(e -> releases.add(e));
		releases.sort(Handy::compareReverseReleaseDate);
		return releases;
	}
}
