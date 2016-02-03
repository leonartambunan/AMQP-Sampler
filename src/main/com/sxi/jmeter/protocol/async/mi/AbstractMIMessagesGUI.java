package com.sxi.jmeter.protocol.async.mi;

import com.sxi.jmeter.protocol.base.AbstractNotificationGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractMIMessagesGUI extends AbstractNotificationGUI {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField exchange = new JLabeledTextField("Exchange");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");

    @Override
    public String getStaticLabel() {
        return "Trimegah Notification Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractMIMessagesOrder)) return;
        AbstractMIMessagesOrder sampler = (AbstractMIMessagesOrder) element;

        exchange.setText(sampler.getExchange());
        routingKey.setText(sampler.getRoutingKey());

    }

    @Override
    public void clearGui() {

        super.clearGui();

        exchange.setText("mi.orderbook_summary-d");
        routingKey.setText("");
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);

        AbstractMIMessagesOrder sampler = (AbstractMIMessagesOrder) element;

        configureTestElement(sampler);

        sampler.setExchange(exchange.getText());
        sampler.setRoutingKey(routingKey.getText());

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
                BorderFactory.createEtchedBorder(), "Topic To Listen"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        queueSettings.add(exchange, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(routingKey, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(queueSettings, gridBagConstraintsCommon);

        JPanel orderSettings = new JPanel(new GridBagLayout());
        orderSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Order Detail"));

        return commonPanel;
    }
}
