package com.sxi.jmeter.protocol.amqp.marketinfo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class MarketInfoGUI extends AbstractMarketInfoGUI {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public MarketInfoGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return "Trimegah Market Info Sampler";
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof MarketInfo)) return;
        MarketInfo sampler = (MarketInfo) element;
        configureHeaders(sampler);
    }

    @Override
    public TestElement createTestElement() {
        MarketInfo sampler = new MarketInfo();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        MarketInfo sampler = (MarketInfo) te;
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

    private void configureHeaders(MarketInfo sampler)
    {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }
}