/*
 * Erstellt am: 5 Nov 2019 21:15:25
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

//Import all needed packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @see https://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
 * 
 * @author Jonas Michel
 *
 */
public class ZipUtils {

	public static void zipIt(String sourceFolder, File targetZip) {
		List<String> fileList = new LinkedList<>();
		generateFileList(sourceFolder, fileList, new File(sourceFolder));
		byte[] buffer = new byte[1024];
		String source = new File(sourceFolder).getName();
		try (FileOutputStream fos = new FileOutputStream(targetZip); ZipOutputStream zos = new ZipOutputStream(fos);) {

			for (String file : fileList) {
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try (FileInputStream in = new FileInputStream(sourceFolder + File.separator + file)) {
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				}
			}

			zos.closeEntry();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void generateFileList(String sourceFolder, List<String> fileList, File node) {
		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(sourceFolder, node.toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(sourceFolder, fileList, new File(node, filename));
			}
		}
	}

	private static String generateZipEntry(String sourceFolder, String file) {
		return file.substring(sourceFolder.length() + 1, file.length());
	}
}