package com.sxi.jmeter.protocol.amqp.orderhistory;

import com.sxi.jmeter.protocol.amqp.login.AbstractLoginGUI;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractOrderHistoryGUI extends AbstractLoginGUI{

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField sessionId = new JLabeledTextField("Session Id");
    protected JLabeledTextField accNo = new JLabeledTextField("Acc No");
    protected JLabeledTextField startDate = new JLabeledTextField("Start Date");
    protected JLabeledTextField endDate = new JLabeledTextField("End Date");

    @Override
    public String getStaticLabel() {
        return "Trimegah Order History Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractOrderHistory)) return;
        AbstractOrderHistory sampler = (AbstractOrderHistory) element;

        sessionId.setText(sampler.getSessionId());
        accNo.setText(sampler.getAccNo());
        startDate.setText(sampler.getStartDate());
        endDate.setText(sampler.getEndDate());

    }

    @Override
    public void clearGui() {

        sessionId.setText("");
        accNo.setText("");
        startDate.setText("");
        endDate.setText("");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractOrderHistory sampler = (AbstractOrderHistory) element;

        sampler.clear();

        configureTestElement(sampler);

        sampler.setSessionId(sessionId.getText());
        sampler.setAccNo(accNo.getText());
        sampler.setStartDate(startDate.getText());
        sampler.setEndDate(endDate.getText());

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
        orderSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Order History"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(accNo, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        orderSettings.add(startDate, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        orderSettings.add(endDate, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
