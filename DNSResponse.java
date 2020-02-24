import java.util.ArrayList;
import java.util.List;


// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has a bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion.  Feel free to add or delete methods or instance variables to best suit your implementation.

public class DNSResponse {
    private int queryID;                  // this is for the response it must match the one in the request 
    private boolean validResponse = true; // Was this response successfully decoded
    private int answerCount = 0;          // number of answers  
    private int nsCount = 0;              // number of nscount response records
    private int additionalCount = 0;      // number of additional (alternate) response records
    private boolean authoritative = false;// Is this an authoritative record
    private int rcode = 0;
    private List<DNSAnswer> answers = new ArrayList<>(); // List of answers
    

    public DNSResponse (byte[] data, int len) {
        len *= 2;
        // Convert byte array to hex string
        final String hex = DNSUtils.byteArrayToHexString(data);
        if (!parseHeader(hex)) {
            this.validResponse = false;
            return;
        }

        int offset = 0;
        // Header length is 24 until NAME
        int i = 24;
        while (i < len) {
            if (hex.charAt(i) == '0' && hex.charAt(i + 1) == '0') {
                offset = i;
                break;
            }
            int length = DNSUtils.hexStringToInteger(hex.substring(i, i + 2));
            i += (2 * length) + 2;
        }
        offset += 10;
        // Extract list of answers, name server, and additional information response records
        while (offset < len) {
            DNSAnswer answer = new DNSAnswer(hex, offset);
            answers.add(answer);
            offset += answer.getLength();
        }
    }

    // Returns false if header contains errors
    private boolean parseHeader(String hex) {
        // Extract the query ID
        this.queryID = DNSUtils.hexStringToInteger(hex.substring(0, 4));
        // QR, Opcode, AA, TC, RD
        String code1 = DNSUtils.hexStringToBinaryString(hex.substring(4, 6));
        // Determine if a query response
        if (code1.charAt(0) != '1') {
            return false;
        }
        // Determine if an authoritative response or not
        if (code1.charAt(5) == '1') {
            this.authoritative = true;
        }
        // RCODE
        this.rcode = DNSUtils.hexStringToInteger(hex.substring(7, 8));
        if (this.rcode != 0) {
            return false;
        }
        // Determine answer count
        this.answerCount = DNSUtils.hexStringToInteger(hex.substring(12, 16));
        // Determine NS Count
        this.nsCount = DNSUtils.hexStringToInteger(hex.substring(16, 20));
        // Determine additional record count
        this.additionalCount = DNSUtils.hexStringToInteger(hex.substring(20, 24));
        return true;
    }

    public int getQueryID() {
        return this.queryID;
    }

    public boolean getValidResponse() {
        return this.validResponse;
    }

    public int getAnswerCount() {
        return this.answerCount;
    }

    public int getNSCount() {
        return this.nsCount;
    }

    public int getAdditionalCount() {
        return this.additionalCount;
    }

    public boolean getAuthoritative() {
        return this.authoritative;
    }

    public int getRCODE() {
        return this.rcode;
    }

    public DNSAnswer getFirstAnswer() {
        return this.answers.get(0);
    }

    public List<DNSAnswer> getAnswers() {
        return this.answers.subList(0, answerCount);
    }

    public List<DNSAnswer> getNSResponses() {
        return this.answers.subList(answerCount, answerCount + nsCount);
    }

    public List<DNSAnswer> getAdditional() {
        return this.answers.subList(answerCount + nsCount, answerCount + nsCount + additionalCount);
    }
}
