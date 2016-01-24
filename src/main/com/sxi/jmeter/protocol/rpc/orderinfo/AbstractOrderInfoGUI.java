package com.sxi.jmeter.protocol.rpc.orderinfo;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractOrderInfoGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");

    protected JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    protected JLabeledTextField accNo = new JLabeledTextField("Acc No");

    @Override
    public String getStaticLabel() {
        return "Trimegah Order Info Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractOrderInfo)) return;
        AbstractOrderInfo sampler = (AbstractOrderInfo) element;

        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());

        sessionId.setText(sampler.getSessionId());
        accNo.setText(sampler.getAccNo());

    }

    @Override
    public void clearGui() {
        requestQueue.setText("olt.order_request-rpc");
    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractOrderInfo sampler = (AbstractOrderInfo) element;

        configureTestElement(sampler);

        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseQueue(responseQueue.getText());

        sampler.setSessionId(sessionId.getText());
        sampler.setAccNo(accNo.getText());

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
                BorderFactory.createEtchedBorder(), "Order Info Queues"));

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
                BorderFactory.createEtchedBorder(), "Order Info Request"));

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
