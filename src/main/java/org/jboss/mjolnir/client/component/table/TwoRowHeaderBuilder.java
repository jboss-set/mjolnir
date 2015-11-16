package org.jboss.mjolnir.client.component.table;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.builder.shared.DivBuilder;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.DefaultHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Header;
import org.jboss.mjolnir.shared.domain.Subscription;

import java.util.List;

/**
 * Custom header builder that displays second header row.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class TwoRowHeaderBuilder extends DefaultHeaderOrFooterBuilder<Subscription> {

    private List<Header<?>> secondRowHeaders;

    /**
     * Create a new DefaultHeaderBuilder for the header of footer section.
     *
     * @param table    the table being built
     * @param isFooter true if building the footer, false if the header
     * @param secondRowHeaders filtering headers
     */
    public TwoRowHeaderBuilder(AbstractCellTable<Subscription> table, boolean isFooter, List<Header<?>> secondRowHeaders) {
        super(table, isFooter);
        this.secondRowHeaders = secondRowHeaders;
    }

    @Override
    protected boolean buildHeaderOrFooterImpl() {
        super.buildHeaderOrFooterImpl();

        // Get the common style names.
        AbstractCellTable.Style style = getTable().getResources().style();
        String className = style.header() + "Filter";
        String classes;

        if (isBuildingFooter()) {
            return true;
        }

        TableRowBuilder tr = startRow();
        TableCellBuilder th;

        int curColumn;
        for (curColumn = 0; curColumn < secondRowHeaders.size(); curColumn++) {
            if (curColumn == 0) {
                classes = className + " " + style.firstColumnHeader();
            } else if (curColumn == secondRowHeaders.size() - 1) {
                classes = className + " " + style.lastColumnHeader();
            } else {
                classes = className;
            }

            Header<?> header = secondRowHeaders.get(curColumn);

            th = tr.startTH().className(classes);
            DivBuilder div = th.startDiv();

            if (header != null) {
                Cell.Context context = new Cell.Context(0, curColumn, header.getKey());
                renderHeader(div, context, header);
            }

            div.endDiv();
            tr.endTH();
        }

        // empty cells for remaining columns
        for (; curColumn < getTable().getColumnCount(); curColumn++) {
            th = tr.startTH().className(className);
            DivBuilder div = th.startDiv();
            div.endDiv();
            tr.endTH();
        }

        tr.endTR();

        return true;
    }
}
