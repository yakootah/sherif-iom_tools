package IBM.panorama.jdbc.release_4_4;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import IBM.panorama.dbUtility.Category;
import IBM.panorama.jdbc.CategoryCreator;
import IBM.panorama.jdbc.DBManager;
import IBM.panorama.jdbc.Handy;
import IBM.panorama.jdbc.dwr.BrowserCaller;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class RequisitionCreator_4_4 implements CategoryCreator
{

	// List<int[]> reqList--> int[] = new int[7]
	// 0 pos. for requisition
	// 1 pos. for specimen
	// 2 pos. for test result reports
	// 3 pos. for req test
	// 4 pos. for req report test
	// 5 pos. for req test result
	// 6 pos. for encounter grp cncpt id
	private List<long[]> reqList = new ArrayList<long[]>();
	private HashMap<String, Long> categories = new HashMap<String, Long>();

	private List<Long> diseaseList;

	private long requisitionReportedTestMaxId;
	private long requisitionTestMaxId;
	private long resultReportMaxId;
	private long requisitionMaxId;
	private long specimenMaxId;
	private long respOrgMaxId;
	private long requisitionTestResultMaxId;
	private long testEncounterGroupMaxId;
	private int repeat;
	private int requisitionPeriod;
	private int originalRepeat;
	private int chunk;

	public enum AFFLIATION_OPTIONS
	{
		SUBJECT, OUTBREAK, INVESTIGATION, COHORT, NON_HUMAN, NONE;

		public static AFFLIATION_OPTIONS getEnum(String name)
		{
			return Arrays.asList(AFFLIATION_OPTIONS.class.getEnumConstants()).stream()
					.filter(f -> f.name().equals(name)).findAny().orElse(NONE);
		}
	}

	public enum REQ_STREAM_CODE
	{
		HUMAN_REQUISITION, NON_HUMAN_REQUISITION;
	}

	@Value("${specimenSize}")
	private String specimenSize;

	@Value("${humanSpecimenType}")
	private String humanSpecimenType;

	@Value("${nonHumanSpecimenType}")
	private String nonHumanSpecimenType;

	@Value("${iterm.oid.req_status}")
	private String itermOidReqStatus;

	@Value("${iterm.oid.result_report}")
	private String itermOidResultReport;

	@Value("${iterm.oid.reqTest}")
	private String itermOidReqTest;

	@Value("${iterm.oid.resultStatus}")
	private String itermOidResultStatus;

	@Value("${iterm.oid.resultName}")
	private String itermOidResultName;

	@Value("${SubjectIdRandomSize}")
	private String subjectIdRandomSize;

	@Value("${SubjectIdRange}")
	private String subjectIdRange;

	@Value("${InvestigationIdRandomSize}")
	private String investigationIdRandomSize;

	@Value("${InvestigationIdRange}")
	private String investigationIdRange;

	@Value("${OutbreakIdRandomSize}")
	private String outbreakIdRandomSize;

	@Value("${OutbreakIdRange}")
	private String outbreakIdRange;

	@Value("${RequisitionDatePeriod}")
	private String requisitionDatePeriod;

	@Value("${CohortIdRange}")
	private String cohortIdRange;

	@Value("${CohortIdRandomSize}")
	private String cohortIdRandomSize;

	@Value("${EncounterIdRandomSize}")
	private String encounterIdRandomSize;

	@Value("${EncounterIdRange}")
	private String encountertIdRange;

	@Value("${NonHumanIdRandomSize}")
	private String nonHumanIdRandomSize;

	@Value("${NonHumanIdRange}")
	private String nonHumanIdRange;

	@Value("${sql.query.panorama}")
	private String org;
	private Long orgId;

	@Value("${sql.alter.seq}")
	private String alterSeq;

	private StringBuilder status = new StringBuilder(1024);
	private boolean interrupt;

	private Map<String, Long> currentMaxIds = new HashMap<String, Long>();

	private String subjectIdList;
	private String cohortIdList;
	private String encounterIdList;
	private String outbreakIdList;
	private String nonHumanIdList;
	private String investigationIdList;

	private String subjectRowsLimit;
	private String cohortRowsLimit;
	private String outbreakRowsLimit;
	private String nonHumanRowsLimit;
	private String encounterRowsLimit;
	private String investigationRowsLimit;

	private List<Long> humanSpecimenTypeList;
	private List<Long> nonHumanSpecimenTypeList;
	private List<Long> sdlList;

	private int RequisitionDate_Period;

	private List<Long> itermReqStatusCodedConceptList;
	private List<Long> itermResultReportCodedConceptList;
	private List<Long> itermReqResultCodedConceptList;
	private List<Long> itermResultStatusCodedConceptList;
	private List<Long> itermResultNameCodedConceptList;

	private int maxSpecimens;

	class AffliatedList
	{
		List<Long> subjectList = null;
		List<Long> nonHumanSubjectList = null;
		List<long[]> outbreakList = null;
		List<Long> cohortList = null;
		List<long[]> encounterList = null;
		List<long[]> investigationList = null;
	}

	@PostConstruct
	private void init()
	{
		itermReqStatusCodedConceptList = DBManager.getConceptCodeList(itermOidReqStatus);
		itermResultReportCodedConceptList = DBManager.getConceptCodeList(itermOidResultReport);
		itermReqResultCodedConceptList = DBManager.getConceptCodeList(itermOidReqTest);

		itermResultStatusCodedConceptList = DBManager.getConceptCodeList(itermOidResultStatus);
		itermResultNameCodedConceptList = DBManager.getConceptCodeList(itermOidResultName);

		humanSpecimenTypeList = DBManager.getConceptCodeListUsingValueSet(humanSpecimenType);
		nonHumanSpecimenTypeList = DBManager.getConceptCodeListUsingValueSet(nonHumanSpecimenType);
		orgId = DBManager.fetchIdColumnValues(org).get(0);
		sdlList = DBManager.getSDL(orgId);

	}

	@Override
	public void create(Category info) throws SQLException
	{
		repeat = info.getSize();
		originalRepeat = repeat;
		chunk = info.getChunk();
		requisitionPeriod = Integer.parseInt(info.getProperties().stream()
				.filter(e -> e.getName().equals(requisitionDatePeriod)).findAny().get().getValue());

		List<String> requisitionAffliatedList = new ArrayList<String>();

		loadProperties(info, requisitionAffliatedList);

		while (repeat > 0 && !isInterrupt())
		{
			String affliation = Handy.getRandomString(requisitionAffliatedList);

			loadMaxIdValues();
			createChunkOfRequisitions(AFFLIATION_OPTIONS.getEnum(affliation));
			reset(repeat);
		}
		alertEnd();
	}

	private void loadProperties(Category info, List<String> requisitionAffliatedList)
	{
		maxSpecimens = Integer.parseInt(
				info.getProperties().stream().filter(e -> e.getName().equals(specimenSize)).findAny().get().getValue());

		subjectRowsLimit = info.getProperties().stream().filter(e -> e.getName().equals(subjectIdRandomSize)).findAny()
				.get().getValue();
		subjectIdList = info.getProperties().stream().filter(e -> e.getName().equals(subjectIdRange)).findAny().get()
				.getValue();
		if (!Handy.isEmptyString(subjectIdList))
		{
			requisitionAffliatedList.add(AFFLIATION_OPTIONS.SUBJECT.name());
		}

		investigationRowsLimit = info.getProperties().stream()
				.filter(e -> e.getName().equals(investigationIdRandomSize)).findAny().get().getValue();
		investigationIdList = info.getProperties().stream().filter(e -> e.getName().equals(investigationIdRange))
				.findAny().get().getValue();
		if (!Handy.isEmptyString(investigationIdList))
		{
			requisitionAffliatedList.add(AFFLIATION_OPTIONS.INVESTIGATION.name());
		}

		outbreakRowsLimit = info.getProperties().stream().filter(e -> e.getName().equals(outbreakIdRandomSize))
				.findAny().get().getValue();
		outbreakIdList = info.getProperties().stream().filter(e -> e.getName().equals(outbreakIdRange)).findAny().get()
				.getValue();
		if (!Handy.isEmptyString(outbreakIdList))
		{
			requisitionAffliatedList.add(AFFLIATION_OPTIONS.OUTBREAK.name());
		}
		encounterRowsLimit = info.getProperties().stream().filter(e -> e.getName().equals(encounterIdRandomSize))
				.findAny().get().getValue();
		encounterIdList = info.getProperties().stream().filter(e -> e.getName().equals(encountertIdRange)).findAny()
				.get().getValue();

		cohortRowsLimit = info.getProperties().stream().filter(e -> e.getName().equals(cohortIdRandomSize)).findAny()
				.get().getValue();
		cohortIdList = info.getProperties().stream().filter(e -> e.getName().equals(cohortIdRange)).findAny().get()
				.getValue();
		if (!Handy.isEmptyString(cohortIdList))
		{
			requisitionAffliatedList.add(AFFLIATION_OPTIONS.COHORT.name());
		}
		nonHumanRowsLimit = info.getProperties().stream().filter(e -> e.getName().equals(nonHumanIdRandomSize))
				.findAny().get().getValue();
		nonHumanIdList = info.getProperties().stream().filter(e -> e.getName().equals(nonHumanIdRange)).findAny().get()
				.getValue();
		if (!Handy.isEmptyString(nonHumanIdList))
		{
			requisitionAffliatedList.add(AFFLIATION_OPTIONS.NON_HUMAN.name());
		}
	}

	private void alertEnd()
	{
		interrupt = false;

	}

	public void reset(int repeat)
	{
		String percentage = Handy.getPercentageOfWorkDone(repeat, originalRepeat);
		BrowserCaller.pushStatusToWeb(getStatus().toString(), percentage, getId());
		reqList = new ArrayList<long[]>();
		status.delete(0, status.length());
		currentMaxIds.clear();

	}

	private void createChunkOfRequisitions(AFFLIATION_OPTIONS affliation) throws SQLException
	{
		runInsertRequisition(affliation);
		runInsertSpecimen(affliation);
		runInsertTestResultReport();
		runInsertRequisitionTest();
		runInsertRequisitionReportedTest();
		runInsertRequisitionTestResult();
		runInsertTestEncounterGroup();

		updateSequences();
		DBManager.commit();
		decrementRepeat();
	}

	private void decrementRepeat()
	{
		repeat -= chunk;

	}

	private int getChunkSize()
	{
		return repeat < chunk ? (chunk = repeat) : chunk;
	}

	private void runInsertRequisition(AFFLIATION_OPTIONS affliation) throws SQLException
	{
		append("insert requisition records");
		long id = 0;
		reqList = new ArrayList<long[]>();
		requisitionMaxId = currentMaxIds.get("requisition");

		AffliatedList affliatedList = new AffliatedList();
		populateAffiatedList(affliation, affliatedList);

		String sql = "insert into lab.requisition (requisition_ID, ENCOUNTER_GROUP_CODE_CNCPT_ID, encounter_id, NONHUMAN_SUBJECT_ID, outbreak_id, cohort_id, subject_id,investigation_id, req_stream_code, STATUS_CODE_CNCPT_ID, requisition_date, ORGANIZATIONAL_UNIT_ID, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		populatePreparedStatement(id, affliatedList, preparedStatement);

		preparedStatement.executeBatch();

		getStatus().append("...Rows: " + chunk);

	}

	private void populatePreparedStatement(long id, AffliatedList affliatedList, PreparedStatement preparedStatement)
			throws SQLException
	{
		for (int i = 1; i <= getChunkSize(); i++)
		{
			id = requisitionMaxId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setString(9, REQ_STREAM_CODE.HUMAN_REQUISITION.name());

			long[] encounter = null;

			if (affliatedList.nonHumanSubjectList != null)
			{
				preparedStatement.setLong(4, Handy.getRandomValue(affliatedList.nonHumanSubjectList));
				preparedStatement.setString(9, REQ_STREAM_CODE.NON_HUMAN_REQUISITION.name());
			} else
			{
				preparedStatement.setNull(4, java.sql.Types.INTEGER);
			}
			if (affliatedList.outbreakList != null)
			{
				long[] outbreak = Handy.getRandomTuple(affliatedList.outbreakList);
				preparedStatement.setLong(5, outbreak[0]);
				encounter = getCompatibleRndomEncounterTuple(affliatedList.encounterList, outbreak[1]);
			} else
			{
				preparedStatement.setNull(5, java.sql.Types.INTEGER);
			}
			if (affliatedList.cohortList != null)
			{
				preparedStatement.setLong(6, Handy.getRandomValue(affliatedList.cohortList));
			} else
			{
				preparedStatement.setNull(6, java.sql.Types.INTEGER);
			}

			if (affliatedList.subjectList != null)
			{
				long subjectId = Handy.getRandomValue(affliatedList.subjectList);
				preparedStatement.setLong(7, subjectId);

			} else
			{
				preparedStatement.setNull(7, java.sql.Types.INTEGER);
			}
			if (affliatedList.investigationList != null)
			{
				long[] investigation = Handy.getRandomTuple(affliatedList.investigationList);
				preparedStatement.setLong(8, investigation[0]);
				preparedStatement.setLong(7, investigation[2]);
				encounter = getCompatibleRndomEncounterTuple(affliatedList.encounterList, investigation[1]);

			} else
			{
				preparedStatement.setNull(8, java.sql.Types.INTEGER);
			}

			if (encounter == null)

			{
				encounter = Handy.getRandomTuple(affliatedList.encounterList);
				preparedStatement.setLong(7, encounter[2]);
			}
			preparedStatement.setLong(2, encounter[1]);
			preparedStatement.setLong(3, encounter[0]);

			reqList.add(i - 1, new long[]
			{ id, 0, 0, 0, 0, 0, encounter[1] });

			preparedStatement.setLong(10, Handy.getRandomValue(itermReqStatusCodedConceptList));

			preparedStatement.setDate(11, Handy.getRandomDate(requisitionPeriod));

			preparedStatement.setLong(12, orgId);

			preparedStatement.addBatch();
		}
		requisitionMaxId = id;
	}

	private long[] getCompatibleRndomEncounterTuple(List<long[]> encounterList, long encounterGroupCCId)
	{
		return encounterList.stream().filter(e -> e[1] == encounterGroupCCId).findAny().orElse(null);

	}

	private void populateAffiatedList(AFFLIATION_OPTIONS affliation, AffliatedList affliatedList)
	{
		switch (affliation)
		{
		case SUBJECT:

			affliatedList.subjectList = DBManager.fetchIdColumnValues(DBManager.getLimitedCandidateListSql("subject_id",
					"client.human_subject", subjectIdList, subjectRowsLimit));
			break;

		case NON_HUMAN:

			affliatedList.nonHumanSubjectList = DBManager.fetchIdColumnValues(DBManager.getLimitedCandidateListSql(
					"NONHUMAN_SUBJECT_ID", "client.NONHUMAN_SUBJECT", nonHumanIdList, nonHumanRowsLimit));
			break;

		case OUTBREAK:

			affliatedList.outbreakList = DBManager
					.fetchIdAndSecondaryIdColumnValues(DBManager.getLimitedCandidateListSql("outbreak_id",
							"ENCOUNTER_GROUP_CODE_CNCPT_ID", "case.outbreak", outbreakIdList, outbreakRowsLimit));
			break;

		case COHORT:

			affliatedList.cohortList = DBManager.fetchIdColumnValues(
					DBManager.getLimitedCandidateListSql("cohort_id", "client.cohort", cohortIdList, cohortRowsLimit));
			break;
		case INVESTIGATION:
			affliatedList.investigationList = DBManager.fetch3IdsColumnValues(
					DBManager.getLimitedCandidateListSql("investigation_id", "ENCOUNTER_GROUP_ID", "subject_id",
							"case.investigation", investigationIdList, investigationRowsLimit));

			affliatedList.investigationList = filterOutNonHumanIds(affliatedList.investigationList);
			break;

		default:
			break;

		}
		affliatedList.encounterList = DBManager
				.fetch3IdsColumnValues(DBManager.getLimitedCandidateListSql("encounter_id", "ENCOUNTER_GROUP_ID",
						"subject_id", "case.encounter", encounterIdList, encounterRowsLimit));

		affliatedList.encounterList = filterOutNonHumanIds(affliatedList.encounterList);

	}

	private List<long[]> filterOutNonHumanIds(List<long[]> list)
	{
		return Handy.filterOutList2FromList1(list, DBManager
				.fetchIdColumnValues(DBManager.getCandidateListSql("NONHUMAN_SUBJECT_ID", "client.NONHUMAN_SUBJECT")));
	}

	private void runInsertSpecimen(AFFLIATION_OPTIONS affliation) throws SQLException
	{

		append("insert Specimen records");
		long id = 0;
		specimenMaxId = currentMaxIds.get("specimen");
		String sql = "insert into lab.requisition_specimen (requisition_specimen_ID, requisition_ID, specimen_identifier, collection_date, SPECIMEN_TYPE_CODE_CNCPT_ID, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ? , CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= reqList.size(); i++)
		{
			for (int j = 1; j <= maxSpecimens; j++)
			{
				id = specimenMaxId + j;
				preparedStatement.setLong(1, id);
				preparedStatement.setLong(2, reqList.get(i - 1)[0]);
				reqList.get(i - 1)[1] = id;
				preparedStatement.setLong(3, j);
				preparedStatement.setLong(4, getSettingType(affliation));

				preparedStatement.addBatch();
			}
			specimenMaxId = id;
		}
		specimenMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk * maxSpecimens);

	}

	private void runInsertTestResultReport() throws SQLException
	{

		append("insert Req. Test Result Report records");
		long id = 0;
		resultReportMaxId = currentMaxIds.get("resultReport");
		String sql = "insert into lab.REQ_TEST_RESULTS_REPORT (REQ_TEST_RESULTS_REPORT_ID, LAB_ACCESSION_NUMBER, REPORT_SOURCE_CODE_CNCPT_ID, RESULTING_LAB_SDL_ID, PH_RECEIPT_DATETIME, report_date, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		String accessionNum = Handy.getUniqueString();
		for (int i = 1; i <= reqList.size(); i++)
		{

			id = resultReportMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setString(2, accessionNum + Handy.DASH + i);
			preparedStatement.setLong(3, Handy.getRandomValue(itermResultReportCodedConceptList));
			reqList.get(i - 1)[2] = id;
			preparedStatement.setLong(4, Handy.getRandomValue(sdlList));
			preparedStatement.addBatch();

		}
		resultReportMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runInsertRequisitionReportedTest() throws SQLException
	{

		append("insert Requisition Reported Test records");
		long id = 0;
		requisitionReportedTestMaxId = currentMaxIds.get("requisitionReportedTest");
		String sql = "insert into lab.REQUISITION_REPORTED_TEST (REQUISITION_REPORTED_TEST_ID, REQUISITION_TEST_ID, REQ_TEST_RESULTS_REPORT_ID, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ?, CURRENT_TIMESTAMP, 'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= reqList.size(); i++)
		{

			id = requisitionReportedTestMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, reqList.get(i - 1)[3]);
			preparedStatement.setLong(3, reqList.get(i - 1)[2]);
			reqList.get(i - 1)[4] = id;
			preparedStatement.addBatch();

		}
		requisitionReportedTestMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runInsertRequisitionTestResult() throws SQLException
	{

		append("insert Requisition Test Result records");
		long id = 0;
		requisitionTestResultMaxId = currentMaxIds.get("requisitionTestResult");
		String sql = "insert into lab.REQUISITION_TEST_RESULT (REQUISITION_TEST_RESULT_ID, REQUISITION_TEST_ID, REQ_TEST_RESULTS_REPORT_ID, RESULT_STATUS_CODE_CNCPT_ID, RESULT_NAME_CD_CNCPT_ID, result_datetime, PHYSICIAN_OTHER_PROVIDER, RADIOLOGIST_OTHER_PROVIDER, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= reqList.size(); i++)
		{

			id = requisitionTestResultMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, reqList.get(i - 1)[3]);
			preparedStatement.setLong(3, reqList.get(i - 1)[2]);
			preparedStatement.setLong(4, Handy.getRandomValue(itermResultStatusCodedConceptList));
			preparedStatement.setLong(5, Handy.getRandomValue(itermResultNameCodedConceptList));
			preparedStatement.setString(6, Handy.DUMMY_PROVIDER);
			preparedStatement.setString(7, Handy.DUMMY_PROVIDER);

			reqList.get(i - 1)[5] = id;
			preparedStatement.addBatch();

		}
		requisitionTestResultMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runInsertTestEncounterGroup() throws SQLException
	{

		append("insert Req. Test Encounter Group records");
		long id = 0;
		testEncounterGroupMaxId = currentMaxIds.get("testEncounterGroup");
		String sql = "insert into lab.REQ_TEST_ENCOUNTER_GROUP (REQ_TEST_ENCOUNTER_GROUP_ID, REQUISITION_TEST_ID, ENCOUNTER_GROUP_CD_CNCPT_ID, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (?, ?, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= reqList.size(); i++)
		{

			id = testEncounterGroupMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, reqList.get(i - 1)[3]);
			preparedStatement.setLong(3, reqList.get(i - 1)[6]);

			preparedStatement.addBatch();

		}
		testEncounterGroupMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runInsertRequisitionTest() throws SQLException
	{

		append("insert Test Result records");
		long id = 0;
		requisitionTestMaxId = currentMaxIds.get("requisitionTest");
		String sql = "insert into lab.REQUISITION_TEST (REQUISITION_TEST_ID, REQUISITION_ID, REQUISITION_SPECIMEN_ID, REQ_TEST_RESULTS_REPORT_ID, TEST_CODE_CNCPT_ID, test_date, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= reqList.size(); i++)
		{

			id = requisitionTestMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, reqList.get(i - 1)[0]);
			preparedStatement.setLong(3, reqList.get(i - 1)[1]);
			preparedStatement.setLong(4, reqList.get(i - 1)[2]);
			preparedStatement.setLong(5, Handy.getRandomValue(itermReqResultCodedConceptList));
			reqList.get(i - 1)[3] = id;
			preparedStatement.addBatch();

		}
		requisitionTestMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private Long getSettingType(AFFLIATION_OPTIONS affliation)
	{
		if (AFFLIATION_OPTIONS.NON_HUMAN.equals(affliation))
		{
			return Handy.getRandomValue(nonHumanSpecimenTypeList);
		}
		return Handy.getRandomValue(humanSpecimenTypeList);
	}

	private void updateSequences() throws SQLException
	{
		append("Update Sequences");
		List<String> list = buildUpdateSequenceList();
		Statement statement = DBManager.getSqlConnection().createStatement();

		for (int i = 0; i < list.size(); i++)
		{
			statement.addBatch(list.get(i));
		}
		statement.executeBatch();

	}

	private void loadMaxIdValues()
	{
		String maxIdSql = DBManager.getMaxIdSql("requisition_id", "lab.requisition");
		currentMaxIds.put("requisition", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("requisition_specimen_id", "lab.requisition_specimen");
		currentMaxIds.put("specimen", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("REQ_TEST_RESULTS_REPORT_ID", "lab.REQ_TEST_RESULTS_REPORT");
		currentMaxIds.put("resultReport", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("REQUISITION_TEST_ID", "lab.REQUISITION_TEST");
		currentMaxIds.put("requisitionTest", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("REQUISITION_REPORTED_TEST_ID", "lab.REQUISITION_REPORTED_TEST");
		currentMaxIds.put("requisitionReportedTest", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("REQUISITION_TEST_RESULT_ID", "lab.REQUISITION_TEST_RESULT");
		currentMaxIds.put("requisitionTestResult", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("REQ_TEST_ENCOUNTER_GROUP_ID", "lab.REQ_TEST_ENCOUNTER_GROUP");
		currentMaxIds.put("testEncounterGroup", DBManager.fetchIdColumn(maxIdSql));

	}

	private List<String> buildUpdateSequenceList()
	{
		List<String> list = new ArrayList<String>();
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQUISITION_SEQ", String.valueOf(++requisitionMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQUISITION_SPECIMEN_SEQ", String.valueOf(++specimenMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQ_TEST_RESULTS_REPORT_SEQ",
				String.valueOf(++resultReportMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQUISITION_TEST_SEQ",
				String.valueOf(++requisitionTestMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQUISITION_REPORTED_TEST_SEQ",
				String.valueOf(++requisitionReportedTestMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQUISITION_TEST_RESULT_SEQ",
				String.valueOf(++requisitionTestResultMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "lab.REQ_TEST_ENCOUNTER_GROUP_SEQ",
				String.valueOf(++testEncounterGroupMaxId)));
		return list;

	}

	private void append(String line)
	{
		getStatus().append("<br/>" + line);
	}

	@Override
	public void interrupt()
	{
		interrupt = true;

	}
}
