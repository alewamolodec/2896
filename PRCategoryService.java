package lms.core.newprocedure.category;

import lms.core.access.filter.FilterServiceFactory;
import lms.core.ca.post.PostFrame;
import lms.core.newprocedure.ProcedureBean;
import lms.core.newprocedure.ProcedureRepository;
import lms.core.newprocedure.ProcedureSQL;
import lms.core.newprocedure.ProcedureService;
import lms.core.newprocedure.assignment.mass.PRMassService;
import lms.core.newprocedure.assignment.mass.PersonForAssignEstimatorBean;
import lms.core.newprocedure.assignment.mass.ProcedureSurveyAssignMode;
import lms.core.newprocedure.assignment.mass.ProcedureSurveyAssignRepository;
import lms.core.newprocedure.category.assignment.PRCategoryAssignBean;
import lms.core.newprocedure.category.assignment.PRCategoryAssignEstimatorBean;
import lms.core.newprocedure.category.assignment.PrCategoryAssignEstimatorType;
import lms.core.newprocedure.category.autoassignment.PrCategoryMultiAutoAssignment;
import lms.core.newprocedure.category.qua.PRCategoryIndicatorBean;
import lms.core.newprocedure.category.qua.PRCategoryQuaBean;
import lms.core.newprocedure.category.qua.PRCategoryQuaCategoryBean;
import lms.core.newprocedure.category.qua.PRCategoryQuaComparisonBean;
import lms.core.newprocedure.category.set.PrCategorySetBean;
import lms.core.newprocedure.category.viewresult.CategoryViewResultType;
import lms.core.newprocedure.category.viewresult.PrCategoryViewResultBean;
import lms.core.newprocedure.expert.PRExpertBean;
import lms.core.newprocedure.member.PRMemberBean;
import lms.core.newprocedure.member.PRMemberService;
import lms.core.newprocedure.member.qua.PRMemberQuaBean;
import lms.core.newprocedure.qua.AbstractScoreBean;
import lms.core.newprocedure.qua.PRCategorySNCountBean;
import lms.core.newprocedure.route.ProcedureRouteObject;
import lms.core.newprocedure.route.phasedate.ProcedurePhaseDateBean;
import lms.core.newprocedure.settings.ProcedureCategoryCaptionBean;
import lms.core.newprocedure.survey.*;
import lms.core.newprocedure.surveycandidate.CategoryRecommendedCondition;
import lms.core.newprocedure.surveycandidate.PRSurveyCandidateBean;
import lms.core.newprocedure.surveycandidate.PRSurveyCandidateService;
import lms.core.person.PersonBean;
import lms.core.person.PersonFrame;
import lms.core.person.PersonGroupCatalog;
import lms.core.person.work.PersonSubordinationService;
import lms.core.qua.scale.ScaleService;
import lms.core.qua.scale.number.ScaleNumberBean;
import lms.route.object.RouteObjService;
import mira.groups.GroupCatalogService;
import org.mirapolis.core.Context;
import org.mirapolis.data.DataSet;
import org.mirapolis.data.bean.BeanHelper;
import org.mirapolis.data.bean.DoubleValue;
import org.mirapolis.db.Session;
import org.mirapolis.exception.CoreException;
import org.mirapolis.log.Log;
import org.mirapolis.mvc.model.SaveInfo;
import org.mirapolis.mvc.model.entity.*;
import org.mirapolis.orm.DataObject;
import org.mirapolis.orm.Entity;
import org.mirapolis.orm.EntityManager;
import org.mirapolis.orm.ORM;
import org.mirapolis.orm.paging.BeanQueryPagingIterator;
import org.mirapolis.service.ServiceFactory;
import org.mirapolis.sql.Pager;
import org.mirapolis.sql.QueryData;
import org.mirapolis.sql.fragment.*;
import org.mirapolis.util.CollectionUtils;
import org.mirapolis.util.Counter;
import org.mirapolis.util.IntHelper;
import org.mirapolis.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.mirapolis.orm.DataObject.PARENT_ALIAS;
import static org.mirapolis.sql.fragment.Column.column;
import static org.mirapolis.sql.fragment.Parameter.parameter;
import static org.mirapolis.sql.fragment.Table.table;

/**
 * @author Elena Puzakova
 * @since 18.05.12 11:39
 */
@Service
public class PRCategoryService {
    private final ProcedureService procedureService;
    private final PRSurveyService surveyService;
    private final ProcedureEstimatingCategoryStore categoryStore;
    private final PersonSubordinationService personSubordinationService;
    private final RouteObjService routeObjService;
    private final FilterServiceFactory filterServiceFactory;
    private final ORM orm;
    private final GroupCatalogService groupCatalogService;
    private final ProcedureRepository procedureRepository;
    private final ProcedureSurveyAssignRepository surveyAssignRepository;
    private final CategoryCaptionCache categoryCaptionCache;

    @Autowired
    public PRCategoryService(ProcedureService procedureService,
                             PRSurveyService surveyService,
                             ProcedureEstimatingCategoryStore categoryStore,
                             PersonSubordinationService personSubordinationService,
                             RouteObjService routeObjService,
                             FilterServiceFactory filterServiceFactory,
                             ORM orm,
                             GroupCatalogService groupCatalogService,
                             ProcedureRepository procedureRepository,
                             ProcedureSurveyAssignRepository surveyAssignRepository,
                             @Lazy CategoryCaptionCache categoryCaptionCache) {
        this.procedureService = procedureService;
        this.surveyService = surveyService;
        this.categoryStore = categoryStore;
        this.personSubordinationService = personSubordinationService;
        this.routeObjService = routeObjService;
        this.filterServiceFactory = filterServiceFactory;
        this.orm = orm;
        this.groupCatalogService = groupCatalogService;
        this.procedureRepository = procedureRepository;
        this.surveyAssignRepository = surveyAssignRepository;
        this.categoryCaptionCache = categoryCaptionCache;
    }

    public static PRCategoryService getInstance() {
        return ServiceFactory.getService(PRCategoryService.class);
    }

    public List<ProcedureCategoryCaptionBean> getCategoryCaptions() {
        return categoryCaptionCache.getCategoryCaptions();
    }

    public void clearCategoryCaptionCache() {
        categoryCaptionCache.clearCategoryCaptionCache();
    }

    /**
     * Оценивающая категория
     */
    public static EntityListener<PRCategoryBean> getCategoryListener() {
        EntityListener<PRCategoryBean> listener = new EntityListener<>();
		listener.setDeleteListener(new DeleteListener(PRCategoryFrame.NAME) {
			@Override
			protected void doDelete(Context context, EntityManager mgr, DeleteData data) throws CoreException {
				super.doDelete(context, mgr, data);
				ProcedureService.getInstance().clearProcedureCache();
			}
		});
        listener.setSaveListener(new SaveListener<PRCategoryBean>(PRCategoryFrame.NAME, PRCategoryBean.class) {
            @Override
            protected SaveInfo<PRCategoryBean> createSaveInfo(PRCategoryBean prCategoryBean) {
                return new SaveInfo<>(prCategoryBean.getId(), getType(), prCategoryBean.getCategory().getCaption(), false);
            }

            protected EntitySessionListener<PRCategoryBean> getSessionListener() {
                return new EntitySessionListener<PRCategoryBean>() {
                    public void beforeSessionCommit(Session session) throws CoreException {
                        super.beforeSessionCommit(session);
						ProcedureService.getInstance().clearProcedureCache();
                    }
                };
            }
        }).addLinkListener(new LinkListener(PersonFrame.NAME) {//Добавление эксперта - пользователь
            @Override
            protected void doLink(Context context, EntityManager mgr, LinkData data) throws CoreException {
                PRExpertBean bean = new PRExpertBean();
                if (data.isInclude()) {
                    bean.getPerson().setId(data.getLinkId());
                    bean.setCategoryId(data.getId());
                    mgr.insertBean(bean, true, Entity.SELECT_INSERT);
                } else {
                    bean.setId(data.getLinkId());
                    mgr.deleteBean(bean, true);
                }
            }
        }).addLinkListener(new LinkListener(PostFrame.NAME) { //Добавление эксперта - должность
            @Override
            protected void doLink(Context context, EntityManager mgr, LinkData data) throws CoreException {
                PRExpertBean bean = new PRExpertBean();
                if (data.isInclude()) {
                    bean.getPost().setId(data.getLinkId());
                    bean.setCategoryId(data.getId());
                    mgr.insertBean(bean, true, Entity.SELECT_INSERT);
                } else {
                    bean.setId(data.getLinkId());
                    mgr.deleteBean(bean, true);
                }
            }
        });
        listener.setChooseListener(new ChooseListenerAdapter(PRCategoryAllChooseListFrame.NAME));
        return listener;
    }

    private QueryData<SelectQuery> getCategoryQueryData() {
        return getCategoryQueryData(categoryStore.getCategories());
    }

    private QueryData<SelectQuery> getCategoryQueryData(Collection<ProcedureEstimatingCategory> categories) {
        SelectQuery query = orm.getDataObject(PRCategoryBean.DATANAME).getQueryWithLookups();
        query.where(Column.column(DataObject.PARENT_ALIAS, PRCategoryBean.CATEGORY)
                .in(NamedParameter.namedParameter(PRCategoryBean.CATEGORY).setHasBrackets()));
        return QueryData.fromQuery(query).withNamedDbComboValueParameter(PRCategoryBean.CATEGORY, categories);
    }

    private QueryData<SelectQuery> getCategoryByProcedureQueryData(Collection<String> procedureIds) {
        return getCategoryByProcedureQueryData(procedureIds, categoryStore.getCategories());
    }

    private QueryData<SelectQuery> getCategoryByProcedureQueryData(Collection<String> procedureIds,
                                                                   Collection<ProcedureEstimatingCategory> categories) {
        QueryData<SelectQuery> queryData = getCategoryQueryData(categories);
        queryData.getQuery().where(Column.column(DataObject.PARENT_ALIAS, PRCategoryBean.PROCEDURE_ID)
                .in(NamedParameter.namedParameter(PRCategoryBean.PROCEDURE_ID).setHasBrackets()));
        queryData.withNamedIdParameter(PRCategoryBean.PROCEDURE_ID, procedureIds);
        return queryData;
    }

    private QueryData<SelectQuery> getCategoryByMemberQueryData(String memberId,
                                                                Collection<ProcedureEstimatingCategory> categories) {
        QueryData<SelectQuery> queryData = getCategoryQueryData(categories);
        queryData.getQuery().getFromWhereClause().getJoins().pushJoin(
                Table.table(PRMemberBean.DATANAME, "M"),
                Column.column("M", PRMemberBean.PROCEDURE_ID)
                        .eq(Column.column(DataObject.PARENT_ALIAS, PRCategoryBean.PROCEDURE_ID)),
                Join.INNER_JOIN);
        queryData.getQuery().where(Column.column("M", PRMemberBean.ID).eq(Parameter.parameter()));
        queryData.addIntParam(memberId);
        return queryData;
    }

    /**
     * WHERE C.prccategory = ?
     */
    public void addCategoryCondition(QueryData<SelectQuery> queryData, ProcedureEstimatingCategory category) {
        String categoryAlias = queryData.getQuery().getFromWhereClause().getTableByName(PRCategoryBean.DATANAME).getAlias();
        queryData.getQuery().where(Column.column(categoryAlias, PRCategoryBean.CATEGORY)
                .eq(Parameter.parameter()));
        queryData.addIntParam(category.getValue());
    }

    public void addCategoryAdditionalCondition(QueryData<SelectQuery> queryData,
                                               ProcedureEstimatingCategory category,
                                               String additionalId) {
        String categoryAlias = queryData.getQuery().getFromWhereClause().getTableByName(PRCategoryBean.DATANAME).getAlias();
        queryData.getQuery().where(Column.column(categoryAlias, category.getFieldValueForCompare())
                .eq(Parameter.parameter()));
        queryData.addIntParam(additionalId);
    }

    /**
     * Добавление в запрос всех childDataObject категории
     */
    public QueryData<SelectQuery> addChildDataObjectsToCategoryQuery(QueryData<SelectQuery> queryData) {
        DataObject categoryDataObject = orm.getDataObject(PRCategoryBean.class);
        SelectQuery query = queryData.getQuery().copy();
        String categoryAlias = query.getFromWhereClause().getTableByName(PRCategoryBean.DATANAME).getAlias();
        Counter counter = new Counter();
        categoryDataObject.children().forEach(childDataObject -> {
            if (query.getFromWhereClause().getTableByName(childDataObject.getName()) == null) {
                String childAlias = "C" + counter.inc();
                query.leftJoin(childDataObject.getName(), childAlias)
                    .on(Column.column(categoryAlias, PRCategoryBean.ID)
                        .eq(Column.column(childAlias, PRCategoryBean.ID)));
                query.addValue(Column.all(childAlias));
            }
        });
        queryData.setQuery(query);
        return queryData;
    }

    public static PRCategoryBean getCategory(String id) {
        return EntityManager.get(id, PRCategoryBean.class);
    }

    public static PRCategoryBean getCategory(String procedureId, ProcedureEstimatingCategory category) {
        PRCategoryBean bean = new PRCategoryBean();
        bean.getProcedure().setId(procedureId);
        bean.setCategory(category);
	    return EntityManager.load(bean) ? bean : null;
    }

    public List<PRCategoryBean> getCategoriesByMember(String memberId, ProcedureEstimatingCategory category) {
        return EntityManager.list(
                getCategoryByMemberQueryData(memberId, Collections.singletonList(category)), PRCategoryBean.class);
    }

    public Optional<PRCategoryBean> getCategoryByMember(String memberId, ProcedureEstimatingCategory category) {
        return EntityManager.findOptional(
                getCategoryByMemberQueryData(memberId, Collections.singletonList(category)), PRCategoryBean.class);
    }

    public Optional<PRCategoryBean> getCategoryByMember(String memberId,
                                                        ProcedureEstimatingCategory category,
                                                        String additionalId) {
        QueryData<SelectQuery> queryData = getCategoryByMemberQueryData(memberId, Collections.singletonList(category));
        addCategoryAdditionalCondition(queryData, category, additionalId);
        return EntityManager.findOptional(queryData, PRCategoryBean.class);
    }

    public PRCategoryBean getCategoryBySurvey(String surveyId) {
		SelectQuery query = orm.getDataObject(PRCategoryBean.class).getQueryWithLookups();
		String surveyAlias = "S";
		query.pushJoinOnTopmostPosition(
		        new Join(table(PRSurveyBean.DATANAME, surveyAlias), Join.INNER_JOIN)
                        .setCondition(column(surveyAlias, PRSurveyBean.CATEGORY_ID)
                                .eq(column(PARENT_ALIAS, PRCategoryBean.ID))),
                PARENT_ALIAS)
             .where(column(surveyAlias, PRSurveyBean.ID).eq(parameter()));
        return EntityManager.list(QueryData.fromQuery(query).addIntParam(surveyId), PRCategoryBean.class).get(0);
    }
    /**
     * Оценивающие категории процедуры
     */
    public List<PRCategoryBean> getProcedureCategories(String procedureId) {
	    return EntityManager.list(
	            getCategoryByProcedureQueryData(Collections.singleton(procedureId)),
                PRCategoryBean.class);
    }

    public void sortCategories(List<PRCategoryBean> categories) {
        categories.sort(Comparator.comparing(bean -> StringHelper.defaultIfNull(bean.getCategoryName())));
        categories.sort(Comparator.comparing(bean -> bean.getCategory().getOrder()));
    }

    /**
     * Используемые категории оценивающих
     */
    public List<PRCategoryBean> getUsedProcedureCategories(String procedureId) {
        QueryData<SelectQuery> queryData =
                getCategoryByProcedureQueryData(Collections.singleton(procedureId));
        queryData.getQuery().where(
                Column.column(DataObject.PARENT_ALIAS, PRCategoryBean.IS_USE).eq(Constant.trueConst()));
	    return EntityManager.list(queryData, PRCategoryBean.class);
    }

    public List<PRCategoryBean> getProcedureCategories(String procedureId,
                                                       Collection<ProcedureEstimatingCategory> estimatingCategories) {
        return procedureService.getProcedureWithCategories(procedureId).getCategories().stream()
                .filter(category -> estimatingCategories.contains(category.getCategory()))
                .collect(Collectors.toList());
    }

    public List<PRCategoryBean> getProcedureCategories(Collection<String> procedureIds,
                                                       ProcedureEstimatingCategory category,
                                                       Optional<String> additionalIdOptional) {
        QueryData<SelectQuery> queryData = getCategoryByProcedureQueryData(procedureIds, Collections.singleton(category));
        if (additionalIdOptional.isPresent()) {
            addCategoryAdditionalCondition(queryData, category, additionalIdOptional.get());
        }
        return EntityManager.list(queryData, PRCategoryBean.class);
    }

    /**
     * Видимость чужих оценок для категории
     */
    public static List<PrCategoryViewResultBean> listCategoryViewMark(String categoryId, CategoryViewResultType type) {
        PrCategoryViewResultBean bean = new PrCategoryViewResultBean();
        bean.setCategoryId(categoryId);
        bean.setResultType(type);
        return EntityManager.list(bean);
    }

    /**
     * Видимость чужих результатов для категории по всем типам
     */
    public static List<PrCategoryViewResultBean> listCategoryViewAllTypeResult(String categoryId) {
        PrCategoryViewResultBean bean = new PrCategoryViewResultBean();
        bean.setCategoryId(categoryId);
        return EntityManager.list(bean);
    }

    /**
     * Подчиненные
     */
    public Map<String, List<PersonForAssignEstimatorBean>> listInferiors(Collection<PRMemberBean> members,
                                                                         boolean levelN,
                                                                         boolean levelN1,
                                                                         boolean levelAll,
                                                                         boolean byFunctional,
                                                                         boolean filterByGroup,
                                                                         String groupId,
                                                                         CategoryRecommendedCondition condition,
                                                                         ProcedureSurveyAssignMode assignMode) {
        //id оцениваемого - {ключ - {список подчиненных}}
        Map<String, Map<String, List<PersonForAssignEstimatorBean>>> allPersons = new HashMap<>();
        Set<String> allMemberIds = BeanHelper.getIdSet(members);
        for (List<String> memberIds : CollectionUtils.split(allMemberIds, Pager.INCREASED_MAX_ON_PAGE)) {
            if (levelN) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "levelN",
                    surveyAssignRepository.getInferiors(memberIds), assignMode);
            }
            if (levelN1) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "levelN1",
                    surveyAssignRepository.getInferiorsN1(memberIds), assignMode);
            }
            if (byFunctional) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "byFunctional",
                    surveyAssignRepository.getFunctionalInferiors(memberIds), assignMode);
            }
        }
        if (levelAll) {
            Set<String> memberPersonIds = BeanHelper.getNameBeanIdSet(members, PRMemberBean.PERSON_ID);
            Map<String, List<PersonForAssignEstimatorBean>> allInferiors = new HashMap<>();
            memberPersonIds.forEach(memberPersonId -> {
                Set<String> inferiorIds = personSubordinationService.getDirectorAllInferiorIds(memberPersonId);
                allInferiors.put(
                    memberPersonId,
                    PRMassService.getInstance().getPersonsForAssignEstimatorByIds(assignMode, inferiorIds));
            });
            members.forEach(member -> addPersonsByMemberKeyToList(
                member.getId(), member.getPerson().getId(), allPersons, "levelAll", allInferiors));
        }

        Map<String, List<PersonForAssignEstimatorBean>> memberInferiors = new HashMap<>();
        allPersons.forEach((memberId, personsByKey) -> {
            List<PersonForAssignEstimatorBean> persons = condition.getPersons(personsByKey);
            if (filterByGroup && StringHelper.isNotEmpty(groupId)) {
                persons = filterByGroup(persons, groupId);
            }
            memberInferiors.put(memberId, persons);
        });
        return memberInferiors;
    }

    /**
     * Коллеги
     */
    public Map<String, List<PersonForAssignEstimatorBean>> listColleague(Collection<PRMemberBean> members,
                                                                         boolean byOrg,
                                                                         boolean byPost,
                                                                         boolean bySubordination,
                                                                         boolean byFunctionalSubordination,
                                                                         boolean byCity,
                                                                         boolean filterByGroup,
                                                                         String groupId,
                                                                         CategoryRecommendedCondition condition,
                                                                         ProcedureSurveyAssignMode assignMode) {
        //id оцениваемого - {ключ - {список коллег}}
		Map<String, Map<String, List<PersonForAssignEstimatorBean>>> allPersons = new HashMap<>();
        Set<String> allMemberIds = BeanHelper.getIdSet(members);
        for (List<String> memberIds : CollectionUtils.split(allMemberIds, Pager.INCREASED_MAX_ON_PAGE)) {
            if (byPost) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "byPost",
                    surveyAssignRepository.getColleaguesByPost(memberIds), assignMode);
            }
            if (byOrg) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "byOrg",
                    surveyAssignRepository.getColleaguesByCa(memberIds),
                    assignMode);
            }
            if (byCity) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "byCity",
                    surveyAssignRepository.getColleaguesByCity(memberIds), assignMode);
            }
            if (bySubordination) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "bySubordination",
                    surveyAssignRepository.getColleaguesBySubordination(memberIds), assignMode);
            }
            if (byFunctionalSubordination) {
                addMemberPersonsToList(
                    memberIds,
                    allPersons,
                    "byFunctionalSubordination",
                    surveyAssignRepository.getColleaguesByFunctionalSubordination(memberIds), assignMode);
            }
        }

        Map<String, List<PersonForAssignEstimatorBean>> memberColleague = new HashMap<>();
        allPersons.forEach((memberId, personsByKey) -> {
            List<PersonForAssignEstimatorBean> persons = condition.getPersons(personsByKey);
            if (filterByGroup && StringHelper.isNotEmpty(groupId)) {
                persons = filterByGroup(persons, groupId);
            }
            memberColleague.put(memberId, persons);
        });
        return memberColleague;
    }

	/**
	 * Выбор из списка пользователей всех или случайно заданное кол-во
	 */
    public List<PersonForAssignEstimatorBean> filterPersonsByCount(List<PersonForAssignEstimatorBean> allPersons,
                                                                   int count,
                                                                   PrCategoryMultiAutoAssignment assignment,
                                                                   PRCategoryBean category) {
        List<PersonForAssignEstimatorBean> result = new ArrayList<>();
        if (assignment == PrCategoryMultiAutoAssignment.no) {
            return result;
        }
        if (assignment == PrCategoryMultiAutoAssignment.all) {
            return allPersons;
        }
        int resultCount = count <= allPersons.size() ? count : allPersons.size();
        Collections.shuffle(allPersons);
        for (PersonForAssignEstimatorBean person : allPersons) {
            if (result.size() < resultCount &&
                    PRMassService.getInstance().checkMaxSurveyForPersonByCategory(person.getId(), category)) {
                result.add(person);
            }
        }
        return result;
    }

	/**
	 * Выбор из пользователей входящих в группу
	 */
	private List<PersonForAssignEstimatorBean> filterByGroup(List<PersonForAssignEstimatorBean> allPersons,
                                                             String groupId) {
        List<String> allPersonIds = CollectionUtils.stream(allPersons)
            .map(PersonForAssignEstimatorBean::getId)
            .collect(Collectors.toList());
        return groupCatalogService.getObjectsFromGroup(groupId, PersonGroupCatalog.NAME, PersonBean.class)
            .filter(personBean -> allPersonIds.contains(personBean.getId()))
            .map(personBean ->
                BeanHelper.getBeanById(allPersons, personBean.getId(), PersonForAssignEstimatorBean::getId))
            .collect(Collectors.toList());
	}

    private void addMemberPersonsToList(List<String> memberIds,
                                        Map<String, Map<String, List<PersonForAssignEstimatorBean>>> allMembersPersons,
                                        String key,
                                        QueryData<SelectQuery> queryData,
                                        ProcedureSurveyAssignMode assignMode) {
        //id карты - оценивающие
        Map<String, List<PersonForAssignEstimatorBean>> personsByMemberId =
            PRMassService.getInstance().getPersonEstimatorsByMemberId(queryData, assignMode);
        memberIds.forEach(memberId ->
            addPersonsByMemberKeyToList(memberId, memberId, allMembersPersons, key, personsByMemberId));
    }

    private void addPersonsByMemberKeyToList(
            String memberId,
            String memberKey,
            Map<String, Map<String, List<PersonForAssignEstimatorBean>>> allMembersPersons,
            String key,
            Map<String, List<PersonForAssignEstimatorBean>> personsByMemberKey) {
        List<PersonForAssignEstimatorBean> persons = personsByMemberKey.containsKey(memberKey) ?
                personsByMemberKey.get(memberKey) : new ArrayList<>();
        if (allMembersPersons.containsKey(memberId)) {
            allMembersPersons.get(memberId).put(key, persons);
        } else {
            Map<String, List<PersonForAssignEstimatorBean>> map = new HashMap<>();
            map.put(key, persons);
            allMembersPersons.put(memberId, map);
        }
    }

    /**
     * Оценка категории по компетенции оцениваемого
     */
    public static PRCategoryQuaBean getCategoryQua(String id) {
        return EntityManager.get(id, PRCategoryQuaBean.class);
    }

    /**
     * Оценки категгрий по компетенции оцениваемого
     */
    public static List<PRCategoryQuaBean> listCategoryQuaScoresByMemberQuaId(String memberQuaId) {
        PRCategoryQuaBean bean = new PRCategoryQuaBean();
        bean.setMemberQuaId(memberQuaId);
        return EntityManager.list(bean);
    }

	public static List<PRCategoryQuaBean> listCategoryQuaScoresByMember(String categoryId, String memberId) {
		return getInstance().getCategoryQuaScoresByMemberIds(
		    Collections.singleton(categoryId), Collections.singleton(memberId));
	}

    public List<PRCategoryQuaBean> getCategoryQuaScoresByMemberIds(Collection<String> categoryIds,
                                                                   Collection<String> memberIds) {
        SelectQuery query = orm.getDataObject(PRCategoryQuaBean.class).getQuery();
        query.innerJoin(PRMemberQuaBean.DATANAME, "MQ")
            .on(Column.column(PARENT_ALIAS, PRCategoryQuaBean.MEMBER_QUA_ID)
                .eq(Column.column("MQ", PRMemberQuaBean.ID)));
        query.where(Column.column(PARENT_ALIAS, PRCategoryQuaBean.CATEGORY_ID)
            .in(NamedParameter.namedParameter(PRCategoryQuaBean.CATEGORY_ID).setHasBrackets()));
        query.where(Column.column("MQ", PRMemberQuaBean.MEMBER_ID)
            .in(NamedParameter.namedParameter(PRMemberQuaBean.MEMBER_ID).setHasBrackets()));
        return EntityManager.list(
            QueryData.fromQuery(query)
                .withNamedIdParameter(PRCategoryQuaBean.CATEGORY_ID, categoryIds)
                .withNamedIdParameter(PRMemberQuaBean.MEMBER_ID, memberIds),
            PRCategoryQuaBean.class);
    }

    /**
     * Оценки категорий по компетенциям оцениваемого
     */
    public static List<PRCategoryQuaBean> listCategoryQuaByMember(String memberId) {
        return EntityManager.list(ProcedureSQL.ListCategoryQuaScoreByMember.create(memberId), PRCategoryQuaBean.class);
    }

    public static List<PRCategoryQuaBean> listCategoryQuaByMembers(Collection<String> memberIds) {
        return EntityManager.list(ProcedureSQL.ListCategoryQuaScoreByMember.create(memberIds), PRCategoryQuaBean.class);
    }

    /**
     * Оценка категории по компетенции
     *
     * @param surveyId id анкеты
     * @param memberQuaId id оценки по компетенции участника
     * @return
     */
    public static PRCategoryQuaBean getCategoryQuaBySurveyIdAndMemberQuaId(String surveyId, String memberQuaId) {
	    return EntityManager.find(ProcedureSQL.GetCategoryQuaBySurveyIdAndMemberQuaId.create(surveyId, memberQuaId), PRCategoryQuaBean.class);
    }

    /**
     * Оценки категории по компетенциям анкеты
     */
    public static List<PRCategoryQuaBean> listCategoryQuaBySurveyId(String surveyId) {
        return getInstance().getCategoryQuaBySurveyIds(Collections.singleton(surveyId))
            .getOrDefault(surveyId, Collections.emptyList());
    }

    /**
     * Оценки категории по компетенциям анкеты
     */
    public Map<String, List<PRCategoryQuaBean>> getCategoryQuaBySurveyIds(Collection<String> surveyIds) {
        List<DataSet> dataSets = Session.list(ProcedureSQL.ListCategoryQuaBySurveyId.create(surveyIds));
        Map<String, List<PRCategoryQuaBean>> categoryQuaBySurveyIds = new HashMap<>();
        dataSets.forEach(dataSet -> {
            String surveyId = dataSet.getValue(PRSurveyBean.ID);
            PRCategoryQuaBean categoryQua =BeanHelper.createFromDBDataSet(PRCategoryQuaBean.class, dataSet);
            CollectionUtils.getOrCreate(categoryQuaBySurveyIds, surveyId, new ArrayList<>()).add(categoryQua);
        });
        return categoryQuaBySurveyIds;
    }

    /**
     * @return оценка анкеты по компетенции - оценка категории по компетенции
     */
    public Map<String, PRCategoryQuaBean> getCategoryQuaBySurveyQuaIds(Collection<String> surveyQuaIds) {
        List<DataSet> dataSets = Session.list(procedureRepository.getCategoryQuaBySurveyQuaIds(surveyQuaIds));
        Map<String, PRCategoryQuaBean> result = new HashMap<>();
        dataSets.forEach(dataSet -> result.put(
                dataSet.getValue(PRSurveyQuaBean.ID),
                BeanHelper.createFromDBDataSet(PRCategoryQuaBean.class, dataSet)));
        return result;
    }

    /**
     * Оценки категорий по процедуре
     */
    public static List<PRCategoryQuaBean> listCategoryQuaByProcedure(String procedureId) {
        return EntityManager.list(ProcedureSQL.ListCategoryQuaScoreByProcedure.create(procedureId), PRCategoryQuaBean.class);
    }

    public static List<PRCategoryIndicatorBean> listCategoryIndByQua(String categoryQuaId) {
        PRCategoryIndicatorBean bean = new PRCategoryIndicatorBean();
        bean.setCategoryQuaId(categoryQuaId);
        return EntityManager.list(bean);
    }

    public static Map<String, List<PRCategoryIndicatorBean>> getCategoryIndicators(Collection<String> categoryQuaIds) {
        if (CollectionUtils.isEmpty(categoryQuaIds)) {
            return new HashMap<>();
        }
        SelectQuery query = ORM.getInstance().getDataObject(PRCategoryIndicatorBean.class).getQuery();
        query.where(new Expression(
            new Column(PRCategoryIndicatorBean.CATEGORY_QUA_ID, PARENT_ALIAS),
            NamedParameter.namedParameter(PRCategoryIndicatorBean.CATEGORY_QUA_ID).setHasBrackets(),
            Expression.IN));
        return BeanHelper.createMapFromListByFK(EntityManager.list(QueryData.fromQuery(query)
                .withNamedIdParameter(PRCategoryIndicatorBean.CATEGORY_QUA_ID, categoryQuaIds),
                PRCategoryIndicatorBean.class), PRCategoryIndicatorBean.CATEGORY_QUA_ID);
    }

    /**
     * Категории процедур
     * {id процедуры - список категорий}
     */
    public Map<String, List<PRCategoryBean>> mapProcedureCategories(Collection<String> procedureIds) {
        return BeanHelper.createMapFromListByLookup(
                EntityManager.list(getCategoryByProcedureQueryData(procedureIds), PRCategoryBean.class),
                PRCategoryBean.PROCEDURE_ID);
    }

    /**
     * Оценки категорий процедур по компетециям
     * {id процедуры - список оценок категорий}
     */
    public static Map<String, List<PRCategoryQuaBean>> mapProcedureCategoryQuaScores(Collection<String> procedureIds) {
        List<PRCategoryQuaBean> scores = EntityManager.list(ProcedureSQL.ListCategoryQuaScoreByProcedure.create(procedureIds), PRCategoryQuaBean.class);
        return BeanHelper.createMapFromListByLookup(scores, PRCategoryBean.PROCEDURE_ID);
    }

	public static PRCategoryBean findSingleCategory(List<PRCategoryBean> categories, ProcedureEstimatingCategory category) {
		for (PRCategoryBean bean : categories) {
			if (bean.getCategory().equals(category)) {
				return bean;
			}
		}
		return null;
	}

	public static List<PRCategoryBean> findAllCategories(List<PRCategoryBean> categories, ProcedureEstimatingCategory category) {
		List<PRCategoryBean> result = new ArrayList<PRCategoryBean>();
		for (PRCategoryBean bean : categories) {
			if (bean.getCategory().equals(category)) {
				result.add(bean);
			}
		}
		return result;
	}

    public Optional<PRCategoryBean> findCategory(Collection<PRCategoryBean> categories,
                                                 ProcedureEstimatingCategory category,
                                                 Optional<String> additionalValue) {
        return categories.stream()
                .filter(bean ->
                        bean.getCategory() == category &&
                        category.getValueForCompare(bean)
                                .map(nameBean ->
                                        additionalValue.map(value -> value.equals(nameBean.getId())).orElse(false))
                                .orElse(true))
                .findFirst();
    }

    public Optional<PRCategoryBean> findCategory(String procedureId,
                                                 ProcedureEstimatingCategory category,
                                                 Optional<String> additionalValue) {
        return findCategory(getProcedureCategories(procedureId), category, additionalValue);
    }

    public Optional<PRCategoryBean> findAutoCategory(Collection<PRCategoryBean> categories,
                                                     ProcedureEstimatingCategory category) {
        return findCategory(categories, category, Optional.empty());
    }

    /**
     * Оценки оценивающих категорий по категориям компетенций с оценками анкет этих категорий
     */
	public static List<PRCategoryQuaCategoryBean> listCategoryQuaCategoryByMemberId(String memberId) {
        List<PRCategoryQuaCategoryBean> categoryScores = EntityManager.list(ProcedureSQL.ListCategoryQuaCategoryByMemberId.create(memberId), PRCategoryQuaCategoryBean.class);
        List<PRSurveyQuaCategoryBean> surveyScores = PRSurveyService.getInstance().listSurveyQuaCategoryScoresByMemberId(memberId);
        for (PRCategoryQuaCategoryBean categoryScore : categoryScores) {
            //Оценки анкет оценивающей категории
            List<PRSurveyQuaCategoryBean> categorySurveyScores = new ArrayList<PRSurveyQuaCategoryBean>();
            for (PRSurveyQuaCategoryBean surveyScore : surveyScores) {
                if (surveyScore.getMemberCategoryId().equals(categoryScore.getMemberCategoryId()) &&
                        surveyScore.getSurveyCategoryId().equals(categoryScore.getCategoryId())) {
                    categorySurveyScores.add(surveyScore);

                }
            }
            categoryScore.setChildren(categorySurveyScores);
        }
        return categoryScores;
	}

    public static List<PRCategoryQuaComparisonBean> listCategoryQuaComparison(String procedureId) {
        return EntityManager.list(ProcedureSQL.ListCategoryQuaComparison.create(procedureId), PRCategoryQuaComparisonBean.class);
    }

    /**
     * Кол-во оценок по единой шкале для категорий процедуры
     */
    public static List<PRCategorySNCountBean> listCategorySNCount(String procedureId) {
        return EntityManager.list(ProcedureSQL.ListPRCategorySNCount.create(procedureId), PRCategorySNCountBean.class);
    }

    /**
     * Ограничения по единой шкале для категории
     */
    public List<PRCategorySNCountBean> getSNCountByCategoryId(String categoryId) {
        PRCategorySNCountBean filter = new PRCategorySNCountBean();
        filter.setCategoryId(categoryId);
        return EntityManager.list(filter);
    }

    /**
     * Ограничения по единой шкале для всех категории процедуры
     */
    public Map<String, List<PRCategorySNCountBean>> getCategorySNCount(String procedureId) {
        List<PRCategorySNCountBean> beans = listCategorySNCount(procedureId);
        return BeanHelper.createMapFromListByFK(beans, PRCategorySNCountBean.CATEGORY_ID);
    }

    /**
     * Из списка значений шкал удалить значения недопустимые для категории
     */
    public static List<ScaleNumberBean> getScaleValuesByCategoryQua(String scaleId, PRCategoryQuaBean categoryQua) {
        return getScaleValuesByCategoryQua(ScaleService.getInstance().listScaleNumber(scaleId), categoryQua);
    }

    /**
     * Из списка значений шкал удалить значения недопустимые для категории
     */
    public static List<ScaleNumberBean> getScaleValuesByCategoryQua(List<ScaleNumberBean> scaleValues,
                                                                    PRCategoryQuaBean categoryQua) {
        Integer above = categoryQua == null ? null : categoryQua.getUnavailableAbove();
        Integer below = categoryQua == null ? null : categoryQua.getUnavailableBelow();
        //Для категории не заданы ограничения на кол-во значений шкал
        if (IntHelper.isNull(above) && IntHelper.isNull(below)) {
            return scaleValues;
        }
        List<ScaleNumberBean> resultScaleValues = new ArrayList<>();
        for (ScaleNumberBean sn : scaleValues) {
            int index = scaleValues.indexOf(sn) + 1;
            if ((IntHelper.isNull(above) || index <= (scaleValues.size() - above)) && //Удалить значение сверху
                    (IntHelper.isNull(below) || index > below)) {//Удалить значения снизу
                resultScaleValues.add(sn);
            }
        }
        return resultScaleValues;
    }

    /**
     * Кол-во анкет для оценивающего по категории
     */
    public static int getCountSurveyForPersonAndCategory(PRCategoryBean category, String personId) {
        PRSurveyBean surveyFilter = new PRSurveyBean();
        surveyFilter.getPerson().setId(personId);
        surveyFilter.getCategory().setId(category.getId());
        //Анкеты оценивающего по категории
        List<PRSurveyBean> surveys = EntityManager.list(surveyFilter);
        //Исключить отмененные
        List<PRSurveyBean> activeSurveys = new ArrayList<PRSurveyBean>();
        for (PRSurveyBean survey : surveys) {
            if (!survey.getStatus().isCanceled()) {
                activeSurveys.add(survey);
            }
        }
        //Назначения пользователя по категории при самоназначении
        List<PRSurveyCandidateBean> selfAssignments = EntityManager.list(ProcedureSQL.ListSelfAssignmentByPersonAndCategory.create(personId, category.getProcedure().getId(), category.getCategory()), PRSurveyCandidateBean.class);
		List<PRSurveyCandidateBean> activeCandidates = PRSurveyCandidateService.getInstance().filterActiveSurveys(selfAssignments);
        return (activeSurveys.size() + activeCandidates.size());
    }


    /**
     * Проверка максимального кол-ва анкет на оценивающенго по категории
     */
    public static boolean checkMaxSurveyCountForPerson(PRCategoryBean category, String personId) {
        if (category.getCategory().isOneSurvey() || IntHelper.isNull(category.getMaxSurveyCount())) {
            return true;
        }
        return getCountSurveyForPersonAndCategory(category, personId) < category.getMaxSurveyCount();
    }

    /**
     * Расхождения в оценках по компетенциям оцениваемых процедуры
     * @return {Оцениваемый - {Название компетенции - Категории}}
     */
    public static Map<PRMemberBean, Map<String, List<String>>> getCategoryQuaDifferencesByProcedure(String procedureId) {
        Map<PRMemberBean, Map<String, List<String>>> map = new HashMap<PRMemberBean, Map<String, List<String>>>();
        ProcedureBean procedure = ProcedureService.getInstance().getProcedureWithCategories(procedureId);
        Double minPointDifference = procedure.getAdditional().getQuaMinPointDifference();
        //Не задан минимальный балл расхождения
        if (DoubleValue.isNull(minPointDifference) || minPointDifference == 0) {
            return map;
        }

        List<PRMemberBean> members = PRMemberService.listProcedureActiveMember(procedureId);
        Map<String, List<PRMemberQuaBean>> memberQuaScores = PRMemberService.mapMemberQuaScore(BeanHelper.getIdSet(members));
        List<PRCategoryQuaBean> categoryQuaScores = mapProcedureCategoryQuaScores(Arrays.asList(procedureId)).get(procedureId);
        List<PRCategoryQuaComparisonBean> comparisonBeans = listCategoryQuaComparison(procedure.getId());

        for (PRMemberBean member : members) {
            List<PRMemberQuaBean> quaScores = memberQuaScores.get(member.getId());
            if (CollectionUtils.isNotEmpty(quaScores)) {
                Map<String, List<String>> memberDiff = getCategoryQuaDifferencesByMember(quaScores, categoryQuaScores, comparisonBeans, procedure);
                if (!memberDiff.isEmpty()) {
                    map.put(member, memberDiff);
                }
            }
        }
        return map;
    }

    private static Map<String, List<String>> getCategoryQuaDifferencesByMember(List<PRMemberQuaBean> memberQuaScores,
                List<PRCategoryQuaBean> categoryQuaScores, List<PRCategoryQuaComparisonBean> comparisonBeans, ProcedureBean procedure) {
        Double minPointDifference = procedure.getAdditional().getQuaMinPointDifference();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (PRMemberQuaBean memberQuaScore : memberQuaScores) {
            String quaName = memberQuaScore.getQua().getName();
            for (PRCategoryQuaComparisonBean comparison : comparisonBeans) {
                PRCategoryQuaBean categoryScore1 = findCategoryQua(categoryQuaScores, comparison.getCategoryId1(), memberQuaScore.getId());
                PRCategoryQuaBean categoryScore2 = findCategoryQua(categoryQuaScores, comparison.getCategoryId2(), memberQuaScore.getId());
                if (categoryScore1 != null && categoryScore2 != null &&
                        !DoubleValue.isNull(categoryScore1.getPointForCalculate()) &&
                        !DoubleValue.isNull(categoryScore2.getPointForCalculate()) &&
                        Math.abs(categoryScore2.getPointForCalculate() - categoryScore1.getPointForCalculate()) >= minPointDifference) {
                    //Есть расхождение по компетенции между категориями
                    String category1 = BeanHelper.getBeanById(procedure.getCategories(), categoryScore1.getCategoryId()).getCategoryName();
                    String category2 = BeanHelper.getBeanById(procedure.getCategories(), categoryScore2.getCategoryId()).getCategoryName();
                    if (map.containsKey(quaName)) {
                        map.get(quaName).add(category1);
                        map.get(quaName).add(category2);
                    } else {
                        List<String> categories = new ArrayList<String>();
                        categories.add(category1);
                        categories.add(category2);
                        map.put(quaName, categories);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Расхождения в оценках по компетенциям оцениваемого
     * @return {Название компетенции - Категории}
     */
    public static Map<String, List<String>> getCategoryQuaDifferencesByMember(String memberId) {
        Optional<ProcedureBean> procedure = ProcedureService.getInstance().getProcedureByMemberId(memberId);
        if (!procedure.isPresent()) {
            return Collections.emptyMap();
        }
        Double minPointDifference = procedure.get().getAdditional().getQuaMinPointDifference();
        //Не задан минимальный балл расхождения
        if (DoubleValue.isNull(minPointDifference) || minPointDifference == 0) {
            return Collections.emptyMap();
        }
        List<PRMemberQuaBean> memberQuaScores = PRMemberService.listMemberQuaScore(memberId);
        //Оценки категорий по компетенциям
        List<PRCategoryQuaBean> categoryQuaScores = listCategoryQuaByMember(memberId);
        List<PRCategoryQuaComparisonBean> comparisonBeans = listCategoryQuaComparison(procedure.get().getId());
        return getCategoryQuaDifferencesByMember(memberQuaScores, categoryQuaScores, comparisonBeans, procedure.get());
    }

    public static PRCategoryQuaBean findCategoryQua(List<PRCategoryQuaBean> categoryQuaScores, String categoryId, String memberQuaId) {
        for (PRCategoryQuaBean categoryQua : categoryQuaScores) {
            if (categoryQua.getCategoryId().equals(categoryId) && categoryQua.getMemberQuaId().equals(memberQuaId))
                return categoryQua;
        }
        return null;
    }

    /**
     * Оценки категории по компетенциям карты
     */
    public static List<PRCategoryQuaBean> findCategoryQuaScoresByMember(String categoryId,
                                                                        List<PRMemberQuaBean> memberQuaScores) {
        List<PRCategoryQuaBean> categoryQuaScores = new ArrayList<PRCategoryQuaBean>();
        for (PRMemberQuaBean memberQuaScore : memberQuaScores) {
            for (PRCategoryQuaBean memberCategoryScore : memberQuaScore.getChildren()) {
                if (memberCategoryScore.getCategoryId().equals(categoryId)) {
                    categoryQuaScores.add(memberCategoryScore);
                }
            }
        }
        return categoryQuaScores;
    }

    public static Optional<PRCategoryIndicatorBean> findCategoryIndByMemberInd(PRCategoryQuaBean categoryQuaScore,
                                                                               String memberIndId) {
        return getInstance().findCategoryIndByMemberInd(categoryQuaScore.getCategoryIndScores(), memberIndId);
    }

    public Optional<PRCategoryIndicatorBean> findCategoryIndByMemberInd(List<PRCategoryIndicatorBean> categoryIndScores,
                                                                        String memberIndId) {
        return categoryIndScores.stream()
                .filter(categoryIndScore -> categoryIndScore.getMemberIndicatorId().equals(memberIndId))
                .findFirst();
    }

    /**
     * ДЛя анкеты по категории собеседование заполнить и сохранить оценки по источнику
     */
    public void fillAndSaveInterviewSurveyScoreBySource(PRMemberBean member, Log log) {
        Set<String> memberIds = CollectionUtils.newUnorderedSet(member.getId());
        ProcedureBean procedure = procedureService.getProcedureWithCategories(member.getProcedure().getId());

        PRCategoryBean interviewCategory = Optional.ofNullable(
            findSingleCategory(procedure.getCategories(), EstimatingCategory.INTERVIEW)
        ) .orElseThrow(() -> new IllegalArgumentException("interviewCategory should not be null"));
        if (!interviewCategory.getScoreFromSource()) {
            log.info("Interview category " + interviewCategory.getId() + " scoreFromSource is false");
            return;//Оценки не из источника
        }
        List<PRSurveyBean> interviewSurveys = surveyService.getMemberActiveSurveyByCategory(
                member.getId(), Collections.singleton(interviewCategory.getId()))
                .get(interviewCategory.getId());
        if (CollectionUtils.isEmpty(interviewSurveys)) {
            log.info("Interview surveys is empty");
            return;//Нет анкет по категории собеседование
        }
        //Анкета по собеседованию
        PRSurveyBean interviewSurvey = interviewSurveys.get(0);
        //Собеседование не оценивает компетенции или завершена оценка компетенций
        if (!interviewSurvey.getIsQua() || interviewSurvey.getIsQuaComplete()) {
            log.info("Interview survey " + interviewSurvey.getId() +
                    " isQua=" + interviewSurvey.getIsQua() +
                    " isQuaComplete=" + interviewSurvey.getIsQuaComplete());
            return;
        }
        //Оценки анкеты по компетенциям и индикаторам
        List<PRSurveyQuaBean> interviewSurveyQuaScores = PRSurveyService.getSurveyQuaScoresWithIndicatorsAndCharacteristics(
                Collections.singleton(interviewSurvey.getId()))
                .get(interviewSurvey.getId());
        //Категория источник
        PRCategoryBean sourceCategory = Optional.ofNullable(
            findSingleCategory(procedure.getCategories(), interviewCategory.getCategorySource())
        ).orElseThrow(() -> new IllegalArgumentException("sourceCategory should not be null"));
        log.info("Source category " + sourceCategory.getId() + ", " + sourceCategory.getCategory());
        //Оценки оцениваемого по компетенциям
        Map<String, List<PRMemberQuaBean>> memberQuaScoresByMember = PRMemberService.getMemberQuaWithIndicatorScore(memberIds);
        PRMemberService.fillCategoryWithAllScoreByMembers(memberQuaScoresByMember, memberIds, Arrays.asList(interviewCategory, sourceCategory));
        List<PRMemberQuaBean> memberQuaScores = memberQuaScoresByMember.get(member.getId());

        //Оценки собеседования
        List<PRCategoryQuaBean> interviewCategoryQuaScores = findCategoryQuaScoresByMember(interviewCategory.getId(), memberQuaScores);
        //Оценки источника
        List<PRCategoryQuaBean> sourceCategoryQuaScores = findCategoryQuaScoresByMember(sourceCategory.getId(), memberQuaScores);

        fillSurveyScoreBySource(
                member,
                interviewSurvey,
                interviewCategoryQuaScores,
                sourceCategoryQuaScores,
                interviewCategory,
                sourceCategory,
                interviewSurveyQuaScores,
                Optional.of(log),
                true);
    }

    /**
     * Заполнить оценки анкеты по оценкам категории-источника
     */
    public void fillSurveyScoreBySource(PRMemberBean member,
                                        PRSurveyBean currentSurvey,
                                        List<PRCategoryQuaBean> currentQuaScores,
                                        List<PRCategoryQuaBean> sourceQuaScores,
                                        PRCategoryBean currentCategory,
                                        PRCategoryBean sourceCategory,
                                        List<PRSurveyQuaBean> currentSurveyQuaScores,
                                        Optional<Log> logOptional,
                                        boolean isSave) {
        //Заполнить оценками из источника, если не проставлено ни одной оценки
        if (!isSave) {
            for (PRSurveyQuaBean surveyQua : currentSurveyQuaScores) {
                if (!DoubleValue.isNull(surveyQua.getPointForCalculate())) {
                    logOptional.ifPresent(log -> log.info("Survey qua score is not null"));
                    return;
                }
                if (CollectionUtils.isNotEmpty(surveyQua.getChildren())) {
                    for (PRSurveyIndicatorBean surveyInd : surveyQua.getChildren()) {
                        if (!DoubleValue.isNull(surveyInd.getPointForCalculate())) {
                            logOptional.ifPresent(log -> log.info("Survey indicator score is not null"));
                            return;
                        }
                    }
                }
            }
        }

        currentSurveyQuaScores.forEach(surveyQua -> fillSurveyQuaScoreBySource(
                surveyQua,
                currentQuaScores,
                sourceQuaScores,
                currentCategory,
                sourceCategory,
                logOptional,
                isSave));
        if (isSave) {
            PRMemberService.getInstance().getQuaCalculator().calculateSurveyQuaScoresByChildScores(member, currentSurvey);
            currentSurvey.setQuaScores(
                    PRSurveyService.listSurveyQuaBySurveyIdWithIndicatorsAndCharacteristics(currentSurvey.getId()));
            new SurveyScoreQuantityInfo(currentSurvey).calculateScores();
        }
    }

    private void fillSurveyQuaScoreBySource(PRSurveyQuaBean surveyQua,
                                            List<PRCategoryQuaBean> currentQuaScores,
                                            List<PRCategoryQuaBean> sourceQuaScores,
                                            PRCategoryBean currentCategory,
                                            PRCategoryBean sourceCategory,
                                            Optional<Log> logOptional,
                                            boolean isSave) {
        logOptional.ifPresent(log -> log.info("Fill survey qua score by source: id = " + surveyQua.getId() +
                ", memberQuaId = " + surveyQua.getMemberQuaId() +
                ", surveyId = " + surveyQua.getSurvey().getId()));
        PRCategoryQuaBean sourceCategoryQuaScore = findCategoryQua(
                sourceQuaScores, sourceCategory.getId(), surveyQua.getMemberQuaId());
        PRCategoryQuaBean categoryQuaScore = findCategoryQua(
                currentQuaScores, currentCategory.getId(), surveyQua.getMemberQuaId());
        if (sourceCategoryQuaScore == null) {
            logOptional.ifPresent(log -> log.info("SourceCategoryQuaScore is null"));
            return;
        }
        EstimatingMethod method = categoryQuaScore != null ? categoryQuaScore.getMethod() : null;
        EstimatingMethod sourceMethod = sourceCategoryQuaScore.getMethod();
        if (method == null || method != sourceMethod) {
            logOptional.ifPresent(log -> log.info("Estimated methods not equals: method = " + method +
                    " sourceMethod = " + sourceMethod));
            return;
        }
        Optional<PRSurveyQuaBean> sourceSurveyQuaScore = sourceCategoryQuaScore.getSurveyQuaScores().stream()
            .filter(sourceSurveyQua -> sourceSurveyQua.getSurveyQua().getMemberQuaId().equals(surveyQua.getMemberQuaId()))
            .findFirst()
            .map(PrSurveyQuaWithSurveyBean::getSurveyQua);
        //Категория-источник с одной анкетой: оценки из анкеты
        AbstractScoreBean sourceQuaScore =
                sourceCategory.getCategory().isOneSurvey() && sourceSurveyQuaScore.isPresent() ?
                        sourceSurveyQuaScore.get() :
                        sourceCategoryQuaScore;

        //Оценка по компетенции
        surveyQua.setPoint(sourceQuaScore.getPointForCalculate());
        surveyQua.setScore(sourceQuaScore.getScoreForCalculate());
        surveyQua.setIsRefusal(sourceQuaScore.isRefusalForCalculate());

        if (method.byIndicators() || method.byScaleAndIndicators()) {
            surveyQua.getChildren().forEach(surveyInd -> fillSurveyIndicatorScoreBySource(
                    surveyInd,
                    sourceCategoryQuaScore,
                    sourceSurveyQuaScore,
                    sourceCategory,
                    logOptional,
                    isSave));
        }
        if (method.byCharacteristic() && sourceCategory.getCategory().isOneSurvey()) {
            surveyQua.getCharacteristics().forEach(characteristic -> fillSurveyCharacteristicScoreBySource(
                    characteristic,
                    sourceSurveyQuaScore,
                    logOptional,
                    isSave));
        }
        if (isSave) {
            EntityManager.update(surveyQua, false);
        }
    }

    private void fillSurveyIndicatorScoreBySource(PRSurveyIndicatorBean surveyInd,
                                                  PRCategoryQuaBean sourceCategoryQuaScore,
                                                  Optional<PRSurveyQuaBean> sourceSurveyQuaScore,
                                                  PRCategoryBean sourceCategory,
                                                  Optional<Log> logOptional,
                                                  boolean isSave) {
        logOptional.ifPresent(log -> log.info("Fill survey indicator score by source: id = " + surveyInd.getId() +
                ", memberIndId = " + surveyInd.getMemberIndicatorId() +
                ", surveyQuaId = " + surveyInd.getSurveyQuaId()));
        Optional<PRCategoryIndicatorBean> sourceCategoryIndicatorOptional = findCategoryIndByMemberInd(
                sourceCategoryQuaScore, surveyInd.getMemberIndicatorId());
        if (!sourceCategoryIndicatorOptional.isPresent()) {
            logOptional.ifPresent(log -> log.info("SourceCategoryIndicator is null"));
            return;
        }
        Optional<PRSurveyIndicatorBean> sourceSurveyIndScore = sourceSurveyQuaScore.isPresent() ?
                sourceSurveyQuaScore.get().getChildren().stream()
                        .filter(sourceSurveyInd -> sourceSurveyInd.getMemberIndicatorId()
                                .equals(surveyInd.getMemberIndicatorId()))
                        .findFirst() :
                Optional.empty();
        AbstractScoreBean sourceIndScore =
                sourceCategory.getCategory().isOneSurvey() && sourceSurveyIndScore.isPresent() ?
                        sourceSurveyIndScore.get() :
                        sourceCategoryIndicatorOptional.get();
        //Оценка по индикатору
        surveyInd.setPoint(sourceIndScore.getPointForCalculate());
        surveyInd.setScore(sourceIndScore.getScoreForCalculate());
        surveyInd.setIsRefusal(sourceIndScore.isRefusalForCalculate());
        if (isSave) {
            EntityManager.update(surveyInd, false);
        }
    }

    private void fillSurveyCharacteristicScoreBySource(PRSurveyCharacteristicBean characteristic,
                                                       Optional<PRSurveyQuaBean> sourceSurveyQuaScore,
                                                       Optional<Log> logOptional,
                                                       boolean isSave) {
        logOptional.ifPresent(log -> log.info("Fill survey characteristic score by source: id = " +
                characteristic.getId() + ", surveyQuaId = " + characteristic.getSurveyQuaId()));
        if (!sourceSurveyQuaScore.isPresent()) {
            logOptional.ifPresent(log -> log.info("SourceSurveyQuaScore is null"));
            return;
        }
        //Оценки по проявлениям, у категории -источника только одна анкета
        sourceSurveyQuaScore.get().getCharacteristics()
                .forEach(sourceCharacteristic -> {
                    if (characteristic.getCharacteristic().getId().equals(sourceCharacteristic.getCharacteristic().getId())) {
                        characteristic.setPoint(sourceCharacteristic.getPointForCalculate());
                    }
                });
        if (isSave) {
            EntityManager.update(characteristic, false);
        }
    }

    /**
     * Настройка назначения оценивающих для дополнительной категории
     */
    public PRCategoryAssignBean getCategoryAssign(String id) {
        return EntityManager.find(id, PRCategoryAssignBean.class);
    }

    /**
     * Настройки назначения оценивающих для дополнительной категории
     */
    public List<PRCategoryAssignBean> getCategoryAssigns(String categoryId) {
        PRCategoryAssignBean bean = new PRCategoryAssignBean();
        bean.setCategoryId(categoryId);
        return EntityManager.list(bean);
    }

    /**
     * Оценивающие настройки дополнительной категории
     */
    public List<PersonBean> getCategoryAssignEstimatorPersons(String categoryAssignId) {
        PRCategoryAssignEstimatorBean bean = new PRCategoryAssignEstimatorBean();
        bean.setCategoryAssignId(categoryAssignId);
        Set<String> personIds = BeanHelper.getValueSet(EntityManager.list(bean), PRCategoryAssignEstimatorBean.PERSON_ID);
        return EntityManager.list(PersonBean.class, personIds);
    }

    /**
     * Оценивающие настроек дополнительной категории
     */
    public Map<String, List<PRCategoryAssignEstimatorBean>> getCategoryAssignEstimators(
            Collection<String> categoryAssignIds) {
        if (CollectionUtils.isEmpty(categoryAssignIds)) {
            return Collections.emptyMap();
        }
        SelectQuery query = orm.getDataObject(PRCategoryAssignEstimatorBean.class).getQueryWithLookups();
        query.innerJoin(PRCategoryAssignBean.DATANAME, PRCategoryBean.ALIAS)
                .on(column(PRCategoryBean.ALIAS, PRCategoryAssignBean.ID)
                        .eq(column(PARENT_ALIAS, PRCategoryAssignEstimatorBean.CATEGORY_ASSIGN_ID)));
        query.where(column(PRCategoryBean.ALIAS, PRCategoryAssignBean.ESTIMATOR_TYPE).eq(parameter()));
        query.where(column(PARENT_ALIAS, PRCategoryAssignEstimatorBean.CATEGORY_ASSIGN_ID)
                .in(NamedParameter.namedParameter(PRCategoryAssignEstimatorBean.CATEGORY_ASSIGN_ID).setHasBrackets()));
        List<PRCategoryAssignEstimatorBean> estimators = EntityManager.list(
                QueryData.fromQuery(query)
                        .addIntParam(PrCategoryAssignEstimatorType.from_list.getValue())
                        .withNamedIdParameter(
                                PRCategoryAssignEstimatorBean.CATEGORY_ASSIGN_ID, categoryAssignIds),
                PRCategoryAssignEstimatorBean.class);
        return BeanHelper.createMapFromListByFK(estimators, PRCategoryAssignEstimatorBean.CATEGORY_ASSIGN_ID);
    }

    /**
     * Оценивающие для карт по настройкам дополнительной категории
     * @return ID оцениваемого - {ID оценивающих}
     */
    public Map<String, Set<String>> getCategoryAssignEstimatorsByMemberIds(PRCategoryBean category) {
        List<PRCategoryAssignBean> categoryAssigns = getCategoryAssigns(category.getId());
        Map<String, List<PRCategoryAssignEstimatorBean>> generalEstimators = getCategoryAssignEstimators(
                BeanHelper.getIdSet(categoryAssigns));
        Map<String, Set<String>> personsByMemberId = new HashMap<>();
        categoryAssigns.forEach(categoryAssign ->
                        categoryAssign.getMemberType().getMemberIdsQueryData(
                                categoryAssign, category.getProcedure().getId())
                                .ifPresent(queryData -> {
                                    BeanQueryPagingIterator<PRMemberBean> iterator = new BeanQueryPagingIterator<>(
                                            Pager.dbLimit(), queryData, PRMemberBean.class);
                                    iterator.forEach(members -> categoryAssign.getEstimatorType().fillEstimatorIds(
                                            categoryAssign,
                                            generalEstimators.get(categoryAssign.getId()),
                                            BeanHelper.getIdSet(members),
                                            personsByMemberId));
                                })
                );
        return personsByMemberId;
    }

    /**
     * Оценивающие для карт по настройкам дополнительной категории
     * @return ID оцениваемого - {ID оценивающих}
     */
    public Map<String, Set<String>> getCategoryAssignEstimatorsByMemberIds(PRCategoryBean category,
                                                                           Collection<String> allMemberIds) {
        List<PRCategoryAssignBean> categoryAssigns = getCategoryAssigns(category.getId());
        Map<String, List<PRCategoryAssignEstimatorBean>> generalEstimators = getCategoryAssignEstimators(
                BeanHelper.getIdSet(categoryAssigns));
        Map<String, Set<String>> personsByMemberId = new HashMap<>();
        categoryAssigns.forEach(categoryAssign -> {
            Collection<String> memberIds = categoryAssign.getMemberType().getMemberIds(categoryAssign, allMemberIds);
            if (CollectionUtils.isNotEmpty(memberIds)) {
                categoryAssign.getEstimatorType().fillEstimatorIds(
                        categoryAssign, generalEstimators.get(categoryAssign.getId()), memberIds, personsByMemberId);
            }
        });
        return personsByMemberId;
    }

    /**
     * Актуализировать анкеты по настройкам дополнительной категории
     */
    public void updateSurveysByAdditionalCategoryAssign(String categoryId) {
        PRCategoryBean category = getCategory(categoryId);
        //Не отмененные анкеты по категории
        List<PRSurveyBean> surveys = surveyService.getActiveSurveyByProcedureAndCategory(
            category.getProcedure().getId(), categoryId);
        Map<String, List<PRSurveyBean>> surveysByMemberId =
            BeanHelper.createMapFromListByLookup(surveys, PRSurveyBean.MEMBER_ID);
        Map<String, Set<String>> personIdsByMemberId = getCategoryAssignEstimatorsByMemberIds(category);

        List<ProcedurePhaseDateBean> procedurePhases = ProcedurePhaseDateBean.list(category.getProcedure().getId(), new ProcedurePhaseDateBean());
        Map<String, String> currentPhaseIds = routeObjService.getObjectCurrentPhaseIds(ProcedureRouteObject.TYPE, personIdsByMemberId.keySet());

        for (String memberId : personIdsByMemberId.keySet()) {
            Set<String> personIds = personIdsByMemberId.get(memberId);
            List<PRSurveyBean> memberSurveys = surveysByMemberId.get(memberId);
            if (CollectionUtils.isEmpty(memberSurveys)) {
                //Нет анкет - добавить по всем
                surveyService.addMemberSurveysByCategory(
                        memberId, category.getId(), personIds, procedurePhases, currentPhaseIds.get(memberId));
            } else {
                Set<String> surveyPersonIds = BeanHelper.getNameBeanIdSet(memberSurveys, PRSurveyBean.PERSON_ID);
                //Отменить анкеты
                memberSurveys.stream()
                    .filter(survey -> !personIds.contains(survey.getPerson().getId()))
                    .forEach(survey -> surveyService.cancelSurvey(survey, category.getProcedure().getId()));
                //Добавить анкеты
                List<String> newEstimatorIds = personIds.stream()
                    .filter(personId -> !surveyPersonIds.contains(personId))
                    .collect(Collectors.toList());
                surveyService.addMemberSurveysByCategory(
                        memberId, category.getId(), newEstimatorIds, procedurePhases, currentPhaseIds.get(memberId));
            }
        }

        //Отменить анкеты для карт, которые не подходят под фильтры
        surveys.stream()
            .filter(survey -> !personIdsByMemberId.keySet().contains(survey.getMember().getId()))
            .forEach(survey -> surveyService.cancelSurvey(survey, category.getProcedure().getId()));
    }

    /**
     * Наборы категорий процедуры
     */
    public List<PrCategorySetBean> getProcedureCategorySets(String procedureId) {
        PrCategorySetBean bean = new PrCategorySetBean();
        bean.getProcedure().setId(procedureId);
        return EntityManager.list(bean);
    }

    /**
     * Доступно изменение анкет по категории
     */
    public boolean isAccessEditCategorySurveys(String categoryId, ProcedureBean procedure) {
        return procedure.getKind().isAllowAddSurveys() &&
            filterServiceFactory.getFilterService(PRCategoryFrame.NAME)
                .getAccessLevelOfCurrentUserForObject(categoryId).isModerator();
    }

    public boolean isAccessEditCategorySurveys(String categoryId, String procedureId) {
        return isAccessEditCategorySurveys(categoryId, procedureService.getProcedureWithCategories(procedureId));
    }

    /**
     * Оценивающие пользователи по автоназначению
     */
    public Map<String, List<PersonForAssignEstimatorBean>> getAutoAssignEstimators(Collection<PRMemberBean> members,
                                                                                   ProcedureBean procedure,
                                                                                   PRCategoryBean category,
                                                                                   ProcedureSurveyAssignMode assignMode) {
        return getAutoAssignEstimators(members, procedure, category, assignMode, false);
    }

    public Map<String, List<PersonForAssignEstimatorBean>> getAutoAssignEstimators(Collection<PRMemberBean> members,
                                                                                   ProcedureBean procedure,
                                                                                   PRCategoryBean category,
                                                                                   ProcedureSurveyAssignMode assignMode,
                                                                                   boolean allowArchive) {
        Map<String, List<PersonForAssignEstimatorBean>> estimators =
            category.getCategory().getEstimators(members, procedure, category, assignMode);
        Map<String, List<PersonForAssignEstimatorBean>> filteredEstimators = new HashMap<>();
        estimators.forEach((memberId, memberEstimators) -> {
            List<PersonForAssignEstimatorBean> persons = category.getCategory().filterAutoAssignEstimators(
                category.getFilteredEstimators(memberEstimators, allowArchive), category);
            if (!persons.isEmpty()) {
                filteredEstimators.put(memberId, persons);
            }
        });
        return filteredEstimators;
    }
}
