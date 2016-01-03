package com.sxi.jmeter.protocol.amqp.gui;

import com.sxi.jmeter.protocol.amqp.PreOpening;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class PreOpeningGUI extends AbstractPreOpeningSamplerGUI{

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public PreOpeningGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return "Trimegah PreOpening Sampler";
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof PreOpening)) return;
        PreOpening sampler = (PreOpening) element;
        configureHeaders(sampler);
    }

    @Override
    public TestElement createTestElement() {
        PreOpening sampler = new PreOpening();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        PreOpening sampler = (PreOpening) te;
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

    private void configureHeaders(PreOpening sampler)
    {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }
}