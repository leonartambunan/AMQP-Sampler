package com.sxi.jmeter.protocol.rpc.getwatchlist;

import com.sxi.jmeter.protocol.base.AbstractRabbitGUI;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGetWatchListGUI extends AbstractRabbitGUI {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField requestQueue = new JLabeledTextField("Request Queue");
    protected JLabeledTextField responseQueue = new JLabeledTextField("Response Queue");

    protected JLabeledTextField sessionId = new JLabeledTextField("Session Id");

    @Override
    public String getStaticLabel() {
        return "Trimegah Get Watch List Sampler";
    }

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractGetWatchList)) return;
        AbstractGetWatchList sampler = (AbstractGetWatchList) element;

        sessionId.setText(sampler.getSessionId());
        requestQueue.setText(sampler.getRequestQueue());
        responseQueue.setText(sampler.getResponseQueue());

    }

    @Override
    public void clearGui() {
        sessionId.setText("");
        requestQueue.setText("olt.get_watchlist_request-rpc");
        responseQueue.setText("");
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);
        AbstractGetWatchList sampler = (AbstractGetWatchList) element;

        configureTestElement(sampler);

        sampler.setSessionId(sessionId.getText());
        sampler.setRequestQueue(requestQueue.getText());
        sampler.setResponseQueue(responseQueue.getText());

    }

//    protected void init() {
//        setLayout(new BorderLayout(0, 5));
//        setBorder(makeBorder());
//        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title
//
//        JPanel mainPanel = new VerticalPanel();
//
//        mainPanel.add(makeCommonPanel());
//
//        add(mainPanel);
//
//        setMainPanel(mainPanel);
//    }

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
                BorderFactory.createEtchedBorder(), "Watch List Queues"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        queueSettings.add(requestQueue, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(responseQueue, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(queueSettings, gridBagConstraintsCommon);

        JPanel orderSettings = new JPanel(new GridBagLayout());
        orderSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Watch List Request Detail"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(sessionId, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(orderSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
