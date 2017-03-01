package uk.ac.shef;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;

public class Main {

  private static ClassPool classPool = ClassPool.getDefault();

  private static final String USAGE =
      "Usage: java -jar uk.ac.shef.remove-bytecode-instructions-0.0.1.jar"
          + " <.jar or .class file>" + " <class canonical name>" + " <start line>"
          + " <remove line> <directory to save the updated .class file>";

  public static void main(String[] args) throws Exception {

    if (args.length != 5) {
      System.err.println(USAGE);
      System.exit(1);
    }

    String jar_or_class = args[0];
    File jar_or_class_file = new File(jar_or_class);
    if (!jar_or_class.endsWith(".jar") && !jar_or_class.endsWith(".class")
        || !jar_or_class_file.exists() || !jar_or_class_file.canRead()) {
      System.err.println(USAGE);
      System.exit(1);
    }

    String className = args[1];
    Integer startLine = Integer.valueOf(args[2]);
    Integer endLine = Integer.valueOf(args[3]);
    String directoryPath = args[4];

    byte[] classByteArray = null;

    if (jar_or_class.endsWith(".jar")) {
      JarFile jarFile = new JarFile(jar_or_class_file);

      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String entryName = entry.getName();

        if (entryName.endsWith(".class")) {
          InputStream in = jarFile.getInputStream(entry);

          CtClass cc = classPool.makeClass(in);
          if (cc.getName().equals(className)) {
            classByteArray = Utils.getByteArray(entry.getSize(), jarFile.getInputStream(entry));
            break;
          }

          in.close();
        }
      }

      jarFile.close();
    } else if (jar_or_class.endsWith(".class")) {
      classByteArray = Utils.getByteArray(jar_or_class.length(), new FileInputStream(jar_or_class));
    }
    assert classByteArray != null;

    MethodTransformer.transform(classByteArray, startLine, endLine, directoryPath);
    System.out.println("DONE!");
    System.exit(0);
  }
}
