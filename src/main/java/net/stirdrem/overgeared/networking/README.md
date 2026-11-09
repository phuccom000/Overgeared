# Overgeared networking (Minecraft 26.3 / Fabric API 0.161)

Every packet is a `record` in `networking/packet` implementing `CustomPacketPayload`, with a
`public static final Type<...> TYPE` and a `StreamCodec<RegistryFriendlyByteBuf, ...> STREAM_CODEC`.

- `ModMessages.register()` (common init, already called from `Overgeared#onInitialize`) registers every payload
  type with `PayloadTypeRegistry.serverboundPlay()/clientboundPlay()` and the server receivers.
- `ModMessages.registerClient()` must be called from the client entrypoint (`OvergearedClient`) to register
  the client receivers. Client-only code lives in `ModMessagesClient` (only loaded on the client).
- Fabric runs all receivers on the main (server / render) thread, so handlers touch game state directly.

## Payloads

| Payload | Dir | Id | Fields | Send helper |
|---|---|---|---|---|
| `KnappingChipC2SPacket` | C2S | `overgeared:knapping_chip` | `int index` | `ModMessages.sendKnappingChip(index)` |
| `SelectToolTypeC2SPacket` | C2S | `overgeared:select_tool_type` | `String toolTypeId, int containerId` | `ModMessages.sendSelectToolType(toolTypeId, containerId)` |
| `PacketSendCounterC2SPacket` | C2S | `overgeared:send_counter` | `BlockPos pos, String quality` | `ModMessages.sendCounter(pos, quality)` |
| `SetMinigameVisibleC2SPacket` | C2S | `overgeared:set_minigame_visible` | `BlockPos pos, boolean visible` | `ModMessages.sendSetMinigameVisible(pos, visible)` |
| `MinigameSetStartedC2SPacket` | C2S | `overgeared:minigame_set_started` | `BlockPos pos` | `ModMessages.sendMinigameSetStarted(pos)` |
| `MinigameSyncS2CPacket` | S2C (all players) | `overgeared:minigame_sync` | `CompoundTag minigameData` (`anvilOwner` stored with `UUIDUtil.CODEC`, `anvilPos` long) | `ModMessages.sendMinigameSync(server, tag)` |
| `MinigameSetStartedS2CPacket` | S2C | `overgeared:minigame_set_started_ack` | `BlockPos pos` | sent by the C2S handler |
| `StartMinigameS2CPacket` | S2C | `overgeared:start_minigame` | `BlockPos pos, int hits, String quality` | `ModMessages.sendStartMinigame(player, pos, hits, quality)` |
| `ToggleMinigameS2CPacket` | S2C | `overgeared:toggle_minigame` | `BlockPos pos, boolean visible` | `ModMessages.sendToggleMinigame(player, pos, visible)` |
| `HideMinigameS2CPacket` | S2C | `overgeared:hide_minigame` | - | `ModMessages.sendHideMinigame(player)` |
| `ResetMinigameS2CPacket` | S2C | `overgeared:reset_minigame` | `BlockPos anvilPos` | `ModMessages.sendResetMinigame(player, anvilPos)` |
| `OnlyResetMinigameS2CPacket` | S2C | `overgeared:only_reset_minigame` | - | `ModMessages.sendOnlyResetMinigame(player)` |

Generic helpers: `ModMessages.sendToPlayer(ServerPlayer, CustomPacketPayload)`,
`ModMessages.sendToAll(MinecraftServer, CustomPacketPayload)`, and (client side only)
`ModMessages.sendToServer(CustomPacketPayload)`.

The old `ModMessages.buf()` / `Identifier` channel constants / `encode`/`decode` statics are gone:
replace `var buf = ModMessages.buf(); XPacket.encode(new XPacket(...), buf); ClientModMessages.sendToServer(ModMessages.X, buf);`
with `ModMessages.sendToServer(new XPacket(...))` (or the typed helper above).
