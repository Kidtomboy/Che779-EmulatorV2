# Security Notes

## Protections in this revision

- IPC binds explicitly to `127.0.0.1`.
- At most 16 client sockets are served concurrently.
- Each line is capped at 64 KiB and socket reads time out after 10 seconds of inactivity.
- IPC parser rejects malformed JSON, duplicate keys, nested JSON values and trailing garbage.

## Known limitations

- No authentication token: another local process running as the same user may connect and issue commands, including input injection or quit.
- IPC commands can manipulate emulator state. Keep the port local; do not expose it via LAN, reverse proxy or tunnel.
- Proxy passwords may be stored in plaintext configuration (`tab_proxies.txt`); filesystem ACLs/encryption need OS-specific review.
- Bundled ASM 3.2 license metadata is not a license grant. Verify upstream license and preserve required notices before public redistribution.
- JNA native integration depends on external `jna.jar`/native binaries and platform permissions.

## Disclosure

This document is not a security certification. It records observed behavior and remaining work for maintainers.
