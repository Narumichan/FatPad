package com.narumi.Tools;

import com.narumi.FatPad;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.List;

public class DragDropListener implements DropTargetListener {

    FatPad owner = null;

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

                    // Get all of the dropped files
                    List files = (List) transferable.getTransferData(flavor);

                    // Loop them through

                    for(int i=0;i<files.size();++i)
                    {
                        int xd = i; //workaround jer i ne moze u inner class
                        String path = ((File) files.get(i)).getPath();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                new FatPad(path, xd*200);
                            }
                        });
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