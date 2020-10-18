package IBM.panorama.jdbc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import IBM.panorama.dbUtility.DataObject;
import IBM.panorama.dbUtility.Release;

public class Handy
{
	private static final String COMMA = ",";

	private static final String ID_IN_RANGE = "id in (";

	private static final String ID = "id";

	private static final String CLOSE_PARANTHEIS = ")";

	public static final String DASH = "-";

	private static final String WHERE = " where (";

	private static final String AND = " and ";

	private static final String OR = " or ";

	private static final String ID_BETWEEN = " id between ";

	private static final String NUMBER_PATTERN = "\\d+";

	private static final String Range_PATTERN = "\\d+-\\d+";

	public static final String SEPARATOR = ":";

	public static final String EMPTY = "";

	public static final String DUMMY_PROVIDER = "Dummy Provider";

	public static long getRandomValue(List<Long> list)
	{
		if (list.size() == 0)
		{
			return -1;
		}
		Random rand = new Random();

		int rand1 = rand.nextInt(list.size());

		return list.get(rand1);
	}

	public static long[] getRandomTuple(List<long[]> list)
	{
		if (list.size() == 0)
		{
			return new long[]
			{ -1l, -1l };
		}
		Random rand = new Random();

		int rand1 = rand.nextInt(list.size());

		return list.get(rand1);
	}

	public static String getRandomString(List<String> list)
	{
		if (list.size() == 0)
		{
			return EMPTY;
		}
		Random rand = new Random();

		int rand1 = rand.nextInt(list.size());

		return list.get(rand1);
	}

	public static String getUniqueString()
	{
		return String.valueOf(LocalDate.now().toEpochDay());
	}

	public static Date getRandomDate(int period)
	{
		LocalDate date;
		if (period == 0)
		{
			date = LocalDate.now();
		} else
		{
			Random rand = new Random();

			int rand1 = rand.nextInt(period);

			date = LocalDate.now().minusDays(rand1);
		}
		return Date.valueOf(date);
	}

	public static int compareName(DataObject o1, DataObject o2)
	{
		return o1.getName().compareTo(o2.getName());
	}

	public static int compareReverseReleaseDate(Release o1, Release o2)
	{
		return o1.getReleaseDate().compareTo(o2.getReleaseDate()) * -1;
	}

	public static String replacePlaceholders(String phrase, String... list)
	{
		for (int i = 0; i < list.length; i++)
		{
			phrase = phrase.replace("{" + i + "}", list[i]);
		}
		return phrase;
	}

	public static String getPercentageOfWorkDone(int remaining, int total)
	{
		double value = (total - remaining) / (double) total;
		return (int) (value * 100) + "%";
	}

	public static String getListOfIds(String values, String primaryKey)
	{
		String result = "";
		if (values != null && !values.equals("0"))
		{
			String[] ids = values.split(COMMA);

			String part1 = handleCSVIds(ids);

			String part2 = handleRangeIds(ids);

			result = concatTwoLists(part1, part2);
		}

		return replaceIDTokenByPrimaryKey(primaryKey, result);
	}

	private static String handleRangeIds(String[] ids)
	{
		List<String> range = Arrays.asList(ids).stream().filter(e -> e.contains(DASH)).collect(Collectors.toList());

		if (isListViolatePattern(range, Range_PATTERN))
		{
			throw new RuntimeException("Range value is not properly formatted");
		}

		String part2 = convertRangeToBetweenClause(range);
		return part2;
	}

	private static String handleCSVIds(String[] ids)
	{
		List<String> csv = Arrays.asList(ids).stream().filter(e -> e.length() > 0 && !e.contains(DASH))
				.collect(Collectors.toList());

		if (isListViolatePattern(csv, NUMBER_PATTERN))
		{
			throw new RuntimeException("Range value is not properly formatted");
		}

		String part1 = convertIdArraytoInClause(csv);
		return part1;
	}

	private static boolean isListViolatePattern(List<String> csv, String pattern)
	{
		return csv.stream().anyMatch(e -> !e.replaceAll(" ", "").matches(pattern));

	}

	private static String replaceIDTokenByPrimaryKey(String primaryKey, String result)
	{
		return result.replace(ID, primaryKey);
	}

	private static String concatTwoLists(String part1, String part2)
	{
		String result = "";
		if ((part1.length() > 0) || (part2.length() > 0))
		{
			result += WHERE;

			if ((part1.length() > 0) && (part2.length() > 0))
			{
				part1 += OR;
			}

			if (part2.endsWith(OR))
			{
				part2 = part2.substring(0, part2.length() - 4);
			}
			result += part1 + part2 + CLOSE_PARANTHEIS;
		}
		return result;
	}

	private static String convertIdArraytoInClause(List<String> values)
	{
		StringBuilder builder = new StringBuilder();

		if (values.size() > 0)
		{
			builder.append(ID_IN_RANGE);

			for (String id : values)
			{
				builder.append(id).append(COMMA);
			}
			builder.deleteCharAt(builder.length() - 1);

			builder.append(CLOSE_PARANTHEIS);
		}
		return builder.toString();
	}

	public static String convertRangeToBetweenClause(List<String> values)
	{
		StringBuilder builder = new StringBuilder();

		if (values.size() > 0)
		{
			for (String range : values)
			{
				builder.append(ID_BETWEEN);
				builder.append(range.replace(DASH, AND));
				builder.append(OR);
			}
		}
		return builder.toString();
	}

	public static boolean isEmptyString(String data)
	{
		boolean result = true;
		if (data != null)
		{
			if (data.trim().length() > 0)
			{
				result = false;
			}
		}
		return result;
	}

	public static List<long[]> filterOutList2FromList1(List<long[]> list1, List<Long> list2)
	{
		return list1.stream().filter(e -> list2.stream().noneMatch(f -> e[2] == f)).collect(Collectors.toList());

	}

}
