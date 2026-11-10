package com.cardesk.home;

import android.content.Context;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

/** One legacy ADB transport and one shell stream; restricted to this Android device. */
final class LocalAdbClient implements Closeable {
    private static final int CNXN = command("CNXN"), AUTH = command("AUTH"), OPEN = command("OPEN"),
        OKAY = command("OKAY"), WRTE = command("WRTE"), CLSE = command("CLSE");
    private static final int MAX_PAYLOAD = 256 * 1024;
    private final Socket socket = new Socket();
    private final Context context;
    private DataInputStream input;
    private OutputStream output;
    LocalAdbClient(Context context) { this.context = context.getApplicationContext(); }
    void connect(String host, int port, Consumer<String> state) throws Exception {
        InetAddress address = InetAddress.getByName(host);
        if (!(address.isLoopbackAddress() || NetworkInterface.getByInetAddress(address) != null))
            throw new IOException("仅允许连接本机ADB地址，不能连接其他设备");
        state.accept("正在连接本机ADB…");
        socket.connect(new InetSocketAddress(address, port), 5000); socket.setSoTimeout(60000); socket.setTcpNoDelay(true);
        input = new DataInputStream(socket.getInputStream()); output = socket.getOutputStream();
        AdbIdentity identity = new AdbIdentity(context);
        write(CNXN, 0x01000000, 4096, "host::cardesk\0".getBytes("UTF-8"));
        boolean signed = false, sentPublic = false;
        for (int count = 0; count < 8; count++) {
            Packet packet = read();
            if (packet.command == CNXN) { socket.setSoTimeout(15000); state.accept("ADB认证成功"); return; }
            if (packet.command != AUTH || packet.arg0 != 1) throw new IOException("非传统ADB协议；不支持配对码/TLS端口");
            if (!signed) { write(AUTH, 2, 0, identity.sign(packet.data)); signed = true; }
            else if (!sentPublic) {
                state.accept("等待调试授权：请在系统弹窗允许CarDesk密钥（最长60秒）");
                write(AUTH, 3, 0, identity.publicPayload()); sentPublic = true;
            } else throw new IOException("ADB认证未获允许，请重新连接并确认授权");
        }
        throw new IOException("ADB握手次数超限");
    }
    void shell(String script, Consumer<String> received) throws Exception {
        write(OPEN, 1, 0, ("shell:" + script + "\0").getBytes("UTF-8"));
        int remote = 0;
        boolean opened = false;
        while (!socket.isClosed()) {
            Packet packet = read();
            if (packet.arg1 != 1) throw new IOException("ADB流标识错误");
            if (packet.command == OKAY) {
                if (opened && packet.arg0 != remote) throw new IOException("ADB远端流变更");
                remote = packet.arg0; opened = true; socket.setSoTimeout(0);
            } else if (packet.command == WRTE && opened && packet.arg0 == remote) {
                received.accept(new String(packet.data, "UTF-8")); write(OKAY, 1, remote, new byte[0]);
            } else if (packet.command == CLSE) {
                write(CLSE, 1, packet.arg0, new byte[0]); return;
            } else throw new IOException("ADB流协议错误");
        }
    }
    private static int command(String value) {
        return value.charAt(0) | value.charAt(1) << 8 | value.charAt(2) << 16 | value.charAt(3) << 24;
    }
    private void write(int command, int arg0, int arg1, byte[] data) throws IOException {
        int checksum = 0; for (byte value : data) checksum += value & 255;
        byte[] header = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN).putInt(command).putInt(arg0).putInt(arg1)
            .putInt(data.length).putInt(checksum).putInt(command ^ -1).array();
        output.write(header); output.write(data); output.flush();
    }
    private Packet read() throws IOException {
        byte[] header = new byte[24]; input.readFully(header);
        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        Packet packet = new Packet(); packet.command = buffer.getInt(); packet.arg0 = buffer.getInt(); packet.arg1 = buffer.getInt();
        int size = buffer.getInt(), checksum = buffer.getInt(), magic = buffer.getInt();
        if (size < 0 || size > MAX_PAYLOAD || magic != (packet.command ^ -1)) throw new IOException("ADB报文头无效");
        packet.data = new byte[size]; input.readFully(packet.data);
        int sum = 0; for (byte value : packet.data) sum += value & 255;
        if (checksum != sum) throw new IOException("ADB报文校验失败");
        return packet;
    }
    @Override public void close() { try { socket.close(); } catch (IOException ignored) { } }
    private static final class Packet { int command, arg0, arg1; byte[] data; }
}
