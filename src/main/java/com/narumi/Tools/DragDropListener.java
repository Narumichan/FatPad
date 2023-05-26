package com.narumi.Tools;

import com.narumi.FatPad;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class DragDropListener implements DropTargetListener {

    FatPad owner;

    public DragDropListener(FatPad newOwner)
    {
        owner = newOwner;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent event) {

        // Accept copy drops
        event.acceptDrop(DnDConstants.ACTION_COPY);

        // Get the transfer which can provide the dropped item data
        Transferable transferable = event.getTransferable();

        // Get the data formats of the dropped item
        DataFlavor[] flavors = transferable.getTransferDataFlavors();

        // Loop through the flavors
        for (DataFlavor flavor : flavors) {

            try {

                // If the drop items are files  
                if (flavor.isFlavorJavaFileListType()){

                    // Get all the dropped files
                    List files = (List) transferable.getTransferData(flavor);

                    // Loop them through

                    for (Object file : files)
                    {
                        String path = ((File) file).getPath();
                        EventQueue.invokeLater(() -> owner.openFile(new File(path)));
                    }

                }
                else if(flavor.isFlavorTextType())
                {
                    System.out.println("cccc222");
                }

            } catch (Exception e) {

                // Print out the error stack
                e.printStackTrace();

            }
        }

        // Inform that the drop is complete
        event.dropComplete(true);

    }
}