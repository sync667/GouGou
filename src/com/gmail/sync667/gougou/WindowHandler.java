package com.gmail.sync667.gougou;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowHandler implements WindowListener {

    private final GouGou gougou;

    public WindowHandler(GouGou gougou) {
        this.gougou = gougou;
        this.gougou.frame.addWindowListener(this);
    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        // GouGou.getClient().sendData(new Packet04Disconnect(this.entityId, "1").getData());
    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

}
