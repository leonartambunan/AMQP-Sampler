package com.sxi.jmeter.protocol.rpc.logout;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import com.sxi.jmeter.protocol.rpc.constants.Trimegah;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLogoutGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");
    protected JLabeledTextField sessionID = new JLabeledTextField("Session ID");
    protected JLabeledTextField logoutReason = new JLabeledTextField("Reason");

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractLogout)) return;
        AbstractLogout sampler = (AbstractLogout) element;

        sessionID.setText(sampler.getSessionId());
        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());
        logoutReason.setText(sampler.getLogoutReason());

    }

    @Override
    public void clearGui() {

        sessionID.setText(Trimegah.MOBILE_USER_ID);


    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractLogout sampler = (AbstractLogout) element;

        configureTestElement(sampler);

        sampler.setSessionId(sessionID.getText());
        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseQueue(responseQueue.getText());
        sampler.setLogoutReason(logoutReason.getText());

    }

//    protected void init() {
//        setLayout(new BorderLayout(0, 5));
//        setBorder(makeBorder());
//        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title
//
//        JPanel mainPanel = new VerticalPanel();
//
//        mainPanel.add(makeCommonPanel());
//
//        add(mainPanel);
//
//        setMainPanel(mainPanel);
//    }

    protected Component makeCommonPanel() {

        JPanel commonPanel = (JPanel) super.makeCommonPanel();

        GridBagConstraints gridBagConstraints, gridBagConstraintsCommon;

        gridBagConstraintsCommon = new GridBagConstraints();
        gridBagConstraintsCommon.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsCommon.anchor = GridBagConstraints.WEST;
        gridBagConstraintsCommon.weightx = 0.5;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        JPanel queueSettings = new JPanel(new GridBagLayout());
        queueSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Logout Queues"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        queueSettings.add(requestQueue, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(responseQueue, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(queueSettings, gridBagConstraintsCommon);

        JPanel orderSettings = new JPanel(new GridBagLayout());

        orderSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Logout Request Data"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionID, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(logoutReason, gridBagConstraints);


        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }

}
