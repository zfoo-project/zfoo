/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package cmder

import (
	"bytes"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"runtime"
	"strings"
	"time"
	"unsafe"
)

var cmdArg [2]string

func init() {
	if runtime.GOOS == "windows" {
		cmdArg[0] = "cmd"
		cmdArg[1] = "/c"
	} else {
		cmdArg[0] = "/bin/sh"
		cmdArg[1] = "-c"
	}
}

// Run exec cmd and catch the result.
// Waits for the given command to finish with a timeout.
// If the command times out, it attempts to kill the process.
func Run(cmdLine string, timeout ...time.Duration) *Result {
	cmd := exec.Command(cmdArg[0], cmdArg[1], cmdLine)
	var ret = new(Result)
	cmd.Stdout = &ret.buf
	cmd.Stderr = &ret.buf
	cmd.Env = os.Environ()
	ret.err = cmd.Start()
	if ret.err != nil {
		return ret
	}
	if len(timeout) == 0 || timeout[0] <= 0 {
		ret.err = cmd.Wait()
		return ret
	}
	timer := time.NewTimer(timeout[0])
	done := make(chan error)
	go func() { done <- cmd.Wait() }()
	select {
	case ret.err = <-done:
		timer.Stop()
	case <-timer.C:
		if err := cmd.Process.Kill(); err != nil {
			ret.err = fmt.Errorf("command timed out and killing process fail: %s", err.Error())
		} else {
			// wait for the command to return after killing it
			<-done
			ret.err = errors.New("command timed out")
		}
	}
	return ret
}

// Result cmd exec result
type Result struct {
	buf bytes.Buffer
	err error
	str *string
}

// Err returns the error log.
func (r *Result) Err() error {
	if r.err == nil {
		return nil
	}
	r.err = errors.New(r.String())
	return r.err
}

// String returns the exec log.
func (r *Result) String() string {
	if r.str == nil {
		b := bytes.TrimSpace(r.buf.Bytes())
		if r.err != nil {
			b = append(b, ' ', '(')
			b = append(b, r.err.Error()...)
			b = append(b, ')')
		}
		r.str = (*string)(unsafe.Pointer(&b))
	}
	return *r.str
}


// IsWin determine whether the system is windows
func IsWin() bool {
	return runtime.GOOS == "windows"
}

// IsMac determines whether the system is darwin
func IsMac() bool {
	return runtime.GOOS == "darwin"
}

// IsLinux determines whether the system is linux
func IsLinux() bool {
	return runtime.GOOS == "linux"
}

// IsSupportColor check current console whether support color.
// Supported: linux, mac, or windows's ConEmu, Cmder, putty, git-bash.exe
// Not support: windows cmd.exe, powerShell.exe
func IsSupportColor() bool {
	// Support color: "TERM=xterm" "TERM=xterm-vt220" "TERM=xterm-256color" "TERM=screen-256color"
	// Don't support color: "TERM=cygwin"
	envTerm := os.Getenv("TERM")
	if strings.Contains(envTerm, "xterm") || strings.Contains(envTerm, "screen") {
		return true
	}

	// like on ConEmu software, e.g "ConEmuANSI=ON"
	if os.Getenv("ConEmuANSI") == "ON" {
		return true
	}

	// like on ConEmu software, e.g "ANSICON=189x2000 (189x43)"
	if os.Getenv("ANSICON") != "" {
		return true
	}

	return false
}

// IsSupport256Color check current console whether support 256 color.
func IsSupport256Color() bool {
	// "TERM=xterm-256color" "TERM=screen-256color"
	return strings.Contains(os.Getenv("TERM"), "256color")
}

// IsSupportTrueColor check current console whether support true color
func IsSupportTrueColor() bool {
	// "COLORTERM=truecolor"
	return strings.Contains(os.Getenv("COLORTERM"), "truecolor")
}
