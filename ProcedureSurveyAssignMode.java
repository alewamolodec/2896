package lms.core.newprocedure.assignment.mass;

import lms.core.person.PersonBean;
import lms.core.person.PersonPrivateBean;
import lms.core.person.work.PersonWorkBean;
import mira.vv.rubricator.standard.RSBean;
import org.mirapolis.sql.QueryData;
import org.mirapolis.sql.fragment.Column;
import org.mirapolis.sql.fragment.SelectQuery;

/**
 * Назначение оценивающих
 *
 * @author Elena Puzakova
 * @since 13.01.2023 8:53
 */
public enum ProcedureSurveyAssignMode {
    auto,
    manual {
        private static final String WORK_ALIAS = "EW";
        private static final String POST_RS_ALIAS = "POSTRS";
        private static final String PRIVATE_ALIAS = "EPP";

        @Override
        public QueryData<SelectQuery> updatePersonQueryData(QueryData<SelectQuery> queryData) {
            SelectQuery query = queryData.getQuery().copy();
            query.processAllQueriesWithUnion(unionQuery -> {
                unionQuery.innerJoin(PersonPrivateBean.DATANAME, PRIVATE_ALIAS)
                    .on(Column.column(PRIVATE_ALIAS, PersonBean.ID)
                        .eq(Column.column(
                            query.getFromWhereClause().getTableByName(PersonBean.DATANAME).getAlias(),
                            PersonBean.ID)));
                unionQuery.addColumn(PersonPrivateBean.FILE_ID, PRIVATE_ALIAS, PersonForAssignEstimatorBean.PHOTO_ID);
                unionQuery.addColumn(PersonPrivateBean.SEX, PRIVATE_ALIAS, PersonForAssignEstimatorBean.SEX);

                unionQuery.innerJoin(PersonWorkBean.DATANAME, WORK_ALIAS)
                    .on(Column.column(WORK_ALIAS, PersonWorkBean.ID)
                        .eq(Column.column(
                            query.getFromWhereClause().getTableByName(PersonBean.DATANAME).getAlias(),
                            PersonBean.PERSON_MAIN_WORK)));
                unionQuery.leftJoin(RSBean.DATANAME, POST_RS_ALIAS)
                    .on(Column.column(POST_RS_ALIAS, RSBean.ID)
                        .eq(Column.column(WORK_ALIAS, PersonWorkBean.POST_RS_ID)));
                unionQuery.addColumn(RSBean.NAME, POST_RS_ALIAS, PersonForAssignEstimatorBean.POST_NAME);
            });
            queryData.setQuery(query);
            return queryData;
        }
    }
    ;

    public QueryData<SelectQuery> updatePersonQueryData(QueryData<SelectQuery> queryData) {
        return queryData;
    }
}
