// SPDX-License-Identifier: BSD-3-Clause
// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IPv6 Address Record - maps a domain name to an IPv6 address
 *
 * @author Brian Wellington
 * @see <a href="https://tools.ietf.org/html/rfc3596">RFC 3596: DNS Extensions to Support IP Version
 *     6</a>
 */
public class AAAARecord extends Record {
  private byte[] address;

  AAAARecord() {}

  /**
   * Creates an AAAA record from the given address.
   *
   * @param address The address that the name refers to.
   */
  public AAAARecord(Name name, int dclass, long ttl, InetAddress address) {
    super(name, Type.AAAA, dclass, ttl);
    if (Address.familyOf(address) != Address.IPv4 && Address.familyOf(address) != Address.IPv6) {
      throw new IllegalArgumentException("invalid IPv4/IPv6 address");
    }
    this.address = address.getAddress();
  }

  /**
   * Creates an AAAA record from the given data.
   *
   * @param address The address that the name refers to
   * @since 3.6
   */
  public AAAARecord(Name name, int dclass, long ttl, byte[] address) {
    super(name, Type.AAAA, dclass, ttl);
    if (address == null || address.length != 16) {
      throw new IllegalArgumentException("invalid IPv6 address");
    }
    this.address = address;
  }

  @Override
  protected void rrFromWire(DNSInput in) throws IOException {
    address = in.readByteArray(16);
  }

  @Override
  protected void rdataFromString(Tokenizer st, Name origin) throws IOException {
    address = st.getAddressBytes(Address.IPv6);
  }

  /** Converts rdata to a String */
  @Override
  protected String rrToString() {
    InetAddress addr;
    try {
      addr = InetAddress.getByAddress(null, address);
    } catch (UnknownHostException e) {
      return null;
    }
    if (addr.getAddress().length == 4) {
      // Deal with Java's broken handling of mapped IPv4 addresses.
      return "::ffff:" + addr.getHostAddress();
    }
    return addr.getHostAddress();
  }

  /** Returns the address */
  public InetAddress getAddress() {
    try {
      if (name == null) {
        return InetAddress.getByAddress(address);
      } else {
        return InetAddress.getByAddress(name.toString(), address);
      }
    } catch (UnknownHostException e) {
      return null;
    }
  }

  @Override
  protected void rrToWire(DNSOutput out, Compression c, boolean canonical) {
    out.writeByteArray(address);
  }
}
