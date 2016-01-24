package com.sxi.jmeter.protocol.rpc.acccashposition;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractCashPositionGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    private JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    private JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");

    private JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    private JLabeledTextField accNo = new JLabeledTextField("Acc No");

    @Override
    public String getStaticLabel() {
        return "Trimegah Cash Position Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {

        super.configure(element);

        if (!(element instanceof AbstractCashPosition)) return;

        AbstractCashPosition sampler = (AbstractCashPosition) element;
        sessionId.setText(sampler.getSessionId());
        accNo.setText(sampler.getAccNo());
        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());
    }

    @Override
    public void clearGui() {
        sessionId.setText("");
        accNo.setText("");
        requestQueue.setText("olt.acc_cash_pos_request-rpc");
        responseQueue.setText("");
    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractCashPosition sampler = (AbstractCashPosition) element;
        //sampler.clear();

        configureTestElement(sampler);

        sampler.setSessionId(sessionId.getText());
        sampler.setAccNo(accNo.getText());
        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseQueue(responseQueue.getText());

    }


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
                BorderFactory.createEtchedBorder(), "Cash  Position Queues"));

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
                BorderFactory.createEtchedBorder(), "Cash Position Request"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(accNo, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
