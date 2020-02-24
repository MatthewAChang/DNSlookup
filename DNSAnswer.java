public class DNSAnswer {
    private String name;
    private int aType;
    private int ttl;
    private int dataLength;
    private String typeValue;
    private int length;
    
    public DNSAnswer(String hex, int offset) {
        this.name = DNSUtils.hexStringToAddress(hex, offset);
        int length = DNSUtils.hexStringGetLength(hex, offset);
        offset += length;
        this.aType = DNSUtils.hexStringToInteger(hex.substring(offset, offset + 4));
        this.ttl = DNSUtils.hexStringToInteger(hex.substring(offset + 8, offset + 16));
        this.dataLength = DNSUtils.hexStringToInteger(hex.substring(offset + 16, offset + 20));
        // Parse data depending on type
        if (this.aType == 1) {
            this.typeValue = DNSUtils.hexStringToIPAddress(hex.substring(offset + 20, offset + 20 + (2 * dataLength)));
        } else if (this.aType == 2 || this.aType == 5) {
            this.typeValue = DNSUtils.hexStringToAddress(hex, offset + 20);
        } else if (this.aType == 28) {
            this.typeValue = DNSUtils.hexStringToAddressAAAA(hex.substring(offset + 20, offset + 20 + (2 * dataLength)));
        }
        this.length = length + 20 + (2 * dataLength);
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        switch(this.aType) {
            case 1: return "A";
            case 2: return "NS";
            case 5: return "CN";
            case 28: return "AAAA";
            default: return Integer.toString(this.aType);
        }
    }

    public int getTypeAsInt() {
        return this.aType;
    }

    public int getTTL() {
        return this.ttl;
    }

    public String getTypeValue() {
        return this.typeValue;
    }

    public int getLength() {
        return this.length;
    }
}