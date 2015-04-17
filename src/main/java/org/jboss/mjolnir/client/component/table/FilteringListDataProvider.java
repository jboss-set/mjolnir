package org.jboss.mjolnir.client.component.table;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import org.jboss.mjolnir.client.component.administration.SubscriptionsTable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data provider with filtering capabilities.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class FilteringListDataProvider<T> extends ListDataProvider<T> {

    private final static Logger logger = Logger.getLogger(FilteringListDataProvider.class.getName());

    private SubscriptionsTable.SearchPreditcate filter;

    /**
     * {@inheritDoc}
     *
     * @param listToWrap list
     * @param filter predicate to be used for data filtering
     */
    public FilteringListDataProvider(List<T> listToWrap, SubscriptionsTable.SearchPreditcate<T> filter) {
        super(listToWrap);
        this.filter = filter;
    }

    /*@Override
    protected void updateRowData(int start, List<T> values) {
        if (filter != null) {
            logger.log(Level.SEVERE, "updateRowData start: " + start + ", before: " + values.size());
            final List<T> filteredValues = Lists.newArrayList(Iterables.filter(values, filter));
            logger.log(Level.SEVERE, "updateRowData after: " + filteredValues.size());
            super.updateRowData(start, filteredValues);
        } else {
            super.updateRowData(start, values);
        }
    }*/

    @Override
    protected void updateRowData(HasData<T> display, int start, List<T> values) {
        if (filter == null || filter.isEmpty()) {
            super.updateRowData(display, start, values);
        } else {
            List<T> filteredValues = Lists.newArrayList(Iterables.filter(values, filter));
            logger.log(Level.SEVERE, "Display: " + display.getClass().getSimpleName() + ", start: " + start + ", orig size: " + values.size() + ", new size: " + filteredValues.size());

            if (display.getRowCount() != filteredValues.size()) {
                logger.log(Level.SEVERE, "Resetting row count from " + display.getRowCount() + " to " + filteredValues.size());
                display.setRowCount(filteredValues.size());
            }

            int end = start + filteredValues.size();
            Range range = display.getVisibleRange();
            int curStart = range.getStart();
            int curLength = range.getLength();
            int curEnd = curStart + curLength;
            if (start == curStart || (curStart < end && curEnd > start)) {
                // Fire the handler with the data that is in the range.
                // Allow an empty list that starts on the page start.
                int realStart = curStart < start ? start : curStart;
                int realEnd = curEnd > end ? end : curEnd;
                int realLength = realEnd - realStart;
                List<T> realValues = filteredValues.subList(
                        realStart - start, realStart - start + realLength);
                logger.log(Level.SEVERE, "end: " + end + ", realStart: " + realStart + ", realEnd: " + realEnd + ", realValues: " + realValues.size());
                display.setRowData(realStart, realValues);
            }
        }
        /*logger.log(Level.SEVERE, "updateRowData2 start: " + start + ", before: " + values.size()
                + ", display: " + display.getClass().getSimpleName());
        List<T> filteredValues;
        if (filter != null) {
            filteredValues = Lists.newArrayList(Iterables.filter(values, filter));
        } else {
            filteredValues = values;
        }
        logger.log(Level.SEVERE, "updateRowData2 after: " + filteredValues.size());

        if (filteredValues.size() > 0) {
            super.updateRowData(display, start, filteredValues);
        }

        int rowCount = filteredValues.size() + start;
        if (display.getRowCount() != rowCount) {
            display.setRowCount(rowCount);
        }*/
    }
}
