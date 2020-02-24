# DNSlookup

DNSlookup is a Java utility for querying DNS nameservers. It is an implementation of Linux's dig utility. DNSlookup creates queries based on the user's input and on the answers received. Answers will be parsed for displaying and for creation of the next query.

Usage: java -jar DNSlookup.jar rootDNS name [-t6]<br />
rootDNS - the IP address (in dotted form) of the root DNS server you are to start your search at<br />
name    - fully qualified domain name to lookup<br />
-6      - return an IPV6 address<br />
-t      - trace the queries made and responses received<br />
-t6     - return and IPV6 address and trace all the queries made and responses received.<br />
