package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsForceVBOFix extends DataFix {
   public OptionsForceVBOFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsForceVBOFix", this.getInputSchema().getType(References.OPTIONS), (p_16623_) -> {
         return p_16623_.update(DSL.remainderFinder(), (p_145572_) -> {
            return p_145572_.set("useVbo", p_145572_.createString("true"));
         });
      });
   }
}