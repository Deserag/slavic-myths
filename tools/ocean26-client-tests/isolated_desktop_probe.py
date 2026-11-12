"""Launch the disposable Minecraft probe on a private Windows desktop.

Never calls SwitchDesktop or sends input to the user's active desktop.
"""
import ctypes
import os
import subprocess
import sys
from ctypes import wintypes

user32 = ctypes.WinDLL("user32", use_last_error=True)
kernel32 = ctypes.WinDLL("kernel32", use_last_error=True)

class STARTUPINFO(ctypes.Structure):
    _fields_ = [("cb", wintypes.DWORD), ("lpReserved", wintypes.LPWSTR),
                ("lpDesktop", wintypes.LPWSTR), ("lpTitle", wintypes.LPWSTR),
                ("dwX", wintypes.DWORD), ("dwY", wintypes.DWORD),
                ("dwXSize", wintypes.DWORD), ("dwYSize", wintypes.DWORD),
                ("dwXCountChars", wintypes.DWORD), ("dwYCountChars", wintypes.DWORD),
                ("dwFillAttribute", wintypes.DWORD), ("dwFlags", wintypes.DWORD),
                ("wShowWindow", wintypes.WORD), ("cbReserved2", wintypes.WORD),
                ("lpReserved2", ctypes.c_void_p), ("hStdInput", wintypes.HANDLE),
                ("hStdOutput", wintypes.HANDLE), ("hStdError", wintypes.HANDLE)]

class PROCESS_INFORMATION(ctypes.Structure):
    _fields_ = [("hProcess", wintypes.HANDLE), ("hThread", wintypes.HANDLE),
                ("dwProcessId", wintypes.DWORD), ("dwThreadId", wintypes.DWORD)]

user32.CreateDesktopW.argtypes = [wintypes.LPCWSTR, wintypes.LPCWSTR,
    ctypes.c_void_p, wintypes.DWORD, wintypes.DWORD, ctypes.c_void_p]
user32.CreateDesktopW.restype = wintypes.HANDLE
kernel32.CreateProcessW.argtypes = [wintypes.LPCWSTR, wintypes.LPWSTR,
    ctypes.c_void_p, ctypes.c_void_p, wintypes.BOOL, wintypes.DWORD,
    ctypes.c_void_p, wintypes.LPCWSTR, ctypes.POINTER(STARTUPINFO),
    ctypes.POINTER(PROCESS_INFORMATION)]
kernel32.CreateProcessW.restype = wintypes.BOOL
kernel32.WaitForSingleObject.argtypes = [wintypes.HANDLE, wintypes.DWORD]
kernel32.GetExitCodeProcess.argtypes = [wintypes.HANDLE, ctypes.POINTER(wintypes.DWORD)]

desktop = user32.CreateDesktopW("SlavicMythsShipQA", None, None, 0, 0x01FF, None)
if not desktop:
    raise ctypes.WinError(ctypes.get_last_error())
startup = STARTUPINFO()
startup.cb = ctypes.sizeof(startup)
startup.lpDesktop = "SlavicMythsShipQA"
process = PROCESS_INFORMATION()
world = sys.argv[1] if len(sys.argv) > 1 else "Ocean26IsolatedQA"
if not world.isalnum():
    raise ValueError("Probe world name must be alphanumeric")
allowed = {"main": "", "modular": "-Pocean26ModularVisualProbe",
           "visual": "-Pocean26VisualProbe",
           "modularvisual": "-Pocean26ModularVisualProbe -Pocean26VisualProbe",
           "longforward": "-Pocean26ModularVisualProbe -Pocean26LongForwardProbe",
           "nativelongforward": "-Pocean26LongForwardProbe",
           "save": "-Pocean26SaveMovingProbe", "reload": "-Pocean26ReloadProbe",
           "modularsave": "-Pocean26ModularVisualProbe -Pocean26SaveMovingProbe",
           "hold": "-Pocean26HoldProbe", "reverse": "-Pocean26ReverseProbe",
           "shipyard": "-Pocean26ShipyardSwitchProbe"}
mode = sys.argv[2] if len(sys.argv) > 2 else "main"
if mode not in allowed:
    raise ValueError("Unknown probe mode")
ship_class = sys.argv[3] if len(sys.argv) > 3 else "MEDIUM"
if ship_class not in {"SMALL", "MEDIUM", "LARGE", "LARGEST"}:
    raise ValueError("Unknown ship class")
command = ('cmd.exe /d /s /c "gradlew.bat runOcean26ClientProbe '
           f'-Pocean26ClientProbe {allowed[mode]} -Pocean26ProbeWorld={world} '
           f'-Pocean26ShipClass={ship_class} '
           '--offline > run-ocean26-client\\isolated-probe.log 2>&1"')
buffer = ctypes.create_unicode_buffer(command)
if not kernel32.CreateProcessW(None, buffer, None, None, False, 0,
                              None, os.getcwd(), ctypes.byref(startup),
                              ctypes.byref(process)):
    raise ctypes.WinError(ctypes.get_last_error())
print(f"private_desktop=SlavicMythsShipQA pid={process.dwProcessId}", flush=True)
kernel32.WaitForSingleObject(process.hProcess, 0xFFFFFFFF)
exit_code = wintypes.DWORD()
kernel32.GetExitCodeProcess(process.hProcess, ctypes.byref(exit_code))
print(f"probe_exit={exit_code.value}", flush=True)
marker = os.path.join(os.getcwd(), "run-ocean26-client", "ocean26-probe-result.txt")
result = open(marker, encoding="utf-8").read().strip() if os.path.isfile(marker) else "MISSING"
print(f"probe_result={result}", flush=True)
sys.exit(exit_code.value if exit_code.value else (0 if result.startswith("PASS ") else 1))
