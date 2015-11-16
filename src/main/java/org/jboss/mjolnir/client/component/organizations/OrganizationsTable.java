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
import org.jboss.mjolnir.shared.domain.GithubOrganization;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class OrganizationsTable implements IsWidget {

    private CellTable<GithubOrganization> table;
    private ListDataProvider<GithubOrganization> dataProvider;
    private SingleSelectionModel<GithubOrganization> selectionModel;


    @Override
    public CellTable asWidget() {
        table = new DefaultCellTable<>();

        dataProvider = new ListDataProvider<>();
        dataProvider.addDataDisplay(table);

        ProvidesKey<GithubOrganization> providesKey = new ProvidesKey<GithubOrganization>() {
            @Override
            public Object getKey(GithubOrganization item) {
                return item != null ? item.getName() : null;
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

        table.addColumn(new TextColumn<GithubOrganization>() {
            @Override
            public String getValue(GithubOrganization object) {
                return object != null ? object.getName() : "";
            }
        }, "Organization");

        return table;
    }

    public void setData(List<GithubOrganization> values) {
        dataProvider.setList(values);
        if (!values.isEmpty()) {
            selectionModel.setSelected(values.get(0), true);
        }
    }

    protected abstract void onSelectionChanged(GithubOrganization selectedObject);
}
