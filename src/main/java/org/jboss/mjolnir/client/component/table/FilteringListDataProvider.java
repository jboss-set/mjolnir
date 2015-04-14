package org.jboss.mjolnir.client.component.table;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

import java.util.List;

/**
 * Data provider with filtering capabilities.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class FilteringListDataProvider<T> extends ListDataProvider<T> {

    private Predicate<T> filter;

    /**
     * {@inheritDoc}
     *
     * @param listToWrap list
     * @param filter predicate to be used for data filtering
     */
    public FilteringListDataProvider(List<T> listToWrap, Predicate<T> filter) {
        super(listToWrap);
        this.filter = filter;
    }

    @Override
    protected void updateRowData(int start, List<T> values) {
        if (filter != null) {
            final List<T> filteredValues = Lists.newArrayList(Iterables.filter(values, filter));
            super.updateRowData(start, filteredValues);
        } else {
            super.updateRowData(start, values);
        }
    }

    @Override
    protected void updateRowData(HasData<T> display, int start, List<T> values) {
        List<T> filteredValues;
        if (filter != null) {
            filteredValues = Lists.newArrayList(Iterables.filter(values, filter));
        } else {
            filteredValues = values;
        }

        if (filteredValues.size() > 0) {
            super.updateRowData(display, start, filteredValues);
        }

        if (display.getRowCount() != filteredValues.size()) {
            display.setRowCount(filteredValues.size());
        }
    }
}
