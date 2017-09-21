package org.jboss.set.mjolnir.client.component.organizations;

import java.util.List;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.set.mjolnir.client.component.table.DefaultCellTable;
import org.jboss.set.mjolnir.shared.domain.SubscriptionSummary;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class SubscriptionSummaryTable implements IsWidget {

    private CellTable<SubscriptionSummary> table;
    private ListDataProvider<SubscriptionSummary> dataProvider;
    private SingleSelectionModel<SubscriptionSummary> selectionModel;


    @Override
    public CellTable asWidget() {
        table = new DefaultCellTable<>();

        dataProvider = new ListDataProvider<>();
        dataProvider.addDataDisplay(table);

        ProvidesKey<SubscriptionSummary> providesKey = new ProvidesKey<SubscriptionSummary>() {
            @Override
            public Object getKey(SubscriptionSummary item) {
                return item.getOrganization() != null ? item.getOrganization().getName() : null;
            }
        };

        selectionModel = new SingleSelectionModel<>(providesKey);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                onSelectionChanged(selectionModel.getSelectedObject());
            }
        });
        table.setSelectionModel(selectionModel);

        table.addColumn(new TextColumn<SubscriptionSummary>() {
            @Override
            public String getValue(SubscriptionSummary object) {
                return object != null && object.getOrganization() != null ? object.getOrganization().getName() : "";
            }
        }, "Name");

        return table;
    }

    public void update(List<SubscriptionSummary> values) {
        dataProvider.setList(values);
    }

    protected abstract void onSelectionChanged(SubscriptionSummary selectedObject);
}
