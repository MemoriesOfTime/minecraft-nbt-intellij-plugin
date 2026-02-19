package com.github.tth05.minecraftnbtintellijplugin.actions;

import com.github.tth05.minecraftnbtintellijplugin.NBTTagTreeNode;
import com.github.tth05.minecraftnbtintellijplugin.editor.ui.NBTFileEditorUI;
import com.github.tth05.minecraftnbtintellijplugin.snbt.SNbtSerializer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class CopyAsSNbtAction extends AnAction {

	public CopyAsSNbtAction() {
		super("Copy as SNBT", "Copy this node as SNBT text", AllIcons.Actions.Copy);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		NBTFileEditorUI nbtFileEditorUI = e.getData(NBTFileEditorUI.DATA_KEY);
		if (nbtFileEditorUI == null)
			return;

		NBTTagTreeNode node = (NBTTagTreeNode) nbtFileEditorUI.getTree().getLastSelectedPathComponent();
		if (node == null)
			return;

		String snbt = SNbtSerializer.serialize(node);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(snbt), null);
	}
}
