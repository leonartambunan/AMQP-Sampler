package com.sxi.jmeter.protocol.async.subscribeorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import com.sxi.jmeter.protocol.rpc.login.AbstractLoginGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractSubscribeOrderGUI extends AbstractRabbitGUI{

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestQueueName = new JLabeledTextField("Request Queue Name");
    protected JLabeledTextField responseExchangeName = new JLabeledTextField("Response Exchange Name");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");

    protected JLabeledTextField accNo = new JLabeledTextField("Acc No");
    protected JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    protected JLabeledTextField cif = new JLabeledTextField("CIF");
    protected JLabeledTextField orderRef = new JLabeledTextField("Order Ref");
    protected JLabeledTextField productCode = new JLabeledTextField("Product Code");
    protected JLabeledTextField productId = new JLabeledTextField("Product Id");

    @Override
    public String getStaticLabel() {
        return "Trimegah MF Subscribe Order Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractSubscribeOrder)) return;
        AbstractSubscribeOrder sampler = (AbstractSubscribeOrder) element;

        responseExchangeName.setText(sampler.getResponseExchange());
        requestQueueName.setText(sampler.getRequestQueue());
        routingKey.setText(sampler.getRoutingKey());

        accNo.setText(sampler.getStockCode());
        sessionId.setText(sampler.getSessionId());
        cif.setText(sampler.getCif());
        orderRef.setText(sampler.getOrderRef());
        productCode.setText(sampler.getProductCode());
        productId.setText(sampler.getProductId());

    }

    @Override
    public void clearGui() {
        accNo.setText("");
        sessionId.setText("");
        cif.setText("");
        orderRef.setText("");
        productCode.setText("");
        productId.setText("");
        requestQueueName.setText("olt.mf_subscribe_order");
        responseExchangeName.setText("olt.order_reply");
        routingKey.setText("");
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);

        AbstractSubscribeOrder sampler = (AbstractSubscribeOrder) element;

        configureTestElement(sampler);

        sampler.setRequestQueue(requestQueueName.getText());
        sampler.setResponseExchange(responseExchangeName.getText());
        sampler.setRoutingKey(routingKey.getText());

        sampler.setStockCode(accNo.getText());
        sampler.setSessionId(sessionId.getText());
        sampler.setCif(cif.getText());
        sampler.setOrderRef(orderRef.getText());
        sampler.setProductCode(productCode.getText());
        sampler.setProductId(productId.getText());

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
                BorderFactory.createEtchedBorder(), "Order Queues"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;

        queueSettings.add(requestQueueName, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(routingKey, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        queueSettings.add(responseExchangeName, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(queueSettings, gridBagConstraintsCommon);

        JPanel orderSettings = new JPanel(new GridBagLayout());
        orderSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Order Detail"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(accNo, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        orderSettings.add(productId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        orderSettings.add(orderRef, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        orderSettings.add(productCode, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        orderSettings.add(productId, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
