package com.sxi.jmeter.protocol.test;

import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class TestGUI extends AbstractTestGUI {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public TestGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return "Trimegah Test Sampler";
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
    }

    @Override
    public TestElement createTestElement() {
        Test sampler = new Test();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        Test sampler = (Test) te;
        sampler.clear();
        configureTestElement(sampler);
        super.modifyTestElement(sampler);
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

}