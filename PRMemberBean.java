package lms.core.newprocedure.member;

import hr.attestation.AttestationPersonBean;
import hr.attestation.AttestationService;
import hr.careerplanning.CareerPlanningMessage;
import hr.development.DevelopmentKind;
import hr.development.plan.PlanDevelopmentSaveValidationErrorProcessor;
import lms.core.ca.CAFrame;
import lms.core.ca.CAService;
import lms.core.ca.post.PostBean;
import lms.core.ca.post.PostFrame;
import lms.core.ca.post.PostMessage;
import lms.core.ca.post.profilerequirement.CAPostProfileRequirementFrame;
import lms.core.measure.MeasureMessage;
import lms.core.newprocedure.*;
import lms.core.newprocedure.analysis.ProcedureReportBuilder;
import lms.core.newprocedure.analysis.ProcedureTaskService;
import lms.core.newprocedure.analysis.member.ProcedureTaskMemberBean;
import lms.core.newprocedure.analysis.reportdata.IndividualByMemberIdReportDataCreator;
import lms.core.newprocedure.category.*;
import lms.core.newprocedure.kpi.catalog.profile.ProfileKPIFrame;
import lms.core.newprocedure.kpi.field.KpiField;
import lms.core.newprocedure.kpi.field.ProcedureKPIFieldStore;
import lms.core.newprocedure.member.add.PRMemberRegisterBean;
import lms.core.newprocedure.member.add.PRMemberRegistrator;
import lms.core.newprocedure.member.add.PrMemberAfterRegistrationAction;
import lms.core.newprocedure.member.development.PRMemberDevelopmentMethodBean;
import lms.core.newprocedure.member.development.PRMemberDevelopmentService;
import lms.core.newprocedure.member.kpi.PRMemberKPIBean;
import lms.core.newprocedure.member.kpi.PRMemberKPIService;
import lms.core.newprocedure.member.kpi.point.ChooseControlPointCondition;
import lms.core.newprocedure.member.kpi.point.ControlPointConfirmAction;
import lms.core.newprocedure.member.kpi.restriction.PRMemberKPIRestrictionService;
import lms.core.newprocedure.member.kpi.restriction.PRMemberKPIValidator;
import lms.core.newprocedure.member.kpi.settings.PrMemberKpiSettingsBean;
import lms.core.newprocedure.member.launch.MemberCardLauncher;
import lms.core.newprocedure.member.persondata.PrMemberPersonDataBean;
import lms.core.newprocedure.member.persondata.PrMemberPersonDataService;
import lms.core.newprocedure.member.qua.PRMemberQuaBean;
import lms.core.newprocedure.member.version.PRMemberVersionService;
import lms.core.newprocedure.my.EstimatorSurveyFormScoreSaver;
import lms.core.newprocedure.my.development.EstimatorDevelopmentMethodErrorType;
import lms.core.newprocedure.person.ProcedureAssignPersonAttributeService;
import lms.core.newprocedure.qua.category.PrQuaCategoryQuaCountService;
import lms.core.newprocedure.qua.profileresultlimit.PrQuaProfileResultLimitService;
import lms.core.newprocedure.route.ProcedureRouteObject;
import lms.core.newprocedure.route.ProcedureRouteSurveyCreator;
import lms.core.newprocedure.route.phasedate.PRMemberPhaseDateBean;
import lms.core.newprocedure.route.phasedate.ProcedurePhaseDateBean;
import lms.core.newprocedure.route.sysaction.PrSurveyCandidateSysActionService;
import lms.core.newprocedure.route.sysaction.ProcedureSysActionService;
import lms.core.newprocedure.route.sysaction.kpi.PRMemberKPIByTypeDivisionFrame;
import lms.core.newprocedure.route.sysaction.kpi.PrMemberControlPointChangeStatusGoAction;
import lms.core.newprocedure.route.sysaction.kpi.PrMemberKpiChangeStatusGoAction;
import lms.core.newprocedure.route.sysaction.kpi.PrMemberKpiSysActionService;
import lms.core.newprocedure.route.sysaction.qua.importance.ChangePRMemberQuaImportanceFrame;
import lms.core.newprocedure.survey.PRSurveyBean;
import lms.core.newprocedure.survey.PRSurveyQuaBean;
import lms.core.newprocedure.survey.PRSurveyService;
import lms.core.newprocedure.surveycandidate.PRSurveyCandidateBean;
import lms.core.newprocedure.surveycandidate.PRSurveyCandidateService;
import lms.core.newprocedure.surveycandidate.selfassign.PRSurveyCreateFromCandidateAction;
import lms.core.newprocedure.vv.ControlPointStatusRubricator;
import lms.core.newprocedure.vv.GoalStatusRubricator;
import lms.core.newprocedure.vv.ProcedureCategoryRubricator;
import lms.core.person.*;
import lms.core.person.vv.PostRubricator;
import lms.core.person.work.PersonWorkBean;
import lms.core.qua.QuaMessage;
import lms.core.qua.vv.QuaCategoryRubImpl;
import lms.route.RouteMessage;
import lms.route.object.ObjPhaseBean;
import lms.route.object.ObjRouteBean;
import lms.route.object.RouteObjService;
import lms.route.object.core.RouteAnonymousLinkService;
import lms.route.object.core.RouteCommandProcessor;
import lms.route.object.core.RouteSystemActionProcessor;
import lms.route.object.core.sysaction.RouteSystemAction;
import lms.route.object.core.sysaction.RouteSystemActionType;
import lms.route.phase.source.requiredaction.MyActionRequirementType;
import mira.creator.CreatorService;
import mira.vv.rubricator.field.MultiRSField;
import mira.vv.rubricator.field.RSField;
import mira.vv.rubricator.standard.RSBean;
import mira.vv.rubricator.standard.RSService;
import mira.vv.rubs.formula.FormulaFieldBuilder;
import mira.vv.rubs.usertext.UserTextRubricator;
import org.mirapolis.core.Application;
import org.mirapolis.core.Context;
import org.mirapolis.data.bean.BeanHelper;
import org.mirapolis.data.bean.DoubleValue;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.data.bean.reflect.Name;
import org.mirapolis.data.bean.reflect.ReflectDataBean;
import org.mirapolis.db.DatabaseStore;
import org.mirapolis.db.Session;
import org.mirapolis.exception.LogicErrorException;
import org.mirapolis.log.Log;
import org.mirapolis.log.LogFactory;
import org.mirapolis.mvc.action.client.ConfirmActionClientElement;
import org.mirapolis.mvc.action.client.OpenModalActionClientElement;
import org.mirapolis.mvc.model.entity.datafields.LookupField;
import org.mirapolis.mvc.model.entity.fields.FloatFieldBuilder;
import org.mirapolis.mvc.model.entity.fields.MemoFieldBuilder;
import org.mirapolis.mvc.model.entity.fields.processors.ChangeLabelFieldComponentProcessor;
import org.mirapolis.orm.ComboValueHelper;
import org.mirapolis.orm.DataObject;
import org.mirapolis.orm.constraint.NotEmptyBean;
import org.mirapolis.orm.ddl.ExecScriptException;
import org.mirapolis.orm.ddl.Script;
import org.mirapolis.orm.fields.*;
import org.mirapolis.service.message.Localized;
import org.mirapolis.service.message.LocalizedMessage;
import org.mirapolis.sql.SQL;
import org.mirapolis.sql.fragment.Column;
import org.mirapolis.sql.fragment.Constant;
import org.mirapolis.sql.fragment.DeleteQuery;
import org.mirapolis.sql.fragment.SelectQuery;
import org.mirapolis.util.CollectionUtils;
import org.mirapolis.util.DateHelper;
import org.mirapolis.util.IntHelper;
import org.mirapolis.util.StringHelper;

import javax.validation.constraints.NotNull;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Оцениваемый
 *
 * @author Elena Puzakova
 * @since 21.05.12 10:18
 */
public class PRMemberBean extends ReflectDataBean {
	public static final String DATANAME = "AT$PRMEMBER";
    private static final Integer COMMENT_LENGTH = 4000;
	public static final String ALIAS = "PRM";

	public static final String ID = "prmid";
	public static final String PROCEDURE_ID = ProcedureBean.ID;
    public static final String PARENT_ID = "parentid";
	public static final String PERSON_ID = "personid";
    public static final String WORK_ID = "workid";
    public static final String POST_ID = "personpost";
    public static final String CA_ID = "personcaid";
    public static final String CA_POST_ID = PostBean.ID;
	public static final String SCORE = "prmscore";
	public static final String POINT = "prmpoint";
	public static final String START_DATE = "prmstartdate";
	public static final String END_DATE = "prmenddate";
	public static final String KPI_RESULT = "prmkpiresult";
	public static final String ARCHIVE_DATE = "prmdatearchive";
    public static final String RESTORE_ARCHIVE_DATE = "prmdaterestorearchive";
    public static final String ESTIMATOR_COMMENT = "prmestimatorcomment";
    public static final String MANUAL_RESULT_POINT = "prmresultpoint";
    public static final String MANUAL_RESULT_PERCENT = "prmresultpercent";
    public static final String MANUAL_FREE_RESULT = "prmfreeresult";
	public static final String PROFILE_KPI_ID = "profilekpiid";
    public static final String MANUAL_KPI_RESULT_POINT = "prmkpiresultpoint";
    public static final String MANUAL_KPI_RESULT_PERCENT = "prmkpiresultpercent";
    public static final String MANUAL_KPI_FREE_RESULT = "prmkpifreeresult";
	public static final String MANUAL_KPI_RESULT_COMMENT = "prmkpiresultcomment";
    public static final String TRIGGER_RESULT = "triggerresult";
    public static final String TRIGGER_RESULT_FORMULA = "triggerresultformula";
	public static final String DEVELOPMENT_RESULT_PERCENT = "prmdevresultpercent";
    public static final String RECOMMEND_RESERVE = "prmreserve";
    public static final String CAREER_EXPECTATIONS_POST = "post";
    public static final String CAREER_EXPECTATIONS = "prmcareerexp";
    public static final String INDIVIDUAL_PHASE_DATES = "prmindphasedates";
    public static final String PROCEDURE_RESULT = "prmprocresult";
    public static final String PERSON_DATA = "persondata";
    public static final String KPI_SETTINGS_ID = "kpisettingsid";
    public static final String POST_PROFILE_REQUIREMENT_ID = "postprofilerequirementid";

    private static final Log log = LogFactory.getLog(PRMemberBean.class);

	@Name(ID)
	private String id;
    /**
     * Для версии ссылка на карту
     */
    @Name(PARENT_ID)
    private String parentId;
	/**
	 * Оцениваемый пользователь
	 */
	@Name(PERSON_ID)
    @NotEmptyBean
	private NameBean person;
    /**
     * Работа, по которой зарегистрирован пользователь
     */
	@Name(WORK_ID)
    @NotNull
    private String workId;
    /**
     * Должность пользователя при создании карты
     */
    @Name(POST_ID)
    private NameBean post;
    /**
     * Организация пользователя при создании карты
     */
    @Name(CA_ID)
    private NameBean organization;
    /**
     * Штатная должность пользователя при создании карты
     */
    @Name(CA_POST_ID)
    private NameBean caPost;
	/**
	 * Процедура
	 */
	@Name(PROCEDURE_ID)
	private NameBean procedure;
	/**
	 * Итоговая оценка в процентах
	 */
	@Name(SCORE)
	private Double score;
	/**
	 * Итоговая оценка в баллах
	 */
	@Name(POINT)
	private Double point;
    /**
     * Комментарий оценивающего
     */
    @Name(ESTIMATOR_COMMENT)
    private String estimatorComment;
	/**
	 * Дата начала оценки
	 */
	@Name(START_DATE)
	private Date startDate;
	/**
	 * Дата окончания
	 */
	@Name(END_DATE)
	private Date endDate;
	/**
	 * Дата помещения в архив
	 */
	@Name(ARCHIVE_DATE)
	private Date archiveDate;
    /**
     * Дата восстановления из архива
     */
	@Name(RESTORE_ARCHIVE_DATE)
    private Date restoreArchiveDate;
	/**
	 * Итог по kpi
	 */
	@Name(KPI_RESULT)
	private Double kpiResult;
    /**
     * Процент (Ручной ввод итога по процедуре)
     */
    @Name(MANUAL_RESULT_PERCENT)
    private Double manualResultPercent;
    /**
     * Балл (Ручной ввод итога по процедуре)
     */
    @Name(MANUAL_RESULT_POINT)
    private Double manualResultPoint;
    /**
     * Свободный ввод (Ручной ввод итога по процедуре)
     */
    @Name(MANUAL_FREE_RESULT)
    private String manualFreeResult;
	/**
	 * Профиль KPI
	 */
	@Name(PROFILE_KPI_ID)
	private NameBean profileKpi;
    /**
     * Процент kpi (Руччной ввод итога по процедуре)
     */
    @Name(MANUAL_KPI_RESULT_PERCENT)
    private Double manualKPIResultPercent;
    /**
     * Балл kpi (Ручной ввод итога по процедуре)
     */
    @Name(MANUAL_KPI_RESULT_POINT)
    private Double manualKPIResultPoint;
    /**
     * Свободный ввод kpi (Ручной ввод итога по процедуре)
     */
    @Name(MANUAL_KPI_FREE_RESULT)
    private String manualKPIFreeResult;
	/**
	 * Комментарий к результату по kpi
	 */
	@Name(MANUAL_KPI_RESULT_COMMENT)
	private String manualKPIResultComment;
    /**
     * Оценка с учетом триггеров
     */
	@Name(TRIGGER_RESULT)
    private Double triggerResult;
    /**
     * Формула для расчета оценки с учетом триггеров
     */
	@Name(TRIGGER_RESULT_FORMULA)
    private String triggerResultFormula;
    /**
     * Рекомендовать в кадровый резерв
     */
    @Name(RECOMMEND_RESERVE)
    private Boolean recommendReserve;
    /**
     * Карьерные ожидания
     */
    @Name(CAREER_EXPECTATIONS_POST)
    private Set<NameBean> careerExpectationPosts;
    /**
     * Карьерные ожидания
     */
    @Name(CAREER_EXPECTATIONS)
    private String careerExpectations;
    /**
     * Использовать индивидуальные сроки этапов
     */
    @Name(INDIVIDUAL_PHASE_DATES)
    private Boolean individualPhaseDates;
	/**
	 * Текущий процент выполнения рекомендаций
	 */
	@Name(DEVELOPMENT_RESULT_PERCENT)
	private Double developmentResultPercent;
    /**
     * Итоговая оценка по процедуре
     */
    @Name(PROCEDURE_RESULT)
    private Double procedureResult;
    @Name(PERSON_DATA)
	private PrMemberPersonDataBean personData;
    /**
     * Индивидуальное условие настройки показателей
     */
    @Name(KPI_SETTINGS_ID)
    private String kpiSettingsId;
    /**
     * Профиль требований к должности
     */
    @Name(POST_PROFILE_REQUIREMENT_ID)
    private NameBean postProfileRequirement;

	/**
	 * Оценки по компетенциям
	 */
	private List<PRMemberQuaBean> quaScores;
	/**
	 * Даты этапа (для уведрмлений)
	 */
	private ProcedurePhaseDateBean procedurePhaseDates;
    private PRMemberPhaseDateBean memberPhaseDates;

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public NameBean getPerson() {
		return person;
	}

	public void setPerson(NameBean person) {
		this.person = person;
	}

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public NameBean getPost() {
        return post;
    }

    public void setPost(NameBean post) {
        this.post = post;
    }

    public NameBean getOrganization() {
        return organization;
    }

    public void setOrganization(NameBean organization) {
        this.organization = organization;
    }

    public NameBean getCaPost() {
        return caPost;
    }

    public void setCaPost(NameBean caPost) {
        this.caPost = caPost;
    }

    public NameBean getProcedure() {
		return procedure;
	}

	public void setProcedure(NameBean procedure) {
		this.procedure = procedure;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getArchiveDate() {
		return archiveDate;
	}

	public void setArchiveDate(Date archiveDate) {
		this.archiveDate = archiveDate;
	}

    public Date getRestoreArchiveDate() {
        return restoreArchiveDate;
    }

    public void setRestoreArchiveDate(Date restoreArchiveDate) {
        this.restoreArchiveDate = restoreArchiveDate;
    }

	public Double getPoint() {
		return point;
	}

	public void setPoint(Double point) {
		this.point = point;
	}

	public Double getKpiResult() {
		return kpiResult;
	}

	public void setKpiResult(Double kpiResult) {
		this.kpiResult = kpiResult;
	}

    public String getEstimatorComment() {
        return estimatorComment;
    }

    public void setEstimatorComment(String estimatorComment) {
        this.estimatorComment = estimatorComment;
    }

    public Double getManualResultPercent() {
        return manualResultPercent;
    }

    public void setManualResultPercent(Double manualResultPercent) {
        this.manualResultPercent = manualResultPercent;
    }

    public Double getManualResultPoint() {
        return manualResultPoint;
    }

    public void setManualResultPoint(Double manualResultPoint) {
        this.manualResultPoint = manualResultPoint;
    }

    public String getManualFreeResult() {
        return manualFreeResult;
    }

    public void setManualFreeResult(String manualFreeResult) {
        this.manualFreeResult = manualFreeResult;
    }

    public NameBean getProfileKpi() {
        return profileKpi;
    }

    public void setProfileKpi(NameBean profileKpi) {
        this.profileKpi = profileKpi;
    }

    public Double getManualKPIResultPercent() {
        return manualKPIResultPercent;
    }

    public void setManualKPIResultPercent(Double manualKPIResultPercent) {
        this.manualKPIResultPercent = manualKPIResultPercent;
    }

    public Double getManualKPIResultPoint() {
        return manualKPIResultPoint;
    }

    public void setManualKPIResultPoint(Double manualKPIResultPoint) {
        this.manualKPIResultPoint = manualKPIResultPoint;
    }

    public String getManualKPIFreeResult() {
        return manualKPIFreeResult;
    }

    public void setManualKPIFreeResult(String manualKPIFreeResult) {
        this.manualKPIFreeResult = manualKPIFreeResult;
    }

    public String getManualKPIResultComment() {
        return manualKPIResultComment;
    }

    public void setManualKPIResultComment(String manualKPIResultComment) {
        this.manualKPIResultComment = manualKPIResultComment;
    }

    public Boolean getRecommendReserve() {
        return recommendReserve;
    }

    public void setRecommendReserve(Boolean recommendReserve) {
        this.recommendReserve = recommendReserve;
    }

    public Set<NameBean> getCareerExpectationPosts() {
        return careerExpectationPosts;
    }

    public void setCareerExpectationPosts(Set<NameBean> careerExpectationPosts) {
        this.careerExpectationPosts = careerExpectationPosts;
    }

    public String getCareerExpectations() {
        return careerExpectations;
    }

    public void setCareerExpectations(String careerExpectations) {
        this.careerExpectations = careerExpectations;
    }

    public Boolean getIndividualPhaseDates() {
        return individualPhaseDates;
    }

    public void setIndividualPhaseDates(Boolean individualPhaseDates) {
        this.individualPhaseDates = individualPhaseDates;
    }

	public Double getDevelopmentResultPercent() {
		return developmentResultPercent;
	}

	public void setDevelopmentResultPercent(Double developmentResultPercent) {
		this.developmentResultPercent = developmentResultPercent;
	}

    public Double getProcedureResult() {
        return procedureResult;
    }

    public void setProcedureResult(Double procedureResult) {
        this.procedureResult = procedureResult;
    }

    public Double getTriggerResult() {
        return triggerResult;
    }

    public void setTriggerResult(Double triggerResult) {
        this.triggerResult = triggerResult;
    }

    public String getTriggerResultFormula() {
        return triggerResultFormula;
    }

    public void setTriggerResultFormula(String triggerResultFormula) {
        this.triggerResultFormula = triggerResultFormula;
    }

	public PrMemberPersonDataBean getPersonData() {
		return personData;
	}

	public void setPersonData(PrMemberPersonDataBean personData) {
		this.personData = personData;
	}

    public String getKpiSettingsId() {
        return kpiSettingsId;
    }

    public void setKpiSettingsId(String kpiSettingsId) {
        this.kpiSettingsId = kpiSettingsId;
    }

    public NameBean getPostProfileRequirement() {
        return postProfileRequirement;
    }

    public void setPostProfileRequirement(NameBean postProfileRequirement) {
        this.postProfileRequirement = postProfileRequirement;
    }

    @Override
	public String getDataName() {
		return DATANAME;
	}

    public static DataObject createDataObject() {
        DataObject dataObject = new DataObject(DATANAME, ProcedureMessage.member_scored, PRMemberBean.class).setFields(
                new KeyField(ID),
                new FKField(PARENT_ID, ProcedureMessage.member_map, DATANAME, FKField.CASCADE),
                new LookupField(PROCEDURE_ID, ProcedureMessage.name_procedure, ProcedureFrame.NAME),
                new PersonLookupFieldBuilder(
                        new LookupField(PERSON_ID, ProcedureMessage.member_scored, PersonFrame.NAME, FKField.RESTRICT)
                                .addBeforeSetNotNullConstraintDdlScript(getDeleteMemberScript(PERSON_ID)))
                        .getDataField()
                        .setIsName(),
                new FKField(WORK_ID, PersonMessage.aw_work, PersonWorkBean.DATANAME, FKField.RESTRICT)
                        .addBeforeSetNotNullConstraintDdlScript(getDeleteMemberScript(WORK_ID)),
                new RSField(POST_ID, PersonMessage.post, PostRubricator.PERSON_POST, FKField.RESTRICT),
                new LookupField(CA_ID, PersonMessage.division, CAFrame.NAME, FKField.RESTRICT),
                new LookupField(CA_POST_ID, PersonMessage.state_post, PostFrame.NAME, FKField.RESTRICT),
                new DoubleField(SCORE, ProcedureMessage.final_assessment_competency_profile),
                new DoubleField(POINT, CareerPlanningMessage.final_point_competence_profile),
                new DateField(START_DATE, ProcedureMessage.start_date_evaluation_procedure),
                new DateField(END_DATE, ProcedureMessage.end_date_evaluation_procedure),
                new DateTimeField(ARCHIVE_DATE, ProcedureMessage.archive_date),
                new DateTimeField(RESTORE_ARCHIVE_DATE, ProcedureMessage.restoration_date_from_archive),
                new DoubleField(KPI_RESULT, ProcedureMessage.result_kpi_profile),
                new MemoFieldBuilder(
                        new StringField(ESTIMATOR_COMMENT, ProcedureMessage.comment_estimator, COMMENT_LENGTH))
                        .getDataField(),
                new FloatFieldBuilder(
                        new DoubleField(
                                MANUAL_RESULT_PERCENT, getScorePercentCaption(ProcedureMessage.manual_result_procedure)))
                        .addFieldComponentProcessor(
                                new ChangeLabelFieldComponentProcessor(ProcedureMessage.manual_result_procedure))
                        .getDataField(),
                new FloatFieldBuilder(
                        new DoubleField(
                                MANUAL_RESULT_POINT, getScorePointCaption(ProcedureMessage.manual_result_procedure)))
                        .addFieldComponentProcessor(
                                new ChangeLabelFieldComponentProcessor(ProcedureMessage.manual_result_procedure))
                        .getDataField(),
                new StringField(MANUAL_FREE_RESULT, ProcedureMessage.manual_result_procedure),
                new FloatFieldBuilder(
                        new DoubleField(
                                MANUAL_KPI_RESULT_PERCENT, getScorePercentCaption(ProcedureMessage.final_kpi_score)))
                        .addFieldComponentProcessor(
                                new ChangeLabelFieldComponentProcessor(ProcedureMessage.final_kpi_score))
                        .getDataField(),
                new FloatFieldBuilder(
                        new DoubleField(
                                MANUAL_KPI_RESULT_POINT, getScorePointCaption(ProcedureMessage.final_kpi_score)))
                        .addFieldComponentProcessor(
                                new ChangeLabelFieldComponentProcessor(ProcedureMessage.final_kpi_score))
                        .getDataField(),
                new StringField(MANUAL_KPI_FREE_RESULT, ProcedureMessage.final_kpi_score),
                new PRMemberKPIResultCommentFieldBuilder(
                        new StringField(MANUAL_KPI_RESULT_COMMENT, MeasureMessage.comment, COMMENT_LENGTH))
                        .getDataField(),
                new CheckField(RECOMMEND_RESERVE, ProcedureMessage.recommend_reserve),
                new MultiRSField(
                        CAREER_EXPECTATIONS_POST, ProcedureMessage.career_expectations, PostRubricator.PERSON_POST),
                new MemoFieldBuilder(new StringField(CAREER_EXPECTATIONS, ProcedureMessage.career_expectations))
                        .getDataField(),
                new CheckField(INDIVIDUAL_PHASE_DATES, ProcedureMessage.use_individual_phase_dates),
                new DoubleField(DEVELOPMENT_RESULT_PERCENT, ProcedureMessage.current_percentage_recommendations),
                new DoubleField(PROCEDURE_RESULT, ProcedureMessage.result_procedure),
                new DoubleField(TRIGGER_RESULT, ProcedureMessage.assessment_by_trigger),
                new StringField(
                        TRIGGER_RESULT_FORMULA,
                        Localized.group(
                                ProcedureMessage.formula_calculating,
                                Localized.valueOf(": "),
                                ProcedureMessage.assessment_by_trigger),
                        FormulaFieldBuilder.LENGTH),
                new LookupField(PROFILE_KPI_ID, PostMessage.profile_kpi, ProfileKPIFrame.NAME, FKField.SET_NULL),
                new FKField(
                        KPI_SETTINGS_ID,
                        ProcedureMessage.individual_condition_settings_kpi,
                        PrMemberKpiSettingsBean.DATANAME,
                        FKField.SET_NULL),
                new LookupField(
                        POST_PROFILE_REQUIREMENT_ID,
                        PostMessage.post_profile_requirements,
                        CAPostProfileRequirementFrame.NAME,
                        FKField.SET_NULL)
        );
        dataObject.addChildDataObject(PrMemberPersonDataBean.createDataObject());
        return dataObject;
    }

    private static LocalizedMessage getScorePercentCaption(LocalizedMessage caption) {
        return Localized.group(caption, Localized.valueOf(" ("), QuaMessage.procent, Localized.valueOf(")"));
    }

    private static LocalizedMessage getScorePointCaption(LocalizedMessage caption) {
        return Localized.group(caption, Localized.valueOf(" ("),  QuaMessage.point, Localized.valueOf(")"));
    }

    private static Script getDeleteMemberScript(String field) {
        return new Script() {
            @Override
            protected void doExecute(Statement stmt, Boolean doUpdate) throws ExecScriptException {
                SelectQuery idQuery = SelectQuery.select(ID).from(DATANAME)
                        .where(Column.column(field).isNull());
                deleteWrongMembers(idQuery);
            }
        };
    }

    public static void deleteWrongMembers(SelectQuery idQuery) {
        Session.execute(DatabaseStore.getDefault(), new SQL(
                DeleteQuery.deleteFrom(ObjRouteBean.DATANAME)
                        .where(Column.column(ObjRouteBean.OBJ_TYPE)
                                .eq(Constant.textWhereConst(ProcedureRouteObject.TYPE))
                                .and(Column.column(ObjRouteBean.OBJ_TABLE_ID)
                                        .in(idQuery.setChild())))
        ));

        Session.execute(DatabaseStore.getDefault(), new SQL(
                DeleteQuery.deleteFrom(ProcedureTaskMemberBean.DATANAME)
                        .where(Column.column(ProcedureTaskMemberBean.OBJTABLENAME)
                                .eq(Constant.textWhereConst(PRMemberFrame.NAME))
                                .and(Column.column(ProcedureTaskMemberBean.OBJTABLEID)
                                        .in(idQuery.setChild())))
        ));

        Session.execute(DatabaseStore.getDefault(), new SQL(
                DeleteQuery.deleteFrom(DATANAME)
                        .where(Column.column(ID).in(idQuery.setChild()))
        ));
    }

    public void updateScoreForView() {
        //Оценки отображаются с 2 знаками
        Stream.of(POINT, SCORE, KPI_RESULT, DEVELOPMENT_RESULT_PERCENT)
                .forEach(field -> {
                    Double value = getFieldValueByName(field);
                    setFieldValueByName(field, ProcedureService.getInstance().getFormattedDouble(value));
                });
    }

	public void updateScoreForSave(PRMemberBean old) {
		//Заполнить оценки старыми значениями, т.к. для отображения убирались знаки
		setPoint(old.getPoint());
		setScore(old.getScore());
		setKpiResult(old.getKpiResult());
		setDevelopmentResultPercent(old.getDevelopmentResultPercent());
		//На форме оцениваемого нет работы, заполнить, чтобы не очистилось при сохранении
		setWorkId(old.getWorkId());
	}

	public void updateOrganization() {
        CAService.getInstance().updateNameBeanCALocalizedName(getOrganization());
    }

	public boolean isArchive() {
		return DateHelper.isNotNull(getArchiveDate());
	}

	public List<PRMemberQuaBean> getQuaScores() {
		return quaScores;
	}

	public void setQuaScores(List<PRMemberQuaBean> quaScores) {
		this.quaScores = quaScores;
	}

    public String getPeriod() {
        return DateHelper.getDate(getStartDate()) + " - " + DateHelper.getDate(getEndDate());
    }

	public PRMemberPhaseDateBean getMemberPhaseDates(String phaseId) {
		if (getIndividualPhaseDates() && (memberPhaseDates == null || !phaseId.equals(memberPhaseDates.getPhaseId()))) {
			memberPhaseDates = PRMemberPhaseDateBean.getByPhase(getId(), phaseId, new PRMemberPhaseDateBean());
		}
		return memberPhaseDates;
	}

    public ProcedurePhaseDateBean getProcedurePhaseDates(String phaseId) {
        if (procedurePhaseDates == null || !phaseId.equals(procedurePhaseDates.getPhaseId())) {
            procedurePhaseDates = ProcedurePhaseDateBean.getByPhase(getProcedure().getId(), phaseId, new ProcedurePhaseDateBean());
        }
        return procedurePhaseDates;
    }

    public static final String CATEGORIES_DESC = "lms.core.newprocedure.ProcedureMessage.estimator_categories";
    public static final String CATEGORY_DESC = "lms.core.newprocedure.ProcedureMessage.category_estimating";
    public static final String ADDITIONAL_CATEGORY_CODE_DESC =
        CATEGORY_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]";
    private static final String KPI_FIELD_NAME_DB_DESC = "lms.core.newprocedure.ProcedureMessage.kpi_field_names|[ (]|" +
        "mira.entityversion.EntityVersionMessage.database|[)]";
    private static final String POINT_FIELD_NAME_DB_DESC =
        "lms.core.newprocedure.ProcedureMessage.control_point_field_names|[ (]|" +
        "mira.entityversion.EntityVersionMessage.database|[)]";

    @RouteSystemAction(actionTypes = {RouteSystemActionType.button})
    public EstimatorSurveyFormScoreSaver getScoreSaver(RouteCommandProcessor processor) {
        return new EstimatorSurveyFormScoreSaver(this, processor);
    }

    @RouteSystemAction
    @Deprecated
	public boolean saveSurveyScores(RouteCommandProcessor processor, String pollTextCode) {
        return new EstimatorSurveyFormScoreSaver(this, processor).setPollTextCode(pollTextCode).save();
	}

    @RouteSystemAction
    @Deprecated
    public void saveDraft(RouteCommandProcessor processor) {
        new EstimatorSurveyFormScoreSaver(this, processor).saveDraft();
    }

	@RouteSystemAction
    @Deprecated
	public void saveSurveyQuaScores(RouteCommandProcessor processor, boolean completeQua) {
		new EstimatorSurveyFormScoreSaver(this, processor).saveAndCompleteQua(completeQua);
	}

    @RouteSystemAction
    @Deprecated
	public boolean isSurveyCompetencyScoresFilled(RouteCommandProcessor processor) {
        return new EstimatorSurveyFormScoreSaver(this, processor).isSurveyCompetencyScoresFilled();
    }

	@RouteSystemAction
    @Deprecated
	public void savePoll(RouteCommandProcessor processor, boolean isComplete, String textCode) {
		new EstimatorSurveyFormScoreSaver(this, processor).setPollTextCode(textCode).saveAndCompletePoll(isComplete);
	}

    @RouteSystemAction
    @Deprecated
    public void savePollAndGoIfNotFilled(RouteCommandProcessor processor,
                                         String paramName,
                                         String paramValue,
                                         String textCode) {
        new EstimatorSurveyFormScoreSaver(this, processor)
                .setPollTextCode(textCode).savePollAndGoIfNotFilled(paramName, paramValue);
    }

	@RouteSystemAction(description =
        "hr.checking.CheckingMessage.recalculate_results|[ (]|lms.core.person.PersonMessage.qualifications|[)]")
	public void recalculateQuaScoresByFilledSurveys() {
        logStartAction("recalculateQuaScoresByFilledSurveys");
        PRMemberService.getInstance().getQuaCalculator().calculateMemberScoreByFilledSurveys(this);
        logEndAction("recalculateQuaScoresByFilledSurveys");
    }

	/**
	 * Категория завершила оценивание
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.check_category_complete_estimation",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
	public boolean isCategoryComplete(String categoryValue, String categoryCode) {
        logStartAction("isCategoryComplete");
        ProcedureEstimatingCategory category = getCategory(categoryValue);
		if (category == null) {
		    return false;
        }
		//Анкеты по категории
		List<PRSurveyBean> surveys = getCategorySurveys(categoryValue, categoryCode);
        //Нет анкет по категории
        if (surveys.isEmpty()) {
            return true;
        }
		//Все анкеты завершены
		boolean allComplete = PRSurveyService.isSurveysComplete(surveys);
		if (category.isOneSurvey()) {
			return allComplete;
		} else {
			PRCategoryBean bean = PRCategoryService.getCategory(surveys.get(0).getCategory().getId());
			Integer minCount = bean.getMinCount();
			return IntHelper.isNotNull(minCount) ?
					bean.getMinUnit().isComplete(surveys, minCount) :
					allComplete;
		}
	}

	/**
	 * Изменить статус анкет категории на Не завершена
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.set_category_surveys_not_completed",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
	public void setSurveyNotCompleted(String categoryValue, String categoryCode) {
        logStartAction("setSurveyNotCompleted");
		PRSurveyService.getInstance().setSurveysNotCompletedStatus(getCategorySurveys(categoryValue, categoryCode));
		logEndAction("setSurveyNotCompleted");
	}


    /**
     * Изменить статус анкеты на Не завершена
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.set_category_surveys_not_completed",
        actionTypes = {RouteSystemActionType.button})
    public void setSurveyNotCompleted(RouteCommandProcessor processor) {
        updateSurvey(
            "setSurveyNotCompleted",
            processor,
            surveys -> PRSurveyService.getInstance().setSurveysNotCompletedStatus(surveys));
    }

    @RouteSystemAction
    @Deprecated
	public void saveKPIs(RouteCommandProcessor processor) {
        new EstimatorSurveyFormScoreSaver(this, processor).saveKpi();
    }

    @RouteSystemAction
    @Deprecated
	public void setCompleteKPI(RouteCommandProcessor processor) {
        new EstimatorSurveyFormScoreSaver(this, processor).saveAndCompleteKpi();
	}

	/**
	 * Изменить статус заполнения KPI анкет категории на Не завершен
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.cancel_completion_kpi",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
	public void setNotCompleteKPI(String categoryValue, String categoryCode) {
        logStartAction("setNotCompleteKPI: categoryValue=" + categoryValue + ", categoryCode=" + categoryCode);
        PRSurveyService.getInstance().setSurveysCompleteKpi(
                getCategorySurveys(categoryValue, categoryCode), false);
        logEndAction("setNotCompleteKPI: categoryValue=" + categoryValue + ", categoryCode=" + categoryCode);
	}

    /**
     * Изменить статус заполнения KPI анкет категории Завершен
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_kpi",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public void setCompleteKPI(String categoryValue, String categoryCode) {
        logStartAction("setCompleteKPI: categoryValue=" + categoryValue + ", categoryCode=" + categoryCode);
        PRSurveyService.getInstance().setSurveysCompleteKpi(
                getCategorySurveys(categoryValue, categoryCode), true);
        logEndAction("setCompleteKPI: categoryValue=" + categoryValue + ", categoryCode=" + categoryCode);
    }

    /**
     * Изменить статус заполнения KPI анкет категории Завершен
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.complete_kpi")
    public void completeKPIForAllSurveys() {
        logStartAction("completeKPIForAllSurveys");
        PRSurveyService.getInstance().setSurveysCompleteKpi(PRSurveyService.listMemberSurvey(getId()), true);
        logEndAction("completeKPIForAllSurveys");
    }

	/**
	 * Изменить статус заполнения задач текущей анкеты на Завершен
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_tasks",
        actionTypes = {RouteSystemActionType.button})
	public void setCompleteTask(RouteCommandProcessor processor) {
		updateSurvey(
		    "setCompleteTask",
            processor,
            surveys -> PRSurveyService.getInstance().setSurveysCompleteTask(surveys, true));
	}

	/**
	 * Изменить статус заполнения задач анкет категории на Не завершен
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.cancel_completion_tasks",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
	public void setNotCompleteTask(String categoryValue, String categoryCode) {
        logStartAction("setNotCompleteTask");
        PRSurveyService.getInstance().setSurveysCompleteTask(
                getCategorySurveys(categoryValue, categoryCode), false);
        logEndAction("setNotCompleteTask");
	}

    /**
     * Изменить статус заполнения рекомендаций по развитию текущей анкеты на Завершен
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_filling_development_recommendations",
        actionTypes = {RouteSystemActionType.button})
    public void setCompleteDevelopment(RouteCommandProcessor processor) {
        updateSurvey(
            "setCompleteDevelopment",
            processor,
            surveys -> PRSurveyService.getInstance().setSurveysCompleteDevelopment(surveys, true));
    }

    /**
     * Изменить статус заполнения рекомендаций по развитию на Не завершен для анкет категории
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.cancel_completion_development_recommendations",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public void setNotCompleteDevelopment(String categoryValue, String categoryCode) {
        logStartAction("setNotCompleteDevelopment");
        PRSurveyService.getInstance().setSurveysCompleteDevelopment(
                getCategorySurveys(categoryValue, categoryCode), false);
        logEndAction("setNotCompleteDevelopment");
    }

    /**
     * Завершить заполнения рекомендаций по развитию для категории
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_filling_development_recommendations",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public void setCompleteDevelopmentForCategory(String categoryValue, String categoryCode) {
        logStartAction("setCompleteDevelopmentForCategory");
        PRSurveyService.getInstance().setSurveysCompleteDevelopment(
                getCategorySurveys(categoryValue, categoryCode), true);
        logEndAction("setCompleteDevelopmentForCategory");
    }

    /**
     * Завершить заполнения рекомендаций по развитию по всем анкетам карты
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_filling_development_recommendations")
    public void completeDevelopmentForAllSurveys() {
        logStartAction("completeDevelopmentForAllSurveys");
        PRSurveyService.getInstance().setSurveysCompleteDevelopment(
                PRSurveyService.listMemberSurvey(getId()), true);
        logEndAction("completeDevelopmentForAllSurveys");
    }
    /**
     * Изменение статуса анкеты на Завершена (для анкет, в которых отключены все оценочные блоки)
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_survey",
        actionTypes = {RouteSystemActionType.button})
    public void completeSurvey(RouteCommandProcessor processor) {
        updateSurvey(
            "completeSurvey",
            processor,
            surveys -> PRSurveyService.getInstance().setSurveysCompletedStatusIfNotEstimate(surveys));
    }

    /**
     * Изменение статуса анкет категории на Завершена (для анкет, в которых отключены все оценочные блоки);
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.complete_survey",
        paramsDescription = {CATEGORIES_DESC})
    public void completeCategorySurveys(String ... categoryValues) {
        logStartAction("completeCategorySurveys");
        getCategories(categoryValues).forEach(category ->
                PRSurveyService.getInstance().setSurveysCompletedStatusIfNotEstimate(
                        PRSurveyService.listSurveyByMemberAndCategory(getId(), category)));
        logEndAction("completeCategorySurveys");
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.cancel_surveys_by_category",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public void cancelCategorySurveys(String categoryValue, String categoryCode) {
        logStartAction("cancelCategorySurveys");
        PRSurveyService.getInstance().cancelSurveysWithoutCheckAccess(getCategorySurveys(categoryValue, categoryCode));
        logEndAction("cancelCategorySurveys");
    }

    /**
     * @return Все ли анкеты завершили оценку по компетенциям
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.checking_complete_qua")
    public boolean isAllCategoryCompleteQua() {
        logStartAction("isAllCategoryCompleteQua");
        return PRSurveyService.listMemberActiveSurvey(getId()).stream()
                .allMatch(survey -> !survey.getIsQua() || survey.getIsQuaComplete());
    }

    @RouteSystemAction
    @Deprecated
    public void confirmSelfAssign(RouteCommandProcessor processor, String ... categories) {
        PrSurveyCandidateSysActionService.getInstance().confirmSelfAssign(processor, categories);
    }

	@RouteSystemAction
    @Deprecated
	public void saveSelfAssignComments(RouteCommandProcessor processor, String ... categories) {
        PrSurveyCandidateSysActionService.getInstance().saveSelfAssignComments(processor, categories);
    }

    @RouteSystemAction
    @Deprecated
    public boolean checkRepeatingAssignment(RouteCommandProcessor processor) {
        return PrSurveyCandidateSysActionService.getInstance().checkRepeatingAssignment(processor);
    }

    /**
     * Создание плана развития
     */
    @RouteSystemAction(description = "hr.development.DevelopmentMessage.addPlan")
    public void createPlanDevelopment() {
        logStartAction("createPlanDevelopment");
        PRMemberDevelopmentService.getInstance().createPlanDevelopmentByMember(
                this, getProcedureBean(), PlanDevelopmentSaveValidationErrorProcessor.log);
        logEndAction("createPlanDevelopment");
    }

    /**
     * Добавление рекомендаций по развитию по компетенциям профиля сотрудника
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.generate_recommendations_by_qua",
        paramsDescription = {
            "lms.core.newprocedure.ProcedureMessage.estimation_not_match_required_level",
            "lms.core.newprocedure.ProcedureMessage.not_include_stages_executed"})
    public void createDevelopmentMethodsByPersonQuaProfile(boolean addMethodNotMatchRequirement,
                                                           boolean notIncludePlanStages) {
        logStartAction("createDevelopmentMethodsByPersonQuaProfile");
        PRMemberDevelopmentService.getInstance().createDevelopmentMethodsByCompetenciesPersonProfile(
                getId(), addMethodNotMatchRequirement, notIncludePlanStages);
        logEndAction("createDevelopmentMethodsByPersonQuaProfile");
    }

    /**
     * Добавление рекомендаций по развитию по компетенциям профиля оцениваемого
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.generate_recommendations_by_qua",
        paramsDescription = {
            "lms.core.newprocedure.ProcedureMessage.estimation_not_match_required_level",
            "lms.core.newprocedure.ProcedureMessage.not_include_stages_executed"})
    public void createDevelopmentMethodsByProcedureQuaProfile(boolean addMethodNotMatchRequirement,
                                                              boolean notIncludePlanStages) {
        logStartAction("createDevelopmentMethodsByProcedureQuaProfile");
        PRMemberDevelopmentService.getInstance().createDevelopmentMethodsByCompetenciesProcedureProfile(
                getId(), addMethodNotMatchRequirement, notIncludePlanStages);
        logEndAction("createDevelopmentMethodsByProcedureQuaProfile");
    }

    /**
     * Добавление рекомендаций по развитию по индикаторам профиля оцениваемого
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.generate_recommendations_by_indicator",
        paramsDescription = {
            "lms.core.newprocedure.ProcedureMessage.estimation_not_match_required_level",
            "lms.core.newprocedure.ProcedureMessage.not_include_stages_executed"})
    public void createDevelopmentMethodsByIndicatorProcedureProfile(boolean addMethodNotMatchRequirement,
                                                                    boolean notIncludePlanStages) {
        logStartAction("createDevelopmentMethodsByIndicatorProcedureProfile");
        PRMemberDevelopmentService.getInstance().createDevelopmentMethodsByIndicatorsProcedureProfile(
                getId(), addMethodNotMatchRequirement, notIncludePlanStages);
        logEndAction("createDevelopmentMethodsByIndicatorProcedureProfile");
    }

	/**
	 * Завершить оценку компетенций по всем анкетам карты
	 */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.complete_qua_for_all_surveys")
	public void completeQuaForAllSurveys() {
        logStartAction("completeQuaForAllSurveys");
        List<PRSurveyBean> surveys = PRSurveyService.listMemberSurvey(getId());
        Map<String, List<PRSurveyQuaBean>> allSurveyQuaScores = PRSurveyService.getSurveyQuaScoresWithIndicatorsAndCharacteristics(
                BeanHelper.getIdSet(surveys));
        boolean isRecalculateQua = false;
        for (PRSurveyBean survey : surveys) {
            PRSurveyService.getInstance().setSurveyCompleteQuaWithoutCheckAndRecalculate(
                    survey, allSurveyQuaScores.get(survey.getId()));
            isRecalculateQua = true;
        }
        if (isRecalculateQua) {
            PRMemberService.getInstance().getQuaCalculator().calculateMemberScore(this);
        }
        logEndAction("completeQuaForAllSurveys");
	}

	@RouteSystemAction
    @Deprecated
	public void completeQua(RouteCommandProcessor processor) {
        getScoreSaver(processor).completeQua();
	}

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.cancel_completion_qua",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public void setNotCompleteQua(String categoryValue, String categoryCode) {
        logStartAction("setNotCompleteQua: categoryValue = " + categoryValue + ", categoryCode = " + categoryCode);
        //Анкеты по категории
        List<PRSurveyBean> surveys = getCategorySurveys(categoryValue, categoryCode);
        Map<String, List<PRSurveyQuaBean>> allSurveyQuaScores = PRSurveyService.getSurveyQuaScoresWithIndicatorsAndCharacteristics(
                BeanHelper.getIdSet(surveys));
        boolean isRecalculateQua = surveys.stream()
                .anyMatch(survey -> PRSurveyService.getInstance().setSurveyNotCompleteQuaWithoutCheckAndRecalculate(
                        survey,allSurveyQuaScores.get(survey.getId())));
        if (isRecalculateQua) {
            PRMemberService.getInstance().getQuaCalculator().calculateMemberScore(this);
        }
        logEndAction("setNotCompleteQua: categoryValue = " + categoryValue + ", categoryCode = " + categoryCode);
    }

    /**
     * Завершить опросы по всем анкетам карты
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.complete_polls_all_questionnaires")
    public void completePollForAllSurveys() {
        logStartAction("completePollForAllSurveys");
        PRSurveyService.getInstance().setSurveysCompletePoll(PRSurveyService.listMemberSurvey(getId()));
        logEndAction("completePollForAllSurveys");
    }

	/**
	 * Изменить параметр Требуются действия для анкеты
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.set_parameter_required_actions",
        paramsDescription = {RouteCommandProcessor.REQUIRED_ACTION_DESC})
	public void setRequiredActions(String value) {
        logStartAction("setRequiredActions");
		String surveyId = getSurveyIdParam();
		if (StringHelper.isNotEmpty(surveyId)) {
            MyActionRequirementType requiredAction = ComboValueHelper.getComboValueByValue(
                    MyActionRequirementType.values(), value);
            ObjRouteBean objRoute = getObjRoute();
            PRMemberService.getInstance().setSurveyRequiredAction(objRoute.getId(), surveyId, requiredAction);
		} else {
            log.warn("Call system action setRequiredActions with empty params: surveyId=" +
                surveyId + ", memberId=" + getId());
        }
		logEndAction("setRequiredActions");
	}

	/**
	 * Изменить параметры Требуются действия для анкет категории
	 */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.set_parameter_required_actions_category",
        paramsDescription = {
            CATEGORY_DESC, RouteCommandProcessor.CODE_DESC, RouteCommandProcessor.REQUIRED_ACTION_DESC})
	public void setRequiredActionsForCategory(String categoryValue, String categoryCode, String value) {
        logStartAction("setRequiredActionsForCategory: " +
                "categoryValue = " + categoryValue + ", categoryCode = " + categoryCode + ", value = " + value);
		//Анкеты по категории
		List<PRSurveyBean> surveys = getCategorySurveys(categoryValue, categoryCode);
		ObjRouteBean objRoute = getObjRoute();
        MyActionRequirementType requiredAction = ComboValueHelper.getComboValueByValue(
                MyActionRequirementType.values(), value);
		for (PRSurveyBean survey : surveys) {
            PRMemberService.getInstance().setSurveyRequiredAction(objRoute.getId(), survey.getId(), requiredAction);
		}
        logEndAction("setRequiredActionsForCategory: " +
                "categoryValue = " + categoryValue + ", categoryCode = " + categoryCode + ", value = " + value);
	}

    /**
     * Есть расхождения в оценках по компетенциям
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.exist_differences_categories_qua_score")
    public boolean isDifferenceQuaScores() {
        logStartAction("isDifferenceQuaScores");
        boolean result = !PRCategoryService.getCategoryQuaDifferencesByMember(getId()).isEmpty();
        logEndAction("isDifferenceQuaScores " + result);
        return result;
    }

    @RouteSystemAction
    @Deprecated
    public void saveProcedureResult(RouteCommandProcessor processor) {
		new EstimatorSurveyFormScoreSaver(this, processor).saveProcedureResult();
	}

    @RouteSystemAction
    @Deprecated
    public boolean checkMinMaxQuaScoreComment(RouteCommandProcessor processor) {
        return new EstimatorSurveyFormScoreSaver(this, processor).checkMinMaxQuaScoreComment();
    }

    /**
     * Расчет результата участника по аттестации
     */
    @RouteSystemAction(description = "hr.attestation.AttestationMessage.calculation_participant_attestation_result")
    public Double calculateAttestationResult(RouteCommandProcessor processor) {
        return processWithLogicError(
                "calculateAttestationResult",
                processor,
                () -> {
                    ProcedureBean procedure = getProcedureBean();
                    String attestationId = procedure.getAttestation().getAttestation().getId();
                    if (StringHelper.isNotEmpty(attestationId)) {
                        AttestationService.getInstance().calculateAttestationPersonResult(
                                attestationId, getPerson().getId());
                        Double result = AttestationService.getInstance().getAttestationPerson(
                                attestationId, getPerson().getId()).getResult();
                        return DoubleValue.isNull(result) ? 0d : result;
                    }
                    return 0d;
                },
                0d);
    }

    /**
     * Результат участника по аттестации
     */
    @RouteSystemAction(description = "hr.attestation.AttestationMessage.results_participant_attestation")
    public Double getAttestationResult() {
        logStartAction("getAttestationResult");
        ProcedureBean procedure = getProcedureBean();
        String attestationId = procedure.getAttestation().getAttestation().getId();
        Double result = 0d;
        if (StringHelper.isNotEmpty(attestationId)) {
            AttestationPersonBean attPerson = AttestationService.getInstance().getAttestationPerson(
                    attestationId, getPerson().getId());
            result = attPerson == null || DoubleValue.isNull(attPerson.getResult()) ? 0d : attPerson.getResult();
        }
        logEndAction("getAttestationResult " + result);
        return result;
    }

	/**
	 * Сохранение измененного результата по аттестации
	 */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.saving_final_assessment_attestation")
	public void saveChangedAttestationResult() {
        logStartAction("saveChangedAttestationResult");
		ProcedureBean procedure = getProcedureBean();
		String attestationId = procedure.getAttestation().getAttestation().getId();
		if (StringHelper.isNotEmpty(attestationId)) {
			String scaleValueId = Context.get().getDataParameter(PRMemberFormBean.ATTESTATION_CHANGED_RESULT);
			AttestationPersonBean attestationPerson = AttestationService.getInstance().getAttestationPerson(
			        procedure.getAttestation().getAttestation().getId(), getPerson().getId());
			if (attestationPerson != null) {
                AttestationService.getInstance().changeAttestationPersonResult(
                        attestationPerson.getId(), scaleValueId, ProcedureFrame.NAME, procedure.getId());
			}
		}
		logEndAction("saveChangedAttestationResult");
	}

    /**
     * Проверка заполненности параметров показателей
     */
    @RouteSystemAction(actionTypes = {RouteSystemActionType.button})
    @Deprecated
    public boolean checkKPIFields(RouteCommandProcessor processor, Set<String> fieldNames, boolean errorInFields) {
        if (!checkActionForButton(processor, "checkKPIFields")) {
            return false;
        }
        Set<String> kpiFields = new HashSet<>();
        fieldNames.forEach(fieldName ->
            ProcedureKPIFieldStore.getInstance().findKpiFieldByMemberField(fieldName)
                .ifPresent(kpiField -> kpiFields.add(kpiField.getFieldName())));
        List<PRMemberKPIBean> memberKPIs = PRMemberKPIService.getInstance().getActiveMemberKPIForCheckInSurvey(
                getId(), getSurveyIdParam(), getProcedureBean());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkEmptyFieldsByAllKPIs(kpiFields);
        return !validator.isErrors();
    }

    @RouteSystemAction(actionTypes = {RouteSystemActionType.button})
    @Deprecated
    public boolean checkKPIFieldsByCategories(RouteCommandProcessor processor,
                                              Set<String> fieldNames,
                                              Set<String> kpiCategoryCodes,
                                              boolean errorInFields) {
        if (!checkActionForButton(processor, "checkKPIFieldsByCategories")) {
            return false;
        }
        Set<String> kpiFields = new HashSet<>();
        fieldNames.forEach(fieldName ->
            ProcedureKPIFieldStore.getInstance().findKpiFieldByMemberField(fieldName)
                .ifPresent(kpiField -> kpiFields.add(kpiField.getFieldName())));
        List<PRMemberKPIBean> memberKPIs = getMemberKPIsByCategoryCodesForCheckValues(kpiCategoryCodes, getProcedureBean());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkEmptyFieldsByCategories(kpiFields, kpiCategoryCodes);
        return !validator.isErrors();
    }

	/**
	 * Проверка заполненности параметров показателей
	 * @param processor
	 * @param kpiFieldNames названия параметров не зависящих от статуса показателя
	 * @param excludeStatusId статус, с котороым исключить показатели из проверки для полей statusKpiFieldNames
	 * @param statusKpiFieldNames названия параметров зависящих от статуса показателя (статус не должен быть excludeStatusId)
	 * @return есть пустые поля в паказателях
	 */
	@RouteSystemAction(actionTypes = {RouteSystemActionType.button})
    @Deprecated
    public boolean checkKPIFieldsByStatus(RouteCommandProcessor processor,
                                          Set<String> kpiFieldNames,
                                          String excludeStatusId,
                                          Set<String> statusKpiFieldNames,
                                          boolean errorInFields) {
		if (!checkActionForButton(processor, "checkKPIFieldsByStatus")) {
            return false;
        }
        Map<List<PRMemberKPIBean>, List<KpiField>> kpiFields = new HashMap<>();
		//Показатели, доступные на текущий момент оценивающему
		List<PRMemberKPIBean> memberKPIs = PRMemberKPIService.getInstance().getActiveMemberKPIForCheckInSurvey(
		        getId(), getSurveyIdParam(), getProcedureBean());

        //Проверить все показатели по полям kpiFieldNames
		if (CollectionUtils.isNotEmpty(kpiFieldNames)) {
            List<KpiField> fields = new ArrayList<>();
            kpiFieldNames.forEach(fieldName ->
                ProcedureKPIFieldStore.getInstance().findKpiFieldByMemberField(fieldName)
                    .ifPresent(fields::add));
            kpiFields.put(memberKPIs, fields);
        }
        //Проверить показатели, статус которых не excludeStatusId по полям statusKpiFieldNames
        if (StringHelper.isNotEmpty(excludeStatusId) && CollectionUtils.isNotEmpty(statusKpiFieldNames)) {
            List<PRMemberKPIBean> kpiByStatus = memberKPIs.stream()
                    .filter(bean -> excludeStatusId.equals(bean.getResultFields().getStatus().getId()))
                    .collect(Collectors.toList());
            List<KpiField> fields = new ArrayList<>();
            statusKpiFieldNames.forEach(fieldName ->
                ProcedureKPIFieldStore.getInstance().findKpiFieldByMemberField(fieldName)
                    .ifPresent(fields::add));
		    kpiFields.put(kpiByStatus, fields);
        }
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkEmptyKpiFields(kpiFields);
        return !validator.isErrors();
	}

    /**
     * Проверка количества показателей в карте
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkKPICount(RouteCommandProcessor processor, boolean errorInFields) {
        List<PRMemberKPIBean> memberKPIs = PRMemberKPIRestrictionService.getInstance().getMemberKPIWithImp(getId());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkSumCount();
        return !validator.isErrors();
    }

    /**
     * Проверка изменения полей показателей на текущем этапе
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.check_kpi_field_change_current_phase",
        paramsDescription = {KPI_FIELD_NAME_DB_DESC})
    public boolean isChangeKPIField(Set<String> fieldNames) {
        logStartAction("isChangeKPIField");
        return PrMemberKpiSysActionService.getInstance().isChangeKpiField(this, fieldNames);
    }

	/**
	 * Проверка важности показателей (сумма)
	 */
	@RouteSystemAction()
    @Deprecated
    public boolean checkKPISumImportance(RouteCommandProcessor processor, boolean isEqual, boolean errorInFields) {
        List<PRMemberKPIBean> memberKPIs = PRMemberKPIRestrictionService.getInstance().getMemberKPIWithImp(getId());
	    PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
	    validator.checkSumImportance(isEqual);
	    return !validator.isErrors();
	}

    /**
     * Проверка дат Должна быть достугнута для показателей:
     * попадают в Оцениваемый период процедуры
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.check_kpi_due_dates")
    public boolean checkKPIDueDate(RouteCommandProcessor processor) {
        logStartAction("checkKPIDueDate");
        ProcedureBean procedure = getProcedureBean();
        List<PRMemberKPIBean> kpiList = PRMemberKPIService.getInstance().getActiveMemberKPIs(getId());
        for (PRMemberKPIBean kpi : kpiList) {
            if (!kpi.checkDueDate(procedure.getEvaluationStartDate(), procedure.getEvaluationEndDate())) {
                processor.appendError(Localized.format(
                        ProcedureMessage.kpi_due_date_range,
                        DateHelper.getDate(procedure.getEvaluationStartDate()),
                        DateHelper.getDate(procedure.getEvaluationEndDate())).toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Изменение статусов всех показателей
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_status_all_kpi",
        paramsDescription = {RouteSystemActionProcessor.NEW_VALUE_DESC})
    public void changeAllKPIStatus(String status) {
        logStartAction("changeAllKPIStatus");
        PrMemberKpiSysActionService.getInstance().changeKpiStatus(this, status, Optional.empty());
        logEndAction("changeAllKPIStatus");
    }

    /**
     * Изменение статусов показателей, измененных на текущем этапе
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_status_kpi_current_stage",
        paramsDescription = {RouteSystemActionProcessor.NEW_VALUE_DESC})
    public void changeChangedKPIStatus(String status) {
        logStartAction("changeChangedKPIStatus");
        PrMemberKpiSysActionService.getInstance().changeKpiStatus(
            PRMemberKPIService.getInstance().getChangedKPIOnPhase(
                PRMemberKPIService.getInstance().getActiveMemberKPIs(getId()), getCurrentObjPhase()),
            status);
        logEndAction("changeChangedKPIStatus");
    }

    /**
     * Изменение статусов показателей, статус которых равен указанному в параметре
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_status_kpi_old_status",
        paramsDescription = {RouteSystemActionProcessor.NEW_VALUE_DESC, RouteSystemActionProcessor.CURRENT_VALUE_DESC})
    public void changeKPIStatus(String newStatus, String oldStatus) {
        logStartAction("changeKPIStatus");
        PrMemberKpiSysActionService.getInstance().changeKpiStatus(this, newStatus, Optional.of(oldStatus));
        logEndAction("changeKPIStatus");
    }

    /**
     * Изменить статусы показателей, находящихся в статусе currentStatusCode,
     * если были изменены поля fieldNames после присвоения этого статуса
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_kpi_status",
        paramsDescription = {
            RouteSystemActionProcessor.CURRENT_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            RouteSystemActionProcessor.NEW_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            KPI_FIELD_NAME_DB_DESC})
    public void changeKPIStatusIfChangeFields(String currentStatusCode, String newStatusCode, Set<String> fieldNames) {
        logStartAction("changeKPIStatusIfChangeFields");
        PrMemberKpiSysActionService.getInstance().changeKpiStatusIfChangeFields(
            this, currentStatusCode, newStatusCode, fieldNames);
        logEndAction("changeKPIStatusIfChangeFields");
    }

    @RouteSystemAction(
        description = "org.mirapolis.core.SystemMessages.updating|[: ]|" +
            "lms.core.newprocedure.ProcedureMessage.goal_states",
        paramsDescription = {
            RouteSystemActionProcessor.CURRENT_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            RouteSystemActionProcessor.NEW_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]"})
    public void changeKPIState(String currentStateCode, String newStateCode) {
        logStartAction("changeKPIState");
        PrMemberKpiSysActionService.getInstance().changeKpiState(this, currentStateCode, newStateCode);
        logEndAction("changeKPIState");
    }

    @RouteSystemAction(
        description = "org.mirapolis.core.SystemMessages.updating|[: ]|" +
            "lms.core.newprocedure.ProcedureMessage.control_points_states",
        paramsDescription = {
            RouteSystemActionProcessor.CURRENT_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            RouteSystemActionProcessor.NEW_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]"})
    public void changePointState(String currentStateCode, String newStateCode) {
        logStartAction("changePointState");
        PrMemberKpiSysActionService.getInstance().changeControlPointState(this, currentStateCode, newStateCode);
        logEndAction("changePointState");
    }

    /**
     * Изменить статусы показателей, находящихся в статусе currentStatusCode,
     * если были изменены поля fieldNames после присвоения этого статуса
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_control_points_status",
        paramsDescription = {
            RouteSystemActionProcessor.CURRENT_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            RouteSystemActionProcessor.NEW_VALUE_DESC + "|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            POINT_FIELD_NAME_DB_DESC})
    public void changePointStatusIfChangeFields(String currentStatusCode, String newStatusCode, Set<String> fieldNames) {
        logStartAction("changePointStatusIfChangeFields");
        PrMemberKpiSysActionService.getInstance().changePointStatusIfChangeFields(
            this, currentStatusCode, newStatusCode, fieldNames);
        logEndAction("changePointStatusIfChangeFields");
    }

	@RouteSystemAction(
	    description = "lms.core.newprocedure.ProcedureMessage.changing_kpi_status",
		paramsDescription = {
	        "lms.core.person.PersonMessage.status|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]",
            "lms.core.person.PersonMessage.status|[ (]|" + RouteSystemActionProcessor.CODE_DESC + "|[)]"})
	public void changeKPIStatusByPastDate(String statusCodeIfMore, String statusCodeIfLess, int days) {
        logStartAction("changeKPIStatusByPastDate");
		PrMemberKpiSysActionService.getInstance().changeKpiStatusByPastDate(
		    this, statusCodeIfMore, statusCodeIfLess, days);
		logEndAction("changeKPIStatusByPastDate");
	}

    /**
     * На текущем этапе добавлены показатели
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.current_stage_add_kpi")
    public boolean isAddKPIOnCurrentPhase() {
        logStartAction("isAddKPIOnCurrentPhase");
        ObjPhaseBean objPhase = getCurrentObjPhase();
        List<PRMemberKPIBean> kpiList = PRMemberKPIService.getInstance().getActiveMemberKPIs(getId());
		List<PRMemberKPIBean> createdKPIs = PRMemberKPIService.getInstance().getCreatedKPIOnPhase(kpiList, objPhase);
        boolean result = !createdKPIs.isEmpty();
        logEndAction("isAddKPIOnCurrentPhase " + result);
        return result;
    }

    /**
     * ДЛя карты есть анкеты по категории
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.checking_questionnaires_by_category",
        paramsDescription = {CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public boolean isExistSurveyByCategory(String categoryValue, String categoryCode) {
        //Анкеты по категории
        return !getCategorySurveys(categoryValue, categoryCode).isEmpty();
    }

    @RouteSystemAction
    @Deprecated
    public boolean checkManualKPIResult(RouteCommandProcessor processor) {
        return new EstimatorSurveyFormScoreSaver(this, processor).checkManualKpiResult();
    }

    /**
     * Дерево показателей по типу подразделения
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.goals_division",
         paramsDescription = {"", "lms.core.ca.CAMessage.type_division"})
    public void viewKPITree(RouteCommandProcessor processor, String typeDivisionId) {
        if (StringHelper.isNotEmpty(typeDivisionId)) {
            processor.addActionClientElement(new OpenModalActionClientElement(
                    PRMemberKPIByTypeDivisionFrame.getGoAction(getId(), typeDivisionId)));
        } else {
            processor.appendError("Empty typeDivisionId");
        }
    }

    /**
     * Проверка заполненности полей контрольных точек
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkControlPointFields(RouteCommandProcessor processor, Set<String> cpFieldNames) {
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(processor, false);
        Set<String> fields = new HashSet<>();
        cpFieldNames.forEach(fieldName ->
            ProcedureKPIFieldStore.getInstance().findControlPointFieldByMemberField(fieldName)
                .ifPresent(pointField -> fields.add(pointField.getFieldName())));
        validator.checkEmptyFieldsAllPoints(fields);
        return validateKPIValues(validator);
    }

    /**
     * Проверка заполненности полей контрольных точек, даты которых меньше текущей
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkPastControlPointFields(RouteCommandProcessor processor, Set<String> cpFieldNames) {
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(processor, false);
        Set<String> fields = new HashSet<>();
        cpFieldNames.forEach(fieldName ->
            ProcedureKPIFieldStore.getInstance().findControlPointFieldByMemberField(fieldName)
                .ifPresent(pointField -> fields.add(pointField.getFieldName())));
        validator.checkEmptyFieldsPastPoints(fields);
        return validateKPIValues(validator);
    }

    /**
     * Проверка заполненности полей показателей и контрольных точек
     * @param beforeNow проверять показатели и точки, даты которых меньше текущей. false  проверять все
     */
    @RouteSystemAction(actionTypes = {RouteSystemActionType.button})
    @Deprecated
    public boolean checkKPIAndControlPointFields(RouteCommandProcessor processor,
                                                 String kpiCategoryCode,
                                                 Set<String> kpiFieldNames,
                                                 Set<String> cpFieldNames,
                                                 boolean beforeNow) {
        if (!checkActionForButton(processor, "checkKPIAndControlPointFields")) {
            return false;
        }
        Set<String> kpiFields = new HashSet<>();
        kpiFieldNames.forEach(fieldName ->
            ProcedureKPIFieldStore.getInstance().findKpiFieldByMemberField(fieldName)
                .ifPresent(kpiField -> kpiFields.add(kpiField.getFieldName())));

        Set<String> pointFields = new HashSet<>();
        cpFieldNames.forEach(fieldName ->
            ProcedureKPIFieldStore.getInstance().findControlPointFieldByMemberField(fieldName)
                .ifPresent(pointField -> pointFields.add(pointField.getFieldName())));

        PRMemberKPIValidator kpiValidator = createKPIValidatorWithoutSave(processor, false);

        if (StringHelper.isNotEmpty(kpiCategoryCode)) {
            Set<String> kpiCategoryCodes = new HashSet<String>() {{add(kpiCategoryCode);}};
            if (beforeNow) {
                kpiValidator.checkEmptyFieldsPastKPIsByCategories(kpiFields, kpiCategoryCodes);
                kpiValidator.checkEmptyFieldsPastPointsByKPICategory(pointFields, kpiCategoryCodes);
            } else {
                kpiValidator.checkEmptyFieldsByCategories(kpiFields, kpiCategoryCodes);
                kpiValidator.checkEmptyFieldsPointsByKPICategory(pointFields, kpiCategoryCodes);
            }
        } else {
            kpiValidator.checkEmptyFieldsByAllKPIs(kpiFields);
            if (beforeNow) {
                kpiValidator.checkEmptyFieldsPastKPIs(kpiFields);
                kpiValidator.checkEmptyFieldsPastPoints(pointFields);
            } else {
                kpiValidator.checkEmptyFieldsByAllKPIs(kpiFields);
                kpiValidator.checkEmptyFieldsAllPoints(pointFields);
            }
        }
        return validateKPIValues(kpiValidator);
    }

	@RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.confirm_control_points")
	public void confirmPastControlPoints() {
        logStartAction("confirmPastControlPoints");
		PrMemberKpiSysActionService.getInstance().confirmPastControlPoints(this);
        logEndAction("confirmPastControlPoints");
	}

	@RouteSystemAction(
	    description = "lms.core.newprocedure.ProcedureMessage.change_confirm_availability_linking_kpi",
        paramsDescription = {"org.mirapolis.core.SystemMessages.value"})
	public void setConfirmationLink(String value) {
        logStartAction("setConfirmationLink");
		PrMemberKpiSysActionService.getInstance().setKpiConfirmationLink(this, value);
        logEndAction("setConfirmationLink");
	}

	@RouteSystemAction()
    public ProcedureRouteSurveyCreator getSurveyCreator(RouteCommandProcessor processor) {
        return new ProcedureRouteSurveyCreator(this, processor);
    }

    @Deprecated
    public void addSurveys(RouteCommandProcessor processor,
                           String categoryValue,
                           String additionalCategoryId,
                           String sourceCategoryValue,
                           String sourceAdditionalCategoryId) {
        getSurveyCreator(processor).addByAutoAssignSourceCategory(
                categoryValue, additionalCategoryId, sourceCategoryValue, sourceAdditionalCategoryId);
    }

	@RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.check_link_recommendations_qua_kpi")
	public boolean checkRecommendationQuaKPILink(RouteCommandProcessor processor) {
        logStartAction("checkRecommendationQuaKPILink");
		Set<String> methodNames = BeanHelper.getValueSet(
		        PRMemberDevelopmentService.getInstance().getMemberDevelopmentMethodQua(getId()),
                PRMemberDevelopmentMethodBean.NAME);
		if (!methodNames.isEmpty()) {
            processor.appendLocalizedError(Localized.format(
                    ProcedureMessage.recommendation_without_competences_kpi, StringHelper.joinWithComa(methodNames)));
            processor.addActionClientElement(EstimatorDevelopmentMethodErrorType.emptyQuaKPI.getClientAction());
		}
		logEndAction("checkRecommendationQuaKPILink");
		return methodNames.isEmpty();
	}

    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.fill_survey_interview_by_source")
    public void updateInterviewScoresBySource() {
        logStartAction("updateInterviewScoresBySource");
        PRCategoryService.getInstance().fillAndSaveInterviewSurveyScoreBySource(this, log);
        logEndAction("updateInterviewScoresBySource");
    }

    /**
     * Проверка рекомедуемого кол-ва анкет для карты по категории
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.checking_recommended_number",
        paramsDescription = {"", CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public boolean checkCategoryRecommendedCount(RouteCommandProcessor processor,
                                                 String categoryValue,
                                                 String categoryCode) {
        return processWithLogicError(
            "checkCategoryRecommendedCount",
            processor,
            v -> {
                Optional<PRCategoryBean> categoryOptional = getCategoryOptional(categoryValue, categoryCode);
                categoryOptional.ifPresent(category -> {
                    Map<String, List<PRSurveyBean>> surveys = PRSurveyService.getInstance().getMembersActiveSurveys(
                        Collections.singletonList(getId()));
                    Map<String, List<PRSurveyCandidateBean>> surveyCandidates = PRSurveyCandidateService.getInstance()
                        .getSurveyCandidateByMemberIds(Collections.singletonList(getId()));
                    PRSurveyService.getInstance().checkRecommendedSurveyCountByCategory(
                        Collections.singletonList(category),
                        Collections.singletonList(this),
                        surveys,
                        surveyCandidates);
                });
            });
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.checking_recommended_number",
        paramsDescription = {"", CATEGORY_DESC, RouteCommandProcessor.CODE_DESC})
    public boolean checkCategoryMinRecommendedCountIfExist(RouteCommandProcessor processor,
                                                           String categoryValue,
                                                           String categoryCode) {
        logStartAction("checkCategoryMinRecommendedCountIfExist: " +
                "categoryValue = " + categoryValue + ", categoryCode = " + categoryCode);
        Optional<PRCategoryBean> categoryOptional = getCategoryOptional(categoryValue, categoryCode);
        boolean error = categoryOptional.isPresent() &&
                !PRSurveyService.getInstance().checkMinRecommendedSurveyCountIfExist(getId(), categoryOptional.get());
        if (error) {
            processor.appendLocalizedError(Localized.format(ProcedureMessage.number_survey_category_less,
                    categoryOptional.get().getCategoryName(),
                    Integer.toString(categoryOptional.get().getSelfAssignment().getMin())));
        }
        logEndAction("checkCategoryMinRecommendedCountIfExist: " +
                "categoryValue = " + categoryValue + ", categoryCode = " + categoryCode);
        return !error;
    }

    /**
     * Проверка рекомедуемого кол-ва анкет для карты по всем категориям
     */
    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.checking_recommended_number")
    public boolean checkRecommendedCount(RouteCommandProcessor processor) {
        return processWithLogicError(
                "checkRecommendedCount",
                processor,
                v -> PRSurveyService.getInstance().checkRecommendedSurveyCount(
                        getProcedure().getId(), Collections.singletonList(this)));
    }

    /**
     * Проверка рекомедуемого кол-ва анкет для карты по категории
     * Присваивается параметр, если анкеты созданы
     */
    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.checking_recommended_number",
        paramsDescription = {
            "", RouteSystemActionProcessor.PARAM_NAME_DESC, RouteSystemActionProcessor.PARAM_VALUE_DESC, CATEGORIES_DESC})
    public boolean checkRecommendedCountAndCreateSurveys(RouteCommandProcessor processor,
                                                         String paramName,
                                                         String paramValue,
                                                         String ... categoryValues) {
        logStartAction("checkRecommendedCountAndCreateSurveys");
        List<ProcedureEstimatingCategory> estimatingCategories = getCategories(categoryValues);
        List<PRCategoryBean> categories = PRCategoryService.getInstance().getProcedureCategories(
                getProcedure().getId(), estimatingCategories);
        Map<String, List<PRSurveyBean>> surveys = PRSurveyService.getInstance().getMembersActiveSurveys(
                Collections.singletonList(getId()));
        Map<String, List<PRSurveyCandidateBean>> surveyCandidates =
                PRSurveyCandidateService.getInstance().getSurveyCandidateByMemberIds(Collections.singletonList(getId()));
        try {
            PRSurveyService.getInstance().checkRecommendedSurveyCountByCategory(
                    categories, Collections.singletonList(this), surveys, surveyCandidates);
            if (surveyCandidates.containsKey(getId())) {//Есть анкеты-претенденты для карты
                List<PRSurveyCandidateBean> memberSurveyCandidates = surveyCandidates.get(getId());
                List<PRSurveyCandidateBean> categorySurveyCandidates =
                        PRSurveyCandidateService.getInstance().filterSurveyCandidateByCategories(
                                memberSurveyCandidates, estimatingCategories);
                if (!categorySurveyCandidates.isEmpty()) {
                    PRSurveyCandidateService.getInstance().saveSurveysCandidates(this, categorySurveyCandidates);
                }
            }
            if (StringHelper.isAllNotEmpty(paramName, paramValue)) {
                processor.getParams().put(paramName, paramValue);
            }
            logEndAction("checkRecommendedCountAndCreateSurveys");
            return true;
        } catch (LogicErrorException e) {
            processor.addActionClientElement(new ConfirmActionClientElement(
                    Localized.valueOf(e.getMessage()),
                    new PRSurveyCreateFromCandidateAction(
                            getId(),  processor.getUserId(), paramName, paramValue, categoryValues)
                            .getActionClientElement()));
            logEndAction("checkRecommendedCountAndCreateSurveys");
            return false;
        }
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.add_estimated_version",
        paramsDescription = {"", "lms.core.newprocedure.ProcedureMessage.version_name"})
    public void createVersion(RouteCommandProcessor processor, String versionName) {
        logStartAction("createVersion");
        //Этап, с которым связать версиию
        //Если действие в переходе, то этап, с которого переход, если в кнопке - текущий
        String phaseId = StringHelper.isNotEmpty(processor.getGoFromPhaseId()) ?
                processor.getGoFromPhaseId() :
                getCurrentObjPhase().getPhase().getId();
        PRMemberVersionService.getInstance().createVersion(
                getId(),
                RouteAnonymousLinkService.getInstance().getUserId(Context.get()),
                phaseId,
                versionName);
        logEndAction("createVersion");
    }

    @RouteSystemAction(paramsDescription = {"lms.core.newprocedure.ProcedureMessage.version_name"})
    public void updateLastVersionName(String versionName) {
        logStartAction("updateLastVersionName");
        PRMemberVersionService.getInstance().updateLastVersionName(getId(), versionName);
        logEndAction("updateLastVersionName");
    }

    @RouteSystemAction(paramsDescription = {"lms.core.newprocedure.ProcedureMessage.version_name"})
    public void restoreKpiFromVersion(String versionName) {
        logStartAction("restoreKpiFromVersion");
        PRMemberVersionService.getInstance().getLastVersionByName(getId(), versionName).ifPresent(
            version -> PRMemberVersionService.getInstance().restoreMemberKpiFromVersion(this, version.getId()));
        logEndAction("restoreKpiFromVersion");
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_status_confirmation_control_points",
        paramsDescription = {
            "",
            RouteCommandProcessor.CODE_DESC,
            RouteCommandProcessor.CODE_DESC,
            "[combo#lms.core.newprocedure.member.kpi.point.ChooseControlPointCondition]",
            "[combo#lms.core.newprocedure.member.kpi.point.ControlPointConfirmAction]",
            RouteCommandProcessor.CUSTOM_TEXT_CODE_PARAM_DESC,
            RouteCommandProcessor.CUSTOM_TEXT_CODE_PARAM_DESC,
            RouteSystemActionProcessor.PARAMS_DESC})
    public void changePointStatusAndConfirmation(RouteCommandProcessor processor,
                                                 String statusCode,
                                                 String newStatusCode,
												 String pointCondition,
                                                 String pointConfirmation,
                                                 String emptyPointTextCode,
                                                 String descPointTextCode,
                                                 String params) {
        logStartAction("changePointStatusAndConfirmation");
        RSBean status = RSService.getInstance().getRSBean(
                statusCode, ControlPointStatusRubricator.CONTROL_POINT_STATUS);
        RSBean newStatus = RSService.getInstance().getRSBean(
                newStatusCode, ControlPointStatusRubricator.CONTROL_POINT_STATUS);
        ChooseControlPointCondition condition = ComboValueHelper.getComboValueByValue(
                ChooseControlPointCondition.values(), pointCondition);
        ControlPointConfirmAction confirm = ComboValueHelper.getComboValueByValue(
                ControlPointConfirmAction.values(), pointConfirmation);
        if (status == null || newStatus == null || condition == null || confirm == null) {
            processor.appendError(RouteMessage.incorrect_action_parameters.toString());
            return;
        }
		if (PrMemberKpiSysActionService.getInstance()
            .getControlPointsForChangeStatus(condition, getId(), status.getId()).isEmpty()) {
			processor.appendError(UserTextRubricator.getLocalizedTextByCode(emptyPointTextCode));
		} else {
		    processor.addActionClientElement(
		        new PrMemberControlPointChangeStatusGoAction(
		            getId(),
                    status.getId(),
                    newStatus.getId(),
                    processor.getPhaseSource().getId(),
                    getSurveyIdParam(),
                    descPointTextCode,
                    pointCondition,
                    pointConfirmation,
                    params)
                    .getActionClientElement());
		}
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_status_control_points",
        paramsDescription = {"", RouteCommandProcessor.CODE_DESC, RouteCommandProcessor.CODE_DESC})
    public void changePointStatus(RouteCommandProcessor processor, String statusCode, String newStatusCode) {
        logStartAction("changePointStatus");
        PrMemberKpiSysActionService.getInstance().changePointStatus(this, processor, statusCode, newStatusCode);
        logEndAction("changePointStatus");
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.changing_status_kpi_old_status",
        paramsDescription = {
            "",
            RouteCommandProcessor.CODE_DESC,
            RouteCommandProcessor.CODE_DESC,
            RouteCommandProcessor.CUSTOM_TEXT_CODE_PARAM_DESC,
            RouteCommandProcessor.CUSTOM_TEXT_CODE_PARAM_DESC,
            KPI_FIELD_NAME_DB_DESC,
            POINT_FIELD_NAME_DB_DESC,
            RouteSystemActionProcessor.PARAMS_DESC})
    public void changeKPIStatusInNewWindow(RouteCommandProcessor processor,
                                           String statusCode,
                                           String newStatusCode,
                                           String emptyKPITextCode,
                                           String descKPITextCode,
                                           Set<String> kpiRequiredFields,
                                           Set<String> pointRequiredFields,
                                           String params) {
        logStartAction("changeKPIStatusInNewWindow");
        RSBean status = RSService.getInstance().getRSBean(statusCode, GoalStatusRubricator.GOAL_STATUS);
        RSBean newStatus = RSService.getInstance().getRSBean(newStatusCode, GoalStatusRubricator.GOAL_STATUS);
        if (status == null || newStatus == null) {
            processor.appendError(RouteMessage.incorrect_action_parameters.toString());
            return;
        }
		if (PrMemberKpiSysActionService.getInstance().getKpisForChangeStatus(
		    getId(), status.getId(), kpiRequiredFields, pointRequiredFields).isEmpty()) {
			processor.appendError(UserTextRubricator.getLocalizedTextByCode(emptyKPITextCode));
		} else {
			processor.addActionClientElement(
			    new PrMemberKpiChangeStatusGoAction(
			        getId(),
                    status.getId(),
                    newStatus.getId(),
                    processor.getPhaseSource().getId(),
                    getSurveyIdParam(),
                    descKPITextCode,
                    kpiRequiredFields,
                    pointRequiredFields,
                    params)
                    .getActionClientElement());
		}
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.check_filling_estimating_category_kpi_score")
    public boolean checkEstimatingCategoryScoreByKPI(RouteCommandProcessor processor) {
        logStartAction("checkEstimatingCategoryScoreByKPI");
        boolean allScores = PrMemberKpiSysActionService.getInstance().checkEstimatingCategoryScoreByKpi(this);
        if (!allScores) {
            processor.appendError(ProcedureSysText.procedure_check_kpi_estimating_category_score.getText());
        }
        return allScores;
    }

	@RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.launching_subordinates")
	public void launchSubordinates() {
        logStartAction("launchSubordinates");
		ProcedureBean procedure = ProcedureService.getProcedure(getProcedure().getId());
		List<PRMemberBean> members = procedure.getKpi().getSubordinationType().getInferiorMembers(this);
        MemberCardLauncher.getInstance().throwLogicErrorException(
                MemberCardLauncher.getInstance().launchCards(members));
		logEndAction("launchSubordinates");
	}

    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.registration_start_assessment_procedure")
    public void addAndLaunchEstimated() {
        logStartAction("addAndLaunchEstimated");
        PersonBean person = PersonService.getPersonBean(getPerson().getId());
        PRMemberRegisterBean settings = PRMemberRegistrator.getInstance().getCreateOtherCardRegistrationSettings();
        PRMemberRegistrator.getInstance().autoRegistration(
                getProcedure().getId(),
                Collections.singletonList(person),
                settings,
                PrMemberAfterRegistrationAction.launch_error_in_exception);
        logStartAction("addAndLaunchEstimated");
    }

	@RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.recalculation_procedure_results")
	public void recalculateProcedureResult() {
        logStartAction("recalculateProcedureResult");
		new MemberProcedureResultCalculator(this).calculate();
		logEndAction("recalculateProcedureResult");
	}

    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.result_kpi_profile")
    public void recalculateKPIProfileResult() {
        logStartAction("recalculateKPIProfileResult");
        PRMemberKPIService.getInstance().recalculateKPIProfileResult(this);
        logEndAction("recalculateKPIProfileResult");
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.change_importance_competence",
        paramsDescription = {"", "hr.candidate.CandidateMessage.codes"})
    public void changeCompetenceImportance(RouteCommandProcessor processor, Set<String> categoryCodes) {
        logStartAction("changeCompetenceImportance");
        List<String> categoryIds = getQuaCategoryIdsByCodes(categoryCodes);
        if (categoryIds.isEmpty()) {
            processor.appendError(ProcedureMessage.not_defined + ": " + PersonMessage.categories);
        } else {
            processor.addActionClientElement(new OpenModalActionClientElement(
                    ChangePRMemberQuaImportanceFrame.getGoAction(getId(), categoryIds)));
        }
        logEndAction("changeCompetenceImportance");
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.checking_competence_total_importance",
        paramsDescription = {"", RouteSystemActionProcessor.CODE_DESC + "|[ - ]|lms.core.account.AccountMessage.amount"})
    public boolean checkCompetenceSumImportance(RouteCommandProcessor processor,
                                                Map<String, Integer> categorySumImportance) {
        logStartAction("checkCompetenceSumImportance");
        List<String> categoryIds = getQuaCategoryIdsByCodes(categorySumImportance.keySet());
        if (categoryIds.isEmpty()) {
            processor.appendError(ProcedureMessage.not_defined + ": " + PersonMessage.categories);
        }
        boolean result = !categoryIds.isEmpty() &&
                getErrorCompetenceSumImportanceByCategory(categoryIds, categorySumImportance).isEmpty();
        logEndAction("checkCompetenceSumImportance " + result);
        return result;
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.checking_competence_total_importance|[, ]|" +
            "lms.core.newprocedure.ProcedureMessage.change_importance_competence",
        paramsDescription = {
            "",
            RouteSystemActionProcessor.CODE_DESC + "|[ - ]|lms.core.account.AccountMessage.amount",
            RouteSystemActionProcessor.PARAM_NAME_DESC,
            RouteSystemActionProcessor.PARAM_VALUE_DESC})
    public boolean checkAndChangeCompetenceImportance(RouteCommandProcessor processor,
                                                      Map<String, Integer> categorySumImportance,
                                                      String paramName,
                                                      String paramValue) {
        logStartAction("checkAndChangeCompetenceImportance");
        List<String> categoryIds = getQuaCategoryIdsByCodes(categorySumImportance.keySet());
        if (categoryIds.isEmpty()) {
            processor.appendError(ProcedureMessage.not_defined + ": " + PersonMessage.categories);
            return false;
        }
        Map<String, Integer> errorImportance = getErrorCompetenceSumImportanceByCategory(
                categoryIds, categorySumImportance);
        if (!errorImportance.isEmpty()) {
            processor.addActionClientElement(new OpenModalActionClientElement(ChangePRMemberQuaImportanceFrame.getGoAction(
                    getId(), errorImportance, paramName, paramValue)));
        }
        logEndAction("checkAndChangeCompetenceImportance");
        return errorImportance.isEmpty();
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.check_max_number_competencies_by_categories|[. ]|" +
            "[sys_text_keyword#categories, max]")
    public boolean checkCompetenceCountByCategory(RouteCommandProcessor processor) {
        return processWithLogicError(
                "checkCompetenceCountByCategory",
                processor,
                v -> PrQuaCategoryQuaCountService.getInstance().checkCompetenceCountByCategory(this));
    }

    @RouteSystemAction(
        paramsDescription = {"", "[user_text_keyword#" + PrQuaProfileResultLimitService.KEYWORDS + "]"},
        actionTypes = {RouteSystemActionType.button})
    public boolean checkQuaProfileResultLimits(RouteCommandProcessor processor, String textCode) {
        return processWithLogicError(
                "checkQuaProfileResultLimits",
                processor,
                v -> PrQuaProfileResultLimitService.getInstance().checkQuaProfileResultLimits(this, textCode));
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.amount_kpi_parameter_values",
        paramsDescription = {
            "lms.core.person.PersonMessage.field",
            "hr.candidate.CandidateMessage.codes|[ (]|lms.core.newprocedure.ProcedureMessage.category_kpi|[)]"})
    public Double getFloatSumKPIField(String field, Set<String> kpiCategoryCodes) {
        logStartAction("getFloatSumKPIField");
        Double sum = PrMemberKpiSysActionService.getInstance().getKpiFloatSumField(this, field, kpiCategoryCodes);
        logEndAction("getFloatSumKPIField");
        return sum;
    }

    @RouteSystemAction(
        description = "lms.core.newprocedure.ProcedureMessage.amount_kpi_parameter_values",
        paramsDescription = {
            "lms.core.person.PersonMessage.field",
            "hr.candidate.CandidateMessage.codes|[ (]|lms.core.newprocedure.ProcedureMessage.category_kpi|[)]"})
    public Integer getIntegerSumKPIField(String field, Set<String> kpiCategoryCodes) {
        logStartAction("getIntegerSumKPIField");
        Integer sum = PrMemberKpiSysActionService.getInstance().getKpiIntegerSumField(
            this, field, kpiCategoryCodes);
        logEndAction("getIntegerSumKPIField");
        return sum;
    }

    @RouteSystemAction(description = "lms.core.newprocedure.ProcedureMessage.change_by_person")
    public void updateByPerson() {
        logStartAction("updateByPerson");
		PrMemberPersonDataService.getInstance().updateMemberByPerson(this);
		logEndAction("updateByPerson");
	}

	@RouteSystemAction(
	    description = "lms.core.newprocedure.ProcedureMessage.checking_number_methods_development_by_kind",
		paramsDescription = {
	        "",
            "[combo#hr.development.DevelopmentKind]",
            "lms.core.newprocedure.ProcedureMessage.minimum",
            RouteCommandProcessor.CUSTOM_TEXT_CODE_PARAM_DESC,
            "lms.core.newprocedure.ProcedureMessage.maximum",
            RouteCommandProcessor.CUSTOM_TEXT_CODE_PARAM_DESC})
	public boolean checkRecommendationCount(RouteCommandProcessor processor,
                                            String kindValue,
                                            Integer min,
                                            String minTextCode,
                                            Integer max,
                                            String maxTextCode) {
        logStartAction("checkRecommendationCount");
		DevelopmentKind kind = ComboValueHelper.getComboValueByValue(DevelopmentKind.values(), kindValue);
		if (kind == null) {
			processor.appendError("Error param kindValue = " + kindValue);
			return false;
		}
		List<PRMemberDevelopmentMethodBean> methods =
                PRMemberDevelopmentService.getInstance().listMemberDevelopmentMethods(getId());
		int count = 0;
		for (PRMemberDevelopmentMethodBean method : methods) {
			if (kind.equals(method.getType())) {
				count ++;
			}
		}
		boolean error = false;
		if (IntHelper.isNotNull(min) && count < min) {
			processor.appendErrorByCode(minTextCode);
			error = true;
		}
		if (IntHelper.isNotNull(max) && count > max) {
			processor.appendErrorByCode(maxTextCode);
			error = true;
		}
		logEndAction("checkRecommendationCount");
		return !error;
	}

    /**
     * Проверка ограничений значений показателей по заданным полям заданных категорий
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkKPIValueLimitByCategoryAndFields(RouteCommandProcessor processor,
                                                         Map<String, Set<String>> fieldsByCategory,
                                                         boolean errorInFields,
                                                         String textCode) {
        List<PRMemberKPIBean> memberKPIs = getMemberKPIsByCategoryCodesForCheckValues(
                fieldsByCategory.keySet(), getProcedureBean());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkValueLimitByCategoriesAndFields(fieldsByCategory, textCode);
        return !validator.isErrors();
    }

    /**
     * Проверка ограничений значений показателей по всем полям заданных категорий
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkKPIValueLimitByCategory(RouteCommandProcessor processor,
                                                Set<String> categoryCodes,
                                                boolean errorInFields,
                                                String textCode) {
        List<PRMemberKPIBean> memberKPIs = getMemberKPIsByCategoryCodesForCheckValues(categoryCodes, getProcedureBean());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkValueLimitByCategories(categoryCodes, textCode);
        return !validator.isErrors();
    }

    /**
     * Проверка ограничений значений всех показателей по заданным полям
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkKPIValueLimitByFields(RouteCommandProcessor processor,
                                              Set<String> fieldNames,
                                              boolean errorInFields,
                                              String textCode) {
        List<PRMemberKPIBean> memberKPIs = PRMemberKPIService.getInstance().getActiveMemberKPIForCheckInSurvey(
                getId(), getSurveyIdParam(), getProcedureBean());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkValueLimitByFields(fieldNames, textCode);
        return !validator.isErrors();
    }

    /**
     * Проверка ограничений значений всех показателей по всем полям
     */
    @RouteSystemAction()
    @Deprecated
    public boolean checkKPIValueLimit(RouteCommandProcessor processor, boolean errorInFields, String textCode) {
        List<PRMemberKPIBean> memberKPIs = PRMemberKPIService.getInstance().getActiveMemberKPIForCheckInSurvey(
                getId(), getSurveyIdParam(), getProcedureBean());
        PRMemberKPIValidator validator = createKPIValidatorWithoutSave(memberKPIs, processor, errorInFields);
        validator.checkValueLimitByAllKPIs(textCode);
        return !validator.isErrors();
    }

    private PRMemberKPIValidator createKPIValidatorWithoutSave(List<PRMemberKPIBean> memberKPIs,
                                                               RouteCommandProcessor processor,
                                                               boolean errorInFields) {
        PRMemberKPIValidator validator = new PRMemberKPIValidator(this, getSurveyIdParam(), processor);
        if (!errorInFields) {
            validator.setErrorNotInFields();
        }
        validator.setValidate(memberKPIs);
        return validator;
    }

    @RouteSystemAction(paramsDescription = {"", RouteSystemActionProcessor.TRUE_FALSE_DESC})
    public PRMemberKPIValidator createKPIValidatorWithoutSave(RouteCommandProcessor processor, boolean errorInFields) {
        PRMemberKPIValidator validator = new PRMemberKPIValidator(this, getSurveyIdParam(), processor);
        if (!errorInFields) {
            validator.setErrorNotInFields();
        }
        validator.setValidate();
        return validator;
    }

    @RouteSystemAction(paramsDescription = {"", RouteSystemActionProcessor.TRUE_FALSE_DESC})
    public PRMemberKPIValidator createKPIValidatorWithSave(RouteCommandProcessor processor, boolean errorInFields) {
        PRMemberKPIValidator validator = new PRMemberKPIValidator(this, getSurveyIdParam(), processor);
        if (!errorInFields) {
            validator.setErrorNotInFields();
        }
        validator.setValidateAndSave(
                Context.get().getRequestData(), RouteAnonymousLinkService.getInstance().getUserId(Context.get()));
        return validator;
    }

    @RouteSystemAction()
    public boolean validateKPIValues(PRMemberKPIValidator kpiValidator) {
        return !kpiValidator.isErrors();
    }

    @RouteSystemAction
    @Deprecated
    public boolean checkOtherMemberRouteParam(RouteCommandProcessor processor,
                                              String categoryValue,
                                              String categoryCode,
                                              String paramName,
                                              String paramValue,
                                              String textCode) {
        return ProcedureSysActionService.getInstance().checkOtherMemberRouteParam(
            processor, categoryValue, categoryCode, paramName, paramValue, textCode);
    }

    @RouteSystemAction
    @Deprecated
    public boolean isFiltered(String filterCode) {
        return ProcedureSysActionService.getInstance().isFiltered(this, filterCode);
    }

    @RouteSystemAction
    @Deprecated
    public boolean isSurveyFiltered(String filterCode) {
        return ProcedureSysActionService.getInstance().isSurveyFiltered(this, filterCode);
    }

    @RouteSystemAction()
    public void buildTaskReport(String taskId) {
        logStartAction("buildTaskReport");
        ProcedureTaskService.getInstance().runBuilder(
            taskId, task -> new ProcedureReportBuilder(task, new IndividualByMemberIdReportDataCreator(getId(), task)));
    }

    @RouteSystemAction
    public void assignCompetence() {
        logStartAction("assignCompetence");
        ProcedureAssignPersonAttributeService.getInstance().assignQua(getId());
        logEndAction("assignCompetence");
    }

    private List<PRMemberKPIBean> getMemberKPIsByCategoryCodesForCheckValues(Set<String> kpiCategoryCodes,
                                                                             ProcedureBean procedure) {
        List<PRMemberKPIBean> memberKPIs =
            PRMemberKPIService.getInstance().getActiveMemberKpiByCategoryCodes(getId(), kpiCategoryCodes);
        PRMemberKPIService.getInstance().updateMemberKPIGroupCategory(memberKPIs, procedure);
        return memberKPIs;
    }

    private List<String> getQuaCategoryIdsByCodes(Collection<String> categoryCodes) {
        List<String> categoryIds = new ArrayList<>();
        for (String categoryCode : categoryCodes) {
            RSBean rs = RSService.getInstance().getRSBean(categoryCode, QuaCategoryRubImpl.QUA_CATEGORY);
            if (rs != null) {
                categoryIds.add(rs.getId());
            }
        }
        return categoryIds;
    }

    /**
     * Категории компетенций, для которых суммарная важность не равна заданной
     * @return id категории - суммарная важность
     */
    private Map<String, Integer> getErrorCompetenceSumImportanceByCategory(List<String> categoryIds,
                                                                           Map<String, Integer> categorySumImportance) {
        List<PRMemberQuaBean> memberQuaBeans = PRMemberService.getInstance().getMemberQuaByCategories(getId(), categoryIds);
        Map<String, List<PRMemberQuaBean>> memberQuaByCategory = BeanHelper.createMapFromListByLookup(
                memberQuaBeans, PRMemberQuaBean.QUA_CATEGORY);
        Map<String, Integer> errorImportance = new HashMap<>();
        for (String categoryCode : categorySumImportance.keySet()) {
            Integer sumImp = categorySumImportance.get(categoryCode);
            RSBean rs = RSService.getInstance().getRSBean(categoryCode, QuaCategoryRubImpl.QUA_CATEGORY);
            if (rs != null && memberQuaByCategory.containsKey(rs.getId())) {
                List<PRMemberQuaBean> beans = memberQuaByCategory.get(rs.getId());
                int categorySumImp = ProcedureService.calculateSumImp(beans);
                if (categorySumImp != sumImp) {
                    errorImportance.put(rs.getId(), sumImp);
                }
            }
        }
        return errorImportance;
    }

    /**
     * Изменение текущей анкеты
     */
    private void updateSurvey(String action, RouteCommandProcessor processor, Consumer<List<PRSurveyBean>> surveyUpdater) {
        logStartAction(action);
        if (checkActionForButton(processor, action)) {
            surveyUpdater.accept(Collections.singletonList(PRSurveyService.getSurvey(getSurveyIdParam())));
        }
        logEndAction(action);
    }

    private boolean checkActionForButtonWithDefaultError(RouteCommandProcessor processor, String action) {
        return checkActionForButton(
                processor, action, ProcedureMessage.error_save_survey_scores.toString());
    }

    private boolean checkActionForButton(RouteCommandProcessor processor, String action) {
        return checkActionForButton(
                processor, action, "Call system action " + action + " with empty surveyId");
    }

    private boolean checkActionForButton(RouteCommandProcessor processor, String action, String errorSurveyMessage) {
        processor.checkActionType(this, action);
        if (StringHelper.isEmpty(getSurveyIdParam())) {
            Application.log.warn("Call system action with empty surveyId");
            processor.appendError(errorSurveyMessage);
            return false;
        }
        return true;
    }

    public String getSurveyIdParam() {
        return Context.get().getDataParameter(PRSurveyBean.ID);
    }

    public ProcedureEstimatingCategory getCategory(String value) {
        return ProcedureEstimatingCategoryStore.getInstance().getCategory(value);
    }

    public List<ProcedureEstimatingCategory> getCategories(String ... categoryValues) {
        List<ProcedureEstimatingCategory> categories = new ArrayList<>();
        for (String value : categoryValues) {
            if (ProcedureEstimatingCategoryStore.getInstance().isExistCategory(value)) {
                categories.add(ProcedureEstimatingCategoryStore.getInstance().getCategory(value));
            }
        }
        return categories;
    }

    public List<PRSurveyBean> getCategorySurveys(String categoryValue, String additionalValue) {
        ProcedureEstimatingCategory category = getCategory(categoryValue);
        if (category == null) {
            return Collections.emptyList();
        }
        if (EstimatingCategory.ADDITIONAL.isEquals(category) && StringHelper.isNotEmpty(additionalValue)) {
            RSBean rs = RSService.getInstance().getRSBean(additionalValue, ProcedureCategoryRubricator.CATEGORY);
            if (rs == null) {
                //Дополнительная категория по коду не найдена
                return Collections.emptyList();
            }
            additionalValue = rs.getId();
        }
        return StringHelper.isEmpty(additionalValue) ?
                PRSurveyService.getInstance().getMemberActiveSurveyByCategory(getId(), category) :
                PRSurveyService.getInstance().getMemberActiveSurveyByCategory(getId(), category, additionalValue);
    }

    public Optional<PRCategoryBean> getCategoryOptional(String categoryValue, String categoryCode) {
        ProcedureEstimatingCategory category = getCategory(categoryValue);
        if (EstimatingCategory.ADDITIONAL.isEquals(category)) {
            if (StringHelper.isNotEmpty(categoryCode)) {
                 return RSService.getInstance().getRSByCode(categoryCode, ProcedureCategoryRubricator.CATEGORY)
                     .flatMap(rs -> PRCategoryService.getInstance().getCategoryByMember(getId(), category, rs.getId()));
            } else {
                List<PRCategoryBean> categories = getProcedureBean().getCategories().stream()
                    .filter(bean -> bean.getCategory().isEquals(category))
                    .collect(Collectors.toList());
                return categories.size() == 1 ? Optional.of(categories.get(0)) : Optional.empty();
            }
        }
        return PRCategoryService.getInstance().getCategoryByMember(getId(), category);
    }

    public ProcedureBean getProcedureBean() {
        return ProcedureService.getInstance().getProcedureWithCategories(getProcedure().getId());
    }

    private ObjRouteBean getObjRoute() {
        return RouteObjService.getInstance().mapObjRouteByIds(
                ProcedureRouteObject.TYPE, Collections.singletonList(getId())).get(getId());
    }

    public ObjPhaseBean getCurrentObjPhase() {
        return RouteObjService.getInstance().getCurrentObjPhase(ProcedureRouteObject.TYPE, getId());
    }

    public boolean processWithLogicError(String actionName, RouteCommandProcessor processor, Consumer<Void> action) {
        return ProcedureSysActionService.getInstance().processWithLogicError(
            actionName, processor, action, this::logStartAction, this::logEndAction);
    }

    private <T>T processWithLogicError(String actionName,
                                       RouteCommandProcessor processor,
                                       Supplier<T> action,
                                       T defaultValue) {
        try {
            logStartAction(actionName);
            T result = action.get();
            logEndAction(actionName);
            return result;
        } catch (LogicErrorException e) {
            processor.appendError(e.getMessage());
            logEndAction(actionName);
            return defaultValue;
        }
    }

    private void logStartAction(String action) {
        log.debug(action + " Start-----------------");
    }

    private void logEndAction(String action) {
        log.debug(action + " End-------------------");
    }

    public static class ByPointDescComparator implements Comparator<PRMemberBean> {
		public int compare(PRMemberBean bean1, PRMemberBean bean2) {
			Double a0 = bean1.getPoint();
			Double a1 = bean2.getPoint();
			return a1.compareTo(a0);
		}
	}

    public static class ByPersonNameComparator implements Comparator<PRMemberBean> {
        @Override
        public int compare(PRMemberBean bean1, PRMemberBean bean2) {
            return bean1.getPerson().getName().compareTo(bean2.getPerson().getName());
        }
    }

    public static class ByCreateDateDescComparator implements Comparator<PRMemberBean> {
        @Override
        public int compare(PRMemberBean bean1, PRMemberBean bean2) {
            return CreatorService.getInstance().getCreateDate(bean2)
                .compareTo(CreatorService.getInstance().getCreateDate(bean1));
        }
    }

    public static class ByCaPostComparator implements Comparator<PRMemberBean> {
        @Override
        public int compare(PRMemberBean bean1, PRMemberBean bean2) {
            return StringHelper.defaultIfNull(bean1.getCaPost().getName())
                .compareTo(StringHelper.defaultIfNull(bean2.getCaPost().getName()));
        }
    }
}
