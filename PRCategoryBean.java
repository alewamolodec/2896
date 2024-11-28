package lms.core.newprocedure.category;

import lms.core.newprocedure.ProcedureAssessmentBlock;
import lms.core.newprocedure.ProcedureBean;
import lms.core.newprocedure.ProcedureFrame;
import lms.core.newprocedure.ProcedureMessage;
import lms.core.newprocedure.assignment.mass.PersonForAssignEstimatorBean;
import lms.core.newprocedure.category.autoassignment.PrCategoryAutoAssignBean;
import lms.core.newprocedure.category.comment.PrCategoryComment;
import lms.core.newprocedure.category.comment.PrCategoryScoreComment;
import lms.core.newprocedure.category.comment.PrCategoryScoreCommentConditionBean;
import lms.core.newprocedure.category.comment.PrCategoryScoreCommentLengthLimit;
import lms.core.newprocedure.category.qua.PrCategoryQuaFormulaType;
import lms.core.newprocedure.category.qua.refusal.PrCategoryIndByQuaCategoryRefusalLimitBean;
import lms.core.newprocedure.category.qua.refusal.PrCategoryIndicatorRefusalLimit;
import lms.core.newprocedure.category.qua.refusal.PrCategoryIndicatorRefusalLimitUnit;
import lms.core.newprocedure.category.qua.refusal.PrCategoryQuaRefusalLimit;
import lms.core.newprocedure.formula.ProcedureFormulaType;
import lms.core.newprocedure.kind.ProcedureKind;
import lms.core.newprocedure.kind.ProcedureKindStore;
import lms.core.newprocedure.kpi.KPIRestriction;
import lms.core.newprocedure.surveycandidate.PRCategorySelfAssignmentBean;
import lms.core.newprocedure.vv.ProcedureCategoryRubricator;
import lms.core.newprocedure.vv.ProcedureTypeRubricator;
import lms.core.person.*;
import lms.core.qua.QuaMessage;
import lms.service.mail.EMailMessage;
import lms.service.poll.SurveyBean;
import lms.service.poll.SurveyFrame;
import lms.service.poll.SurveyType;
import mira.vv.VVMessage;
import mira.vv.rubricator.field.RSField;
import mira.vv.rubs.formula.FormulaFieldBuilder;
import org.mirapolis.core.Context;
import org.mirapolis.core.SystemMessages;
import org.mirapolis.data.bean.ArrayBean;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.data.bean.beanfieldconverter.Converter;
import org.mirapolis.data.bean.reflect.AbstractReflectDataBean;
import org.mirapolis.data.bean.reflect.Name;
import org.mirapolis.data.bean.reflect.ReflectDataBean;
import org.mirapolis.mvc.model.entity.ArrayFieldBuilder;
import org.mirapolis.mvc.model.entity.datafields.LookupField;
import org.mirapolis.mvc.model.entity.fields.*;
import org.mirapolis.mvc.model.entity.fields.processors.DisableTypingFieldComponentProcessor;
import org.mirapolis.mvc.model.entity.fields.processors.QuickInsertDisableFieldComponentProcessor;
import org.mirapolis.mvc.model.entity.fields.processors.SetChoosingParamFieldComponentProcessor;
import org.mirapolis.mvc.model.entity.fields.processors.SetInputWidthComponentProcessor;
import org.mirapolis.mvc.view.clientscript.builders.CheckExpressionBuilder;
import org.mirapolis.mvc.view.clientscript.builders.ComboBoxExpressionBuilder;
import org.mirapolis.mvc.view.clientscript.builders.FieldVarExpressionType;
import org.mirapolis.mvc.view.clientscript.expressions.*;
import org.mirapolis.mvc.view.clientscript.expressions.field.CheckFieldExpression;
import org.mirapolis.mvc.view.clientscript.expressions.field.ComboBoxFieldExpression;
import org.mirapolis.mvc.view.clientscript.expressions.field.FieldExpression;
import org.mirapolis.mvc.view.element.component.layout.VerticalLayout;
import org.mirapolis.orm.*;
import org.mirapolis.orm.constraint.NotEmptyBean;
import org.mirapolis.orm.fields.*;
import org.mirapolis.orm.validation.hibernate.custom.FieldErrors;
import org.mirapolis.util.CollectionUtils;
import org.mirapolis.util.IntHelper;
import org.mirapolis.util.StringHelper;

import javax.validation.constraints.AssertTrue;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mirapolis.mvc.view.clientscript.builders.EntityExpressionBuilder.getField;
import static org.mirapolis.mvc.view.clientscript.builders.FieldExpressionBuilder.*;
import static org.mirapolis.mvc.view.clientscript.expressions.ConstantExpression.bool;
import static org.mirapolis.mvc.view.clientscript.expressions.ConstantExpression.string;
import static org.mirapolis.mvc.view.clientscript.expressions.Expression.block;
import static org.mirapolis.mvc.view.clientscript.expressions.Expression.str;

/**
 * Оценивающая категория
 *
 * @author Elena Puzakova
 * @since 17.05.12 17:22
 */
public class PRCategoryBean extends ReflectDataBean implements ProcedureEstimatingCategorySupplier {
	public static final String DATANAME = "AT$PRCATEGORY";
	public static final String ALIAS = "PRC";

	private static final Integer DEFAULT_MIN_COUNT = 100;

	public static final String ID = "prcid";
	public static final String PROCEDURE_ID = ProcedureBean.ID;
	public static final String CATEGORY = "prccategory";
	public static final String IS_USE = "prcisuse";
	public static final String NOT_CANCEL_COMPLETED_SURVEY_BY_ARCHIVE_PERSON_COMPOSITE = "prcnotcancelcompletesurveys";
	private static final String NOT_CANCEL_COMPLETED_SURVEY_BY_ARCHIVE_PERSON = "prcnotcancelcomplete";
	private static final String COMPLETION_SURVEY_METHOD = "prccompletesurveymethod";
	public static final String NOT_CANCEL_FINAL_PHASE_SURVEY_BY_ARCHIVE_PERSON = "prcnotcancelfinalphase";
	public static final String NOT_CANCEL_SURVEY_BEFORE_END = "prcnotcancelbrforeend";
	public static final String CANCEL_SURVEY_ESTIMATOR_CATEGORY_ID = "canceledsurveyprcid";
	public static final String CANCEL_SURVEY_CATEGORY_ID = "addcancelsurveyprcid";
	public static final String ASSIGN_ESTIMATOR_MATERNITY_LEAVE = "prcestimatormaternityleave";
	public static final String ASSIGN_CANDIDATE_MATERNITY_LEAVE = "prccandidatematernityleave";
	//Компетенции
	public static final String IS_QUA = "prcisqua";
	public static final String IS_QUA_CATEGORY = "prcisquacategory";
	public static final String METHOD = "prcmethod";
	public static final String COMMENT_QUA = "prccommentqua";
	public static final String COMMENT_IND = "prccommentind";
	public static final String COMMENT_IND_SCORE_CONDITIONS = "commentindcondition";
	public static final String COMMENT_IND_LIMIT_LENGTH = "prccommentindlimit";
	public static final String COMMENT_IND_MIN_LENGTH = "prccommentindlength";
	public static final String STRENGTHS_WEAKNESSES_QUA = "prquastrengthsweaknesses";
	public static final String COMMENT = "prccomment";
	public static final String VIEW_RESULT_SCORE = "prcviewresultscore";
	public static final String IMPORTANCE_FROM_QUA = "prcimpfromqua";
	public static final String IMPORTANCE = "prcimportance";
	public static final String ALLOW_QUA_REFUSAL = "prcquarefusal";
	public static final String QUA_REFUSAL_LIMIT_COMPOSITE = "prcquarefusallimitcomposite";
	private static final String QUA_REFUSAL_LIMIT = "prcquarefusallimit";
	private static final String QUA_REFUSAL_LIMIT_VALUE = "prcquarefusallimitvalue";
	public static final String INDICATOR_REFUSAL_LIMIT_COMPOSITE = "prcindrefusallimitcomposite";
	private static final String INDICATOR_REFUSAL_LIMIT = "prcindrefusallimit";
	private static final String INDICATOR_REFUSAL_LIMIT_UNIT = "prcindrefusallimitunit";
	private static final String INDICATOR_REFUSAL_LIMIT_VALUE = "prcindrefusallimitvalue";
	private static final String INDICATOR_REFUSAL_LIMIT_BY_QUA_CATEGORY = "prcindrefusallimitquacategory";
	public static final String VIEW_QUA_MARK_BY_SOURCE = "prquamarkbysource";
	public static final String CALCULATE_FORMULA_QUA = "calculatequaformula";
	private static final String FORMULA_QUA_TYPE = "prcquaformulatype";
	public static final String FORMULA_QUA = "prcquaformula";
	//KPI
    public static final String IS_KPI = "prciskpi";
    public static final String EVALUATE_KPI = "prcevaluatekpi";
	public static final String EVALUATE_CONTROL_POINT = "prcevaluatecontrolpoint";
	public static final String ALLOW_COPY_KPI = "prccopykpi";
	public static final String IS_SCHEDULE = "prcisschedule";
	public static final String KPI_RESTRICTION = "prckpirestrict";
	public static final String KPI_RESTRICTION_COUNT = "prckpirestrictcount";
	public static final String KPI_RESTRICTION_SUM_IMP = "prckpirestrictsumimp";
	public static final String KPI_RESTRICTION_VALUE = "prckpirestrictvalue";
	public static final String CONTROL_POINT_RESTRICTION = "prccontrolpointrestrict";
	public static final String CONTROL_POINT_RESTRICTION_COUNT = "prccontrolpointrrestrictcount";
	public static final String CONTROL_POINT_RESTRICTION_VALUE = "prccontrolpointrrestrictvalue";
	//Задачи
	public static final String IS_TASK = "prcistask";
	//Опрос
	public static final String IS_SURVEY = "prcissurvey";
	public static final String TYPE_SURVEY = "prctypesurvey";
	public static final String SURVEY_ID = SurveyBean.ID;
	//Для категории Наблюдатели
	public static final String OBSERVER_SOURCE = "prcobserversource";
	public static final String OBSERVER_GROUP_ID = "observergrid";
	public static final String OBSERVER_PERSON_ID = "observerid";
    //Для дополнительных категорий
    public static final String ADD_CATEGORY_ID = "addcategoryid";
    public static final String PERSON_GROUP_ID = PersonGroupCatalogBean.ID;
    //Для категории оценочное собеседование
    public static final String SCORE_FROM_SOURCE = "prcscorefromsource";
    public static final String CATEGORY_SOURCE = "prccategorysource";
    //Для категории Результаты другой процедуры
    public static final String PROCEDURE_TYPE_ID = "prtypeid";
    //КАтегория с несколькими оценивающими
	public static final String COMPLETE_CONDITION = "completecondition";
	private static final String MIN_COUNT = "prcmincount";
	private static final String MIN_UNIT = "prcminunit";
	public static final String MAX_SURVEY_COUNT = "prcmaxsurvey";

    public static final String AUTO_ASSIGNMENT = "autoassign";
	public static final String SELF_ASSIGNMENT = "selfassign";
    public static final String DEVELOPMENT = "development";

	@Name(ID)
	private String id;
	@Name(PROCEDURE_ID)
    @NotEmptyBean
	private NameBean procedure;
	/**
	 * Категория
	 */
	@Name(CATEGORY)
	@Converter(ProcedureEstimatingCategoryFieldConverter.class)
	private ProcedureEstimatingCategory category;
	/**
	 * Способ оценивания
	 */
	@Name(METHOD)
	private EstimatingMethod method;
	/**
	 * Комментарий к оценке по компетенции
	 */
	@Name(COMMENT_QUA)
	private PrCategoryComment commentQua;
	/**
	 * Комментарий к оценке по индикатору
	 */
	@Name(COMMENT_IND)
	private PrCategoryScoreComment commentInd;
	@Name(COMMENT_IND_SCORE_CONDITIONS)
	private ArrayBean<PrCategoryScoreCommentConditionBean> commentIndScoreConditions;
	/**
	 * Ограничивать минимальную длину комментария к индикатору
	 */
	@Name(COMMENT_IND_LIMIT_LENGTH)
	private PrCategoryScoreCommentLengthLimit commentIndLengthLimit;
	/**
	 * Минимальное количество символов комментария к оценке по индикатору
	 */
	@Name(COMMENT_IND_MIN_LENGTH)
	private Integer commentIndMinLength;
	/**
	 * Участие
	 */
	@Name(IS_USE)
	private Boolean isUse;
	/**
	 * Мин кол-во заполненных анкет
	 */
	@Name(MIN_COUNT)
	private Integer minCount;
	@Name(MIN_UNIT)
	private CompletionUnit minUnit;
	/**
	 * Сильные и слабые стороны по компетенции
	 */
	@Name(STRENGTHS_WEAKNESSES_QUA)
	private PrCategoryQuaStrengthsWeaknesses strengthsWeaknessesQua;
	/**
	 * Комментарий к анкете
	 */
	@Name(COMMENT)
	private PrCategoryComment comment;
	/**
	 * Включить опрос
	 */
	@Name(IS_SURVEY)
	private Boolean isSurvey;
	/**
	 * Опрос
	 */
	@Name(TYPE_SURVEY)
	private CategoryProfileType typeSurvey;
	/**
	 * Индивидуальный опрос
	 */
	@Name(SURVEY_ID)
	private NameBean survey;
	/**
	 * Разрешен просмотр итоговой оценки
	 */
	@Name(VIEW_RESULT_SCORE)
	private Boolean viewResultScore;
	/**
	 * Оцениваются компетенции
	 */
	@Name(IS_QUA)
	private Boolean isQua;
    /**
     * Проставлять оценку по категориям
     */
    @Name(IS_QUA_CATEGORY)
    private Boolean isQuaCategory;
	/**
	 * Оцениваются kpi
	 */
	@Name(IS_KPI)
	private Boolean isKpi;
    /**
     * Проставлять оценку по показателю
     */
    @Name(EVALUATE_KPI)
    private Boolean evaluateKPI;
	/**
	 * Проставлять оценку по контрольной точке
	 */
	@Name(EVALUATE_CONTROL_POINT)
	private Boolean evaluateControlPoint;
	/**
	 * Разрешить копировать показатели из других карт
	 */
	@Name(ALLOW_COPY_KPI)
	private Boolean allowCopyKPI;
	/**
	 * Оцениваются задачи
	 */
	@Name(IS_TASK)
	private Boolean isTask;
    /**
     * Разрешить отказываться от оценивания
     */
    @Name(ALLOW_QUA_REFUSAL)
    private Boolean allowQuaRefusal;
	/**
	 * Лимит компетенций с отказом от оценивания
	 */
	@Name(QUA_REFUSAL_LIMIT)
	private PrCategoryQuaRefusalLimit quaRefusalLimit;
    @Name(QUA_REFUSAL_LIMIT_VALUE)
	private Integer quaRefusalLimitValue;
	/**
	 * Лимит индикаторов с отказом от оценивания
	 */
	@Name(INDICATOR_REFUSAL_LIMIT)
	private PrCategoryIndicatorRefusalLimit indicatorRefusalLimit;
	@Name(INDICATOR_REFUSAL_LIMIT_UNIT)
	private PrCategoryIndicatorRefusalLimitUnit indicatorRefusalLimitUnit;
	@Name(INDICATOR_REFUSAL_LIMIT_VALUE)
	private Integer indicatorRefusalLimitValue;
	@Name(INDICATOR_REFUSAL_LIMIT_BY_QUA_CATEGORY)
	private ArrayBean<PrCategoryIndByQuaCategoryRefusalLimitBean> indicatorByQuaCategoryRefusalLimits;
	/**
	 * Источник наблюдателей
	 */
	@Name(OBSERVER_SOURCE)
	private ObserverSource observerSource;
	@Name(OBSERVER_GROUP_ID)
	private NameBean observerGroup;
	@Name(OBSERVER_PERSON_ID)
	private NameBean observerPerson;
    /**
     * Использовать расписание
     */
    @Name(IS_SCHEDULE)
    private Boolean isSchedule;
    /**
     * Категория
     */
    @Name(ADD_CATEGORY_ID)
    private NameBean addCategory;
    /**
     * Группа физических лиц
     */
    @Name(PERSON_GROUP_ID)
    private NameBean personGroup;
    /**
     * Максимальное количество анкет на одного оценивающего
     */
    @Name(MAX_SURVEY_COUNT)
    private Integer maxSurveyCount;
    /**
     * Проставлять оценки из источника
     */
    @Name(SCORE_FROM_SOURCE)
    private Boolean scoreFromSource;
    /**
     * Источник для оценок по умолчанию
     */
    @Name(CATEGORY_SOURCE)
    private EstimatingCategory categorySource;
    /**
     * Важность категории оценивающих из требования компетенции
     */
    @Name(IMPORTANCE_FROM_QUA)
    private Boolean importanceFromQua;
    /**
     * Важность
     */
    @Name(IMPORTANCE)
    private Integer importance;
    @Name(PROCEDURE_TYPE_ID)
    private NameBean procedureType;
	/**
	 * Проверка ограничений при добавлении и каскадировании показателей: По количеству
	 */
	@Name(KPI_RESTRICTION_COUNT)
	private KPIRestriction kpiRestrictionCount;
	/**
	 * Проверка ограничений при добавлении и каскадировании показателей: По сумме важностей
	 */
	@Name(KPI_RESTRICTION_SUM_IMP)
	private KPIRestriction kpiRestrictionSumImp;
	/**
	 * Проверка ограничений при добавлении и каскадировании показателей: По значениям полей
	 */
	@Name(KPI_RESTRICTION_VALUE)
	private KPIRestriction kpiRestrictionValue;
	/**
	 * Проверка ограничений при добавлении контрольных точек: По количеству
	 */
	@Name(CONTROL_POINT_RESTRICTION_COUNT)
	private KPIRestriction controlPointRestrictionCount;
	/**
	 * Проверка ограничений при добавлении контрольных точек: По значениям полей
	 */
	@Name(CONTROL_POINT_RESTRICTION_VALUE)
	private KPIRestriction controlPointRestrictionValue;
	/**
	 * Не отменять завершенные анкеты архивных оценивающих
	 */
	@Name(NOT_CANCEL_COMPLETED_SURVEY_BY_ARCHIVE_PERSON)
	private Boolean notCancelCompletedSurveyByArchivePerson;
	/**
	 * Анкета завершена
	 */
	@Name(COMPLETION_SURVEY_METHOD)
	private SurveyCompletionMethod completionSurveyMethod;
	/**
	 * Не отменять анкеты архивных оценивающих для карт на завершающем этапе
	 */
	@Name(NOT_CANCEL_FINAL_PHASE_SURVEY_BY_ARCHIVE_PERSON)
	private Boolean notCancelFinalPhaseSurveyByArchivePerson;
	/**
	 * Не обновлять оценивающего до окончания оцениваемого периода за N дней
	 */
	@Name(NOT_CANCEL_SURVEY_BEFORE_END)
	private Integer notCancelSurveyBeforeEndEvaluationPeriod;
	/**
	 * При отмене анкеты добавлять для оценивающего анкету по категории
	 */
	@Name(CANCEL_SURVEY_ESTIMATOR_CATEGORY_ID)
	private NameBean categoryForEstimatorWhenCancelSurvey;
	/**
	 * При отмене анкеты добавлять анкету по категории
	 */
	@Name(CANCEL_SURVEY_CATEGORY_ID)
	private NameBean categoryWhenCancelSurvey;
	/**
	 * Назначать анкеты для оценивающих в декретном отпуске
	 */
	@Name(ASSIGN_ESTIMATOR_MATERNITY_LEAVE)
	private Boolean assignEstimatorMaternityLeave;
	/**
	 * Назначать анкеты-претенденты для оценивающих в декретном отпуске
	 */
	@Name(ASSIGN_CANDIDATE_MATERNITY_LEAVE)
	private Boolean assignCandidateMaternityLeave;
	/**
	 * Видимость чужих оценок по компетенциям: Определять по источнику этапа
	 */
	@Name(VIEW_QUA_MARK_BY_SOURCE)
	private Boolean viewQuaMarkBySource;
	/**
	 * Формула расчета оценки категории оценивающих по компетенции
	 */
	@Name(FORMULA_QUA_TYPE)
	private PrCategoryQuaFormulaType formulaQuaType;
	@Name(FORMULA_QUA)
	private String formulaQua;

	@Name(AUTO_ASSIGNMENT)
	private PrCategoryAutoAssignBean autoAssignment;
	@Name(SELF_ASSIGNMENT)
	private PRCategorySelfAssignmentBean selfAssignment;
    @Name(DEVELOPMENT)
    private PRCategoryDevelopmentBean development;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public NameBean getProcedure() {
		return procedure;
	}

	public void setProcedure(NameBean procedure) {
		this.procedure = procedure;
	}

	public ProcedureEstimatingCategory getCategory() {
		return category;
	}

	public void setCategory(ProcedureEstimatingCategory category) {
		this.category = category;
	}

	public EstimatingMethod getMethod() {
		return method;
	}

	public void setMethod(EstimatingMethod method) {
		this.method = method;
	}

	public PrCategoryScoreComment getCommentInd() {
		return commentInd;
	}

	public void setCommentInd(PrCategoryScoreComment commentInd) {
		this.commentInd = commentInd;
	}

	public ArrayBean<PrCategoryScoreCommentConditionBean> getCommentIndScoreConditions() {
		return commentIndScoreConditions;
	}

	public void setCommentIndScoreConditions(ArrayBean<PrCategoryScoreCommentConditionBean> commentIndScoreConditions) {
		this.commentIndScoreConditions = commentIndScoreConditions;
	}

	public PrCategoryScoreCommentLengthLimit getCommentIndLengthLimit() {
		return commentIndLengthLimit;
	}

	public void setCommentIndLengthLimit(PrCategoryScoreCommentLengthLimit commentIndLengthLimit) {
		this.commentIndLengthLimit = commentIndLengthLimit;
	}

	public Integer getCommentIndMinLength() {
		return commentIndMinLength;
	}

	public void setCommentIndMinLength(Integer commentIndMinLength) {
		this.commentIndMinLength = commentIndMinLength;
	}

	public PrCategoryComment getCommentQua() {
		return commentQua;
	}

	public void setCommentQua(PrCategoryComment commentQua) {
		this.commentQua = commentQua;
	}

	public Boolean getIsUse() {
		return isUse;
	}

	public void setIsUse(Boolean use) {
		isUse = use;
	}

	public CompletionUnit getMinUnit() {
		return minUnit;
	}

	public void setMinUnit(CompletionUnit minUnit) {
		this.minUnit = minUnit;
	}

	public Integer getMinCount() {
		return minCount;
	}

	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}

	public PrCategoryQuaStrengthsWeaknesses getStrengthsWeaknessesQua() {
		return strengthsWeaknessesQua;
	}

	public void setStrengthsWeaknessesQua(PrCategoryQuaStrengthsWeaknesses strengthsWeaknessesQua) {
		this.strengthsWeaknessesQua = strengthsWeaknessesQua;
	}

	public PrCategoryComment getComment() {
		return comment;
	}

	public void setComment(PrCategoryComment comment) {
		this.comment = comment;
	}

	public void setIsSurvey(Boolean survey) {
		isSurvey = survey;
	}

	public Boolean getIsSurvey() {
		return isSurvey;
	}

	public void setSurvey(NameBean survey) {
		this.survey = survey;
	}

	public NameBean getSurvey() {
		return survey;
	}

	public CategoryProfileType getTypeSurvey() {
		return typeSurvey;
	}

	public void setTypeSurvey(CategoryProfileType typeSurvey) {
		this.typeSurvey = typeSurvey;
	}

	public Boolean getViewResultScore() {
		return viewResultScore;
	}

	public void setViewResultScore(Boolean viewResultScore) {
		this.viewResultScore = viewResultScore;
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

	public Boolean getAllowCopyKPI() {
		return allowCopyKPI;
	}

	public void setAllowCopyKPI(Boolean allowCopyKPI) {
		this.allowCopyKPI = allowCopyKPI;
	}

	public Boolean getIsTask() {
		return isTask;
	}

	public void setIsTask(Boolean task) {
		isTask = task;
	}

    public Boolean getAllowQuaRefusal() {
        return allowQuaRefusal;
    }

    public void setAllowQuaRefusal(Boolean allowQuaRefusal) {
        this.allowQuaRefusal = allowQuaRefusal;
    }

    public PRCategorySelfAssignmentBean getSelfAssignment() {
        return selfAssignment;
    }

    public void setSelfAssignment(PRCategorySelfAssignmentBean selfAssignment) {
        this.selfAssignment = selfAssignment;
    }

	public ObserverSource getObserverSource() {
		return observerSource;
	}

	public void setObserverSource(ObserverSource observerSource) {
		this.observerSource = observerSource;
	}

	public NameBean getObserverGroup() {
		return observerGroup;
	}

	public void setObserverGroup(NameBean observerGroup) {
		this.observerGroup = observerGroup;
	}

	public NameBean getObserverPerson() {
		return observerPerson;
	}

	public void setObserverPerson(NameBean observerPerson) {
		this.observerPerson = observerPerson;
	}

	@Override
	public Optional<ProcedureEstimatingCategory> getEstimatingCategory() {
		return Optional.of(getCategory());
	}

	@Override
	public NameBean getAddCategory() {
		return addCategory;
	}

	public void setAddCategory(NameBean addCategory) {
		this.addCategory = addCategory;
	}

	@Override
    public NameBean getPersonGroup() {
        return personGroup;
    }

    public void setPersonGroup(NameBean personGroup) {
        this.personGroup = personGroup;
    }

    public Integer getMaxSurveyCount() {
        return maxSurveyCount;
    }

    public void setMaxSurveyCount(Integer maxSurveyCount) {
        this.maxSurveyCount = maxSurveyCount;
    }

    public Boolean getIsSchedule() {
        return isSchedule;
    }

    public void setIsSchedule(Boolean schedule) {
        isSchedule = schedule;
    }

    public Boolean getIsQuaCategory() {
        return isQuaCategory;
    }

    public void setIsQuaCategory(Boolean quaCategory) {
        isQuaCategory = quaCategory;
    }

    public EstimatingCategory getCategorySource() {
        return categorySource;
    }

    public void setCategorySource(EstimatingCategory categorySource) {
        this.categorySource = categorySource;
    }

    public Boolean getScoreFromSource() {
        return scoreFromSource;
    }

    public void setScoreFromSource(Boolean scoreFromSource) {
        this.scoreFromSource = scoreFromSource;
    }

    public Boolean getEvaluateKPI() {
        return evaluateKPI;
    }

    public void setEvaluateKPI(Boolean evaluateKPI) {
        this.evaluateKPI = evaluateKPI;
    }

	public Boolean getEvaluateControlPoint() {
		return evaluateControlPoint;
	}

	public void setEvaluateControlPoint(Boolean evaluateControlPoint) {
		this.evaluateControlPoint = evaluateControlPoint;
	}

	public Boolean getImportanceFromQua() {
        return importanceFromQua;
    }

    public void setImportanceFromQua(Boolean importanceFromQua) {
        this.importanceFromQua = importanceFromQua;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

	@Override
    public NameBean getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(NameBean procedureType) {
        this.procedureType = procedureType;
    }

    public PRCategoryDevelopmentBean getDevelopment() {
        return development;
    }

    public void setDevelopment(PRCategoryDevelopmentBean development) {
        this.development = development;
    }

	public KPIRestriction getKpiRestrictionCount() {
		return kpiRestrictionCount;
	}

	public void setKpiRestrictionCount(KPIRestriction kpiRestrictionCount) {
		this.kpiRestrictionCount = kpiRestrictionCount;
	}

	public KPIRestriction getKpiRestrictionSumImp() {
		return kpiRestrictionSumImp;
	}

	public void setKpiRestrictionSumImp(KPIRestriction kpiRestrictionSumImp) {
		this.kpiRestrictionSumImp = kpiRestrictionSumImp;
	}

	public KPIRestriction getKpiRestrictionValue() {
		return kpiRestrictionValue;
	}

	public void setKpiRestrictionValue(KPIRestriction kpiRestrictionValue) {
		this.kpiRestrictionValue = kpiRestrictionValue;
	}

	public KPIRestriction getControlPointRestrictionCount() {
		return controlPointRestrictionCount;
	}

	public void setControlPointRestrictionCount(KPIRestriction controlPointRestrictionCount) {
		this.controlPointRestrictionCount = controlPointRestrictionCount;
	}

	public KPIRestriction getControlPointRestrictionValue() {
		return controlPointRestrictionValue;
	}

	public void setControlPointRestrictionValue(KPIRestriction controlPointRestrictionValue) {
		this.controlPointRestrictionValue = controlPointRestrictionValue;
	}

	public Boolean getNotCancelCompletedSurveyByArchivePerson() {
		return notCancelCompletedSurveyByArchivePerson;
	}

	public void setNotCancelCompletedSurveyByArchivePerson(Boolean notCancelCompletedSurveyByArchivePerson) {
		this.notCancelCompletedSurveyByArchivePerson = notCancelCompletedSurveyByArchivePerson;
	}

	public SurveyCompletionMethod getCompletionSurveyMethod() {
		return completionSurveyMethod;
	}

	public void setCompletionSurveyMethod(SurveyCompletionMethod completionSurveyMethod) {
		this.completionSurveyMethod = completionSurveyMethod;
	}

	public Boolean getNotCancelFinalPhaseSurveyByArchivePerson() {
		return notCancelFinalPhaseSurveyByArchivePerson;
	}

	public void setNotCancelFinalPhaseSurveyByArchivePerson(Boolean notCancelFinalPhaseSurveyByArchivePerson) {
		this.notCancelFinalPhaseSurveyByArchivePerson = notCancelFinalPhaseSurveyByArchivePerson;
	}

	public Integer getNotCancelSurveyBeforeEndEvaluationPeriod() {
		return notCancelSurveyBeforeEndEvaluationPeriod;
	}

	public void setNotCancelSurveyBeforeEndEvaluationPeriod(Integer notCancelSurveyBeforeEndEvaluationPeriod) {
		this.notCancelSurveyBeforeEndEvaluationPeriod = notCancelSurveyBeforeEndEvaluationPeriod;
	}

	public NameBean getCategoryForEstimatorWhenCancelSurvey() {
		return categoryForEstimatorWhenCancelSurvey;
	}

	public void setCategoryForEstimatorWhenCancelSurvey(NameBean categoryForEstimatorWhenCancelSurvey) {
		this.categoryForEstimatorWhenCancelSurvey = categoryForEstimatorWhenCancelSurvey;
	}

	public NameBean getCategoryWhenCancelSurvey() {
		return categoryWhenCancelSurvey;
	}

	public void setCategoryWhenCancelSurvey(NameBean categoryWhenCancelSurvey) {
		this.categoryWhenCancelSurvey = categoryWhenCancelSurvey;
	}

	public Boolean getAssignEstimatorMaternityLeave() {
		return assignEstimatorMaternityLeave;
	}

	public void setAssignEstimatorMaternityLeave(Boolean assignEstimatorMaternityLeave) {
		this.assignEstimatorMaternityLeave = assignEstimatorMaternityLeave;
	}

	public Boolean getAssignCandidateMaternityLeave() {
		return assignCandidateMaternityLeave;
	}

	public void setAssignCandidateMaternityLeave(Boolean assignCandidateMaternityLeave) {
		this.assignCandidateMaternityLeave = assignCandidateMaternityLeave;
	}

	public Boolean getViewQuaMarkBySource() {
		return viewQuaMarkBySource;
	}

	public void setViewQuaMarkBySource(Boolean viewQuaMarkBySource) {
		this.viewQuaMarkBySource = viewQuaMarkBySource;
	}

	public PrCategoryQuaRefusalLimit getQuaRefusalLimit() {
		return quaRefusalLimit;
	}

	public void setQuaRefusalLimit(PrCategoryQuaRefusalLimit quaRefusalLimit) {
		this.quaRefusalLimit = quaRefusalLimit;
	}

	public Integer getQuaRefusalLimitValue() {
		return quaRefusalLimitValue;
	}

	public void setQuaRefusalLimitValue(Integer quaRefusalLimitValue) {
		this.quaRefusalLimitValue = quaRefusalLimitValue;
	}

	public PrCategoryIndicatorRefusalLimit getIndicatorRefusalLimit() {
		return indicatorRefusalLimit;
	}

	public void setIndicatorRefusalLimit(PrCategoryIndicatorRefusalLimit indicatorRefusalLimit) {
		this.indicatorRefusalLimit = indicatorRefusalLimit;
	}

	public PrCategoryIndicatorRefusalLimitUnit getIndicatorRefusalLimitUnit() {
		return indicatorRefusalLimitUnit;
	}

	public void setIndicatorRefusalLimitUnit(PrCategoryIndicatorRefusalLimitUnit indicatorRefusalLimitUnit) {
		this.indicatorRefusalLimitUnit = indicatorRefusalLimitUnit;
	}

	public Integer getIndicatorRefusalLimitValue() {
		return indicatorRefusalLimitValue;
	}

	public void setIndicatorRefusalLimitValue(Integer indicatorRefusalLimitValue) {
		this.indicatorRefusalLimitValue = indicatorRefusalLimitValue;
	}

	public ArrayBean<PrCategoryIndByQuaCategoryRefusalLimitBean> getIndicatorByQuaCategoryRefusalLimits() {
		return indicatorByQuaCategoryRefusalLimits;
	}

	public void setIndicatorByQuaCategoryRefusalLimits(
			ArrayBean<PrCategoryIndByQuaCategoryRefusalLimitBean> indicatorByQuaCategoryRefusalLimits) {
		this.indicatorByQuaCategoryRefusalLimits = indicatorByQuaCategoryRefusalLimits;
	}

    public PrCategoryQuaFormulaType getFormulaQuaType() {
        return formulaQuaType;
    }

    public void setFormulaQuaType(PrCategoryQuaFormulaType formulaQuaType) {
        this.formulaQuaType = formulaQuaType;
    }

    public String getFormulaQua() {
        return formulaQua;
    }

    public void setFormulaQua(String formulaQua) {
        this.formulaQua = formulaQua;
    }

	public PrCategoryAutoAssignBean getAutoAssignment() {
		return autoAssignment;
	}

	public void setAutoAssignment(PrCategoryAutoAssignBean autoAssignment) {
		this.autoAssignment = autoAssignment;
	}

	@AssertTrue(message = "{lms.core.newprocedure.ProcedureMessage.category_already_added}")
	public boolean isCategory() {
		PRCategoryBean filter = new PRCategoryBean();
		filter.getProcedure().setId(procedure.getId());
		filter.setCategory(category);
		category.getValueForCompare(this)
			.ifPresent(nameBean -> category.setValueForCompare(filter, nameBean.getId()));
		List<PRCategoryBean> categories = EntityManager.list(filter);
		return StringHelper.isEmpty(id) ?
			categories.isEmpty() :
			categories.isEmpty() || categories.size() == 1 && categories.get(0).getId().equals(id);
	}

	@FieldErrors(fieldName = FORMULA_QUA)
	public Optional<String> getFormulaQuaErrors() {
		return formulaQuaType == PrCategoryQuaFormulaType.individual ?
			ProcedureFormulaType.procedure_estimating_category_competence.checkFormula(this, formulaQua) :
			Optional.empty();
	}

	@Override
	public String getDataName() {
		return DATANAME;
	}

	public static DataObject createDataObject() {
		DataObject dataObject = new DataObject(DATANAME, SystemMessages.category, PRCategoryBean.class)
			.setFields(
                new KeyField(ID),
                new LookupField(PROCEDURE_ID, EMailMessage.procedure, ProcedureFrame.NAME, FKField.CASCADE),
                new ComboField(
                	CATEGORY,
					ProcedureMessage.category_estimating,
					ProcedureEstimatingCategoryStore.getInstance().getComboValues()
				).setIsName(),
                new ComboField(
					METHOD,
					ProcedureMessage.type_estimate,
					new SimpleComboValues(EstimatingMethod.getCategoryValues()),
					EstimatingMethod.FROM_QUA),
                new ComboField(
					COMMENT_QUA,
					ProcedureMessage.comment_assessment_competence,
					PrCategoryComment.class,
					PrCategoryComment.NOT_REQUIRED),
                new ComboFieldBuilder(
                	new ComboField(
                		COMMENT_IND,
						ProcedureMessage.comment_assessment_indicator,
						PrCategoryScoreComment.class,
						PrCategoryScoreComment.NOT_REQUIRED))
					.setScript(new BlockExpression()
						.add(getCommentIndicatorExpression())
						.add(getCommentIndicatorLimitLengthExpression()))
					.getDataField(),
				new ArrayFieldBuilder<>(
					new ArrayField<>(
						COMMENT_IND_SCORE_CONDITIONS, SystemMessages.empty, PrCategoryScoreCommentConditionBean.class))
					.getDataField(),
                new ComboFieldBuilder(
                	new ComboField(
                		COMMENT_IND_LIMIT_LENGTH,
						ProcedureMessage.limit_min_length_indicator_comment,
						PrCategoryScoreCommentLengthLimit.class))
					.setScript(getCommentIndicatorLimitLengthExpression())
					.getDataField(),
                new IntegerField<>(COMMENT_IND_MIN_LENGTH, ProcedureMessage.min_characters_comment_indicator),
				new ComboField(
					STRENGTHS_WEAKNESSES_QUA,
					ProcedureMessage.competence_strengths_weaknesses,
					PrCategoryQuaStrengthsWeaknesses.class),
				new CheckField(IS_USE, VVMessage.use_type),
                new CompositeField(
                	COMPLETE_CONDITION,
					ProcedureMessage.min_completed_surveys,
					getCountField(MIN_COUNT).setDefaultValue(DEFAULT_MIN_COUNT),
					getUnitFieldBuilder(MIN_UNIT, CompletionUnit.class)
						.getDataField()
						.setDefaultValue(CompletionUnit.PERCENT)),
                new ComboField(
					COMMENT,
					ProcedureMessage.survey_comment,
					PrCategoryComment.class,
					PrCategoryComment.NOT_REQUIRED),
                new CheckFieldBuilder(
                	new CheckField(IS_SURVEY, ProcedureMessage.include_survey))
					.setScript(getAssessmentExp(TYPE_SURVEY, SURVEY_ID))
					.getDataField(),
                new ComboFieldBuilder(
                	new ComboField(TYPE_SURVEY, ProcedureMessage.questionnaire, CategoryProfileType.class))
					.setScript(getTypeProfileExp(SURVEY_ID))
					.getDataField(),
                new LookupFieldBuilder(
                	new LookupField(
                		SURVEY_ID, ProcedureMessage.individual_questionnaire, SurveyFrame.NAME, FKField.RESTRICT))
					.addFieldComponentProcessor(
						new SetChoosingParamFieldComponentProcessor<>(SurveyType.standard.getValue()))
					.getDataField(),
                new CheckField(VIEW_RESULT_SCORE, ProcedureMessage.show_result_score),
                new CheckFieldBuilder(new CheckField(IS_QUA, ProcedureMessage.estimate))
					.setScript(new BlockExpression()
						.add(getIsQuaExpression())
						.add(getCommentIndicatorExpression())
						.add(getQuaImportanceExpression())
						.add(getCommentIndicatorLimitLengthExpression())
						.add(getQuaAllowRefusalExpression())
						.add(getQuaFormulaExpression()))
					.getDataField(),
                new CheckField(IS_QUA_CATEGORY, ProcedureMessage.evaluate_category),
                new CheckFieldBuilder(new CheckField(IS_KPI, ProcedureMessage.estimate))
					.setScript(CheckExpressionBuilder.createExpressionForShowFields(
						EVALUATE_KPI, EVALUATE_CONTROL_POINT, ALLOW_COPY_KPI))
					.getDataField(),
                new CheckField(EVALUATE_KPI, ProcedureMessage.evaluate_kpi),
				new CheckField(EVALUATE_CONTROL_POINT, ProcedureMessage.evaluate_control_point),
				new CheckField(ALLOW_COPY_KPI, ProcedureMessage.allow_copy_kpi_from_cards),
                new CheckField(IS_TASK, ProcedureMessage.estimate),
                new CheckFieldBuilder(new CheckField(ALLOW_QUA_REFUSAL, ProcedureMessage.allow_refuse_estimation)
					.setDefaultTrue())
					.setScript(getQuaAllowRefusalExpression())
					.getDataField(),
				new CompositeFieldBuilder(
					new CompositeField(
						QUA_REFUSAL_LIMIT_COMPOSITE,
						ProcedureMessage.competency_refusal_limit,
						getUnitFieldBuilder(QUA_REFUSAL_LIMIT, PrCategoryQuaRefusalLimit.class)
							.setScript(getExpressionByProcedureKind(PRCategoryBean::getQuaRefusalLimitExpression))
							.getDataField(),
						getCountField(QUA_REFUSAL_LIMIT_VALUE)))
					.getDataField(),
				new CompositeFieldBuilder(
					new CompositeField(
						INDICATOR_REFUSAL_LIMIT_COMPOSITE,
						ProcedureMessage.indicator_refusal_limit,
						getUnitFieldBuilder(INDICATOR_REFUSAL_LIMIT, PrCategoryIndicatorRefusalLimit.class)
							.setScript(getExpressionByProcedureKind(PRCategoryBean::getIndicatorRefusalLimitExpression))
							.getDataField(),
						getCountField(INDICATOR_REFUSAL_LIMIT_VALUE),
						getUnitFieldBuilder(INDICATOR_REFUSAL_LIMIT_UNIT, PrCategoryIndicatorRefusalLimitUnit.class)
							.getDataField(),
						new ArrayFieldBuilder<>(
							new ArrayField<>(
								INDICATOR_REFUSAL_LIMIT_BY_QUA_CATEGORY,
								SystemMessages.empty,
								PrCategoryIndByQuaCategoryRefusalLimitBean.class))
							.setUniqueBy(PrCategoryIndByQuaCategoryRefusalLimitBean.QUA_CATEGORY_ID)
							.getDataField()))
					.getDataField(),
                new ComboFieldBuilder(
                	new ComboField(OBSERVER_SOURCE, PersonMessage.source, ObserverSource.class, ObserverSource.manager))
					.setScript(new BlockExpression()
						.add(ComboBoxExpressionBuilder.createExpressionForShowFields(
							ObserverSource.group.getValue(), OBSERVER_GROUP_ID))
						.add(ComboBoxExpressionBuilder.createExpressionForShowFields(
							ObserverSource.person.getValue(), OBSERVER_PERSON_ID)))
					.getDataField(),
                new LookupField(
                	OBSERVER_GROUP_ID, PersonMessage.group_persons, PersonGroupCatalog.NAME, FKField.RESTRICT),
                new PersonLookupFieldBuilder(
                	new LookupField(OBSERVER_PERSON_ID, VVMessage.person, PersonFrame.NAME, FKField.RESTRICT))
					.getDataField(),
                new RSField(
                	ADD_CATEGORY_ID, SystemMessages.name, ProcedureCategoryRubricator.CATEGORY, FKField.RESTRICT),
				new RSField(
					PROCEDURE_TYPE_ID,
					ProcedureMessage.procedure_type,
					ProcedureTypeRubricator.PROCEDURE_TYPE,
					FKField.RESTRICT),
                new IntegerField<>(MAX_SURVEY_COUNT, ProcedureMessage.maximum_questionnaires_one_estimator),
                new CheckField(IS_SCHEDULE, ProcedureMessage.use_schedule),
                new CheckFieldBuilder(new CheckField(SCORE_FROM_SOURCE, ProcedureMessage.estimates_from_source))
					.setScript(CheckExpressionBuilder.createExpressionForShowFields(CATEGORY_SOURCE))
					.getDataField(),
                new ComboField(
                	CATEGORY_SOURCE,
					ProcedureMessage.source_for_estimates,
					new SimpleComboValues(
						EstimatingCategory.SELF,
						EstimatingCategory.MANAGER_N,
						EstimatingCategory.MANAGER_N1,
						EstimatingCategory.MANAGER_N2,
						EstimatingCategory.FUNC_MANAGER)),
                new LookupField(PERSON_GROUP_ID, PersonMessage.group_persons, PersonGroupCatalog.NAME, FKField.SET_NULL),
                new CheckFieldBuilder(new CheckField(IMPORTANCE_FROM_QUA, ProcedureMessage.importance_from_competence))
					.setScript(getQuaImportanceExpression())
					.getDataField()
					.setDefaultTrue(),
                new IntegerField<>(IMPORTANCE, QuaMessage.importance),
				new CompositeFieldBuilder(
					new CompositeField(
						KPI_RESTRICTION,
						ProcedureMessage.checking_constraints_adding_cascading,
						new ComboField(KPI_RESTRICTION_COUNT, ProcedureMessage.by_quantity, KPIRestriction.class),
						new ComboField(
							KPI_RESTRICTION_SUM_IMP, ProcedureMessage.by_amount_importance, KPIRestriction.class),
						new ComboField(KPI_RESTRICTION_VALUE, ProcedureMessage.by_field_values, KPIRestriction.class)))
					.setContainerLayout(new VerticalLayout())
					.getDataField(),
				new CompositeFieldBuilder(
					new CompositeField(
						CONTROL_POINT_RESTRICTION,
						ProcedureMessage.checking_constraints_adding_control_points,
						new ComboField(CONTROL_POINT_RESTRICTION_COUNT, ProcedureMessage.by_quantity, KPIRestriction.class),
						new ComboField(CONTROL_POINT_RESTRICTION_VALUE, ProcedureMessage.by_field_values, KPIRestriction.class)))
					.setContainerLayout(new VerticalLayout())
					.getDataField(),
				new CompositeField(
					NOT_CANCEL_COMPLETED_SURVEY_BY_ARCHIVE_PERSON_COMPOSITE,
					ProcedureMessage.not_cancel_completed_surveys_archived_estimators,
					new CheckFieldBuilder(new CheckField(NOT_CANCEL_COMPLETED_SURVEY_BY_ARCHIVE_PERSON).setDefaultTrue())
						.setScript(getNotCancelCompletedSurveyByArchivePersonExpression())
						.getDataField(),
					new ComboField(
						COMPLETION_SURVEY_METHOD, ProcedureMessage.questionnaire_completed, SurveyCompletionMethod.class)),
				new CheckField(
					NOT_CANCEL_FINAL_PHASE_SURVEY_BY_ARCHIVE_PERSON,
					ProcedureMessage.not_cancel_surveys_archived_estimators_final_stage),
				new LookupFieldBuilder(
					new LookupField(
						CANCEL_SURVEY_ESTIMATOR_CATEGORY_ID,
						ProcedureMessage.add_survey_by_category_for_estimator_when_canceling,
						PRCategoryFrame.NAME,
						FKField.SET_NULL))
					.addFieldComponentProcessor(new DisableTypingFieldComponentProcessor())
					.addFieldComponentProcessor(new QuickInsertDisableFieldComponentProcessor())
					.getDataField(),
				new LookupFieldBuilder(
					new LookupField(
						CANCEL_SURVEY_CATEGORY_ID,
						ProcedureMessage.add_survey_by_category_when_canceling,
						PRCategoryFrame.NAME,
						FKField.SET_NULL))
					.addFieldComponentProcessor(new DisableTypingFieldComponentProcessor())
					.addFieldComponentProcessor(new QuickInsertDisableFieldComponentProcessor())
					.getDataField(),
				new IntegerField<>(
					NOT_CANCEL_SURVEY_BEFORE_END,
					ProcedureMessage.not_update_estimator_before_end_evaluation_period),
				new CheckField(ASSIGN_ESTIMATOR_MATERNITY_LEAVE, ProcedureMessage.assign_survey_maternity_leave)
					.setDefaultTrue(),
				new CheckField(ASSIGN_CANDIDATE_MATERNITY_LEAVE, ProcedureMessage.assign_survey_candidate_maternity_leave)
					.setDefaultTrue(),
				new CheckField(VIEW_QUA_MARK_BY_SOURCE, ProcedureMessage.determine_by_phase_source),
				new CompositeFieldBuilder(
					new CompositeField(
						CALCULATE_FORMULA_QUA,
						ProcedureMessage.formula_calculating_estimating_category_competence,
						new ComboFieldBuilder(
							new ComboField(FORMULA_QUA_TYPE, SystemMessages.empty, PrCategoryQuaFormulaType.class))
							.setScript(getQuaFormulaExpression())
							.getDataField(),
						new FormulaFieldBuilder(
							FORMULA_QUA,
							SystemMessages.empty,
							ProcedureFormulaType.procedure_estimating_category_competence)
							.getDataField()))
					.setContainerLayout(new VerticalLayout())
					.getDataField()
        );
		dataObject.addChildDataObject(PrCategoryAutoAssignBean.createDataObject());
        dataObject.addChildDataObject(PRCategorySelfAssignmentBean.createDataObject());
        dataObject.addChildDataObject(PRCategoryDevelopmentBean.createDataObject());
		return dataObject;
	}

	public static IntegerField<?> getCountField(String field) {
		return new IntegerFieldBuilder(new IntegerField<>(field, SystemMessages.empty))
				.addFieldComponentProcessor(new SetInputWidthComponentProcessor(100))
				.getDataField();
	}

	public static ComboFieldBuilder getUnitFieldBuilder(String field, Class<? extends ComboValue> values) {
		return new ComboFieldBuilder(new ComboField(field, SystemMessages.empty, values))
				.addFieldComponentProcessor(new SetInputWidthComponentProcessor(120));
	}

	private static Expression getTypeProfileExp(String field) {
		return ComboBoxExpressionBuilder.createExpressionForShowFields(CategoryProfileType.INDIVIDUAL.getValue(), field);
	}

	private static Expression getAssessmentExp(String typeField, String profileField, String... fields) {
		BlockExpression resultExpression = new BlockExpression();

		VarExpression valueExp = new VarExpression("value", getValue());
		resultExpression.add(valueExp);

		VarAccessExpression varAccessExpValue = new VarAccessExpression(valueExp);

		List<String> allFields = new ArrayList<String>();
		allFields.add(typeField);
		allFields.add(profileField);
		allFields.addAll(Arrays.asList(fields));

		//Показывать поля, если включено
		for (String field : allFields) {
			VarExpression fieldExp = new VarExpression("Field" + field, getField(field));
			resultExpression.add(fieldExp);

			resultExpression.add(Expression.IF(Expression.eq(varAccessExpValue, bool(true)),
					show(fieldExp),
					hide(fieldExp)
			));
		}
		//Показывать поле Индивидуальный профиль, если включно и тип Индивидуальный
		VarExpression typeFieldExp = new VarExpression("Field" + typeField, getField(typeField));
		VarExpression valueTypeExp = new VarExpression("valueType", new PropertyExpression(getValue(typeFieldExp), "objId"));
		resultExpression.add(valueTypeExp);

		VarAccessExpression varAccessTypeExpValue = new VarAccessExpression(valueTypeExp);

		String fieldProfileVarName = "Field" + profileField;
		VarExpression fieldProfileVarExp = new VarExpression(fieldProfileVarName, getField(profileField));
		resultExpression.add(fieldProfileVarExp);

		Expression andExp = Expression.and(Expression.eq(varAccessTypeExpValue, string(CategoryProfileType.INDIVIDUAL.getValue())),
				Expression.eq(varAccessExpValue, bool(true)));
		resultExpression.add(Expression.IF(Expression.val(fieldProfileVarName),
				Expression.IF(andExp,
						show(fieldProfileVarExp),
						hide(fieldProfileVarExp)
				)));

		return resultExpression;
	}

	private static Expression getIsQuaExpression() {
		return getExpressionByProcedureKind(procedureKind -> {
			BlockExpression resultExpression = new BlockExpression();
			resultExpression.add(CheckExpressionBuilder.createExpressionForShowFields(
				METHOD,
				COMMENT_QUA,
				COMMENT_IND,
				IMPORTANCE_FROM_QUA,
				STRENGTHS_WEAKNESSES_QUA));
			VarExpression isQuaValue = new VarExpression(
				"fieldIsQua", new CheckFieldExpression(IS_QUA).getValue());
			resultExpression.add(isQuaValue);
			Stream.of(
				COMMENT,
				VIEW_RESULT_SCORE,
				IS_QUA_CATEGORY,
				SCORE_FROM_SOURCE)
				.forEach(fieldName -> {
					VarExpression fieldExp = new VarExpression(
						"field" + fieldName, new FieldExpression(fieldName));
					resultExpression.add(fieldExp);
					if (procedureKind.isSurveyEvaluate()) {
						resultExpression.add(getExpressionIfExistField(
							fieldExp,
							Expression.IF(
								Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
								show(fieldExp),
								hide(fieldExp))));
					} else {
						resultExpression.add(getExpressionIfExistField(fieldExp, hide(fieldExp)));
					}
				});
			resultExpression.add(CheckExpressionBuilder.createExpressionForShowCompositeFields(
				CALCULATE_FORMULA_QUA));
			return resultExpression;
		});
	}

	private static Expression getCommentIndicatorExpression() {
		return getExpressionByProcedureKind(procedureKind -> {
			BlockExpression resultExpression = new BlockExpression();

			VarExpression isQuaValue = new VarExpression(
				"fieldIsQua", new CheckFieldExpression(IS_QUA).getValue());
			resultExpression.add(isQuaValue);
			VarExpression commentValue = new VarExpression(
				"fieldIndComment", new ComboBoxFieldExpression(COMMENT_IND).objId());
			resultExpression.add(commentValue);

			VarExpression scoreFieldExp = new VarExpression(
				"indScoreField", new FieldExpression(COMMENT_IND_SCORE_CONDITIONS));
			resultExpression.add(scoreFieldExp);
			if (procedureKind.isSurveyEvaluate()) {
				resultExpression.add(getExpressionIfExistField(scoreFieldExp,
					Expression.IF(
						Expression.and(
							Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
							Expression.eq(
								new VarAccessExpression(commentValue),
								str(PrCategoryScoreComment.REQUIRED_BY_SCORE.getValue()))),
						show(scoreFieldExp),
						hide(scoreFieldExp)
					)));
			} else {
				resultExpression.add(getExpressionIfExistField(scoreFieldExp, hide(scoreFieldExp)));
			}
			return resultExpression;
		});
	}

	private static Expression getCommentIndicatorLimitLengthExpression() {
		return getExpressionByProcedureKind(procedureKind -> {
			BlockExpression resultExpression = new BlockExpression();

			VarExpression isQuaValue = new VarExpression(
				"value" + IS_QUA, new CheckFieldExpression(IS_QUA).getValue());
			resultExpression.add(isQuaValue);
			VarExpression commentValue = new VarExpression(
				"value" + COMMENT_IND, new ComboBoxFieldExpression(COMMENT_IND).objId());
			resultExpression.add(commentValue);
			VarExpression limitField = new VarExpression(
				"field" + COMMENT_IND_LIMIT_LENGTH, new ComboBoxFieldExpression(COMMENT_IND_LIMIT_LENGTH));
			resultExpression.add(limitField);
			VarExpression limitValue = new VarExpression(
				"value" + COMMENT_IND_LIMIT_LENGTH,
				new ComboBoxFieldExpression(COMMENT_IND_LIMIT_LENGTH).objId());
			resultExpression.add(limitValue);
			VarExpression lengthField = new VarExpression(
				"field" + COMMENT_IND_MIN_LENGTH, new FieldExpression(COMMENT_IND_MIN_LENGTH));
			resultExpression.add(lengthField);

			Expression showCommentExp = Expression.and(
				Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
				Expression.neq(new VarAccessExpression(commentValue), str(PrCategoryScoreComment.OFF.getValue())));

			if (procedureKind.isSurveyEvaluate()) {
				resultExpression.add(getExpressionIfExistField(limitField,
					Expression.IF(
						showCommentExp,
						show(limitField),
						hide(limitField)
					)));
			} else {
				resultExpression.add(getExpressionIfExistField(limitField, hide(limitField)));
			}
			if (procedureKind.isSurveyEvaluate()) {
				resultExpression.add(getExpressionIfExistField(lengthField,
					Expression.IF(
						Expression.and(
							showCommentExp,
							Expression.neq(
								new VarAccessExpression(limitValue),
								str(PrCategoryScoreCommentLengthLimit.not_limit.getValue()))),
						block(show(lengthField), makeRequired(lengthField)),
						block(hide(lengthField), makeOptional(lengthField))
					)));
			} else {
				resultExpression.add(getExpressionIfExistField(lengthField, hide(lengthField)));
			}
			return resultExpression;
		});
	}

	private static Expression getQuaImportanceExpression() {
		BlockExpression resultExpression = new BlockExpression();

		VarExpression isQuaValue = new VarExpression(
				"fieldIsQua", new CheckFieldExpression(IS_QUA).getValue());
		resultExpression.add(isQuaValue);
		VarExpression impFromQuaValue = new VarExpression(
				"fieldImpFromQua", new CheckFieldExpression(IMPORTANCE_FROM_QUA).getValue());
		resultExpression.add(impFromQuaValue);

		VarExpression impFieldExp = new VarExpression("quaImpField", new FieldExpression(IMPORTANCE));
		resultExpression.add(impFieldExp);
		resultExpression.add(getExpressionIfExistField(impFieldExp,
				Expression.IF(
						Expression.and(
								Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
								Expression.eq(new VarAccessExpression(impFromQuaValue), bool(false))),
						block(makeRequired(impFieldExp), show(impFieldExp)),
						block(makeOptional(impFieldExp), hide(impFieldExp))
				)));
		return resultExpression;
	}

	public static LazyExpression<BlockExpression> getQuaAllowRefusalExpression() {
		return getExpressionByProcedureKind(procedureKind -> {
			BlockExpression expression = new BlockExpression();
			VarExpression isQuaValue = new VarExpression(
				"value" + IS_QUA, new CheckFieldExpression(IS_QUA).getValue());
			expression.add(isQuaValue);
			VarExpression allowRefusalValue = new VarExpression(
				"value" + ALLOW_QUA_REFUSAL, new CheckFieldExpression(ALLOW_QUA_REFUSAL).getValue());
			expression.add(allowRefusalValue);
			Stream.of(
				QUA_REFUSAL_LIMIT_COMPOSITE,
				INDICATOR_REFUSAL_LIMIT_COMPOSITE)
				.forEach(fieldName -> {
					VarExpression fieldExp =
						FieldVarExpressionType.compositeField.createVar("field" + fieldName, fieldName);
					expression.add(fieldExp);
					if (procedureKind.isSurveyEvaluate()) {
						expression.add(getExpressionIfExistField(
							fieldExp,
							Expression.IF(
								Expression.and(
									Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
									Expression.eq(new VarAccessExpression(allowRefusalValue), bool(true))),
								show(fieldExp),
								hide(fieldExp))));
					} else {
						expression.add(getExpressionIfExistField(fieldExp, hide(fieldExp)));
					}
				});
			expression
				.add(getQuaRefusalLimitExpression(procedureKind))
				.add(getIndicatorRefusalLimitExpression(procedureKind));
			return expression;
		});
	}

	private static Expression getQuaRefusalLimitExpression(ProcedureKind procedureKind) {
		BlockExpression resultExpression = new BlockExpression();

		VarExpression isQuaValue = new VarExpression(
			"value" + IS_QUA, new CheckFieldExpression(IS_QUA).getValue());
		resultExpression.add(isQuaValue);
		VarExpression allowRefusalValue = new VarExpression(
			"value" + ALLOW_QUA_REFUSAL, new CheckFieldExpression(ALLOW_QUA_REFUSAL).getValue());
		resultExpression.add(allowRefusalValue);
		VarExpression quaRefusalLimitValue = new VarExpression(
			"value" + QUA_REFUSAL_LIMIT, new ComboBoxFieldExpression(QUA_REFUSAL_LIMIT).objId());
		resultExpression.add(quaRefusalLimitValue);

		VarExpression quaRefusalLimitValueField = new VarExpression(
			"field" + QUA_REFUSAL_LIMIT_VALUE, new FieldExpression(QUA_REFUSAL_LIMIT_VALUE));
		resultExpression.add(quaRefusalLimitValueField);
		if (procedureKind.isSurveyEvaluate()) {
			resultExpression.add(getExpressionIfExistField(quaRefusalLimitValueField,
				Expression.IF(
					Expression.and(
						Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
						Expression.eq(new VarAccessExpression(allowRefusalValue), bool(true)),
						Expression.neq(
							new VarAccessExpression(quaRefusalLimitValue),
							string(PrCategoryQuaRefusalLimit.not_limited.getValue()))),
					block(makeRequired(quaRefusalLimitValueField), show(quaRefusalLimitValueField)),
					block(makeOptional(quaRefusalLimitValueField), hide(quaRefusalLimitValueField))
				)));
		} else {
			resultExpression.add(getExpressionIfExistField(quaRefusalLimitValueField, hide(quaRefusalLimitValueField)));
		}
		return resultExpression;
	}

	private static Expression getIndicatorRefusalLimitExpression(ProcedureKind procedureKind) {
		BlockExpression resultExpression = new BlockExpression();

		VarExpression isQuaValue = new VarExpression(
			"value" + IS_QUA, new CheckFieldExpression(IS_QUA).getValue());
		resultExpression.add(isQuaValue);
		VarExpression allowRefusalValue = new VarExpression(
			"value" + ALLOW_QUA_REFUSAL, new CheckFieldExpression(ALLOW_QUA_REFUSAL).getValue());
		resultExpression.add(allowRefusalValue);
		VarExpression indicatorRefusalLimitValue = new VarExpression(
			"value" + INDICATOR_REFUSAL_LIMIT, new ComboBoxFieldExpression(INDICATOR_REFUSAL_LIMIT).objId());
		resultExpression.add(indicatorRefusalLimitValue);

		VarAccessExpression indicatorRefusalLimitVarAccess = new VarAccessExpression(indicatorRefusalLimitValue);

		Expression valueUnitShowCondition = Expression.and(
			Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
			Expression.eq(new VarAccessExpression(allowRefusalValue), bool(true)),
			Expression.or(
				Expression.eq(
					indicatorRefusalLimitVarAccess, string(PrCategoryIndicatorRefusalLimit.by_competence.getValue())),
				Expression.eq(
					indicatorRefusalLimitVarAccess, string(PrCategoryIndicatorRefusalLimit.by_survey.getValue()))
			)
		);
		Stream.of(
			INDICATOR_REFUSAL_LIMIT_VALUE,
			INDICATOR_REFUSAL_LIMIT_UNIT)
			.forEach(field -> {
				VarExpression fieldExp = new VarExpression("field" + field, new FieldExpression(field));
				resultExpression.add(fieldExp);
				if (procedureKind.isSurveyEvaluate()) {
					resultExpression.add(getExpressionIfExistField(
						fieldExp,
						Expression.IF(
							valueUnitShowCondition,
							block(makeRequired(fieldExp), show(fieldExp)),
							block(makeOptional(fieldExp), hide(fieldExp))
						))
					);
				} else {
					resultExpression.add(
						getExpressionIfExistField(fieldExp, hide(fieldExp)));
				}
			});

		Expression quaCategoryShowCondition = Expression.and(
			Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
			Expression.eq(new VarAccessExpression(allowRefusalValue), bool(true)),
			Expression.eq(
				indicatorRefusalLimitVarAccess,
				string(PrCategoryIndicatorRefusalLimit.by_competence_category.getValue()))
		);

		VarExpression quaCategoryFieldExp = new VarExpression(
			"field" + INDICATOR_REFUSAL_LIMIT_BY_QUA_CATEGORY,
			new FieldExpression(INDICATOR_REFUSAL_LIMIT_BY_QUA_CATEGORY));
		resultExpression.add(quaCategoryFieldExp);
		if (procedureKind.isSurveyEvaluate()) {
			resultExpression.add(getExpressionIfExistField(
				quaCategoryFieldExp,
				Expression.IF(
					quaCategoryShowCondition,
					show(quaCategoryFieldExp),
					hide(quaCategoryFieldExp)
				))
			);
		} else {
			resultExpression.add(
				getExpressionIfExistField(quaCategoryFieldExp, hide(quaCategoryFieldExp)));
		}

		return resultExpression;
	}

	private static Expression getNotCancelCompletedSurveyByArchivePersonExpression() {
		return getExpressionByProcedureKind(procedureKind ->
			procedureKind.isSurveyEvaluate() ?
				CheckExpressionBuilder.createExpressionForShowFields(COMPLETION_SURVEY_METHOD) :
				getHideFieldExpression(COMPLETION_SURVEY_METHOD));
	}

	private static Expression getQuaFormulaExpression() {
		BlockExpression resultExpression = new BlockExpression();

		VarExpression isQuaValue = new VarExpression(
			"value" + IS_QUA, new CheckFieldExpression(IS_QUA).getValue());
		resultExpression.add(isQuaValue);
		VarExpression formulaTypeValue = new VarExpression(
			"value" + FORMULA_QUA_TYPE, new ComboBoxFieldExpression(FORMULA_QUA_TYPE).objId());
		resultExpression.add(formulaTypeValue);

		FormulaFieldBuilder.addExpressionShowOrHideFormula(
			resultExpression,
			Expression.and(
				Expression.eq(new VarAccessExpression(isQuaValue), bool(true)),
				Expression.eq(
					new VarAccessExpression(formulaTypeValue), string(PrCategoryQuaFormulaType.individual.getValue()))),
			FORMULA_QUA);
		return resultExpression;
	}

	private static Expression getHideFieldExpression(String fieldName) {
		BlockExpression expression = new BlockExpression();
		VarExpression fieldExp = new VarExpression("field" + fieldName, new FieldExpression(fieldName));
		expression.add(fieldExp);
		expression.add(getExpressionIfExistField(fieldExp, hide(fieldExp)));
		return expression;
	}

	private static <T extends Expression>LazyExpression<T> getExpressionByProcedureKind(
			Function<ProcedureKind, T> createExpression) {
		return new LazyExpression<T>() {
			@Override
			protected T createExpression() {
				ProcedureKind procedureKind =
					ProcedureKindStore.getInstance().getKind(Context.get().getDataParameter(ProcedureBean.KIND));
				return createExpression.apply(procedureKind);
			}
		};
	}

    public void fillDefaultByProcedure(ProcedureBean procedure) {
		Stream.of(ProcedureAssessmentBlock.values())
				.forEach(block -> block.setIsUseCategory(this, block.isUseProcedure(procedure)));
        setIsQuaCategory(procedure.getAdditional().getSurveyEstimateQuaCategory());
    }

    /**
     * Категория оценивает хотя бы один блок процедуры
     */
    public boolean isEstimate(ProcedureBean procedure) {
        return Stream.of(ProcedureAssessmentBlock.values())
				.anyMatch(block -> block.isUseProcedure(procedure) && block.isUseCategory(this));
    }

    public List<PersonForAssignEstimatorBean> getFilteredEstimators(Collection<PersonForAssignEstimatorBean> persons) {
    	return getFilteredEstimators(persons, false);
	}

	public List<PersonForAssignEstimatorBean> getFilteredEstimators(Collection<PersonForAssignEstimatorBean> persons,
																	boolean assignArchive) {
		return getFilteredPersons(persons, getAssignEstimatorMaternityLeave(), assignArchive);
	}

	public List<PersonForAssignEstimatorBean> getFilteredCandidates(Collection<PersonForAssignEstimatorBean> persons) {
		return getFilteredPersons(persons, getAssignCandidateMaternityLeave(), false);
	}

	public List<PersonBean> getFilteredPersonCandidates(Collection<PersonBean> persons) {
		return getFilteredPersons(
			persons,
			PersonBean::getStatus,
			PersonBean::getOnMaternityLeave,
			getAssignCandidateMaternityLeave(),
			false);
	}

	/**
	 * @param assignMaternityLeave Назначать анкеты для оценивающих в декретном отпуске
	 * @param assignArchive Назначать анкеты для пользователей в архиве
	 */
	private List<PersonForAssignEstimatorBean> getFilteredPersons(Collection<PersonForAssignEstimatorBean> persons,
																  boolean assignMaternityLeave,
																  boolean assignArchive) {
		return getFilteredPersons(
			persons,
			PersonForAssignEstimatorBean::getStatus,
			PersonForAssignEstimatorBean::getOnMaternityLeave,
			assignMaternityLeave,
			assignArchive);
	}

	/**
	 * @param assignMaternityLeave Назначать анкеты для оценивающих в декретном отпуске
	 * @param assignArchive Назначать анкеты для пользователей в архиве
	 */
	private <T extends AbstractReflectDataBean>List<T> getFilteredPersons(Collection<T> persons,
																		  Function<T, PersonStatus> statusGetter,
																		  Function<T, Boolean> onMaternityLeaveGetter,
																		  boolean assignMaternityLeave,
																		  boolean assignArchive) {
		if (CollectionUtils.isEmpty(persons)) {
			return Collections.emptyList();
		}
		return persons.stream()
			.filter(person ->
				(assignArchive || statusGetter.apply(person).isActive()) &&
				(assignMaternityLeave || !onMaternityLeaveGetter.apply(person)))
			.collect(Collectors.toList());
	}

	public boolean isRequiredQuaComment() {
    	return getCommentQua().isRequired();
	}

	public boolean isRequiredIndComment(Double score) {
		return getCommentInd().isRequired(getCommentIndScoreConditions().getEntries(), score);
	}

	public boolean isErrorIndCommentLength(Double score, String comment) {
		if (getCommentInd().isOff() ||
				getCommentIndLengthLimit() == PrCategoryScoreCommentLengthLimit.not_limit ||
				IntHelper.isNull(getCommentIndMinLength())) {
			return false;
		}
		if (getCommentIndLengthLimit() == PrCategoryScoreCommentLengthLimit.all) {
			return StringHelper.isNotEmpty(comment) && comment.length() < getCommentIndMinLength();
		}
		if (getCommentIndLengthLimit() == PrCategoryScoreCommentLengthLimit.compulsory && isRequiredIndComment(score)) {
			return StringHelper.isEmpty(comment) || comment.length() < getCommentIndMinLength();
		}
		return false;
	}
}
