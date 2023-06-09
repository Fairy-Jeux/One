package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, FormattedText {
   /**
    * Gets the style of this component.
    */
   Style getStyle();

   /**
    * Gets the raw content of this component (but not its sibling components), without any formatting codes. For
    * example, this is the raw text in a {@link TextComponentString}, but it's the translated text for a {@link
    * TextComponentTranslation} and it's the score value for a {@link TextComponentScore}.
    */
   String getContents();

   default String getString() {
      return FormattedText.super.getString();
   }

   /**
    * Get the plain text of this FormattedText, without any styling or formatting codes, limited to {@code maxLength}
    * characters.
    */
   default String getString(int pMaxLength) {
      StringBuilder stringbuilder = new StringBuilder();
      this.visit((p_130673_) -> {
         int i = pMaxLength - stringbuilder.length();
         if (i <= 0) {
            return STOP_ITERATION;
         } else {
            stringbuilder.append(p_130673_.length() <= i ? p_130673_ : p_130673_.substring(0, i));
            return Optional.empty();
         }
      });
      return stringbuilder.toString();
   }

   /**
    * Gets the sibling components of this one.
    */
   List<Component> getSiblings();

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   MutableComponent plainCopy();

   /**
    * Creates a copy of this component and also copies the style and siblings. Note that the siblings are copied
    * shallowly, meaning the siblings themselves are not copied.
    */
   MutableComponent copy();

   FormattedCharSequence getVisualOrderText();

   default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style pStyle) {
      Style style = this.getStyle().applyTo(pStyle);
      Optional<T> optional = this.visitSelf(pAcceptor, style);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(Component component : this.getSiblings()) {
            Optional<T> optional1 = component.visit(pAcceptor, style);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor) {
      Optional<T> optional = this.visitSelf(pAcceptor);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(Component component : this.getSiblings()) {
            Optional<T> optional1 = component.visit(pAcceptor);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> pConsumer, Style pStyle) {
      return pConsumer.accept(pStyle, this.getContents());
   }

   default <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> pConsumer) {
      return pConsumer.accept(this.getContents());
   }

   default List<Component> toFlatList(Style pStyle) {
      List<Component> list = Lists.newArrayList();
      this.visit((p_178403_, p_178404_) -> {
         if (!p_178404_.isEmpty()) {
            list.add((new TextComponent(p_178404_)).withStyle(p_178403_));
         }

         return Optional.empty();
      }, pStyle);
      return list;
   }

   static Component nullToEmpty(@Nullable String pText) {
      return (Component)(pText != null ? new TextComponent(pText) : TextComponent.EMPTY);
   }

   public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
      private static final Gson GSON = Util.make(() -> {
         GsonBuilder gsonbuilder = new GsonBuilder();
         gsonbuilder.disableHtmlEscaping();
         gsonbuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
         gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         gsonbuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         return gsonbuilder.create();
      });
      private static final Field JSON_READER_POS = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
         }
      });
      private static final Field JSON_READER_LINESTART = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
         }
      });

      public MutableComponent deserialize(JsonElement pJson, Type pTypeOfT, JsonDeserializationContext pContext) throws JsonParseException {
         if (pJson.isJsonPrimitive()) {
            return new TextComponent(pJson.getAsString());
         } else if (!pJson.isJsonObject()) {
            if (pJson.isJsonArray()) {
               JsonArray jsonarray1 = pJson.getAsJsonArray();
               MutableComponent mutablecomponent1 = null;

               for(JsonElement jsonelement : jsonarray1) {
                  MutableComponent mutablecomponent2 = this.deserialize(jsonelement, jsonelement.getClass(), pContext);
                  if (mutablecomponent1 == null) {
                     mutablecomponent1 = mutablecomponent2;
                  } else {
                     mutablecomponent1.append(mutablecomponent2);
                  }
               }

               return mutablecomponent1;
            } else {
               throw new JsonParseException("Don't know how to turn " + pJson + " into a Component");
            }
         } else {
            JsonObject jsonobject = pJson.getAsJsonObject();
            MutableComponent mutablecomponent;
            if (jsonobject.has("text")) {
               mutablecomponent = new TextComponent(GsonHelper.getAsString(jsonobject, "text"));
            } else if (jsonobject.has("translate")) {
               String s = GsonHelper.getAsString(jsonobject, "translate");
               if (jsonobject.has("with")) {
                  JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "with");
                  Object[] aobject = new Object[jsonarray.size()];

                  for(int i = 0; i < aobject.length; ++i) {
                     aobject[i] = this.deserialize(jsonarray.get(i), pTypeOfT, pContext);
                     if (aobject[i] instanceof TextComponent) {
                        TextComponent textcomponent = (TextComponent)aobject[i];
                        if (textcomponent.getStyle().isEmpty() && textcomponent.getSiblings().isEmpty()) {
                           aobject[i] = textcomponent.getText();
                        }
                     }
                  }

                  mutablecomponent = new TranslatableComponent(s, aobject);
               } else {
                  mutablecomponent = new TranslatableComponent(s);
               }
            } else if (jsonobject.has("score")) {
               JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "score");
               if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                  throw new JsonParseException("A score component needs a least a name and an objective");
               }

               mutablecomponent = new ScoreComponent(GsonHelper.getAsString(jsonobject1, "name"), GsonHelper.getAsString(jsonobject1, "objective"));
            } else if (jsonobject.has("selector")) {
               Optional<Component> optional = this.parseSeparator(pTypeOfT, pContext, jsonobject);
               mutablecomponent = new SelectorComponent(GsonHelper.getAsString(jsonobject, "selector"), optional);
            } else if (jsonobject.has("keybind")) {
               mutablecomponent = new KeybindComponent(GsonHelper.getAsString(jsonobject, "keybind"));
            } else {
               if (!jsonobject.has("nbt")) {
                  throw new JsonParseException("Don't know how to turn " + pJson + " into a Component");
               }

               String s1 = GsonHelper.getAsString(jsonobject, "nbt");
               Optional<Component> optional1 = this.parseSeparator(pTypeOfT, pContext, jsonobject);
               boolean flag = GsonHelper.getAsBoolean(jsonobject, "interpret", false);
               if (jsonobject.has("block")) {
                  mutablecomponent = new NbtComponent.BlockNbtComponent(s1, flag, GsonHelper.getAsString(jsonobject, "block"), optional1);
               } else if (jsonobject.has("entity")) {
                  mutablecomponent = new NbtComponent.EntityNbtComponent(s1, flag, GsonHelper.getAsString(jsonobject, "entity"), optional1);
               } else {
                  if (!jsonobject.has("storage")) {
                     throw new JsonParseException("Don't know how to turn " + pJson + " into a Component");
                  }

                  mutablecomponent = new NbtComponent.StorageNbtComponent(s1, flag, new ResourceLocation(GsonHelper.getAsString(jsonobject, "storage")), optional1);
               }
            }

            if (jsonobject.has("extra")) {
               JsonArray jsonarray2 = GsonHelper.getAsJsonArray(jsonobject, "extra");
               if (jsonarray2.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int j = 0; j < jsonarray2.size(); ++j) {
                  mutablecomponent.append(this.deserialize(jsonarray2.get(j), pTypeOfT, pContext));
               }
            }

            mutablecomponent.setStyle(pContext.deserialize(pJson, Style.class));
            return mutablecomponent;
         }
      }

      private Optional<Component> parseSeparator(Type pType, JsonDeserializationContext pJsonContext, JsonObject pJsonObject) {
         return pJsonObject.has("separator") ? Optional.of(this.deserialize(pJsonObject.get("separator"), pType, pJsonContext)) : Optional.empty();
      }

      private void serializeStyle(Style pStyle, JsonObject pObject, JsonSerializationContext pCtx) {
         JsonElement jsonelement = pCtx.serialize(pStyle);
         if (jsonelement.isJsonObject()) {
            JsonObject jsonobject = (JsonObject)jsonelement;

            for(Entry<String, JsonElement> entry : jsonobject.entrySet()) {
               pObject.add(entry.getKey(), entry.getValue());
            }
         }

      }

      public JsonElement serialize(Component pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         if (!pSrc.getStyle().isEmpty()) {
            this.serializeStyle(pSrc.getStyle(), jsonobject, pContext);
         }

         if (!pSrc.getSiblings().isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Component component : pSrc.getSiblings()) {
               jsonarray.add(this.serialize(component, component.getClass(), pContext));
            }

            jsonobject.add("extra", jsonarray);
         }

         if (pSrc instanceof TextComponent) {
            jsonobject.addProperty("text", ((TextComponent)pSrc).getText());
         } else if (pSrc instanceof TranslatableComponent) {
            TranslatableComponent translatablecomponent = (TranslatableComponent)pSrc;
            jsonobject.addProperty("translate", translatablecomponent.getKey());
            if (translatablecomponent.getArgs() != null && translatablecomponent.getArgs().length > 0) {
               JsonArray jsonarray1 = new JsonArray();

               for(Object object : translatablecomponent.getArgs()) {
                  if (object instanceof Component) {
                     jsonarray1.add(this.serialize((Component)object, object.getClass(), pContext));
                  } else {
                     jsonarray1.add(new JsonPrimitive(String.valueOf(object)));
                  }
               }

               jsonobject.add("with", jsonarray1);
            }
         } else if (pSrc instanceof ScoreComponent) {
            ScoreComponent scorecomponent = (ScoreComponent)pSrc;
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("name", scorecomponent.getName());
            jsonobject1.addProperty("objective", scorecomponent.getObjective());
            jsonobject.add("score", jsonobject1);
         } else if (pSrc instanceof SelectorComponent) {
            SelectorComponent selectorcomponent = (SelectorComponent)pSrc;
            jsonobject.addProperty("selector", selectorcomponent.getPattern());
            this.serializeSeparator(pContext, jsonobject, selectorcomponent.getSeparator());
         } else if (pSrc instanceof KeybindComponent) {
            KeybindComponent keybindcomponent = (KeybindComponent)pSrc;
            jsonobject.addProperty("keybind", keybindcomponent.getName());
         } else {
            if (!(pSrc instanceof NbtComponent)) {
               throw new IllegalArgumentException("Don't know how to serialize " + pSrc + " as a Component");
            }

            NbtComponent nbtcomponent = (NbtComponent)pSrc;
            jsonobject.addProperty("nbt", nbtcomponent.getNbtPath());
            jsonobject.addProperty("interpret", nbtcomponent.isInterpreting());
            this.serializeSeparator(pContext, jsonobject, nbtcomponent.separator);
            if (pSrc instanceof NbtComponent.BlockNbtComponent) {
               NbtComponent.BlockNbtComponent nbtcomponent$blocknbtcomponent = (NbtComponent.BlockNbtComponent)pSrc;
               jsonobject.addProperty("block", nbtcomponent$blocknbtcomponent.getPos());
            } else if (pSrc instanceof NbtComponent.EntityNbtComponent) {
               NbtComponent.EntityNbtComponent nbtcomponent$entitynbtcomponent = (NbtComponent.EntityNbtComponent)pSrc;
               jsonobject.addProperty("entity", nbtcomponent$entitynbtcomponent.getSelector());
            } else {
               if (!(pSrc instanceof NbtComponent.StorageNbtComponent)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + pSrc + " as a Component");
               }

               NbtComponent.StorageNbtComponent nbtcomponent$storagenbtcomponent = (NbtComponent.StorageNbtComponent)pSrc;
               jsonobject.addProperty("storage", nbtcomponent$storagenbtcomponent.getId().toString());
            }
         }

         return jsonobject;
      }

      private void serializeSeparator(JsonSerializationContext pContext, JsonObject pJson, Optional<Component> pSeparator) {
         pSeparator.ifPresent((p_178410_) -> {
            pJson.add("separator", this.serialize(p_178410_, p_178410_.getClass(), pContext));
         });
      }

      /**
       * Serializes a component into JSON.
       */
      public static String toJson(Component pComponent) {
         return GSON.toJson(pComponent);
      }

      public static JsonElement toJsonTree(Component pComponent) {
         return GSON.toJsonTree(pComponent);
      }

      @Nullable
      public static MutableComponent fromJson(String pJson) {
         return GsonHelper.fromJson(GSON, pJson, MutableComponent.class, false);
      }

      @Nullable
      public static MutableComponent fromJson(JsonElement pJson) {
         return GSON.fromJson(pJson, MutableComponent.class);
      }

      @Nullable
      public static MutableComponent fromJsonLenient(String pJson) {
         return GsonHelper.fromJson(GSON, pJson, MutableComponent.class, true);
      }

      public static MutableComponent fromJson(com.mojang.brigadier.StringReader pReader) {
         try {
            JsonReader jsonreader = new JsonReader(new StringReader(pReader.getRemaining()));
            jsonreader.setLenient(false);
            MutableComponent mutablecomponent = GSON.getAdapter(MutableComponent.class).read(jsonreader);
            pReader.setCursor(pReader.getCursor() + getPos(jsonreader));
            return mutablecomponent;
         } catch (StackOverflowError | IOException ioexception) {
            throw new JsonParseException(ioexception);
         }
      }

      private static int getPos(JsonReader pReader) {
         try {
            return JSON_READER_POS.getInt(pReader) - JSON_READER_LINESTART.getInt(pReader) + 1;
         } catch (IllegalAccessException illegalaccessexception) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
         }
      }
   }
}