package com.sxi.jmeter.protocol.amqp.order;

import com.sxi.jmeter.protocol.amqp.login.AbstractLoginGUI;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractNewOrderGUI extends AbstractLoginGUI{

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField stockCode = new JLabeledTextField("Stock Code");
    protected JLabeledTextField stockQty = new JLabeledTextField("Stock Qty");

    @Override
    public String getStaticLabel() {
        return "Trimegah New Order Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractNewOrder)) return;
        AbstractNewOrder sampler = (AbstractNewOrder) element;

        stockCode.setText(sampler.getStockCode());
        stockQty.setText(sampler.getStockQty());

    }

    @Override
    public void clearGui() {

        stockCode.setText("");
        stockQty.setText("1");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractNewOrder sampler = (AbstractNewOrder) element;

        sampler.clear();

        configureTestElement(sampler);

        sampler.setStockCode(stockCode.getText());
        sampler.setStockQty(stockQty.getText());
    }

    protected void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title

        JPanel mainPanel = new VerticalPanel();

        mainPanel.add(makeCommonPanel());

        add(mainPanel);

        setMainPanel(mainPanel);
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


        JPanel orderSettings = new JPanel(new GridBagLayout());
        orderSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Order Detail"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(stockCode, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(stockQty, gridBagConstraints);
//
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 2;
//        serverSettings.add(port, gridBagConstraints);
//
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 2;
//        serverSettings.add(SSL, gridBagConstraints);
//
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        serverSettings.add(username, gridBagConstraints);
//
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 4;
//        serverSettings.add(password, gridBagConstraints);
//
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 5;
//        serverSettings.add(timeout, gridBagConstraints);
//
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 6;
//        serverSettings.add(varNameAuthenticatedCon, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
