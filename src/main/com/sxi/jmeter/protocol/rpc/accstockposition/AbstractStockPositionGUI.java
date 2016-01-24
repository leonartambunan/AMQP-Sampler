package com.sxi.jmeter.protocol.rpc.accstockposition;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractStockPositionGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;
    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");

    protected JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    protected JLabeledTextField accNo = new JLabeledTextField("Acc No");
    protected JLabeledTextField stockCode = new JLabeledTextField("Stock Code");

    @Override
    public String getStaticLabel() {
        return "Trimegah Stock Position Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractStockPosition)) return;
        AbstractStockPosition sampler = (AbstractStockPosition) element;

        sessionId.setText(sampler.getSessionId());
        stockCode.setText(sampler.getStockCode());
        accNo.setText(sampler.getAccNo());
        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());

    }

    @Override
    public void clearGui() {
        sessionId.setText("");
        accNo.setText("");
        stockCode.setText("");
        responseQueue.setText("");
        requestQueue.setText("olt.acc_stock_pos_request-rpc");
    }

    @Override
    public void modifyTestElement(TestElement element) {

        super.modifyTestElement(element);
        AbstractStockPosition sampler = (AbstractStockPosition) element;

        configureTestElement(sampler);

        sampler.setSessionId(sessionId.getText());
        sampler.setAccNo(accNo.getText());
        sampler.setStockCode(stockCode.getText());

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
                BorderFactory.createEtchedBorder(), "Stock Position Queues"));

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
                BorderFactory.createEtchedBorder(), "Stock Request"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(accNo, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        orderSettings.add(stockCode, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
