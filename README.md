# rpcnis
A general purpose java RPC library, plugs in like magic

# Design considerations
### To queue or not to queue
The library itself is/should be unopinionated about transport and thus execution.
If you were to use a loopback or redis transport with multiple listeners, then invocations will be ran in parallel on all listeners, where the first one to acknowledge the result will be the one to return it to the caller.
This might not be what you want, so you can use a queue transport, which will ensure that only one listener will receive the invocation, and that the result will be returned to the caller by the same listener.

### To serialize or not to serialize
The library itself is/should be unopinionated about serialization.
GSON gets used by default, but you can use any other serialization library you want, as long as it can serialize and deserialize generic types with another fallback method for unknown types.


# Notes
- The serializer is only used for argument and reponse values, not for the methods, classes or internal interfaces themselves.