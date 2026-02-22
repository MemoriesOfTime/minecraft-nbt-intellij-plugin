package com.github.tth05.minecraftnbtintellijplugin.diff;

import com.github.tth05.minecraftnbtintellijplugin.NBTFileType;
import com.github.tth05.minecraftnbtintellijplugin.actions.CompareNBTFilesAction;
import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.FileContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.tools.simple.SimpleDiffViewer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NBTDiffTool implements FrameDiffTool {

	@NotNull
	@Override
	public String getName() {
		return "NBT Diff";
	}

	@Override
	public boolean canShow(@NotNull DiffContext context, @NotNull DiffRequest request) {
		if (!(request instanceof ContentDiffRequest))
			return false;
		List<DiffContent> contents = ((ContentDiffRequest) request).getContents();
		if (contents.size() != 2)
			return false;
		for (DiffContent content : contents) {
			if (!(content instanceof FileContent))
				return false;
			if (!isNBTFile(((FileContent) content).getFile()))
				return false;
		}
		return true;
	}

	@NotNull
	@Override
	public DiffViewer createComponent(@NotNull DiffContext context, @NotNull DiffRequest request) {
		ContentDiffRequest contentRequest = (ContentDiffRequest) request;
		List<DiffContent> contents = contentRequest.getContents();

		VirtualFile file1 = ((FileContent) contents.get(0)).getFile();
		VirtualFile file2 = ((FileContent) contents.get(1)).getFile();

		String snbt1 = CompareNBTFilesAction.loadFileAsSnbt(file1);
		String snbt2 = CompareNBTFilesAction.loadFileAsSnbt(file2);
		if (snbt1 == null) snbt1 = "";
		if (snbt2 == null) snbt2 = "";

		DiffContentFactory factory = DiffContentFactory.getInstance();
		DiffContent textContent1 = factory.create(snbt1);
		DiffContent textContent2 = factory.create(snbt2);

		List<String> titles = contentRequest.getContentTitles();
		SimpleDiffRequest textRequest = new SimpleDiffRequest(
				contentRequest.getTitle(),
				textContent1, textContent2,
				titles.get(0), titles.get(1)
		);

		return new SimpleDiffViewer(context, textRequest);
	}

	private static boolean isNBTFile(@NotNull VirtualFile file) {
		if (file.isDirectory())
			return false;
		String ext = file.getExtension();
		return "nbt".equals(ext) || "dat".equals(ext) || file.getFileType() instanceof NBTFileType;
	}
}
