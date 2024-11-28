package lms.core.newprocedure.assignment.mass;

import lms.core.person.PersonBean;
import lms.core.person.PersonPrivateBean;
import lms.core.person.PersonStatus;
import lms.core.person.Sex;
import org.mirapolis.data.bean.reflect.Name;
import org.mirapolis.data.bean.reflect.virtual.QueryVirtualBean;

/**
 * Данные физ. лица для назачания оценивающим
 *
 * @author Elena Puzakova
 * @since 12.01.2023 16:51
 */
public class PersonForAssignEstimatorBean extends QueryVirtualBean {
    public static final String ID = PersonBean.ID;
    public static final String NAME = PersonBean.FIO;
    public static final String STATUS = PersonBean.STATUS;
    public static final String ON_MATERNITY_LEAVE = PersonBean.ON_MATERNITY_LEAVE;
    public static final String POST_NAME = "postname";
    public static final String PHOTO_ID = PersonPrivateBean.FILE_ID;
    public static final String SEX = PersonPrivateBean.SEX;

    @Name(ID)
    private String id;
    @Name(NAME)
    private String name;
    @Name(STATUS)
    private PersonStatus status;
    @Name(ON_MATERNITY_LEAVE)
    private Boolean onMaternityLeave;
    @Name(POST_NAME)
    private String postName;
    @Name(PHOTO_ID)
    private String photoId;
    @Name(SEX)
    private Sex sex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersonStatus getStatus() {
        return status;
    }

    public void setStatus(PersonStatus status) {
        this.status = status;
    }

    public Boolean getOnMaternityLeave() {
        return onMaternityLeave;
    }

    public void setOnMaternityLeave(Boolean onMaternityLeave) {
        this.onMaternityLeave = onMaternityLeave;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public boolean isArchive() {
        return status == PersonStatus.ARCHIVE;
    }

    public PersonForAssignEstimatorBean fillByPerson(PersonBean person) {
        setId(person.getId());
        setName(person.getFio());
        setStatus(person.getStatus());
        setPostName(person.getMainWork().getPostRs().getName());
        setPhotoId(person.getPrivateBean().getFileId());
        setSex(person.getPrivateBean().getSex());
        return this;
    }
}
