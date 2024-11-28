package lms.core.newprocedure.assignment.mass;

import lms.core.newprocedure.category.PRCategoryBean;
import lms.core.newprocedure.category.PRCategoryService;
import org.mirapolis.core.Context;
import org.mirapolis.core.SystemMessages;
import org.mirapolis.exception.CoreException;
import org.mirapolis.mvc.model.State;
import org.mirapolis.mvc.model.frame.ChildListFrame;
import org.mirapolis.mvc.model.grid.AbstractDataGrid;
import org.mirapolis.mvc.model.grid.VirtualDataSource;
import org.mirapolis.mvc.view.element.GridButtonsElement;
import org.mirapolis.mvc.view.element.component.Label;
import org.mirapolis.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Анкеты
 *
 * @author Elena Puzakova
 * @since 22.05.12 14:04
 */
public class PRMassSurveyListFrame extends ChildListFrame {
    public static final String NAME = "prmasssurveylist";
    private static final String CHOOSE_GRID = PRMassService.SURVEY_VGRID + "choose";
    @Autowired
    private PRCategoryService categoryService;

    @Override
    protected GridButtonsElement getGridButtons(State state) {
        String procedureId = state.getId();
		List<PRCategoryBean> categories = categoryService.getUsedProcedureCategories(procedureId).stream()
            .filter(category -> categoryService.isAccessEditCategorySurveys(category.getId(), procedureId))
            .collect(Collectors.toList());
        categoryService.sortCategories(categories);

        GridButtonsElement buttons = new GridButtonsElement();
        buttons.addChild(new Label(SystemMessages.add + StringHelper.COLON));
        categories.forEach(category -> category.getCategory().addSurveyCreateActions(buttons, category));
        buttons.add(new PRMassExcludeChosenAction(PRMassService.SURVEY_VGRID, CHOOSE_GRID));
        return buttons;
    }

    @Override
    public AbstractDataGrid getGridProxy(Context context) {
        return new PRMassSurveyGrid(PRMassService.SURVEY_VGRID, CHOOSE_GRID);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public State createState(Context context) throws CoreException {
        VirtualDataSource.clear(context, CHOOSE_GRID);
        return super.createState(context);
    }
}