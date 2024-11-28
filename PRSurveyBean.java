package lms.core.newprocedure.survey;

import lms.core.measure.MeasureMessage;
import lms.core.newprocedure.ProcedureAssessmentBlock;
import lms.core.newprocedure.ProcedureBean;
import lms.core.newprocedure.ProcedureMessage;
import lms.core.newprocedure.ProcedureModule;
import lms.core.newprocedure.category.*;
import lms.core.newprocedure.member.PRMemberBean;
import lms.core.newprocedure.member.PRMemberFrame;
import lms.core.newprocedure.my.qua.EstimatorPRSurveyScoreListFrame;
import lms.core.newprocedure.poll.PRSurveyPollAnswerBean;
import lms.core.newprocedure.vv.ExpertCategoryRubricator;
import lms.core.person.PersonBean;
import lms.core.person.PersonFrame;
import lms.core.person.PersonLookupFieldBuilder;
import lms.core.person.PersonMessage;
import lms.core.person.localization.PersonNameLocalizationService;
import lms.core.progress.ProgressMessage;
import lms.route.RouteMessage;
import lms.route.phase.source.PhaseSourceBean;
import lms.service.media.MediaMessage;
import lms.service.poll.PollSqlService;
import lms.service.poll.SurveyBean;
import lms.service.poll.SurveyFrame;
import lms.service.poll.results.ObjectPollAnswerBean;
import mira.vv.rubricator.field.RSField;
import org.mirapolis.control.file.FileField;
import org.mirapolis.core.SystemMessages;
import org.mirapolis.data.bean.LongValue;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.data.bean.reflect.Name;
import org.mirapolis.data.bean.reflect.ReflectDataBean;
import org.mirapolis.mvc.model.entity.datafields.LookupField;
import org.mirapolis.mvc.model.entity.datafields.MultiLookupField;
import org.mirapolis.mvc.model.entity.fields.MemoFieldBuilder;
import org.mirapolis.orm.DataObject;
import org.mirapolis.orm.constraint.NotEmptyBean;
import org.mirapolis.orm.fields.*;
import org.mirapolis.util.DateHelper;
import org.mirapolis.util.IntHelper;
import org.mirapolis.util.StringHelper;

import java.util.*;
import java.util.stream.Stream;

/**
 * Анкета
 *
 * @author Elena Puzakova
 * @since 22.05.12 9:40
 */
public class PRSurveyBean extends ReflectDataBean {
	public static final String DATANAME = "AT$PRSURVEY";
	public static final String ALIAS = "PRS";

	public static final String ID = "prsid";
	public static final String MEMBER_ID = PRMemberBean.ID;
	public static final String PERSON_ID = PersonBean.ID;
    public static final String CATEGORY_ID = PRCategoryBean.ID;
	public static final String STATUS = "prsstatus";
	public static final String COMMENT = "prscomment";
	public static final String SURVEY_ID = SurveyBean.ID;
	public static final String IS_SURVEY_COMPLETE = "prsissurveycomplete";
	public static final String SURVEY_REPORT_FILE_ID = "surveyreportfileid";
	public static final String IS_KPI_COMPLETE = "prsiskpicomplete";
	public static final String IS_QUA = "prsisqua";
	public static final String IS_KPI = "prsiskpi";
	public static final String IS_TASK = "prsistask";
	public static final String IS_SURVEY = "prsissurvey";
	public static final String IS_TASK_COMPLETE = "prsistaskcomplete";
    public static final String IS_DEVELOPMENT_COMPLETE = "prsisdevcomplete";
    public static final String IS_DEVELOPMENT = "prsisdevelopment";
    public static final String IS_QUA_COMPLETE = "prsisquacomplete";
	public static final String NOT_CALCULATE_QUA = "prsnotcalcqua";
    public static final String EXPERT_CATEGORY = "expertcategoryid";
	public static final String EXPERT_PERSON = "expert";
	public static final String END_DATE = "prsenddate";

    public static final String QUA_PERCENT = "prsquapercent";
    public static final String QUA_POINT = "prsquapoint";

    public static final String LAST_DATE = "prslastdate";
    public static final String QUA_DATE = "prsquadate";
    public static final String QUA_TIME = "prsquatime";

    //Статистика по оценкам
    public static final String ALL_SCORE_COUNT = "prsallscorecount";
    public static final String SCORE_COUNT = "prsscorecount";
    public static final String REFUSAL_COUNT = "prsrefusalcount";


    public static final String LAST_DATE_SAVE_QUA = "prsdatesavequa";
	public static final String LAST_INDEX_SAVE_QUA = "prsindexsavequa";

	public static final String PHASE_SOURCE_ID = "phasesourceid";

	@Name(ID)
	private String id;
	/**
	 * Оцениваемый
	 */
	@Name(MEMBER_ID)
    @NotEmptyBean
	private NameBean member;
	/**
	 * Оценивающий
	 */
	@Name(PERSON_ID)
	private NameBean person;
    /**
     * Категория
     */
    @Name(CATEGORY_ID)
    @NotEmptyBean
    private NameBean category;
	/**
	 * Статус анкеты
	 */
	@Name(STATUS)
	private SurveyStatus status;
	/**
	 * Комментарий оценивающего к анкете
	 */
	@Name(COMMENT)
	private String comment;
	/**
	 * Опрос
	 * заполняется при запуске карты
	 */
	@Name(SURVEY_ID)
	private NameBean survey;
	/**
	 * Опрос заполнен
	 */
	@Name(IS_SURVEY_COMPLETE)
	private Boolean isSurveyComplete;
	/**
	 * Отчет по опросному листу
	 */
	@Name(SURVEY_REPORT_FILE_ID)
	private String surveyReportFileId;
	/**
	 * Завершено заполнение kpi
	 */
	@Name(IS_KPI_COMPLETE)
	private Boolean isKPIComplete;
	/**
	 * Оценивать компетенции (копируется из категории при запуске карты)
	 */
	@Name(IS_QUA)
	private Boolean isQua;
	/**
	 * Оценивать показатели (копируется из категории при запуске карты)
	 */
	@Name(IS_KPI)
	private Boolean isKpi;
	/**
	 * Заполнять опрос (копируется из категории при запуске карты)
	 */
	@Name(IS_SURVEY)
	private Boolean isSurvey;
	/**
	 * Оценивать задачи (копируется из категории при запуске карты)
	 */
	@Name(IS_TASK)
	private Boolean isTask;
	/**
	 * Завершено заполнение задач
	 */
	@Name(IS_TASK_COMPLETE)
	private Boolean isTaskComplete;
    /**
     * Заполнять рекомендациии (копируется из категории при запуске карты)
     */
    @Name(IS_DEVELOPMENT)
    private Boolean isDevelopment;
    /**
     * Завершено заполнение рекомендаций
     */
    @Name(IS_DEVELOPMENT_COMPLETE)
    private Boolean isDevelopmentComplete;
    /**
     * Завершено оценивание компетенций
     */
    @Name(IS_QUA_COMPLETE)
    private Boolean isQuaComplete;
	/**
	 * Не участвует в расчете результата по компетенции (превышен лимит не готов оценивать)
	 */
	@Name(NOT_CALCULATE_QUA)
	private Boolean notCalculateQua;
    /**
     * Категория эксперта
     */
    @Name(EXPERT_CATEGORY)
    private NameBean expertCategory;
    /**
     * Кол-во оценок, которое должно быть проставлено в анкете
     */
    @Name(ALL_SCORE_COUNT)
    private Integer allScoreCount;
    /**
     * Кол-во оценок, проставленное на текущий момент
     */
    @Name(SCORE_COUNT)
    private Integer scoreCount;
    /**
     * Кол-во оценок не готов оценивать на текущий момент
     */
    @Name(REFUSAL_COUNT)
    private Integer refusalCount;
    /**
     * ПОследняя дата перехода в анкету
     */
    @Name(LAST_DATE)
    private Date lastDate;
    /**
     * Дата завершения оценки компетенций
     */
    @Name(QUA_DATE)
    private Date quaDate;
    /**
     * Время в минитах, затраченное на оценку компетенций
	 * в минутах
     */
    @Name(QUA_TIME)
    private Integer quaTime;
	/**
	 * Дата завершения анкеты
	 */
	@Name(END_DATE)
	private Date endDate;
	/**
	 * Оценивающие категории Группа экспертов
	 */
	@Name(EXPERT_PERSON)
	private Set<NameBean> expertPersons;
    /**
     * Средняя оценка по компетенциям (Проценты)
     */
    @Name(QUA_PERCENT)
    private Double quaPercent;
    /**
     * Средняя оценка по компетенциям (Баллы)
     */
    @Name(QUA_POINT)
    private Double quaPoint;
    /**
     * Дата последнего сохранения компетенций анкетой
     */
    @Name(LAST_DATE_SAVE_QUA)
    private Date lastDateSaveQua;
	@Name(LAST_INDEX_SAVE_QUA)
	private Long lastIndexSaveQua;
	@Name(PHASE_SOURCE_ID)
	private String phaseSourceId;

	/**
     * Категория для отчетов
     */
    private PRCategoryBean procedureCategory;

    private List<PRSurveyQuaBean> quaScores;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public NameBean getMember() {
		return member;
	}

	public void setMember(NameBean member) {
		this.member = member;
	}

    public NameBean getCategory() {
        return category;
    }

    public void setCategory(NameBean category) {
        this.category = category;
    }

    public NameBean getPerson() {
		return person;
	}

	public void setPerson(NameBean person) {
		this.person = person;
	}

	public SurveyStatus getStatus() {
		return status;
	}

	public void setStatus(SurveyStatus status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public NameBean getSurvey() {
		return survey;
	}

	public void setSurvey(NameBean survey) {
		this.survey = survey;
	}

	public Boolean getIsSurveyComplete() {
		return isSurveyComplete;
	}

	public void setIsSurveyComplete(Boolean surveyComplete) {
		isSurveyComplete = surveyComplete;
	}

	public String getSurveyReportFileId() {
		return surveyReportFileId;
	}

	public void setSurveyReportFileId(String surveyReportFileId) {
		this.surveyReportFileId = surveyReportFileId;
	}

	public Boolean getIsKPIComplete() {
		return isKPIComplete;
	}

	public void setIsKPIComplete(Boolean KPIComplete) {
		isKPIComplete = KPIComplete;
	}

	public Boolean getIsQua() {
		return isQua;
	}

	public void setIsQua(Boolean qua) {
		isQua = qua;
	}

	public Boolean getIsKpi() {
		return isKpi;
	}

	public void setIsKpi(Boolean kpi) {
		isKpi = kpi;
	}

	public Boolean getIsSurvey() {
		return isSurvey;
	}

	public void setIsSurvey(Boolean isSurvey) {
		this.isSurvey = isSurvey;
	}

	public Boolean getIsTask() {
		return isTask;
	}

	public void setIsTask(Boolean task) {
		isTask = task;
	}

	public Boolean getIsTaskComplete() {
		return isTaskComplete;
	}

	public void setIsTaskComplete(Boolean taskComplete) {
		isTaskComplete = taskComplete;
	}

    public Boolean getIsDevelopmentComplete() {
        return isDevelopmentComplete;
    }

    public void setIsDevelopmentComplete(Boolean developmentComplete) {
        isDevelopmentComplete = developmentComplete;
    }

    public Boolean getIsDevelopment() {
        return isDevelopment;
    }

    public void setIsDevelopment(Boolean development) {
        isDevelopment = development;
    }

    public Boolean getIsQuaComplete() {
        return isQuaComplete;
    }

    public void setIsQuaComplete(Boolean quaComplete) {
        isQuaComplete = quaComplete;
    }

    public NameBean getExpertCategory() {
        return expertCategory;
    }

    public void setExpertCategory(NameBean expertCategory) {
        this.expertCategory = expertCategory;
    }

    public Integer getRefusalCount() {
        return refusalCount;
    }

    public void setRefusalCount(Integer refusalCount) {
        this.refusalCount = refusalCount;
    }

    public Integer getScoreCount() {
        return scoreCount;
    }

    public void setScoreCount(Integer scoreCount) {
        this.scoreCount = scoreCount;
    }

    public Integer getAllScoreCount() {
        return allScoreCount;
    }

    public void setAllScoreCount(Integer allScoreCount) {
        this.allScoreCount = allScoreCount;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public Date getQuaDate() {
        return quaDate;
    }

    public void setQuaDate(Date quaDate) {
        this.quaDate = quaDate;
    }

    public Integer getQuaTime() {
        return quaTime;
    }

    public void setQuaTime(Integer quaTime) {
        this.quaTime = quaTime;
    }

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Boolean getNotCalculateQua() {
		return notCalculateQua;
	}

	public void setNotCalculateQua(Boolean notCalculateQua) {
		this.notCalculateQua = notCalculateQua;
	}

	public Set<NameBean> getExpertPersons() {
		return expertPersons;
	}

	public void setExpertPersons(Set<NameBean> expertPersons) {
		this.expertPersons = expertPersons;
	}

    public Double getQuaPercent() {
        return quaPercent;
    }

    public void setQuaPercent(Double quaPercent) {
        this.quaPercent = quaPercent;
    }

    public Double getQuaPoint() {
        return quaPoint;
    }

    public void setQuaPoint(Double quaPoint) {
        this.quaPoint = quaPoint;
    }

    public Date getLastDateSaveQua() {
        return lastDateSaveQua;
    }

    public void setLastDateSaveQua(Date lastDateSaveQua) {
        this.lastDateSaveQua = lastDateSaveQua;
    }

	public Long getLastIndexSaveQua() {
		return lastIndexSaveQua;
	}

	public void setLastIndexSaveQua(Long lastIndexSaveQua) {
		this.lastIndexSaveQua = lastIndexSaveQua;
	}

	public String getPhaseSourceId() {
		return phaseSourceId;
	}

	public void setPhaseSourceId(String phaseSourceId) {
		this.phaseSourceId = phaseSourceId;
	}

	@Override
	public String getDataName() {
		return DATANAME;
	}

	public static DataObject createDataObject() {
		return new DataObject(DATANAME, PersonMessage.form, PRSurveyBean.class)
			.setFields(
				new KeyField(ID),
				new LookupField(MEMBER_ID, ProcedureMessage.member_scored, PRMemberFrame.NAME),
                new PersonLookupFieldBuilder(
					new LookupField(PERSON_ID, ProcedureMessage.estimator, PersonFrame.NAME, FKField.RESTRICT))
					.getDataField(),
				new LookupField(CATEGORY_ID, SystemMessages.category, PRCategoryFrame.NAME, FKField.RESTRICT)
					.setIsName(),
				new ComboField(STATUS, ProcedureMessage.survey_status, SurveyStatus.class),
				new MemoFieldBuilder(new StringField(COMMENT, MeasureMessage.comment, 4000))
					.getDataField(),
				new LookupField(SURVEY_ID, ProcedureMessage.questionnaire, SurveyFrame.NAME, FKField.RESTRICT),
				new CheckField(IS_SURVEY_COMPLETE, ProcedureMessage.is_complete),
				new CheckField(IS_KPI_COMPLETE, ProcedureMessage.is_complete),
				new FileField(
					SURVEY_REPORT_FILE_ID, ProcedureMessage.questionnaire, ProcedureModule.SYSFOLDER_PROCEDURE_SURVEY),
				new CheckField(IS_QUA, ProcedureMessage.is_complete),
				new CheckField(IS_SURVEY, ProcedureMessage.is_complete),
				new CheckField(IS_KPI, ProcedureMessage.is_complete),
				new CheckField(IS_TASK, ProcedureMessage.is_complete),
				new CheckField(IS_TASK_COMPLETE, ProcedureMessage.is_complete),
                new CheckField(IS_DEVELOPMENT, ProcedureMessage.is_complete),
                new CheckField(IS_DEVELOPMENT_COMPLETE, ProcedureMessage.is_complete),
                new CheckField(IS_QUA_COMPLETE, ProcedureMessage.is_complete),
                new RSField(
					EXPERT_CATEGORY,
					ProcedureMessage.expert_category,
					ExpertCategoryRubricator.EXPERT_CATEGORY,
					FKField.RESTRICT),
                new IntegerField<>(ALL_SCORE_COUNT, MediaMessage.count_of_ratings),
                new IntegerField<>(SCORE_COUNT, PersonMessage.give_mark_count),
                new IntegerField<>(REFUSAL_COUNT, ProcedureMessage.count_refusals),
                new DateTimeField(LAST_DATE, ProgressMessage.attemptEnd),
                new DateTimeField(QUA_DATE, ProcedureMessage.date_completion_competence),
                new IntegerField<>(QUA_TIME, ProcedureMessage.time_spent_on_competence),
				new DateTimeField(END_DATE, MeasureMessage.finishDate),
				new CheckField(NOT_CALCULATE_QUA, ProcedureMessage.not_taken_into_account_calculation),
				new MultiLookupField(EXPERT_PERSON, ProcedureMessage.estimators, PersonFrame.NAME),
                new DoubleField(QUA_PERCENT, ProcedureMessage.final_score_competencies),
                new DoubleField(QUA_POINT, ProcedureMessage.final_point_competencies),
                new DateTimeField(LAST_DATE_SAVE_QUA, ProcedureMessage.change_results),
				new IntegerField<>(LAST_INDEX_SAVE_QUA).setBigInt(),
				new FKField<>(PHASE_SOURCE_ID, RouteMessage.source_settings, PhaseSourceBean.DATANAME, FKField.SET_NULL)
		);
	}

    public void updateCategory() {
        PRCategoryBean category = PRCategoryService.getCategory(getCategory().getId());
        getCategory().setName(category.getCategoryName());
        setProcedureCategory(category);
    }

	public String getSurveyName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMember().getName()).append(" - ");
		if (EstimatingCategory.ADDITIONAL_GROUP.isEquals(getProcedureCategory().getCategory())) {
			sb.append(getProcedureCategory().getPersonGroup().getName());
		} else if (!getProcedureCategory().getCategory().isSurveyWithPerson()) {
			sb.append(getProcedureCategory().getCategory().getCaption());
		} else {
            sb.append(getPerson().getName());
        }
		return sb.toString();
	}

	public String getName(PRCategoryBean category) {
		if (EstimatingCategory.GROUP.isEquals(category.getCategory())) {
			return getExpertNames();
		} else if (EstimatingCategory.ADDITIONAL_GROUP.isEquals(category.getCategory())) {
			return category.getPersonGroup().getName();
		} else if (category.getCategory().isSurveyWithPerson()) {
			return getPerson().getName();
		} else {
			return category.getCategory().getCaption().toString();
		}
	}

	/**
	 * В анкеты есть опросный лист
	 */
	public boolean isUseSurvey() {
		return getIsSurvey() && StringHelper.isNotEmpty(getSurvey().getId());
	}

    public List<PRSurveyQuaBean> getQuaScores() {
        return quaScores;
    }

    public void setQuaScores(List<PRSurveyQuaBean> surveyQuaScores) {
        this.quaScores = surveyQuaScores;
    }

    public PRCategoryBean getProcedureCategory() {
        return procedureCategory;
    }

    public void setProcedureCategory(PRCategoryBean procedureCategory) {
        this.procedureCategory = procedureCategory;
    }

    public static class ByCategoryComparator implements Comparator<PRSurveyBean> {
        public int compare(PRSurveyBean bean1, PRSurveyBean bean2) {
            Integer a0 = bean1.getProcedureCategory().getCategory().getOrder();
            Integer a1 = bean2.getProcedureCategory().getCategory().getOrder();
            return a0.compareTo(a1);
        }
    }

    /**
     * Объект, по которому заполняется опрос
     */
    public String getObjTableNameForPoll() {
        if (procedureCategory == null) {
            setProcedureCategory(PRCategoryService.getCategory(getCategory().getId()));
        }
        ProcedureEstimatingCategory category = procedureCategory.getCategory();
        String categoryValue = category.getValueForCompare(procedureCategory)
				.map(nameBean -> category.getValue() + "_" + nameBean.getId())
				.orElse(category.getValue());
        return PRMemberFrame.NAME + categoryValue;
    }

    /**
     * Пользователь, который должен заполнить опрос
     */
    public String getPollUserId(String estimatorId) {
        return getPollUserId(estimatorId, getSurvey().getId(), PRSurveyPollAnswerBean.class);
    }

	/**
	 * Пользователь, который должен заполнить опрос
	 */
	public String getPollUserId(String estimatorId,
								String pollId,
								Class<? extends ObjectPollAnswerBean> answerBeanClass) {
		PRCategoryBean category = PRCategoryService.getCategory(getCategory().getId());
		if (!EstimatingCategory.GROUP.isEquals(category.getCategory()) && category.getCategory().isSurveyWithPerson()) {
			return getPerson().getId();
		} else if (getStatus().isComplete() ||
				getStatus().isCanceled() ||
				EstimatingCategory.GROUP.isEquals(category.getCategory()) ||
				!category.getCategory().isSurveyWithPerson()) {
			//Если категория группа и анкета завершена, найти пользователя, который заполнил опрос
			Set<String> answeredPersonIds = PollSqlService.getAnsweredPersonIds(pollId, answerBeanClass, getId());
			if (!answeredPersonIds.isEmpty()) {
				return answeredPersonIds.iterator().next();
			}
		}
		return estimatorId;
	}
    
    public String getCategoryName(PRCategoryBean category) {
        return StringHelper.isEmpty(expertCategory.getName()) ?
                category.getCategoryName() :
                (category.getCategoryName() + "/" + expertCategory.getName());
    }

	public Set<String> getExpertPersonIds() {
		return NameBean.getIdSet(getExpertPersons());
	}

	public String getExpertNames() {
    	updateExpertNames();
		List<String> names = new ArrayList<>();
		for (NameBean nb : getExpertPersons()) {
			names.add(nb.getName());
		}
		return StringHelper.joinWithComa(names);
	}

	public void updateExpertNames() {
		PersonNameLocalizationService.getInstance().updateNameBeansPersonLocalizedName(getExpertPersons());
	}

    public void addQuaScore(PRSurveyQuaBean score) {
        if (quaScores == null) {
            quaScores = new ArrayList<>();
        }
        quaScores.add(score);
    }

	/**
	 * @return % оценок не готов оценивать
	 */
	public Double getRefusalPercent() {
		Integer refusalCount = IntHelper.getZeroValue(getRefusalCount());
		return IntHelper.getZeroValue(getAllScoreCount()) == 0 ? 0 : (Double.valueOf(refusalCount) / getAllScoreCount()) * 100;
	}

	public Long getQuaFormLastSaveIndex() {
		return !LongValue.isNull(lastIndexSaveQua) ?
				lastIndexSaveQua :
				(DateHelper.isNotNull(lastDateSaveQua) ? lastDateSaveQua.getTime() : null);
	}

	public void setQuaSaveInfo(Long clientIndex) {
		setLastDateSaveQua(new Date());
		setLastIndexSaveQua(clientIndex != null ?
				(clientIndex + 1) :
				(LongValue.isNull(lastIndexSaveQua) ? 1 : lastIndexSaveQua + 1));
	}

	/**
	 * При обновлении анкеты
	 * index должен быть больше автосохраненного на клиенте, чтобы не отображались автосохраненные оценки по компетенциям
	 */
	public void updateServerQuaSaveInfo() {
		setQuaSaveInfo(LongValue.isNull(getQuaFormLastSaveIndex()) ? 10 : getQuaFormLastSaveIndex() + 10);
	}

	public String getQuaAutoSaveId() {
		return EstimatorPRSurveyScoreListFrame.NAME + getId();
	}

	/**
	 * Анкета оценивает хотя бы один блок процедуры
	 */
	public boolean isEstimate(ProcedureBean procedure) {
		return Stream.of(ProcedureAssessmentBlock.values())
				.anyMatch(block -> block.isUseProcedure(procedure) && block.isSurveyUse(this));
	}
}
