package ext.nizhpharm.newprocedure.category.autoassignment;

import ext.nizhpharm.newprocedure.assignment.mass.NizhpharmProcedureSurveyAssignRepository;
import ext.nizhpharm.newprocedure.category.CPPartnersCategory;
import lms.core.newprocedure.assignment.mass.PersonForAssignEstimatorBean;
import lms.core.newprocedure.assignment.mass.ProcedureSurveyAssignMode;
import lms.core.newprocedure.member.PRMemberBean;
import org.mirapolis.core.SystemMessages;
import org.mirapolis.orm.ComboValue;
import org.mirapolis.service.message.LocalizedMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Автоназначение категории C&P партнер
 *
 * @author Anatoliy Korbasov
 * @since 23.07.2024
 */
public enum CPPartnersAutoAssignment implements ComboValue {
	/**
	 * Нет
	 */
	no(SystemMessages.no),
	/**
	 * Да
	 */
	yes(SystemMessages.yes) {
		@Override
		public Map<String, List<PersonForAssignEstimatorBean>> getCPPartner(Collection<PRMemberBean> members,
																			ProcedureSurveyAssignMode assignMode) {
			CPPartnersCategory category = new CPPartnersCategory();
			return category.getEstimatorsByWork(
				members,
				assignMode,
				workIds -> NizhpharmProcedureSurveyAssignRepository.getInstance().getCPPartnersByWorkIds(workIds));
		}
	};

	private LocalizedMessage caption;

	CPPartnersAutoAssignment(LocalizedMessage caption) {
		this.caption = caption;
	}

	@Override
	public LocalizedMessage getCaption() {
		return caption;
	}

	@Override
	public String getValue() {
		return Integer.toString(ordinal());
	}

	public Map<String, List<PersonForAssignEstimatorBean>> getCPPartner(Collection<PRMemberBean> members,
																		ProcedureSurveyAssignMode assignMode) {
		return new HashMap<>();
	}
}
