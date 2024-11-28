package lms.core.newprocedure.assignment.mass;

import lms.core.newprocedure.ProcedureMessage;
import lms.core.newprocedure.category.PRCategoryBean;
import lms.core.newprocedure.category.ProcedureEstimatingCategory;
import lms.core.newprocedure.category.ProcedureEstimatingCategoryFieldConverter;
import lms.core.newprocedure.category.ProcedureEstimatingCategoryStore;
import lms.core.newprocedure.member.PRMemberBean;
import lms.core.newprocedure.survey.PRSurveyBean;
import lms.core.person.PersonBean;
import lms.core.person.PersonMessage;
import org.mirapolis.core.SystemMessages;
import org.mirapolis.data.bean.NameBean;
import org.mirapolis.data.bean.beanfieldconverter.Converter;
import org.mirapolis.data.bean.reflect.Name;
import org.mirapolis.data.bean.reflect.VirtualReflectDataBean;
import org.mirapolis.mvc.model.grid.bean.BeanDataSetList;
import org.mirapolis.orm.DataField;
import org.mirapolis.orm.fields.CheckField;
import org.mirapolis.orm.fields.ComboField;
import org.mirapolis.orm.fields.StringField;
import org.mirapolis.util.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Массовое назначение
 * Оценочные анкеты
 *
 * @author Elena Puzakova
 * @since 10.06.2019 7:46
 */
public class PRMassSurveyBean extends VirtualReflectDataBean {
    public static final String EMPTY_ESTIMATOR_ID = "-1";

    public static final String ID = BeanDataSetList.ID;
    public static final String CHOOSE = "choose";
    public static final String ESTIMATED_MEMBER_ID = PRMemberBean.ID;
    public static final String ESTIMATED_PERSON_ID = PersonBean.ID;
    public static final String ESTIMATED_PERSON_NAME = "personname";
    public static final String ESTIMATED_PERSON_POST = "personpost";
    public static final String ESTIMATOR_ID = "estimatorid";
    public static final String ESTIMATOR_NAME = "estimatorname";
    public static final String ESTIMATOR_POST = "estimatorpost";
    public static final String CATEGORY_VALUE = "category";
    public static final String CATEGORY_ID = "categoryid";
    public static final String CATEGORY_NAME = "categoryname";
    public static final String EXPERT_CATEGORY_ID = PRSurveyBean.EXPERT_CATEGORY;

    @Name(ID)
    private String id;
    @Name(CHOOSE)
    private Boolean choose;
    @Name(ESTIMATED_MEMBER_ID)
    private String estimatedMemberId;
    @Name(ESTIMATED_PERSON_ID)
    private String estimatedPersonId;
    @Name(ESTIMATED_PERSON_NAME)
    private String estimatedPersonName;
    @Name(ESTIMATED_PERSON_POST)
    private String estimatedPersonPost;
    @Name(ESTIMATOR_ID)
    private String estimatorId;
    @Name(ESTIMATOR_NAME)
    private String estimatorName;
    @Name(ESTIMATOR_POST)
    private String estimatorPost;
    @Name(CATEGORY_VALUE)
    @Converter(ProcedureEstimatingCategoryFieldConverter.class)
    private ProcedureEstimatingCategory categoryValue;
    @Name(CATEGORY_ID)
    private String categoryId;
    @Name(CATEGORY_NAME)
    private String categoryName;
    @Name(EXPERT_CATEGORY_ID)
    private String expertCategoryId;

    private boolean allowArchive;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getChoose() {
        return choose;
    }

    public void setChoose(Boolean choose) {
        this.choose = choose;
    }

    public String getEstimatedMemberId() {
        return estimatedMemberId;
    }

    public void setEstimatedMemberId(String estimatedMemberId) {
        this.estimatedMemberId = estimatedMemberId;
    }

    public String getEstimatedPersonId() {
        return estimatedPersonId;
    }

    public void setEstimatedPersonId(String estimatedPersonId) {
        this.estimatedPersonId = estimatedPersonId;
    }

    public String getEstimatedPersonName() {
        return estimatedPersonName;
    }

    public void setEstimatedPersonName(String estimatedPersonName) {
        this.estimatedPersonName = estimatedPersonName;
    }

    public String getEstimatedPersonPost() {
        return estimatedPersonPost;
    }

    public void setEstimatedPersonPost(String estimatedPersonPost) {
        this.estimatedPersonPost = estimatedPersonPost;
    }

    public String getEstimatorId() {
        return estimatorId;
    }

    public void setEstimatorId(String estimatorId) {
        this.estimatorId = estimatorId;
    }

    public String getEstimatorName() {
        return estimatorName;
    }

    public void setEstimatorName(String estimatorName) {
        this.estimatorName = estimatorName;
    }

    public String getEstimatorPost() {
        return estimatorPost;
    }

    public void setEstimatorPost(String estimatorPost) {
        this.estimatorPost = estimatorPost;
    }

    public ProcedureEstimatingCategory getCategoryValue() {
        return categoryValue;
    }

    public void setCategoryValue(ProcedureEstimatingCategory categoryValue) {
        this.categoryValue = categoryValue;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getExpertCategoryId() {
        return expertCategoryId;
    }

    public void setExpertCategoryId(String expertCategoryId) {
        this.expertCategoryId = expertCategoryId;
    }

    @Override
    public List<DataField> createFields() {
        List<DataField> fields = new ArrayList<>();
        fields.add(new StringField(ID, SystemMessages.empty));
        fields.add(new CheckField(CHOOSE, SystemMessages.choose));
        fields.add(new StringField(ESTIMATED_MEMBER_ID, ProcedureMessage.member_scored));
        fields.add(new StringField(ESTIMATED_PERSON_ID, ProcedureMessage.member_scored));
        fields.add(new StringField(ESTIMATED_PERSON_NAME, ProcedureMessage.member_scored));
        fields.add(new StringField(ESTIMATED_PERSON_POST, PersonMessage.post));
        fields.add(new StringField(ESTIMATOR_ID, ProcedureMessage.estimator));
        fields.add(new StringField(ESTIMATOR_NAME, ProcedureMessage.estimator));
        fields.add(new StringField(ESTIMATOR_POST, PersonMessage.post));
        fields.add(new ComboField(CATEGORY_VALUE, SystemMessages.category, ProcedureEstimatingCategoryStore.getInstance().getComboValues()));
        fields.add(new StringField(CATEGORY_ID, SystemMessages.category));
        fields.add(new StringField(CATEGORY_NAME, SystemMessages.category));
        fields.add(new StringField(EXPERT_CATEGORY_ID, SystemMessages.category));
        return fields;
    }

    public PRMassSurveyBean setValues(PRMemberBean member, PRCategoryBean category) {
        setMemberValues(member);
        setCategoryValues(category, new NameBean());
        setEstimatorId(EMPTY_ESTIMATOR_ID);
        generateId();
        return this;
    }

    public PRMassSurveyBean setValues(PRMemberBean member,
                                      PersonForAssignEstimatorBean estimator,
                                      PRCategoryBean category) {
        return setValues(member, estimator, category, new NameBean());
    }

    public PRMassSurveyBean setValues(PRMemberBean member,
                                      PersonForAssignEstimatorBean estimator,
                                      PRCategoryBean category,
                                      NameBean expertCategory) {
        setMemberValues(member);
        setEstimatorValues(estimator);
        setCategoryValues(category, expertCategory);
        generateId();
        return this;
    }

    private void setMemberValues(PRMemberBean member) {
        setEstimatedMemberId(member.getId());
        setEstimatedPersonId(member.getPerson().getId());
        setEstimatedPersonName(member.getPerson().getName());
        setEstimatedPersonPost(member.getPost().getName());
    }

    private void setEstimatorValues(PersonForAssignEstimatorBean estimator) {
        setEstimatorId(estimator.getId());
        setEstimatorName(estimator.getName());
        setEstimatorPost(estimator.getPostName());
    }

    private void setCategoryValues(PRCategoryBean category, NameBean expertCategory) {
        setCategoryValue(category.getCategory());
        setCategoryId(category.getId());
        setExpertCategoryId(expertCategory.getId());
        setCategoryName(StringHelper.isEmpty(expertCategory.getName()) ?
                category.getCategoryName() :
                (category.getCategoryName() + "/" + expertCategory.getName()));
    }

    private void generateId() {
        setId(getEstimatedMemberId() + "_" + getEstimatorId() + "_" + getCategoryId());
    }

    public boolean isAllowArchive() {
        return allowArchive;
    }

    public PRMassSurveyBean setAllowArchive(boolean allowArchive) {
        this.allowArchive = allowArchive;
        return this;
    }
}
