package traffic_capture;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TrafficCapture {
    public static void main(String[] args) throws PcapNativeException, IOException {
        PcapNetworkInterface device = null;

        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        int snapshotLength = 65536;
        int readTimeout = 50;
        final PcapHandle handle;
        handle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);
        ServerSocket serverSocket = new ServerSocket(9999);
        Socket socket = serverSocket.accept();
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


        PacketListener listener = packet -> {
            out.println(packet.length() + " " + packet.get(IpV4Packet.class).getHeader().getSrcAddr() +
                    " " + packet.get(IpV4Packet.class).getHeader().getDstAddr());
        };


        while (true) {
            try {
                int maxPackets = 5;
                handle.loop(maxPackets, listener);
            } catch (InterruptedException | NotOpenException e) {
                e.printStackTrace();
            }
        }


    }

}
