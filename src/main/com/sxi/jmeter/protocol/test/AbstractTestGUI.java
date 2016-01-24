package com.sxi.jmeter.protocol.test;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractTestGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField scheduledHour = new JLabeledTextField("Hour");
    protected JLabeledTextField scheduledMinute = new JLabeledTextField("Minute");
    protected JLabeledTextField scheduledSecond = new JLabeledTextField("Second");

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractTest)) return;
        AbstractTest sampler = (AbstractTest) element;

        scheduledHour.setText(sampler.getScheduleHour());
        scheduledMinute.setText(sampler.getScheduleMinute());
        scheduledSecond.setText(sampler.getScheduleSecond());
    }

    @Override
    public void clearGui() {

        scheduledHour.setText("10");
        scheduledMinute.setText("0");
        scheduledSecond.setText("0");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractTest sampler = (AbstractTest) element;

        sampler.clear();

        configureTestElement(sampler);

        sampler.setScheduleHour(scheduledHour.getText());
        sampler.setScheduleMinute(scheduledMinute.getText());
        sampler.setScheduleSecond(scheduledSecond.getText());
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

    private Component makeCommonPanel() {
        GridBagConstraints gridBagConstraints, gridBagConstraintsCommon;

        gridBagConstraintsCommon = new GridBagConstraints();
        gridBagConstraintsCommon.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsCommon.anchor = GridBagConstraints.WEST;
        gridBagConstraintsCommon.weightx = 0.5;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        JPanel commonPanel = new JPanel(new GridBagLayout());

        JPanel scheduleSetting = new JPanel(new GridBagLayout());
        scheduleSetting.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Market Info Scheduled Time"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        scheduleSetting.add(scheduledHour, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        scheduleSetting.add(scheduledMinute, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        scheduleSetting.add(scheduledSecond, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 0;
        commonPanel.add(scheduleSetting, gridBagConstraintsCommon);

        return commonPanel;
    }
}
