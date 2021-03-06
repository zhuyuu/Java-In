/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.example.local;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.example.echo.EchoServerHandler;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class LocalExample {

    static final String PORT = System.getProperty("port", "test_port");

    public static void main(String[] args) throws Exception {
        // Address to bind on / connect to.
        LocalAddress socketAddress = new LocalAddress(PORT);

        // Create the bootstraps for both client and server side.
        ServerBootstrap sb = new ServerBootstrap(new DefaultLocalServerChannelFactory());
        ClientBootstrap cb = new ClientBootstrap(new DefaultLocalClientChannelFactory());

        try {
            // Set up the default server-side event pipeline.
            EchoServerHandler handler = new EchoServerHandler();
            sb.getPipeline().addLast("handler", handler);

            // Start up the server.
            sb.bind(socketAddress);

            // Set up the client-side pipeline factory.
            cb.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    return Channels.pipeline(
                            new StringDecoder(),
                            new StringEncoder(),
                            new LoggingHandler(InternalLogLevel.INFO));
                }
            });

            // Make the connection attempt to the server.
            ChannelFuture channelFuture = cb.connect(socketAddress);
            channelFuture.sync();

            // Read commands from the stdin.
            System.err.println("Enter text (quit to end)");
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null || "quit".equalsIgnoreCase(line)) {
                    break;
                }

                // Sends the received line to the server.
                lastWriteFuture = channelFuture.getChannel().write(line);
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
            channelFuture.getChannel().close();

            // Wait until the connection is closed or the connection attempt fails.
            channelFuture.getChannel().getCloseFuture().sync();
        } finally {
            // Release all resources used by the local transport.
            cb.releaseExternalResources();
            sb.releaseExternalResources();
        }
    }
}
