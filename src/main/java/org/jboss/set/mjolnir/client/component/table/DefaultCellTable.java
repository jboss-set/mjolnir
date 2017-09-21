package org.jboss.set.mjolnir.client.component.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class DefaultCellTable<T> extends CellTable<T> {

    interface TableResources extends CellTable.Resources {
        @Source({CellTable.Style.DEFAULT_CSS, "DefaultCellTable.css"})
        TableStyle cellTableStyle();
    }

    interface TableStyle extends CellTable.Style {
        String cellTableHeaderFilter();
    }


    public DefaultCellTable() {
        this(null);
    }

    public DefaultCellTable(ProvidesKey<T> keyProvider) {
        this(15, (TableResources) GWT.create(TableResources.class), keyProvider);
    }

    public DefaultCellTable(int pageSize, Resources resources, ProvidesKey<T> keyProvider) {
        super(pageSize, resources, keyProvider);
    }
}
