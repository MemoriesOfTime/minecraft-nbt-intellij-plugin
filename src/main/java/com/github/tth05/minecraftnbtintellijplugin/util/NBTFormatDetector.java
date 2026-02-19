package com.github.tth05.minecraftnbtintellijplugin.util;

import org.apache.commons.lang3.mutable.MutableInt;

import javax.swing.tree.DefaultMutableTreeNode;

public class NBTFormatDetector {

	public static class DetectionResult {
		public final DefaultMutableTreeNode root;
		public final boolean littleEndian;
		public final boolean network;
		public final boolean levelDat;
		public final boolean namelessRoot;
		public final int levelDatVersion;

		public DetectionResult(DefaultMutableTreeNode root, boolean littleEndian, boolean network,
		                       boolean levelDat, boolean namelessRoot, int levelDatVersion) {
			this.root = root;
			this.littleEndian = littleEndian;
			this.network = network;
			this.levelDat = levelDat;
			this.namelessRoot = namelessRoot;
			this.levelDatVersion = levelDatVersion;
		}
	}

	/**
	 * Attempts to detect the NBT format of the given bytes by trying various formats in priority order.
	 *
	 * @return DetectionResult if successful, null if no format matched
	 */
	public static DetectionResult detect(byte[] bytes) {
		if (bytes == null || bytes.length < 1)
			return null;

		// 1. Try level.dat (8-byte header: LE int version + LE int payload size)
		if (bytes.length >= 12) {
			int payloadSize = readLittleEndianInt(bytes, 4);
			if (payloadSize == bytes.length - 8 && payloadSize > 0) {
				DetectionResult result = tryLevelDat(bytes);
				if (result != null)
					return result;
			}
		}

		// 2. Try GZIP (check 0x1F 0x8B magic)
		if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B) {
			DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, false, false, null, false);
			if (root != null)
				return new DetectionResult(root, false, false, false, false, 0);
		}

		// 3. Try big-endian uncompressed (first byte should be a valid tag type 1-12)
		int firstByte = bytes[0] & 0xFF;
		if (firstByte >= 1 && firstByte <= 12) {
			DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, false, false, null, false);
			if (root != null)
				return new DetectionResult(root, false, false, false, false, 0);
		}

		// 4. Try little-endian
		{
			DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, true, false, null, false);
			if (root != null)
				return new DetectionResult(root, true, false, false, false, 0);
		}

		// 5. Try little-endian + nameless root
		{
			DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, true, false, null, true);
			if (root != null)
				return new DetectionResult(root, true, false, false, true, 0);
		}

		// 6. Try network
		{
			DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, false, true, null, false);
			if (root != null)
				return new DetectionResult(root, false, true, false, false, 0);
		}

		// 7. Try network + nameless root
		{
			DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, false, true, null, true);
			if (root != null)
				return new DetectionResult(root, false, true, false, true, 0);
		}

		return null;
	}

	private static DetectionResult tryLevelDat(byte[] bytes) {
		MutableInt version = new MutableInt();
		DefaultMutableTreeNode root = NBTFileUtil.loadNBTFromBytes(bytes, true, false, version, false);
		if (root != null)
			return new DetectionResult(root, true, false, true, false, version.getValue());
		return null;
	}

	private static int readLittleEndianInt(byte[] bytes, int offset) {
		return (bytes[offset] & 0xFF) |
				((bytes[offset + 1] & 0xFF) << 8) |
				((bytes[offset + 2] & 0xFF) << 16) |
				((bytes[offset + 3] & 0xFF) << 24);
	}
}
