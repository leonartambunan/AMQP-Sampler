package com.sxi.jmeter.protocol.amqp.logout;

import com.sxi.jmeter.protocol.amqp.constants.Trimegah;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLogoutGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField serverQueue = new JLabeledTextField("Logout Queue");
    protected JLabeledTextField replyToQueue = new JLabeledTextField("ReplyTo Queue");
    protected JLabeledTextField virtualHost = new JLabeledTextField("Virtual Host");

    protected JLabeledTextField host = new JLabeledTextField("Host");
    protected JLabeledTextField port = new JLabeledTextField("Port");
    protected JLabeledTextField timeout = new JLabeledTextField("Timeout");
    protected JLabeledTextField username = new JLabeledTextField("Username");
    protected JLabeledTextField password = new JLabeledTextField("Password");
    private final JCheckBox SSL = new JCheckBox("SSL", false);

    protected JLabeledTextField mobileUserId = new JLabeledTextField("Mobile User ID");
    protected JLabeledTextField sessionID = new JLabeledTextField("Session ID");

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractLogout)) return;
        AbstractLogout sampler = (AbstractLogout) element;

        virtualHost.setText(sampler.getVirtualHost());
        timeout.setText(sampler.getTimeout());
        host.setText(sampler.getHost());
        port.setText(sampler.getPort());
        username.setText(sampler.getUsername());
        password.setText(sampler.getPassword());
        SSL.setSelected(sampler.isConnectionSSL());

        mobileUserId.setText(sampler.getMobileUserId());
        sessionID.setText(sampler.getSessionId());

        serverQueue.setText(sampler.getServerQueue());
        replyToQueue.setText(sampler.getReplyToQueue());

    }

    @Override
    public void clearGui() {

        serverQueue.setText(AbstractLogout.DEFAULT_SERVER_QUEUE);
        replyToQueue.setText(AbstractLogout.DEFAULT_REPLY_QUEUE);

        virtualHost.setText("/");

        timeout.setText(""+Trimegah.TIMEOUT);

        host.setText(Trimegah.HOST);
        port.setText(Trimegah.PORT);
        username.setText(Trimegah.USERNAME);
        password.setText(Trimegah.PASSWORD);
        SSL.setSelected(false);

        mobileUserId.setText(Trimegah.MOBILE_USER_ID);

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractLogout sampler = (AbstractLogout) element;

        sampler.clear();

        configureTestElement(sampler);

        sampler.setVirtualHost(virtualHost.getText());

        sampler.setTimeout(timeout.getText());

        sampler.setHost(host.getText());
        sampler.setPort(port.getText());
        sampler.setUsername(username.getText());
        sampler.setPassword(password.getText());
        sampler.setConnectionSSL(SSL.isSelected());

        sampler.setMobileUserId(mobileUserId.getText());

        sampler.setSessionId(sessionID.getText());

        sampler.setServerQueue(serverQueue.getText());

        sampler.setReplyToQueue(replyToQueue.getText());
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
        gridBagConstraintsCommon.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsCommon.anchor = GridBagConstraints.WEST;
        gridBagConstraintsCommon.weightx = 0.5;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        JPanel commonPanel = new JPanel(new GridBagLayout());

        JPanel queueSettings = new JPanel(new GridBagLayout());
        queueSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Queues"));
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        queueSettings.add(serverQueue, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(replyToQueue, gridBagConstraints);

        JPanel mobileSettings = new JPanel(new GridBagLayout());
        mobileSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mobile Device"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        mobileSettings.add(mobileUserId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        mobileSettings.add(sessionID, gridBagConstraints);


        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 0;

        JPanel mobileQueueSettings = new VerticalPanel();
        mobileQueueSettings.add(mobileSettings);
        mobileQueueSettings.add(queueSettings);

        commonPanel.add(mobileQueueSettings, gridBagConstraintsCommon);

        JPanel serverSettings = new JPanel(new GridBagLayout());
        serverSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connection"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        serverSettings.add(virtualHost, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        serverSettings.add(host, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        serverSettings.add(port, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        serverSettings.add(SSL, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        serverSettings.add(username, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        serverSettings.add(password, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        serverSettings.add(timeout, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 0;
        commonPanel.add(serverSettings, gridBagConstraintsCommon);

        return commonPanel;
    }

}
