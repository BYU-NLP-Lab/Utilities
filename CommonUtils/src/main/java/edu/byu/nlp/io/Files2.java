/**
 * 
 */
package edu.byu.nlp.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

/**
 * @author rah67
 * 
 */
public class Files2 {

  private Files2() {}

  // TODO: rename
  public static Iterable<String> open(Reader rdr) {
    return new LineReaderIterable(rdr);
  }

  public static Iterable<String> open(File file) throws FileNotFoundException {
    return open(file, Charset.defaultCharset());
  }

  public static Iterable<String> open(String filename) throws FileNotFoundException {
    return open(filename, Charset.defaultCharset());
  }

  public static Iterable<String> open(String filename, Charset charset)
      throws FileNotFoundException {
    return open(new File(filename), charset);
  }

  public static Iterable<String> open(File file, Charset charset) throws FileNotFoundException {
    return open(Files.newReader(file, charset));
  }

  public static Iterable<String> open(FileObject file, Charset charset) throws FileSystemException {
    return open(new InputStreamReader(file.getContent().getInputStream(), charset));
  }

  public static Iterable<String> open(Class<?> clz, String resource) {
    return open(new InputStreamReader(clz.getResourceAsStream(resource)));
  }

  public static void writeLines(Iterable<? extends CharSequence> it, String fn) throws IOException {
    writeLines(it, new File(fn));
  }

  public static void writeLines(Iterable<? extends CharSequence> it, File file) throws IOException {
    Writers.writeLines(new FileWriter(file), it);
  }

  public static void writeLines(Iterable<? extends CharSequence> it, String fn, Charset charset)
      throws IOException {
    writeLines(it, new File(fn), charset);
  }

  public static void writeLines(Iterable<? extends CharSequence> it, File file, Charset charset)
      throws FileNotFoundException {
    Writers.writeLines(new OutputStreamWriter(new FileOutputStream(file), charset), it);
  }
  
  public static String toString(FileObject file, Charset charset) throws IOException {
    InputStream in = new BufferedInputStream(file.getContent().getInputStream());
    return new String(ByteStreams.toByteArray(in), charset);
  }
}