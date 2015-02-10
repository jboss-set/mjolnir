package org.jboss.mjolnir.client.component.table;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Action cell that can be enabled / disabled according to state of the instance which it represents.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public abstract class ConditionalActionCell<C> extends ActionCell<C> {

    public ConditionalActionCell(String text, Delegate<C> delegate) {
        super(text, delegate);
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        if (isEnabled(value)) {
            super.render(context, value, sb);
        }
    }

    /**
     * Specifies whether action cell is active.
     *
     * @param value instance representing current row
     * @return is action cell active?
     */
    public abstract boolean isEnabled(C value);
}