package com.narumi.Tools;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ComboboxToolTipRenderer extends DefaultListCellRenderer { //https://stackoverflow.com/questions/480261/java-swing-mouseover-text-on-jcombobox-items#4480209
    List<String> tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);

        if (-1 < index && null != value && null != tooltips) {
            list.setToolTipText(tooltips.get(index));
        }
        return comp;
    }

    public void setTooltips(List<String> tooltips) {
        this.tooltips = tooltips;
    }
}