package org.etsit.uma.androidrsa.server.util.rsa;

import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

@SuppressWarnings({"rawtypes", "unchecked"})
public class X500PrincipalHelper {
	public static int LEASTSIGNIFICANT = 0;
	public static int MOSTSIGNIFICANT = 1;

	public final static String attrCN = "CN";
	public final static String attrOU = "OU";
	public final static String attrO = "O";
	public final static String attrC = "C";
	public final static String attrL = "L";
	public final static String attrST = "ST";
	public final static String attrSTREET = "STREET";
	public final static String attrEMAIL = "EMAILADDRESS";
	public final static String attrUID = "UID";

	ArrayList rdnNameArray = new ArrayList();

	private final static String attrTerminator = "=";

	public X500PrincipalHelper() {
	}

	public X500PrincipalHelper(X500Principal principal) {
		parseDN(principal.getName(X500Principal.RFC2253));
	}

	public void setPrincipal(X500Principal principal) {
		parseDN(principal.getName(X500Principal.RFC2253));
	}

	public String getCN() {
		return findPart(attrCN);
	}

	public String getOU() {
		return findPart(attrOU);

	}

	public String getO() {
		return findPart(attrO);

	}

	public String getC() {
		return findPart(attrC);
	}

	public String getL() {
		return findPart(attrL);
	}

	public String getST() {
		return findPart(attrST);
	}

	public String getSTREET() {
		return findPart(attrSTREET);
	}

	public String getEMAILDDRESS() {
		return findPart(attrEMAIL);
	}

	public String getUID() {
		return findPart(attrUID);
	}

	private void parseDN(String dn) throws IllegalArgumentException {
		int startIndex = 0;
		char c = '\0';
		ArrayList nameValues = new ArrayList();

		// Clear the existing array, in case this instance is being re-used
		rdnNameArray.clear();

		while (startIndex < dn.length()) {
			int endIndex;
			for (endIndex = startIndex; endIndex < dn.length(); endIndex++) {
				c = dn.charAt(endIndex);
				if (c == ',' || c == '+')
					break;
				if (c == '\\') {
					endIndex++; // skip the escaped char
				}
			}

			if (endIndex > dn.length())
				throw new IllegalArgumentException("unterminated escape " + dn);

			nameValues.add(dn.substring(startIndex, endIndex));

			if (c != '+') {
				rdnNameArray.add(nameValues);
				if (endIndex != dn.length())
					nameValues = new ArrayList();
				else
					nameValues = null;
			}
			startIndex = endIndex + 1;
		}
		if (nameValues != null) {
			throw new IllegalArgumentException("improperly terminated DN " + dn);
		}
	}

	public ArrayList getAllValues(String attributeID) {
		ArrayList retList = new ArrayList();
		String searchPart = attributeID + attrTerminator;

		for (Iterator iter = rdnNameArray.iterator(); iter.hasNext();) {
			ArrayList nameList = (ArrayList) iter.next();
			String namePart = (String) nameList.get(0);

			if (namePart.startsWith(searchPart)) {
				// Return the string starting after the ID string and the = sign
				// that follows it.
				retList.add(namePart.toString().substring(searchPart.length()));
			}
		}

		return retList;

	}

	private String findPart(String attributeID) {
		return findSignificantPart(attributeID, MOSTSIGNIFICANT);
	}

	private String findSignificantPart(String attributeID, int significance) {
		String retNamePart = null;
		String searchPart = attributeID + attrTerminator;

		for (Iterator iter = rdnNameArray.iterator(); iter.hasNext();) {
			ArrayList nameList = (ArrayList) iter.next();
			String namePart = (String) nameList.get(0);

			if (namePart.startsWith(searchPart)) {
				// Return the string starting after the ID string and the = sign
				// that follows it.
				retNamePart = namePart.toString().substring(searchPart.length());
				// By definition the first one is most significant
				if (significance == MOSTSIGNIFICANT)
					break;
			}
		}

		return retNamePart;
	}

}