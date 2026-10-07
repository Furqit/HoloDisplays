### Updates & Improvements
- Updated to support 26.3
- Major performance pass on the tick path (visibility caching, placeholder/animation caching, bundled packets)
- Config and entity tracking memory is now bounded and cleaned up on reload/stop
- Fixed async chunk/entity access that tripped Async Catcher
- Fixed billboard reset command affecting rotation instead
- Fixed `updateBackground` crash when opacity was omitted
- Fixed hologram commands NPE-ing on missing holograms
- Fixed animation interval 0, invalid animation names, and invalid world/lookup entries not updating or crashing
- Fixed reload wiping configs when a file failed to parse
- Fixed display conditions not updating live while a hologram is visible

