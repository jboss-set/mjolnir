package org.jboss.mjolnir.client.component.administration;

import com.google.common.base.Predicate;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ListDataProvider;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.client.ExceptionHandler;
import org.jboss.mjolnir.client.component.ConfirmationDialog;
import org.jboss.mjolnir.client.component.table.ConditionalActionCell;
import org.jboss.mjolnir.client.component.table.DropDownCell;
import org.jboss.mjolnir.client.component.table.FilteringListDataProvider;
import org.jboss.mjolnir.client.component.table.TwoRowHeaderBuilder;
import org.jboss.mjolnir.client.domain.Subscription;
import org.jboss.mjolnir.client.service.AdministrationService;
import org.jboss.mjolnir.client.service.AdministrationServiceAsync;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Table composite displaying list of Subscriptions objects.
 * <p/>
 * Contains action button for editing and deleting related users. Allows filtering and sorting.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionsTable extends Composite {

    private static final int PAGE_SIZE = 50;
    private static final String DELETE_USER_TEXT = "Delete user from database?";
    private static final String DELETE_USER_SUBTEXT = "Note: this will not remove user's subscriptions on GitHub.";
    private static final List<String> KRB_ACCOUNT_FILTER_OPTIONS = new ArrayList<String>();

    static {
        KRB_ACCOUNT_FILTER_OPTIONS.add("-");
        KRB_ACCOUNT_FILTER_OPTIONS.add("yes");
        KRB_ACCOUNT_FILTER_OPTIONS.add("no");
    }


    private AdministrationServiceAsync administrationService = AdministrationService.Util.getInstance();
    protected ListDataProvider<Subscription> dataProvider;
    protected SearchPredicate searchPredicate;

    public SubscriptionsTable(List<Subscription> subscriptions) {
        final HTMLPanel panel = new HTMLPanel("");
        initWidget(panel);

        final CellTable<Subscription> subscriptionTable = new CellTable<Subscription>();
        subscriptionTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
        panel.add(subscriptionTable);

        searchPredicate = new SearchPredicate();
        dataProvider = new FilteringListDataProvider<Subscription>(subscriptions, searchPredicate);
        dataProvider.addDataDisplay(subscriptionTable);


        // column definitions

        final TextColumn<Subscription> krbNameCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.getKerberosName();
            }
        };
        krbNameCol.setSortable(true);
        subscriptionTable.addColumn(krbNameCol, "Kerberos Username");

        final TextColumn<Subscription> gitHubNameCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.getGitHubName();
            }
        };
        gitHubNameCol.setSortable(true);
        subscriptionTable.addColumn(gitHubNameCol, "GitHub Username");

        final TextColumn<Subscription> krbAccCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.isActiveKerberosAccount() ? "yes" : "no";
            }
        };
        krbAccCol.setSortable(true);
        subscriptionTable.addColumn(krbAccCol, "Active Krb Account?");

        subscriptionTable.addColumn(createActionColumn(), "Actions");


        // sorting

        final ColumnSortEvent.ListHandler<Subscription> sortHandler =
                new ColumnSortEvent.ListHandler<Subscription>(subscriptions) {
                    @Override
                    public void onColumnSort(ColumnSortEvent event) {
                        super.onColumnSort(event);
                        dataProvider.refresh();
                    }
                };
        sortHandler.setComparator(krbNameCol, new KrbNameComparator());
        sortHandler.setComparator(gitHubNameCol, new GitHubNameComparator());
        sortHandler.setComparator(krbAccCol, new IsRegisteredComparator());
        subscriptionTable.addColumnSortHandler(sortHandler);
//        subscriptionTable.getColumnSortList().push(krbNameCol);


        // paging

        final SimplePager pager = new SimplePager();
        pager.setDisplay(subscriptionTable);
        pager.setPageSize(PAGE_SIZE);
        panel.add(pager);


        // filtering

        final List<Header<?>> filterHeaders = createFilterHeaders();
        subscriptionTable.setHeaderBuilder(new TwoRowHeaderBuilder(subscriptionTable, false, filterHeaders));
    }

    /**
     * Creates list of headers containing input boxes for specifying filtering criteria.
     *
     * @return list of headers
     */
    protected List<Header<?>> createFilterHeaders() {
        List<Header<?>> filterHeaders = new ArrayList<Header<?>>();

        // krb name
        Cell<String> krbNameInputCell = new TextInputCell();
        Header<String> krbNameFilterHeader = new Header<String>(krbNameInputCell) {
            @Override
            public String getValue() {
                return "";
            }
        };
        krbNameFilterHeader.setUpdater(new ValueUpdater<String>() {
            @Override
            public void update(String value) {
                searchPredicate.setKrbNameExpression(value);
                dataProvider.refresh();
            }
        });
        filterHeaders.add(krbNameFilterHeader);

        // github name
        TextInputCell gitHubNameInputCell = new TextInputCell();
        Header<String> gitHubNameFilterHeader = new Header<String>(gitHubNameInputCell) {
            @Override
            public String getValue() {
                return "";
            }
        };
        gitHubNameFilterHeader.setUpdater(new ValueUpdater<String>() {
            @Override
            public void update(String value) {
                searchPredicate.setGitHubNameExpression(value);
                dataProvider.refresh();
            }
        });
        filterHeaders.add(gitHubNameFilterHeader);

        // krb account
        final DropDownCell krbAccountSelectionCell = new DropDownCell(KRB_ACCOUNT_FILTER_OPTIONS);
        Header<String> krbAccountFilterHeader = new Header<String>(krbAccountSelectionCell) {
            @Override
            public String getValue() {
                return krbAccountSelectionCell.getValue();
            }
        };
        krbAccountFilterHeader.setUpdater(new ValueUpdater<String>() {
            @Override
            public void update(String value) {
                Boolean boolValue;
                if ("yes".equals(value)) {
                    boolValue = true;
                } else if ("no".equals(value)) {
                    boolValue = false;
                } else {
                    boolValue = null;
                }

                searchPredicate.setKrbAccount(boolValue);
                dataProvider.refresh();
            }
        });
        filterHeaders.add(krbAccountFilterHeader);

        return filterHeaders;
    }

    /**
     * Creates last table column with action buttons.
     *
     * @return action column
     */
    protected Column<Subscription, Subscription> createActionColumn() {
        final List<HasCell<Subscription, ?>> hasCells = createActionCells();

        final Cell<Subscription> cell = new CompositeCell<Subscription>(hasCells);
        return new Column<Subscription, Subscription>(cell) {
            @Override
            public Subscription getValue(Subscription object) {
                return object;
            }
        };
    }

    protected List<HasCell<Subscription, ?>> createActionCells() {
        final List<HasCell<Subscription, ?>> hasCells = new ArrayList<HasCell<Subscription, ?>>();

        // edit button
        hasCells.add(new ConditionalActionCell<Subscription>("Edit", new EditDelegate()));

        // subscriptions button
        hasCells.add(new ConditionalActionCell<Subscription>("Subscriptions", new SubscribeDelegate()) {
            @Override
            public boolean isEnabled(Subscription value) {
                return value.getGitHubName() != null;
            }
        });

        // delete button
        hasCells.add(new ConditionalActionCell<Subscription>("Delete", new DeleteDelegate()) {
            @Override
            public boolean isEnabled(Subscription value) {
                return value.getKerberosUser() != null;
            }
        });

        return hasCells;
    }

    /**
     * Called after delete action.
     *
     * @param object deleted item
     */
    protected void onDeleted(Subscription object) {
        // remove object from the list
        dataProvider.getList().remove(object);
        dataProvider.refresh();
    }

    /**
     * Called after item was modified.
     *
     * @param object    modified item
     * @param savedUser user instance that was actually saved on server
     */
    protected void onEdited(Subscription object, KerberosUser savedUser) {
        // updates subscription item in the list with current user object
        object.setKerberosUser(savedUser);
        object.setGitHubName(savedUser.getGithubName());
        dataProvider.refresh();
    }


    // comparators

    /**
     * Compares Subscription objects by krb name.
     */
    private class KrbNameComparator implements Comparator<Subscription> {
        @Override
        public int compare(Subscription subscription, Subscription subscription2) {
            if (subscription == null || subscription.getKerberosName() == null) {
                return -1;
            } else if (subscription2 == null || subscription2.getKerberosName() == null) {
                return 1;
            }
            return subscription.getKerberosName().toLowerCase()
                    .compareTo(subscription2.getKerberosName().toLowerCase());
        }
    }

    /**
     * Compares Subscription objects by GitHub name.
     */
    private class GitHubNameComparator implements Comparator<Subscription> {
        @Override
        public int compare(Subscription subscription, Subscription subscription2) {
            if (subscription == null || subscription.getGitHubName() == null) {
                return -1;
            } else if (subscription2 == null || subscription2.getGitHubName() == null) {
                return 1;
            }
            return subscription.getGitHubName().toLowerCase()
                    .compareTo(subscription2.getGitHubName().toLowerCase());
        }
    }

    /**
     * Compares Subscription objects according to whether they are related to a registered user.
     */
    private class IsRegisteredComparator implements Comparator<Subscription> {
        @Override
        public int compare(Subscription subscription, Subscription subscription2) {
            if (subscription == null) {
                return -1;
            } else if (subscription2 == null) {
                return 1;
            } else if (subscription.isActiveKerberosAccount() == subscription2.isActiveKerberosAccount()) {
                return 0;
            } else {
                return subscription.isActiveKerberosAccount() ? 1 : -1;
            }
        }
    }


    // button delegates

    /**
     * Edit button delegate.
     */
    private class EditDelegate implements ActionCell.Delegate<Subscription> {
        @Override
        public void execute(final Subscription object) {
            // displays edit dialog
            KerberosUser userToEdit = object.getKerberosUser();
            if (userToEdit == null) { // if user is not yet in our database, create new object
                userToEdit = new KerberosUser();
                userToEdit.setGithubName(object.getGitHubName());
            }
            final EditUserDialog editDialog = new EditUserDialog(userToEdit) {
                @Override
                protected void onSave(KerberosUser savedUser) {
                    onEdited(object, savedUser);
                }
            };
            editDialog.center();
        }
    }

    /**
     * Subscribe button delegate.
     */
    private class SubscribeDelegate implements ActionCell.Delegate<Subscription> {
        @Override
        public void execute(final Subscription object) {
            // displays subscription dialog
            final SubscribeUserDialog dialog = new SubscribeUserDialog(object.getGitHubName());
            dialog.center();
        }
    }

    /**
     * Delete button delegate.
     * <p/>
     * This deletes related user from database. Subscription object as such may remain in the list.
     */
    private class DeleteDelegate implements ActionCell.Delegate<Subscription> {
        @Override
        public void execute(final Subscription object) {
            final ConfirmationDialog confirmDialog = new ConfirmationDialog(DELETE_USER_TEXT, DELETE_USER_SUBTEXT) {
                @Override
                public void onConfirm() {
                    administrationService.deleteUser(object.getKerberosUser(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            ExceptionHandler.handle(caught);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            onDeleted(object);
                        }
                    });
                }
            };
            confirmDialog.center();
        }
    }


    // predicates

    /**
     * Predicate for filtering subscriptions according to given criteria.
     * <p/>
     * Any subscription with krb name and/or github name *containing* given strings qualifies.
     */
    protected class SearchPredicate implements Predicate<Subscription> {

        private String krbNameExpression;
        private String gitHubNameExpression;
        private Boolean krbAccount;

        @Override
        public boolean apply(@Nullable Subscription o) {
            if (o == null) {
                return false;
            } else if (isEmpty(krbNameExpression) && isEmpty(gitHubNameExpression) && krbAccount == null) {
                return true;
            }
            return (isEmpty(krbNameExpression) || (o.getKerberosName() != null && o.getKerberosName().contains(krbNameExpression)))
                    && (isEmpty(gitHubNameExpression) || (o.getGitHubName() != null && o.getGitHubName().contains(gitHubNameExpression)))
                    && (krbAccount == null || krbAccount == o.isActiveKerberosAccount());
        }

        public void setKrbNameExpression(String krbNameExpression) {
            this.krbNameExpression = krbNameExpression;
        }

        public void setGitHubNameExpression(String gitHubNameExpression) {
            this.gitHubNameExpression = gitHubNameExpression;
        }

        public void setKrbAccount(Boolean krbAccount) {
            this.krbAccount = krbAccount;
        }

        private boolean isEmpty(String value) {
            return value == null || "".equals(value);
        }
    }

}
