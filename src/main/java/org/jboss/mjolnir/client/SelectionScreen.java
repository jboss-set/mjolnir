package org.jboss.mjolnir.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Screen that will allow users to select whether they want to proceed to the subscriptions screen or to modify their
 * github username if it is incorrect. This will only be shown if the user has successfully logged in and is already
 * registered.
 *
 * @version : 0.3
 * @author: navssurtani
 */
public class SelectionScreen extends Composite {
    private final String krb5Name;
    private final RootPanel selectionPanel;
    private Grid selectionGrid;

    public SelectionScreen(String krb5Name) {
        this.krb5Name = krb5Name;
        this.selectionPanel = RootPanel.get("selectionPanelContainer");
        buildPage();
    }

    private void buildPage() {
        selectionGrid = new Grid(4, 3);
        selectionGrid.setWidget(0, 0, new Label("Hello: "));
        selectionGrid.setWidget(0, 1, new Label(krb5Name));
        selectionGrid.setWidget(2, 0, new Label("What would you like to do?"));
        Button subscriptions = subscriptionsButton();
        Button modify = modifyButton();
        selectionGrid.setWidget(3, 0, subscriptions);
        selectionGrid.setWidget(3, 2, modify);
        selectionPanel.add(selectionGrid);
    }

    private Button modifyButton() {
        Button b = new Button("Modify Github name");
        b.getElement().setId("modify");
        b.setEnabled(true);
        b.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionPanel.remove(selectionGrid);
                EntryPage.getInstance().moveToGithubModifyScreen(krb5Name);
            }
        });
        return b;
    }

    private Button subscriptionsButton() {
        Button b = new Button("View subscriptions");
        b.getElement().setId("subscriptions");
        b.setEnabled(true);
        b.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionPanel.remove(selectionGrid);
                EntryPage.getInstance().moveToSubscriptionScreen(krb5Name);
            }
        });
        return b;
    }
}
