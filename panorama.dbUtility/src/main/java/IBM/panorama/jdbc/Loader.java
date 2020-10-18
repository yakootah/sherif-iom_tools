package IBM.panorama.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Loader {
	private static List<Integer> PHYSICAL_ADDRESS_TYPE = Arrays
			.asList(new Integer[] { 431917, 493778, 1018453, 493777 });
	private int clientMaxId;
	private int maxPhysicalId;
	private int maxLiaisonId;
	private int maxSubjectId;
	private int outbreakMaxId;
	private int chunk = 100;
	private int repeat = 1000;

	public static void main(String[] args) throws SQLException {
		try {
			Loader loader = new Loader();

			if (args.length > 0)
				loader.repeat = Integer.parseInt(args[0]);
			if (args.length > 1)
				loader.chunk = Integer.parseInt(args[1]);

			loader.runInsertPhysicalAddress(loader.repeat, loader.chunk);
			loader.runInsertClientAddress(loader.repeat, loader.chunk);
			loader.runInsertNonHumanSubject(loader.repeat, loader.chunk);
			loader.runInsertLiaison(loader.repeat, loader.chunk);
			loader.runInsertOutbreak(loader.repeat, loader.chunk);
			loader.printCleanupStatement();
		} finally {
			System.out.println("close Connection");
			DBManager.closeSqlConnection();
		}
	}

	public void printCleanupStatement() {
		System.out.println("Cleanup Scripts");
		if (clientMaxId > 0)
			System.out.println("delete from client.ADDRESS_XREF where ADDRESS_XREF_id > " + clientMaxId + ";\ncommit;");
		if (maxPhysicalId > 0)
			System.out.println(
					"delete from client.PHYSICAL_ADDRESS where PHYSICAL_ADDRESS_id >" + maxPhysicalId + ";\ncommit;");
		if (maxLiaisonId > 0)
			System.out.println("delete from client.liaison where liaison_id > " + maxLiaisonId + ";\ncommit;");
		if (maxSubjectId > 0)
			System.out.println(
					"delete from client.nonhuman_subject where nonhuman_subject_id > " + maxSubjectId + ";\ncommit;");
		if (outbreakMaxId > 0)
			System.out.println("delete from case.outbreak where outbreak_id > " + outbreakMaxId + ";\ncommit;");
	}

	public void runInsertClientAddress(int repeat, int chunk) throws SQLException {
		System.out.println("insert client address records");
		int id = 0;
		String maxIdSql = DBManager.getMaxIdSql("ADDRESS_XREF_ID", "client.address_xref");
		List<Integer> physicalList = DBManager
				.fetchIdColumnValues(DBManager.getLimitedCandidateListSql("physical_address_id", "client.physical_address"));
		clientMaxId = DBManager.fetchIdColumn(maxIdSql);
		List<Integer> subjectList = DBManager
				.fetchIdColumnValues(DBManager.getLimitedCandidateListSql("nonhuman_subject_id", "client.nonhuman_subject"));
		String sql = "insert into client.address_xref (ADDRESS_XREF_ID,PHYSICAL_ADDRESS_ID,PHYSICAL_ADDRESS_TYPE,CREATED_ON,CREATED_BY,LOCK_SEQ_NUMBER,IS_DELETED_INDICATOR,entity_type,entity_id) "
				+ "values (? , ?, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N',1,?)";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= repeat; i++) {
			id = clientMaxId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, Handy.getRandomValue(physicalList));
			preparedStatement.setLong(3, Handy.getRandomValue(PHYSICAL_ADDRESS_TYPE));
			preparedStatement.setLong(4, Handy.getRandomValue(subjectList));
			preparedStatement.addBatch();

			if (i % chunk == 0) {
				preparedStatement.executeBatch();
				System.out.println("insert " + chunk);
			}
		}
		DBManager.closePreparedStatement(preparedStatement);
		System.out.println("....done");

	}

	public void runInsertPhysicalAddress(int repeat, int chunk) throws SQLException {
		System.out.println("insert physical address records");
		int id = 0;
		String maxIdSql = DBManager.getMaxIdSql("physical_address_id", "client.physical_address");
		maxPhysicalId = DBManager.fetchIdColumn(maxIdSql);
		String sql = "insert into client.physical_address (PHYSICAL_ADDRESS_ID,CREATED_ON,CREATED_BY,LOCK_SEQ_NUMBER,IS_DELETED_INDICATOR) "
				+ "values (?,CURRENT_TIMESTAMP,'DbUtility',0,'N')";

		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= repeat; i++) {
			id = maxPhysicalId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.addBatch();

			if (i % chunk == 0) {
				preparedStatement.executeBatch();
				System.out.println("insert " + chunk);
			}
		}
		DBManager.closePreparedStatement(preparedStatement);
		System.out.println("....done");
	}

	public void runInsertLiaison(int repeat, int chunk) throws SQLException {
		System.out.println("insert liaison records");

		int id = 0;
		String maxIdSql = DBManager.getMaxIdSql("LIAISON_ID", "client.LIAISON");
		List<Integer> subjectList = DBManager
				.fetchIdColumnValues(DBManager.getLimitedCandidateListSql("nonhuman_subject_id", "client.nonhuman_subject"));
		maxLiaisonId = DBManager.fetchIdColumn(maxIdSql);
		String sql = "insert into client.LIAISON (LIAISON_ID, NONHUMAN_SUBJECT_ID,CREATED_ON,CREATED_BY,LOCK_SEQ_NUMBER,IS_DELETED_INDICATOR) "
				+ "values (? , ? ,CURRENT_TIMESTAMP,'DbUtility',0,'N')";

		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= repeat; i++) {
			id = maxLiaisonId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, Handy.getRandomValue(subjectList));
			preparedStatement.addBatch();

			if (i % chunk == 0) {
				preparedStatement.executeBatch();
				System.out.println("insert " + chunk);
			}
		}
		DBManager.closePreparedStatement(preparedStatement);
		System.out.println("....done");
	}

	public void runInsertNonHumanSubject(int repeat, int chunk) throws SQLException {
		System.out.println("insert non human subject records");

		int id = 0;
		String maxIdSql = DBManager.getMaxIdSql("NONHUMAN_SUBJECT_ID", "client.NONHUMAN_SUBJECT");
		List<Integer> snomedList = DBManager.fetchIdColumnValues(
				DBManager.getLimitedCandidateListSql("SNOMED_HIER_CODED_CNCPT_ID", "client.nonhuman_subject"));
		maxSubjectId = DBManager.fetchIdColumn(maxIdSql);
		String sql = "insert into client.NONHUMAN_SUBJECT(NONHUMAN_SUBJECT_ID,SNOMED_HIER_CODED_CNCPT_ID,CREATED_ON,CREATED_BY,LOCK_SEQ_NUMBER,IS_DELETED_INDICATOR) "
				+ "values (? , ? ,CURRENT_TIMESTAMP,'DbUtility',0,'N')";

		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= repeat; i++) {
			id = maxSubjectId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, Handy.getRandomValue(snomedList));
			preparedStatement.addBatch();

			if (i % chunk == 0) {
				preparedStatement.executeBatch();
				System.out.println("insert " + chunk);
			}
		}
		DBManager.closePreparedStatement(preparedStatement);
		System.out.println("....done");

	}

	public void runInsertOutbreak(int repeat, int chunk) throws SQLException {
		System.out.println("insert Outbreak records");
		int id = 0;
		String maxIdSql = DBManager.getMaxIdSql("outbreak_id", "case.outbreak");
		outbreakMaxId = DBManager.fetchIdColumn(maxIdSql);
		String sql = "insert into case.outbreak (outbreak_ID,ENCOUNTER_GROUP_CODE_CNCPT_ID,OUTBREAK_TYPE_CODE_CNCPT_ID, OUTBREAK_STTNG_TYP_CD_CNCPT_ID, OUTBREAK_STATUS_CODE_CNCPT_ID, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ?, ?, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= repeat; i++) {
			id = outbreakMaxId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, 483471);
			preparedStatement.setLong(3, 365603);
			preparedStatement.setLong(4, 1002498);
			preparedStatement.setLong(5, 1002536);
			preparedStatement.addBatch();

			if (i % chunk == 0) {
				preparedStatement.executeBatch();
				System.out.println("insert " + chunk);
			}
		}
		DBManager.closePreparedStatement(preparedStatement);
		System.out.println("....done");

	}
}
