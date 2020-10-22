package ibm.converter.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class XwikiHelper
{
	private static final String DELIMITER = ":";
	private static final String SemiColon = ";";
	private static final String ATTACHMENT_NOTE = "Page Attachments:";
	private static final String HEIGHT = "height";
	private static final String WIDTH = "width";
	private static final String EQUAL = "=";
	private static final String CELL_STYLE = "(% style=\"border-color: black; ${width}\" %)";
	private static final String STYLE = "style";
	private static final String SPACE = " ";
	private static final String LIST_ITEM = "li";
	private static final String NAV = "nav";
	private static final String ANCHOR = "a";
	private static final String PAGE = "page";
	private static final String HREF = "href";
	private static final String HEADER_PATTERN = "h[1-6]";
	private static final Object TABLE = "table";
	private static final String IMAGE = "img";
	private static final String SOURCE = "src";
	private static final String ANCHOR_FILE_ATTRIBUTE = "files";
	static final String SEPARATOR = String.valueOf(File.separatorChar);
	private static final String TITLE = "title";
	private static final String VIEW_DETAILS = "View details";
	private static final String HTTP = "http";
	private static final String ARIAL = "Arial";
	static final String DOT = ".";
	private static final String NEW_LINE = "\n";
	private static final String UNDER_LINE = "_";
	private static final String URL_SEPARATOR = "/";
	static final String EMPTY = "";
	private static final String QUOTES = "\"";
	private static Map<String, String[]> map = new HashMap<String, String[]>();

	static String getXwikiPageHeader(String title, String reference)
	{
		String header = "<?xml version='1.1' encoding='UTF-8'?>\r\n"
				+ "<xwikidoc version=\"1.3\" reference=\"${reference}\" locale=\"\">\r\n" + "  <language/>\r\n"
				+ "  <defaultLanguage>en</defaultLanguage>\r\n" + "  <translation>0</translation>\r\n"
				+ "  <creator>XWiki.Admin</creator>\r\n" + "  <creationDate>1602726761000</creationDate>\r\n"
				+ "<parent>" + FileHandler.getConfigMap().get("parent") + "</parent>\r\n" + "<author>"
				+ FileHandler.getConfigMap().get("author") + "</author>\r\n" + "  <contentAuthor>"
				+ FileHandler.getConfigMap().get("contentAuthor") + "</contentAuthor>\r\n"
				+ "  <date>1602728773000</date>\r\n" + "  <contentUpdateDate>1602728773000</contentUpdateDate>\r\n"
				+ "  <version>3.1</version>\r\n" + "  <title>${title}</title>\r\n" + "  <comment/>\r\n"
				+ "  <minorEdit>false</minorEdit>\r\n" + "  <syntaxId>xwiki/2.1</syntaxId>\r\n"
				+ "  <hidden>false</hidden>\r\n" + "  <content>\r\n";

		return header.replace("${title}", title).replace("${reference}", reference);
	}

	public static String buildWikiPage(String title, String reference, String body, StringBuilder attachmentBuf)
	{
		return cleanHeader(getXwikiPageHeader(title, reference)) + cleanData(body) + getTail(attachmentBuf);
	}

	public static String getTail(StringBuilder attachmentBuf)
	{
		if (attachmentBuf.length() == 0)
		{
			return "\r\n</content>\r\n" + "</xwikidoc>";
		} else
		{
			return "\r\n</content>\r\n" + attachmentBuf.toString() + "</xwikidoc>";
		}
	}

	public static String getTail()
	{

		return "\r\n</content>\r\n" + "</xwikidoc>";

	}

	@Deprecated
	public static String getTextImage(File file, Node node) throws IOException
	{
		return "[[image:" + file.getName() + "||" + getDimension(node) + "]]";
	}

	@Deprecated
	public static String getTextImage(String file, Node node) throws IOException
	{
		return "[[image:" + file + "||" + getDimension(node) + "]]";
	}

	@Deprecated
	public static void buildAttachment(File file, StringBuilder attachmentBuf) throws IOException
	{
		attachmentBuf.append("\r\n<attachment>\r\n").append("<filename>").append(file.getName())
				.append("</filename>\r\n").append("<author>").append(FileHandler.getConfigMap().get("author"))
				.append("</author>\r\n").append("<date>1602781230000</date>\r\n").append("<version>1.1</version>\r\n")
				.append("<comment/>\r\n").append("<content>\r\n");
		attachmentBuf.append(transformImageToText(file)).append(getTextImageTrailer());
	}

	@Deprecated
	private static String getDimension(Node node)
	{
		String dimension = "";
		if (!Handy.isEmptyString(node.attr(HEIGHT)))
		{
			dimension += " height=\"" + (node.attr(HEIGHT)) + "\"";
		}
		if (!Handy.isEmptyString(node.attr(WIDTH)))
		{
			dimension += " width=\"" + (node.attr(WIDTH)) + "\"";
		}
		return dimension;
	}

	@Deprecated
	public static void buildAttachment(String file, String body, StringBuilder attachmentBuf) throws IOException
	{
		attachmentBuf.append("\r\n<attachment>\r\n").append("<filename>").append(file).append("</filename>\r\n")
				.append("<author>").append(FileHandler.getConfigMap().get("author")).append("</author>\r\n")
				.append("<date>1602781230000</date>\r\n").append("<version>1.1</version>\r\n").append("<comment/>\r\n")
				.append("<content>\r\n");
		attachmentBuf.append(body).append(getTextImageTrailer());
	}

	@Deprecated
	private static String transformImageToText(File file) throws IOException
	{
		String fileName = cleanupImagePath(file.getAbsolutePath());
		byte[] fileContent = FileUtils.readFileToByteArray(new File(fileName));
		return Base64.getEncoder().encodeToString(fileContent);
	}

	private static String cleanupImagePath(String absoluteFile)
	{
		return absoluteFile.split("\\?")[0];

	}

	@Deprecated
	private static String getTextImageTrailer() throws IOException
	{
		return "</content>\r\n" + "</attachment>\r\n";
	}

	private static String removeImportToolAttatmentNotes(String str)
	{
		return str.split(ATTACHMENT_NOTE)[0];
	}

	private static String cleanData(String data)
	{
		data = data.replaceAll("Â", EMPTY);
		data = data.replaceAll("&nbsp;", EMPTY);
		// data = data.replaceAll("&", ",");
		data = data.replaceAll("<", "&lt;");
		data = data.replaceAll("-webkit-text-stroke-width:0px", EMPTY);
		data = data.replaceAll("&quot;Helvetica Neue&quot;", ARIAL);
		data = data.replaceAll("â€˜", QUOTES);
		data = data.replaceAll("â€™", QUOTES);
		data = data.replaceAll("â€œ", QUOTES);
		data = data.replaceAll("â€", QUOTES);
		data = data.replace("&gt;", ">");
		return removeImportToolAttatmentNotes(data);
	}

	static String cleanHeader(String data)
	{
		data = data.replaceAll("Â", "");
		data = data.replaceAll("&nbsp;", ",");
		return data.replaceAll("&", ",");
	}

	@Deprecated
	public static void buildTable(Element table, StringBuilder buf)
	{
		addTableStyle(table, buf);
		Elements headers = table.select("thead");
		buildTableHeader(buf, headers);

		Elements bodies = table.select("tbody");
		buildTableRows(buf, bodies);
	}

	@Deprecated
	private static void addTableStyle(Element table, StringBuilder buf)
	{
		if (!Handy.isEmptyString(table.attr(WIDTH)))
		{
			buf.append("\n(% border=\"2\" style=\"width:" + (table.attr(WIDTH)) + "px\" %)\n");
		} else
		{
			buf.append("\n(% border=\"2\" %)\n");
		}
	}

	@Deprecated
	private static void buildTableHeader(StringBuilder buf, Elements headers)
	{
		if (headers != null && headers.size() > 0)
		{
			Elements rows = headers.get(0).select("tr");
			if (rows != null && rows.size() > 0)
			{
				Elements cols = rows.get(0).select("th");

				for (int i = 0; i < cols.size(); i++)
				{
					buf.append("|=");
					updateCellStyleWithWidth(buf, cols.get(i)).append(cols.get(i).text());
				}
				buf.append(NEW_LINE);
			}
		}

	}

	@Deprecated
	private static void buildTableRows(StringBuilder buf, Elements bodies)
	{
		if (bodies != null && bodies.size() > 0)
		{
			Elements rows = bodies.get(0).select("tr");

			for (int i = 0; i < rows.size(); i++)
			{
				Element row = rows.get(i);
				Elements cols = row.select("th");
				Elements tds = row.select("td");
				cols.addAll(tds);

				for (int j = 0; j < cols.size(); j++)
				{
					buf.append("|");
					updateCellStyleWithWidth(buf, cols.get(j)).append(cols.get(j).text());
				}
				buf.append(NEW_LINE);
			}
		}
	}

	@Deprecated
	private static StringBuilder updateCellStyleWithWidth(StringBuilder buf, Element col)
	{
		String style = CELL_STYLE.replace("${width}", getCellWidth(col));
		return buf.append(style);
	}

	@Deprecated
	private static String getCellWidth(Element col)
	{
		String width = "";
		String style = col.attr(STYLE);
		if (!Handy.isEmptyString(style))
		{
			if (style.indexOf(WIDTH) > -1)
			{
				width = style.split(WIDTH)[1];
				int startPos = width.indexOf(DELIMITER) + 1;
				int endPos = width.indexOf(SemiColon) > startPos ? width.indexOf(SemiColon) : width.length();
				width = WIDTH + DELIMITER + SPACE + width.substring(startPos, endPos).trim();

			}
		}
		return width;
	}

	public static void buildHierachyMap(String nav)
	{
		if (getHierarchyMap().size() == 0)
		{
			Element node = Jsoup.parse(nav).body();

			node.select(NAV).select(ANCHOR).stream().forEach(e -> storeNode(e));
		}
	}

	private static void storeNode(Element anchor)
	{
		StringBuilder buf = new StringBuilder();
		getPath(anchor, buf);
		removeTrailingDotFrombuf(buf);
		getHierarchyMap().put(anchor.attr(HREF), new String[]
		{ anchor.text(), buf.toString() });
	}

	private static void removeTrailingDotFrombuf(StringBuilder buf)
	{
		if (buf.length() > 0)
		{
			buf.deleteCharAt(buf.length() - 1);
		}

	}

	private static void getPath(Element node, StringBuilder buf)
	{
		if (node.tag().getName().equals(NAV))
		{
			buf.insert(0, FileHandler.getPagePathRoot());
		} else if (node.tag().getName().equals(LIST_ITEM))
		{
			buf.insert(0, massageTheCuurrentLevelPath(node));
			getPath(node.parent(), buf);
		} else
		{
			getPath(node.parent(), buf);
		}

	}

	private static String massageTheCuurrentLevelPath(Element node)
	{
		int pos = node.select(ANCHOR).attr(HREF).lastIndexOf(DOT);
		String output = node.select(ANCHOR).attr(HREF).substring(0, pos);
		output = output.replaceAll("\\" + DOT, UNDER_LINE);
		return output + DOT;
	}

	public static Map<String, String[]> getHierarchyMap()
	{
		if (map == null)
		{
			map = new HashMap<String, String[]>();
		}
		return map;
	}

	@Deprecated
	static void handleElement(Element body, StringBuilder buf, StringBuilder attachmentBuf)
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

	@Deprecated
	private static void buildTextNodeForImageElement(Node node, StringBuilder buf, StringBuilder attachmentBuf)
	{
		try
		{
			String src = node.attr(SOURCE);
			if (src.trim().startsWith("images"))
			{
				File file = new File(FileHandler.getSourceDirectory() + SEPARATOR + src);

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

	@Deprecated
	private static void buildTextNodeForInternalAnchor(Node node, StringBuilder buf)
	{
		String link = node.childNode(0).toString();
		link = link.replaceAll("&", "&amp;");

		buf.append("[[").append(node.attr(PAGE)).append(">>").append(link).append("]]");

	}

	@Deprecated
	private static void buildTextNodeForExternalAnchor(Node node, StringBuilder buf)
	{
		String link = node.attr(HREF);
		link = link.replaceAll("&", "&amp;");
		buf.append("[[").append(node.childNode(0).toString()).append(">>").append(link).append("]]");
	}

	@Deprecated
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

	public static String buildWikiPageUsingHtmlMacro(String title, String reference, String body,
			StringBuilder attachmentBuf)
	{
		return cleanHeader(getXwikiPageHeader(title, reference)) + addHtmlMacroTag(body) + getTail(attachmentBuf);
	}

	private static String addHtmlMacroTag(String data)
	{
		return "{{html}}" + NEW_LINE + cleanData(data) + NEW_LINE + "{{/html}}";
	}

	public static String processLinks(String data)
	{
		Document doc = Jsoup.parse(data);

		processAllImages(doc);

		processAllAnchors(doc);

		return doc.body().outerHtml();
	}

	public static void processAllImages(Document doc)
	{

		Elements imgs = doc.getElementsByTag(IMAGE);

		imgs.stream().forEach(e -> processImageSource(e));

	}

	private static void processImageSource(Node e)
	{
		String src = e.attr(SOURCE);
		try
		{
			if (src.trim().startsWith("images"))
			{
				src = transformImageToTextWithMeta(new File(FileHandler.getSourceDirectory() + SEPARATOR + src));
				e.attr(SOURCE, src);
			}
		} catch (FileNotFoundException e1)
		{
			// ignore
		} catch (IOException e2)
		{
			throw new RuntimeException(e2);
		}
	}

	private static String transformImageToTextWithMeta(File file) throws IOException
	{
		String meta = "data:image;base64,";
		String fileName = cleanupImagePath(file.getAbsolutePath());
		byte[] fileContent = FileUtils.readFileToByteArray(new File(fileName));
		return meta + Base64.getEncoder().encodeToString(fileContent);
	}

	public static void processAllAnchors(Document doc)
	{
		Elements anchors = doc.getElementsByTag(ANCHOR);

		anchors.stream().forEach(e -> processAnchor(e));

	}

	private static void processAnchor(Element e)
	{
		String ref = e.attr(HREF);
		if (isFileOnW3(e))
		{
			e.attr(HREF, buildLinkForFile(ref));
		} else if (isViewDetailLinkOnW3(e))
		{
			e.remove();
		} else if (isLinkOnW3(e))
		{
			buildLinkForXwikiPage(e);
		}
	}

	private static boolean isFileOnW3(Node node)
	{
		return !(Handy.isEmptyString(node.attr(ANCHOR_FILE_ATTRIBUTE)) && Handy.isEmptyString(node.attr("_ic_source")))
				&& isLinkOnW3(node);
	}

	private static String buildLinkForFile(String ref)
	{
		return FileHandler.getXwikiFileManagerLocation() + getFile(ref);
	}

	private static boolean isLinkOnW3(Node node)
	{
		return node.attr(HREF).contains(FileHandler.getW3FilesLocation()) || !node.attr(HREF).contains(HTTP);

	}

	private static boolean isViewDetailLinkOnW3(Node node)
	{
		return isLinkOnW3(node) & node.attr(TITLE).startsWith(VIEW_DETAILS);
	}

	private static void buildLinkForXwikiPage(Node e)
	{
		String ref = e.attr(HREF);
		String path = getPagePath(getFile(ref));
		if (Handy.isEmptyString(path))
		{
			e.attr("Style", "background-color:red; color:white;");
			return;
		}
		e.attr(HREF, path);
	}

	static String getFile(String ref)
	{
		String[] parts = ref.split("/");
		String filePart = parts[parts.length - 1];
		return cleanupImagePath(filePart);
	}

	static String getSimpleFileName(String filePath)
	{
		String[] parts = filePath.split("\\\\");
		String filePart = parts[parts.length - 1];
		return cleanupImagePath(filePart);
	}

	static String getPagePath(String fileName)
	{
		try
		{
			String pagePath = getHierarchyMap().get(fileName)[1];
			String filePath = convertPagePathToFilePath(pagePath);
			return FileHandler.getXwikiXmlBaseLocation() + filePath;
		} catch (Exception e)
		{
			String file = FileHandler.getAnchorFileName(fileName);
			if (Handy.isEmptyString(file))
			{
				return EMPTY;
			}
			return FileHandler.getXwikiFileManagerLocation() + file;
		}
	}

	static String convertPagePathToFilePath(String path)
	{
		return path.replaceAll("\\" + DOT, URL_SEPARATOR);
	}

}
