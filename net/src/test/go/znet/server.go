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
package znet

import (
	"context"
	"net"
	"sync"
)

// Server struct
type Server struct {
	onMessage    func(*Session, any)
	onConnect    func(*Session)
	onDisconnect func(*Session, error)
	sessions     *sync.Map
	address      string
	listener     net.Listener
}

// NewServer create a new socket service
func NewServer(addr string) *Server {
	listen, _ := net.Listen("tcp", addr)
	server := &Server{
		sessions: &sync.Map{},
		address:  addr,
		listener: listen,
	}
	return server
}


// Start Start socket service
func (s *Server) Start() {

	ctx, cancel := context.WithCancel(context.Background())

	defer func() {
		cancel()
		s.listener.Close()
	}()

	s.acceptHandler(ctx)
}

func (s *Server) acceptHandler(ctx context.Context) {
	for {
		conn, _ := s.listener.Accept()
		go s.connectHandler(ctx, conn)
	}
}

func (s *Server) connectHandler(ctx context.Context, c net.Conn) {
	var session = NewSession(c)
	s.sessions.Store(session.sid, session)

	connctx, cancel := context.WithCancel(ctx)

	defer func() {
		cancel()
		session.Close()
		s.sessions.Delete(session.sid)
	}()

	go session.readCoroutine(connctx)
	go session.writeCoroutine(connctx)

	if s.onConnect != nil {
		s.onConnect(session)
	}

	for {
		select {
		case err := <-session.done:

			if s.onDisconnect != nil {
				s.onDisconnect(session, err)
			}
			return

		case packet := <-session.messageCh:
			if s.onMessage != nil {
				s.onMessage(session, packet)
			}
		}
	}
}
