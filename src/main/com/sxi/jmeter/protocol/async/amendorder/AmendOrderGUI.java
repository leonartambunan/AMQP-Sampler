package com.sxi.jmeter.protocol.async.amendorder;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class AmendOrderGUI extends AbstractAmendOrderGUI {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public AmendOrderGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AmendOrder)) return;
        AmendOrder sampler = (AmendOrder) element;

        configureHeaders(sampler);
    }

    @Override
    public TestElement createTestElement() {
        AmendOrder sampler = new AmendOrder();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        AmendOrder sampler = (AmendOrder) te;
        sampler.clear();
        configureTestElement(sampler);

        super.modifyTestElement(sampler);

        sampler.setHeaders((Arguments) headers.createTestElement());
    }

    @Override
    protected void setMainPanel(JPanel panel){
        mainPanel = panel;
    }

    @Override
    protected final void init() {
        super.init();
        mainPanel.add(headers);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        headers.clearGui();
    }

    private void configureHeaders(AmendOrder sampler)
    {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }
}