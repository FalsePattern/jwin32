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