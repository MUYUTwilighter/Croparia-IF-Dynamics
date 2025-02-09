package cool.muyucloud.croparia.dynamics.api.core.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;
import cool.muyucloud.croparia.dynamics.api.core.recipe.ElemForgeRecipe;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.FluidResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemEntry;
import cool.muyucloud.croparia.dynamics.api.core.recipe.entry.ItemResult;
import cool.muyucloud.croparia.dynamics.api.core.recipe.type.EfrType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class EfrSerializer implements RecipeSerializer<ElemForgeRecipe> {
    public static final EfrSerializer INSTANCE = new EfrSerializer();

    @Override
    public @NotNull ElemForgeRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation recipeTypeId = ResourceLocation.CODEC.decode(JsonOps.INSTANCE, json.get("type")).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode recipe type: %s".formatted(msg))
        ).getFirst();
        EfrType recipeType = (EfrType) BuiltInRegistries.RECIPE_TYPE.get(recipeTypeId);
        if (recipeType == null) throw new IllegalArgumentException("Unknown recipe type: " + recipeTypeId);
        List<ItemEntry> itemInputs = readList(json.getAsJsonArray("item_inputs"), ItemEntry.CODEC, recipeType.itemInputSize());
        List<FluidEntry> fluidInputs = readList(json.getAsJsonArray("fluid_inputs"), FluidEntry.CODEC, recipeType.fluidInputSize());
        List<ItemResult> itemOutputs = readList(json.getAsJsonArray("item_outputs"), ItemResult.CODEC, recipeType.itemOutputSize());
        List<FluidResult> fluidOutputs = readList(json.getAsJsonArray("fluid_outputs"), FluidResult.CODEC, recipeType.fluidOutputSize());
        int duration = json.get("duration").getAsInt();
        int fuel = GsonHelper.getAsInt(json, "fuel", 0);
        return new ElemForgeRecipe(id, recipeType, this, itemInputs, fluidInputs, itemOutputs, fluidOutputs, duration, fuel);
    }

    public <E> List<E> readList(@Nullable JsonElement json, MapCodec<E> elemCodec, int maxSize) {
        List<E> result = Collections.emptyList();
        if (json == null) {
            return result;
        }
        if (maxSize > 0) {
            result = elemCodec.codec().listOf().decode(JsonOps.INSTANCE, json).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode item entries: %s".formatted(msg))
            ).getFirst();
        }
        if (result.size() > maxSize) {
            throw new IllegalArgumentException("Entry size %s exceeds the limit %s".formatted(result.size(), maxSize));
        }
        return result;
    }

    @Override
    public @NotNull ElemForgeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation recipeTypeId = friendlyByteBuf.readResourceLocation();
        EfrType recipeType = (EfrType) BuiltInRegistries.RECIPE_TYPE.get(recipeTypeId);
        if (recipeType == null) throw new IllegalArgumentException("Unknown recipe type: " + recipeTypeId);
        List<ItemEntry> itemInputs = recipeType.itemInputSize() > 0 ? friendlyByteBuf.readJsonWithCodec(ItemEntry.CODEC.codec().listOf()) : Collections.emptyList();
        List<FluidEntry> fluidInputs = recipeType.fluidInputSize() > 0 ? friendlyByteBuf.readJsonWithCodec(FluidEntry.CODEC.codec().listOf()) : Collections.emptyList();
        List<ItemResult> itemOutputs = recipeType.itemOutputSize() > 0 ? friendlyByteBuf.readJsonWithCodec(ItemResult.CODEC.codec().listOf()) : Collections.emptyList();
        List<FluidResult> fluidOutputs = recipeType.fluidOutputSize() > 0 ? friendlyByteBuf.readJsonWithCodec(FluidResult.CODEC.codec().listOf()) : Collections.emptyList();
        int duration = friendlyByteBuf.readInt();
        int fuel = friendlyByteBuf.readInt();
        return new ElemForgeRecipe(id, recipeType, this, itemInputs, fluidInputs, itemOutputs, fluidOutputs, duration, fuel);
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, ElemForgeRecipe recipe) {
        friendlyByteBuf.writeResourceLocation(recipe.getType().id());
        friendlyByteBuf.writeJsonWithCodec(ItemEntry.CODEC.codec().listOf(), recipe.getItemEntries());
        friendlyByteBuf.writeJsonWithCodec(FluidEntry.CODEC.codec().listOf(), recipe.getFluidEntries());
        friendlyByteBuf.writeJsonWithCodec(ItemResult.CODEC.codec().listOf(), recipe.getItemResults());
        friendlyByteBuf.writeJsonWithCodec(FluidResult.CODEC.codec().listOf(), recipe.getFluidResults());
        friendlyByteBuf.writeInt(recipe.getDuration());
    }
}
