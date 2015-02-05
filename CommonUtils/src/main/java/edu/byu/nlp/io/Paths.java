/**
 * 
 */
package edu.byu.nlp.io;

import java.io.File;

import com.google.common.base.Preconditions;

import edu.byu.nlp.util.Pair;

/**
 * @author robbie
 *
 */
public class Paths {

	private Paths() {}
	
	// TODO(rhaertel) : write test cases
	public static Pair<String, String> splitExtension(String path) {
		int periodIndex = path.lastIndexOf('.');
		
		// No extension; Note that cases such as ".cshrc" are NOT considered extensions
		if (periodIndex <= 0) {
			return Pair.of(path, "");
		}
		
		String ext;
		if (periodIndex == path.length() - 1) {
			ext = "";
		} else {
			ext = path.substring(periodIndex + 1, path.length());
		}
		return Pair.of(path.substring(0, periodIndex), ext);
	}

	public static String baseName(String path) {
		return new File(path).getName();
	}

	public static String directory(String path) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(new File(path).getParent());
		return new File(new File(path).getParent()).getAbsolutePath();
	}
}
