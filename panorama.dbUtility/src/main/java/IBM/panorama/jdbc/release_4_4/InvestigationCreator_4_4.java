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

@Getter
@Setter
@Component
public class InvestigationCreator_4_4 implements CategoryCreator
{
	private long investigationMaxId;
	private long outbreakGroupId;

	// List<int[]> invList--> int[] = new int[5]
	// 0 pos. for investigation
	// 1 pos. for classification
	// 2 pos. for disease event
	// 3 pos. for investigation history
	// 4 pos. for disease event history
	private List<long[]> invList = new ArrayList<long[]>();
	private HashMap<String, Long> categories = new HashMap<String, Long>();

	private List<Long> diseaseList;

	private long diseaseEventMaxId;
	private long classDiseaseEventMaxId;
	private long diseaseEventHistoryMaxId;
	private long investigatorMaxId;
	private long investigationHistoryMaxId;
	private long respOrgMaxId;
	private boolean linkToOutbreak;
	private int repeat;
	private int originalRepeat;
	private int chunk;

	private String rowsLimit;

	@Value("${sql.query.cdDiseaseList}")
	private String cdDiseaseListSql;
	private List<Long> cdDiseaseList;

	@Value("${sql.query.stiDiseaseList}")
	private String stiDiseaseListSql;
	private List<Long> stiDiseaseList;

	@Value("${sql.query.tbDiseaseList}")
	private String tbDiseaseListSql;
	private List<Long> tbDiseaseList;

	@Value("${sql.query.workGroupList}")
	private String workGroupListSql;
	private List<Long> workGroupList;

	@Value("${sql.query.classificationCaseList}")
	private String classificationCaseListSql;
	private List<Long> classificationCaseList;

	@Value("${sql.query.panorama}")
	private String org;
	private Long orgId;

	@Value("${sql.query.openInvestigation}")
	private String openInvestigation;
	private Long openInvestigationId;

	@Value("${sql.qurey.cdGroup}")
	private String cdGroup;
	private long cdGroupId;

	@Value("${sql.qurey.stiGroup}")
	private String stiGroup;
	private long stiGroupId;

	@Value("${sql.qurey.tbGroup}")
	private String tbGroup;
	private long tbGroupId;

	@Value("${SubjectIdRandomSize}")
	private String subjectIdRandomSize;

	@Value("${SubjectIdRange}")
	private String subjectIdRange;

	@Value("${sql.query.dispositionList}")
	private String dispositionListSql;
	private List<Long> dispositionList;

	@Value("${classificationPrimaryCode}")
	private String classificationPrimaryCode;

	@Value("${classificationPrimaryOID}")
	private String classificationPrimaryOID;

	@Value("${sql.alter.seq}")
	private String alterSeq;

	@Value("${authorityNational}")
	private String authorityNational;

	@Value("${authorityNationalOID}")
	private String authorityNationalOID;

	@Value("${classificationCase}")
	private String classificationCase;

	@Value("${classificationCaseOID}")
	private String classificationCaseOID;

	private List<String> groupList = Arrays.asList("CD", "STI", "TB");

	private StringBuilder status = new StringBuilder(1024);
	private boolean interrupt;

	private Map<String, Long> currentMaxIds = new HashMap<String, Long>();
	private String idList;

	@PostConstruct
	private void init()
	{
		cdDiseaseList = DBManager.fetchIdColumnValues(cdDiseaseListSql);
		stiDiseaseList = DBManager.fetchIdColumnValues(stiDiseaseListSql);
		tbDiseaseList = DBManager.fetchIdColumnValues(tbDiseaseListSql);
		orgId = DBManager.fetchIdColumnValues(org).get(0);
		dispositionList = DBManager.fetchIdColumnValues(dispositionListSql);
		classificationCaseList = DBManager.fetchIdColumnValues(classificationCaseListSql);
		workGroupList = DBManager.fetchIdColumnValues(workGroupListSql);
		openInvestigationId = DBManager.fetchIdColumnValues(openInvestigation).get(0);
		cdGroupId = DBManager.fetchIdColumnValues(cdGroup).get(0);
		stiGroupId = DBManager.fetchIdColumnValues(stiGroup).get(0);
		tbGroupId = DBManager.fetchIdColumnValues(tbGroup).get(0);
	}

	@Override
	public void create(Category info) throws SQLException
	{
		repeat = info.getSize();
		originalRepeat = repeat;
		chunk = info.getChunk();
		rowsLimit = info.getProperties().stream().filter(e -> e.getName().equals(subjectIdRandomSize)).findAny().get()
				.getValue();
		idList = info.getProperties().stream().filter(e -> e.getName().equals(subjectIdRange)).findAny().get()
				.getValue();

		String encounterGroup;
		long encounterGrpCCId;
		List<Long> diseaseList;
		while (repeat > 0 && !isInterrupt())
		{
			encounterGroup = Handy.getRandomString(groupList);
			switch (encounterGroup)
			{
			case "CD":
				encounterGrpCCId = cdGroupId;
				diseaseList = cdDiseaseList;
				break;

			case "STI":
				encounterGrpCCId = stiGroupId;
				diseaseList = stiDiseaseList;
				break;
			default:
				encounterGrpCCId = tbGroupId;
				diseaseList = tbDiseaseList;
			}
			loadMaxIdValues();
			createChunkOfInvestigations(encounterGrpCCId, diseaseList);
			reset(repeat);
		}
		alertEnd();
	}

	private void alertEnd()
	{
		interrupt = false;
	}

	public void reset(int repeat)
	{
		String percentage = Handy.getPercentageOfWorkDone(repeat, originalRepeat);
		BrowserCaller.pushStatusToWeb(getStatus().toString(), percentage, getId());
		invList = new ArrayList<long[]>();
		status.delete(0, status.length());
		currentMaxIds.clear();

	}

	private void createChunkOfInvestigations(long encounterGrpCCId, List<Long> diseaseList) throws SQLException
	{
		runInsertInvestigation(encounterGrpCCId);
		runInsertInvestigationHistory();
		runUpdateInvestigation();
		runResponsibleOrganization();
		runClassificationDiseaseEvent();
		runDiseaseEvent();
		runDiseaseEventHistory(diseaseList);
		runUpdateDiseaseEvent();
		runUpdateClassificationDiseaseEvent();
		runInvestigator();
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

	private void runInsertInvestigation(long encounterGrpCCId) throws SQLException
	{
		append("insert investigaiton records");
		long id = 0;
		invList = new ArrayList<long[]>();
		investigationMaxId = currentMaxIds.get("investigation");
		List<Long> subjectList = DBManager.fetchIdColumnValues(
				DBManager.getLimitedCandidateListSql("subject_id", "client.human_subject", idList, rowsLimit));
		String sql = "insert into case.investigation (investigation_ID,ENCOUNTER_GROUP_ID, subject_id, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , ?, ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= getChunkSize(); i++)
		{
			id = investigationMaxId + i;
			invList.add(i - 1, new long[]
			{ id, 0, 0, 0, 0 });
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, encounterGrpCCId);
			preparedStatement.setLong(3, Handy.getRandomValue(subjectList));

			preparedStatement.addBatch();
		}
		investigationMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runInsertInvestigationHistory() throws SQLException
	{

		append("insert inv history records");
		long id = 0;
		investigationHistoryMaxId = currentMaxIds.get("investigationHistory");
		String sql = "insert into case.investigation_history (investigation_history_ID, status_date, STATUS_CODE_CNCPT_ID, INVESTIGATION_ID, DISPOSITION_CODE_CNCPT_ID, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND) "
				+ "values (? , CURRENT_TIMESTAMP, " + openInvestigationId
				+ " , ? , ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			id = investigationHistoryMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, invList.get(i - 1)[0]);
			preparedStatement.setLong(3, Handy.getRandomValue(dispositionList));
			invList.get(i - 1)[3] = id;

			preparedStatement.addBatch();
		}
		investigationHistoryMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runUpdateInvestigation() throws SQLException
	{
		String sql = "update case.investigation set investigation_history_id = ? where investigation_id = ?";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			preparedStatement.setLong(1, invList.get(i - 1)[3]);
			preparedStatement.setLong(2, invList.get(i - 1)[0]);

			preparedStatement.addBatch();

		}
		preparedStatement.executeBatch();

	}

	private void runResponsibleOrganization() throws SQLException
	{
		long id = 0;
		append("insert Resp. Org. records");
		respOrgMaxId = currentMaxIds.get("respOrg");
		String sql = "insert into case.RESPONSIBLE_ORGANIZATION (RESPONSIBLE_ORGANIZATION_ID,investigation_id, workgroup_id,organization_unit_id , ASSIGNED_DATE, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND)"
				+ " values (? , ?, ? , " + orgId + ", CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			id = respOrgMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, invList.get(i - 1)[0]);
			preparedStatement.setLong(3, Handy.getRandomValue(workGroupList));

			preparedStatement.addBatch();
		}
		respOrgMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runClassificationDiseaseEvent() throws SQLException
	{
		long id = 0;
		append("insert Classification Disease Event records");

		long classificationCaseConceptId = DBManager.findCodedConceptId(classificationCase, classificationCaseOID);
		long authorityNatOID = DBManager.findCodedConceptId(authorityNational, authorityNationalOID);

		classDiseaseEventMaxId = currentMaxIds.get("classification");
		String sql = "insert into case.CLASSIFICATION_DISEAS_EVNT (CLASSIFICATION_DISEAS_EVNT_id,CLASSIFICATION_CODE_CNCPT_ID,CLASSN_GROUP_CODE_CNCPT_ID , AUTHORITY_CODE_CNCPT_ID, CLASSIFICATION_DATE, CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND)"
				+ " values (? , ? , ?, " + authorityNatOID + ", CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			id = classDiseaseEventMaxId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, Handy.getRandomValue(classificationCaseList));
			preparedStatement.setLong(3, classificationCaseConceptId);
			invList.get(i - 1)[1] = id;

			preparedStatement.addBatch();
		}
		classDiseaseEventMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runDiseaseEvent() throws SQLException
	{
		long id = 0;
		append("insert Disease Event records");
		diseaseEventMaxId = currentMaxIds.get("diseaseEvent");
		String sql = "insert into case.disease_event evt (disease_event_id,investigation_id , CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND)"
				+ " values (? , ?, CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			id = diseaseEventMaxId + i;

			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, invList.get(i - 1)[0]);
			invList.get(i - 1)[2] = id;
			preparedStatement.addBatch();
		}

		diseaseEventMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runDiseaseEventHistory(List<Long> diseaseList) throws SQLException
	{
		long id = 0;
		append("insert Disease Event History records");
		diseaseEventHistoryMaxId = currentMaxIds.get("diseaseEventHistory");
		String sql = "insert into case.DISEASE_EVENT_HISTORY (DISEASE_EVENT_HISTORY_ID,DISEASE_CODE_CNCPT_ID,DISEASE_EVENT_ID, CLASSIFICATION_DISEAS_EVNT_ID , CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND)"
				+ " values (? , ? , ?,? ,CURRENT_TIMESTAMP,'DbUtility',0,'N')";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			id = diseaseEventHistoryMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, Handy.getRandomValue(diseaseList));
			preparedStatement.setLong(3, invList.get(i - 1)[2]);
			preparedStatement.setLong(4, invList.get(i - 1)[1]);
			invList.get(i - 1)[4] = id;

			preparedStatement.addBatch();
		}
		diseaseEventHistoryMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

	}

	private void runUpdateDiseaseEvent() throws SQLException
	{
		String sql = "update case.disease_event set CURRENT_DISEASE_EVENT_HSTRY_ID = ? where DISEASE_EVENT_ID = ?";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			preparedStatement.setLong(1, invList.get(i - 1)[4]);
			preparedStatement.setLong(2, invList.get(i - 1)[2]);

			preparedStatement.addBatch();
		}
		preparedStatement.executeBatch();

	}

	private void runUpdateClassificationDiseaseEvent() throws SQLException
	{
		String sql = "update case.CLASSIFICATION_DISEAS_EVNT set DISEASE_EVENT_HISTORY_ID = ? where CLASSIFICATION_DISEAS_EVNT_ID = ?";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		for (int i = 1; i <= invList.size(); i++)
		{
			preparedStatement.setLong(1, invList.get(i - 1)[3]);
			preparedStatement.setLong(2, invList.get(i - 1)[1]);

			preparedStatement.addBatch();
		}
		preparedStatement.executeBatch();

	}

	private void runInvestigator() throws SQLException
	{
		long id = 0;
		append("insert Investigator records");
		investigatorMaxId = currentMaxIds.get("investigator");
		String sql = "insert into case.Investigator (investigator_id, investigation_id, investigator_type, ASSIGNED_DATE , CREATED_ON,CREATED_BY,LOCK_SEQ_NUM,IS_DELETED_IND, WORK_GROUP_ID)"
				+ " values (? , ? , ?, CURRENT_TIMESTAMP ,CURRENT_TIMESTAMP,'DbUtility',0,'N', ? )";
		PreparedStatement preparedStatement = DBManager.getSqlConnection().prepareStatement(sql);

		long investigator_type = DBManager.findCodedConceptId(classificationPrimaryCode, classificationPrimaryOID);

		for (int i = 1; i <= invList.size(); i++)
		{
			id = investigatorMaxId + i;
			preparedStatement.setLong(1, id);
			preparedStatement.setLong(2, invList.get(i - 1)[0]);
			preparedStatement.setLong(3, investigator_type);
			preparedStatement.setLong(4, Handy.getRandomValue(workGroupList));

			preparedStatement.addBatch();
		}
		investigatorMaxId = id;
		preparedStatement.executeBatch();
		getStatus().append("...Rows: " + chunk);

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
		String maxIdSql = DBManager.getMaxIdSql("investigation_id", "case.investigation");
		currentMaxIds.put("investigation", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("investigation_history_id", "case.investigation_history");
		currentMaxIds.put("investigationHistory", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("RESPONSIBLE_ORGANIZATION_ID", "case.RESPONSIBLE_ORGANIZATION");
		currentMaxIds.put("respOrg", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("CLASSIFICATION_DISEAS_EVNT_id", "case.CLASSIFICATION_DISEAS_EVNT");
		currentMaxIds.put("classification", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("disease_event_id", "case.disease_event");
		currentMaxIds.put("diseaseEvent", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("DISEASE_EVENT_HISTORY_ID", "case.DISEASE_EVENT_HISTORY");
		currentMaxIds.put("diseaseEventHistory", DBManager.fetchIdColumn(maxIdSql));

		maxIdSql = DBManager.getMaxIdSql("Investigator_id", "case.Investigator");
		currentMaxIds.put("investigator", DBManager.fetchIdColumn(maxIdSql));

	}

	private List<String> buildUpdateSequenceList()
	{
		List<String> list = new ArrayList<String>();
		list.add(Handy.replacePlaceholders(alterSeq, "case.investigation_seq", String.valueOf(++investigationMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "case.investigation_history_seq",
				String.valueOf(++investigationHistoryMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "case.RESPONSIBLE_ORGANIZATION_SEQ",
				String.valueOf(++respOrgMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "case.CLASSIFICATION_DISEAS_EVNT_SEQ",
				String.valueOf(++classDiseaseEventMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "case.DISEASE_EVENT_SEQ", String.valueOf(++diseaseEventMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "case.DISEASE_EVENT_HISTORY_SEQ",
				String.valueOf(++diseaseEventHistoryMaxId)));
		list.add(Handy.replacePlaceholders(alterSeq, "case.INVESTIGATOR_SEQ", String.valueOf(++investigatorMaxId)));
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
