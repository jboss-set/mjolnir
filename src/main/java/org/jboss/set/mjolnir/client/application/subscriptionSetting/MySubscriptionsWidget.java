package org.jboss.set.mjolnir.client.application.subscriptionSetting;

import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.jboss.set.mjolnir.client.UIMessages;
import org.jboss.set.mjolnir.client.component.table.DefaultCellTable;
import org.jboss.set.mjolnir.shared.domain.GithubTeam;
import org.jboss.set.mjolnir.shared.domain.MembershipStates;

/**
 * Shows table of github teams that a user can (un)subscribe to.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class MySubscriptionsWidget implements IsWidget {

    private ListDataProvider<GithubTeam> dataProvider = new ListDataProvider<>();
    private UIMessages uiMessages = GWT.create(UIMessages.class);

    @Override
    public Widget asWidget() {
        final CellTable<GithubTeam> cellTable = new DefaultCellTable<>();
        dataProvider.addDataDisplay(cellTable);

        cellTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED); // disables row selection feature

        final TextColumn<GithubTeam> nameColumn = new TextColumn<GithubTeam>() {
            @Override
            public String getValue(GithubTeam team) {
                return team.getName();
            }
        };
        cellTable.addColumn(nameColumn, uiMessages.team());

        final TextColumn<GithubTeam> subscribedColumn = new TextColumn<GithubTeam>() {
            @Override
            public String getValue(GithubTeam object) {
                if (MembershipStates.NONE.equals(object.getMembershipState())) {
                    return uiMessages.no();
                } else if (MembershipStates.ACTIVE.equals(object.getMembershipState())) {
                    return uiMessages.yes();
                } else if (MembershipStates.PENDING.equals(object.getMembershipState())) {
                    return uiMessages.subscribtionStatusPendnig();
                }
                return "?"; // unknown state
            }
        };
        cellTable.addColumn(subscribedColumn, uiMessages.membership());

        final ButtonCell cell = new ButtonCell();
        final Column<GithubTeam, String> actionColumn = new Column<GithubTeam, String>(cell) {
            @Override
            public String getValue(GithubTeam object) {
                return MembershipStates.NONE.equals(object.getMembershipState())
                        ? uiMessages.subscribe() : uiMessages.unsubscribe();
            }
        };
        actionColumn.setFieldUpdater(new FieldUpdater<GithubTeam, String>() {
            @Override
            public void update(int index, final GithubTeam object, String value) {
                if (MembershipStates.NONE.equals(object.getMembershipState())) {
                    dispatchSubscribe(object);
                } else {
                    dispatchUnSubscribe(object);
                }
            }
        });
        cellTable.addColumn(actionColumn, uiMessages.subscribtion());

        return cellTable;
    }

    public void setData(List<GithubTeam> data) {
        dataProvider.setList(data);
    }

    public void refresh() {
        dataProvider.refresh();
    }

    protected abstract void dispatchSubscribe(GithubTeam team);

    protected abstract void dispatchUnSubscribe(GithubTeam team);
}
