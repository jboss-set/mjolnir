package org.jboss.set.mjolnir.client.component.organizations;

import java.util.List;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.set.mjolnir.client.component.table.DefaultCellTable;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class SelectionTable<T> implements IsWidget {

    private ListDataProvider<T> dataProvider;
    private SingleSelectionModel<T> selectionModel;


    @Override
    public Widget asWidget() {
        CellTable<T> table = new DefaultCellTable<>();
        table.getElement().addClassName("selectionTable");

        dataProvider = new ListDataProvider<>();
        dataProvider.addDataDisplay(table);

        selectionModel = new SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                onSelectionChanged(selectionModel.getSelectedObject());
            }
        });
        table.setSelectionModel(selectionModel);

        table.addColumn(new TextColumn<T>() {
            @Override
            public String getValue(T object) {
                return SelectionTable.this.getName(object);
            }
        }, "Name");

        return table;
    }

    public void setData(List<T> values) {
        dataProvider.setList(values);
        T oldSelection = selectionModel.getSelectedObject();
        selectionModel.clear();
        if (!values.isEmpty()) {
            T selectedItem = values.get(0);
            selectionModel.setSelected(selectedItem, true);

            // only trigger on-selection event if the old & new selection items are equal, otherwise it's triggered
            // automatically by SelectionModel
            if (selectedItem.equals(oldSelection)) {
                onSelectionChanged(selectedItem);
            }
        }
    }

    protected abstract String getName(T item);

    protected abstract void onSelectionChanged(T selectedObject);
}
