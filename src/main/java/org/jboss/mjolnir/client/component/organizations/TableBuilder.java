package org.jboss.mjolnir.client.component.organizations;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.mjolnir.client.component.table.DefaultCellTable;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class TableBuilder<T> {

    private ProvidesKey<T> providesKey;
    private List<ColumnHeader<T, ?>> columns = new ArrayList<>();
    private SelectionChangeEvent.Handler selectionChangeHandler;
    ListDataProvider<T> dataProvider;

    public TableBuilder(ListDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void setProvidesKey(ProvidesKey<T> providesKey) {
        this.providesKey = providesKey;
    }

    public void setSelectionChangeHandler(SelectionChangeEvent.Handler selectionChangeHandler) {
        this.selectionChangeHandler = selectionChangeHandler;
    }

    public void addColumn(Column<T, ?> column, String header) {
        columns.add(new ColumnHeader<>(column, new TextHeader(header)));
    }

    public CellTable<T> build() {
        CellTable<T> table = new DefaultCellTable<>();
        dataProvider.addDataDisplay(table);

        SingleSelectionModel<T> selectionModel = new SingleSelectionModel<>(providesKey);
        if (selectionChangeHandler != null) {
            selectionModel.addSelectionChangeHandler(selectionChangeHandler);
        }
        table.setSelectionModel(selectionModel);

        for (ColumnHeader<T, ?> column: columns) {
            table.addColumn(column.getColumn(), column.getHeader());
        }

        return table;
    }

    private static class ColumnHeader<T, C> {
        private Column<T, C> column;
        private Header<?> header;

        public ColumnHeader(Column<T, C> column, Header<?> header) {
            this.column = column;
            this.header = header;
        }

        public Column<T, C> getColumn() {
            return column;
        }

        public Header<?> getHeader() {
            return header;
        }
    }
}
