package ibm.converter.utilities;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class XwikiHelper
{
	private static final String DELIMITER = ":";
	private static final String SemiColon = ";";
	private static final String ATTACHMENT_NOTE = "Page Attachments:";
	private static final String HEIGHT = "height";
	private static final String WIDTH = "width";
	private static final String CELL_STYLE = "(% style=\"border-color: black; ${width}\" %)";
	private static final String STYLE = "style";
	private static final String SPACE = " ";
	private static final String HREF = "href";
	private static final String LIST_ITEM = "li";
	private static final String NAV = "nav";
	private static final String ANCHOR = "a";
	static final String DOT = ".";
	private static final String NEW_LINE = "\n";
	private static final String UNDER_LINE = "_";
	private static Map<String, String[]> map = new HashMap<String, String[]>();

	private static String getXwikiPageHeader(String title, String reference)
	{
		String header = "<?xml version='1.1' encoding='UTF-8'?>\r\n"
				+ "<xwikidoc version=\"1.3\" reference=\"${reference}\" locale=\"\">\r\n" + "  <language/>\r\n"
				+ "  <defaultLanguage>en</defaultLanguage>\r\n" + "  <translation>0</translation>\r\n"
				+ "  <creator>XWiki.Admin</creator>\r\n" + "  <creationDate>1602726761000</creationDate>\r\n"
				+ "<parent>" + FileLoader.getConfigMap().get("parent") + "</parent>\r\n" + "<author>"
				+ FileLoader.getConfigMap().get("author") + "</author>\r\n" + "  <contentAuthor>"
				+ FileLoader.getConfigMap().get("contentAuthor") + "</contentAuthor>\r\n"
				+ "  <date>1602728773000</date>\r\n" + "  <contentUpdateDate>1602728773000</contentUpdateDate>\r\n"
				+ "  <version>3.1</version>\r\n" + "  <title>${title}</title>\r\n" + "  <comment/>\r\n"
				+ "  <minorEdit>false</minorEdit>\r\n" + "  <syntaxId>xwiki/2.1</syntaxId>\r\n"
				+ "  <hidden>false</hidden>\r\n"
				+ "  <content>{{box cssClass=\"floatinginfobox\" title=\"**Contents**\"}}\r\n" + "{{toc/}}\r\n"
				+ "{{/box}}\r\n" + "\r\n";

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

	/*
	 * public static String getTitle(String title) { if (title.contains(DASH)) {
	 * return title.split("-")[0].trim(); } return title; }
	 */

	public static String getTextImage(File file, Node node) throws IOException
	{
		return "[[image:" + file.getName() + "||" + getDimension(node) + "]]";
	}

	public static String getTextImage(String file, Node node) throws IOException
	{
		return "[[image:" + file + "||" + getDimension(node) + "]]";
	}

	public static void buildAttachment(File file, StringBuilder attachmentBuf) throws IOException
	{
		attachmentBuf.append("\r\n<attachment>\r\n").append("<filename>").append(file.getName())
				.append("</filename>\r\n").append("<author>").append(FileLoader.getConfigMap().get("author"))
				.append("</author>\r\n").append("<date>1602781230000</date>\r\n").append("<version>1.1</version>\r\n")
				.append("<comment/>\r\n").append("<content>\r\n");
		attachmentBuf.append(transformImageToText(file)).append(getTextImageTrailer());
	}

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

	public static void buildAttachment(String file, String body, StringBuilder attachmentBuf) throws IOException
	{
		attachmentBuf.append("\r\n<attachment>\r\n").append("<filename>").append(file).append("</filename>\r\n")
				.append("<author>").append(FileLoader.getConfigMap().get("author")).append("</author>\r\n")
				.append("<date>1602781230000</date>\r\n").append("<version>1.1</version>\r\n").append("<comment/>\r\n")
				.append("<content>\r\n");
		attachmentBuf.append(body).append(getTextImageTrailer());
	}

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
		data = data.replaceAll("Â", "");
		data = data.replaceAll("&nbsp;", ",");
		data = data.replaceAll("&", ",");
		data = data.replaceAll("\\<", "&lt;");
		// data = StringEscapeUtils.escapeHtml4(data);
		return removeImportToolAttatmentNotes(data);
	}

	private static String cleanHeader(String data)
	{
		data = data.replaceAll("Â", "");
		data = data.replaceAll("&nbsp;", ",");
		return data.replaceAll("&", ",");
	}

	public static void buildTable(Element table, StringBuilder buf)
	{
		addTableStyle(table, buf);
		Elements headers = table.select("thead");
		buildTableHeader(buf, headers);

		Elements bodies = table.select("tbody");
		buildTableRows(buf, bodies);
	}

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

	private static StringBuilder updateCellStyleWithWidth(StringBuilder buf, Element col)
	{
		String style = CELL_STYLE.replace("${width}", getCellWidth(col));
		return buf.append(style);
	}

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
			return;
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
		output = output.replaceAll("\\.", UNDER_LINE);
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
}
