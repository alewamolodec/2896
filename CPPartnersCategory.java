package ext.nizhpharm.newprocedure.category;

import ext.nizhpharm.newprocedure.category.autoassignment.CPPartnersAutoAssignment;
import ext.terralink.TerraMessage;
import lms.core.newprocedure.ProcedureBean;
import lms.core.newprocedure.assignment.mass.PersonForAssignEstimatorBean;
import lms.core.newprocedure.assignment.mass.ProcedureSurveyAssignMode;
import lms.core.newprocedure.category.PRCategoryBean;
import lms.core.newprocedure.category.ProcedureEstimatingCategory;
import lms.core.newprocedure.member.PRMemberBean;
import lms.core.newprocedure.surveycandidate.CategorySelfAssignmentSource;
import lms.core.person.work.PersonWorkBean;
import org.mirapolis.data.DataSet;
import org.mirapolis.data.bean.BeanHelper;
import org.mirapolis.db.Session;
import org.mirapolis.orm.ComboValue;
import org.mirapolis.orm.ComboValueHelper;
import org.mirapolis.service.message.LocalizedMessage;
import org.mirapolis.service.spring.BeanFactory;
import org.mirapolis.sql.Pager;
import org.mirapolis.sql.QueryData;
import org.mirapolis.sql.fragment.SelectQuery;
import org.mirapolis.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;

/**
 * 21 - C&P партнер
 *
 * @author Anatoliy Korbasov
 * @since 04.08.2024
 */
public class CPPartnersCategory implements ProcedureEstimatingCategory {
	public static final String VALUE = "21";

	public CPPartnersCategory() {
		BeanFactory.autoWire(this);
	}

	@Override
	public String getValue() {
		return VALUE;
	}

	@Override
	public Integer getOrder() {
		return 21;
	}

	@Override
	public String getName() {
		return "C&P Partner";
	}

	@Override
	public LocalizedMessage getSystemCaption() {
		return TerraMessage.person_cp_partner;
	}

	@Override
	public Map<String, List<PersonForAssignEstimatorBean>> getEstimators(Collection<PRMemberBean> members,
																		 ProcedureBean procedure,
																		 PRCategoryBean category,
																		 ProcedureSurveyAssignMode assignMode) {
		return ComboValueHelper.getComboValueByValue(
				CPPartnersAutoAssignment.values(), category.getAutoAssignment().getAutoAssign())
			.getCPPartner(members, assignMode);
	}


	@Override
	public boolean isAutoAssign() {
		return true;
	}

	@Override
	public ComboValue[] getAutoAssignValues() {
		return CPPartnersAutoAssignment.values();
	}

	@Override
	public boolean isAutoAssign(PRCategoryBean category) {
		return !CPPartnersAutoAssignment.no.getValue().equals(category.getAutoAssignment().getAutoAssign());
	}

	@Override
	public boolean isOneSurvey() {
		return true;
	}

	@Override
	public boolean isSelfAssignment() {
		return true;
	}

	@Override
	public ComboValue[] getSelfAssignmentSourceValues() {
		return new ComboValue[]{
			CategorySelfAssignmentSource.all_person,
			CategorySelfAssignmentSource.own_employees,
			CategorySelfAssignmentSource.group,
			CategorySelfAssignmentSource.manager,
			CategorySelfAssignmentSource.organization};
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

}
