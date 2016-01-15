package com.sxi.jmeter.protocol.async.amendorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractAmendOrderGUI extends AbstractRabbitGUI{

    private static final long serialVersionUID = 1L;

    private JLabeledTextField requestExchangeName = new JLabeledTextField("Request Queue");
    private JLabeledTextField responseExchangeName = new JLabeledTextField("Response Exchange");
    private JLabeledTextField routingKey = new JLabeledTextField("Routing Key");
    private JLabeledTextField sessionId = new JLabeledTextField("Session ID");

    private JLabeledTextField orderRef = new JLabeledTextField("Old Order Ref");
    private JLabeledTextField orderId = new JLabeledTextField("Old Order ID");
    private JLabeledTextField orderQty = new JLabeledTextField("New Order Qty");
    private JLabeledTextField orderPrice = new JLabeledTextField("New Order Price");
    private JLabeledTextField orderPeriod = new JLabeledTextField("New Order Period");

    @Override
    public String getStaticLabel() {
        return "Trimegah Amend Order Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractAmendOrder)) return;
        AbstractAmendOrder sampler = (AbstractAmendOrder) element;

        responseExchangeName.setText(sampler.getResponseExchange());
        requestExchangeName.setText(sampler.getRequestQueue());
        routingKey.setText(sampler.getRoutingKey());
        sessionId.setText(sampler.getSessionId());
        orderId.setText(sampler.getOrderId());
        orderRef.setText(sampler.getOrderRef());
        orderQty.setText(sampler.getOrderQty());
        orderPrice.setText(sampler.getOrderPrice());
        orderPeriod.setText(sampler.getOrderPeriod());

    }

    @Override
    public void clearGui() {
        orderQty.setText("1");
        orderPrice.setText("");
        orderPeriod.setText("");
        requestExchangeName.setText("olt.amend_olt_order_request");
        responseExchangeName.setText("olt.order_reply");
    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractAmendOrder sampler = (AbstractAmendOrder) element;

        configureTestElement(sampler);

        sampler.setRequestQueue(requestExchangeName.getText());
        sampler.setResponseExchange(responseExchangeName.getText());
        sampler.setRoutingKey(routingKey.getText());
        sampler.setSessionId(sessionId.getText());

        sampler.setOrderId(orderId.getText());
        sampler.setOrderRef(orderRef.getText());
        sampler.setOrderQty(orderQty.getText());
        sampler.setOrderPrice(orderPrice.getText());
        sampler.setOrderPeriod(orderPeriod.getText());

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

        queueSettings.add(requestExchangeName, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(responseExchangeName, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        queueSettings.add(routingKey, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        queueSettings.add(sessionId, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(queueSettings, gridBagConstraintsCommon);

        JPanel orderSettings = new JPanel(new GridBagLayout());
        orderSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Order Detail"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(orderId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(orderRef, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        orderSettings.add(orderPrice, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        orderSettings.add(orderQty, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        orderSettings.add(orderPeriod, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
