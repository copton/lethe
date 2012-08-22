package service;

import Ice.Current;
import IcePatch2.FileAccessException;
import IcePatch2.FileInfo;
import IcePatch2.PartitionOutOfRangeException;
import IcePatch2._FileServerDisp;

import java.util.*;
import java.io.*;
import java.security.*;

public class PatchServer extends _FileServerDisp {

	private String directory;
	private FileTreeRoot fileTree;
	private static final String checksumFile = "IcePatch2.sum";
	private static final String logFile = "IcePatch2.log";
	public PatchServer(String baseDirectory) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		this.directory = simplifyPath(baseDirectory);
		FileInfo [] fileInfo = loadFileInfo();
		fileTree = new FileTreeRoot(fileInfo);	
	}
	
	public FileInfo[] getFileInfoSeq(int partition, Current __current) throws PartitionOutOfRangeException {
		if(partition < 0 || partition > 255) throw new PartitionOutOfRangeException();
		
		return fileTree.nodes[partition].files;
	}

	public byte[][] getChecksumSeq(Current __current) {
		byte [][] checksums = new byte[256][0];
		for(int i=0; i<checksums.length; i++) 
			checksums[i] = fileTree.nodes[i].checksum;
		
		return checksums;
	}

	public byte[] getChecksum(Current __current) {
		return fileTree.checksum;
	}

	public byte[] getFileCompressed(String path, int pos, int num, Current __current) throws FileAccessException {
		if(path.startsWith(java.io.File.separator)) {
			FileAccessException e = new FileAccessException();
			e.reason = "illegal absolute path: "+path;
			throw e;
		}
	
		path = simplifyPath(path);
		if(path.indexOf("..") > 0) {
			FileAccessException e = new FileAccessException();
			e.reason = "illegal '..' component in path: "+path;
			throw e;		
		}
		
		if(num <= 0 || pos < 0) return new byte[0];
		
		try {
			BufferedInputStream stream = new BufferedInputStream(
					new FileInputStream(new File(directory, path+".bz2")));
			stream.skip(pos);
			byte [] bytes = new byte[num];
			int len = stream.read(bytes);
			if(len < bytes.length) {
				byte [] smaller = new byte[len];
				for(int i=0; i<smaller.length; i++) 
					smaller[i] = bytes[i];
				return smaller;
			} else return bytes;
		} catch(Exception ex) {
			FileAccessException e = new FileAccessException();
			e.reason = "file doesn't exist: "+path;
			throw e;
		}
	}

	private String simplifyPath(String path) {
		return path;
	}
	
	private FileInfo [] loadFileInfo() throws FileNotFoundException, IOException {
		FileInfoLessComparator comparator = new FileInfoLessComparator();
		SortedSet files = new TreeSet(comparator);
		String checksum = (new File(directory, checksumFile)).getAbsolutePath();
		
		BufferedReader reader = new BufferedReader(
				new FileReader(checksum));
		FileInfo info;
		while((info = readFileInfo(reader)) != null) 
			files.add(info);
	
		FileInfo [] fileInfo = (FileInfo[])files.toArray(new FileInfo[0]);
		String log = (new File(directory, logFile)).getAbsolutePath();
		try {
			reader = new BufferedReader(new FileReader(log));
		
			SortedSet remove = new TreeSet(comparator);
			SortedSet update = new TreeSet(comparator);
			while(true) {
				int read = reader.read();
				if(read < 0) break;
			
				info = readFileInfo(reader);
				if(read == '-') remove.add(info);
				else if(read == '+') update.add(info);
			}
		
			files.removeAll(remove);
			files.retainAll(update);
			
			fileInfo = (FileInfo[])files.toArray(new FileInfo[0]);
			saveFileInfoSeq(fileInfo);
		} catch(Exception e) {}
		
		return fileInfo;
	}
	
	private void saveFileInfoSeq(FileInfo [] fileInfo) throws IOException {
		String checksum = (new File(directory, checksumFile)).getAbsolutePath();
		BufferedWriter writer = new BufferedWriter(new FileWriter(checksum));
		for(int i=0; i<fileInfo.length; i++) {
			String line = fileInfo[i].path
				+"\t"+bytesToString(fileInfo[i].checksum)
				+"\t"+fileInfo[i].size
				+"\t"+(fileInfo[i].executable?1:0);
			writer.write(line+"\n");
		}
		(new File(directory, logFile)).delete();
	}
	
	private String bytesToString(byte [] bytes) {
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<bytes.length; i++)
			buf.append(toHexString(bytes[i]));
		
		return buf.toString();
	}
	
	private String toHexString(byte b) {
		String s="";
		int [] nums = {b/16, b%16};
		
		for(int i=0; i<nums.length; i++)
			if(nums[i] < 10) s+=Integer.toString(nums[i]);
			else s+= (char)('A'+nums[i]-10);
		
		return s;
	}
	
	private FileInfo readFileInfo(BufferedReader reader) {
		try {
			String line = reader.readLine();
			if(line == null)
				return null;
			String [] parts = line.split("\\t");
			
			FileInfo info = new FileInfo();
			info.path = parts[0];
			info.checksum = stringToBytes(parts[1]);
			info.size = Integer.parseInt(parts[2]);
			info.executable = Integer.parseInt(parts[3])!=0?true:false;
			
			return info;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private byte [] stringToBytes(String s) {
		byte [] bytes = new byte[(s.length()+1)/2];
		for(int i=0; i<bytes.length; i++) {
			String substring = s.substring(i*2, (i+1)*2);
			bytes[i] = (byte)Integer.parseInt(substring, 16);
		}
		return bytes;
	}
	
	private class FileTreeRoot {
		FileTreeNode [] nodes = new FileTreeNode[256];
		byte [] checksum = new byte[20];
		
		public FileTreeRoot(FileInfo [] files) throws NoSuchAlgorithmException {
		
			MessageDigest md = MessageDigest.getInstance("SHA1");
			
			byte [] allChecksums = new byte[256*20];
			for(int i=0, c0=0; i<256; i++) {
				FileTreeNode treeNode = new FileTreeNode();
				
				List fittingFiles = new ArrayList();
				for(int p=0; p<files.length; p++) {
					if(i == (0xFF&(int)files[p].checksum[0]))
						fittingFiles.add(files[p]);
				}
				treeNode.files = (FileInfo[])fittingFiles.toArray(new FileInfo[0]);
				byte [] allChecksums1 = new byte[treeNode.files.length*21];
				
				for(int p=0, c1=0; p<treeNode.files.length; p++) {
					for(int c=0; c<20; c++) 
						allChecksums1[c1++] = files[p].checksum[c];
					allChecksums[c1++] = (byte)(files[p].executable?1:0); // ??
				}
				
				if(allChecksums1.length != 0) {
					md.reset();
					treeNode.checksum = md.digest(allChecksums1);
				} else {
					for(int p=0; p<treeNode.checksum.length; p++) {
						treeNode.checksum[p] = 0;
					}
				}
				for(int c=0; c<treeNode.checksum.length; c++) 
					allChecksums[c0++] = treeNode.checksum[c]; 
			
				nodes[i] = treeNode;
			}
			
			if(allChecksums.length != 0) {
				md.reset();
				this.checksum = md.digest(allChecksums);
			}
		}
	}
	
	private class FileTreeNode {
		FileInfo [] files;
		byte [] checksum = new byte[20];
	}
	
	private class FileInfoLessComparator implements java.util.Comparator {

		public int compare(Object obj1, Object obj2) {
			FileInfo info1 = (FileInfo)obj1;
			FileInfo info2 = (FileInfo)obj2;
		
			int comp;
			comp = info1.path.compareTo(info2.path);
			if(comp != 0) return comp;
			
			comp = info2.size - info1.size;
			if(comp != 0) return comp;	
			
			for(int i=0; i<info1.checksum.length; i++) {
				comp = info2.checksum[i] - info1.checksum[i];
				if(comp != 0) return comp;	
			}
			
			return info2.executable?1:-1;
		}
		
	}
}
