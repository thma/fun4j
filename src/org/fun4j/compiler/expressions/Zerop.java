package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Predicate that returns true if a number equals 0.
 * SUpports Integers and BigIntegers
 */
public class Zerop extends Expression {

	Expression e;

	public Zerop(final Expression e) {
		this.e = e;
		returnType = Boolean.class;
	}

	public String toString() {
		return "(zero? " + e + ")";
	}

	public void compile(final MethodVisitor mv) {
		writeLineInfo(mv);
		Label ifFalse = new Label();
		Label end = new Label();

		// compile argument
		e.compile(mv);
		if (useBigIntArithmetics()) {
			mv.visitTypeInsn(CHECKCAST, "java/math/BigInteger");
		} else {
			mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue",
					"()I");
		}

		if (useBigIntArithmetics()) {
			mv.visitFieldInsn(GETSTATIC, "java/math/BigInteger", "ZERO",
					"Ljava/math/BigInteger;");
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigInteger", "equals",
					"(Ljava/lang/Object;)Z");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
					"(Z)Ljava/lang/Boolean;");
			// mv.visitInsn(ARETURN);
		} else {
			mv.visitJumpInsn(IFNE, ifFalse);
			mv.visitInsn(ICONST_1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
					"(Z)Ljava/lang/Boolean;");
			mv.visitJumpInsn(GOTO, end);

			mv.visitLabel(ifFalse);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
					"(Z)Ljava/lang/Boolean;");

			mv.visitLabel(end);
		}

	}
}