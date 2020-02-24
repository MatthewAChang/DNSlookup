public class DNSUtils {
    // Converts a hex string to a byte array
    public static byte[] hexStringToByteArray(String hex) {
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // Converts a byte array to a hex string
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    // Converts a hex string to a binary string
    public static String hexStringToBinaryString(String hex) {
        String binary = Integer.toBinaryString(Integer.parseInt(hex, 16));
        while (binary.length() < hex.length() * 4) {
            binary = "0" + binary;
        }
        return binary;
    }

    // Converts a hex string to an integer
    public static int hexStringToInteger(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static int hexStringGetLength(String hex, int start) {
        int index = start;
        while (true) {
            // If finds two ending zeros
            if (hex.charAt(index) == '0' && hex.charAt(index + 1) == '0') {
                return index + 2 - start;
            // If finds a "c", it is a pointer
            } else if (Character.toLowerCase(hex.charAt(index)) == 'c') {
                return index + 4 - start;
            }
            int length = 2 * hexStringToInteger(hex.substring(index, index + 2));
            index += length + 2;
        }
    }

    // Converts a hex string into an IP address
    public static String hexStringToIPAddress(String hex) {
        String ret = "";
        for (int i = 0; i < hex.length(); i += 2) {
            // Every two hexadecimals are a number
            ret += hexStringToInteger(hex.substring(i, i + 2));
            if (i + 2 < hex.length()) {
                ret += ".";
            }
        }
        return ret;
    }

    // Converts a hex string into an IPV4 address
    public static String hexStringToAddress(String hex, int start) {
        int index = start;
        String ret = "";
        while (true) {
            // If finds two ending zeros
            if (hex.charAt(index) == '0' && hex.charAt(index + 1) == '0') {
                break;
            // If finds a "c", it is a pointer
            } else if (Character.toLowerCase(hex.charAt(index)) == 'c') {
                int pointer = 2 * hexStringToInteger(hex.substring(index + 1, index + 4));
                // Go to pointer location and parse
                ret += hexStringToAddress(hex, pointer);
                break;
            }
            int length = 2 * hexStringToInteger(hex.substring(index, index + 2));
            index += 2;
            for (int j = 0; j < length; j += 2) {
                char character = (char)hexStringToInteger(hex.substring(index + j, index + j + 2));
                ret += character;
            }
            ret += ".";
            index += length;
        }
        // Remove trailing .
        if (ret.charAt(ret.length() - 1) == '.') {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    // Converts a hex string into an IPV6 address
    public static String hexStringToAddressAAAA(String hex) {
        String ret = "";
        int index = 0;
        while (index < hex.length()) {
            // Remove leading zeros
            String temp = Long.toHexString(Long.parseLong(hex.substring(index, index + 4), 16));
            ret += temp.isEmpty() ? "0" : temp;
            if (index + 4 < hex.length()) {
                ret += ":";
            }
            index += 4;
        }
        return ret;
    }
}