package com.sxi.jmeter.protocol.async.cancelorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import com.sxi.jmeter.protocol.rpc.login.AbstractLoginGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractCancelOrderGUI extends AbstractRabbitGUI{

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestExchangeName = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseExchangeName = new JLabeledTextField("Response Exchange");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");
    protected JLabeledTextField sessionId = new JLabeledTextField("Session ID");

    protected JLabeledTextField orderRef = new JLabeledTextField("Order Ref");
    protected JLabeledTextField orderId = new JLabeledTextField("Order Id");

    @Override
    public String getStaticLabel() {
        return "Trimegah Cancel Order Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractCancelOrder)) return;
        AbstractCancelOrder sampler = (AbstractCancelOrder) element;

        requestExchangeName.setText(sampler.getRequestQueue());
        responseExchangeName.setText(sampler.getResponseExchange());
        routingKey.setText(sampler.getRoutingKey());
        sessionId.setText(sampler.getSessionId());
        orderRef.setText(sampler.getOrderRef());
        orderId.setText(sampler.getOrderId());


    }

    @Override
    public void clearGui() {
        orderRef.setText("TRIM");
        orderId.setText("1");
        requestExchangeName.setText("olt.cancel_olt_order_request");
        responseExchangeName.setText("olt.order_reply");
        routingKey.setText("");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);

        AbstractCancelOrder sampler = (AbstractCancelOrder) element;

        configureTestElement(sampler);

        sampler.setRequestQueue(requestExchangeName.getText());
        sampler.setResponseExchange(responseExchangeName.getText());
        sampler.setRoutingKey(routingKey.getText());
        sampler.setSessionId(sessionId.getText());

        sampler.setOrderRef(orderRef.getText());
        sampler.setOrderId(orderId.getText());

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
        orderSettings.add(orderRef, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(orderId, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
