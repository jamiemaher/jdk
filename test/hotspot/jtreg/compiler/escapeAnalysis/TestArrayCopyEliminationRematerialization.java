/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @bug 8370416
 * @summary Ensure that rematerialization loads for a scalarized arraycopy destination use the correct control and memory state.
 * @library /test/lib /
 * @run driver ${test.main.class}
 */

package compiler.escapeAnalysis;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Random;

import compiler.lib.ir_framework.*;
import jdk.test.lib.Asserts;
import jdk.test.lib.Utils;


public class TestArrayCopyEliminationRematerialization {
    private static final Random RANDOM = Utils.getRandomInstance();
    private static final int SRC_SIZE = 100;
    private static final int COPY_LEN = 12;
    private static final int ORIG_VAL = 1;
    private static final int WRITE_IDX = 1;
    private static final VarHandle INT_ARR = MethodHandles.arrayElementVarHandle(int[].class);

    private static short storeVal = 0x7357;
    private static int[] srcI = new int[SRC_SIZE];
    private static long[] srcL = new long[SRC_SIZE];
    private static int[] otherI = new int[60];
    static {for (int i = 0; i < otherI.length; i++) {
        otherI[i] = RANDOM.nextInt(ORIG_VAL + 1, Integer.MAX_VALUE);
    }}

    public static void main(String[] args) {
        TestFramework t = new TestFramework();
        t.start();
    }

    //@Run(test = "testSwitch")
    //@Warmup(20000)
    //static void runSwitch(RunInfo info) {
    //    int[] src = new int[SRC_SIZE];
    //    Arrays.fill(src, ORIG_VAL);
    //    int res = testSwitch(src);
    //    Asserts.assertEQ(ORIG_VAL, res, "Wrong Result from " + info.getTest().getName());
    //}

    //@Test
    //@IR(counts = { IRNode.LOAD_I, "=19" })
    //static int testSwitch(int[] src) {
    //    int[] dst = new int[10];
    //    System.arraycopy(src, 0, dst, 0, COPY_LEN);

    //    for (int i = 0; i < 4; i++) {
    //        for (int j = 0; j < 8; j++) {
    //            switch (i) {
    //                case -1 -> { /*nop*/ }
    //                case 0 -> src[1] = 0xcafe;
    //            }
    //        }
    //    }

    //    return dst[1];
    //}

//    @Run(test = "testStore")
//    static void runStore(RunInfo info) {
//        Arrays.fill(srcI, ORIG_VAL);
//        int res = testStore(srcI, info.isWarmUp());
//        Asserts.assertEQ(ORIG_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//    }
//
//    @Test
//    @IR(counts = { IRNode.LOAD_I, "=19" })
//    static int testStore(int[] src, boolean flag) {
//        int[] dst = new int[COPY_LEN];
//        System.arraycopy(src, 10, dst, 0, COPY_LEN);
//
//        src[11] = storeVal;
//
//        if (flag) {
//            src[0] = 0xabcd;
//        }
//
//        return dst[1];
//    }
//
//    @Run(test = "testStoreL")
//    static void runStoreL(RunInfo info) {
//        Arrays.fill(srcL, ORIG_VAL);
//        long res = testStoreL(srcL, info.isWarmUp());
//        Asserts.assertEQ((long) ORIG_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//    }
//
//    @Test
//    @IR(counts = { IRNode.LOAD_L, "=19" })
//    static long testStoreL(long[] src, boolean flag) {
//        long[] dst = new long[COPY_LEN];
//        System.arraycopy(src, 10, dst, 0, COPY_LEN);
//
//        src[11] = storeVal;
//
//        if (flag) {
//            src[0] = 0xabcd;
//        }
//
//        return dst[1];
//    }
//
//    @Run(test = "testStoreLoop")
//    static void runStoreLoop(RunInfo info) {
//        Arrays.fill(srcI, ORIG_VAL);
//        int res = testStoreLoop(srcI, info.isWarmUp());
//        Asserts.assertEQ(ORIG_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//    }
//
//    @Test
//    @IR(counts = { IRNode.LOAD_I, "=19" })
//    static int testStoreLoop(int[] src, boolean flag) {
//        int[] dst = new int[10];
//        System.arraycopy(src, 10, dst, 0, COPY_LEN);
//
//        src[11] = 0xcafe;
//        for (int i = 0; i < 1234; i++) {
//            if (flag && i >= 0) {
//                src[0] = 0xabcd;
//            }
//        }
//
//        return dst[1];
//    }

    @Run(test = "testArrayCopy")
    static void runArrayCopy(RunInfo info) {
        Arrays.fill(srcI, ORIG_VAL);
        int res = testArrayCopy(srcI, info.isWarmUp());
        Asserts.assertEQ(ORIG_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
    }

    @Test
    @IR(counts = { IRNode.LOAD_I, "=27" })
    static int testArrayCopy(int[] src, boolean flag) {
        int[] dst = new int[COPY_LEN];
        System.arraycopy(src, 0, dst, 0, COPY_LEN);

        System.arraycopy(otherI, 3, src, WRITE_IDX, 11);

        if (flag) {
            src[0] = 0xabcd;
        }

        return dst[1];
    }

//    @Run(test = "testGetAndSet")
//    static void runGetAndSet(RunInfo info) {
//        Arrays.fill(srcI, ORIG_VAL);
//        int res = testGetAndSet(srcI, info.isWarmUp());
//        Asserts.assertEQ(ORIG_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//    }
//
//    @Test
//    @IR(counts = { IRNode.LOAD_I, "=19" })
//    static int testGetAndSet(int[] src, boolean flag) {
//        int[] dst = new int[COPY_LEN];
//        System.arraycopy(src, 10, dst, 0, COPY_LEN);
//
//        INT_ARR.getAndSet(src, 11, 0xcafe);
//
//        if (flag) {
//            src[0] = 0xabcd;
//        }
//
//        return dst[1];
//    }
//
//    @Run(test = "testCAS")
//    static void runCAS(RunInfo info) {
//        Arrays.fill(srcI, ORIG_VAL);
//        int res = testCAS(srcI, info.isWarmUp());
//        Asserts.assertEQ(ORIG_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//    }
//
//    @Test
//    @IR(counts = { IRNode.LOAD_I, "=19" })
//    static int testCAS(int[] src, boolean flag) {
//        int[] dst = new int[COPY_LEN];
//        System.arraycopy(src, 10, dst, 0, COPY_LEN);
//
//        INT_ARR.compareAndSet(src, 11, ORIG_VAL, 0xcafe);
//
//        if (flag) {
//            src[0] = 0xabcd;
//        }
//
//        return dst[1];
//    }

}

//private static final int SRC_SIZE = 847;
//private static final int COPY_LEN = 36;
//private static final int COPY_IDX = 786;
//private static final int WRITE_IDX = 789;
//private static final int RETURN_IDX = 3;
//private static final byte SRC_VAL = (byte)1;
//private static final byte WRITE_VAL = (byte)31;
//private static final double[] srcD = new double[SRC_SIZE];
//private static final double[] otherD = new double[41];
//private static final VarHandle DOUBLE_ARR = MethodHandles.arrayElementVarHandle(double[].class);
//private static final float[] srcF = new float[SRC_SIZE];
//private static final float[] otherF = new float[10];
//private static final VarHandle FLOAT_ARR = MethodHandles.arrayElementVarHandle(float[].class);
//private static final long[] srcL = new long[SRC_SIZE];
//private static final long[] otherL = new long[2];
//private static final VarHandle LONG_ARR = MethodHandles.arrayElementVarHandle(long[].class);
//private static final int[] srcI = new int[SRC_SIZE];
//private static final int[] otherI = new int[32];
//private static final VarHandle INT_ARR = MethodHandles.arrayElementVarHandle(int[].class);
//private static final short[] srcS = new short[SRC_SIZE];
//private static final short[] otherS = new short[11];
//private static final VarHandle SHORT_ARR = MethodHandles.arrayElementVarHandle(short[].class);
//
//@Run(test = "testGetAndSetL")
//static void runGetAndSetL(RunInfo info) {
//    Arrays.fill(srcL, SRC_VAL);
//    long res = testGetAndSetL(srcL, info.isWarmUp());
//    Asserts.assertEQ((long) SRC_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//}
//@Test
//@IR(counts = { IRNode.LOAD_L, "=25" })
//static long testGetAndSetL(long[] src, boolean flag) {
//    long[] dst = new long[COPY_LEN];
//    System.arraycopy(src, COPY_IDX, dst, 0, COPY_LEN);
//LONG_ARR.getAndSet(src, WRITE_IDX, WRITE_VAL);
//if (flag) {
//    src[0] = WRITE_VAL;
//}
//    return dst[RETURN_IDX];
//}
//@Run(test = "testCASL")
//static void runCASL(RunInfo info) {
//    Arrays.fill(srcL, SRC_VAL);
//    long res = testCASL(srcL, info.isWarmUp());
//    Asserts.assertEQ((long) SRC_VAL, res, "Wrong result from " + info.getTest().getName() + " with flag " + info.isWarmUp());
//}
//
//@Test
//@IR(counts = { IRNode.LOAD_L, "=25" })
//static long testCASL(long[] src, boolean flag) {
//    long[] dst = new long[COPY_LEN];
//    System.arraycopy(src, COPY_IDX, dst, 0, COPY_LEN);
//LONG_ARR.compareAndSet(src, WRITE_IDX, SRC_VAL, WRITE_VAL);
//if (flag) {
//    src[0] = WRITE_VAL;
//}
//    return dst[RETURN_IDX];
//}
//}
