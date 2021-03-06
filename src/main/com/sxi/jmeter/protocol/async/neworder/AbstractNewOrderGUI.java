package com.sxi.jmeter.protocol.async.neworder;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractNewOrderGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseExchange = new JLabeledTextField("Response Exchange");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");

    protected JLabeledTextField sessionId = new JLabeledTextField("Session ID");
    protected JLabeledTextField stockCode = new JLabeledTextField("Stock Code");
    protected JLabeledTextField orderQty = new JLabeledTextField("Order Qty");

    protected JLabeledTextField orderPrice = new JLabeledTextField("Order Price");
    protected JLabeledTextField board = new JLabeledTextField("Board");
    protected JLabeledTextField investorType = new JLabeledTextField("Investor Type");
    protected JLabeledTextField buySell = new JLabeledTextField("Buy/Sell (B/S)");
    protected JLabeledTextField clientCode = new JLabeledTextField("Client Code");
    protected JLabeledTextField timeInForce = new JLabeledTextField("Time in Force (0,1,6)");
    protected JLabeledTextField orderPeriod = new JLabeledTextField("Order Period");

    @Override
    public String getStaticLabel() {
        return "Trimegah New Order Async Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractNewOrder)) return;
        AbstractNewOrder sampler = (AbstractNewOrder) element;

        requestQueue.setText(sampler.getRequestQueue());
        responseExchange.setText(sampler.getResponseExchange());
        routingKey.setText(sampler.getRoutingKey());

        stockCode.setText(sampler.getStockCode());
        orderQty.setText(sampler.getOrderQty());
        orderPrice.setText(sampler.getOrderPrice());
        board.setText(sampler.getBoard());
        investorType.setText(sampler.getInvestorType());
        timeInForce.setText(sampler.getTimeInForce());
        clientCode.setText(sampler.getClientCode());
        buySell.setText(sampler.getBuySell());
        orderPeriod.setText(sampler.getOrderPeriod());
        sessionId.setText(sampler.getSessionId());
//        orderRef.setText(sampler.getOrderRef());

    }

    @Override
    public void clearGui() {

        super.clearGui();

        stockCode.setText("TRIM");
        orderQty.setText("1");
        orderPrice.setText("");
        board.setText("RG");
        investorType.setText("");
        timeInForce.setText("0");
        clientCode.setText("");
        buySell.setText("B");
        orderPeriod.setText("");
        requestQueue.setText("olt.new_olt_order");
        responseExchange.setText("olt.order_reply");
        routingKey.setText("");
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);

        AbstractNewOrder sampler = (AbstractNewOrder) element;

        configureTestElement(sampler);

        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseExchange(responseExchange.getText());
        sampler.setRoutingKey(routingKey.getText());

        sampler.setStockCode(stockCode.getText());
        sampler.setOrderQty(orderQty.getText());
        sampler.setOrderPrice(orderPrice.getText());
        sampler.setBoard(board.getText());
        sampler.setInvestorType(investorType.getText());
        sampler.setTimeInForce(timeInForce.getText());
        sampler.setClientCode(clientCode.getText());
        sampler.setBuySell(buySell.getText());
        sampler.setOrderPeriod(orderPeriod.getText());
        sampler.setSessionId(sessionId.getText());
//        sampler.setOrderRef(orderRef.getText());

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
        queueSettings.add(requestQueue, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(responseExchange, gridBagConstraints);

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
        orderSettings.add(stockCode, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(orderQty, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        orderSettings.add(buySell, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        orderSettings.add(clientCode, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        orderSettings.add(board, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        orderSettings.add(investorType, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        orderSettings.add(buySell, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        orderSettings.add(timeInForce, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        orderSettings.add(orderPeriod, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        orderSettings.add(orderPrice, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
