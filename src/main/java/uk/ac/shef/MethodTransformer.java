package uk.ac.shef;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.MethodInfo;

public class MethodTransformer {

  public static void transform(byte[] classfileBuffer, int startLine, int endLine,
      String directoryPath)
      throws IllegalClassFormatException, CannotCompileException, IOException, BadBytecode {

    ClassPool cp = ClassPool.getDefault();

    CtClass cc = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
    transformMethod(cp, cc, startLine, endLine);
    cc.writeFile(directoryPath + File.separator);
    cc.defrost();
  }

  private static void transformMethod(ClassPool cp, CtClass cc, int startLine, int endLine) throws BadBytecode {

    for (CtBehavior cb : cc.getDeclaredBehaviors()) {
      MethodInfo methodInfo = cb.getMethodInfo();

      // if there is not any code, do not instrument
      CodeAttribute ca = methodInfo.getCodeAttribute();
      if (ca == null) {
        continue;
      }

      if (removeLines(cc, cb, startLine, endLine)) {
        ca.setMaxStack(ca.computeMaxStack());
        methodInfo.rebuildStackMapIf6(cp, cc.getClassFile());

        break;
      }
    }
  }

  private static boolean removeLines(CtClass cc, CtBehavior m, int startLine, int endLine) {

    String methodLabel = m.getName() + m.getSignature();

    MethodInfo info = m.getMethodInfo();
    CodeAttribute ca = info.getCodeAttribute();

    // access the LineNumberAttribute
    LineNumberAttribute lineNumberAttribute =
        (LineNumberAttribute) ca.getAttribute(LineNumberAttribute.tag);

    // index in bytecode array where the instruction starts
    int startPc = lineNumberAttribute.toStartPc(startLine);

    // index in the bytecode array where the following instruction starts
    int endPc = lineNumberAttribute.toStartPc(endLine + 1);

    if (startPc == -1 || endPc == -1) {
      return false;
    }

    System.out.println("Modifying method " + methodLabel + " of class " + cc.getName() + " ("
        + startLine + ":" + startPc + " to " + endLine + ":" + endPc + ")");

    byte[] code = ca.getCode();
    for (int i = startPc; i < endPc; i++) {
      // change byte to a no operation code
      code[i] = CodeAttribute.NOP;
    }

    return true;
  }
}
