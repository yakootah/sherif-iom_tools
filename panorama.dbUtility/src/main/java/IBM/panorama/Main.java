package IBM.panorama;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import IBM.panorama.dbUtility.Category;
import IBM.panorama.dbUtility.CategoryService;
import IBM.panorama.dbUtility.Release;
import IBM.panorama.dbUtility.ReleaseService;
import lombok.Getter;
import lombok.Setter;

@Controller
@Getter
@Setter
public class Main
{

	@Value("${application.module}")
	private String module;

	@Value("${application.utility}")
	private String utility;

	@Value("${application.version}")
	private String version;

	@Autowired
	CategoryService categoryService;

	@Autowired
	ReleaseService releaseService;

	private List<Category> getTableList()
	{
		return categoryService.getAll();
	}

	private List<Release> getReleases()
	{
		return releaseService.getAll();
	}

	@RequestMapping("/")
	public String welcome(Map<String, Object> model)
	{
		model.put("list", getTableList());
		model.put("utility", this.utility);
		model.put("module", this.module);
		model.put("version", this.version);
		model.put("releases", this.getReleases());
		return "index";

	}

}