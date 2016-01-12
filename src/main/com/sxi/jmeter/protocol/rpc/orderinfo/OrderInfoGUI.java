package com.sxi.jmeter.protocol.rpc.orderinfo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class OrderInfoGUI extends AbstractOrderInfoGUI {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public OrderInfoGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof OrderInfo)) return;
        OrderInfo sampler = (OrderInfo) element;

        configureHeaders(sampler);
    }

    @Override
    public TestElement createTestElement() {
        OrderInfo sampler = new OrderInfo();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        OrderInfo sampler = (OrderInfo) te;
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

    private void configureHeaders(OrderInfo sampler)
    {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }
}