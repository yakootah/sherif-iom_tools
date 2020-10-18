package ibm.converter.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class FileLoader
{

	private static final String SOURCE_DIR = "sourceDir";
	private static final String DESTINATION_DIR = "destinatoinDir";
	private static final String COMMENT_MARK = "#";
	private static final String DELIMITER_MARK = "=";
	private static final String CFG_FILE = "/sources/xWiki.cfg";
	private static final String PAGE = "page";
	private static final String HREF = "href";
	private static final String NEW_LINE = "\n";
	private static final String HEADER_PATTERN = "h[1-6]";
	private static final String EQUAL = "=";
	private static final String XML = ".xml";
	private static final String STOP = "\\.";
	private static final String IMAGE = "img";
	private static final String SOURCE = "src";
	private static final char SEPARATOR = File.separatorChar;
	private static final Object TABLE = "table";
	private static final Object ROOT = "root";

	private static Map<String, String> configMap = new HashMap<String, String>();

	static
	{
		loadConfigFile();
	}

	public static void convertFile()
	{
		StringBuilder attachmentBuf;
		// String file = getListOfFiles().get(1040);
		for (File file : getListOfFiles())
		{
			attachmentBuf = new StringBuilder();
			String data = getWholeFileContent(file);
			String navigation;
			// String title = Jsoup.parse(data).title();
			// title = XwikiHelper.getTitle(title);
			try
			{
				navigation = data.split("</nav>")[0];
				data = data.split("</nav>")[1];
				XwikiHelper.buildHierachyMap(navigation.split("</nav>")[0]);
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
				reference = getConfigMap().get(ROOT) + XwikiHelper.DOT + value[1];
			} catch (Exception e)
			{
				continue;
			}
			writeFile(XwikiHelper.buildWikiPage(value[0], reference, traverse(data, attachmentBuf), attachmentBuf),
					file);
		}
	}

	public static String traverse(String data, StringBuilder attachmentBuf)
	{
		StringBuilder buf = new StringBuilder();
		Element body = Jsoup.parse(data).body();
		handleElement(body, buf, attachmentBuf);
		return buf.toString();
		// .replaceAll("&nbsp;", " ");
	}

	private static void handleElement(Element body, StringBuilder buf, StringBuilder attachmentBuf)
	{
		for (int i = 0; i < body.childNodeSize(); i++)
		{
			Node node = body.childNode(i);
			if (node instanceof TextNode)
			{
				buf.append(NEW_LINE).append((node).toString());
			} else if (node instanceof Element)
			{
				if (node.hasAttr(PAGE))
				{
					buildTextNodeForInternalAnchor(node, buf);
				} else if (node.hasAttr(HREF))
				{
					buildTextNodeForExternalAnchor(node, buf);
				} else if (node.nodeName().matches(HEADER_PATTERN))
				{
					buildTextNodeForHeaderElement(node, buf);
				} else if (node.nodeName().equals(IMAGE))
				{
					buildTextNodeForImageElement(node, buf, attachmentBuf);
				} else if (node.nodeName().equals(TABLE))
				{
					XwikiHelper.buildTable((Element) node, buf);
				} else
				{
					handleElement((Element) node, buf, attachmentBuf);
				}
			}
		}

	}

	private static void buildTextNodeForInternalAnchor(Node node, StringBuilder buf)
	{
		String link = node.childNode(0).toString();
		link = link.replaceAll("&", "&amp;");

		buf.append("[[").append(node.attr(PAGE)).append(">>").append(link).append("]]");

	}

	private static void buildTextNodeForExternalAnchor(Node node, StringBuilder buf)
	{
		String link = node.attr(HREF);
		link = link.replaceAll("&", "&amp;");
		buf.append("[[").append(node.childNode(0).toString()).append(">>").append(link).append("]]");
	}

	private static void buildTextNodeForHeaderElement(Node node, StringBuilder buf)
	{
		int count = Integer.parseInt(node.nodeName().substring(1));
		for (int i = 0; i < count; i++)
		{
			buf.append(EQUAL);
		}
		buf.append(Jsoup.parse(node.outerHtml()).body().wholeText());
		for (int i = 0; i < count; i++)
		{
			buf.append(EQUAL);
		}

	}

	private static void buildTextNodeForImageElement(Node node, StringBuilder buf, StringBuilder attachmentBuf)
	{
		try
		{
			String src = node.attr(SOURCE);
			if (src.trim().startsWith("images"))
			{
				File file = new File(configMap.get(SOURCE_DIR) + SEPARATOR + src);

				buf.append(XwikiHelper.getTextImage(file, node));
				XwikiHelper.buildAttachment(file, attachmentBuf);

			} else if (src.trim().startsWith("http"))
			{
				String dummyName = String.valueOf(LocalDateTime.now().getNano());
				buf.append(XwikiHelper.getTextImage(dummyName, node));

			} else if (src.trim().contains("base64,"))
			{
				String dummyName = String.valueOf(LocalDateTime.now().getNano());
				buf.append(XwikiHelper.getTextImage(dummyName, node));
				String imgBody = src.split("base64,")[1];
				XwikiHelper.buildAttachment(dummyName, imgBody, attachmentBuf);

			}
		} catch (FileNotFoundException e)
		{
			// ignore
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static List<File> getListOfFiles()
	{
		File dir = new File(configMap.get(SOURCE_DIR));
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
			File myObj = new File(fileName);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine())
			{
				lines.add(myReader.nextLine());
			}
			myReader.close();
		} catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return lines;
	}

	private static String getWholeFileContent(File myObj)
	{
		StringBuilder buf = new StringBuilder(1 - 000 - 000);
		try
		{
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine())
			{
				buf.append(myReader.nextLine()).append(NEW_LINE);
			}
			myReader.close();
		} catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
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
			configMap.put(property, line.split(DELIMITER_MARK)[1]);
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

	public static void writeFile(String data, File file)
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

}
