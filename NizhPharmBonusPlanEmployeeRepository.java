package ext.nizhpharm.hr.bonus.plan.employee;

import ext.nizhpharm.NizhPharmModule;
import hr.bonus.plan.employee.BonusPlanEmployeeBean;
import lms.core.person.PersonBean;
import org.mirapolis.core.Module;
import org.mirapolis.data.bean.reflect.repository.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для BonusPlanEmployee
 *
 * @author Andrey Ermolaev
 * @since 11.09.2024
 */

@Repository
@Module(NizhPharmModule.class)
public interface NizhPharmBonusPlanEmployeeRepository {

	/**
	 * @param bonusId айди {@link BonusPlanEmployeeBean}
	 * @return возвращает список физ лиц c&p который были сохранены при формировании
	 */
	@Select(
	"SELECT L1.personid, L1.pfio\n"
		+ "FROM bns$npbpemployee P\n"
		+ "LEFT OUTER JOIN bns$npbpemployee_MULTI_cacpperson L0 ON P.bnspeid = L0.bnspeid\n"
		+ "LEFT OUTER JOIN pp$person L1 ON L0.personid = L1.personid\n"
		+ "WHERE L0.bnspeid = :bonusId"
	)
	List<PersonBean> getCpPartners(String bonusId);
}
