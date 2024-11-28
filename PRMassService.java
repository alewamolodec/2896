package lms.core.newprocedure.assignment.mass;

import lms.core.newprocedure.ProcedureBean;
import lms.core.newprocedure.ProcedureMessage;
import lms.core.newprocedure.ProcedureService;
import lms.core.newprocedure.category.EstimatingCategory;
import lms.core.newprocedure.category.PRCategoryBean;
import lms.core.newprocedure.category.PRCategoryService;
import lms.core.newprocedure.category.ProcedureEstimatingCategory;
import lms.core.newprocedure.category.set.PrCategoryInSetBean;
import lms.core.newprocedure.category.set.PrCategorySetBean;
import lms.core.newprocedure.member.PRMemberBean;
import lms.core.newprocedure.route.ProcedureRouteObject;
import lms.core.newprocedure.route.phasedate.ProcedurePhaseDateBean;
import lms.core.newprocedure.survey.PRSurveyBean;
import lms.core.newprocedure.survey.PRSurveyService;
import lms.route.object.RouteObjService;
import org.mirapolis.data.DataSet;
import org.mirapolis.data.DataSetHelper;
import org.mirapolis.data.bean.BeanHelper;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.db.Session;
import org.mirapolis.db.SessionRunnable;
import org.mirapolis.exception.CoreException;
import org.mirapolis.exception.LogicErrorException;
import org.mirapolis.mvc.model.grid.VirtualDataSource;
import org.mirapolis.mvc.model.grid.bean.BeanDataSetList;
import org.mirapolis.mvc.model.table.NameColumn;
import org.mirapolis.orm.EntityManager;
import org.mirapolis.orm.ORM;
import org.mirapolis.service.ServiceFactory;
import org.mirapolis.service.message.Localized;
import org.mirapolis.service.message.LocalizedMessage;
import org.mirapolis.sql.Pager;
import org.mirapolis.sql.QueryData;
import org.mirapolis.sql.fragment.SelectQuery;
import org.mirapolis.util.CollectionUtils;
import org.mirapolis.util.IntHelper;
import org.mirapolis.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Массовое назначение
 *
 * @author Elena Puzakova
 * @since 22.05.12 12:40
 */
@Service
public class PRMassService {
	public static final String MEMBER_VGRID = "massmembers";
	public static final String SURVEY_VGRID = "masssurveys";
    public static final String SURVEY_KEY_VGRID = "masssurveykey";

	private final ProcedureService procedureService;
	private final PRSurveyService surveyService;
	private final PRCategoryService categoryService;
    private final RouteObjService routeObjService;
    private final ORM orm;
    private final ProcedureSurveyAssignRepository surveyAssignRepository;

	@Autowired
	public PRMassService(ProcedureService procedureService,
                         PRSurveyService surveyService,
                         PRCategoryService categoryService,
                         RouteObjService routeObjService,
                         ORM orm,
                         ProcedureSurveyAssignRepository surveyAssignRepository) {
		this.procedureService = procedureService;
        this.surveyService = surveyService;
        this.categoryService = categoryService;
        this.routeObjService = routeObjService;
        this.orm = orm;
        this.surveyAssignRepository = surveyAssignRepository;
    }

	public static PRMassService getInstance() {
        return ServiceFactory.getService(PRMassService.class);
    }

    public List<PersonForAssignEstimatorBean> getPersonsForAssignEstimatorByIds(ProcedureSurveyAssignMode assignMode,
                                                                                Collection<String> personIds) {
        return CollectionUtils.isEmpty(personIds) ?
            Collections.emptyList() :
            EntityManager.listVirtual(
                assignMode.updatePersonQueryData(surveyAssignRepository.getPersonsByIds(personIds)),
                PersonForAssignEstimatorBean.class);
    }
    /**
     * @return id карты - оценивающие
     */
    public Map<String, List<PersonForAssignEstimatorBean>> getPersonEstimatorsByMemberId(
            QueryData<SelectQuery> queryData,
            ProcedureSurveyAssignMode assignMode) {
        //id карты - оценивающие
        Map<String, List<PersonForAssignEstimatorBean>> personsByMemberId = new HashMap<>();
        List<DataSet> dataSets = Session.list(assignMode.updatePersonQueryData(queryData));
        dataSets.forEach(dataSet -> {
            String memberId = dataSet.getValue(PRMemberBean.ID);
            PersonForAssignEstimatorBean person =
                BeanHelper.createFromDBDataSet(PersonForAssignEstimatorBean.class, dataSet);
            CollectionUtils.getOrCreate(personsByMemberId, memberId, new ArrayList<>()).add(person);
        });
        return personsByMemberId;
    }

	/**
	 * Заполнение списка оцениваемых
     */
	public void fillMassMembers(Collection<String> memberIds) {
        fillMassMembers(EntityManager.list(PRMemberBean.class, memberIds));
	}

    public void fillMassMembers(List<PRMemberBean> members) {
	    members.forEach(this::addMemberToVDS);
    }

    private void addMemberToVDS(PRMemberBean member) {
        VirtualDataSource vds = VirtualDataSource.get(MEMBER_VGRID);
        List<DataSet> records = vds.getRecords();
        for (DataSet ds : records) {
            if (member.getId().equals(ds.getValue(PRMemberBean.ID))) {
                return;
            }
        }
        DataSet ds = member.get();
        ds.putValue(NameColumn.COLUMN_NAME, member.getPerson().getName());
        ds.putValue(BeanDataSetList.ID, member.getId());
        vds.addRecord(ds);
    }

	/**
	 * Добавление пользователя как категории
	 */
	public void fillMassSurveysByPersons(PRCategoryBean category,
                                         List<PersonForAssignEstimatorBean> estimators,
                                         NameBean expertCategory) {
	    Map<String, List<PersonForAssignEstimatorBean>> estimatorsByMemberId = new HashMap<>();
	    getMembers().forEach(member -> estimatorsByMemberId.put(member.getId(), estimators));
	    fillMassSurveys(category, estimatorsByMemberId, expertCategory);
	}

    /**
     * Заполнение анкет по втоназначение по процедуре для выбранных карт
     */
	public void fillMassAutoSurveysByProcedure(String procedureId) {
        ProcedureBean procedure = procedureService.getProcedureWithCategories(procedureId);
        List<PRCategoryBean> categories = procedure.getCategories().stream()
            .filter(category ->
                category.getIsUse() &&
                category.getCategory().isAutoAssign(category) &&
                categoryService.isAccessEditCategorySurveys(category.getId(), procedure))
            .collect(Collectors.toList());
        Set<String> categoryIds = BeanHelper.getIdSet(categories);
        List<PrCategorySetBean> categorySets = procedure.getCategorySets().stream()
            .filter(PrCategorySetBean::getNotAssignSurveySamePerson)
            .collect(Collectors.toList());

        Set<String> processedCategoryIds = new HashSet<>();

        StringBuilder errors = new StringBuilder();
        //Назначение по категориям из наборов по порядку, указанному в наборе
        categorySets.forEach(categorySet ->
            categorySet.getCategories().getEntries().stream()
                .filter(categoryInSet -> categoryIds.contains(categoryInSet.getCategory().getId()))
                .sorted(Comparator.comparing(PrCategoryInSetBean::getOrder))
                .forEach(categoryInSet -> {
                    try {
                        fillMassAutoSurveysByCategory(
                            procedure, BeanHelper.getBeanById(categories, categoryInSet.getCategory().getId()));
                    } catch (LogicErrorException e) {
                        addMessage(errors, e.getMessage());
                    }
                    processedCategoryIds.add(categoryInSet.getCategory().getId());
                }));
        //Назначение по категориям без наборов
        categories.stream()
            .filter(category -> !processedCategoryIds.contains(category.getId()))
            .forEach(category -> {
                try {
                    fillMassAutoSurveysByCategory(procedure, category);
                } catch (LogicErrorException e) {
                    addMessage(errors, e.getMessage());
                }
            });
        if (errors.length() > 0) {
            throw new LogicErrorException(errors.toString());
        }
    }

	/**
	 * Добавление анкет по категории при автоназначении
	 */
	public void fillMassAutoSurveysByCategory(ProcedureBean procedure, PRCategoryBean category) {
        List<PRMemberBean> members = getMembers();
		if (!category.getIsUse() || members.isEmpty()) {
		    return;
        }
        Map<String, List<PersonForAssignEstimatorBean>> categoryEstimators = categoryService.getAutoAssignEstimators(
            members, procedure, category, ProcedureSurveyAssignMode.manual);
        fillMassSurveys(category, categoryEstimators, new NameBean());
    }

    /**
     * Заполнение списка анкет
     * @param estimatorsByMemberId - {id карты - оценивающие по категории category}
     */
    public void fillMassSurveys(PRCategoryBean category,
                                Map<String, List<PersonForAssignEstimatorBean>> estimatorsByMemberId) {
        fillMassSurveys(category, estimatorsByMemberId, new NameBean());
    }

    /**
     * Заполнение списка анкет
     * @param estimatorsByMemberId - {id карты - оценивающие по категории category}
     */
    private void fillMassSurveys(PRCategoryBean category,
                                 Map<String, List<PersonForAssignEstimatorBean>> estimatorsByMemberId,
                                 NameBean expertCategory) {
        StringBuilder errors = new StringBuilder();
        getMembers().stream()
            .filter(member ->
                !category.getCategory().isSurveyWithPerson() || estimatorsByMemberId.containsKey(member.getId()))
            .forEach(member ->
                fillMassSurveysForOneEstimated(
                    errors, category, member, estimatorsByMemberId.get(member.getId()), expertCategory));
        if (errors.length() > 0) {
            throw new LogicErrorException(errors.toString());
        }
    }

    private void fillMassSurveysForOneEstimated(StringBuilder errors,
                                                PRCategoryBean category,
                                                PRMemberBean member,
                                                List<PersonForAssignEstimatorBean> personEstimators,
                                                NameBean expertCategory) {
        VirtualDataSource surveyVds = VirtualDataSource.get(SURVEY_VGRID);
        if (!category.getCategory().isSurveyWithPerson()) {
            if (!isExistSurveyKey(member.getId(), null, category)) {
                addSurveyToDataSource(surveyVds, new PRMassSurveyBean().setValues(member, category), category);
            }
            return;
        }
        for (PersonForAssignEstimatorBean estimator : category.getFilteredEstimators(personEstimators)) {
            if (!isExistSurveyKey(member.getId(), estimator.getId(), category)) {
                if (checkEstimatorByCategorySets(estimator, member, category, errors)) {
                    if (checkMaxSurveyForPersonByCategory(estimator.getId(), category)) {
                        addSurveyToDataSource(
                            surveyVds,
                            new PRMassSurveyBean().setValues(member, estimator, category, expertCategory),
                            category);
                    } else {
                        addMessage(
                            errors,
                            ProcedureMessage.error_max_survey_by_category,
                            member.getPerson().getName(),
                            estimator.getName(),
                            category.getCategory().getCaption().toString());
                    }
                }
            }
        }
    }

    private void addSurveyToDataSource(VirtualDataSource dataSource, PRMassSurveyBean survey, PRCategoryBean category) {
        dataSource.addRecord(survey);
        VirtualDataSource.get(SURVEY_KEY_VGRID)
            .addRecord(DataSetHelper.createDataSet(
                BeanDataSetList.ID,
                getSurveyKey(category, survey.getCategoryId(), survey.getEstimatedMemberId(), survey.getEstimatorId())));
    }

    private String getSurveyKey(PRCategoryBean category,
                                String surveyCategoryId,
                                String surveyMemberId,
                                String surveyEstimatorId) {
        return surveyCategoryId + StringHelper.UNDERSCORE +
            surveyMemberId +
            (isCheckEstimatorId(category) ? StringHelper.UNDERSCORE + surveyEstimatorId : StringHelper.EMPTY_STRING);
    }

    /**
     * Для категории проверять оценивающего при поиске существующей анкеты
     */
    private boolean isCheckEstimatorId(PRCategoryBean category) {
        ProcedureEstimatingCategory estimatingCategory = category.getCategory();
        if (EstimatingCategory.GROUP.isEquals(estimatingCategory)) {
            //Для группы одна анкета, но при массовом назначении выбор пользователей, нужно вывести несколько строк
            return true;
        }
        return estimatingCategory.isSurveyWithPerson() && !estimatingCategory.isOneSurvey();
    }

    /**
     * Проверить оценивающего по наборам категорий, для которых включено "Не назначать анкеты для одного физического лица"
     */
    private boolean checkEstimatorByCategorySets(PersonForAssignEstimatorBean estimator,
                                                 PRMemberBean member,
                                                 PRCategoryBean category,
                                                 StringBuilder errors) {
        if (!category.getCategory().withSurveys() || !category.getCategory().isSurveyWithPerson()) {
            return true;
        }
        ProcedureBean procedure = procedureService.getProcedureWithCategories(category.getProcedure().getId());
        List<PRCategoryBean> categories = procedure.getCategories();
        //Наборы, в которых есть категория и не назначать анкеты по одному пользователю
        List<PrCategorySetBean> categorySets = procedure.getCategorySets().stream()
                .filter(categorySet -> categorySet.getNotAssignSurveySamePerson() &&
                        BeanHelper.getNameBeanIdSet(
                                categorySet.getCategories().getEntries(), PrCategoryInSetBean.CATEGORY_ID)
                                .contains(category.getId()))
                .collect(Collectors.toList());
        if (categorySets.isEmpty()) {
            return true;
        }
        return categorySets.stream().allMatch(categorySet -> {
            Set<String> categoryIds = BeanHelper.getNameBeanIdSet(
                    categorySet.getCategories().getEntries(), PrCategoryInSetBean.CATEGORY_ID);
            //Проверить добавленных оценивающих
            Optional<PRMassSurveyBean> massSurvey = getSurveys().stream()
                    .filter(survey -> survey.getEstimatedMemberId().equals(member.getId()) &&
                            estimator.getId().equals(survey.getEstimatorId()) &&
                            categoryIds.contains(survey.getCategoryId()))
                    .findFirst();
            if (massSurvey.isPresent()) {
                addMessage(
                        errors,
                        ProcedureMessage.exist_survey_with_same_person,
                        member.getPerson().getName(),
                        estimator.getName(),
                        BeanHelper.getBeanById(categories, massSurvey.get().getCategoryId()).getCategoryName(),
                        category.getCategoryName());
                return false;
            }
            //Проверить анкеты карты
            List<PRSurveyBean> surveys = surveyService.getActiveMemberSurveysByPersonAndCategory(
                    member.getId(), estimator.getId(), categoryIds);
            if (!surveys.isEmpty()) {
                surveys.forEach(survey -> addMessage(
                        errors,
                        ProcedureMessage.exist_survey_with_same_person,
                        member.getPerson().getName(),
                        estimator.getName(),
                        BeanHelper.getBeanById(categories, survey.getCategory().getId()).getCategoryName(),
                        category.getCategoryName()));
                return false;
            }
            return true;
        });
    }

    /**
     * Проверка максимального кол-ва анкет для оценивающего по категории
     */
    public boolean checkMaxSurveyForPersonByCategory(String personId, PRCategoryBean category) {
        if (category.getCategory().isOneSurvey() || IntHelper.isNull(category.getMaxSurveyCount())) {
            return true;
        }
        List<PRMassSurveyBean> surveys = getSurveys();
        int surveyCount = 0;
        for (PRMassSurveyBean survey : surveys) {
            if (survey.getEstimatorId().equals(personId) && survey.getCategoryId().equals(category.getId())) {
                surveyCount ++;
            }
        }
        return (surveyCount + PRCategoryService.getCountSurveyForPersonAndCategory(category, personId)) <
                category.getMaxSurveyCount();
    }
    
    public void autoAssignCategorySurveys(Collection<PRMemberBean> members,
                                          List<PRMassSurveyBean> surveys,
                                          PRCategoryBean category,
                                          ProcedureBean procedure,
                                          boolean allowArchive) {
        //Оценивающие
        Map<String, List<PersonForAssignEstimatorBean>> memberEstimators = categoryService.getAutoAssignEstimators(
                members, procedure, category, ProcedureSurveyAssignMode.auto, allowArchive);
        members.forEach(member ->
            autoAssignCategorySurveys(member, surveys, memberEstimators.get(member.getId()), category, allowArchive));
    }

    private void autoAssignCategorySurveys(PRMemberBean member,
                                           List<PRMassSurveyBean> surveys,
                                           List<PersonForAssignEstimatorBean> estimators,
                                           PRCategoryBean category,
                                           boolean allowArchive) {
        if (!category.getCategory().isSurveyWithPerson()) {
            if (!isExistSurvey(surveys, member.getId(), null, category)) {
                surveys.add(new PRMassSurveyBean().setValues(member, category));
            }
        } else {
            category.getFilteredEstimators(estimators, allowArchive).stream()
                .filter(estimator -> !isExistSurvey(surveys, member.getId(), estimator.getId(), category))
                .forEach(estimator -> surveys.add(
                    new PRMassSurveyBean().setValues(member, estimator, category).setAllowArchive(allowArchive))
                );
        }
    }

    public void autoAssignCategorySurveys(PRMemberBean member,
                                          List<PRMassSurveyBean> surveys,
                                          List<PersonForAssignEstimatorBean> estimators,
                                          PRCategoryBean category) {
        autoAssignCategorySurveys(member, surveys, estimators, category, false);
    }

    public boolean isExistSurvey(String dataSource, String memberId, String estimatorId, PRCategoryBean category) {
        return isExistSurvey(getSurveys(dataSource), memberId, estimatorId, category);
    }

	/**
	 * Добавлена анкета для пользователя
	 *
	 * @param surveys все анкеты
	 * @param memberId оцениваемый
	 * @param category категория
	 */
	public boolean isExistSurvey(List<PRMassSurveyBean> surveys,
                                 String memberId,
                                 String estimatorId,
                                 PRCategoryBean category) {
	    if (surveys.isEmpty()) {
            return false;
        }
       return surveys.stream()
            .map(survey -> {
                //оцениваемый анкеты
                String estimatedMemberId = survey.getEstimatedMemberId();
                //Оценивающий анкеты
                String surveyEstimatorId = survey.getEstimatorId();
                //категория анкеты
                String surveyCategoryId = survey.getCategoryId();
                return surveyCategoryId.equals(category.getId()) && memberId.equals(estimatedMemberId) &&
                    (!isCheckEstimatorId(category) || estimatorId.equals(surveyEstimatorId));
            }).findFirst()
            .orElse(false);
	}

    public boolean isExistSurveyKey(String memberId, String estimatorId, PRCategoryBean category) {
        Set<String> surveyKeys = DataSetHelper.getValuesSet(
            VirtualDataSource.get(SURVEY_KEY_VGRID).getRecords(), BeanDataSetList.ID);
        if (surveyKeys.isEmpty()) {
            return false;
        }
        String key = getSurveyKey(category, category.getId(), memberId, estimatorId);
        return surveyKeys.contains(key);
    }

	private void addMessage(StringBuilder sb, LocalizedMessage pattern, String... args)  {
        addMessage(sb, Localized.format(pattern, args).toString());
    }

    private void addMessage(StringBuilder sb, String message)  {
        if (sb.length() > 0) {
            sb.append("</br>");
        }
        sb.append(message);
    }

    public List<PRMemberBean> getMembers() {
        return BeanHelper.toList(VirtualDataSource.get(MEMBER_VGRID).getRecords(), PRMemberBean.class);
    }

    public List<PRMemberBean> getDbMembers() {
        return EntityManager.list(PRMemberBean.class, BeanHelper.getIdSet(getMembers()));
    }

    public List<PRMassSurveyBean> getSurveys() {
	    return getSurveys(SURVEY_VGRID);
    }

    public List<PRMassSurveyBean> getSurveys(String dataSource) {
        return BeanHelper.toList(VirtualDataSource.get(dataSource).getRecords(), PRMassSurveyBean.class);
    }

    /**
     * Сохранение анкет
     */
    public void saveMassAssignment(String procedureId, Set<String> memberIds, List<PRMassSurveyBean> surveys) {
        CollectionUtils.split(memberIds, Pager.DEFAULT_MAX_ON_PAGE)
            .forEach(ids -> saveMassAssignment(
                procedureId, EntityManager.list(PRMemberBean.class, ids), surveys, Collections.emptyList()));
    }

    public void saveMassAssignment(List<ProcedurePhaseDateBean> procedurePhases,
                                   Collection<PRMemberBean> members,
                                   List<PRMassSurveyBean> surveys) {
        saveMassAssignment(procedurePhases, members, surveys, Collections.emptyList());
    }

    public void saveMassAssignment(String procedureId,
                                   Collection<PRMemberBean> members,
                                   List<PRMassSurveyBean> surveys,
                                   Collection<ProcedureEstimatingCategory> allowArchiveCategories) {
        List<ProcedurePhaseDateBean> procedurePhases = ProcedurePhaseDateBean.list(
            procedureId, new ProcedurePhaseDateBean());
        saveMassAssignment(procedurePhases, members, surveys, allowArchiveCategories);
    }

    private void saveMassAssignment(List<ProcedurePhaseDateBean> procedurePhases,
                                   Collection<PRMemberBean> allMembers,
                                   List<PRMassSurveyBean> surveys,
                                   Collection<ProcedureEstimatingCategory> allowArchiveCategories) {
		Session.runInDefaultSession(
		    orm.getDataObject(ProcedureBean.class).getDatabase(),
            new SessionRunnable() {
		        @Override
                public void run(Session session) throws CoreException {
		            //Карты, для которых есть новые анкеты
                    List<PRMemberBean> membersWithSurvey = allMembers.stream()
                        .filter(member ->
                            surveys.stream().anyMatch(survey -> survey.getEstimatedMemberId().equals(member.getId())))
                        .collect(Collectors.toList());
                    for (List<PRMemberBean> members :
                        CollectionUtils.split(membersWithSurvey, Pager.INCREASED_MAX_ON_PAGE)) {
                        Map<String, String> currentPhaseIds = routeObjService.getObjectCurrentPhaseIds(
                            ProcedureRouteObject.TYPE, BeanHelper.getIdSet(members));
                        members.forEach(member -> {
                            MassMemberSaver saver = new MassMemberSaver(
                                member,
                                procedurePhases,
                                currentPhaseIds.get(member.getId()))
                                .setAllowArchiveCategories(allowArchiveCategories);
                            surveys.stream()
                                .filter(survey -> survey.getEstimatedMemberId().equals(member.getId()))
                                .forEach(survey ->
                                    saver.addSurvey(
                                        PRMassSurveyBean.EMPTY_ESTIMATOR_ID.equals(survey.getEstimatorId()) ?
                                            null : survey.getEstimatorId(),
                                        survey.getCategoryId(),
                                        survey.getExpertCategoryId())
                                );
                            saver.save();
                        });
                    }
                }
            }
        );
    }
}
