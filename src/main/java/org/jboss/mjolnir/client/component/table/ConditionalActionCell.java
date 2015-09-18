package org.jboss.mjolnir.client.component.table;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Action cell that can be enabled / disabled according to state of the instance which it represents.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ConditionalActionCell<C> extends ActionCell<C> implements HasCell<C, C> {

    private SafeHtml html;

    public ConditionalActionCell(SafeHtml message, Delegate<C> delegate) {
        super(message, delegate);
        this.html = new SafeHtmlBuilder().appendHtmlConstant(
                "<button type=\"button\" class=\"gwt-Button narrow\" tabindex=\"-1\">").append(message).appendHtmlConstant(
                "</button>").toSafeHtml();
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        if (isEnabled(value)) {
            sb.append(html);
        }
    }

    /**
     * Specifies whether action cell is active.
     *
     * @param value instance representing current row
     * @return is action cell active?
     */
    public boolean isEnabled(C value) {
        return true;
    }

    @Override
    public Cell<C> getCell() {
        return this;
    }

    @Override
    public FieldUpdater<C, C> getFieldUpdater() {
        return null;
    }

    @Override
    public C getValue(C object) {
        return object;
    }
}