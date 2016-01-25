package com.sxi.jmeter.protocol.rpc.pinvalidation;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractPinValidationGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");

    protected JLabeledTextField sessionId = new JLabeledTextField("Session ID");
    protected JLabeledTextField pin = new JLabeledTextField("PIN");

    @Override
    public String getStaticLabel() {
        return "Trimegah Pin Validation RPC Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {

        super.configure(element);

        if (!(element instanceof AbstractPinValidation)) return;

        AbstractPinValidation sampler = (AbstractPinValidation) element;

        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());

        sessionId.setText(sampler.getSessionId());
        pin.setText(sampler.getPin());
    }

    @Override
    public void clearGui() {
        sessionId.setText("");
        pin.setText("1234");
        requestQueue.setText("olt.pin_validation_request-rpc");
        responseQueue.setText("amq.rabbitmq.reply-to");
        super.clearGui();

    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractPinValidation sampler = (AbstractPinValidation) element;

        configureTestElement(sampler);

        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseQueue(responseQueue.getText());

        sampler.setSessionId(sessionId.getText());
        sampler.setPin(pin.getText());

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
                BorderFactory.createEtchedBorder(), "Pin Validation Queues"));

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
        orderSettings.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "PIN Detail"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(pin, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
