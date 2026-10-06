# Overgeared recipes (Minecraft 26.3 port)

Contract for code that uses the recipe package (block entities, screens, events, JEI/EMI).

## General

- Recipe types / serializers: `ModRecipeTypes.<TYPE>` and `ModRecipes.<SERIALIZER>` (same constant names as 1.20.1).
  Each recipe class also has `T.SERIALIZER`, `T.MAP_CODEC`, `T.STREAM_CODEC`; the old inner `Serializer` classes
  are gone (`RecipeSerializer` is a final record in 26.3). `Type.INSTANCE` inner classes are kept.
- `ModRecipes.register()` must be called from the common initializer (it also registers
  `ModRecipeBookCategories`, which must exist before registries freeze). Every serializer is registered with
  `RecipeSynchronization.synchronizeRecipeSerializer`, so `RecipeLookup` works on the client too.
- Lookups: `RecipeLookup.firstMatch / firstMatchValue / all / allValues(level, ModRecipeTypes.X, input)`.
  Results of `firstMatch`/`all` are `RecipeHolder<T>` (`.value()`, `.id()`).
- Mod recipe types take `ItemListInput` (`ItemListInput.of(container)`, `of(container, from, to)`, `of(stacks...)`),
  index i = old Container slot i. Recipes extending vanilla classes keep the vanilla input
  (`SingleRecipeInput` for cooking, `CraftingInput` for crafting).
- `Recipe#assemble(input)` no longer takes registries. `getResultItem(RegistryAccess)` no longer exists in vanilla;
  every recipe has `getResultItem()` returning a **fresh copy**, plus a `@Deprecated getResultItem(HolderLookup.Provider)`
  overload (so `recipe.getResultItem(level.registryAccess())` still compiles). The raw `ItemStackTemplate` is
  available via `result()` (or `output()` for cooling/grinding).
- `getGroup()` -> `group()` (vanilla name); a deprecated `getGroup()` is kept where it existed.
- `Ingredient.EMPTY` does not exist in 26.3: blank/optional ingredients are `Optional<Ingredient>`
  (`Ingredient.testOptionalIngredient(opt, stack)` = vanilla "empty matches empty stack").
  `Ingredient#getItems()` is gone; use `ingredient.items()` (stream of `Holder<Item>`, deprecated but fine for
  viewers) or `ingredient.display()`.
- NBT is replaced by data components (`ModComponents`): HEATED, POLISHED, FORGING_QUALITY, CREATOR, CAST_DATA,
  BLUEPRINT_DATA, ...
- None of the mod recipe types provide vanilla `RecipeDisplay`s (`display()` is empty), so they never appear in
  the vanilla recipe book. Each reports a `ModRecipeBookCategories.*` category.
- Shared codec helpers: `RecipeCodecs` (`INGREDIENT`, `RESULT`, `FORGING_QUALITY`, `MATERIALS`, `pattern(n)`,
  `key(codec)`, `legacyNbtToPatch(CompoundTag)`), `CookingCodecs`.

### JSON format

26.3 vanilla formats: `recipe/` folder (singular); ingredients are `"ns:item"`, `"#ns:tag"`, a list of item ids,
or a Fabric custom ingredient object; results are `"ns:item"` or `{"id": ..., "count": ..., "components": {...}}`.
For datapack compatibility all **Overgeared** serializers additionally accept the 1.20.1 forms
`{"item": ...}` / `{"tag": ...}` (and lists of `{"item"}`), and results `{"item": ..., "count": ...}`
(legacy `"nbt"` on results is ignored; use `"components"`). Encoding (datagen) always writes the new format.
Field names of every mod recipe type are unchanged unless noted below.

## Recipe types

### Forging - `overgeared:forging`, `ForgingRecipe`, type `ModRecipeTypes.FORGING`
- Input: `ItemListInput` of the anvil container. Slots 0..8 = 3x3 grid (row-major), slot 11 = blueprint
  (`ForgingRecipe.BLUEPRINT_SLOT`). Other slots ignored.
- `ForgingRecipe.findBestMatch(level, ItemListInput | Container)` -> `Optional<ForgingRecipe>` (largest matching
  pattern; client + server).
- Blueprint check: reads `BLUEPRINT_DATA.toolType()` of slot 11 (was NBT "ToolType").
- Accessors: `getResultItem()`, `result()`, `hasFailedResult()`, `getFailedResultItem()` (EMPTY if none),
  `failedResult()`, `getHammeringRequired()`/`getRemainingHits()`, `getAnvilTier()` (String display name),
  `hasQuality()`, `needsMinigame()`, `hasPolishing()`, `needQuenching()`, `getMinimumQuality()`,
  `getQualityDifficulty()`, `getBlueprintTypes()` (lower-case Set), `requiresBlueprint()`, `getRecipeBookTab()`
  (`ForgingBookCategory`), `width`/`height` (public fields) + `getWidth()`/`getHeight()`, `getPattern()`, `getKey()`,
  `containsIngredient(stack)`, `showNotification()`, `group()`.
- `getForgingIngredients()` -> `List<ForgingIngredient>` (width*height, row-major; blanks are
  `ForgingIngredient.EMPTY`). **Changed:** `ForgingIngredient(Optional<Ingredient> ingredient, boolean requiresHeated,
  boolean transferNbt)`; use `isEmpty()` and `test(stack)` (requires_heated checks `overgeared:heated == true`).
- `getIngredients()` -> `List<Optional<Ingredient>>` (was `NonNullList<Ingredient>` with EMPTY).
- `assemble(input)`: result with the component patches of every `transfer_nbt` ingredient applied (ingredient
  index i read from input slot i, as in 1.20.1). The anvil block entity builds its own output.
- JSON: `pattern` (max 3x3, not trimmed), `key` values = ingredient or
  `{"ingredient": <ingredient>, "requires_heated": bool, "transfer_nbt": bool}` (1.20.1 `{"item": .., "requires_heated": ..}`
  still accepted), `result`, `result_failed`, `blueprint` (string or list), `requires_blueprint`, `tier`,
  `hammering`, `has_quality`, `needs_minigame`, `has_polishing`, `need_quenching` (default: result is not armor,
  via `minecraft:equippable` slot), `show_notification`, `minimum_quality` (**the 1.20.1 reader used
  `minimumQuality` while the datagen wrote `minimum_quality`; both are read now, `minimum_quality` is written**),
  `quality_difficulty`, `category`, `group`.

### Rock knapping - `overgeared:rock_knapping`, `RockKnappingRecipe`, `ModRecipeTypes.KNAPPING`
- Input: `ItemListInput` of exactly 9 stacks (3x3, row-major). Empty slot = chipped.
- Accessors: `getResultItem()`, `result()`, `getIngredient()`, `getPattern()` (`boolean[row][col]`, true = chipped),
  `getPatternRows()`, `getWidth()`, `getHeight()`, `isMirrored()`.

### Fletching - `overgeared:fletching`, `FletchingRecipe`, `ModRecipeTypes.FLETCHING`
- Input: `ItemListInput`: 0 = tip, 1 = shaft, 2 = feather, 3 = potion. Absent ingredient = slot accepts anything.
- **Changed:** `getTip()/getShaft()/getFeather()/getPotion()` return `Optional<Ingredient>`.
- `assemble()` / `getDefaultResult()` / `getResultItem()` = base result copy; `getTippedResult()`,
  `getLingeringResult()` (EMPTY if absent), `hasTippedResult()`, `hasLingeringResult()`, `hasPotion()`.
- `getTippedTag()`/`getLingeringTag()` still return the JSON `"tag"` strings ("Potion"/"LingeringPotion"), but in
  26.3 the screen should store the potion in `minecraft:potion_contents` and lingering in
  `overgeared:lingering_status` instead of NBT keys.

### Alloy smelting
| JSON type | class | type constant | input slots |
|---|---|---|---|
| `overgeared:alloy_smelting` | `AlloySmeltingRecipe` (shapeless, <=4 ingr.) | `ALLOY_SMELTING` | 0..3 |
| `overgeared:shaped_alloy_smelting` | `ShapedAlloySmeltingRecipe` (2x2) | `SHAPED_ALLOY_SMELTING` | 0..3 (2x2 row-major) |
| `overgeared:nether_alloy_smelting` | `NetherAlloySmeltingRecipe` (shapeless, <=9) | `NETHER_ALLOY_SMELTING` | 0..8 |
| `overgeared:shaped_nether_alloy_smelting` | `ShapedNetherAlloySmeltingRecipe` (3x3) | `SHAPED_NETHER_ALLOY_SMELTING` | 0..8 (3x3 row-major) |
- All use `ItemListInput`; `matches` returns false client-side (unchanged).
- Interfaces `IAlloyRecipe` / `INetherAlloyRecipe`: `getIngredientsList()` (**changed** to
  `List<Optional<Ingredient>>`; shapeless = all present, shaped = trimmed pattern with blanks empty),
  `getResultItem()`, `getExperience()`, `getCookingTime()` (new in the interface), `isShaped()`, `getWidth()`,
  `getHeight()`. Shapeless also has `getInputs()` (`List<Ingredient>`); shaped has `getGridSize()`. Both: `category()`.
- JSON unchanged (`ingredients` / `pattern`+`key`, `result`, `experience`, `cookingtime` default 200,
  `category`, `group`). The `util/ShapedAlloySerializerUtil` helper is no longer used.

### Cooling / grinding - `CoolingRecipe` (`COOLING_RECIPE`), `GrindingRecipe` (`GRINDING_RECIPE`)
- Input: `ItemListInput` slot 0. JSON `input`, `output`.
- Accessors: `getInput()`, `getResultItem()`, `getOutput()` (now a copy), `output()`.

### Item -> tool type - `overgeared:item_to_tooltype`, `ItemToToolTypeRecipe`, `ITEM_TO_TOOLTYPE`
- Input: `ItemListInput` slot 0. `getInput()`, `getToolType()`, `getItems()` (one stack per matched item).
  Marked `isSpecial()`. JSON `item` (ingredient), `tooltype`.

### Casting (cast furnace) - `overgeared:casting`, `CastingRecipe`, `ModRecipeTypes.CASTING`
- Input: `ItemListInput`: slot 0 = material stack (`CastingRecipe.MATERIAL_SLOT`), slot 1 = tool cast
  (`CAST_SLOT`). Matches when the cast is a `ToolCastItem` whose `CAST_DATA.toolType()` equals `getToolType()` and
  material value * count covers `getRequiredMaterials()`. Server only (false on client).
- Accessors: `getResultItem()` (plain), `result()`, `getCookingTime()`, `getExperience()`, `requiresPolishing()`,
  `getRequiredMaterials()` (`Map<String, Double>`), `getToolType()`, `getDisplayCast()` (clay cast stack with
  CAST_DATA for viewers; replaces the 1.20.1 `getIngredients()` dummy cast).
- **Changed:** `assemble()` returns the result with cast quality/polish/heated/creator applied
  (`CastRecipeHelper.applyCastToResult`) and no longer damages the cast (the 1.20.1 version read a nonexistent
  slot and returned EMPTY). Cast wear stays the block entity's job. `isSpecial()` = true (no item ingredients).
- `CastRecipeHelper`: `castData(stack)`, `hasToolType(stack, type)`, `applyCastToResult(result, cast, needPolishing)`,
  `displayCast(item, toolType, materials)`.

### Cast cooking - `overgeared:cast_smelting` / `overgeared:cast_blasting`
- `castcooking.CastSmeltingRecipe extends SmeltingRecipe` (type `minecraft:smelting`),
  `CastBlastingRecipe extends BlastingRecipe` (type `minecraft:blasting`). Vanilla furnace input
  (`SingleRecipeInput`), ingredient = clay or nether tool cast.
- Matches a cast whose CAST_DATA has the tool type, non-empty materials, amount > 0 and enough of each material.
  `assemble` returns a copy of the cast with `CAST_DATA.output` = finished part (quality/polish/heated/creator
  applied), materials cleared, heated = true, +1 damage (or the bare part if the cast breaks).
- Accessors: `getResultItem()`, `getRequiredMaterials()`/`getMaterialInputs()`, `getToolType()`,
  `requiresPolishing()`, `getDisplayCasts()` (clay + nether display stacks), plus vanilla `input()`,
  `experience()`, `cookingTime()`.
- JSON unchanged: `input` (material -> amount), `tool_type`, `result`, `experience`, `cookingtime` (200),
  `need_polishing`, `group`.

### Component-keeping cooking - `overgeared:nbt_smelting` / `overgeared:nbt_blasting`
- `NBTKeepingSmeltingRecipe extends SmeltingRecipe`, `NBTKeepingBlastingRecipe extends BlastingRecipe`.
  Result = result template with the **input's component patch** applied (1.20.1 copied the input NBT).
  Vanilla cooking JSON, `cookingtime` optional (200).

### Component-adding cooking - `overgeared:nbt_add_smelting` / `nbt_add_blasting` / `nbt_add_campfire_cooking`
- `nbtcooking.NBTSmeltingRecipe` / `NBTBlastingRecipe` / `NBTCampfireRecipe` extend the matching vanilla class
  and implement the interface `AbstractNBTCookingRecipe` (**was an abstract class**).
- Vanilla cooking JSON + optional legacy `nbt` object; known Overgeared keys (Heated, Polished, ForgingQuality,
  Creator, HeatedSince, failedResult, LingeringPotion, TippedUsed, ReducedMaxDurability, Damage, Required) become
  components, others go into `minecraft:custom_data`. `getResultTag()` (raw tag), `getResultPatch()`.
  New packs can put `components` on `result` instead.

### Crafting (type `minecraft:crafting`, input `CraftingInput`)
- `overgeared:crafting_shapeless` - `OvergearedShapelessRecipe extends ShapelessRecipe`. Passes ingredient
  FORGING_QUALITY / CREATOR to the result (downgraded per unpolished `POLISHED=false` / still `HEATED=true`
  ingredient; with the minigame disabled such ingredients block crafting and quality defaults to POOR).
  Ingredients: plain ingredient or `{"ingredient": .., "remainder": bool, "durability_decrease": int}`
  (1.20.1 `{"item": .., "remainder": ..}` accepted). `getIngredientsWithRemainder()`, `getIngredients()`,
  `getResultItem()`, `result()`. `OvergearedShapelessRecipe.Type` is deprecated (recipes are `minecraft:crafting`).
- `overgeared:crafting_cloning` - `BlueprintCloningRecipe` (empty blueprint + blueprint -> 2 blueprints, BLUEPRINT_DATA
  quality downgraded).
- `overgeared:crafting_cast` - `DynamicToolCastRecipe` (cast + materials -> cast with materials added to CAST_DATA
  via `CastData.withAddedMaterial`).
- `overgeared:crafting_initial_cast` - `ClayToolCastRecipe` (tool in centre, 4 clay / nether bricks N/E/S/W ->
  unfired / nether tool cast with CAST_DATA tool type, downgraded quality, max amount). Indexes are those of the
  trimmed `CraftingInput`; the pattern fills the whole 3x3 box so slot 4 is the centre.
- These three are stateless `CustomRecipe`s whose codecs build a fresh instance per JSON file (26.3 recipes are registry values, so a shared singleton crashes with "Adding duplicate value"); extra JSON fields such as `category`
  are ignored.

## Datagen

`datagen/ModRecipeProvider` uses the 26.3 `FabricRecipeProvider#createRecipeProvider` API; constructor
`(FabricPackOutput, CompletableFuture<HolderLookup.Provider>)` (works with `pack.addProvider(ModRecipeProvider::new)`).
Custom builders construct the recipe objects and pass them to `RecipeOutput#accept`; JSON is produced by the
serializers' MapCodecs. Ids and advancement paths are unchanged.
