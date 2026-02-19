package com.github.tth05.minecraftnbtintellijplugin.actions;

import com.github.tth05.minecraftnbtintellijplugin.NBTTagTreeNode;
import com.github.tth05.minecraftnbtintellijplugin.NBTTagType;
import com.github.tth05.minecraftnbtintellijplugin.editor.ui.NBTFileEditorUI;
import com.github.tth05.minecraftnbtintellijplugin.snbt.SNbtParseException;
import com.github.tth05.minecraftnbtintellijplugin.snbt.SNbtParser;
import com.github.tth05.minecraftnbtintellijplugin.util.NBTFileUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class PasteFromSNbtAction extends AnAction {

	public PasteFromSNbtAction() {
		super("Paste from SNBT", "Add a child node from SNBT text", AllIcons.Actions.MenuPaste);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		NBTFileEditorUI nbtFileEditorUI = e.getData(NBTFileEditorUI.DATA_KEY);
		if (nbtFileEditorUI == null)
			return;

		NBTTagTreeNode selectedNode = (NBTTagTreeNode) nbtFileEditorUI.getTree().getLastSelectedPathComponent();
		if (selectedNode == null)
			return;

		String snbtInput = Messages.showInputDialog(
				e.getProject(),
				selectedNode.getType() == NBTTagType.COMPOUND
						? "Enter SNBT (key:value format, e.g. Health:20s):"
						: "Enter SNBT value (e.g. {id:\"stone\"}):",
				"Paste from SNBT",
				null
		);

		if (snbtInput == null || snbtInput.trim().isEmpty())
			return;

		try {
			NBTTagTreeNode child;
			if (selectedNode.getType() == NBTTagType.COMPOUND) {
				child = new SNbtParser(snbtInput.trim()).parseNamedValue();
			} else {
				// List or array: parse as value, set index as name
				child = new SNbtParser(snbtInput.trim()).parse();
				child.setName("[" + selectedNode.getChildCount() + "]");
			}

			((DefaultTreeModel) nbtFileEditorUI.getTree().getModel()).insertNodeInto(
					child, selectedNode, selectedNode.getChildCount());
			nbtFileEditorUI.getTree().expandPath(new TreePath(selectedNode.getPath()));
			NBTFileUtil.saveTree(e);
		} catch (SNbtParseException ex) {
			Messages.showErrorDialog(e.getProject(), ex.getMessage(), "SNBT Parse Error");
		}
	}
}
