package lms.core.newprocedure.assignment.mass;

import lms.core.newprocedure.ProcedureModule;
import org.mirapolis.core.Module;
import org.mirapolis.data.bean.reflect.repository.Select;
import org.mirapolis.service.ServiceFactory;
import org.mirapolis.sql.SelectQueryData;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Назначение оценивающих
 *
 * @author Elena Puzakova
 * @since 13.01.2023 8:50
 */
@Repository
@Module(ProcedureModule.class)
public interface ProcedureSurveyAssignRepository {
    static ProcedureSurveyAssignRepository getInstance() {
        return ServiceFactory.getService(ProcedureSurveyAssignRepository.class);
    }

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "WHERE P.personid IN (:personIds)")
    SelectQueryData getPersonsByIds(Collection<String> personIds);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM CA$POST POST\n" +
            "INNER JOIN PP$PERSON P ON P.personid = POST.personid\n" +
            "WHERE POST.postid = :postId")
    SelectQueryData getPersonByPostId(String postId);

    @Select("SELECT W.pwid, M.personid, M.pfio, M.pstatus, M.ponmaternityleave\n" +
            "FROM PP$PERSONWORK W\n" +
            "INNER JOIN PP$PERSON M ON M.personid = W.directorid\n" +
            "WHERE W.pwid IN (:workIds)")
    SelectQueryData getManagersByWorkIds(Collection<String> workIds);

    @Select("SELECT P.postid, M.personid, M.pfio, M.pstatus, M.ponmaternityleave\n" +
            "FROM CA$POST P\n" +
            "INNER JOIN CA$POST PP ON PP.postid = P.postparentid\n" +
            "INNER JOIN PP$PERSON M ON M.personid = PP.personid\n" +
            "WHERE P.postid IN (:postIds)")
    SelectQueryData getManagersByPostIds(Collection<String> postIds);

    @Select("SELECT W.pwid, M.personid, M.pfio, M.pstatus, M.ponmaternityleave\n" +
            "FROM PP$PERSONWORK W\n" +
            "INNER JOIN PP$PERSONWORK MW ON MW.pwid = W.directorworkid\n" +
            "INNER JOIN PP$PERSON M ON M.personid = MW.directorid\n" +
            "WHERE W.pwid IN (:workIds)")
    SelectQueryData getManagersN1(Collection<String> workIds);

    @Select("SELECT W.pwid, M.personid, M.pfio, M.pstatus, M.ponmaternityleave\n" +
            "FROM PP$PERSONWORK W\n" +
            "INNER JOIN PP$PERSONWORK MW1 ON MW1.pwid = W.directorworkid\n" +
            "INNER JOIN PP$PERSONWORK MW2 ON MW2.pwid = MW1.directorworkid\n" +
            "INNER JOIN PP$PERSON M ON M.personid = MW2.directorid\n" +
            "WHERE W.pwid IN (:workIds)")
    SelectQueryData getManagersN2(Collection<String> workIds);

    @Select("SELECT W.pwid, M.personid, M.pfio, M.pstatus, M.ponmaternityleave\n" +
            "FROM PP$PERSONWORK W\n" +
            "INNER JOIN PP$PERSON M ON M.personid = W.funcdirectorid\n" +
            "WHERE W.pwid IN (:workIds)")
    SelectQueryData getFunctionalManagers(Collection<String> workIds);

    @Select("SELECT W.pwid, M.personid, M.pfio, M.pstatus, M.ponmaternityleave\n" +
            "FROM PP$PERSONWORK W\n" +
            "INNER JOIN PP$PERSONWORK MW ON MW.pwid = W.funcdirectorworkid\n" +
            "INNER JOIN PP$PERSON M ON M.personid = MW.funcdirectorid\n" +
            "WHERE W.pwid IN (:workIds)")
    SelectQueryData getFunctionalManagersN1(Collection<String> workIds);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM AT$PREXPERT E\n" +
            "INNER JOIN PP$PERSON P ON P.personId = E.personId\n" +
            "WHERE E.prcid = :categoryId\n" +
            "UNION\n" +
            "SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM AT$PREXPERT E\n" +
            "INNER JOIN CA$POST POST ON POST.personid = E.postid\n" +
            "INNER JOIN PP$PERSON P ON P.personId = POST.personId\n" +
            "WHERE E.prcid = :categoryId")
    SelectQueryData getCategoryPersonAndPostExperts(String categoryId);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM AT$PREXPERT E\n" +
            "INNER JOIN PP$PERSON P ON P.personId = E.personId\n" +
            "WHERE E.prcid = :categoryId")
    SelectQueryData getCategoryPersonExperts(String categoryId);

    @Select("SELECT E.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM AT$PRMEMBEREXPERT E\n" +
            "INNER JOIN PP$PERSON P ON P.personId = E.personId\n" +
            "WHERE E.prmId IN (:memberIds)")
    SelectQueryData getMemberExperts(Collection<String> memberIds);

    @Select("SELECT ACR.acid, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM AT$ASSESSMENTCENTERRESP ACR\n" +
            "INNER JOIN CA$POST CAP ON CAP.postId = ACR.postId\n" +
            "INNER JOIN PP$PERSON P ON P.personid = CAP.personid\n" +
            "LEFT JOIN AT$ACRESP_MULTI_PRTYPE RT ON RT.acrid = ACR.acrid AND ACR.acrallprtype = 0\n" +
            "WHERE ACR.acrtype = 0 AND (ACR.acrallprtype = 1 OR RT.rsid = :procedureTypeId) AND ACR.acid IN (:centerIds)\n" +
            "UNION\n" +
            "SELECT ACR.acid, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM AT$ASSESSMENTCENTERRESP ACR\n" +
            "INNER JOIN PP$PERSON P ON P.personid = ACR.personid\n" +
            "LEFT JOIN AT$ACRESP_MULTI_PRTYPE RT ON RT.acrid = ACR.acrid AND ACR.acrallprtype = 0\n" +
            "WHERE ACR.acrtype = 0 AND (ACR.acrallprtype = 1 OR RT.rsid = :procedureTypeId) AND ACR.acid IN (:centerIds)")
    SelectQueryData getCenterResponsiblePersons(String procedureTypeId, Collection<String> centerIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN AT$PRMEMBER M ON M.personpost = W.rspostid AND M.personcaid = W.caId AND " +
            "                               M.personid != P.personid\n" +
            "WHERE P.pstatus = 0 AND (P.typersid != -137 OR P.typersid IS NULL) AND M.prmid IN (:memberIds)")
    SelectQueryData getColleaguesByPost(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN AT$PRMEMBER M ON M.personcaId = W.caId AND M.personId != P.personId\n" +
            "WHERE P.pstatus = 0 AND (P.typersid != -137 OR P.typersid IS NULL) AND M.prmid IN (:memberIds)")
    SelectQueryData getColleaguesByCa(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONCONTACT PC ON PC.personId = P.personId\n" +
            "INNER JOIN VV$ADDRESS A ON A.adid = PC.addressid\n" +
            "INNER JOIN VV$ADDRESS MA ON MA.cityrsid = A.cityrsid AND MA.adid != A.adid\n" +
            "INNER JOIN PP$PERSONCONTACT MPC ON MPC.addressid = MA.adid AND MPC.personId != PC.personId\n" +
            "INNER JOIN AT$PRMEMBER M ON M.personid = MPC.personid\n" +
            "WHERE P.pstatus = 0 AND (P.typersid != -137 OR P.typersid IS NULL) AND M.prmid IN (:memberIds)")
    SelectQueryData getColleaguesByCity(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN PP$PERSONWORK MW ON MW.directorId = W.directorId AND MW.pwId != W.pwid\n" +
            "INNER JOIN AT$PRMEMBER M ON M.workid = MW.pwid\n" +
            "WHERE P.pstatus = 0 AND (P.typersid != -137 OR P.typersid IS NULL) AND M.prmid IN (:memberIds)")
    SelectQueryData getColleaguesBySubordination(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN PP$PERSONWORK MW ON MW.funcdirectorid = W.funcdirectorid AND MW.pwId != W.pwid\n" +
            "INNER JOIN AT$PRMEMBER M ON M.workid = MW.pwid\n" +
            "WHERE P.pstatus = 0 AND (P.typersid != -137 OR P.typersid IS NULL) AND M.prmid IN (:memberIds)")
    SelectQueryData getColleaguesByFunctionalSubordination(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN AT$PRMEMBER M ON M.personid = W.directorid\n" +
            "WHERE M.prmid IN (:memberIds)")
    SelectQueryData getInferiors(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN PP$PERSON P1 ON P1.personid = W.directorid\n"+
            "INNER JOIN PP$PERSONWORK W1 ON W1.pwid = P1.pmainwork\n"+
            "INNER JOIN AT$PRMEMBER M ON M.personid = W1.directorid\n" +
            "WHERE M.prmid IN (:memberIds)")
    SelectQueryData getInferiorsN1(Collection<String> memberIds);

    @Select("SELECT M.prmId, P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "INNER JOIN PP$PERSONWORK W ON W.pwid = P.pmainwork\n" +
            "INNER JOIN AT$PRMEMBER M ON M.personid = W.funcdirectorid\n" +
            "WHERE M.prmid IN (:memberIds)")
    SelectQueryData getFunctionalInferiors(Collection<String> memberIds);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$Person P\n" +
            "INNER JOIN PP$PERSONWORK W ON P.pmainwork = W.pwId\n" +
            "INNER JOIN CA$CA CA ON CA.caId = W.caId\n" +
            "WHERE P.pStatus = 0 AND (P.typersid != -137 OR P.typersid is null) AND CA.typersid = :caTypeId AND " +
            "       (1 = :allPerson OR P.ponmaternityleave = 0)")
    SelectQueryData getOwnPersonsForSelfAssignment(String caTypeId, Integer allPerson);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$Person P\n" +
            "INNER JOIN PP$GC_PERSON GP ON GP.gcobjectid = P.personId\n" +
            "WHERE GP.gcid = :groupId AND P.pStatus = 0 AND (P.typersid != -137 OR P.typersid is null) AND " +
            "       (1 = :allPerson OR P.ponmaternityleave = 0)")
    SelectQueryData getGroupPersonsForSelfAssignment(String groupId, Integer allPerson);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM (SELECTPARENT caid, caname\n" +
            "      FROM CA$CA\n" +
            "      CONNECT BY PRIOR caid = caparentid\n" +
            "      START WITH caid = :caId) CA\n" +
            "INNER JOIN PP$PERSONWORK W ON CA.caId = W.caId\n" +
            "INNER JOIN PP$PERSON P ON P.pmainwork = W.pwId\n" +
            "WHERE P.pStatus = 0 AND (P.typersid != -137 OR P.typersid is null)")
    SelectQueryData getCaPersonsForSelfAssignment(String caId);

    @Select("SELECT P.personid, P.pfio, P.pstatus, P.ponmaternityleave\n" +
            "FROM PP$PERSON P\n" +
            "WHERE P.pStatus = 0 AND (P.typersid != -137 OR P.typersid is null)")
    SelectQueryData getAllPersonsForSelfAssignment();
}
