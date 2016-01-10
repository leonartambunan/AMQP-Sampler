package com.sxi.jmeter.protocol.amqp.marketinfo;

import com.sxi.jmeter.protocol.amqp.constants.Trimegah;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractMarketInfoGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;
    protected JLabeledTextField virtualHost = new JLabeledTextField("Virtual Host");
    protected JLabeledTextField host = new JLabeledTextField("Host");
    protected JLabeledTextField port = new JLabeledTextField("Port");
    protected JLabeledTextField timeout = new JLabeledTextField("Timeout");
    protected JLabeledTextField username = new JLabeledTextField("Username");
    protected JLabeledTextField password = new JLabeledTextField("Password");
    private final JCheckBox SSL = new JCheckBox("SSL", false);
    protected JLabeledTextField mobileUserId = new JLabeledTextField("Mobile User ID");
    protected JLabeledTextField mobilePassword = new JLabeledTextField("Mobile Password");
    protected JLabeledTextField mobileDeviceId = new JLabeledTextField("Mobile Device ID");
    protected JLabeledTextField mobileDeviceType = new JLabeledTextField("Mobile Device Type");
    protected JLabeledTextField mobileAppVersion = new JLabeledTextField("Mobile App Ver.");

    protected JLabeledTextField loginQueue = new JLabeledTextField("Login Queue");
    protected JLabeledTextField loginReplyToQueue = new JLabeledTextField("Login ReplyTo Queue");
    protected JLabeledTextField marketInfoExchange = new JLabeledTextField("Market Info Exchange");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");

    protected JLabeledTextField scheduledHour = new JLabeledTextField("Hour");
    protected JLabeledTextField scheduledMinute = new JLabeledTextField("Minute");
    protected JLabeledTextField scheduledSecond = new JLabeledTextField("Second");

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractMarketInfo)) return;
        AbstractMarketInfo sampler = (AbstractMarketInfo) element;

        virtualHost.setText(sampler.getVirtualHost());

        timeout.setText(sampler.getTimeout());

        host.setText(sampler.getHost());
        port.setText(sampler.getPort());
        username.setText(sampler.getUsername());
        password.setText(sampler.getPassword());
        SSL.setSelected(sampler.isConnectionSSL());

        mobileAppVersion.setText(sampler.getMobileAppVersion());
        mobileDeviceId.setText(sampler.getMobileDeviceId());
        mobilePassword.setText(sampler.getMobilePassword());
        mobileDeviceType.setText(sampler.getMobileType());
        mobileUserId.setText(sampler.getMobileUserId());

        loginQueue.setText(sampler.getLoginQueue());
        loginReplyToQueue.setText(sampler.getReplyToQueue());
        marketInfoExchange.setText(sampler.getMarketInfoExchange());
        routingKey.setText(sampler.getRoutingKey());

        scheduledHour.setText(sampler.getScheduleHour());
        scheduledMinute.setText(sampler.getScheduleMinute());
        scheduledSecond.setText(sampler.getScheduleSecond());

    }

    @Override
    public void clearGui() {
        virtualHost.setText("/");
        timeout.setText(""+Trimegah.TIMEOUT);
        host.setText(Trimegah.HOST);
        port.setText(Trimegah.PORT);
        username.setText(Trimegah.USERNAME);
        password.setText(Trimegah.PASSWORD);
        SSL.setSelected(false);

        mobileAppVersion.setText(Trimegah.MOBILE_APP_VERSION);
        mobileDeviceId.setText(Trimegah.MOBILE_DEVICE_ID);
        mobilePassword.setText(Trimegah.MOBILE_PASSWORD);
        mobileDeviceType.setText(Trimegah.MOBILE_DEVICE_TYPE);
        mobileUserId.setText(Trimegah.MOBILE_USER_ID);

        loginQueue.setText(AbstractMarketInfo.DEFAULT_LOGIN_QUEUE);
        loginReplyToQueue.setText(AbstractMarketInfo.DEFAULT_REPLY_TO_QUEUE);
        marketInfoExchange.setText(AbstractMarketInfo.DEFAULT_MARKET_INFO_QUEUE);
        routingKey.setText("*");

        scheduledHour.setText("10");
        scheduledMinute.setText("0");
        scheduledSecond.setText("0");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractMarketInfo sampler = (AbstractMarketInfo) element;

        sampler.clear();

        configureTestElement(sampler);

        sampler.setVirtualHost(virtualHost.getText());

        sampler.setTimeout(timeout.getText());

        sampler.setHost(host.getText());
        sampler.setPort(port.getText());
        sampler.setUsername(username.getText());
        sampler.setPassword(password.getText());
        sampler.setConnectionSSL(SSL.isSelected());

        sampler.setMobileAppVersion(mobileAppVersion.getText());
        sampler.setMobileDeviceId(mobileDeviceId.getText());
        sampler.setMobilePassword(mobilePassword.getText());
        sampler.setMobileType(mobileDeviceType.getText());
        sampler.setMobileUserId(mobileUserId.getText());

        sampler.setLoginQueue(loginQueue.getText());
        sampler.setReplyToQueue(loginReplyToQueue.getText());
        sampler.setMarketInfoExchange(marketInfoExchange.getText());
        sampler.setRoutingKey(routingKey.getText());

        sampler.setScheduleHour(scheduledHour.getText());
        sampler.setScheduleMinute(scheduledMinute.getText());
        sampler.setScheduleSecond(scheduledSecond.getText());


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
        queueSettings.add(loginQueue, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(loginReplyToQueue, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        queueSettings.add(marketInfoExchange, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        queueSettings.add(routingKey, gridBagConstraints);

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
        JPanel mobilePanel = new VerticalPanel();
        mobilePanel.add(mobileSettings);
        commonPanel.add(mobilePanel, gridBagConstraintsCommon);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
        JPanel queuePanel = new VerticalPanel();
        queuePanel.add(queueSettings);
        commonPanel.add(queuePanel, gridBagConstraintsCommon);

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

        JPanel scheduleSetting = new JPanel(new GridBagLayout());
        scheduleSetting.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Market Info Schedule"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        scheduleSetting.add(scheduledHour, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        scheduleSetting.add(scheduledMinute, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        scheduleSetting.add(scheduledSecond, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 1;
        commonPanel.add(scheduleSetting, gridBagConstraintsCommon);

        return commonPanel;
    }
}
