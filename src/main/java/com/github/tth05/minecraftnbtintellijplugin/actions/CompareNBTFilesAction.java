package com.github.tth05.minecraftnbtintellijplugin.actions;

import com.github.tth05.minecraftnbtintellijplugin.NBTFileType;
import com.github.tth05.minecraftnbtintellijplugin.NBTTagTreeNode;
import com.github.tth05.minecraftnbtintellijplugin.snbt.SNbtSerializer;
import com.github.tth05.minecraftnbtintellijplugin.util.NBTFormatDetector;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class CompareNBTFilesAction extends AnAction {

	public CompareNBTFilesAction() {
		super("Compare NBT Files");
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
		if (files == null || files.length == 0 || files.length > 2) {
			e.getPresentation().setEnabledAndVisible(false);
			return;
		}
		for (VirtualFile file : files) {
			if (!isNBTFile(file)) {
				e.getPresentation().setEnabledAndVisible(false);
				return;
			}
		}
		e.getPresentation().setEnabledAndVisible(e.getProject() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null)
			return;

		VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
		if (files == null || files.length == 0)
			return;

		VirtualFile file1 = files[0];
		VirtualFile file2 = files.length >= 2 ? files[1] : chooseFile(project);
		if (file2 == null)
			return;

		showDiff(project, file1, file2);
	}

	public static void showDiff(@NotNull Project project, @NotNull VirtualFile file1, @NotNull VirtualFile file2) {
		String snbt1 = loadFileAsSnbt(file1);
		String snbt2 = loadFileAsSnbt(file2);
		if (snbt1 == null || snbt2 == null)
			return;

		DiffContentFactory contentFactory = DiffContentFactory.getInstance();
		DiffContent content1 = contentFactory.create(project, snbt1);
		DiffContent content2 = contentFactory.create(project, snbt2);

		SimpleDiffRequest request = new SimpleDiffRequest(
				"NBT Diff: " + file1.getName() + " vs " + file2.getName(),
				content1, content2,
				file1.getName(), file2.getName()
		);

		DiffManager.getInstance().showDiff(project, request);
	}

	@Nullable
	public static String loadFileAsSnbt(@NotNull VirtualFile file) {
		try {
			byte[] bytes = file.contentsToByteArray();
			NBTFormatDetector.DetectionResult result = NBTFormatDetector.detect(bytes);
			if (result == null)
				return null;
			return SNbtSerializer.serializePretty((NBTTagTreeNode) result.root);
		} catch (IOException e) {
			return null;
		}
	}

	@Nullable
	private static VirtualFile chooseFile(@NotNull Project project) {
		return FileChooser.chooseFile(
				FileChooserDescriptorFactory.createSingleFileDescriptor(),
				project, null
		);
	}

	private static boolean isNBTFile(@NotNull VirtualFile file) {
		if (file.isDirectory())
			return false;
		String ext = file.getExtension();
		return "nbt".equals(ext) || "dat".equals(ext) || file.getFileType() instanceof NBTFileType;
	}
}
