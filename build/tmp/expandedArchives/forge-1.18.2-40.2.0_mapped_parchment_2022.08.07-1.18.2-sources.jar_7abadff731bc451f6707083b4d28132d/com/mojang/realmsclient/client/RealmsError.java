package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsError {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String errorMessage;
   private final int errorCode;

   private RealmsError(String pErrorMessage, int pErrorCode) {
      this.errorMessage = pErrorMessage;
      this.errorCode = pErrorCode;
   }

   @Nullable
   public static RealmsError parse(String pError) {
      if (Strings.isNullOrEmpty(pError)) {
         return null;
      } else {
         try {
            JsonObject jsonobject = JsonParser.parseString(pError).getAsJsonObject();
            String s = JsonUtils.getStringOr("errorMsg", jsonobject, "");
            int i = JsonUtils.getIntOr("errorCode", jsonobject, -1);
            return new RealmsError(s, i);
         } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsError: {}", (Object)exception.getMessage());
            LOGGER.error("The error was: {}", (Object)pError);
            return null;
         }
      }
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public int getErrorCode() {
      return this.errorCode;
   }
}