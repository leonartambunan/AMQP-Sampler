package com.sxi.jmeter.protocol.rpc.persistwatchlist;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractAddWatchListGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    private JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    private JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");

    private JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    private JLabeledTextField exchangeName = new JLabeledTextField("Exchange Name");
    private JLabeledTextField bindingKey = new JLabeledTextField("Binding Key");

    @Override
    public String getStaticLabel() {
        return "Trimegah Persist Watch List Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractAddWatchList)) return;
        AbstractAddWatchList sampler = (AbstractAddWatchList) element;

        sessionId.setText(sampler.getSessionId());
        exchangeName.setText(sampler.getExchangeName());
        bindingKey.setText(sampler.getBindingKey());

        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());


    }

    @Override
    public void clearGui() {
        sessionId.setText("");
        exchangeName.setText("");
        bindingKey.setText("");
        requestQueue.setText("olt.persist_watchlist_request-rpc");
        responseQueue.setText("");
    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);
        AbstractAddWatchList sampler = (AbstractAddWatchList) element;
        configureTestElement(sampler);

        sampler.setSessionId(sessionId.getText());
        sampler.setExchangeName(exchangeName.getText());
        sampler.setBindingKey(bindingKey.getText());

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
                BorderFactory.createEtchedBorder(), "Watch List Queues"));

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
                BorderFactory.createEtchedBorder(), "Watch List Request Detail"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(exchangeName, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        orderSettings.add(bindingKey, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
