package ext.nizhpharm.hr.bonus.plan.employee;

import ext.nizhpharm.NizhPharmMessage;
import ext.nizhpharm.ca.post.PostNizhPharmFrame;
import hr.bonus.BonusMessage;
import hr.bonus.plan.employee.BonusPlanEmployeeBean;
import lms.core.ca.CAFrame;
import lms.core.ca.post.PostFrame;
import lms.core.ca.post.PostMessage;
import lms.core.newprocedure.ProcedureMessage;
import lms.core.person.PersonFrame;
import lms.core.person.PersonLookupFieldBuilder;
import lms.core.person.PersonMessage;
import lms.core.person.work.WorkFieldService;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.data.bean.reflect.Name;
import org.mirapolis.data.bean.reflect.ReflectDataBean;
import org.mirapolis.mvc.model.entity.datafields.LookupField;
import org.mirapolis.mvc.model.entity.datafields.MultiLookupField;
import org.mirapolis.mvc.model.entity.fields.LookupFieldBuilder;
import org.mirapolis.orm.DataObject;
import org.mirapolis.orm.fields.DoubleField;
import org.mirapolis.orm.fields.StringField;
import org.mirapolis.service.message.Localized;

import java.util.Set;

/**
 * Дочерний бин для {@link BonusPlanEmployeeBean}
 *
 * @author ErmolaevAn
 * @since 20.08.2024
 */
public class NizhPharmBonusPlanEmployeeBean extends ReflectDataBean {
    public static final String DATANAME = "BNS$NPBPEMPLOYEE";
    public static final String ALIAS = "BPNP";

    public static final String EXT_FIELD_NAME = "bonusplanemployeenizhpharm";

    public static final String DIRECTOR_ID = "bpdirectorid";
    public static final String MANAGER_SECOND_LEVEL = "bpseniormanagement";
    public static final String CP_PERSON = "cacpperson";
    public static final String PERSON_ID_CODE = "personidcode";
    public static final String PERSON_GRID = "persongrid";
    public static final String PERSON_POST = "personpost";
    public static final String PERSON_CA = "personca";
    public static final String PERSON_SEC = "personsec";
    public static final String PERSON_FTE = "personfte";

    /**
     * Руководитель
     */
    @Name(DIRECTOR_ID)
    private NameBean director;

    /**
     * Руководитель второго уровня
     */
    @Name(MANAGER_SECOND_LEVEL)
    private NameBean seniorManagement;

    /**
     * C&P партнер (физическое лицо)
     */
    @Name(CP_PERSON)
    private Set<NameBean> cpPartners;

    /**
     * ID физлица
     */
    @Name(PERSON_ID_CODE)
    private String flID;

    /**
     * Грейд
     */
    @Name(PERSON_GRID)
    private String gridID;

    /**
     * Штатная должность
     */
    @Name(PERSON_POST)
    private NameBean postID;

    /**
     * Подразделение
     */
    @Name(PERSON_CA)
    private NameBean caID;

    /**
     * SEC
     */
    @Name(PERSON_SEC)
    private NameBean flSec;

    /**
     * FTE
     */
    @Name(PERSON_FTE)
    private Double flFte;


    public NameBean getDirector() {
        return director;
    }

    public void setDirector(NameBean director) {
        this.director = director;
    }

    public NameBean getSeniorManagement() {
        return seniorManagement;
    }

    public void setSeniorManagement(NameBean seniorManagement) {
        this.seniorManagement = seniorManagement;
    }

    public Set<NameBean> getCpPartners() {
        return cpPartners;
    }

    public void setCpPartners(Set<NameBean> cpPartners) {
        this.cpPartners = cpPartners;
    }

    public String getFlID() {
        return flID;
    }

    public void setFlID(String flID) {
        this.flID = flID;
    }

    public String getGridID() {
        return gridID;
    }

    public void setGridID(String gridID) {
        this.gridID = gridID;
    }

    public NameBean getPostID() {
        return postID;
    }

    public void setPostID(NameBean postID) {
        this.postID = postID;
    }

    public NameBean getCaID() {
        return caID;
    }

    public void setCaID(NameBean caID) {
        this.caID = caID;
    }

    public NameBean getFlSec() {
        return flSec;
    }

    public void setFlSec(NameBean flSec) {
        this.flSec = flSec;
    }

    public Double getFlFte() {
        return flFte;
    }

    public void setFlFte(Double flFte) {
        this.flFte = flFte;
    }

    public static DataObject createDataObject() {
        WorkFieldService workFieldService = WorkFieldService.getInstance();
        return new DataObject(DATANAME, BonusMessage.bonus_participant, NizhPharmBonusPlanEmployeeBean.class)
                .setFields(
                        new PersonLookupFieldBuilder(
                                new LookupField(
                                        DIRECTOR_ID,
                                        ProcedureMessage.current_manager,
                                        PersonFrame.NAME,
                                        LookupField.SET_NULL
                                )
                        ).getDataField(),
                        new PersonLookupFieldBuilder(
                                new LookupField(
                                        MANAGER_SECOND_LEVEL,
                                        NizhPharmMessage.manager_second_level,
                                        PersonFrame.NAME,
                                        LookupField.SET_NULL
                                )
                        ).getDataField(),
                        new MultiLookupField(CP_PERSON, NizhPharmMessage.cp_partners, PersonFrame.NAME),
                        new StringField(
                                PERSON_ID_CODE,
                                Localized.valueOf("ID")
                        ),
                        new StringField(
                                PERSON_GRID,
                                PostMessage.grade
                        ),
                        new LookupFieldBuilder(
                                new LookupField(
                                        PERSON_POST,
                                        PersonMessage.state_post,
                                        PostFrame.NAME,
                                        LookupField.SET_NULL
                                )
                        ).getDataField(),
                        new LookupFieldBuilder(
                                new LookupField(
                                        PERSON_CA,
                                        PersonMessage.division,
                                        CAFrame.NAME,
                                        LookupField.SET_NULL
                                )
                        ).getDataField(),
                        new PersonLookupFieldBuilder(
                                new LookupField(
                                        PERSON_SEC,
                                        NizhPharmMessage.sec,
                                        PersonFrame.NAME,
                                        LookupField.SET_NULL
                                )
                        ).getDataField(),
                        new DoubleField(PERSON_FTE, NizhPharmMessage.fte)
                );
    }

    @Override
    public String getDataName() {
        return DATANAME;
    }
}
