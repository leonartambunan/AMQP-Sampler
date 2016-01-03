package com.sxi.jmeter.protocol.amqp.gui;

import com.sxi.jmeter.protocol.amqp.AbstractLoginSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLoginSamplerGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;

    protected JLabeledTextField serverQueue = new JLabeledTextField("Login Queue");
    protected JLabeledTextField replyToQueue = new JLabeledTextField("ReplyTo Queue");
    protected JLabeledTextField virtualHost = new JLabeledTextField("Virtual Host");

    protected JLabeledTextField host = new JLabeledTextField("Host");
    protected JLabeledTextField port = new JLabeledTextField("Port");
    protected JLabeledTextField timeout = new JLabeledTextField("Timeout");
    protected JLabeledTextField username = new JLabeledTextField("Username");
    protected JLabeledTextField password = new JLabeledTextField("Password");
    private final JCheckBox SSL = new JCheckBox("SSL", false);
    protected JLabeledTextField authenticatedConnectionVarName = new JLabeledTextField("Authenticated Connection Variable Name");

    protected JLabeledTextField mobileUserId = new JLabeledTextField("Mobile Userid");
    protected JLabeledTextField mobilePassword = new JLabeledTextField("Mobile Password");
    protected JLabeledTextField mobileDeviceId = new JLabeledTextField("Mobile Deviceid");
    protected JLabeledTextField mobileDeviceType = new JLabeledTextField("Mobile Device Type");
    protected JLabeledTextField mobileAppVersion = new JLabeledTextField("Mobile App Ver.");

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractLoginSampler)) return;
        AbstractLoginSampler sampler = (AbstractLoginSampler) element;

        virtualHost.setText(sampler.getVirtualHost());
        timeout.setText(sampler.getTimeout());
        host.setText(sampler.getHost());
        port.setText(sampler.getPort());
        username.setText(sampler.getUsername());
        password.setText(sampler.getPassword());
        SSL.setSelected(sampler.isConnectionSSL());
        authenticatedConnectionVarName.setText(sampler.getAuthenticatedConnectionVarName());

        mobileAppVersion.setText(sampler.getMobileAppVersion());
        mobileDeviceId.setText(sampler.getMobileDeviceid());
        mobilePassword.setText(sampler.getMobilePassword());
        mobileDeviceType.setText(sampler.getMobileType());
        mobileUserId.setText(sampler.getMobileUserid());

        serverQueue.setText(sampler.getServerQueue());
        replyToQueue.setText(sampler.getReplytoQueue());

    }

    @Override
    public void clearGui() {

        serverQueue.setText(AbstractLoginSampler.DEFAULT_SERVER_QUEUE);
        replyToQueue.setText(AbstractLoginSampler.DEFAULT_REPLY_QUEUE);

        virtualHost.setText("/");

        timeout.setText(AbstractLoginSampler.DEFAULT_TIMEOUT_STRING);

        host.setText("localhost");
        port.setText(AbstractLoginSampler.DEFAULT_PORT_STRING);
        username.setText("guest");
        password.setText("guest");
        SSL.setSelected(false);
        authenticatedConnectionVarName.setText("AUTH_CON");

        mobileAppVersion.setText("3.0");
        mobileDeviceId.setText("5f6d41cd63257be");
        mobilePassword.setText("pass");
        mobileDeviceType.setText("ANDROID");
        mobileUserId.setText("test");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractLoginSampler sampler = (AbstractLoginSampler) element;

        sampler.clear();

        configureTestElement(sampler);

        sampler.setVirtualHost(virtualHost.getText());

        sampler.setTimeout(timeout.getText());

        sampler.setHost(host.getText());
        sampler.setPort(port.getText());
        sampler.setUsername(username.getText());
        sampler.setPassword(password.getText());
        sampler.setConnectionSSL(SSL.isSelected());
        sampler.setAuthenticatedConnectionVarName(authenticatedConnectionVarName.getText());

        sampler.setMobileAppVersion(mobileAppVersion.getText());
        sampler.setMobileDeviceid(mobileDeviceId.getText());
        sampler.setMobilePassword(mobilePassword.getText());
        sampler.setMobileType(mobileDeviceType.getText());
        sampler.setMobileUserid(mobileUserId.getText());

        sampler.setServerQueue(serverQueue.getText());
        sampler.setReplytoQueue(replyToQueue.getText());
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
        mobileSettings.add(mobilePassword, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        mobileSettings.add(mobileDeviceId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        mobileSettings.add(mobileDeviceType, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        mobileSettings.add(mobileAppVersion, gridBagConstraints);

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

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        serverSettings.add(authenticatedConnectionVarName, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 0;
        commonPanel.add(serverSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
