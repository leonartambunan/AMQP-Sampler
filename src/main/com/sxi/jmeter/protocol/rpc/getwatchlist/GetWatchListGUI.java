package com.sxi.jmeter.protocol.rpc.getwatchlist;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;

public class GetWatchListGUI extends AbstractGetWatchListGUI {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;

    private ArgumentsPanel headers = new ArgumentsPanel("Headers");

    public GetWatchListGUI(){
        init();
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof GetWatchList)) return;
        GetWatchList sampler = (GetWatchList) element;

        configureHeaders(sampler);
    }

    @Override
    public TestElement createTestElement() {
        GetWatchList sampler = new GetWatchList();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        GetWatchList sampler = (GetWatchList) te;
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

    private void configureHeaders(GetWatchList sampler)
    {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }
}