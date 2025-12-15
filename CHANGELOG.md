### Updates & Improvements
- API Improvements:
  - Added leftRotation and rightRotation variables display entities, allowing advanced control of rotations through quaternion.
  - Added properties variable to Block Display to allow blockstate control.
- Performance Optimizations:
  - Implemented **Spatial Partitioning** to reduce visibility check complexity from O(Players × Holograms) to localized checks based on view distance.
  - Added **Packet Bundling** to reduce the number of packets sent to clients.
  - Reduced memory overhead by **Flattening Entity Maps** and utilizing optimized primitive collections.
