package com.sxi.jmeter.protocol.rpc.stockparam;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class StockParamGUI extends AbstractStockParamGUI {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public StockParamGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof StockParam)) return;
        StockParam sampler = (StockParam) element;

        configureHeaders(sampler);
    }

    @Override
    public TestElement createTestElement() {
        StockParam sampler = new StockParam();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        StockParam sampler = (StockParam) te;
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

    private void configureHeaders(StockParam sampler)
    {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }
}