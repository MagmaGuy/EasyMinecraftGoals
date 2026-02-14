# Changelog

## [1.19.25] - 2026-02-14

### Added

- **FakeItem system** - Packet-based item display using ItemDisplay entities for showing items visually without being pickable by players or hoppers. Builder pattern for configuration (itemStack, billboard, scale, viewRange, glowing, customName). Ideal for coin showers, loot previews, and visual effects. Supported on Minecraft 1.21.4+ (v1_21_R3 through v1_21_R7).

- **PacketInteractionEntity** - Packet-only Interaction entity for detecting player left-click and right-click events. Invisible clickable hitbox with configurable width and height. Uses callback-based API (`setRightClickCallback` / `setLeftClickCallback`). Supported on Minecraft 1.21.1+ (v1_21_R1 through v1_21_R7).

- **PacketInteractionListener** - Netty pipeline-based packet interceptor for `ServerboundInteractPacket`. Detects player interactions with packet-only entities and dispatches events to `PacketEntityInteractionManager`.

- **PacketEntityTracker** - Global visibility manager for packet entities. Automatically shows/hides entities based on player distance using server view distance. Handles player join, quit, respawn, and world change events with proper delays. Configurable tracking range.

- **PacketEntityEventListener** - Bukkit event listener that bridges player lifecycle events (join, quit, respawn, world change) to the PacketEntityTracker.

- **TrackedPacketEntity interface** - Contract for packet entities that want automatic visibility management via PacketEntityTracker.

- **PacketEntityInteractionManager** - Central registry mapping entity IDs to packet entities for dispatching click interaction events.

### Enhanced

- **FakeText** - Added entity mounting support (`mountTo`, `dismount`, `attachTo`, `detach`) for attaching text displays to entities with automatic client-side positioning. Added dynamic property setters: `setTextOpacity`, `setScale`, `setBackgroundColor`, `setShadowed`, `setSeeThrough`, `setBillboard`. Added translation offset support via `Builder.translation(x, y, z)`. FakeTextImpl now implements TrackedPacketEntity for automatic visibility management when using `attachTo`.

- **PacketEntityInterface** - Added `mountTo(int vehicleEntityId)` and `dismount()` for entity mounting. Added `getEntityId()` accessor. Added click interaction callbacks: `setRightClickCallback`, `setLeftClickCallback`, `handleInteraction`.

- **NMSAdapter** - Added factory methods: `createPacketInteractionEntity`, `createFakeItem`, `fakeItemBuilder`, `supportsFakeItems`. Added lifecycle methods: `initializePacketInteractionListener`, `shutdownPacketInteractionListener`.

- **NMSManager** - Added `shutdown()` method for proper cleanup of PacketEntityTracker and interaction listeners during plugin disable. Automatically initializes tracker and interaction listener on startup.

- **FakeTextSettings** - Added translation offset fields (`translationX`, `translationY`, `translationZ`) with `setTranslation` and `hasTranslation` methods.

### Version-specific implementations

- FakeItemImpl added for v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7_common
- PacketInteractionEntity added for v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7_common
- PacketInteractionListener added for v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7_common
- NMSAdapter overrides for createPacketInteractionEntity and initializePacketInteractionListener in v1_21_R1 through v1_21_R7_common
- NMSAdapter overrides for createFakeItem and supportsFakeItems in v1_21_R3 through v1_21_R7_common
