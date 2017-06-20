/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */
         
// MESSAGE SCALED_IMU2 PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.ardupilotmega.CRC;
import java.nio.ByteBuffer;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
* The RAW IMU readings for secondary 9DOF sensor setup. This message should contain the scaled values to the described units
*/
public class msg_scaled_imu2_test{

public static final int MAVLINK_MSG_ID_SCALED_IMU2 = 116;
public static final int MAVLINK_MSG_LENGTH = 22;
private static final long serialVersionUID = MAVLINK_MSG_ID_SCALED_IMU2;

private Parser parser = new Parser();

public CRC generateCRC(byte[] packet){
    CRC crc = new CRC();
    for (int i = 1; i < packet.length - 2; i++) {
        crc.update_checksum(packet[i] & 0xFF);
    }
    crc.finish_checksum(MAVLINK_MSG_ID_SCALED_IMU2);
    return crc;
}

public byte[] generateTestPacket(){
    ByteBuffer payload = ByteBuffer.allocate(6 + MAVLINK_MSG_LENGTH + 2);
    payload.put((byte)MAVLinkPacket.MAVLINK_STX); //stx
    payload.put((byte)MAVLINK_MSG_LENGTH); //len
    payload.put((byte)0); //seq
    payload.put((byte)255); //sysid
    payload.put((byte)190); //comp id
    payload.put((byte)MAVLINK_MSG_ID_SCALED_IMU2); //msg id
    payload.putInt((int)963497464); //time_boot_ms
    payload.putShort((short)17443); //xacc
    payload.putShort((short)17547); //yacc
    payload.putShort((short)17651); //zacc
    payload.putShort((short)17755); //xgyro
    payload.putShort((short)17859); //ygyro
    payload.putShort((short)17963); //zgyro
    payload.putShort((short)18067); //xmag
    payload.putShort((short)18171); //ymag
    payload.putShort((short)18275); //zmag
    
    CRC crc = generateCRC(payload.array());
    payload.put((byte)crc.getLSB());
    payload.put((byte)crc.getMSB());
    return payload.array();
}

@Test
public void test(){
    byte[] packet = generateTestPacket();
    for(int i = 0; i < packet.length - 1; i++){
        parser.mavlink_parse_char(packet[i] & 0xFF);
    }
    MAVLinkPacket m = parser.mavlink_parse_char(packet[packet.length - 1] & 0xFF);
    byte[] processedPacket = m.encodePacket();
    assertArrayEquals("msg_scaled_imu2", processedPacket, packet);
}
}
        