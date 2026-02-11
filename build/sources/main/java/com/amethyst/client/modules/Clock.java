package com.amethyst.client.modules;

import com.amethyst.client.Module;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Clock extends Module {
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    public Clock() {
        super("Clock", "Shows current time");
        this.setEnabled(true);
    }
    
    public String getText() {
        return timeFormat.format(new Date());
    }
}
