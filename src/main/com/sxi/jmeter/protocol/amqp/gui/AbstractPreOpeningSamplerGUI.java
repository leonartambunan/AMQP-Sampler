
package com.sxi.jmeter.protocol.amqp.gui;

import com.sxi.jmeter.protocol.amqp.AbstractPreOpeningSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractPreOpeningSamplerGUI extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;
    protected JLabeledTextField virtualHost = new JLabeledTextField("Virtual Host");
    protected JLabeledTextField host = new JLabeledTextField("Host");
    protected JLabeledTextField port = new JLabeledTextField("Port");
    protected JLabeledTextField timeout = new JLabeledTextField("Timeout");
    protected JLabeledTextField username = new JLabeledTextField("Username");
    protected JLabeledTextField password = new JLabeledTextField("Password");
    private final JCheckBox SSL = new JCheckBox("SSL", false);
    protected JLabeledTextField mobileUserId = new JLabeledTextField("Mobile Userid");
    protected JLabeledTextField mobilePassword = new JLabeledTextField("Mobile Password");
    protected JLabeledTextField mobileDeviceId = new JLabeledTextField("Mobile Deviceid");
    protected JLabeledTextField mobileDeviceType = new JLabeledTextField("Mobile Device Type");
    protected JLabeledTextField mobileAppVersion = new JLabeledTextField("Mobile App Ver.");

    protected JLabeledTextField serverQueue = new JLabeledTextField("Server Queue");
    protected JLabeledTextField replyToQueue = new JLabeledTextField("Reply-To Queue");
    protected JLabeledTextField orderResponseQueue = new JLabeledTextField("Order Response Queue");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");

    protected JLabeledTextField stockId = new JLabeledTextField("Stock ID");
    protected JLabeledTextField stockAmount = new JLabeledTextField("Amount");

    protected JLabeledTextField scheduledHour = new JLabeledTextField("Hour");
    protected JLabeledTextField scheduledMinute = new JLabeledTextField("Minute");
    protected JLabeledTextField scheduledSecond = new JLabeledTextField("Second");

    protected abstract void setMainPanel(JPanel panel);

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AbstractPreOpeningSampler)) return;
        AbstractPreOpeningSampler sampler = (AbstractPreOpeningSampler) element;

        virtualHost.setText(sampler.getVirtualHost());

        timeout.setText(sampler.getTimeout());

        host.setText(sampler.getHost());
        port.setText(sampler.getPort());
        username.setText(sampler.getUsername());
        password.setText(sampler.getPassword());
        SSL.setSelected(sampler.connectionSSL());

        mobileAppVersion.setText(sampler.getMobileAppVersion());
        mobileDeviceId.setText(sampler.getMobileDeviceid());
        mobilePassword.setText(sampler.getMobilePassword());
        mobileDeviceType.setText(sampler.getMobileType());
        mobileUserId.setText(sampler.getMobileUserid());

        serverQueue.setText(sampler.getServerQueue());
        replyToQueue.setText(sampler.getReplytoQueue());
        orderResponseQueue.setText(sampler.getOrderResponseQueue());
        routingKey.setText(sampler.getRoutingKey());
        stockId.setText(sampler.getStockId());
        stockAmount.setText(sampler.getStockAmount());

        scheduledHour.setText(sampler.getScheduleHour());
        scheduledMinute.setText(sampler.getScheduleMinute());
        scheduledSecond.setText(sampler.getScheduleSecond());
    }

    @Override
    public void clearGui() {
        virtualHost.setText("/");
        timeout.setText(AbstractPreOpeningSampler.DEFAULT_TIMEOUT_STRING);
        host.setText("localhost");
        port.setText(AbstractPreOpeningSampler.DEFAULT_PORT_STRING);
        username.setText("guest");
        password.setText("guest");
        SSL.setSelected(false);

        mobileAppVersion.setText("3.0");
        mobileDeviceId.setText("5f6d41cd63257be");
        mobilePassword.setText("pass");
        mobileDeviceType.setText("ANDROID");
        mobileUserId.setText("test");

        serverQueue.setText(AbstractPreOpeningSampler.DEFAULT_SERVER_QUEUE);
        replyToQueue.setText(AbstractPreOpeningSampler.DEFAULT_REPLYTO_QUEUE);
        orderResponseQueue.setText(AbstractPreOpeningSampler.DEFAULT_ORDER_RESPONSE_QUEUE);
        routingKey.setText("");

        stockId.setText("");
        stockAmount.setText("");

        scheduledHour.setText("10");
        scheduledMinute.setText("0");
        scheduledSecond.setText("0");

    }

    @Override
    public void modifyTestElement(TestElement element) {

        AbstractPreOpeningSampler sampler = (AbstractPreOpeningSampler) element;

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
        sampler.setMobileDeviceid(mobileDeviceId.getText());
        sampler.setMobilePassword(mobilePassword.getText());
        sampler.setMobileType(mobileDeviceType.getText());
        sampler.setMobileUserid(mobileUserId.getText());

        sampler.setServerQueue(serverQueue.getText());
        sampler.setReplytoQueue(replyToQueue.getText());
        sampler.setOrderResponseQueue(orderResponseQueue.getText());
        sampler.setRoutingKey(routingKey.getText());

        sampler.setStockId(stockId.getText());
        sampler.setStockAmount(stockAmount.getText());

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
        gridBagConstraintsCommon.fill = GridBagConstraints.VERTICAL;
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

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        queueSettings.add(orderResponseQueue, gridBagConstraints);

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


        JPanel orderSettings = new JPanel(new GridBagLayout());
        orderSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Order"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        orderSettings.add(stockId, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        orderSettings.add(stockAmount, gridBagConstraints);


        JPanel queuePanel = new VerticalPanel();
        queuePanel.add(queueSettings);
        queuePanel.add(orderSettings);
        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 1;
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
