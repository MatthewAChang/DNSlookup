import java.util.Random;
import java.util.List;

public class DNSRequest {
    private int queryID;
    private byte[] query;

    public DNSRequest(String name, boolean IPV6Query, List<Integer> queryIDs) {
        // Query ID
        String query = "";
        // Ensures a new query ID is selected
        while (true) {
            final String id = generateQueryId();
            int queryID = DNSUtils.hexStringToInteger(id);
            if (!queryIDs.contains(queryID)) {
                this.queryID = queryID;
                query += id;
                break;
            }
        }
        // QR, Opcode, AA, TC, RD
        query += "00";
        // RA, Z, RCODE
        query += "00";
        // QDCOUNT
        query += "0001";
        // ANCOUNT
        query += "0000";
        // NSCOUNT
        query += "0000";
        // ARCOUNT
        query += "0000";
        // QNAME
        query += generateQNAME(name);
        // QTYPE
        if (IPV6Query) {
            query += "001C";
        } else {
            query += "0001";
        }
        // QCLASS
        query += "0001";
        // System.out.println(query);
        this.query = DNSUtils.hexStringToByteArray(query);
    }

    public int getQueryID() {
        return this.queryID;
    }

    public byte[] getQuery() {
        return this.query;
    }

    private static String generateQueryId() {
        Random r = new Random();
        String id = Integer.toHexString(r.nextInt(65536));
        while (id.length() < 4) {
            id = "0" + id;
        }
        return id;
    }

    private static String generateQNAME(String name) {
        String[] labels = name.split("[.]");
        String ret = "";
        for (int i = 0; i < labels.length; i++) {
            String lengthOctet = Integer.toHexString(labels[i].length());
            while (lengthOctet.length() < 2) {
                lengthOctet = "0" + lengthOctet;
            }
            ret += lengthOctet;
            for (int j = 0; j < labels[i].length(); j++) {
                int ascii = (int)labels[i].charAt(j);
                ret += Integer.toHexString(ascii);
            }
        }
        ret += "00";
        return ret;
    }
}