package IBM.panorama.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager
{
	private static Connection con;

	public static void closeSqlConnection()
	{
		if (con != null)
		{
			try
			{
				con.close();
				con = null;
			} catch (SQLException e1)
			{
				System.out.println("Close Connection Failed.");
			}
		}
	}

	public static void commit()
	{
		if (con != null)
		{
			try
			{
				con.commit();

			} catch (SQLException e1)
			{
				System.out.println("Close Connection Failed.");
			}
		}
	}

	public static String getMaxIdSql(String primaryKey, String tableNameWithScema)
	{
		return "select max (" + primaryKey + ") id from " + tableNameWithScema;
	}

	public static void closePreparedStatement(PreparedStatement preparedStatement) throws SQLException
	{
		if (preparedStatement != null)
		{
			preparedStatement.executeBatch();
			preparedStatement.close();
		}
		getSqlConnection().commit();
	}

	public static void closeStatement(Statement statement) throws SQLException
	{
		if (statement != null)
		{
			statement.executeBatch();
			statement.close();
		}
		getSqlConnection().commit();
	}

	public static long findCodedConceptId(String code, String oid)
	{
		String sql = "select (coded_concept_id) id from iterm.coded_concept where code = '" + code
				+ "' and secured_IND = 'Y' and is_Deleted_ind = 'N' and code_set_oid = '" + oid + "'";
		return fetchIdColumn(sql);
	}

	public static String getLimitedCandidateListSql(String primaryKey, String tableNameWithScema, String range,
			String maxRows)
	{
		String idList = Handy.getListOfIds(range, primaryKey);
		if (idList.length() > 0)
		{
			return "select distinct (" + primaryKey + ") id from " + tableNameWithScema + idList + " and rowNum < "
					+ maxRows;
		}
		return "select distinct (" + primaryKey + ") id from " + tableNameWithScema + " where rowNum < " + maxRows;

	}

	public static List<Long> getSDL(Long orgId)
	{
		String sql = "select service_delivery_location_id id from PLOEPHS.service_delivery_location where organizational_unit_id = "
				+ orgId + " and is_deleted_indicator = 'N'";

		return fetchIdColumnValues(sql);
	}

	public static List<Long> getConceptCodeList(String oid)
	{
		String sql = "select coded_concept_id id from iterm.coded_concept where code_set_oid = '" + oid
				+ "' and is_deleted_ind = 'N'";

		return fetchIdColumnValues(sql);
	}

	public static List<Long> getConceptCodeListUsingValueSet(String valueSet)
	{
		String sql = "select distinct coded_concept_id id from ITERM.coded_concept where coded_concept_id in ( select coded_concept_id from ITERM.value_set_flat where value_set_id in (select value_set_id from ITERM.value_set where name ='"
				+ valueSet + "')) and is_deleted_ind = 'N'";

		return fetchIdColumnValues(sql);
	}

	public static String getLimitedCandidateListSql(String primaryKey, String secondaryColumn,
			String tableNameWithScema, String range, String maxRows)
	{
		String idList = Handy.getListOfIds(range, primaryKey);
		if (idList.length() > 0)
		{
			return "select distinct (" + primaryKey + ") id, (" + secondaryColumn + ") secondary from "
					+ tableNameWithScema + idList + " and rowNum < " + maxRows;
		}
		return "select distinct (" + primaryKey + ") id, (" + secondaryColumn + ") secondary from " + tableNameWithScema
				+ " where rowNum < " + maxRows;

	}

	public static String getLimitedCandidateListSql(String primaryKey, String secondaryColumn, String subjectId,
			String tableNameWithScema, String range, String maxRows)
	{
		String idList = Handy.getListOfIds(range, primaryKey);
		if (idList.length() > 0)
		{
			return "select distinct (" + primaryKey + ") id, (" + secondaryColumn + ") secondary, (" + subjectId
					+ ") ternary from " + tableNameWithScema + idList + " and rowNum < " + maxRows;
		}
		return "select distinct (" + primaryKey + ") id, (" + secondaryColumn + ") secondary, (" + subjectId
				+ ") ternary from " + tableNameWithScema + " where rowNum < " + maxRows;

	}

	public static String getCandidateListSql(String primaryKey, String tableNameWithScema)
	{
		return "select distinct (" + primaryKey + ") id from " + tableNameWithScema;
	}

	public static Connection getSqlConnection() throws SQLException
	{
		if (con == null)
		{
			String url = "jdbc:oracle:thin:@localhost:1521/EPHSW01";
			String user = "system";
			String pass = "pdwrkstn";
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

			con = DriverManager.getConnection(url, user, pass);

			con.setAutoCommit(false);

		}
		return con;
	}

	public static void updateBatch(List<String> sqls)
	{
		try
		{
			Statement stmt = getSqlConnection().createStatement();

			for (String sql : sqls)
			{
				stmt.addBatch(sql);
			}

			stmt.executeBatch();

			getSqlConnection().commit();

		}

		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("close Connection");
			closeSqlConnection();
			throw new RuntimeException("update failed");
		}
	}

	public static void executeStatment(String sql)
	{
		try
		{
			Statement stmt = getSqlConnection().createStatement();

			stmt.execute(sql);

			getSqlConnection().commit();

		}

		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("close Connection");
			closeSqlConnection();
			throw new RuntimeException("update failed");
		}
	}

	public static long fetchIdColumn(String sql)
	{
		List<Long> list = new ArrayList<>();
		try
		{
			Statement stmt = getSqlConnection().createStatement();

			ResultSet set = stmt.executeQuery(sql);

			while (set.next())
			{
				list.add(set.getLong("id"));
			}

		} catch (SQLException e)
		{
			System.out.println("Fetch max Id Failed");
			closeSqlConnection();
		}
		return list.get(0);
	}

	public static List<Long> fetchIdColumnValues(String sql)
	{
		List<Long> list = new ArrayList<>();
		try
		{

			Statement stmt = getSqlConnection().createStatement();

			ResultSet set = stmt.executeQuery(sql);

			while (set.next())
			{
				list.add(set.getLong("id"));
			}

		} catch (SQLException e)
		{
			System.out.println("Fetch id list Failed");
			closeSqlConnection();
		}
		return list;
	}

	public static List<long[]> fetchIdAndSecondaryIdColumnValues(String sql)
	{
		List<long[]> list = new ArrayList<>();
		try
		{
			Statement stmt = getSqlConnection().createStatement();

			ResultSet set = stmt.executeQuery(sql);

			while (set.next())
			{
				list.add(new long[]
				{ set.getLong("id"), set.getLong("secondary") });
			}

		} catch (SQLException e)
		{
			System.out.println("Fetch id list Failed");
			closeSqlConnection();
		}
		return list;
	}

	public static List<long[]> fetch3IdsColumnValues(String sql)
	{
		List<long[]> list = new ArrayList<>();
		try
		{
			Statement stmt = getSqlConnection().createStatement();

			ResultSet set = stmt.executeQuery(sql);

			while (set.next())
			{
				list.add(new long[]
				{ set.getLong("id"), set.getLong("secondary"), set.getLong("ternary") });
			}

		} catch (SQLException e)
		{
			System.out.println("Fetch id list Failed");
			closeSqlConnection();
		}
		return list;
	}
}
