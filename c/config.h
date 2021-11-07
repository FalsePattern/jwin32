//This file is used for selecting the libraries you want to use.

//Windows.h
//
//The default defines have just enough functionality to allow for basic window management and input handling.
//Comment out the following line to include the full Windows.h header (slows down compilation).
//#define JWIN32_REDUCE_WINDOWS_H

#ifdef JWIN32_REDUCE_WINDOWS_H
    #define WIN32_LEAN_AND_MEAN
    #define VC_EXTRALEAN //Probably don't need this one, but better safe than sorry
    #define NOGDICAPMASKS
    #define NOVIRTUALKEYCODES
    #define NOWINMESSAGES
    #define NOWINSTYLES
    #define NOSYSMETRICS
    #define NOMENUS
    #define NOICONS
    #define NOKEYSTATES
    #define NOSYSCOMMANDS
    #define NORASTEROPS
    #define NOSHOWWINDOW
    #define OEMRESOURCE
    #define NOATOM
    #define NOCLIPBOARD
    #define NOCOLOR
    #define NOCTLMGR
    #define NODRAEXT
    #define NOGDI
    #define NOKERNEL
    #define NOUSER
    #define NONLS
    #define NOMB
    #define NOMEMMGR
    #define NOMETAFILE
    #define NOMINMAX
    #define NOMSG
    #define NOOPENFILE
    #define NOSCROLL
    #define NOSERVICE
    #define NOSOUND
    #define NOTEXTMETRIC
    #define NOWH
    #define NOWINOFFSETS
    #define NOCOMM
    #define NOKANJI
    #define NOHELP
    #define NOPROFILER
    #define NODEFERWINDOWPOS
    #define NOMCX
#endif



//Direct3D
//
//Uncomment to select your desired Direct3D version. You can select any combination of these.
//Inspect the headers inside the d3d folder if you want to know what each of them actually imports.
//NOTE: enabling d3d11on12 also force-enables d3d11 and d3d12.

#define JWIN32_D3D_9
#define JWIN32_D3D_10
#define JWIN32_D3D_11
#define JWIN32_D3D_12
#define JWIN32_D3D11ON12