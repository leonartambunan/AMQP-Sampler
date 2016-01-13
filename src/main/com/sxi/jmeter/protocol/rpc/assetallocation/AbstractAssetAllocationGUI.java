package com.sxi.jmeter.protocol.rpc.assetallocation;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractAssetAllocationGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 2L;
    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");
    protected JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    protected JLabeledTextField requestType = new JLabeledTextField("Request Type");

    @Override
    public String getStaticLabel() {
        return "Trimegah Asset Allocation Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractAssetAllocation)) return;
        AbstractAssetAllocation sampler = (AbstractAssetAllocation) element;
        sessionId.setText(sampler.getSessionId());
        requestType.setText(sampler.getRequestType());
        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());

    }

    @Override
    public void clearGui() {

        sessionId.setText("");
        requestType.setText("1");
        requestQueue.setText("olt.asset_alloc_request-rpc");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractAssetAllocation sampler = (AbstractAssetAllocation) element;

        configureTestElement(sampler);

        sampler.setSessionId(sessionId.getText());
        sampler.setRequestType(requestType.getText());

        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseQueue(responseQueue.getText());


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
                BorderFactory.createEtchedBorder(), "Asset Allocation Queues"));

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
                BorderFactory.createEtchedBorder(), "Asset Allocation Request"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(requestType, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
