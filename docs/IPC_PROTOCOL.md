# IPC Protocol

## Transport

- TCP loopback only: `127.0.0.1`.
- Default base port: 7790; if occupied, server tries the next 199 ports.
- UTF-8 JSON Lines: one flat JSON object per line, one JSON response per command.
- Maximum line length: 65,536 Java characters.
- Maximum active clients: 16; excess accepted sockets are closed.
- Per-socket read timeout: 10 seconds of inactivity.

## Supported shape

The parser accepts a JSON object whose values are strings, numbers, booleans or null. Nested objects/arrays are intentionally unsupported. Duplicate keys, malformed escapes, trailing content and invalid primitive values are rejected.

Example:

```json
{"action":"ping"}
{"action":"open","tab":1,"path":"/games/demo.jar"}
```

## Actions

Existing action names are retained: `ping`, `status`, `version`, `slots`, `keypress`, `keydown`, `keyup`, `text`, `clean_ram`, `sleep`, `hide`, `show`, `mouse`, `proxy`, `proxy_get`, `add_tab`, `open`, `screenshot`, `quit`/`close`.

## Response semantics

Most actions return `{"ok":true}` or `{"ok":false,...}`. For `open` and `add_tab`, success currently means the UI operation was accepted/enqueued, not that the game JAR finished loading. Consumers must inspect `slots`/UI/logs for final game state.

## Security

Loopback binding prevents remote network access by default, but does not authenticate local processes. Do not expose this port through a public tunnel or bind to `0.0.0.0`. Authentication remains a backlog item.
