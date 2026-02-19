package com.github.tth05.minecraftnbtintellijplugin.actions;

import com.github.tth05.minecraftnbtintellijplugin.NBTTagTreeNode;
import com.github.tth05.minecraftnbtintellijplugin.NBTTagType;
import com.github.tth05.minecraftnbtintellijplugin.editor.ui.NBTFileEditorUI;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NBTFileEditorPopupGroup extends ActionGroup {
	@NotNull
	@Override
	public AnAction[] getChildren(@Nullable AnActionEvent e) {
		if (e == null)
			return new AnAction[0];

		NBTFileEditorUI nbtFileEditorUI = e.getData(NBTFileEditorUI.DATA_KEY);
		if (nbtFileEditorUI != null) {
			if (nbtFileEditorUI.getTree().getSelectionModel().getSelectionCount() > 1)
				//Only delete on multi-select
				return new AnAction[] {new DeleteAction()};

			List<AnAction> actions = new ArrayList<>();
			NBTTagTreeNode node = (NBTTagTreeNode) nbtFileEditorUI.getTree().getLastSelectedPathComponent();
			NBTTagTreeNode parent = (NBTTagTreeNode) node.getParent();

			if (parent == null) {
				List<AnAction> rootActions = new ArrayList<>();
				rootActions.add(new RenameAction());
				rootActions.add(new Separator());
				rootActions.add(new AddChildAction());
				rootActions.add(new Separator());
				rootActions.add(new CopyAsSNbtAction());
				if (node.getType() == NBTTagType.COMPOUND || node.getType() == NBTTagType.LIST)
					rootActions.add(new PasteFromSNbtAction());
				return rootActions.toArray(new AnAction[0]);
			}

			if (parent.getType() != NBTTagType.BYTE_ARRAY &&
					parent.getType() != NBTTagType.INT_ARRAY &&
					parent.getType() != NBTTagType.LONG_ARRAY &&
					parent.getType() != NBTTagType.LIST) {
				actions.add(new RenameAction());
				actions.add(new ChangeTypeAction());
			}

			if (node.getType().hasValue())
				actions.add(new ChangeValueAction());

			actions.add(new Separator());

			if (node.getType().allowsChildren())
				actions.add(new AddChildAction());

			actions.add(new DeleteAction());

			actions.add(new Separator());
			actions.add(new CopyAsSNbtAction());
			if (node.getType() == NBTTagType.COMPOUND || node.getType() == NBTTagType.LIST)
				actions.add(new PasteFromSNbtAction());

			return actions.toArray(new AnAction[0]);
		}
		return new AnAction[0];
	}
}
