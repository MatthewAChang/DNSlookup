import java.util.List;

public class DNSPrint {
    // Print the query trace
    public static void printQueryTrace(DNSRequest request, String fqdn, String address, boolean IPV6Query) {
        String type = IPV6Query ? "AAAA": "A";
        System.out.println("\n\nQuery ID     " + request.getQueryID() + " " + fqdn + "  " + type + " --> " + address);
    }

    // Print the response trace
    public static void printResponseTrace(DNSResponse response) {
        String authoritative = response.getAuthoritative() ? "true" : "false";
        System.out.println("Response ID: " + response.getQueryID() + " Authoritative = " + authoritative);

        printGroup(response.getAnswers(), response.getAnswerCount(), "Answers");
        printGroup(response.getNSResponses(), response.getNSCount(), "Nameservers");
        printGroup(response.getAdditional(), response.getAdditionalCount(), "Additional Information");
    }

    private static void printGroup(List<DNSAnswer> answers, int size, String title) {
        System.out.println("  " + title + " (" + size + ")");
        for (int i = 0; i < size; i++) {
            DNSAnswer answer = answers.get(i);
            System.out.printf("       %-31s%-11d%-5s%s\n", answer.getName(), answer.getTTL(), answer.getType(), answer.getTypeValue());
        }
    }

    // Print the response
    public static void printResponse(DNSResponse response, String name) {
        List<DNSAnswer> answers = response.getAnswers();
        int answersCount = response.getAnswerCount();
        for (int i = 0; i < answersCount; i++) {
            DNSAnswer answer = answers.get(i);
            System.out.println(name + " " + answer.getTTL() + "    " + answer.getType() + " " + answer.getTypeValue());
        }
    }

    // Print the response
    public static void printErrorResponse(String name, int errorCode, boolean IPV6Query) {
        String type = IPV6Query ? "AAAA": "A";
        System.out.println(name + " " + errorCode + "    " + type + " 0.0.0.0");
    }

    public static void usage() {
        System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-t6]");
        System.out.println("where");
        System.out.println("    rootDNS - the IP address (in dotted form) of the root");
        System.out.println("              DNS server you are to start your search at");
        System.out.println("    name    - fully qualified domain name to lookup");
        System.out.println("    -6      - return an IPV6 address");
        System.out.println("    -t      - trace the queries made and responses received");
        System.out.println("    -t6     - return and IPV6 address and trace all the queries made and responses received.");
    }
}