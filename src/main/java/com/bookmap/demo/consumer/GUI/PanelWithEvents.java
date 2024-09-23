package com.bookmap.demo.consumer.GUI;

import com.bookmap.demo.consumer.providers.Provider;
import com.bookmap.addons.broadcasting.api.view.Event;
import velox.api.layer1.data.InstrumentInfo;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Panel displaying the events of the Absorption Indicator.
 */
public class PanelWithEvents extends JTextArea {

    private final InstrumentInfo instrumentInfo;

    public PanelWithEvents(InstrumentInfo instrumentInfo) {
        super(0,5);
        this.instrumentInfo = instrumentInfo;
        Border lineBorder = BorderFactory.createLineBorder(Color.WHITE);
        setBorder(lineBorder);
        Dimension dimension = new Dimension(350, 180);
        setPreferredSize(dimension);
        setEditable(false);
    }

    /**
     * Adds an event to the panel.
     */
    public void appendEvent(Event event, Provider provider) {
        SwingUtilities.invokeLater(() -> {
            if (event != null) {
                String[] text = provider.getValueHandler().getTextualVisualizationOfEvent(event,
                        instrumentInfo);

                for(String row : text){
                    setRows(getRows() + 1);
                    if (getRows() > 1) {
                        row = "\n" + row;
                    }
                    append(row);
                }
            }
        });
    }

    public void clear(){
        setText("");
        setCaretPosition(0);
        setRows(0);
    }
}
