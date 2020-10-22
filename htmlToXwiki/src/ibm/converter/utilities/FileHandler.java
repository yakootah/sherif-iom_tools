package ibm.converter.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class FileHandler
{

	private static final String SOURCE_DIR = "sourceDir";
	private static final String DESTINATION_DIR = "destinationDir";
	private static final String COMMENT_MARK = "#";
	private static final String DELIMITER_MARK = "=";
	private static final String CFG_FILE = "/sources/xWiki.cfg";
	private static final String JSON_LINK_FILE = "jsonLinkFilePath";
	private static final String FILE_ON_SERVER_TOKEN = "w3FilesLocation";
	private static final String NEW_LINE = "\n";
	private static final String XML = ".xml";
	private static final String ROOT = "root";
	private static final String XWIKI_FILE_MANAGER_LOCATION = "xWikiFileManagerLocation";
	private static final String XWIKI_XML_BASE_LOCATION = "xWikiXmlBaseLocation";
	private static final String NONE = "None";

	private static final String FAILURE_HEADER_DIR = "failureHeaderDir";
	private static final String FAILURE_BODY_DIR = "failureBodyDir";

	private static Map<String, String> configMap = new HashMap<String, String>();
	private static Map<String, String> fileMap = new HashMap<String, String>();
	static
	{
		loadConfigFile();
		readJsonFile();
	}

	public static void convertFiles()
	{
		StringBuilder attachmentBuf;
		// File file = getListOfFiles().get(859);
		for (File file : getListOfFiles())
		{
			attachmentBuf = new StringBuilder();
			String data = getWholeFileContent(file);
			String navigation;
			try
			{
				navigation = data.split("</nav>")[0];
				data = data.split("</nav>")[1];
				XwikiHelper.buildHierachyMap(navigation);
			} catch (ArrayIndexOutOfBoundsException e)
			{
				try
				{
					data = data.split("</head>")[1];
				} catch (Exception e1)
				{
					// swallow
				}
			}

			String value[] = XwikiHelper.getHierarchyMap().get(file.getName());
			String reference = "";
			try
			{
				reference = value[1];
			} catch (Exception e)
			{
				continue;
			}
			data = XwikiHelper.processLinks(data);
			writeFile(XwikiHelper.buildWikiPageUsingHtmlMacro(value[0], reference, data, attachmentBuf), file);
		}
	}

	@Deprecated
	public static String traverse(String data, StringBuilder attachmentBuf)
	{
		StringBuilder buf = new StringBuilder();
		Element body = Jsoup.parse(data).body();
		XwikiHelper.handleElement(body, buf, attachmentBuf);
		return buf.toString();
		// .replaceAll("&nbsp;", " ");
	}

	public static List<File> getListOfFiles()
	{
		File dir = new File(configMap.get(SOURCE_DIR));
		return Arrays.asList(dir.listFiles()).stream().filter(e -> e.isFile()).collect(Collectors.toList());

	}

	public static List<File> getListOfGeneratedFiles()
	{
		File dir = new File(configMap.get(DESTINATION_DIR));
		return Arrays.asList(dir.listFiles()).stream().filter(e -> e.isFile()).collect(Collectors.toList());

	}

	private static void loadConfigFile()
	{
		String userDir = System.getProperty("user.dir");
		loadConfigParameters(getFileContentAsListOfLines(userDir + CFG_FILE));
	}

	private static List<String> getFileContentAsListOfLines(String fileName)
	{
		List<String> lines = new ArrayList<String>();
		try
		{
			BufferedReader myReader = new BufferedReader(new FileReader(new File(fileName)));
			String st;
			while ((st = myReader.readLine()) != null)
			{
				lines.add(st);
			}
			myReader.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return lines;
	}

	private static String getWholeFileContent(File myObj)
	{
		StringBuilder buf = new StringBuilder();
		try
		{
			BufferedReader myReader = new BufferedReader(new FileReader(myObj));
			String st;
			while ((st = myReader.readLine()) != null)
			{
				buf.append(st).append(NEW_LINE);
			}
			myReader.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return buf.toString();
	}

	public static void loadConfigParameters(List<String> lines)
	{
		lines.stream().forEach(e -> updateConfigParameter(e));
	}

	private static void updateConfigParameter(String line)
	{
		String property = line.split(DELIMITER_MARK)[0];
		if (isNotComment(property))
		{
			String value = line.split(DELIMITER_MARK)[1];
			configMap.put(property, value.equals(NONE) ? XwikiHelper.EMPTY : value);
		}

	}

	private static boolean isNotComment(String property)
	{
		return !property.trim().startsWith(COMMENT_MARK);
	}

	public static Map<String, String> getConfigMap()
	{
		return configMap;
	}

	private static void writeFile(String data, File file)
	{
		try
		{
			createDestinationDirectory();
			String newFileName = replaceExtension(file.getAbsolutePath());

			FileWriter myWriter = new FileWriter(switchDirectories(newFileName));
			myWriter.write(data);
			myWriter.close();
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static String switchDirectories(String fileName)
	{
		return fileName.replace(getConfigMap().get(SOURCE_DIR), getConfigMap().get(DESTINATION_DIR));
	}

	private static void createDestinationDirectory()
	{
		new File(getConfigMap().get(DESTINATION_DIR)).mkdirs();

	}

	private static String replaceExtension(String fileName)
	{
		int pos = fileName.lastIndexOf(XwikiHelper.DOT);
		return fileName.substring(0, pos) + XML;

	}

	static String getSourceDirectory()
	{
		return getConfigMap().get(SOURCE_DIR);
	}

	static String getXwikiFileManagerLocation()
	{
		return getConfigMap().get(XWIKI_FILE_MANAGER_LOCATION);
	}

	static String getXwikiXmlBaseLocation()
	{
		return getConfigMap().get(XWIKI_XML_BASE_LOCATION);
	}

	static String getW3FilesLocation()
	{
		return getConfigMap().get(FILE_ON_SERVER_TOKEN);
	}

	static String getPagePathRoot()
	{
		return getConfigMap().get(ROOT);
	}

	@SuppressWarnings("unchecked")
	public static void readJsonFile()
	{
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader(getConfigMap().get(JSON_LINK_FILE)))
		{
			Object obj = jsonParser.parse(reader);

			JSONArray fileList = (JSONArray) obj;

			fileList.stream().forEach(file -> parseFileObject((JSONObject) file));

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void parseFileObject(JSONObject obj)
	{
		String file = (String) obj.get("link");
		String link = (String) obj.get("file");
		getFileMap().put(file, link);

	}

	private static Map<String, String> getFileMap()
	{
		return fileMap;
	}

	static String getAnchorFileName(String partialLink)
	{
		Collection<String> list = getFileMap().keySet();
		Optional<String> option = list.stream().filter(link -> link.contains(partialLink)).findAny();
		return getFileMap().get(option.orElse(XwikiHelper.EMPTY));

	}

	public static void generateFailureFiles()
	{
		getListOfGeneratedFiles().stream().forEach(e -> createFailureFiles(e));
	}

	private static void createFailureFiles(File file)
	{
		String fileName = XwikiHelper.getSimpleFileName(file.getAbsolutePath());
		createFailureHeaderFile(fileName);
		createFailureBodyFile(fileName);
	}

	private static void createFailureHeaderFile(String file)
	{
		try
		{
			String fileLoc = getConfigMap().get(FAILURE_HEADER_DIR) + file;

			FileWriter myWriter = new FileWriter(fileLoc);

			String content = getWholeFileContent(
					new File(getConfigMap().get(DESTINATION_DIR) + XwikiHelper.SEPARATOR + file));
			content = content.split("\\{\\{html\\}\\}")[0] + content.split("\\{\\{/html\\}\\}")[1];
			myWriter.write(content);
			myWriter.close();
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void createFailureBodyFile(String file)
	{
		try
		{
			String fileLoc = getConfigMap().get(FAILURE_BODY_DIR) + XwikiHelper.SEPARATOR + file;

			FileWriter myWriter = new FileWriter(fileLoc);

			String content = getWholeFileContent(
					new File(getConfigMap().get(DESTINATION_DIR) + XwikiHelper.SEPARATOR + file));
			content = content.split("\\{\\{html\\}\\}")[1].split("\\{\\{/html\\}\\}")[0];
			content = "{{html}}" + NEW_LINE + content + NEW_LINE + "{{/html}}";
			content = content.replaceAll("&lt;", "<");
			myWriter.write(content);
			myWriter.close();
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
