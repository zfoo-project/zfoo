/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.session;

import com.zfoo.net.NetContext;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.ThreadUtils;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class SessionUtils {

    public static void printSessionInfo() {
        Thread thread = new Thread(() -> {
            while (true) {
                ThreadUtils.sleep(10_000);
                var builder = new StringBuilder();
                builder.append(StringUtils.format("clientSession count：[{}]", NetContext.getSessionManager().getClientSessionMap().size()));
                builder.append(FileUtils.LS);
                for (Session session : NetContext.getSessionManager().getClientSessionMap().values()) {
                    builder.append(StringUtils.format("[session:{}]", session.getChannel().remoteAddress()));
                    builder.append(FileUtils.LS);
                }

                builder.append(StringUtils.format("serverSession count：[{}]", NetContext.getSessionManager().getServerSessionMap().size()));
                builder.append(FileUtils.LS);
                for (Session session : NetContext.getSessionManager().getServerSessionMap().values()) {
                    builder.append(StringUtils.format("[session:{}]", session.getChannel().remoteAddress()));
                    builder.append(FileUtils.LS);
                }

                System.out.println(builder.toString());

            }
        });
        thread.start();
    }

}
