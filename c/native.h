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
#ifndef _JWIN32_
#define _JWIN32_
    #include "config.h"
    //Config post-processing
    #ifdef JWIN32_D3D11ON12
        #define JWIN32_D3D_11
        #define JWIN32_D3D_12
    #endif
    #ifdef JWIN32_D3D_9
        #define JWIN32_D3D
    #endif
    #ifdef JWIN32_D3D_10
        #define JWIN32_D3D
    #endif
    #ifdef JWIN32_D3D_11
        #define JWIN32_D3D
    #endif
    #ifdef JWIN32_D3D_12
        #define JWIN32_D3D
    #endif

    #include <Windows.h>

    //GUID support
    #include <guiddef.h>

    //DXGI
    #ifdef DXGI
        #include <dxgi.h>
    #endif

    //Direct3D
    #ifdef JWIN32_D3D
        #include "d3d/d3d.h"
        #ifdef JWIN32_D3D_9
            #include "d3d/d3d9.h"
        #endif
        #ifdef JWIN32_D3D_10
            #include "d3d/d3d10.h"
        #endif
        #ifdef JWIN32_D3D_11
            #include "d3d/d3d11.h"
        #endif
        #ifdef JWIN32_D3D_12
            #include "d3d/d3d12.h"
        #endif
        #ifdef JWIN32_D3D11ON12
            #include <d3d11on12.h>
        #endif
    #endif
#endif