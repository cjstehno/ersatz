# v4.0 Work Notes
- web socket support documentation & site features
- resolve the groovy ws support methods
- refactor the ws client into a reusable form (extension?)
- better mismatch and verification reporting 

# Changes & Notes 
- removed the standalone proxy - use forwarding
- deprecated the verify methods with timeout - use new WaitFor versions
- updated dependencies (groovy 4.0)
- added the ws support back in (see below) - rudimentary (create issue if need more)
- Note that ws does not support secure web sockets `wss://` connections - if this is a desired feature, please file and issue