package com.wws.mysqlclient.packet.connection;

import com.wws.mysqlclient.config.MysqlConfig;
import com.wws.mysqlclient.enums.CapabilityFlags;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 握手响应协议
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse ">HandshakeResponse</a>
 *
 * @author wws
 * @version 1.0.0
 * @date 2019-09-09 19:22
 **/
@Data
public class HandshakeResponse41Packet {

    /**
     * 4
     */
    private int capabilityFlags;

    /**
     * 4
     */
    private int maxPacketSize;

    /**
     * 1
     */
    private byte charset;

    /**
     * 23
     */
    private byte[] reserved;

    /**
     * string[NUL]
     */
    private String username;

    /**
     * string[NUL]
     */
    private byte[] authResponse;

    /**
     * string[NUL]
     * capabilities & CLIENT_CONNECT_WITH_DB
     */
    private String database;

    /**
     * string[NUL]
     * capabilities & CLIENT_PLUGIN_AUTH
     */
    private String authPluginName;

    public static ByteBuf login(MysqlConfig config, HandshakeV10Packet handshakeV10Packet) throws NoSuchAlgorithmException {
        return write(getHandshakeResponsePacket(config, handshakeV10Packet));
    }

    private static ByteBuf write(HandshakeResponse41Packet handshakeResponse41Packet){
        ByteBuffer byteBuffer = ByteBuffer.allocate(handshakeResponse41Packet.length());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        byteBuf.writerIndex(0);
        byteBuf.writeIntLE(handshakeResponse41Packet.getCapabilityFlags());
        byteBuf.writeIntLE(handshakeResponse41Packet.getMaxPacketSize());
        byteBuf.writeByte(handshakeResponse41Packet.getCharset());
        byteBuf.writeBytes(handshakeResponse41Packet.getReserved());
        byteBuf.writeBytes(handshakeResponse41Packet.getUsername().getBytes());
        byteBuf.writeByte((byte)0);
        byteBuf.writeBytes(handshakeResponse41Packet.getAuthResponse());
        byteBuf.writeByte((byte)0);
        byteBuf.writeBytes(handshakeResponse41Packet.getDatabase().getBytes());
        byteBuf.writeByte((byte)0);
        byteBuf.writeBytes(handshakeResponse41Packet.getAuthPluginName().getBytes());
        byteBuf.writeByte((byte)0);
        return byteBuf;
    }

    private static HandshakeResponse41Packet getHandshakeResponsePacket(MysqlConfig config, HandshakeV10Packet handshakeV10Packet) throws NoSuchAlgorithmException {
        String username = config.getUsername();
        String password = config.getPassword();
        String database = config.getDatabase();

        HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet();
        int capabilityFlags = CapabilityFlags.CLIENT_LONG_PASSWORD
                | CapabilityFlags.CLIENT_FOUND_ROWS
                | CapabilityFlags.CLIENT_LONG_FLAG
                | CapabilityFlags.CLIENT_CONNECT_WITH_DB
                | CapabilityFlags.CLIENT_IGNORE_SPACE
                | CapabilityFlags.CLIENT_MULTI_STATEMENTS
                | CapabilityFlags.CLIENT_MULTI_RESULTS
                | CapabilityFlags.CLIENT_PS_MULTI_RESULTS
                | CapabilityFlags.CLIENT_PLUGIN_AUTH;

        byte[] authPluginDataPart1 = handshakeV10Packet.getAuthPluginDataPart1();
        byte[] authPluginDataPart2 = handshakeV10Packet.getAuthPluginDataPart2();
        byte[] seed = new byte[authPluginDataPart1.length + authPluginDataPart2.length];
        System.arraycopy(authPluginDataPart1, 0, seed, 0, authPluginDataPart1.length);
        System.arraycopy(authPluginDataPart2, 0, seed, authPluginDataPart1.length, authPluginDataPart2.length);

        handshakeResponse41Packet.setCapabilityFlags(capabilityFlags);
        handshakeResponse41Packet.setMaxPacketSize(-1);
        handshakeResponse41Packet.setCharset((byte)255);
        handshakeResponse41Packet.setReserved(new byte[23]);
        handshakeResponse41Packet.setUsername(username);
        handshakeResponse41Packet.setAuthResponse(cachingSHA2Password(password.getBytes(), seed));
        handshakeResponse41Packet.setDatabase(database);
        handshakeResponse41Packet.setAuthPluginName("caching_sha2_password");

        System.out.println(handshakeResponse41Packet);
        return handshakeResponse41Packet;
    }

    private static byte[] cachingSHA2Password(byte[] password, byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        messageDigest.update(password);
        byte[] dig1 = messageDigest.digest();
        messageDigest.reset();

        messageDigest.update(dig1);
        byte[] dig2 = messageDigest.digest();
        messageDigest.reset();

        messageDigest.update(dig2);
        messageDigest.update(seed);
        byte[] dig3 = messageDigest.digest();

        for(int i = 0; i < dig1.length; i++){
            dig1[i] ^= dig3[i];
        }
        return dig1;
    }

    private int length(){
        int len = 4 + 4 + 1 + 23;
        len += username.length() + 1;
        len += authResponse.length + 1;
        len += database.length() + 1;
        len += authPluginName.length() + 1;
        return len;
    }
}