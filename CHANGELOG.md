### Updates & Improvements
- Added item and block display animations (linear/sine interpolation, smooth client-side rendering)
- Added support for animation controls in the API for all display types
- Ships a built-in drop_item animation replicating vanilla item drops
- Optimized condition evaluation (player-independent fast path, single-lookup parse cache)
- Added builder-based hologram registration and text-only display updates to the API
- API-registered holograms now warn on missing referenced displays instead of failing registration
- API state hardened against off-thread calls (PR #8 by Shmeeb)
