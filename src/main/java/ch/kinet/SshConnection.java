/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an SSH tunnel. This class requires OpenSSH to be installed on the system and the ssh command be available in
 * the path.
 *
 * It probably only works on *NIX systems.
 */
public class SshConnection {

    private final ProcessBuilder processBuilder;
    private final List<String> command;
    private int localPort;
    private Process process;

    public SshConnection(String userName, String proxyServer) {
        this.command = new ArrayList<>();

        final StringBuilder sshProxy = new StringBuilder();
        sshProxy.append(userName);
        sshProxy.append("@");
        sshProxy.append(proxyServer);

        this.command.add("ssh");
        this.command.add(sshProxy.toString());
        this.processBuilder = new ProcessBuilder();
    }

    public void addTunnel(int localPort, String remoteServer, int remotePort) {
        this.localPort = localPort;
        final StringBuilder sshTunnel = new StringBuilder();
        sshTunnel.append(localPort);
        sshTunnel.append(":");
        sshTunnel.append(remoteServer);
        sshTunnel.append(":");
        sshTunnel.append(remotePort);
        command.add("-L");
        command.add(sshTunnel.toString());
    }

    public void connect() {
        System.out.println("SSH TUNNEL " + Util.concat(command, " "));
        processBuilder.command(command);
        if (process != null) {
            return;
        }

        try {
            process = processBuilder.start();
        }
        catch (IOException ex) {
            ex.printStackTrace(System.err);
            process = null;
        }

        // Wait for the tunnel to be open
        boolean ok = false;
        while (!ok) {
            try {
                final Socket socket = new Socket("localhost", localPort);
                socket.close();
                ok = true;
            }
            catch (Exception ex) {
                try {
                    Thread.sleep(100);
                }
                catch (final InterruptedException ex1) {
                }
            }
        }
    }

    public void disconnect() {
        if (process == null) {
            return;
        }

        process.destroy();
        process = null;
    }

    public boolean isConnected() {
        return process != null;
    }
}
