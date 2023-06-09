package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;

public class AddNewChoices extends DataFix {
   private final String name;
   private final TypeReference type;

   public AddNewChoices(Schema pOutputSchema, String pName, TypeReference pType) {
      super(pOutputSchema, true);
      this.name = pName;
      this.type = pType;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(this.type);
      TaggedChoiceType<?> taggedchoicetype1 = this.getOutputSchema().findChoiceType(this.type);
      return this.cap(this.name, taggedchoicetype, taggedchoicetype1);
   }

   protected final <K> TypeRewriteRule cap(String pName, TaggedChoiceType<K> p_14639_, TaggedChoiceType<?> p_14640_) {
      if (p_14639_.getKeyType() != p_14640_.getKeyType()) {
         throw new IllegalStateException("Could not inject: key type is not the same");
      } else {
         return this.fixTypeEverywhere(pName, p_14639_, (TaggedChoiceType<K>)p_14640_, (p_14636_) -> {
            return (p_145061_) -> {
               if (!((TaggedChoiceType<K>)p_14640_).hasType(p_145061_.getFirst())) {
                  throw new IllegalArgumentException(String.format("Unknown type %s in %s ", p_145061_.getFirst(), this.type));
               } else {
                  return p_145061_;
               }
            };
         });
      }
   }
}