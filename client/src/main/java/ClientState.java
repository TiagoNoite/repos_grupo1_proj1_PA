/**
 * Each state that the client can have:
 * WAIT: clients are waiting from the server's response
 * OK: clients receive approval from the server to entry, leaving the client's waiting list
 * END: client notify the server that they finish their process
 */
enum ClientState {
    WAIT,
    OK,
    END
}
