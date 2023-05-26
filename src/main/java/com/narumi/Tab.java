package com.narumi;

public interface Tab
{
    boolean isSaved();
    void setSaved(boolean saved);

    int continueWithoutSavingDialog(String fileName);
    int closeTab();
    void removeTab();
    void saveFile();
    void saveFileAs();
}
