package lms.core.newprocedure.category;

import hr.development.DevelopmentMessage;
import lms.core.newprocedure.ProcedureBean;
import lms.core.newprocedure.ProcedureMessage;
import lms.core.newprocedure.ProcedureService;
import lms.core.newprocedure.assessmentcenter.AssessmentCenterService;
import lms.core.newprocedure.assignment.mass.*;
import lms.core.newprocedure.assignment.mass.expertwizard.AddExpertWizard;
import lms.core.newprocedure.category.autoassignment.*;
import lms.core.newprocedure.expert.PRExpertFrame;
import lms.core.newprocedure.expert.PRExpertService;
import lms.core.newprocedure.member.PRMemberBean;
import lms.core.newprocedure.member.PRMemberService;
import lms.core.newprocedure.route.ProcedurePhaseSourceBean;
import lms.core.newprocedure.survey.PRSurveyBean;
import lms.core.newprocedure.survey.PRSurveyService;
import lms.core.newprocedure.surveycandidate.CategorySelfAssignmentSource;
import lms.core.newprocedure.surveycandidate.PRCategorySelfAssignmentBean;
import lms.core.newprocedure.surveycandidate.PRCategorySelfAssignmentFormBean;
import lms.core.newprocedure.surveycandidate.PRSurveyCandidateAutoAssignment;
import lms.core.newprocedure.surveycandidate.mass.AddFromProcedureSurveyCandidateAction;
import lms.core.newprocedure.surveycandidate.mass.AddPersonSurveyCandidateAction;
import lms.core.newprocedure.surveycandidate.mass.AddStructureSurveyCandidateAction;
import lms.core.newprocedure.vv.ProcedureCategoryRubricator;
import lms.core.newprocedure.vv.ProcedureTypeRubricator;
import lms.core.person.PersonGroupCatalog;
import lms.core.person.PersonGroupObjLinkBean;
import lms.core.person.PersonMessage;
import lms.core.person.work.PersonWorkBean;
import lms.core.qua.assessment.QuaAssessmentColumns;
import lms.route.phase.source.PhaseSourceBean;
import lms.system.access.LMSUser;
import lms.system.main.Service;
import org.mirapolis.control.entity.FieldInfo;
import org.mirapolis.control.entity.FieldInfoList;
import org.mirapolis.core.Context;
import org.mirapolis.data.DataSet;
import org.mirapolis.data.bean.BeanHelper;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.db.Session;
import org.mirapolis.mvc.action.ClientAction;
import org.mirapolis.mvc.action.wizard.RunWizardAction;
import org.mirapolis.mvc.model.entity.fields.processors.NotNullFieldComponentProcessor;
import org.mirapolis.mvc.model.entity.fields.processors.OnChangeOnInitFieldComponentProcessor;
import org.mirapolis.mvc.view.clientscript.builders.ComboBoxExpressionBuilder;
import org.mirapolis.mvc.view.clientscript.expressions.BlockExpression;
import org.mirapolis.mvc.view.element.DropDownContainerBuilder;
import org.mirapolis.mvc.view.element.GridButtonsElement;
import org.mirapolis.orm.ComboValue;
import org.mirapolis.orm.ComboValueHelper;
import org.mirapolis.orm.DataObject;
import org.mirapolis.orm.ORM;
import org.mirapolis.orm.paging.BeanQueryPagingIterator;
import org.mirapolis.service.message.Localized;
import org.mirapolis.service.message.LocalizedMessage;
import org.mirapolis.sql.Pager;
import org.mirapolis.sql.QueryData;
import org.mirapolis.sql.SelectQueryData;
import org.mirapolis.sql.fragment.Column;
import org.mirapolis.sql.fragment.Parameter;
import org.mirapolis.sql.fragment.SelectQuery;
import org.mirapolis.util.CollectionUtils;
import org.mirapolis.util.StringHelper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Категории оценивающих
 *
 * @author Elena Puzakova
 * @since 12.05.12 10:20
 */
public enum EstimatingCategory implements ProcedureEstimatingCategory {
    /**
     * 0 - Самооценка
     */
    SELF(0, 0, ProcedureMessage.source_self) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            Set<String> memberPersonIds = BeanHelper.getNameBeanIdSet(members, PRMemberBean.PERSON_ID);
            Map<String, List<PersonForAssignEstimatorBean>> map = new HashMap<>();
            for (List<String> personIds : CollectionUtils.split(memberPersonIds, Pager.INCREASED_MAX_ON_PAGE)) {
                List<PersonForAssignEstimatorBean> persons =
                    PRMassService.getInstance().getPersonsForAssignEstimatorByIds(assignMode, personIds);
                persons.forEach(person ->
                    members.stream()
                        .filter(member -> member.getPerson().getId().equals(person.getId()))
                        .forEach(member -> map.put(member.getId(), Collections.singletonList(person)))
                );
            }
            return map;
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            buttons.add(new AddEstimatorAction(category, ProcedureMessage.add_source_self));
        }
    },
    /**
     * 1- Руководитель N
     */
    MANAGER_N(1, 1, ProcedureMessage.manager_n) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return ComboValueHelper.getComboValueByValue(
                    ManagerAutoAssignment.values(), category.getAutoAssignment().getAutoAssign())
                .getManagerN(members, assignMode);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return ManagerAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return !ManagerAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddPersonEstimatorAction(category, ProcedureMessage.manager_n),
                    ProcedureMessage.as_manager,
                    buttons);
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, ProcedureMessage.manager_n),
                    ProcedureMessage.manager_by_structure,
                    buttons);
        }

        @Override
        public String getShortCaption() {
            return getCaption().toString().substring(0, 1).toUpperCase() + "(N)";
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.manager,
                    CategorySelfAssignmentSource.organization};
        }

		@Override
        public ComboValue[] getSurveyCandidateAutoAssignmentValues() {
			return new ComboValue[] {
					PRSurveyCandidateAutoAssignment.not_assign,
					PRSurveyCandidateAutoAssignment.structure};
		}

		@Override
		public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
			addMassSurveyCandidateStructureChooseActions(buttons, procedure, category,
                    Localized.group(ProcedureMessage.add_manager, Localized.valueOf(" N")));
		}

		@Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
			return ManagerAutoAssignment.work.getManagerN(Collections.singletonList(member), assignMode)
                .getOrDefault(member.getId(), Collections.emptyList());
		}
    },
    /**
     * 2 - Руководитель N+1
     */
    MANAGER_N1(2, 2, ProcedureMessage.manager_n1) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                members,
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getManagersN1(workIds));
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddPersonEstimatorAction(category, ProcedureMessage.manager_n1),
                    ProcedureMessage.as_manager,
                    buttons);
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, ProcedureMessage.manager_n1),
                    ProcedureMessage.manager_by_structure,
                    buttons);
        }

        @Override
        public String getShortCaption() {
            return getCaption().toString().substring(0, 1).toUpperCase() + "(N + 1)";
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.organization};
        }

		@Override
        public ComboValue[] getSurveyCandidateAutoAssignmentValues() {
			return new ComboValue[] {
					PRSurveyCandidateAutoAssignment.not_assign,
					PRSurveyCandidateAutoAssignment.structure};
		}

		@Override
		public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureChooseActions(buttons, procedure, category,
                    Localized.group(ProcedureMessage.add_manager, Localized.valueOf(" N+1")));
		}

		@Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                Collections.singletonList(member),
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getManagersN1(workIds))
                .getOrDefault(member.getId(), Collections.emptyList());
        }
    },
    /**
     * 3 - Руководитель N+2
     */
    MANAGER_N2(3, 3, ProcedureMessage.manager_n2) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                members,
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getManagersN2(workIds));
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddPersonEstimatorAction(category, ProcedureMessage.manager_n2),
                    ProcedureMessage.as_manager,
                    buttons);
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, ProcedureMessage.manager_n2),
                    ProcedureMessage.manager_by_structure,
                    buttons);
        }

        @Override
        public String getShortCaption() {
            return getCaption().toString().substring(0, 1).toUpperCase() + "(N + 2)";
        }
    },
    /**
     * 16 - Руководители всех уровней
     * нет анкет
     */
    MANAGER_ALL_LEVEL(16, 4, DevelopmentMessage.all_levels_managers) {
        @Override
        public boolean withSurveys() {
            return false;
        }

        @Override
        public boolean withCompetenceScore() {
            return false;
        }
    },
    /**
     * 4 - Функциональный руководитель
     */
    FUNC_MANAGER(4, 5, PersonMessage.func_director) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                members,
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getFunctionalManagers(workIds));
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddPersonEstimatorAction(category, PersonMessage.func_director),
                    ProcedureMessage.as_manager,
                    buttons);
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, PersonMessage.func_director),
                    ProcedureMessage.manager_by_structure,
                    buttons);
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.organization};
        }

		@Override
        public ComboValue[] getSurveyCandidateAutoAssignmentValues() {
			return new ComboValue[] {
					PRSurveyCandidateAutoAssignment.not_assign,
					PRSurveyCandidateAutoAssignment.structure};
		}

		@Override
		public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureChooseActions(
                    buttons, procedure, category, ProcedureMessage.add_functional_manager);
		}
		
		@Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                Collections.singletonList(member),
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getFunctionalManagers(workIds))
                .getOrDefault(member.getId(), Collections.emptyList());
        }
    },
    /**
     * 15 - Функциональный руководитель N+1
     */
    FUNC_MANAGER_N1(15, 6, PersonMessage.functional_manager_n1) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                members,
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getFunctionalManagersN1(workIds));
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddPersonEstimatorAction(category, PersonMessage.functional_manager_n1),
                    ProcedureMessage.as_manager,
                    buttons);
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, PersonMessage.functional_manager_n1),
                    ProcedureMessage.manager_by_structure,
                    buttons);
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return FUNC_MANAGER.getSelfAssignmentSourceValues();
        }

        @Override
        public ComboValue[] getSurveyCandidateAutoAssignmentValues() {
            return FUNC_MANAGER.getSurveyCandidateAutoAssignmentValues();
        }

        @Override
        public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureChooseActions(
                    buttons, procedure, category, ProcedureMessage.add_functional_manager_n1);
        }

        @Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
            return getEstimatorsByWork(
                Collections.singletonList(member),
                assignMode,
                workIds -> ProcedureSurveyAssignRepository.getInstance().getFunctionalManagersN1(workIds))
                .getOrDefault(member.getId(), Collections.emptyList());
        }

        @Override
        public String getShortCaption() {
            return getCaption().toString().substring(0, 1).toUpperCase() + "(N + 1)";
        }
    },
    /**
     * 17 - Функциональные руководители всех уровней
     * нет анкет
     */
    FUNC_MANAGER_ALL_LEVEL(17, 7, ProcedureMessage.functional_managers_all_levels) {
        @Override
        public boolean withSurveys() {
            return false;
        }

        @Override
        public boolean withCompetenceScore() {
            return false;
        }
    },
    /**
     * 5 - Эксперты
     */
    EXPERT(5, 8, ProcedureMessage.source_experts) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return personsToEstimators(
                members, PRExpertService.getInstance().getCategoryExperts(category.getId(), assignMode));
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            buttons.add(new RunWizardAction(AddExpertWizard.NAME, ProcedureMessage.add_expert)
                    .addParam(ProcedureBean.ID, category.getProcedure().getId()));
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.organization};
        }

		@Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
			return category.getSelfAssignment().getSource().getEstimators(category, member, assignMode);
		}

		@Override
		public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureProcedureChooseActions(
                    buttons, procedure, category, ProcedureMessage.add_expert, true);
		}

        @Override
		public void addSurveyCandidateAssignmentStructureFields(FieldInfoList fields,
                                                                PRCategorySelfAssignmentFormBean bean) {
            //нередактируемый блок Структура, чтобы было понятно от куда значения при выполнении действия По структуре
            fields.addReadOnly(PRCategorySelfAssignmentFormBean.STRUCTURE_BY_SOURCE);
        }
    },
    /**
     * 6 - Оценочное собеседование
     */
    INTERVIEW(6, 9, ProcedureMessage.interview) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return ComboValueHelper.getComboValueByValue(
                InterviewAutoAssignment.values(), category.getAutoAssignment().getAutoAssign()).getInterviewer(
                    members, procedure, category, assignMode);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return InterviewAutoAssignment.values();
        }

        @Override
        public void processAutoAssignFieldInfo(FieldInfo fieldInfo) {
            fieldInfo.addFieldComponentProcessor(
                    new OnChangeOnInitFieldComponentProcessor(new BlockExpression()
                            .add(ComboBoxExpressionBuilder.createExpressionForShowFields(
                                    InterviewAutoAssignment.person.getValue(),
                                    PrCategoryAutoAssignBean.PERSON_ID))
                            .add(ComboBoxExpressionBuilder.createExpressionForShowFields(
                                    InterviewAutoAssignment.post.getValue(),
                                    PrCategoryAutoAssignBean.POST_ID))
                    ));
        }

        @Override
        public Set<String> getAutoAssignmentFields() {
            return CollectionUtils.newUnorderedSet(
                    PrCategoryAutoAssignBean.PERSON_ID,
                    PrCategoryAutoAssignBean.POST_ID
            );
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return !InterviewAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            List<ClientAction<?>> actions = new ArrayList<>();
            //Оцениваемый, Руководители
            Stream.of(
                    InterviewAutoAssignment.self,
                    InterviewAutoAssignment.manager_n,
                    InterviewAutoAssignment.manager_n1,
                    InterviewAutoAssignment.manager_n2,
                    InterviewAutoAssignment.func_manager)
                    .forEach(assignment -> actions.add(new AddInterviewAction(category, assignment)));
            //Сотрудник
            actions.add(AddPersonEstimatorAction.addOnePerson(category, InterviewAutoAssignment.person.getCaption()));
            //Штатная должность
            if (Service.isExtendedPostEnabled()) {
                actions.add(new AddPostEstimatorAction(category, InterviewAutoAssignment.post.getCaption()));
            }
            new DropDownContainerBuilder(ProcedureMessage.proc_interview, actions).removeChooseClass()
                    .addDropDown(buttons);
        }
    },
    /**
     * 7 - Коллеги
     */
    COLLEAGUES(7, 10, ProcedureMessage.source_colleagues) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return PRCategoryService.getInstance().listColleague(
                members,
                category.getAutoAssignment().getByCa(),
                category.getAutoAssignment().getByPost(),
                category.getAutoAssignment().getBySubordination(),
                category.getAutoAssignment().getByFunctionalManager(),
                false,
                category.getAutoAssignment().getFilterByGroup(),
                category.getAutoAssignment().getGroup().getId(),
                category.getAutoAssignment().getCondition(),
                assignMode);
        }

        @Override
        public List<PersonForAssignEstimatorBean> filterAutoAssignEstimators(List<PersonForAssignEstimatorBean> persons,
                                                                             PRCategoryBean category) {
            return PRCategoryService.getInstance().filterPersonsByCount(
                persons,
                category.getAutoAssignment().getCount(),
                ComboValueHelper.getComboValueByValue(
                    PrCategoryMultiAutoAssignment.values(), category.getAutoAssignment().getAutoAssign()),
                category);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryMultiAutoAssignment.values();
        }

        @Override
        public void processAutoAssignFieldInfo(FieldInfo fieldInfo) {
            fieldInfo.addFieldComponentProcessor(
                    new OnChangeOnInitFieldComponentProcessor(
                            ComboBoxExpressionBuilder.createExpressionForShowFields(
                                    PrCategoryMultiAutoAssignment.random_n.getValue(),
                                    PrCategoryAutoAssignBean.COUNT)));
        }

        @Override
        public Set<String> getAutoAssignmentFields() {
            return CollectionUtils.newUnorderedSet(
                    PrCategoryAutoAssignBean.COUNT,
                    PrCategoryAutoAssignBean.FILTER_BY_GROUP,
                    PrCategoryAutoAssignBean.GROUP_ID,
                    PrCategoryAutoAssignBean.CONDITION,
                    PrCategoryAutoAssignBean.BY_CA,
                    PrCategoryAutoAssignBean.BY_POST,
                    PrCategoryAutoAssignBean.BY_FUNCTIONAL_MANAGER,
                    PrCategoryAutoAssignBean.BY_SUBORDINATION
            );
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return !PrCategoryMultiAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            new DropDownContainerBuilder(
                    ProcedureMessage.add_colleagues,
                    //По структуре
                    new AddEstimatorAction(category, ProcedureMessage.by_structure),
                    //По функциональному руководителю
                    new AddColleaguesByFunctionalAction(category),
                    //Выбор пользователей как коллег
                    AddPersonEstimatorAction.addOnePerson(category),
                    AddPersonEstimatorAction.addSeveralPersons(category))
                    .removeChooseClass()
                    .addDropDown(buttons);
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.organization,
                    CategorySelfAssignmentSource.colleagues_by_org,
                    CategorySelfAssignmentSource.colleagues_by_manager,
                    CategorySelfAssignmentSource.member_organization};
        }

        @Override
		public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureProcedureChooseActions(
                    buttons, procedure, category, ProcedureMessage.add_colleagues, false);
		}

		@Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
            PRCategorySelfAssignmentBean selfAssignmentCategory = category.getSelfAssignment();
            Map<String, List<PersonForAssignEstimatorBean>> memberColleague =
                PRCategoryService.getInstance().listColleague(
                    Collections.singletonList(member),
                    selfAssignmentCategory.getStructureByOrg(),
                    selfAssignmentCategory.getStructureByPost(),
                    selfAssignmentCategory.getStructureBySub(),
                    selfAssignmentCategory.getStructureByFuncManager(),
                    selfAssignmentCategory.getStructureByCity(),
                    selfAssignmentCategory.getStructureByGroup(),
                    selfAssignmentCategory.getStructureGroup().getId(),
                    selfAssignmentCategory.getStructureCondition(),
                    assignMode);
            return memberColleague.containsKey(member.getId()) ? memberColleague.get(member.getId()) : new ArrayList<>();
        }

        @Override
		public void addSurveyCandidateAssignmentStructureFields(FieldInfoList fields,
                                                                PRCategorySelfAssignmentFormBean bean) {
            fields.addField(new FieldInfo(PRCategorySelfAssignmentFormBean.STRUCTURE_GROUP_COMPOSITE)
                    .addFieldComponentProcessor(bean.getStructureGroupFieldProcessor()));
            fields.addFields(
                    PRCategorySelfAssignmentBean.STRUCTURE_CONDITION,
                    PRCategorySelfAssignmentBean.STRUCTURE_BY_POST,
                    PRCategorySelfAssignmentBean.STRUCTURE_BY_SUB,
                    PRCategorySelfAssignmentBean.STRUCTURE_BY_ORG,
                    PRCategorySelfAssignmentBean.STRUCTURE_BY_CITY,
                    PRCategorySelfAssignmentBean.STRUCTURE_BY_FUNC_MANAGER);
        }
    },
    /**
     * 8 - Подчиненные
     */
    INFERIORS(8, 11, ProcedureMessage.source_inferiors) {
        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return PRCategoryService.getInstance().listInferiors(
                members,
                category.getAutoAssignment().getLevelN(),
                category.getAutoAssignment().getLevelN1(),
                category.getAutoAssignment().getAllLevel(),
                category.getAutoAssignment().getByFunctionalManager(),
                category.getAutoAssignment().getFilterByGroup(),
                category.getAutoAssignment().getGroup().getId(),
                category.getAutoAssignment().getCondition(),
                assignMode);
        }

        @Override
        public List<PersonForAssignEstimatorBean> filterAutoAssignEstimators(List<PersonForAssignEstimatorBean> persons,
                                                                             PRCategoryBean category) {
            return PRCategoryService.getInstance().filterPersonsByCount(
                persons,
                category.getAutoAssignment().getCount(),
                ComboValueHelper.getComboValueByValue(
                    PrCategoryMultiAutoAssignment.values(), category.getAutoAssignment().getAutoAssign()),
                category);
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return !PrCategoryMultiAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }
        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryMultiAutoAssignment.values();
        }

        @Override
        public void processAutoAssignFieldInfo(FieldInfo fieldInfo) {
            fieldInfo.addFieldComponentProcessor(
                    new OnChangeOnInitFieldComponentProcessor(
                            ComboBoxExpressionBuilder.createExpressionForShowFields(
                                    PrCategoryMultiAutoAssignment.random_n.getValue(),
                                    PrCategoryAutoAssignBean.COUNT)));
        }

        @Override
        public Set<String> getAutoAssignmentFields() {
            return CollectionUtils.newUnorderedSet(
                    PrCategoryAutoAssignBean.COUNT,
                    PrCategoryAutoAssignBean.FILTER_BY_GROUP,
                    PrCategoryAutoAssignBean.GROUP_ID,
                    PrCategoryAutoAssignBean.CONDITION,
                    PrCategoryAutoAssignBean.ALL_LEVEL,
                    PrCategoryAutoAssignBean.LEVEL_N,
                    PrCategoryAutoAssignBean.LEVEL_N_1,
                    PrCategoryAutoAssignBean.BY_FUNCTIONAL_MANAGER
            );
        }


        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            new DropDownContainerBuilder(
                    ProcedureMessage.add_inferiors,
                    //По структуре
                    new AddEstimatorAction(category, ProcedureMessage.by_structure),
                    //По функциональному руководителю
                    new AddInferiorsByFunctionalAction(category),
                    //Выбор пользователей как подчиненных
                    AddPersonEstimatorAction.addOnePerson(category),
                    AddPersonEstimatorAction.addSeveralPersons(category))
                    .removeChooseClass()
                    .addDropDown(buttons);
        }

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.organization,
                    CategorySelfAssignmentSource.first_level_inferiors,
                    CategorySelfAssignmentSource.all_inferiors};
        }
		@Override
		public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureProcedureChooseActions(
                    buttons, procedure, category, ProcedureMessage.add_inferiors, false);
		}
		@Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
            PRCategorySelfAssignmentBean selfAssignmentCategory = category.getSelfAssignment();
            Map<String, List<PersonForAssignEstimatorBean>> memberInferior =
                PRCategoryService.getInstance().listInferiors(
                    Collections.singletonList(member),
                    selfAssignmentCategory.getStructureLevelN(),
                    selfAssignmentCategory.getStructureLevelN1(),
                    selfAssignmentCategory.getStructureLevelAll(),
                    selfAssignmentCategory.getStructureByFuncManager(),
                    selfAssignmentCategory.getStructureByGroup(),
                    selfAssignmentCategory.getStructureGroup().getId(),
                    selfAssignmentCategory.getStructureCondition(),
                    assignMode);
            return memberInferior.containsKey(member.getId()) ? memberInferior.get(member.getId()) : new ArrayList<>();
        }

        @Override
		public void addSurveyCandidateAssignmentStructureFields(FieldInfoList fields,
                                                                PRCategorySelfAssignmentFormBean bean) {
            fields.addField(new FieldInfo(PRCategorySelfAssignmentFormBean.STRUCTURE_GROUP_COMPOSITE)
                    .addFieldComponentProcessor(bean.getStructureGroupFieldProcessor()));
            fields.addFields(
                    PRCategorySelfAssignmentBean.STRUCTURE_CONDITION,
                    PRCategorySelfAssignmentBean.STRUCTURE_LEVEL_N,
                    PRCategorySelfAssignmentBean.STRUCTURE_LEVEL_N1,
                    PRCategorySelfAssignmentBean.STRUCTURE_LEVEL_ALL,
                    PRCategorySelfAssignmentBean.STRUCTURE_BY_FUNC_MANAGER);
        }
    },
    /**
     * 9 - Группа экспертов
     */
    GROUP(9, 12, ProcedureMessage.group_experts) {
        private static final String choose_from_general_list = "fromgeneral";
        private static final String choose_from_private_list = "fromprivate";
        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            new DropDownContainerBuilder(
                    ProcedureMessage.add_group_experts,
                    //Выбрать всех из частного списка
                    new AddEstimatorAction(category, ProcedureMessage.select_all_from_private_list)
                            .addParam(choose_from_private_list, "1"),
                    //Выбрать несколько из общего списка
                    AddPersonEstimatorAction.addSeveralPersons(PRExpertFrame.NAME, category, ProcedureMessage.select_few_from_general_list),
                    //Выбрать всех из общего списка
                    new AddEstimatorAction(category, ProcedureMessage.select_all_from_general_list)
                            .addParam(choose_from_general_list, "1"),
                    //Выбрать из каталога
                    AddPersonEstimatorAction.addSeveralPersons(category, ProcedureMessage.select_from_catalog),
                    //Ответственные за оценку по организации
                    new AddGroupExpertByCenterResponsibleAction(category))
                    .removeChooseClass()
                    .addDropDown(buttons);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return GroupExpertAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return !GroupExpertAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            if ("1".equals(Context.get().getDataParameter(choose_from_private_list))) {
                 return PRExpertService.getInstance().getMemberExperts(members, assignMode);
            } else if ("1".equals(Context.get().getDataParameter(choose_from_general_list))) {
                return EXPERT.getEstimators(members, procedure, category, assignMode);
            } else {
                //Автоназначение
                return ComboValueHelper.getComboValueByValue(
                        GroupExpertAutoAssignment.values(), category.getAutoAssignment().getAutoAssign())
                        .getEstimators(members, procedure, category, assignMode);
            }
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public Map<String, Set<String>> getSurveyPersonIds(ProcedureBean procedure,
                                                           PRCategoryBean category,
                                                           Collection<PRSurveyBean> surveys,
                                                           Collection<PRMemberBean> members) {
            Map<String, Set<String>> personIdsBySurveyId = new HashMap<>();
            surveys.stream()
                .filter(survey -> !survey.getExpertPersons().isEmpty())
                .forEach(survey -> personIdsBySurveyId.put(survey.getId(), survey.getExpertPersonIds()));
            return personIdsBySurveyId;
        }

        @Override
        public Set<String> getCategorySurveyPersonIds(String memberId, Optional<String> additionalIdOptional) {
            List<PRSurveyBean> surveys = PRSurveyService.getInstance().getMemberActiveSurveyByCategory(
                    memberId, this);
            Set<String> personIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(surveys)) {
                surveys.forEach(survey -> personIds.addAll(survey.getExpertPersonIds()));
            }
            return personIds;
        }
    },
	/**
	 * 10 - Наблюдатели
	 * нет анкет
	 */
	OBSERVERS(10, 13, ProcedureMessage.observers) {
        @Override
        public FieldInfoList getCategoryFields() {
            return new FieldInfoList().addFields(
                    PRCategoryBean.OBSERVER_SOURCE,
                    PRCategoryBean.OBSERVER_GROUP_ID,
                    PRCategoryBean.OBSERVER_PERSON_ID);
        }

        @Override
        public boolean withSurveys() {
            return false;
        }

        @Override
        public boolean withCompetenceScore() {
            return false;
        }
	},
    /**
     * 13 - Результаты другой процедуры
     */
    PROCEDURE(13, 14, ProcedureMessage.result_other_procedure) {
		@Override
		public FieldInfoList getCategoryFields() {
			return new FieldInfoList().addField(
				new FieldInfo<>(PRCategoryBean.PROCEDURE_TYPE_ID)
					.addFieldComponentProcessor(new NotNullFieldComponentProcessor())
			);
		}

        @Override
        public String getFieldValueForCompare() {
            return PRCategoryBean.PROCEDURE_TYPE_ID;
        }

        @Override
        public Optional<NameBean> getValueForCompare(ProcedureEstimatingCategorySupplier category) {
            return category.getProcedureType().isIdNull() ?
                Optional.empty() : Optional.of(category.getProcedureType());
        }

        @Override
        public void setValueForCompare(ProcedureEstimatingCategorySupplier category, String valueId) {
            category.getProcedureType().setId(valueId);
        }

        @Override
        public String getLookupObjectType() {
            return ProcedureTypeRubricator.PROCEDURE_TYPE;
        }

        @Override
        public boolean withSurveys() {
            return false;
        }

        @Override
        public boolean isAutoCreate() {
            return false;
        }

        @Override
        public boolean isAddNotAuto(ProcedureBean procedure) {
            return procedure.getAdditional().getIsQua();
        }

        @Override
        public EstimatingCategoryQuaRequirementSettings getQuaRequirementSettings() {
            return new EstimatingCategoryCustomQuaRequirementSettings(this) {
                @Override
                public LocalizedMessage getAddCategoryCaption() {
                    return ProcedureMessage.add_result_other_procedure;
                }

                @Override
                public boolean isAddCategoryActionInDropDown() {
                    return false;
                }

                @Override
                public Collection<QuaAssessmentColumns> getQuaColumns() {
                    return CollectionUtils.newUnorderedSet(
                        QuaAssessmentColumns.importance, QuaAssessmentColumns.indicators);
                }
            };
        }
    },
    /**
     * 14 - Ответственные за центр оценки
     */
    ASSESSMENT_CENTER_RESPONSIBLE(14, 15, DevelopmentMessage.responsible_assessment_center) {
        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, ProcedureMessage.center_estimated),
                    ProcedureMessage.add_responsible_assessment_center,
                    buttons);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isSurveyWithPerson() {
            return false;
        }

        @Override
        public Map<String, Set<String>> getSurveyPersonIds(ProcedureBean procedure,
                                                           PRCategoryBean category,
                                                           Collection<PRSurveyBean> surveys,
                                                           Collection<PRMemberBean> members) {
            Map<String, Set<String>> personIdsBySurveyId = new HashMap<>();
            Map<String, Set<String>> memberIdsByCenterId =
                PRMemberService.getInstance().getMemberIdsByCenterId(BeanHelper.getIdSet(members));
            memberIdsByCenterId.forEach((centerId, memberIds) -> {
                Set<String> personIds =
                    AssessmentCenterService.getInstance().getProcedureAssessmentCenterResponsiblePersonIds(
                        centerId, procedure.getType().getId());
                if (!personIds.isEmpty()) {
                    memberIds.forEach(memberId ->
                        surveys.stream()
                            .filter(survey ->
                                memberId.equals(survey.getMember().getId()) &&
                                category.getId().equals(survey.getCategory().getId()))
                            .findFirst()
                            .ifPresent(survey -> personIdsBySurveyId.put(survey.getId(), personIds)));
                }
            });
            return personIdsBySurveyId;
        }

        @Override
        public Set<String> getCategorySurveyPersonIds(String memberId, Optional<String> additionalCategoryId) {
            Optional<ProcedureBean> procedureOptional = ProcedureService.getInstance().getProcedureByMemberId(memberId);
            if (!procedureOptional.isPresent()) {
                return Collections.emptySet();
            }
            String centerId = PRMemberService.getInstance().getMemberCenterIds(Collections.singleton(memberId))
                .get(memberId);
            if (StringHelper.isEmpty(centerId)) {
                return Collections.emptySet();
            } else {
                return AssessmentCenterService.getInstance().getProcedureAssessmentCenterResponsiblePersonIds(
                    centerId, procedureOptional.get().getType().getId());
            }
        }
    },
    /**
     * 18 - Ответственный за вышестоящие центры оценки
     */
    HIGHER_ASSESSMENT_CENTER_RESPONSIBLE(18, 16, ProcedureMessage.responsible_higher_assessment_centers) {
        @Override
        public boolean isOneSurvey() {
            return true;
        }

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            addActionToDropDownContainer(
                    new AddEstimatorAction(category, ProcedureMessage.higher_centers),
                    ProcedureMessage.add_responsible_assessment_center,
                    buttons);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isSurveyWithPerson() {
            return false;
        }

        @Override
        public Map<String, Set<String>> getSurveyPersonIds(ProcedureBean procedure,
                                                           PRCategoryBean category,
                                                           Collection<PRSurveyBean> surveys,
                                                           Collection<PRMemberBean> members) {
            Map<String, Set<String>> personIdsBySurveyId = new HashMap<>();
            Map<String, Set<String>> memberIdsByCenterId =
                PRMemberService.getInstance().getMemberIdsByCenterId(BeanHelper.getIdSet(members));
            memberIdsByCenterId.forEach((centerId, memberIds) -> {
                Set<String> personIds =
                    AssessmentCenterService.getInstance().getHigherProcedureAssessmentCenterResponsiblePersonIds(
                        centerId, procedure.getType().getId());
                if (!personIds.isEmpty()) {
                    memberIds.forEach(memberId ->
                        surveys.stream()
                            .filter(survey ->
                                memberId.equals(survey.getMember().getId()) &&
                                    category.getId().equals(survey.getCategory().getId()))
                            .findFirst()
                            .ifPresent(survey -> personIdsBySurveyId.put(survey.getId(), personIds)));
                }
            });
            return personIdsBySurveyId;
        }

        @Override
        public Set<String> getCategorySurveyPersonIds(String memberId, Optional<String> additionalCategoryId) {
            Optional<ProcedureBean> procedureOptional = ProcedureService.getInstance().getProcedureByMemberId(memberId);
            if (!procedureOptional.isPresent()) {
                return Collections.emptySet();
            }
            String centerId = PRMemberService.getInstance().getMemberCenterIds(Collections.singleton(memberId))
                .get(memberId);
            if (StringHelper.isEmpty(centerId)) {
                return Collections.emptySet();
            } else {
                return AssessmentCenterService.getInstance().getHigherProcedureAssessmentCenterResponsiblePersonIds(
                    centerId, procedureOptional.get().getType().getId());
            }
        }
    },
    /**
     * 11 - Дополнительная категория
     */
    ADDITIONAL(11, 18, ProcedureMessage.additional_category) {
        @Override
        public boolean isAutoCreate() {
            return false;
        }

        @Override
        public String getFieldValueForCompare() {
            return PRCategoryBean.ADD_CATEGORY_ID;
        }

        @Override
        public Optional<NameBean> getValueForCompare(ProcedureEstimatingCategorySupplier category) {
            return category.getAddCategory().isIdNull() ? Optional.empty() : Optional.of(category.getAddCategory());
        }

        @Override
        public void setValueForCompare(ProcedureEstimatingCategorySupplier category, String valueId) {
            category.getAddCategory().setId(valueId);
        }

        @Override
        public Optional<NameBean> getValueForCompare(PhaseSourceBean phaseSource, ProcedurePhaseSourceBean procedurePhaseSource) {
            return procedurePhaseSource.getAddCategory().isIdNull() ?
                    Optional.empty() : Optional.of(procedurePhaseSource.getAddCategory());
        }

        @Override
        public String getLookupObjectType() {
            return ProcedureCategoryRubricator.CATEGORY;
        }

		@Override
		public FieldInfoList getCategoryFields() {
			return new FieldInfoList().addField(
				new FieldInfo<>(PRCategoryBean.ADD_CATEGORY_ID)
					.addFieldComponentProcessor(new NotNullFieldComponentProcessor())
			);
		}

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            new DropDownContainerBuilder(
                    Localized.valueOf(category.getAddCategory().getName()),
                    //Выбрать несколько из общего списка
                    AddPersonEstimatorAction.addSeveralPersons(
                            PRExpertFrame.NAME, category, ProcedureMessage.select_few_from_general_list),
                    //Выбрать всех из общего списка
                    new AddEstimatorAction(category, ProcedureMessage.select_all_from_general_list),
                    //Выбрать из каталога
                    AddPersonEstimatorAction.addSeveralPersons(category, ProcedureMessage.select_from_catalog),
                    //По настройкам
                    new AddAdditionalCategoryBySettingsAction(category))
                    .removeChooseClass()
                    .addDropDown(buttons);
        }

        @Override
        public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                             ProcedureBean procedure,
                                                                             PRCategoryBean category,
                                                                             ProcedureSurveyAssignMode assignMode) {
            return ComboValueHelper.getComboValueByValue(
                    AdditionalAutoAssignment.values(), category.getAutoAssignment().getAutoAssign())
                    .getEstimators(category, members, assignMode);
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return AdditionalAutoAssignment.values();
        }

        @Override
		public boolean isAutoAssign(PRCategoryBean category) {
            return !AdditionalAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
		}

        @Override
        public boolean isSelfAssignment() {
            return true;
        }

        @Override
        public ComboValue[] getSelfAssignmentSourceValues() {
            return new ComboValue[] {
                    CategorySelfAssignmentSource.all_person,
                    CategorySelfAssignmentSource.own_employees,
                    CategorySelfAssignmentSource.group,
                    CategorySelfAssignmentSource.organization};
        }

        @Override
        public ComboValue[] getSurveyCandidateAutoAssignmentValues() {
            return new ComboValue[] {
                    PRSurveyCandidateAutoAssignment.not_assign,
                    PRSurveyCandidateAutoAssignment.procedure};
        }

        @Override
        public void addSurveyCandidateCreateActions(GridButtonsElement buttons,
                                                    ProcedureBean procedure,
                                                    PRCategoryBean category) {
            addMassSurveyCandidateStructureProcedureChooseActions(
                    buttons, procedure, category, Localized.valueOf(category.getCategoryName()), true);
        }

        @Override
        public List<PersonForAssignEstimatorBean> getPersonCandidateByStructure(PRCategoryBean category,
                                                                                PRMemberBean member,
                                                                                ProcedureSurveyAssignMode assignMode) {
            return category.getSelfAssignment().getSource().getEstimators(category, member, assignMode);
        }

        @Override
		public void addSurveyCandidateAssignmentStructureFields(FieldInfoList fields,
                                                                PRCategorySelfAssignmentFormBean bean) {
            fields.addReadOnly(PRCategorySelfAssignmentFormBean.STRUCTURE_BY_SOURCE);
        }
    },
    /**
     * 12 - Дополнительная категория Группа физических лиц
     * Одна анкета на всю группу
     */
    ADDITIONAL_GROUP(12, 19, PersonMessage.group_persons) {
        @Override
        public boolean isAutoCreate() {
            return false;
        }

        @Override
        public String getFieldValueForCompare() {
            return PRCategoryBean.PERSON_GROUP_ID;
        }

        @Override
        public Optional<NameBean> getValueForCompare(ProcedureEstimatingCategorySupplier category) {
            return category.getPersonGroup().isIdNull() ? Optional.empty() : Optional.of(category.getPersonGroup());
        }

        @Override
        public void setValueForCompare(ProcedureEstimatingCategorySupplier category, String valueId) {
            category.getPersonGroup().setId(valueId);
        }

        @Override
        public Optional<NameBean> getValueForCompare(PhaseSourceBean phaseSource, ProcedurePhaseSourceBean procedurePhaseSource) {
            return phaseSource.getGroupPerson().isIdNull() ?
                    Optional.empty() : Optional.of(phaseSource.getGroupPerson());
        }

        @Override
        public String getLookupObjectType() {
            return PersonGroupCatalog.NAME;
        }

        @Override
        public boolean isOneSurvey() {
            return true;
        }

		@Override
		public FieldInfoList getCategoryFields() {
			return new FieldInfoList().addField(
				new FieldInfo<>(PRCategoryBean.PERSON_GROUP_ID)
					.addFieldComponentProcessor(new NotNullFieldComponentProcessor())
			);
		}

        @Override
        public void addSurveyCreateActions(GridButtonsElement buttons, PRCategoryBean category) {
            buttons.add(new AddEstimatorAction(category, Localized.valueOf(category.getCategoryName())));
        }

        @Override
        public boolean isAutoAssign(PRCategoryBean category) {
            return PrCategoryAutoAssignment.yes.getValue().equals(category.getAutoAssignment().getAutoAssign());
        }

        @Override
        public boolean isAutoAssign() {
            return true;
        }

        @Override
        public ComboValue[] getAutoAssignValues() {
            return PrCategoryAutoAssignment.values();
        }

        @Override
        public boolean isSurveyWithPerson() {
            return false;
        }

        @Override
        public Map<String, Set<String>> getSurveyPersonIds(ProcedureBean procedure,
                                                           PRCategoryBean category,
                                                           Collection<PRSurveyBean> surveys,
                                                           Collection<PRMemberBean> members) {
            Map<String, Set<String>> personIdsBySurveyId = new HashMap<>();
            Set<String> personIds = getGroupPersonIds(category);
            if (!personIds.isEmpty()) {
                surveys.forEach(survey -> personIdsBySurveyId.put(survey.getId(), personIds));
            }
            return personIdsBySurveyId;
        }

        private Set<String> getGroupPersonIds(PRCategoryBean category) {
            SelectQuery query = ORM.getInstance().getDataObject(PersonGroupObjLinkBean.class).getQuery();
            query.where(
                Column.column(DataObject.PARENT_ALIAS, PersonGroupObjLinkBean.GROUP_ID).eq(Parameter.parameter()));
            query.getOrderByClause().addValue(Column.column(DataObject.PARENT_ALIAS, PersonGroupObjLinkBean.OBJ_ID));
            QueryData<SelectQuery> queryData = QueryData.fromQuery(query).addIntParam(category.getPersonGroup().getId());
            BeanQueryPagingIterator<PersonGroupObjLinkBean> iterator = BeanQueryPagingIterator.create(
                Pager.defaultLimit(), new SelectQueryData(queryData), PersonGroupObjLinkBean.class);
            return BeanHelper.getValueSet(
                CollectionUtils.stream(iterator).flatMap(List::stream).collect(Collectors.toList()),
                PersonGroupObjLinkBean.OBJ_ID);
        }

        @Override
        public Set<String> getCategorySurveyPersonIds(String memberId, Optional<String> additionalIdOptional) {
            return PRCategoryService.getInstance().getCategoryByMember(
                    memberId, this)
                    .map(this::getGroupPersonIds).orElse(new HashSet<>());
        }
    }
    ;

    private int value;
    private int order;
    private LocalizedMessage systemCaption;

    EstimatingCategory(int value, int order, LocalizedMessage caption) {
        this.value = value;
        this.order = order;
        systemCaption = caption;
    }

    @Override
    public String getValue() {
        return Integer.toString(value);
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    @Override
    public String getName() {
        return name();
    }

    public LocalizedMessage getSystemCaption() {
        return systemCaption;
    }

    @Override
    public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
                                                                         ProcedureBean procedure,
                                                                         PRCategoryBean category,
                                                                         ProcedureSurveyAssignMode assignMode) {
        return new HashMap<>();
    }

    public Map<String, List<PersonForAssignEstimatorBean>> getEstimatorsByWork(
            Collection<PRMemberBean> members,
            ProcedureSurveyAssignMode assignMode,
            Function<List<String>, QueryData<SelectQuery>> queryDataByWorkIds) {
        Map<String, List<PersonForAssignEstimatorBean>> estimators = new HashMap<>();
        Set<String> memberWorkIds = BeanHelper.getValueSet(members, PRMemberBean.WORK_ID);
        for (List<String> workIds : CollectionUtils.split(memberWorkIds, Pager.INCREASED_MAX_ON_PAGE)) {
            List<DataSet> dataSets = Session.list(assignMode.updatePersonQueryData(queryDataByWorkIds.apply(workIds)));
            dataSets.forEach(dataSet -> {
                String workId = dataSet.getValue(PersonWorkBean.ID);
                PersonForAssignEstimatorBean person =
                    BeanHelper.createFromDBDataSet(PersonForAssignEstimatorBean.class, dataSet);
                members.stream()
                    .filter(member -> workId.equals(member.getWorkId()))
                    .forEach(member -> estimators.put(member.getId(), Collections.singletonList(person)));
            });
        }
        return estimators;
    }

    public Map<String, List<PersonForAssignEstimatorBean>> personsToEstimators(
            Collection<PRMemberBean> members,
            List<PersonForAssignEstimatorBean> persons) {
        Map<String, List<PersonForAssignEstimatorBean>> map = new HashMap<>();
        members.forEach(member -> map.put(member.getId(), persons));
        return map;
    }

    public boolean isEquals(String otherCategory) {
        return getValue().equals(otherCategory);
    }


    /**
     * Действия массового добавления анкет-претендентов
     * По структуре
     * Из другой процедуры
     * Выбрать одного
     * Выбрать несколько
     * Выбрать из каталога
     * @param notShowStructureIfSourceAll По структуре не выводить, если источник Все пользователи
     */
    protected void addMassSurveyCandidateStructureProcedureChooseActions(GridButtonsElement buttons,
                                                                         ProcedureBean procedure,
                                                                         PRCategoryBean category,
                                                                         LocalizedMessage caption,
                                                                         boolean notShowStructureIfSourceAll) {
        CategorySelfAssignmentSource source = category.getSelfAssignment().getSource();
        List<ClientAction<?>> actions = new ArrayList<>();
        if (!category.getSelfAssignment().getSource().isAllPerson() || !notShowStructureIfSourceAll) {
            actions.add(new AddStructureSurveyCandidateAction(category.getId()));
        }
        if (!procedure.getSurveyCandidateProcedures().isEmpty()) {
            actions.add(new AddFromProcedureSurveyCandidateAction(category.getId()));
        }
        if (source.isMassAssignment()) {
            actions.add(AddPersonSurveyCandidateAction.addOnePerson(category.getId()));
            actions.add(AddPersonSurveyCandidateAction.addSeveralPersons(category.getId()));
        }
        processMassSurveyCandidateActions(actions, procedure, category);
        new DropDownContainerBuilder(caption, actions).removeChooseClass().addDropDown(buttons);
    }

    /**
     * Действия массового добавления анкет-претендентов
     * По структуре
     * Выбрать одного
     * Выбрать из каталога
     */
    protected void addMassSurveyCandidateStructureChooseActions(GridButtonsElement buttons,
                                                                ProcedureBean procedure,
                                                                PRCategoryBean category,
                                                                LocalizedMessage caption) {
        CategorySelfAssignmentSource source = category.getSelfAssignment().getSource();
        List<ClientAction<?>> actions = new ArrayList<>();
        actions.add(new AddStructureSurveyCandidateAction(category.getId()));
        if (source.isMassAssignment()) {
            actions.add(AddPersonSurveyCandidateAction.addOnePerson(category.getId()));
        }
        processMassSurveyCandidateActions(actions, procedure, category);
        new DropDownContainerBuilder(caption, actions).removeChooseClass().addDropDown(buttons);
    }

	/**
	 * Администратор может выбирать анкеты-претенденты из всего каталога физ.лиц
	 */
	protected void processMassSurveyCandidateActions(List<ClientAction<?>> actions,
                                                     ProcedureBean procedure,
                                                     PRCategoryBean category) {
		if (procedure.getChooseSurveyCandidateFromCatalog()
                && Service.isUser(LMSUser.ADMIN_LOCAL_ADMIN_ASSESSMENT_CENTER_ADMIN_PC_EXTENDED_ADMIN_CAADMIN_PROFILES)) {
			if (category.getCategory().isOneSurvey()) {
				actions.add(AddPersonSurveyCandidateAction.addOnePersonFromCatalog(category.getId()));
			} else {
				actions.add(AddPersonSurveyCandidateAction.addSeveralPersonsFromCatalog(category.getId()));
			}
		}
	}
}
