package org.jboss.set.mjolnir.client.component.administration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.jboss.set.mjolnir.client.component.table.ConditionalActionCell;
import org.jboss.set.mjolnir.client.component.table.DefaultCellTable;
import org.jboss.set.mjolnir.client.component.table.DropDownCell;
import org.jboss.set.mjolnir.client.component.table.TwoRowHeaderBuilder;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.jboss.set.mjolnir.shared.domain.Subscription;

/**
 * Table composite displaying list of Subscriptions objects.
 *
 * Contains action button for editing and deleting related users. Allows filtering and sorting.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class SubscriptionsTable implements IsWidget {

    private static final int PAGE_SIZE = 50;
    private static final List<String> KRB_ACCOUNT_FILTER_OPTIONS = new ArrayList<>();

    static {
        KRB_ACCOUNT_FILTER_OPTIONS.add("-");
        KRB_ACCOUNT_FILTER_OPTIONS.add("yes");
        KRB_ACCOUNT_FILTER_OPTIONS.add("no");
    }

    protected HTMLPanel panel = new HTMLPanel("");
    private final HTMLPanel buttonsPanel = new HTMLPanel("");
    private List<Subscription> data;
    protected ListDataProvider<Subscription> dataProvider;
    protected SubscriptionSearchPredicate searchPredicate;
    private final List<Button> actionButtons = new ArrayList<>();
    private final Set<Subscription> selectedItems = new HashSet<>();
    private final ColumnSortEvent.ListHandler<Subscription> sortHandler;
    private final List<HasCell<Subscription, ?>> hasCells = new ArrayList<>();
    private final Column<Subscription, Subscription> actionColumn;
    private final List<AbstractInputCell> filterCells = new ArrayList<>();

    public SubscriptionsTable() {
        initStyles();

        initActionPanel();

        final CellTable<Subscription> subscriptionTable = new DefaultCellTable<>();
        subscriptionTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
        HTMLPanel dataPanel = new HTMLPanel("");
        dataPanel.add(subscriptionTable);
        panel.add(dataPanel);

        searchPredicate = new SubscriptionSearchPredicate();
        dataProvider = new ListDataProvider<>();
        dataProvider.addDataDisplay(subscriptionTable);


        // column definitions

        final CheckboxCell selectionCell = new CheckboxCell(true, true);
        final Column<Subscription, Boolean> selectionCol = new Column<Subscription, Boolean>(selectionCell) {
            @Override
            public Boolean getValue(Subscription object) {
                return selectedItems.contains(object);
            }
        };
        subscriptionTable.addColumn(selectionCol);
        selectionCol.setFieldUpdater(new FieldUpdater<Subscription, Boolean>() {
            @Override
            public void update(int index, Subscription object, Boolean value) {
                if (value) {
                    selectedItems.add(object);
                } else {
                    selectedItems.remove(object);
                }

                enableActionButtons(selectedItems.size() > 0);
            }
        });

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

        final TextColumn<Subscription> noteCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.getRegisteredUser() != null ? object.getRegisteredUser().getNote() : "";
            }
        };
        noteCol.setSortable(false);
        subscriptionTable.addColumn(noteCol, "Note");

        final TextColumn<Subscription> krbAccCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.isActiveKerberosAccount() ? "yes" : "no";
            }
        };
        krbAccCol.setSortable(true);
        subscriptionTable.addColumn(krbAccCol, "Krb Account?");

        final TextColumn<Subscription> whitelistCol = new TextColumn<Subscription>() {
            @Override
            public String getValue(Subscription object) {
                return object.isWhitelisted() ? "yes" : "no";
            }
        };
        whitelistCol.setSortable(true);
        subscriptionTable.addColumn(whitelistCol, "Whitelist?");

        subscriptionTable.addColumn(actionColumn = createActionColumn(), "Actions");


        // sorting

        sortHandler = new ColumnSortEvent.ListHandler<Subscription>(dataProvider.getList()) {
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
        dataPanel.add(pager);


        // filtering

        final List<Header<?>> filterHeaders = createFilterHeaders();
        subscriptionTable.setHeaderBuilder(new TwoRowHeaderBuilder(subscriptionTable, false, filterHeaders));
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    protected void initStyles() {
        /*Style dataStyle = dataPanel.getElement().getStyle();
        dataStyle.setPosition(Style.Position.ABSOLUTE);
        dataStyle.setTop(140, Style.Unit.PX);
        dataStyle.setBottom(30, Style.Unit.PX);
        dataStyle.setLeft(1, Style.Unit.EM);
        dataStyle.setRight(1, Style.Unit.EM);
        dataStyle.setOverflowY(Style.Overflow.AUTO);*/
    }

    /**
     * Adds a separator line to the action buttons panel.
     */
    public void addActionSeparator() {
        HTMLPanel span = new HTMLPanel("span", " | ");
        span.getElement().getStyle().setColor("#999");
        buttonsPanel.add(span);
    }

    /**
     * @param caption           button title
     * @param clickHandler      action clickHandler
     * @param separator         show separator in front of button
     * @param isPermanentAction should the button be active even if no items are selected?
     */
    public void addAction(String caption, final ClickHandler clickHandler, boolean isPermanentAction) {
        Button button = new Button(caption, clickHandler);

        if (isPermanentAction) {
            button.setEnabled(true);
        } else {
            button.setEnabled(false);
        }

        buttonsPanel.add(new HTMLPanel("span", " "));

        if (!isPermanentAction) {
            actionButtons.add(button);
        }
        buttonsPanel.add(button);
    }

    private void enableActionButtons(boolean enable) {
        for (Button button : actionButtons) {
            button.setEnabled(enable);
        }
    }

    private void initActionPanel() {
        panel.add(buttonsPanel);

        Style style = buttonsPanel.getElement().getStyle();
//        style.setProperty("borderBottom", "1px solid #999");
        style.setProperty("paddingBottom", "7px");

        addDefaultActions();
    }

    protected void addDefaultActions() {
    }

    public List<Subscription> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public SubscriptionSearchPredicate getSearchPredicate() {
        return searchPredicate;
    }

    public void clearSelection() {
        selectedItems.clear();
        enableActionButtons(false);
    }

    public List<Subscription> getItemList() {
        return data;
    }

    public void refresh() {
        List<Subscription> filteredList = Lists.newArrayList(Iterables.filter(data, searchPredicate));
        clearSelection();
        dataProvider.setList(filteredList);
        sortHandler.setList(filteredList);
    }

    /**
     * Creates list of headers containing input boxes for specifying filtering criteria.
     *
     * @return list of headers
     */
    protected List<Header<?>> createFilterHeaders() {
        final List<Header<?>> filterHeaders = new ArrayList<>();
        filterHeaders.add(null);

        // krb name
        TextInputCell krbNameInputCell = new TextInputCell();
        filterCells.add(krbNameInputCell);
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
                refresh();
            }
        });
        filterHeaders.add(krbNameFilterHeader);

        // github name
        TextInputCell gitHubNameInputCell = new TextInputCell();
        filterCells.add(gitHubNameInputCell);
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
                refresh();
            }
        });
        filterHeaders.add(gitHubNameFilterHeader);

        filterHeaders.add(new TextHeader("")); // empty header for note column

        // krb account
        final DropDownCell krbAccountSelectionCell = new DropDownCell(KRB_ACCOUNT_FILTER_OPTIONS);
        filterCells.add(krbAccountSelectionCell);
        Header<String> krbAccountFilterHeader = new Header<String>(krbAccountSelectionCell) {
            @Override
            public String getValue() {
                return "";
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
                refresh();
            }
        });
        filterHeaders.add(krbAccountFilterHeader);

        // whitelist
        final DropDownCell whitelistCell = new DropDownCell(KRB_ACCOUNT_FILTER_OPTIONS);
        filterCells.add(whitelistCell);
        Header<String> whitelistFilterHeader = new Header<String>(whitelistCell) {
            @Override
            public String getValue() {
                return "";
            }
        };
        whitelistFilterHeader.setUpdater(new ValueUpdater<String>() {
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

                searchPredicate.setWhitelisted(boolValue);
                refresh();
            }
        });
        filterHeaders.add(whitelistFilterHeader);

        return filterHeaders;
    }

    /**
     * Creates last table column with action buttons.
     *
     * @return action column
     */
    private Column<Subscription, Subscription> createActionColumn() {
        addDefaultActionCells();

        final Cell<Subscription> cell = new CompositeCell<>(hasCells);
        return new Column<Subscription, Subscription>(cell) {
            @Override
            public Subscription getValue(Subscription object) {
                return object;
            }
        };
    }

    protected void addDefaultActionCells() {
        // subscriptions button
        addActionCell(new ConditionalActionCell<Subscription>(SafeHtmlUtils.fromString("Modify"), new ModifyUserDelegate()));
    }

    protected void addActionCell(ConditionalActionCell<Subscription> actionCell) {
        hasCells.add(actionCell);
    }

    public void setData(List<Subscription> data) {
        this.data = data;
        dataProvider.setList(data);
        sortHandler.setList(data);

        // clear filtering fields
        searchPredicate.reset();
        for (AbstractInputCell filterCell: filterCells) {
            filterCell.clearViewData("");
        }
    }


    // comparators

    /**
     * Compares Subscription objects by krb name.
     */
    private static class KrbNameComparator implements Comparator<Subscription> {
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
    private static class GitHubNameComparator implements Comparator<Subscription> {
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
    private static class IsRegisteredComparator implements Comparator<Subscription> {
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
     * Subscribe button delegate.
     */
    private class ModifyUserDelegate implements ActionCell.Delegate<Subscription> {
        @Override
        public void execute(final Subscription object) {
            // displays subscription dialog
            final UserAndSubscriptionsUserDialog dialog = new UserAndSubscriptionsUserDialog(object) {
                @Override
                protected void onUserSavedCallback(RegisteredUser registeredUser) {
                    SubscriptionsTable.this.refresh();
                }
            };
            dialog.center();
        }
    }


    // predicates

    /**
     * Predicate for filtering subscriptions according to given criteria.
     * <p/>
     * Any subscription with krb name and/or github name *containing* given strings qualifies.
     */
    public static class SubscriptionSearchPredicate implements Predicate<Subscription> {

        private String krbNameExpression;
        private String gitHubNameExpression;
        private Boolean krbAccount;
        private Boolean whitelisted;

        @Override
        public boolean apply(@Nullable Subscription o) {
            if (o == null) {
                return false;
            } else if (isEmpty()) {
                return true;
            }
            return (isEmpty(krbNameExpression) || (o.getKerberosName() != null && o.getKerberosName().toLowerCase().contains(krbNameExpression.toLowerCase())))
                    && (isEmpty(gitHubNameExpression) || (o.getGitHubName() != null && o.getGitHubName().toLowerCase().contains(gitHubNameExpression.toLowerCase())))
                    && (krbAccount == null || krbAccount == o.isActiveKerberosAccount())
                    && (whitelisted == null || whitelisted == o.isWhitelisted());
        }

        public boolean isEmpty() {
            return isEmpty(krbNameExpression) && isEmpty(gitHubNameExpression) && krbAccount == null && whitelisted == null;
        }

        private boolean isEmpty(String value) {
            return value == null || "".equals(value);
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

        public void setWhitelisted(Boolean whitelisted) {
            this.whitelisted = whitelisted;
        }

        public String getKrbNameExpression() {
            return krbNameExpression;
        }

        public String getGitHubNameExpression() {
            return gitHubNameExpression;
        }

        public Boolean getKrbAccount() {
            return krbAccount;
        }

        public Boolean getWhitelisted() {
            return whitelisted;
        }

        public void reset() {
            this.gitHubNameExpression = "";
            this.krbNameExpression = "";
            this.krbAccount = null;
            this.whitelisted = null;
        }

        @Override
        public String toString() {
            return "SearchPredicate{" +
                    "krbNameExpression='" + krbNameExpression + '\'' +
                    ", gitHubNameExpression='" + gitHubNameExpression + '\'' +
                    ", krbAccount=" + krbAccount +
                    ", whitelisted=" + whitelisted +
                    '}';
        }
    }

}
