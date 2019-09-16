package com.wws.mysqlclient.handler;

import com.wws.mysqlclient.enums.AttributeKeys;
import com.wws.mysqlclient.util.MysqlByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.Attribute;
import com.wws.mysqlclient.packet.connection.HandshakeV10Packet;
import com.wws.mysqlclient.packet.MysqlPacket;

import java.util.List;

/**
 * @author wws
 * @version 1.0.0
 * @date 2019-09-09 17:02
 **/
public class HandshakeDecoder extends MessageToMessageDecoder<MysqlPacket> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, MysqlPacket mysqlPacket, List<Object> list) {
        HandshakeV10Packet handshakeV10Packet = new HandshakeV10Packet();
        ByteBuf payload = mysqlPacket.getPayload();
        handshakeV10Packet.read(payload);

        list.add(handshakeV10Packet);

    }

}
