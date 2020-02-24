
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 * Feel free to modify and rearrange code as you see fit
 */
public class DNSlookup {
    static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
    static final int MAX_PERMITTED_ARGUMENT_COUNT = 3;
    static final int PORT = 53;
    static final int TIMEOUT = 5000;
    static final int BUF_LENGTH = 1024;
    static boolean found = false;
    static boolean tracingOn = false;
    static boolean IPV6Query = false;
    static List<Integer> queryIDs = new ArrayList<>(); // List of query IDs
    /**
     * @param args
     */
    public static void main(String[] args) {
        int argCount = args.length;

        if (argCount < MIN_PERMITTED_ARGUMENT_COUNT || argCount > MAX_PERMITTED_ARGUMENT_COUNT) {
            DNSPrint.usage();
            return;
        }
        InetAddress rootNameServer;
        try {
            rootNameServer = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            DNSPrint.usage();
            return;
        }
        String fqdn = args[1];

        if (argCount == 3) {  // option provided
            if (args[2].equals("-t"))
                tracingOn = true;
            else if (args[2].equals("-6"))
                IPV6Query = true;
            else if (args[2].equals("-t6")) {
                tracingOn = true;
                IPV6Query = true;
            } else  { // option present but wasn't valid option
                DNSPrint.usage();
                return;
            }
        }

        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException e) {
            return;
        }
        resolve(socket, rootNameServer, fqdn);
        socket.close();
    }

    private static void resolve(DatagramSocket socket, InetAddress rootNameServer, String fqdn) {
        InetAddress address = rootNameServer;
        String name = fqdn;
        String previousName = fqdn;
        boolean currentQueryIPV6 = IPV6Query;
        int queryCount = 0;
        while (!found) {
            if (queryCount >= 30) {
                DNSPrint.printErrorResponse(name, -3, IPV6Query);
                break;
            }
            DNSResponse response = requestAndReceive(socket, name, address, currentQueryIPV6);
            ++queryCount;
            if (response == null) {
                DNSPrint.printErrorResponse(name, -2, IPV6Query);
                break;
            }
            
            if (tracingOn) {
                DNSPrint.printResponseTrace(response);
            }

            // If response is invalid
            if (!response.getValidResponse()) {
                if (response.getRCODE() == 3) {
                    DNSPrint.printErrorResponse(name, -1, IPV6Query);
                } else {
                    DNSPrint.printErrorResponse(name, -4, IPV6Query);
                }
                break;
            }

            // Reponse is authoritiative
            if (response.getAuthoritative()) {
                // No answers
                if (response.getAnswerCount() == 0) {
                    DNSPrint.printErrorResponse(name, -6, IPV6Query);
                    break;
                // No ns nor additional records and the answer is a CNAME
                } else if (response.getFirstAnswer().getTypeAsInt() == 5) {
                    address = rootNameServer;
                    name = response.getFirstAnswer().getTypeValue();
                    previousName = name;
                // If the current name does not equal the name being looked for
                } else if (!name.equals(previousName)) {
                    try {
                        address = InetAddress.getByName(response.getFirstAnswer().getTypeValue());
                    } catch (UnknownHostException e) {
                        // Shouldn't happen
                    }
                    name = previousName;
                    if (!currentQueryIPV6 && IPV6Query) {
                        currentQueryIPV6 = true;
                    }
                } else {
                    found = true;
                    DNSPrint.printResponse(response, fqdn);
                }
            // Response is not authoritative
            } else {
                // If there are not additional records
                if (response.getAdditionalCount() == 0) {
                    address = rootNameServer;
                    name = response.getFirstAnswer().getTypeValue();
                    currentQueryIPV6 = false;
                    continue;
                } else {
                    // Get the first additional record
                    for (int i = 0; i < response.getAdditionalCount(); i++) {
                        DNSAnswer answer = response.getAdditional().get(i);
                        if (answer.getTypeAsInt() == 1) {
                            try {
                                address = InetAddress.getByName(answer.getTypeValue());
                            } catch (UnknownHostException e) {
                                // Shouldn't happen
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private static DNSResponse requestAndReceive(DatagramSocket socket, String name, InetAddress rootNameServer, boolean currentQueryIPV6) {
        DNSRequest request = new DNSRequest(name, currentQueryIPV6, queryIDs);
        queryIDs.add(request.getQueryID());
        DatagramPacket sendPacket = new DatagramPacket(request.getQuery(), request.getQuery().length, rootNameServer, PORT);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            return null;
        }
        if (tracingOn) {
            DNSPrint.printQueryTrace(request, name, rootNameServer.getHostAddress(), currentQueryIPV6);
        }
        byte[] buf = new byte[BUF_LENGTH];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        // Receive the packet, repeat once if times out
        try {
            socket.receive(receivePacket);
        } catch (SocketTimeoutException ste) {
            try {
                socket.send(sendPacket);
            } catch (IOException ioe) {
                return null;
            }
            if (tracingOn) {
                DNSPrint.printQueryTrace(request, name, rootNameServer.getHostAddress(), currentQueryIPV6);
            }
            try {
                socket.receive(receivePacket);
            } catch (SocketTimeoutException ste2) {
                return null;
            } catch (IOException ioe) {
                return null;
            }
        } catch (IOException ioe) {
            return null;
        }
        // Create new byte array of only valid bits
        byte[] packet = new byte[receivePacket.getLength()];
        byte[] receiveBytes = receivePacket.getData();
        for (int i = 0; i < receivePacket.getLength(); i++) {
            packet[i] = receiveBytes[i];
        }
        return new DNSResponse(packet, receivePacket.getLength());
    }
}

