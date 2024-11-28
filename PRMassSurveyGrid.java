package lms.core.newprocedure.assignment.mass;

import lms.core.newprocedure.survey.PRSurveyFrame;
import lms.core.person.PersonFrame;
import org.mirapolis.data.DataSet;
import org.mirapolis.exception.CoreException;
import org.mirapolis.mvc.action.grid.GridGoByAttrAction;
import org.mirapolis.mvc.action.grid.VGridCheckAction;
import org.mirapolis.mvc.action.grid.VGridExcludeAction;
import org.mirapolis.mvc.model.grid.Sort;
import org.mirapolis.mvc.model.grid.SortDirection;
import org.mirapolis.mvc.model.grid.TableDataSource;
import org.mirapolis.mvc.model.grid.VirtualDataSource;
import org.mirapolis.mvc.model.grid.bean.BeanDataSetList;
import org.mirapolis.mvc.model.grid.bean.VirtualBeanDataGrid;
import org.mirapolis.mvc.model.table.HiddenColumn;
import org.mirapolis.service.grid.GridChoosingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Массовое назначение
 * Оценочные анкеты
 *
 * @author Elena Puzakova
 * @since 10.06.2019 8:12
 */
public class PRMassSurveyGrid extends VirtualBeanDataGrid<PRMassSurveyBean> {
    private String gridName;
    private String chooseGridName;
    @Autowired
    private GridChoosingService choosingService;

    public PRMassSurveyGrid(String gridName, String chooseGridName) {
        super(PRMassSurveyBean.class, PRMassSurveyBean.ID);
        this.gridName = gridName;
        this.chooseGridName = chooseGridName;
    }

    @Override
    protected void setDefaultSorting() {
        grid.setDefaultSorting(new Sort(0, PRMassSurveyBean.ESTIMATED_PERSON_NAME, SortDirection.ASC));
    }

    @Override
    protected void setColumns() throws CoreException {
        grid.addColumns(
                new HiddenColumn(PRMassSurveyBean.ID),
                new HiddenColumn(BeanDataSetList.TYPE),
                new HiddenColumn(PRMassSurveyBean.ESTIMATED_MEMBER_ID),
                new HiddenColumn(PRMassSurveyBean.ESTIMATED_PERSON_ID),
                new HiddenColumn(PRMassSurveyBean.CATEGORY_VALUE),
                new HiddenColumn(PRMassSurveyBean.CATEGORY_ID),
                new HiddenColumn(PRMassSurveyBean.EXPERT_CATEGORY_ID),
                new HiddenColumn(PRMassSurveyBean.ESTIMATOR_ID));

        grid.addColumn(getTableColumn(PRMassSurveyBean.CHOOSE)
                .setAction(new VGridCheckAction(chooseGridName)));
        grid.addColumn(getTableColumn(PRMassSurveyBean.ESTIMATED_PERSON_NAME)
                .setAction(new GridGoByAttrAction(PRMassSurveyBean.ESTIMATED_PERSON_ID, PersonFrame.NAME).openInModal()));
        addBeanColumns(PRMassSurveyBean.ESTIMATED_PERSON_POST);
        grid.addColumn(getTableColumn(PRMassSurveyBean.ESTIMATOR_NAME)
                .setAction(new GridGoByAttrAction(PRMassSurveyBean.ESTIMATOR_ID, PersonFrame.NAME).openInModal()));
        addBeanColumns(
                PRMassSurveyBean.ESTIMATOR_POST,
                PRMassSurveyBean.CATEGORY_NAME);

        grid.addAction(new VGridExcludeAction(gridName, PRMassSurveyBean.ID));
    }

    @Override
    public TableDataSource getGridDataSource() throws CoreException {
        return VirtualDataSource.get(gridName);
    }

    @Override
    public PRMassSurveyBean createBean(DataSet dataSet) {
        PRMassSurveyBean bean = super.createBean(dataSet);
        bean.setChoose(choosingService.getChosenIdsFromVirtualGrid(chooseGridName).contains(bean.getId()));
        return bean;
    }

    @Override
    public String getType() {
        return PRSurveyFrame.NAME;
    }
}
