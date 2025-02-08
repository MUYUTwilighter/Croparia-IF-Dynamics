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
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public record EfrSerializer(
    int itemEntrySize, int fluidEntrySize, int itemResultSize, int fluidResultSize
) implements RecipeSerializer<ElemForgeRecipe> {
    @Override
    public @NotNull ElemForgeRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation recipeTypeId = ResourceLocation.CODEC.decode(JsonOps.INSTANCE, json.get("type")).getOrThrow(
            false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode recipe type: %s".formatted(msg))
        ).getFirst();
        EfrType recipeType = (EfrType) BuiltInRegistries.RECIPE_TYPE.get(recipeTypeId);
        if (recipeType == null) throw new IllegalArgumentException("Unknown recipe type: " + recipeTypeId);
        List<ItemEntry> itemEntries = readList(json.getAsJsonArray("itemEntries"), ItemEntry.CODEC, this.itemEntrySize());
        List<FluidEntry> fluidEntries = readList(json.getAsJsonArray("fluidEntries"), FluidEntry.CODEC, this.fluidEntrySize());
        List<ItemResult> itemResults = readList(json.getAsJsonArray("itemResults"), ItemResult.CODEC, this.itemResultSize());
        List<FluidResult> fluidResults = readList(json.getAsJsonArray("fluidResults"), FluidResult.CODEC, this.fluidResultSize());
        int duration = json.get("duration").getAsInt();
        int fuel = json.get("fuel").getAsInt();
        return new ElemForgeRecipe(id, recipeType, this, itemEntries, fluidEntries, itemResults, fluidResults, duration, fuel);
    }

    public <E> List<E> readList(@Nullable JsonElement json, MapCodec<E> elemCodec, int maxSize) {
        List<E> result = Collections.emptyList();
        if (json == null) {
            return result;
        }
        if (this.itemEntrySize() > 0) {
            result = elemCodec.codec().listOf().decode(JsonOps.INSTANCE, json).getOrThrow(
                false, msg -> CropariaIfDynamics.LOGGER.error("Failed to decode item entries: %s".formatted(msg))
            ).getFirst();
        }
        if (result.size() > maxSize) {
            throw new IllegalArgumentException("Entry size %s exceeds the limit %s".formatted(result.size(), this.itemEntrySize()));
        }
        return result;
    }

    @Override
    public @NotNull ElemForgeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation recipeTypeId = friendlyByteBuf.readResourceLocation();
        EfrType recipeType = (EfrType) BuiltInRegistries.RECIPE_TYPE.get(recipeTypeId);
        if (recipeType == null) throw new IllegalArgumentException("Unknown recipe type: " + recipeTypeId);
        List<ItemEntry> itemEntries = this.itemEntrySize() > 0 ? friendlyByteBuf.readJsonWithCodec(ItemEntry.CODEC.codec().listOf()) : Collections.emptyList();
        List<FluidEntry> fluidEntries = this.fluidEntrySize() > 0 ? friendlyByteBuf.readJsonWithCodec(FluidEntry.CODEC.codec().listOf()) : Collections.emptyList();
        List<ItemResult> itemResults = this.itemResultSize() > 0 ? friendlyByteBuf.readJsonWithCodec(ItemResult.CODEC.codec().listOf()) : Collections.emptyList();
        List<FluidResult> fluidResults = this.fluidResultSize() > 0 ? friendlyByteBuf.readJsonWithCodec(FluidResult.CODEC.codec().listOf()) : Collections.emptyList();
        int duration = friendlyByteBuf.readInt();
        int fuel = friendlyByteBuf.readInt();
        return new ElemForgeRecipe(id, recipeType, this, itemEntries, fluidEntries, itemResults, fluidResults, duration, fuel);
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
