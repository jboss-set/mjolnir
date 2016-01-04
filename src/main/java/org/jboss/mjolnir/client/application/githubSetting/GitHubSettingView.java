package org.jboss.mjolnir.client.application.githubSetting;

import java.util.List;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.jboss.mjolnir.client.component.util.HTMLUtil;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class GitHubSettingView extends ViewWithUiHandlers<GitHubSettingHandlers>
        implements GitHubSettingPresenter.MyView {

    interface Binder extends UiBinder<Widget, GitHubSettingView> {
    }

    @UiField
    SubmitButton submitButton;

    @UiField
    HTML feedbackLabel;

    @UiField
    TextBox gitHubNameInput;

    @Inject
    public GitHubSettingView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
    }

    @Override
    public void setData(String gitHubName) {
        gitHubNameInput.setText(gitHubName);
        submitButton.setEnabled(true);
    }

    @Override
    public void setFeedbackMessage(List<String> messages) {
        feedbackLabel.setHTML(HTMLUtil.toUl(messages));
        submitButton.setEnabled(true);
    }


    @UiHandler("form")
    public void onSave(FormPanel.SubmitEvent event) {
        submitButton.setEnabled(false);
        String gitHubName = gitHubNameInput.getText();
        getUiHandlers().saveSetting(gitHubName);
    }
}
