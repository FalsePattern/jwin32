/*
 * Copyright (c) 2021 FalsePattern
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.falsepattern.jwin32;

import java.util.function.BiConsumer;

public class Macros {

    public static int HIWORD(long value) {
        return (int) (value >> 16);
    }

    public static int LOWORD(long value) {
        return (int) (value & 0xFFFF);
    }

    public static int GET_X_LPARAM(long lParam) {
        return (short)LOWORD(lParam);
    }

    public static int GET_Y_LPARAM(long lParam) {
        return (short)HIWORD(lParam);
    }

    public static int GET_XBUTTON_WPARAM(long wParam) {
        return HIWORD(wParam);
    }

    public static void MAKEPOINTS(long lParam, int[] buffer) {
        if (buffer.length < 2) {
            throw new IllegalArgumentException("buffer must be at least 2 elements long");
        }
        buffer[0] = GET_X_LPARAM(lParam);
        buffer[1] = GET_Y_LPARAM(lParam);
    }

    public static void MAKEPOINTS(long lParam, BiConsumer<Integer, Integer> consumer) {
        consumer.accept(GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
    }

    public static int MAKELANGID(int primary, int sub) {
        return (sub << 10) | primary;
    }
}
