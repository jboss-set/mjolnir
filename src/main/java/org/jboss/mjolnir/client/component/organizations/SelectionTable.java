package org.jboss.mjolnir.client.component.organizations;

import java.util.List;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.mjolnir.client.component.table.DefaultCellTable;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class SelectionTable<T> implements IsWidget {

    private ListDataProvider<T> dataProvider;
    private SingleSelectionModel<T> selectionModel;


    @Override
    public CellTable asWidget() {
        ProvidesKey<T> providesKey = new ProvidesKey<T>() {
            @Override
            public Object getKey(T item) {
                return SelectionTable.this.getKey(item);
            }
        };

        CellTable<T> table = new DefaultCellTable<>(providesKey);

        dataProvider = new ListDataProvider<>();
        dataProvider.addDataDisplay(table);

        selectionModel = new SingleSelectionModel<>(providesKey);
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
        selectionModel.clear();
        if (!values.isEmpty()) {
            T selectedItem = values.get(0);
            selectionModel.setSelected(selectedItem, true);
            onSelectionChanged(selectedItem);
        }
    }

    protected abstract Object getKey(T item);

    protected abstract String getName(T item);

    protected abstract void onSelectionChanged(T selectedObject);
}
